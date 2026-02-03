package com.example.carniceria_app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carniceria.shared.shared.models.utils.ProductEmpresa
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import com.carniceria.shared.shared.models.utils.SupabaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmpresaViewModel : ViewModel() {

    private val supabaseService = SupabaseService(SupabaseProvider.client)

    // ---------------------------------------------------
    // 🔹 Estados observables
    // ---------------------------------------------------
    private val _productos = MutableStateFlow<List<ProductEmpresa>>(emptyList())
    val productos: StateFlow<List<ProductEmpresa>> = _productos.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ---------------------------------------------------
    // 🔹 Cargar productos empresa
    // ---------------------------------------------------
    fun cargarProductosEmpresa(empresaId: Long) {
        viewModelScope.launch {
            try {
                _cargando.value = true
                val lista = supabaseService.getProductosEmpresa(empresaId)
                _productos.value = lista
                _error.value = null
                Log.i("EmpresaViewModel", "Productos cargados: ${lista.size}")
            } catch (e: Exception) {
                Log.e("EmpresaViewModel", "Error al cargar productos empresa", e)
                _error.value = "Error al cargar productos"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ---------------------------------------------------
    // 🔹 Refrescar productos (puede llamarse tras editar)
    // ---------------------------------------------------
    fun refrescar(empresaId: Long) {
        cargarProductosEmpresa(empresaId)
    }
}
