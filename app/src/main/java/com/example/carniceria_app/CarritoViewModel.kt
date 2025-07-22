package com.example.carniceria_app

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.carniceria.shared.shared.models.utils.CarritoItem
import com.carniceria.shared.shared.models.utils.Product

class CarritoViewModel : ViewModel() {
    var carrito = mutableStateListOf<CarritoItem>()
        private set

    private val stockMap = mutableMapOf<Long, Int>() // productId -> stock

    fun inicializarStock(productos: List<Product>) {
        productos.forEach {
            stockMap[it.id] = it.stock_producto ?: 0
        }
    }

    fun agregarAlCarrito(producto: Product, cantidad: Int): Boolean {
        val stockActual = stockMap[producto.id] ?: producto.stock_producto ?: 0

        return if (cantidad <= stockActual) {
            val index = carrito.indexOfFirst { it.producto.id == producto.id }

            if (index >= 0) {
                carrito[index] = carrito[index].copy(cantidad = carrito[index].cantidad + cantidad)
            } else {
                carrito.add(CarritoItem(producto, cantidad))
            }

            stockMap[producto.id] = stockActual - cantidad
            true
        } else {
            false // no se puede añadir más cantidad que el stock
        }
    }

    fun getStockDisponible(producto: Product): Int {
        return stockMap[producto.id] ?: producto.stock_producto ?: 0
    }
}
