package com.carniceria.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String,
    val email: String,
    val password: String,
    val address: String,
    val postalCode: String,
    val phone: String,
    val isBusiness: Boolean = false // para empresas si quieres luego
)
