package com.carniceria.shared.shared.models.utils

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Long,
    val created_at: String? = null,
    val nombre_producto: String,
    val descripcion_producto: String? = null,
    val categoria_producto: String, // Enum en Supabase: Categorías
    val precio_venta: Double,
    val precio_compra: Double? = null,
    val imagen_producto: String? = null,
    val precio_sin_iva: Double,
    val beneficio: Double? = null,
    val stock_producto: Int? = null,
    val unidad_medida: String? // Enum: "Kilo" o "Unidad"
)
@Serializable
data class Promocion(
    val id: Long,
    val nombre_promocion: String,
    val descripcion_promocion: String?,
    val imagen_promocion: String?,
    val precio_total: Double
)
@Serializable
data class ProductoPromocion(
    val id: Long,
    val promocion_id: Long,
    val producto_id: Long
)
@Serializable
data class PromocionConProductos(
    val promocion: Promocion,
    val productos: List<Product>
)

@Serializable
data class PerfilUsuario(
    val id: String,
    val direccion: String,
    val telefono: String,
    val codigo_postal: String,
    val rol: String
)