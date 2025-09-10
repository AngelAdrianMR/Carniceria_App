package com.carniceria.shared.shared.models.utils

import kotlinx.serialization.Serializable

@Serializable
data class PerfilUsuario(
    val id: String,
    val direccion: String,
    val telefono: String,
    val codigo_postal: String,
    val rol: String
)


