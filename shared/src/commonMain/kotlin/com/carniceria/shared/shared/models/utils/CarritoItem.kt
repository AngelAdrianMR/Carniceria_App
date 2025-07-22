package com.carniceria.shared.shared.models.utils

import kotlinx.serialization.Serializable

@Serializable
data class CarritoItem(
    val producto: Product,
    val cantidad: Int
)
