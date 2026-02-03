package com.carniceria.shared.shared.models.utils

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseUserInfo(
    val id: String,
    val email: String,
    val rol: String?= null,
    val empresaId: Long? =  null
)

data class PerfilConEmail(
    val id: String? = null,
    val id_usuario: String? = null,
    val email: String,
    val nombre_completo: String? = null,
    val calle: String? = null,
    val piso: String? = null,
    val localidad: String? = null,
    val provincia: String? = null,
    val pais: String? = null,
    val telefono: String? = null,
    val codigoPostal: String? = null,
    @SerialName("direcciones_envio")
    val direccionesEnvio: List<DireccionEnvioExtra> = emptyList()

) {
    val direccionCompleta: String
        get() = getDireccionCompleta(calle, piso, localidad, provincia, pais)
}

@Serializable
data class PerfilUsuarioUpsertPayload(
    val id: String,

    @SerialName("nombre_completo")
    val nombreCompleto: String,

    val calle: String,
    val piso: String,
    val localidad: String,
    val provincia: String,
    val pais: String,
    val telefono: String,

    @SerialName("codigo_postal")
    val codigoPostal: String,

    val rol: String = "Cliente",

    @SerialName("direcciones_envio")
    val direccionesEnvio: List<DireccionEnvioExtra> = emptyList()
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
    nombre_completo: String,
    calle: String,
    piso: String,
    localidad: String,
    provincia: String,
    pais: String,
    telefono: String,
    codigo_postal: String,
    rol: String = "Cliente",
    direcciones_envio: List<DireccionEnvioExtra> = emptyList()
): Boolean {
    return try {
        val payload = PerfilUsuarioUpsertPayload(
            id = userId,
            nombreCompleto = nombre_completo,
            calle = calle,
            piso = piso,
            localidad = localidad,
            provincia = provincia,
            pais = pais,
            telefono = telefono,
            codigoPostal = codigo_postal,
            rol = rol,
            direccionesEnvio = direcciones_envio
        )

        SupabaseProvider.client.postgrest["perfil_usuario"]
            .upsert(payload)

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
        id = perfil.id,
        id_usuario = user.id,
        email = user.email,
        nombre_completo = perfil.nombre_completo,
        calle = perfil.calle,
        piso = perfil.piso,
        localidad = perfil.localidad,
        provincia = perfil.provincia,
        pais = perfil.pais,
        telefono = perfil.telefono,
        codigoPostal = perfil.codigo_postal,
        direccionesEnvio = perfil.direcciones_envio ?: emptyList() // ✅ IMPORTANTE
    )
}


suspend fun obtenerPerfilCompletoU(): PerfilUsuario? {
    val user = obtenerUsuarioActual() ?: return null
    val perfil = obtenerPerfilUsuarioActual() ?: return null

    val direccionCompleta = listOfNotNull(
        perfil.calle,
        perfil.piso,
        perfil.localidad,
        perfil.provincia,
        perfil.pais
    ).joinToString(", ")

    return PerfilUsuario(
        id = user.id,
        nombre_completo = perfil.nombre_completo,
        calle = perfil.calle,
        piso = perfil.piso,
        localidad = perfil.localidad,
        provincia = perfil.provincia,
        pais = perfil.pais,
        telefono = perfil.telefono,
        codigo_postal = perfil.codigo_postal,
        rol = "Cliente"
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


