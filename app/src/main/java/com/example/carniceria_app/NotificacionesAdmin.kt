package com.example.carniceria_app

import com.carniceria.shared.shared.models.utils.PerfilUsuario
import com.carniceria.shared.shared.models.utils.SupabaseEnv
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import io.github.jan.supabase.postgrest.from
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import kotlinx.serialization.json.add

// ===================================================
// 👑 TOKENS DE ADMINISTRADORES
// ===================================================

/**
 * 🔹 Obtiene todos los tokens FCM de los usuarios con rol "Administrador".
 */
suspend fun obtenerTokensAdmins(): List<String> {
    return try {
        // 1️⃣ Obtener datos crudos desde Supabase
        val response = SupabaseProvider.client
            .from("perfil_usuario")
            .select()

        // 2️⃣ Parsear con configuración JSON tolerante
        val jsonConfig = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }

        val admins = jsonConfig.decodeFromString<List<PerfilUsuario>>(response.data)

        // 3️⃣ Filtrar tokens válidos
        val tokens = admins
            .filter { it.rol == "Administrador" }
            .mapNotNull { it.fcm_token }
            .filter { it.isNotEmpty() }

        println("🎯 Tokens admin encontrados: ${tokens.size}")
        tokens
    } catch (e: Exception) {
        println("⚠️ Error obteniendo tokens admin: ${e.message}")
        emptyList()
    }
}

// ===================================================
// 📢 ENVÍO DE NOTIFICACIONES A ADMINISTRADORES
// ===================================================

/**
 * 🔹 Envía una notificación FCM a todos los administradores.
 */
suspend fun notificarAdmins(titulo: String, cuerpo: String) {
    val tokens = obtenerTokensAdmins()

    if (tokens.isEmpty()) {
        println("⚠️ No hay tokens de administradores disponibles")
        return
    }

    // ✅ Cliente HTTP Ktor con soporte JSON
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    try {
        val response: HttpResponse = client.post("${SupabaseEnv.URL}/functions/v1/send_notification_admin") {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${SupabaseEnv.ANON_KEY}")
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }

            setBody(
                buildJsonObject {
                    put("title", titulo)
                    put("body", cuerpo)
                    put(
                        "tokens",
                        buildJsonArray {
                            tokens.forEach { add(it) }
                        }
                    )
                }
            )
        }

        println("✅ Notificación enviada a admins (${response.status})")
    } catch (e: Exception) {
        println("❌ Error enviando notificación a admins: ${e.message}")
    } finally {
        client.close()
    }
}
