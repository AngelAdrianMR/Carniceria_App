package com.example.carniceria_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carniceria.shared.shared.models.utils.Product
import com.carniceria.shared.shared.models.utils.Promocion
import com.carniceria.shared.shared.models.utils.PromocionConProductos
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import com.carniceria.shared.shared.models.utils.eliminarPromocionPorId
import com.carniceria.shared.shared.models.utils.obtenerProductos
import com.carniceria.shared.shared.models.utils.obtenerPromocionesAdmin
import io.github.jan.supabase.postgrest.from
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

    // 🔹 Cargar promociones y productos
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

    // 🔹 Eliminar promoción
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

    // ✅ Notifica a todos los clientes una nueva promoción
    fun crearPromocionYNotificar(promocion: Promocion) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                // 🎯 Mensaje dinámico con nombre + precio
                val titulo = "🎉 Nueva promoción: ${promocion.nombre_promocion}"
                val cuerpo = "Disponible por solo ${String.format("%.2f", promocion.precio_total)} € 💰"

                println("🔔 Enviando notificación: $titulo - $cuerpo")

                // ✅ Llamada a la función global de notificación
                notificarClientes(
                    titulo = titulo,
                    cuerpo = cuerpo
                )

                // 🔁 Refrescar la lista de promociones
                cargarDatos()

            } catch (e: Exception) {
                _error.value = "❌ Error al enviar notificación: ${e.localizedMessage}"
                println("❌ Error al enviar notificación: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun notificarNuevoProducto(producto: Product) {
        viewModelScope.launch {
            try {
                val titulo = "🆕 Nuevo producto disponible"
                val cuerpo = "${producto.nombre_producto} por solo ${String.format("%.2f", producto.precio_venta)} € 🛒"

                println("🔔 Enviando notificación a clientes: $titulo - $cuerpo")

                notificarClientes(
                    titulo = titulo,
                    cuerpo = cuerpo
                )

            } catch (e: Exception) {
                println("❌ Error al notificar nuevo producto: ${e.message}")
            }
        }
    }

}

