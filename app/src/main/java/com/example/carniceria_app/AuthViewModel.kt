package com.example.carniceria_app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import com.carniceria.shared.shared.models.utils.SupabaseUserInfo
import com.carniceria.shared.shared.models.utils.obtenerPerfilUsuarioActual
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val _usuario = MutableStateFlow<SupabaseUserInfo?>(null)
    val usuario: StateFlow<SupabaseUserInfo?> get() = _usuario

    init {
        // Al iniciar, intenta cargar usuario actual desde la sesión
        cargarUsuario()
    }

    fun cargarUsuario() {
        viewModelScope.launch {
            val user = SupabaseProvider.client.auth.currentUserOrNull()
            val perfil = obtenerPerfilUsuarioActual()
            _usuario.value = user?.let {
                SupabaseUserInfo(
                    id = it.id,
                    email = it.email ?: "",
                    rol = perfil?.rol
                )
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            SupabaseProvider.client.auth.signOut()
            _usuario.value = null
        }
    }


}
