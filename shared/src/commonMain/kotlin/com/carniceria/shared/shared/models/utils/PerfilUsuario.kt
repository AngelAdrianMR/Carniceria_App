package com.carniceria.shared.shared.models.utils

import kotlinx.serialization.Serializable

/**
 * ========================================================
 * 🧾 MODELOS DE PEDIDOS Y LÍNEAS DE PEDIDO
 * ========================================================
 */

@Serializable
data class Pedido(
    val id: Long,
    val fecha_creacion: String? = null,
    val id_usuario: String,
    val total: Double,
    val estado: String,
    val tipo_entrega: String,
    val codigo_descuento_aplicado: String? = null,
    val descuento_aplicado: Double? = null,
    val total_con_descuento: Double? = null
)

@Serializable
data class LineaPedido(
    val id: Long? = null,
    val id_pedido: Long,
    val id_producto: Long? = null,
    val id_promocion: Long? = null,
    val cantidad: Double,
    val precio_unitario: Double,
    val subtotal: Double,
    val mensaje: String? = null,
    val producto: Product? = null,
    val promocion: PromocionConProductos? = null
)

@Serializable
data class LineaPedidoDB(
    val id: Long? = null,
    val id_pedido: Long,
    val id_producto: Long? = null,
    val id_promocion: Long? = null,
    val cantidad: Double,
    val precio_unitario: Double,
    val subtotal: Double,
    val mensaje: String? = null,
    val producto: Product? = null,
    val promocion: PromocionConProductos? = null
)

@Serializable
data class PerfilUsuario(
    val id: String?,
    val nombre_completo: String? = null, // 👈 nuevo campo
    val calle: String? = null,
    val piso: String? = null,
    val localidad: String? = null,
    val provincia: String? = null,
    val pais: String? = null,
    val telefono: String,
    val codigo_postal: String,
    val rol: String,
    val fcm_token: String? = null
) {
    val direccionCompleta: String
        get() = getDireccionCompleta(calle, piso, localidad, provincia, pais)
}

@Serializable
data class UsuarioAdmin(
    val id: String,
    val email: String,
    val nombre_completo: String? = null, // 👈 nuevo campo
    val calle: String? = null,
    val piso: String? = null,
    val localidad: String? = null,
    val provincia: String? = null,
    val pais: String? = null,
    val telefono: String,
    val codigo_postal: String,
    val rol: String,
    val fcm_token: String? = null
) {
    val direccionCompleta: String
        get() = getDireccionCompleta(calle, piso, localidad, provincia, pais)
}

@Serializable
data class Factura(
    val id: Long,
    val id_usuario: String,
    val fecha: String,
    val estado: String,
    val total: Double,
    val pdf_url: String? = null
)
