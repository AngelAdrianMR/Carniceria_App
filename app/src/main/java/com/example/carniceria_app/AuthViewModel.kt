package com.example.carniceria_app

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.carniceria.shared.shared.models.utils.obtenerUsuarioActual
import com.carniceria.shared.shared.models.utils.SupabaseUserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _usuario = MutableStateFlow<SupabaseUserInfo?>(null)
    val usuario: StateFlow<SupabaseUserInfo?> get() = _usuario

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> get() = _accessToken

    init {
        // Cuando se inicia, intenta cargar sesión
        val token = prefs.getString("access_token", null)
        if (token != null) {
            _accessToken.value = token
            cargarUsuario(token)
        }
    }

    fun guardarToken(token: String) {
        prefs.edit().putString("access_token", token).apply()
        _accessToken.value = token
        cargarUsuario(token)
    }

    fun cerrarSesion() {
        prefs.edit().clear().apply()
        _accessToken.value = null
        _usuario.value = null
    }

    private fun cargarUsuario(token: String) {
        viewModelScope.launch {
            val userInfo = obtenerUsuarioActual(token)
            _usuario.value = userInfo
        }
    }
}
