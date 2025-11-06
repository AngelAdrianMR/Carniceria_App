package com.example.carniceria_app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.carniceria.shared.shared.models.utils.CarritoItem
import com.carniceria.shared.shared.models.utils.Product
import com.carniceria.shared.shared.models.utils.PromocionConProductos
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import com.carniceria.shared.shared.models.utils.SupabaseService

class CarritoViewModel(
    private val supabaseService: SupabaseService = SupabaseService(SupabaseProvider.client)
) : ViewModel() {
    var carrito = mutableStateListOf<CarritoItem>()
        private set
    var codigoDescuento by mutableStateOf<String?>(null)
    var descuentoAplicado by mutableStateOf(0.0)

    /**
     * Añade un producto al carrito sin modificar stock todavía.
     * El stock se descuenta solo cuando se confirma el pedido.
     */
    fun agregarAlCarrito(
        producto: Product,
        cantidad: Double,
        mensaje: String? = null,
    ): Boolean {
        // ⚖️ Validar cantidad mínima si el producto se vende por kilos
        if (producto.unidad_medida.equals("Kilo", ignoreCase = true) && cantidad < 0.5) {
            println("❌ No se puede añadir menos de 0.5 kg del producto ${producto.nombre_producto}")
            return false
        }

        val index = carrito.indexOfFirst { it.producto?.id == producto.id }

        if (index >= 0) {
            carrito[index] = carrito[index].copy(
                cantidad = carrito[index].cantidad + cantidad,
                mensaje = mensaje // 👈 sobrescribimos si añade mensaje
            )
        } else {
            carrito.add(
                CarritoItem(
                    producto = producto,
                    cantidad = cantidad,
                    mensaje = mensaje
                )
            )
        }

        return true
    }

    fun guardarCarritoLocal(context: Context) {
        CarritoStorageManager.guardarCarrito(context, carrito)
    }

    fun cargarCarritoLocal(context: Context) {
        val cargado = CarritoStorageManager.cargarCarrito(context)
        carrito.clear()
        carrito.addAll(cargado)
    }

    fun eliminarProducto(item: CarritoItem, context: Context) {
        println("🗑️ Intentando eliminar: ${item.producto?.nombre_producto ?: item.promocion?.promocion?.nombre_promocion}")

        // 🔹 Intentamos primero eliminar por referencia exacta (misma instancia)
        val eliminado = carrito.remove(item)

        // 🔹 Si no se eliminó, intentamos por ID de producto o promoción
        if (!eliminado) {
            carrito.removeIf {
                when {
                    item.producto != null && it.producto != null ->
                        it.producto?.id == item.producto?.id
                    item.promocion != null && it.promocion != null ->
                        // Compara por ID y además por nombre de promoción para distinguir duplicadas
                        it.promocion?.promocion?.id == item.promocion?.promocion?.id &&
                                it.promocion?.promocion?.nombre_promocion == item.promocion?.promocion?.nombre_promocion
                    else -> false
                }
            }
        }

        guardarCarritoLocal(context)
        carrito = carrito.toMutableStateList() // 🔄 refrescar Compose
    }

    fun agregarPromocionAlCarrito(promocionConProductos: PromocionConProductos, context: Context): Boolean {
        val promo = promocionConProductos.promocion
        val precioSinIva = promo.precio_total?.div(1.21) ?: 0.0

        val productoPromo = Product(
            id = null, // 👉 no usamos id_producto porque es una promo
            nombre_producto = "Promo: ${promo.nombre_promocion}",
            descripcion_producto = promo.descripcion_promocion,
            imagen_producto = promo.imagen_promocion,
            precio_venta = promo.precio_total,
            precio_sin_iva = precioSinIva,
            unidad_medida = "Unidad",
            categoria_producto = "Promoción",
            stock_producto = 1.00
        )

        val index = carrito.indexOfFirst { it.promocion?.promocion?.id == promo.id }
        if (index >= 0) {
            carrito[index] = carrito[index].copy(cantidad = carrito[index].cantidad + 1)
        } else {
            carrito.add(CarritoItem(promocion = promocionConProductos, producto = productoPromo, cantidad = 1.00))
        }

        guardarCarritoLocal(context)
        return true
    }


    /**
     * Confirma un pedido con recogida en tienda:
     * - Actualiza stock en Supabase para cada producto real.
     * - Limpia el carrito local.
     */
    suspend fun confirmarRecogidaEnTienda(usuarioId: String?, context: Context): Long? {
        return confirmarPedido(usuarioId, "Recogida", context)
    }


    suspend fun confirmarPedido(usuarioId: String?, tipoEntrega: String, context: Context): Long? {
        return try {
            val pedidoId = supabaseService.crearPedidoConDescuento(usuarioId, carrito, tipoEntrega, codigoDescuento, descuentoAplicado)
            println("✅ Pedido creado con ID $pedidoId y tipo $tipoEntrega")

            if (pedidoId != null) {
                notificarAdmins(
                    titulo = "Nuevo pedido recibido 🛒",
                    cuerpo = "Un cliente ha realizado un nuevo pedido (#$pedidoId y tipo $tipoEntrega)."
                )
            }

            // limpiar carrito local
            carrito.clear()
            guardarCarritoLocal(context)

            pedidoId
        } catch (e: Exception) {
            println("❌ Error confirmando pedido: ${e.message}")
            null
        }
    }

    suspend fun confirmarEnvio(usuarioId: String?, context: Context): Long? {
        return confirmarPedido(usuarioId , "Envio", context)
    }

    suspend fun aplicarCodigo(codigo: String): Boolean {
        val valido = supabaseService.validarCodigo(codigo)
        return if (valido != null) {
            codigoDescuento = valido.codigo
            descuentoAplicado = if (valido.tipo == "porcentaje") {
                carrito.sumOf { item ->
                    (item.producto?.precio_venta ?: item.promocion?.promocion?.precio_total ?: 0.0) * item.cantidad
                } * (valido.valor / 100.0)
            } else {
                valido.valor
            }
            true
        } else {
            codigoDescuento = null
            descuentoAplicado = 0.0
            false
        }
    }

}
