package com.carniceria.shared.shared.models.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Product(
    val id: Long?= null,
    val created_at: String? = null,
    val nombre_producto: String,
    val descripcion_producto: String? = null,
    val categoria_producto: String, // Enum en Supabase: Categorías
    val precio_venta: Double,
    val precio_compra: Double? = null,
    val imagen_producto: String? = null,
    val precio_sin_iva: Double,
    @Transient val beneficio: Double = 0.0,
    val unidad_medida: String?,
    val stock_producto: Int? = null,
)
@Serializable
data class Promocion(
    val id: Long?= null,
    val nombre_promocion: String,
    val descripcion_promocion: String?,
    val imagen_promocion: String?,
    val precio_total: Double,
    @Transient val precio_compra_promo: Double = 0.0,
    @Transient val beneficio_promo: Double = 0.0,
    val estado: Boolean
)
@Serializable
data class ProductoPromocion(
    val id: Long?= null,
    val promocion_id: Long,
    val producto_id: Long
)
@Serializable
data class PromocionConProductos(
    val promocion: Promocion,
    val productos: List<Product>
)
