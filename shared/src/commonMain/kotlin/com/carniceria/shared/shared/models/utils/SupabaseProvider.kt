package com.carniceria.shared.shared.models.utils

import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.storage.Storage
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType


object SupabaseEnv {
    const val URL = "https://itevswwqvnbspixlacan.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0ZXZzd3dxdm5ic3BpeGxhY2FuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTE1NTQ0MjksImV4cCI6MjA2NzEzMDQyOX0.gEVaPTZH5O5Ofnhnf1IKX9yaRQzJYNRFjh3J3pApho8"
}

object SupabaseProvider {

    @OptIn(SupabaseInternal::class)
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SupabaseEnv.URL,
            supabaseKey = SupabaseEnv.ANON_KEY
        ) {
            // ✅ Configuración global del cliente HTTP
            httpConfig {
                defaultRequest {
                    contentType(ContentType.Application.Json)
                    headers.append("Accept-Charset", "UTF-8")
                }
            }

            // ✅ Plugins instalados
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Functions)
        }
    }
}