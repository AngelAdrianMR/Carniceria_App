package com.carniceria.shared.shared.models.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


// Configuración global
object SupabaseConfig {
    const val BASE_URL = "https://itevswwqvnbspixlacan.supabase.co"
    const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0ZXZzd3dxdm5ic3BpeGxhY2FuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTE1NTQ0MjksImV4cCI6MjA2NzEzMDQyOX0.gEVaPTZH5O5Ofnhnf1IKX9yaRQzJYNRFjh3J3pApho8"
}

// Reutiliza un único cliente global
val supabaseClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

@Serializable
data class SignupRequest(
    val email: String,
    val password: String
)

@Serializable
data class SupabaseAuthResponse(
    val access_token: String? = null,
    val refresh_token: String? = null,
    val token_type: String? = null,
    val expires_in: Int? = null,
    val user: SupabaseUser? = null,
    val error: String? = null,
    val raw: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class SupabaseLoginResponse(
    val access_token: String? = null,
    val user: SupabaseUser? = null,
    val error: String? = null
)

@Serializable
data class SupabaseUser(
    val id: String,
    val email: String
)

@Serializable
data class SupabaseUserInfo(
    val id: String,
    val email: String
)

// Login de usuario (email + password)
suspend fun iniciarSesion(email: String, password: String): SupabaseLoginResponse {
    val response: HttpResponse = supabaseClient.post("${SupabaseConfig.BASE_URL}/auth/v1/token?grant_type=password") {
        headers {
            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            append("apikey", SupabaseConfig.API_KEY)
        }
        setBody(LoginRequest(email, password))
    }
    return response.body()
}

// Registro básico con email y contraseña
suspend fun registrarUsuario(email: String, password: String): SupabaseAuthResponse {
    return try {
        val response: HttpResponse = supabaseClient.post("${SupabaseConfig.BASE_URL}/auth/v1/signup") {
            headers {
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                append("apikey", SupabaseConfig.API_KEY)
            }
            setBody(SignupRequest(email, password))
        }

        if (response.status.value in 200..299) {
            response.body()
        } else {
            SupabaseAuthResponse(error = "Error HTTP ${response.status.value}")
        }
    } catch (e: Exception) {
        SupabaseAuthResponse(error = "Excepción al registrar: ${e.message ?: "Desconocida"}")
    }
}

// Obtener info del usuario actual por su access_token
suspend fun obtenerUsuarioActual(accessToken: String): SupabaseUserInfo? {
    val response = supabaseClient.get("${SupabaseConfig.BASE_URL}/auth/v1/user") {
        headers {
            append("Authorization", "Bearer $accessToken")
            append("apikey", SupabaseConfig.API_KEY)
        }
    }

    return if (response.status.value in 200..299) {
        response.body()
    } else {
        null
    }
}

// Obtener el perfil del usuario actual desde 'perfil_usuario'
suspend fun obtenerPerfilUsuarioActual( accessToken: String): PerfilUsuario? {
    val response = supabaseClient.get("${SupabaseConfig.BASE_URL}/rest/v1/perfil_usuario") {
        headers {
            append("apikey", SupabaseConfig.API_KEY)
            append("Authorization", "Bearer $accessToken")
        }
        parameter("select", "*")
    }

    return if (response.status.value in 200..299) {
        val perfiles = response.body<List<PerfilUsuario>>()
        perfiles.firstOrNull()
    } else {
        null
    }
}

// Guardar perfil del usuario en la tabla 'perfil_usuario'
suspend fun guardarPerfilUsuario(
    userId: String,
    accessToken: String,
    direccion: String,
    telefono: String,
    codigo_postal: String,
    rol: String = "Cliente"
): Boolean {
    val response = supabaseClient.post("${SupabaseConfig.BASE_URL}/rest/v1/perfil_usuario") {
        headers {
            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            append("apikey", SupabaseConfig.API_KEY)
            append("Authorization", "Bearer $accessToken")
        }
        setBody(
            mapOf(
                "id" to userId,
                "direccion" to direccion,
                "telefono" to telefono,
                "codigo_postal" to codigo_postal,
                "rol" to rol
            )
        )
    }

    return response.status.value in 200..299
}





