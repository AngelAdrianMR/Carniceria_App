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
    val nombre_completo: String? = null,
    val calle: String? = null,
    val piso: String? = null,
    val localidad: String? = null,
    val provincia: String? = null,
    val pais: String? = null,
    val telefono: String? = null,
    val codigoPostal: String? = null,
) {
    val direccionCompleta: String
        get() = getDireccionCompleta(calle, piso, localidad, provincia, pais)
}


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
    nombre_completo: String,
    calle: String,
    piso: String,
    localidad: String,
    provincia: String,
    pais: String,
    telefono: String,
    codigo_postal: String,
    rol: String = "Cliente"
): Boolean {
    return try {
        SupabaseProvider.client.postgrest["perfil_usuario"]
            .upsert(
                mapOf(
                    "id" to userId,
                    "nombre_completo" to nombre_completo,
                    "calle" to calle,
                    "piso" to piso,
                    "localidad" to localidad,
                    "provincia" to provincia,
                    "pais" to pais,
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

    val direccionCompleta = listOfNotNull(
        perfil.calle,
        perfil.piso,
        perfil.localidad,
        perfil.provincia,
        perfil.pais
    ).joinToString(", ")

    return PerfilConEmail(
        email = user.email,
        nombre_completo = perfil.nombre_completo,
        calle = perfil.calle,
        piso = perfil.piso,
        localidad = perfil.localidad,
        provincia = perfil.provincia,
        pais = perfil.pais,
        telefono = perfil.telefono,
        codigoPostal = perfil.codigo_postal
    )
}

fun getDireccionCompleta(
    calle: String? = null,
    piso: String? = null,
    localidad: String? = null,
    provincia: String? = null,
    pais: String? = null
): String {
    return listOfNotNull(
        calle?.takeIf { it.isNotBlank() },
        piso?.takeIf { it.isNotBlank() },
        localidad?.takeIf { it.isNotBlank() },
        provincia?.takeIf { it.isNotBlank() },
        pais?.takeIf { it.isNotBlank() }
    ).joinToString(", ")
}


