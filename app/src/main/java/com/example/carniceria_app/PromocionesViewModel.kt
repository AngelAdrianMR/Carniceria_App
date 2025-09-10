package com.example.carniceria_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carniceria.shared.shared.models.utils.Product
import com.carniceria.shared.shared.models.utils.PromocionConProductos
import com.carniceria.shared.shared.models.utils.eliminarPromocionPorId
import com.carniceria.shared.shared.models.utils.obtenerProductos
import com.carniceria.shared.shared.models.utils.obtenerPromociones
import com.carniceria.shared.shared.models.utils.obtenerPromocionesAdmin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PromocionesAdminViewModel : ViewModel() {

    private val _promociones = MutableStateFlow<List<PromocionConProductos>>(emptyList())
    val promociones: StateFlow<List<PromocionConProductos>> = _promociones.asStateFlow()

    private val _productos = MutableStateFlow<List<Product>>(emptyList())
    val productos: StateFlow<List<Product>> = _productos.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            cargarDatos()
        }
    }

    suspend fun cargarDatos() {
        try {
            _loading.value = true
            _error.value = null
            _promociones.value = obtenerPromocionesAdmin()
            _productos.value = obtenerProductos()
        } catch (e: Exception) {
            _error.value = "❌ Error al cargar datos: ${e.localizedMessage}"
        } finally {
            _loading.value = false
        }
    }

    fun eliminarPromocion(promo: PromocionConProductos) {
        viewModelScope.launch {
            try {
                _loading.value = true

                val promoId = promo.promocion.id
                if (promoId != null) {
                    val ok = eliminarPromocionPorId(promoId)
                    if (ok) {
                        cargarDatos()
                    } else {
                        _error.value = "❌ No se pudo eliminar la promoción"
                    }
                } else {
                    _error.value = "❌ No se puede eliminar: promoción sin ID"
                }
            } catch (e: Exception) {
                _error.value = "❌ Error al eliminar: ${e.localizedMessage}"
            } finally {
                _loading.value = false
            }
        }
    }

}
