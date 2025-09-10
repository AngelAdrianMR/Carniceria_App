package com.carniceria.shared.shared.models.utils

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest


object SupabaseEnv {
    const val URL = "https://itevswwqvnbspixlacan.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Iml0ZXZzd3dxdm5ic3BpeGxhY2FuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTE1NTQ0MjksImV4cCI6MjA2NzEzMDQyOX0.gEVaPTZH5O5Ofnhnf1IKX9yaRQzJYNRFjh3J3pApho8"
}

object SupabaseProvider {
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SupabaseEnv.URL,
            supabaseKey = SupabaseEnv.ANON_KEY
        ) { install(Auth)
            install(Postgrest)
        }
    }
}
