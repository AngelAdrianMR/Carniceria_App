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
import com.carniceria.shared.shared.models.utils.ProductEmpresa
import com.carniceria.shared.shared.models.utils.PromocionConProductos
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import com.carniceria.shared.shared.models.utils.SupabaseService
import com.carniceria.shared.shared.models.utils.obtenerPerfilUsuarioActual
import io.github.jan.supabase.auth.auth

class CarritoViewModel(
    private val supabaseService: SupabaseService = SupabaseService(SupabaseProvider.client)
) : ViewModel() {

    var carrito = mutableStateListOf<CarritoItem>()
        private set

    var codigoDescuento by mutableStateOf<String?>(null)
    var descuentoAplicado by mutableStateOf(0.0)

    // ✅ Dirección seleccionada para el envío (la setea CarritoLateral)
    var codigoPostalSeleccionado by mutableStateOf<String?>(null)
    var direccionSeleccionadaTexto by mutableStateOf<String?>(null)
    var tituloDireccionSeleccionada by mutableStateOf<String?>(null)

    fun agregarAlCarrito(
        producto: Product,
        cantidad: Double,
        mensaje: String? = null,
    ): Boolean {
        if (producto.unidad_medida.equals("Kilo", ignoreCase = true) && cantidad < 0.5) {
            println("❌ No se puede añadir menos de 0.5 kg del producto ${producto.nombre_producto}")
            return false
        }

        val index = carrito.indexOfFirst { it.producto?.id == producto.id }

        if (index >= 0) {
            carrito[index] = carrito[index].copy(
                cantidad = carrito[index].cantidad + cantidad,
                mensaje = mensaje
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

        val eliminado = carrito.remove(item)

        if (!eliminado) {
            carrito.removeIf {
                when {
                    item.producto != null && it.producto != null ->
                        it.producto?.id == item.producto?.id

                    item.promocion != null && it.promocion != null ->
                        it.promocion?.promocion?.id == item.promocion?.promocion?.id &&
                                it.promocion?.promocion?.nombre_promocion == item.promocion?.promocion?.nombre_promocion

                    else -> false
                }
            }
        }

        guardarCarritoLocal(context)
        carrito = carrito.toMutableStateList()
    }

    fun agregarPromocionAlCarrito(promocionConProductos: PromocionConProductos, context: Context): Boolean {
        val promo = promocionConProductos.promocion
        val precioSinIva = promo.precio_total?.div(1.21) ?: 0.0

        val productoPromo = Product(
            id = null,
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

    suspend fun confirmarRecogidaEnTienda(usuarioId: String?, context: Context): Long? {
        return confirmarPedido(usuarioId, "Recogida", context)
    }

    suspend fun confirmarEnvio(usuarioId: String?, context: Context): Long? {
        return confirmarPedido(usuarioId, "Envio", context)
    }

    suspend fun confirmarPedido(
        usuarioId: String?,
        tipoEntrega: String,
        context: Context
    ): Long? {
        return try {
            val usuarioIdReal = SupabaseProvider.client.auth.currentSessionOrNull()?.user?.id
            if (usuarioIdReal == null) {
                println("❌ No hay usuario autenticado")
                return null
            }

            val pedidoId = supabaseService.crearPedidoConDescuento(
                usuarioIdReal,
                carrito,
                tipoEntrega,
                codigoDescuento,
                descuentoAplicado
            )

            if (pedidoId != null) {
                // ✅ Detectar automáticamente empresa/cliente desde PerfilUsuario
                val perfil = runCatching { obtenerPerfilUsuarioActual() }.getOrNull()

                val rol = perfil?.rol?.lowercase() // "empresa" o "cliente"
                val nombre = perfil?.nombre_completo?.takeIf { it.isNotBlank() }

                val sujeto = if (rol == "empresa") "Empresa" else "Cliente"
                val sujetoConNombre = if (nombre != null) "$sujeto ($nombre)" else sujeto

                notificarAdmins(
                    titulo = "Nuevo pedido recibido 🛒",
                    cuerpo = "$sujetoConNombre ha realizado un nuevo pedido (#$pedidoId y tipo $tipoEntrega)."
                )
            }

            // limpiar carrito local
            carrito.clear()
            guardarCarritoLocal(context)

            // reset dirección + descuento
            codigoPostalSeleccionado = null
            direccionSeleccionadaTexto = null
            tituloDireccionSeleccionada = null
            codigoDescuento = null
            descuentoAplicado = 0.0

            pedidoId
        } catch (e: Exception) {
            println("❌ Error confirmando pedido: ${e.message}")
            null
        }
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

    fun agregarProductoEmpresaAlCarrito(
        producto: ProductEmpresa,
        cantidad: Double,
        mensaje: String? = null
    ): Boolean {
        if (producto.unidad_medida.equals("Kilo", ignoreCase = true) && cantidad < 0.5) {
            println("❌ No se puede añadir menos de 0.5 kg del producto ${producto.nombre_producto}")
            return false
        }

        val index = carrito.indexOfFirst { it.producto?.id == producto.id }

        if (index >= 0) {
            carrito[index] = carrito[index].copy(
                cantidad = carrito[index].cantidad + cantidad,
                mensaje = mensaje
            )
        } else {
            val productAdaptado = Product(
                id = producto.id,
                nombre_producto = producto.nombre_producto,
                descripcion_producto = producto.descripcion_producto,
                categoria_producto = producto.categoria_producto!!,
                precio_venta = producto.precio_final,
                imagen_producto = producto.imagen_producto,
                unidad_medida = producto.unidad_medida,
                stock_producto = producto.stock_producto,
                precio_sin_iva = producto.precio_final / 1.21
            )

            carrito.add(
                CarritoItem(
                    producto = productAdaptado,
                    cantidad = cantidad,
                    mensaje = mensaje
                )
            )
        }

        println("✅ Producto empresa añadido: ${producto.nombre_producto} x$cantidad (precio: ${producto.precio_final})")
        return true
    }
}
