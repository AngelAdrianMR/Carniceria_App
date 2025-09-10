package com.carniceria.shared.shared.models.utils

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseUserInfo(
    val id: String,
    val email: String,
    val rol: String?= null
)

data class PerfilConEmail(
    val email: String,
    val direccion: String,
    val telefono: String,
    val codigoPostal: String
)

// ----------------- USUARIO -----------------

// Obtener info del usuario actual desde la sesión
fun obtenerUsuarioActual(): SupabaseUserInfo? {
    val user = SupabaseProvider.client.auth.currentUserOrNull() ?: return null
    return SupabaseUserInfo(
        id = user.id,
        email = user.email ?: ""
    )
}

// ----------------- PERFIL -----------------

// Obtener el perfil del usuario actual desde 'perfil_usuario'
suspend fun obtenerPerfilUsuarioActual(): PerfilUsuario? {
    val user = obtenerUsuarioActual() ?: return null

    val perfil = SupabaseProvider.client.postgrest["perfil_usuario"]
        .select {
            filter { eq("id", user.id)   } // 👈 usa eq del DSL, no "to"
        }
        .decodeSingleOrNull<PerfilUsuario>()

    println("Perfil recuperado: id=${perfil?.id}, rol=${perfil?.rol}")
    return perfil
}


// Guardar perfil del usuario en la tabla 'perfil_usuario'
suspend fun guardarPerfilUsuario(
    userId: String,
    direccion: String,
    telefono: String,
    codigo_postal: String,
    rol: String = "Cliente"
): Boolean {
    return try {
        SupabaseProvider.client.postgrest["perfil_usuario"]
            .upsert(
                mapOf(
                    "id" to userId,
                    "direccion" to direccion,
                    "telefono" to telefono,
                    "codigo_postal" to codigo_postal,
                    "rol" to rol
                )
            )
        true
    } catch (e: Exception) {
        println("❌ Error al guardar perfil: ${e.message}")
        false
    }
}

// Obtener perfil + email en un único objeto
suspend fun obtenerPerfilCompleto(): PerfilConEmail? {
    val user = obtenerUsuarioActual() ?: return null
    val perfil = obtenerPerfilUsuarioActual() ?: return null

    return PerfilConEmail(
        email = user.email,
        direccion = perfil.direccion,
        telefono = perfil.telefono,
        codigoPostal = perfil.codigo_postal
    )
}
