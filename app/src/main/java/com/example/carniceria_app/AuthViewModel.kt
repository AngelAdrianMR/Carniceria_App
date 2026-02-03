package com.example.carniceria_app

import android.app.Application
import android.content.Context
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

    private val prefs = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val KEY_EMAIL = "last_email"

    private val _usuario = MutableStateFlow<SupabaseUserInfo?>(null)
    val usuario: StateFlow<SupabaseUserInfo?> get() = _usuario

    private val _rememberMe = MutableStateFlow(prefs.getBoolean("remember_me", false))
    val rememberMe: StateFlow<Boolean> get() = _rememberMe

    private val _savedEmail = MutableStateFlow(prefs.getString(KEY_EMAIL, "") ?: "")
    val savedEmail: StateFlow<String> get() = _savedEmail

    init {
        // Al iniciar, intenta cargar usuario actual desde sesión o token persistido
        viewModelScope.launch {
            val remember = prefs.getBoolean("remember_me", false)
            val token = prefs.getString("access_token", null)

            if (remember && token != null) {
                // Intenta restaurar la sesión guardada
                try {
                    SupabaseProvider.client.auth.currentSessionOrNull()?.let {
                        cargarUsuario()
                    }
                } catch (_: Exception) {
                    // Token expirado o inválido: conserva email y limpia sesión
                    val emailGuardado = prefs.getString(KEY_EMAIL, null)

                    prefs.edit()
                        .remove("access_token")
                        .putBoolean("remember_me", false)
                        .apply()

                    if (!emailGuardado.isNullOrBlank()) {
                        prefs.edit().putString(KEY_EMAIL, emailGuardado).apply()
                    }
                }

            } else {
                cargarUsuario()
            }
        }
    }

    fun cargarUsuario() {
        viewModelScope.launch {
            val user = SupabaseProvider.client.auth.currentUserOrNull()
            val perfil = obtenerPerfilUsuarioActual()
            _usuario.value = user?.let {
                SupabaseUserInfo(
                    id = it.id,
                    email = it.email ?: "",
                    rol = perfil?.rol,
                    empresaId = perfil?.empresa_id
                )
            }
        }
    }

    fun setRememberMe(value: Boolean) {
        prefs.edit().putBoolean("remember_me", value).apply()
        _rememberMe.value = value

        if (!value) {
            // Si no quieres recordar sesión, borra solo el token
            prefs.edit()
                .remove("access_token")
                .apply()
        }
    }

    fun saveSession() {
        viewModelScope.launch {
            val session = SupabaseProvider.client.auth.currentSessionOrNull()
            if (_rememberMe.value && session != null) {
                prefs.edit()
                    .putString("access_token", session.accessToken)
                    .apply()
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            SupabaseProvider.client.auth.signOut()

            prefs.edit()
                .remove("access_token")
                .putBoolean("remember_me", false) // opcional: si quieres desmarcar
                .apply()

            _rememberMe.value = prefs.getBoolean("remember_me", false)
            _usuario.value = null
        }
    }

    fun loadLastEmail() {
        _savedEmail.value = prefs.getString(KEY_EMAIL, "") ?: ""
    }

    fun saveLastEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
        _savedEmail.value = email
    }


}
