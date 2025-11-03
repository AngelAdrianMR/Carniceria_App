package com.carniceria.shared.shared.models.utils

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class CarritoItem(
    val producto: Product,
    val cantidad: Double,
    val mensaje: String? = null,
    val promocion: PromocionConProductos?=null
)

@Serializable
data class PedidoDetalle(
    //val id: String,
    //val fecha_creacion: String? = null,
    //val estado: String,
    //val tipo_entrega: String,
    //val total: Double,
    //val total_con_descuento: Double? = null,
    val usuario: PerfilUsuario? = null,
    val lineas: List<LineaPedido>? = null,
    val pedido: Pedido,
    val factura: Factura?

)

@Serializable
data class CodigoDescuento(
    val id: String,
    val codigo: String,
    val tipo: String, // "porcentaje" o "fijo"
    val valor: Double,
    val fecha_inicio: LocalDate,
    val fecha_fin: LocalDate,
    val uso_maximo: Int,
    val uso_actual: Int,
    val activo: Boolean
)