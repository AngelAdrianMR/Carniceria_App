package com.carniceria.shared.shared.models.utils

import kotlinx.serialization.SerialName
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
    val fcm_token: String? = null,
    val empresa_id: Long? = null,
    @SerialName("direcciones_envio")
    val direcciones_envio: List<DireccionEnvioExtra> = emptyList(),
    val bloqueado: Boolean? = false
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
    val fcm_token: String? = null,
    val bloqueado: Boolean? = null
) {
    val direccionCompleta: String
        get() = getDireccionCompleta(calle, piso, localidad, provincia, pais)
}

@Serializable
data class DireccionEnvioExtra(
    val id: String,
    val alias: String? = null,
    val calle: String,
    val piso: String? = null,
    val localidad: String,
    val provincia: String,
    val pais: String = "España",
    @SerialName("codigo_postal")
    val codigoPostal: String,
    val telefono: String? = null,
    val instrucciones: String? = null
)

@Serializable
data class Factura(
    val id: Long,
    val id_usuario: String,
    val fecha: String,
    val estado: String,
    val total: Double,
    val pdf_url: String? = null,
    val id_pedido: Long?=null
)

@Serializable
data class EmpresaAdmin(
    val id: Long? = null,
    val nombre_empresa: String,
    val cif: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val calle: String? = null,
    val codigo_postal: String? = null,
    val localidad: String? = null,
    val provincia: String? = null,
    val pais: String? = null,
    val perfil_usuario_id: String? = null,
    val bloqueado: Boolean? = null
)

@Serializable
data class NuevaEmpresa(
    val nombre_empresa: String,
    val nif_cif: String,
    val email: String,
    val telefono: String?,
    val direccion_fiscal: String?,
    val activa: Boolean = true,
    val creada_en: String,
    val perfil_usuario_id: String
)

@Serializable
data class Empresa(
    val id: Long,
    val nombre_empresa: String,
    val nif_cif: String?,
    val direccion_fiscal: String?,
    val email: String?,
    val telefono: String?,
    val iban: String?,
    val logo_empresa: String?,
    val activa: Boolean?,
    val creada_en: String?,
    val perfil_usuario_id: String?
)
