package com.example.carniceria_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carniceria.shared.shared.models.utils.Product
import com.carniceria.shared.shared.models.utils.obtenerProductos
import com.carniceria.shared.shared.models.utils.actualizarStockProductos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImportarStockViewModel : ViewModel() {

    private val _productos = MutableStateFlow<List<Product>>(emptyList())
    val productos: StateFlow<List<Product>> = _productos

    // idProducto → cantidad a sumar
    val cantidades = mutableMapOf<Long, Int>()

    init {
        viewModelScope.launch {
            try {
                _productos.value = obtenerProductos()
                _productos.value.forEach { p ->
                    if (p.id != null) cantidades[p.id!!] = 0
                }
            } catch (e: Exception) {
                println("❌ Error al cargar productos: ${e.message}")
            }
        }
    }

    fun actualizarCantidad(idProducto: Long?, cantidad: Int) {
        if (idProducto != null) {
            cantidades[idProducto] = cantidad
        }
    }

    suspend fun guardarCambios() {
        try {
            // Filtra los que tengan cantidad > 0
            val cantidadesValidas = cantidades.filterValues { it != 0 }
            if (cantidadesValidas.isNotEmpty()) {
                actualizarStockProductos(cantidadesValidas)
                println("✅ Stock actualizado para ${cantidadesValidas.size} productos")
            } else {
                println("⚠️ No hay cambios que aplicar")
            }
        } catch (e: Exception) {
            println("❌ Error al actualizar stock: ${e.message}")
        }
    }
}
