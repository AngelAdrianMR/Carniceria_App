package com.example.carniceria_app

import com.carniceria.shared.shared.models.utils.SupabaseEnv
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import com.carniceria.shared.shared.models.utils.SupabaseService.PedidoConToken
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.tasks.await
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.contentnegotiation.*
import kotlinx.serialization.json.JsonObject
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.add
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull


// ✅ Guarda el token FCM del usuario en Supabase
suspend fun registrarTokenFCM(idUsuario: String) {
    try {
        val token = FirebaseMessaging.getInstance().token.await()

        SupabaseProvider.client
            .from("perfil_usuario")
            .update(mapOf("fcm_token" to token)) {
                filter { eq("id", idUsuario) }
            }

        println("✅ Token FCM guardado correctamente: $token")
    } catch (e: Exception) {
        println("⚠️ Error al registrar token FCM: ${e.message}")
    }
}


// ✅ Obtiene el token FCM del usuario asociado a un pedido
suspend fun obtenerTokenUsuario(idPedido: Long): String? {
    return try {
        val pedido = SupabaseProvider.client
            .from("pedido")
            .select(columns = Columns.raw("id_usuario, perfil_usuario(fcm_token)")) {
                filter { eq("id", idPedido) }
            }
            .decodeSingleOrNull<PedidoConToken>()

        val token = pedido?.perfil_usuario?.fcm_token
        println("🎯 Token del usuario para pedido $idPedido: $token")
        token
    } catch (e: Exception) {
        println("⚠️ Error obteniendo token usuario: ${e.message}")
        null
    }
}

suspend fun enviarNotificacionCambioEstado(
    idPedido: Long,
    nuevoEstado: String
) = withContext(Dispatchers.IO) {
    try {
        val token = obtenerTokenUsuario(idPedido)
        if (token.isNullOrEmpty()) {
            println("⚠️ No se encontró token FCM para pedido $idPedido")
            return@withContext
        }

        // ✅ Cliente Ktor con ContentNegotiation
        val client = HttpClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json()
            }
        }

        val titulo = "Tu pedido #$idPedido ha cambiado de estado"
        val mensaje = when (nuevoEstado) {
            "aceptado" -> "Tu pedido ha sido aceptado ✅"
            "rechazado" -> "Tu pedido ha sido rechazado ❌"
            "enviado" -> "Tu pedido ha sido enviado 🚚"
            "entregado" -> "Tu pedido ha sido entregado 🏠"
            "pendiente" -> "Tu pedido vuelve a estar en revisión ⏳"
            else -> "El estado de tu pedido ha cambiado: $nuevoEstado"
        }

        val url = "${SupabaseEnv.URL}/functions/v1/send_notification"

        val body = mapOf(
            "token" to token,
            "title" to titulo,
            "body" to mensaje
        )

        val res = client.post(url) {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${SupabaseEnv.ANON_KEY}")
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            setBody(body)
        }

        println("✅ Notificación enviada (estado ${res.status})")

    } catch (e: Exception) {
        println("❌ Error enviando notificación: ${e.message}")
    }
}

// ✅ Obtiene todos los tokens FCM de usuarios con rol "Cliente"
suspend fun obtenerTokensClientes(): List<String> {
    return try {
        val response = SupabaseProvider.client
            .from("perfil_usuario")
            .select(columns = Columns.raw("fcm_token, rol")) {
                filter { eq("rol", "Cliente") }
            }

        val data = response.decodeList<JsonObject>()

        val tokens = data.mapNotNull {
            it["fcm_token"]?.jsonPrimitive?.contentOrNull
        }.filter { it.isNotBlank() }

        println("📱 Tokens obtenidos (${tokens.size}) usuarios clientes.")
        tokens
    } catch (e: Exception) {
        println("⚠️ Error obteniendo tokens de clientes: ${e.message}")
        emptyList()
    }
}

suspend fun notificarClientes(titulo: String, cuerpo: String) {
    try {
        val tokens = obtenerTokensClientes()
        if (tokens.isEmpty()) {
            println("⚠️ No hay tokens de clientes registrados.")
            return
        }

        val client = HttpClient {
            install(ContentNegotiation) { json() }
        }

        val response: HttpResponse = client.post("${SupabaseEnv.URL}/functions/v1/send_notification_clientes") {
            headers {
                append(HttpHeaders.Authorization, "Bearer ${SupabaseEnv.ANON_KEY}")
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            setBody(
                buildJsonObject {
                    put("title", titulo)
                    put("body", cuerpo)
                    put("tokens", buildJsonArray {
                        tokens.forEach { add(it) }
                    })
                }
            )
        }

        println("✅ Notificación enviada a ${tokens.size} clientes (${response.status})")
        client.close()

    } catch (e: Exception) {
        println("❌ Error al notificar clientes: ${e.message}")
    }
}

