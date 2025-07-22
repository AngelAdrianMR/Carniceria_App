package com.carniceria.shared.shared.models.utils

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

suspend fun obtenerProductos(): List<Product> {
    val response = supabaseClient.get("${SupabaseConfig.BASE_URL}/rest/v1/productos") {
        headers {
            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            append("apikey", SupabaseConfig.API_KEY)
        }
    }

    return if (response.status.value in 200..299) {
        response.body()
    } else {
        emptyList() // O lanza excepción si prefieres
    }
}

suspend fun obtenerPromociones(): List<PromocionConProductos> {
    println("➤ Cargando promociones...")

    val promocionesResponse = supabaseClient.get("${SupabaseConfig.BASE_URL}/rest/v1/combos_promociones") {
        headers {
            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            append("apikey", SupabaseConfig.API_KEY)
        }
    }
    println("✔ Promociones status: ${promocionesResponse.status.value}")
    println("✳️ promosResponse: ${promocionesResponse.bodyAsText()}")


    val promociones: List<Promocion> = if (promocionesResponse.status.value in 200..299) {
        promocionesResponse.body()
    } else emptyList()

    val relacionesResponse = supabaseClient.get("${SupabaseConfig.BASE_URL}/rest/v1/producto_promociones") {
        headers {
            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            append("apikey", SupabaseConfig.API_KEY)
        }
    }
    println("✔ Relaciones status: ${relacionesResponse.status.value}")
    println("✳️ relacionesResponse: ${relacionesResponse.bodyAsText()}")

    val relaciones: List<ProductoPromocion> = if (relacionesResponse.status.value in 200..299) {
        relacionesResponse.body()
    } else emptyList()

    val productosResponse = supabaseClient.get("${SupabaseConfig.BASE_URL}/rest/v1/productos") {
        headers {
            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            append("apikey", SupabaseConfig.API_KEY)
        }
    }
    println("Productos status: ${productosResponse.status.value}")
    println("✳️ productosResponse: ${productosResponse.bodyAsText()}")

    val productos: List<Product> = if (productosResponse.status.value in 200..299) {
        productosResponse.body()
    } else emptyList()

    println("➤ Componiendo resultado final...")

    println("▶ promocionesRaw:")
    promociones.forEach { println("  - ${it.id} ${it.nombre_promocion}") }

    println("▶ relaciones:")
    relaciones.forEach { println("  - promo=${it.promocion_id}, producto=${it.producto_id}") }

    println("▶ productos:")
    productos.forEach { println("  - id=${it.id}, nombre=${it.nombre_producto}") }

    return promociones.mapNotNull { promo ->
        val productosDeEstaPromo = relaciones
            .filter { it.promocion_id == promo.id }
            .mapNotNull { rel -> productos.find { it.id == rel.producto_id } }

        println("🔎 Promo ${promo.id} tiene ${productosDeEstaPromo.size} productos")

        if (productosDeEstaPromo.isNotEmpty()) {
            PromocionConProductos(promocion = promo, productos = productosDeEstaPromo)
        } else null
    }

}
