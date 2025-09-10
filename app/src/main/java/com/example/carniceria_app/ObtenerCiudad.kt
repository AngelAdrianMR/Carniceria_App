package com.example.carniceria_app

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.Normalizer
import java.util.Locale

@Serializable
data class NominatimResponseItem(
    @SerialName("display_name") val displayName: String,
    val address: Address?  = null
)

@Serializable
data class Address(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null
)

fun normalizar(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .lowercase(Locale.getDefault())

val zonasPermitidas = listOf("Roquetas de Mar", "Aguadulce", "El Parador", "La Mojonera")
val costeEnvio = 3.50 // puedes cambiar este valor cuando quieras

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(
            kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            }
        )
    }
}


suspend fun obtenerCiudadDesdeDireccion(direccion: String): String? {
    val url = "https://nominatim.openstreetmap.org/search"

    val response: List<NominatimResponseItem> = client.get(url) {
        parameter("q", direccion)
        parameter("format", "json")
        header(HttpHeaders.UserAgent, "carniceria-app") // Obligatorio para Nominatim
    }.body()

    return response.firstOrNull()?.address?.let {
        it.city ?: it.town ?: it.village
    }
}

suspend fun validarEnvio(direccion: String): Pair<Boolean, Double> {
    val ciudad = obtenerCiudadDesdeDireccion(direccion) ?: return false to 0.0
    val ciudadNormalizada = normalizar(ciudad)

    val esValida = zonasPermitidas.any { zona ->
        ciudadNormalizada.contains(normalizar(zona))
    }

    return esValida to if (esValida) costeEnvio else 0.0
}
