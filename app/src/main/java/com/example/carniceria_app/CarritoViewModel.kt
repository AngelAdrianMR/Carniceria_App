package com.example.carniceria_app

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.carniceria.shared.shared.models.utils.CarritoItem
import com.carniceria.shared.shared.models.utils.Product
import com.carniceria.shared.shared.models.utils.PromocionConProductos
import com.carniceria.shared.shared.models.utils.actualizarStockProducto

class CarritoViewModel : ViewModel() {
    var carrito = mutableStateListOf<CarritoItem>()
        private set

    /**
     * Añade un producto al carrito sin modificar stock todavía.
     * El stock se descuenta solo cuando se confirma el pedido.
     */
    fun agregarAlCarrito(producto: Product, cantidad: Int): Boolean {
        val index = carrito.indexOfFirst { it.producto.id == producto.id }

        if (index >= 0) {
            carrito[index] = carrito[index].copy(cantidad = carrito[index].cantidad + cantidad)
        } else {
            carrito.add(CarritoItem(producto, cantidad))
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

    fun eliminarProducto(productoId: Long, context: Context) {
        carrito.removeIf { it.producto.id == productoId }
        guardarCarritoLocal(context)
    }

    fun agregarPromocionAlCarrito(promocion: PromocionConProductos, context: Context): Boolean {
        val promo = promocion.promocion
        val precioSinIva = promo.precio_total?.div(1.21) ?: 0.0

        // Si el id es null, generamos un id negativo fijo para no romper el carrito
        val promoId = promo.id ?: 0L

        val productoPromo = Product(
            id = -promoId,  // ID negativo para diferenciar promos
            nombre_producto = "Promo: ${promo.nombre_promocion}",
            descripcion_producto = promo.descripcion_promocion,
            imagen_producto = promo.imagen_promocion,
            precio_venta = promo.precio_total,
            precio_sin_iva = precioSinIva,
            unidad_medida = "Unidad",
            categoria_producto = "Promoción",
            stock_producto = 1
        )

        val añadido = agregarAlCarrito(productoPromo, 1)

        if (añadido) {
            guardarCarritoLocal(context)
        }

        return añadido
    }

    /**
     * Confirma un pedido con recogida en tienda:
     * - Actualiza stock en Supabase para cada producto real.
     * - Limpia el carrito local.
     */
    suspend fun confirmarRecogidaEnTienda(context: Context) {
        try {
            for (item in carrito) {
                val productoId = item.producto.id
                if (productoId != null && productoId > 0) { // 👈 comprobamos null + valor
                    val nuevoStock = (item.producto.stock_producto ?: 0) - item.cantidad
                    if (nuevoStock >= 0) {
                        val actualizado = actualizarStockProducto(productoId, nuevoStock)
                        println("🔄 Stock actualizado para ${item.producto.nombre_producto}: $actualizado")
                    }
                }
            }

            carrito.clear()
            guardarCarritoLocal(context)

        } catch (e: Exception) {
            println("❌ Error confirmando recogida: ${e.message}")
        }
    }
}
