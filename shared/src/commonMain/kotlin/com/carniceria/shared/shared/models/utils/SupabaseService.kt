package com.carniceria.shared.shared.models.utils

import com.carniceria.shared.shared.models.utils.SupabaseProvider.client
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.*
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.InternalAPI
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.atomicfu.TraceBase.None.append


class SupabaseService(private val supabase: SupabaseClient) {

    // Crear pedido con código de descuento
    suspend fun crearPedidoConDescuento(
        usuarioId: String?,
        carrito: List<CarritoItem>,
        tipoEntrega: String,
        codigo: String?,
        descuento: Double
    ): Long {
        val payload = carrito.map { item ->
            buildJsonObject {
                item.producto?.id?.let { put("id_producto", it) }
                item.promocion?.promocion?.id?.let { put("id_promocion", it) }
                put("cantidad", item.cantidad)

                // Guardar mensaje si existe
                item.mensaje?.let {
                    put("mensaje", JsonPrimitive(it))
                }
            }
        }

        val params = buildJsonObject {
            put("p_usuario_id", usuarioId)
            put("p_productos", JsonArray(payload))
            put("p_tipo_entrega", tipoEntrega)
            codigo?.let { put("p_codigo_descuento", it) }
            put("p_descuento_aplicado", descuento)
        }

        // 🔹 1️⃣ Crear el pedido mediante la función RPC
        val pedidoId = supabase.postgrest.rpc(
            function = "crear_pedido",
            parameters = params
        ).decodeAs<Long>()

        println("✅ Pedido creado con ID: $pedidoId (tipo: $tipoEntrega)")

        // 🔹 2️⃣ Actualizar stock en Supabase
        try {
            for (item in carrito) {
                when {
                    // 🧾 Producto normal
                    item.producto != null -> {
                        val prod = item.producto!!
                        val idProducto = prod.id
                        if (idProducto != null) {
                            val nuevoStock = prod.stock_producto?.minus(item.cantidad)
                            SupabaseProvider.client.from("productos")
                                .update(mapOf("stock_producto" to nuevoStock?.coerceAtLeast(0.0))) {
                                    filter { eq("id", idProducto) }
                                }
                            println("🔻 Stock actualizado para ${prod.nombre_producto}: $nuevoStock")
                            println("🔻 Stock actualizado para ${prod.nombre_producto}: ${item.cantidad}")
                            println("🔻 Stock actualizado para ${prod.nombre_producto}: ${prod.stock_producto}")
                        } else {
                            println("⚠️ Producto sin ID, no se puede actualizar stock (${prod.nombre_producto})")
                        }
                    }

                    // 🧾 Promoción: restar stock de todos los productos que contiene
                    item.promocion != null -> {
                        val promo = item.promocion!!
                        for (p in promo.productos) {
                            val idProdPromo = p.id
                            if (idProdPromo != null) {
                                val nuevoStock = (p.stock_producto ?: 0.0) - item.cantidad
                                SupabaseProvider.client.from("productos")
                                    .update(mapOf("stock_producto" to nuevoStock.coerceAtLeast(0.0))) {
                                        filter { eq("id", idProdPromo) }
                                    }
                                println("🔻 Stock actualizado para producto ${p.nombre_producto} (promo: ${promo.promocion.nombre_promocion}): $nuevoStock")
                            } else {
                                println("⚠️ Producto de promoción sin ID (${p.nombre_producto})")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error al actualizar stock: ${e.message}")
        }

        return pedidoId
    }


    // ===== Helpers privados =====

    private suspend fun cargarPedidoDetalle(pedido: Pedido, incluirUsuario: Boolean): PedidoDetalle {
        // 1) Cargar líneas (ya con opciones decodificadas gracias al serializer)
        val lineasDB = supabase.postgrest["lineas_pedido"]
            .select { filter { eq("id_pedido", pedido.id) } }
            .decodeList<LineaPedidoDB>()

        // 2) IDs de productos y promos desde líneas
        val idsProductoDeLineas = lineasDB.mapNotNull { it.id_producto }.distinct()
        val idsPromos = lineasDB.mapNotNull { it.id_promocion }.distinct()

        // 3) Productos de promociones (para poder listarlos dentro)
        val productosPromos =
            if (idsPromos.isNotEmpty()) {
                supabase.postgrest["producto_promociones"]
                    .select { filter { "id_promocion" to "in.(${idsPromos.joinToString(",")})" } }
                    .decodeList<ProductoPromocion>()
            } else emptyList()

        val idsProductosEnPromos = productosPromos.map { it.producto_id }.distinct()

        // 4) Unión de todos los productos que debemos cargar (líneas + promos)
        val idsTodosProductos = (idsProductoDeLineas + idsProductosEnPromos).distinct()

        // 5) Mapa de productos
        val productosMap =
            if (idsTodosProductos.isNotEmpty()) {
                supabase.postgrest["productos"]
                    .select { filter { "id" to "in.(${idsTodosProductos.joinToString(",")})" } }
                    .decodeList<Product>()
                    .associateBy { it.id!! }
            } else emptyMap()

        // 6) Mapa de promos
        val promosMap =
            if (idsPromos.isNotEmpty()) {
                supabase.postgrest["combos_promociones"]
                    .select { filter { "id" to "in.(${idsPromos.joinToString(",")})" } }
                    .decodeList<Promocion>()
                    .associateBy { it.id!! }
            } else emptyMap()

        // 7) GroupBy promo → lista de relaciones (para componer PromocionConProductos)
        val productosPromosMap = productosPromos.groupBy { it.promocion_id }

        // 8) Construcción de líneas de pedido enriquecidas
        val lineas = lineasDB.mapNotNull { lp ->
            when {
                lp.id_producto != null -> {
                    productosMap[lp.id_producto]?.let { prod ->
                        LineaPedido(
                            id = lp.id,
                            id_pedido = lp.id_pedido,
                            id_producto = lp.id_producto,
                            cantidad = lp.cantidad,
                            precio_unitario = lp.precio_unitario ?: prod.precio_venta,
                            subtotal = lp.subtotal ?: (prod.precio_venta ?: 0.0) * lp.cantidad,
                            producto = prod,
                            id_promocion = null,
                            promocion = null,
                            mensaje = lp.mensaje // 👈 ya viene decodificado por el serializer
                        )
                    }
                }

                lp.id_promocion != null -> {
                    promosMap[lp.id_promocion]?.let { promo ->
                        val productosDeEstaPromo = productosPromosMap[lp.id_promocion]
                            ?.mapNotNull { productosMap[it.producto_id] }
                            ?: emptyList()

                        LineaPedido(
                            id = lp.id,
                            id_pedido = lp.id_pedido!!,
                            id_producto = null, // es promo, no producto
                            cantidad = lp.cantidad,
                            precio_unitario = lp.precio_unitario ?: promo.precio_total,
                            subtotal = lp.subtotal ?: ((promo.precio_total ?: 0.0) * lp.cantidad),
                            producto = null,
                            id_promocion = lp.id_promocion,
                            promocion = PromocionConProductos(promo, productosDeEstaPromo),
                            mensaje = lp.mensaje // en promos normalmente irá vacío
                        )
                    }
                }

                else -> null
            }
        }

        // 9) Usuario (opcional)
        val usuario = if (incluirUsuario) {
            supabase.postgrest["perfil_usuario"]
                .select { filter { eq("id", pedido.id_usuario) } }
                .decodeSingleOrNull<PerfilUsuario>()
        } else null

        // 10) Factura asociada
        val factura = supabase.postgrest["facturas"]
            .select { filter { eq("id_pedido", pedido.id!!) } }
            .decodeSingleOrNull<Factura>()

        return PedidoDetalle(pedido = pedido, lineas = lineas, usuario = usuario, factura = factura)
    }

    // ===== Públicos =====
    suspend fun obtenerUsuariosAdmin(): List<UsuarioAdmin> {
        return supabase.postgrest.rpc("obtener_usuarios_admin").decodeList<UsuarioAdmin>()
    }

    suspend fun obtenerTodosPedidos(): List<PedidoDetalle> {
        val pedidos = supabase.postgrest["pedido"].select().decodeList<Pedido>()
        if (pedidos.isEmpty()) return emptyList()
        return pedidos.map { cargarPedidoDetalle(it, incluirUsuario = true) }
    }

    suspend fun obtenerPedidosUsuario(usuarioId: String): List<PedidoDetalle> {
        val pedidos = supabase.postgrest["pedido"]
            .select { filter { eq("id_usuario", usuarioId) } }
            .decodeList<Pedido>()
        if (pedidos.isEmpty()) return emptyList()
        return pedidos.map { cargarPedidoDetalle(it, incluirUsuario = false) }
    }

    suspend fun obtenerPedidosUsuarioDesdeVista(usuarioId: String): List<PedidoDetalle> {
        return try {
            client
                .from("pedido_detalle_view")
                .select{ filter{ eq("perfil_usuario->>id", usuarioId)}} // filtra por usuario actual
                .decodeList<PedidoDetalle>()
        } catch (e: Exception) {
            println("❌ Error al obtener pedidos desde la vista: ${e.message}")
            emptyList()
        }
    }

    suspend fun obtenerFacturasUsuario(usuarioId: String): List<Factura> {
        val res = SupabaseProvider.client
            .from("facturas")
            .select{ filter { eq ("id_usuario", usuarioId) }}
            .decodeList<Factura>()
        return res
    }

    // Llamar a Edge Function para crear el PDF en el bucket
    @OptIn(InternalAPI::class)
    suspend fun generarFacturaPdf(facturaId: Long): String? {
        // Llamamos a la Edge Function "generea"
        val res = SupabaseProvider.client.functions.invoke("generea") {
            headers["Content-Type"] = "application/json" // 👈 IMPORTANTE
            body = """{"facturaId": $facturaId}"""       // 👈 body como String JSON
        }

        val bodyStr = res.bodyAsText()
        println("📄 Respuesta Edge Function: $bodyStr")

        val json = Json.parseToJsonElement(bodyStr).jsonObject
        val pdfUrl = json["pdfUrl"]?.jsonPrimitive?.content

        if (pdfUrl != null)
            println("✅ Factura generada correctamente: $pdfUrl")
        else
            println("⚠️ No se devolvió URL de PDF en la respuesta.")

        return pdfUrl
    }
    @OptIn(InternalAPI::class)
    suspend fun generarFactura(pedidoId: Long): String? {
        println("🧾 Generando factura completa para pedido #$pedidoId ...")

        try {
            // ✅ Llamamos a la Edge Function que usa el service_role
            val res = SupabaseProvider.client.functions.invoke("generea") {
                headers["Content-Type"] = "application/json"
                body = """{"pedidoId": $pedidoId}"""
            }

            val bodyStr = res.bodyAsText()
            println("📄 Respuesta Edge Function: $bodyStr")

            val json = Json.parseToJsonElement(bodyStr).jsonObject
            val pdfUrl = json["pdfUrl"]?.jsonPrimitive?.content

            if (pdfUrl != null)
                println("✅ Factura generada correctamente: $pdfUrl")
            else
                println("⚠️ No se devolvió URL de PDF en la respuesta.")

            return pdfUrl
        } catch (e: Exception) {
            println("❌ Error al generar factura desde Edge Function: ${e.message}")
            return null
        }
    }


    suspend fun borrarPedido(pedidoId: Long) {
        supabase.from("pedido")
            .delete {
                filter { eq("id", pedidoId) }
            }
    }

    suspend fun obtenerTodasFacturas(): List<Factura> {
        return try {
            val res = SupabaseProvider.client.from("facturas")
                .select()
                .decodeList<Factura>()
            res
        } catch (e: Exception) {
            println("❌ Error al obtener todas las facturas: ${e.message}")
            emptyList()
        }
    }
    suspend fun actualizarDestacadoProducto(productoId: Long, destacado: Boolean) {
        supabase.from("productos")
            .update(
                mapOf("destacado" to destacado)
            ) {
                filter { eq("id", productoId) }
            }
    }

    suspend fun validarCodigo(codigo: String): CodigoDescuento? {
        val result = client
            .from("codigos_descuento")
            .select{filter { eq("codigo", codigo) }}
            .decodeSingleOrNull<CodigoDescuento>()

        val hoy = kotlinx.datetime.Clock.System.now()
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date

        return if (result != null &&
            result.activo &&
            hoy >= result.fecha_inicio &&
            hoy <= result.fecha_fin &&
            result.uso_actual < result.uso_maximo
        ) {
            result
        } else null
    }

    suspend fun obtenerCodigosDescuento(): List<CodigoDescuento> {
        return client.from("codigos_descuento").select().decodeList()
    }

    suspend fun toggleActivoCodigo(id: String, nuevoEstado: Boolean) {
        client.from("codigos_descuento")
            .update(mapOf("activo" to nuevoEstado)) {
                filter { eq("id", id) }
            }
    }

    suspend fun crearCodigoDescuento(codigo: CodigoDescuento) {
        val json = buildJsonObject {
            put("codigo", codigo.codigo)
            put("tipo", codigo.tipo)
            put("valor", codigo.valor)
            put("fecha_inicio", codigo.fecha_inicio.toString())
            put("fecha_fin", codigo.fecha_fin.toString())
            put("uso_maximo", codigo.uso_maximo)
            put("uso_actual", codigo.uso_actual)
            put("activo", codigo.activo)
        }

        client.from("codigos_descuento").insert(json)
    }

    suspend fun eliminarCodigoDescuento(id: String): Boolean {
        return try {
            val response = client.from("codigos_descuento")
                .delete {
                    filter { eq("id", id) }
                }

            println("🗑️ Código eliminado correctamente (ID: $id)")
            true
        } catch (e: Exception) {
            println("❌ Error al eliminar código: ${e.message}")
            false
        }
    }

    suspend fun obtenerComentariosProducto(productoId: Long): List<ComentarioConUsuario> {
        val response = client
            .from("comentarios_producto")
            .select(
                columns = Columns.raw("id, id_producto, id_usuario, comentario, fecha, perfil_usuario(nombre_completo)")
            ) {
                filter { eq("id_producto", productoId) }
            }

        val data = response.decodeList<JsonObject>()

        return data.map { json ->
            ComentarioConUsuario(
                id = json["id"]?.jsonPrimitive?.longOrNull,
                id_producto = json["id_producto"]!!.jsonPrimitive.long,
                id_usuario = json["id_usuario"]!!.jsonPrimitive.content,
                comentario = json["comentario"]!!.jsonPrimitive.content,
                fecha = json["fecha"]?.jsonPrimitive?.contentOrNull,
                nombre_usuario = json["perfil_usuario"]?.jsonObject
                    ?.get("nombre_completo")?.jsonPrimitive?.contentOrNull
            )
        }
    }

    suspend fun agregarComentario(productoId: Long, usuarioId: String, texto: String): ComentarioConUsuario? {
        return try {
            // 1️⃣ Insertar comentario
            val nuevoComentario = NuevoComentario(
                id_producto = productoId,
                id_usuario = usuarioId,
                comentario = texto
            )

            client.from("comentarios_producto")
                .insert(listOf(nuevoComentario))

            println("💬 Comentario agregado al producto $productoId por usuario $usuarioId")

            // 2️⃣ Obtener el comentario recién insertado con el nombre del usuario
            val response = client.from("comentarios_producto")
                .select(
                    columns = Columns.raw("id, id_producto, id_usuario, comentario, fecha, perfil_usuario(nombre_completo)")
                ) {
                    filter {
                        eq("id_usuario", usuarioId)
                        eq("id_producto", productoId)
                    }
                    limit(1)
                }

            val json = response.decodeSingleOrNull<JsonObject>() ?: return null

            return ComentarioConUsuario(
                id = json["id"]?.jsonPrimitive?.longOrNull,
                id_producto = json["id_producto"]!!.jsonPrimitive.long,
                id_usuario = json["id_usuario"]!!.jsonPrimitive.content,
                comentario = json["comentario"]!!.jsonPrimitive.content,
                fecha = json["fecha"]?.jsonPrimitive?.contentOrNull,
                nombre_usuario = json["perfil_usuario"]?.jsonObject
                    ?.get("nombre_completo")?.jsonPrimitive?.contentOrNull
            )
        } catch (e: Exception) {
            println("❌ Error al agregar comentario: ${e.message}")
            null
        }
    }


    suspend fun obtenerProductoPorId(productoId: Long): Product? {
        return client.from("productos")
            .select{ filter { eq("id", productoId) }}
            .decodeSingleOrNull<Product>()
    }

    @kotlinx.serialization.Serializable
    data class PedidoConToken(
        val id_usuario: String,
        val perfil_usuario: PerfilUsuarioToken? = null
    )

    @kotlinx.serialization.Serializable
    data class PerfilUsuarioToken(
        val fcm_token: String? = null
    )

    //EMPRESAS
    suspend fun getProductosEmpresa(empresaId: Long): List<ProductEmpresa> {
        return try {
            val result = supabase.postgrest
                .rpc(
                    function = "obtener_productos_empresa",
                    parameters = mapOf("empresa" to empresaId)
                )
                .decodeList<ProductEmpresa>()

            println("✅ Productos obtenidos para empresa $empresaId: ${result.size}")
            result
        } catch (e: Exception) {
            println("❌ Error al obtener productos de empresa: ${e.message}")
            emptyList()
        }
    }


    suspend fun updatePrecioTodasEmpresas(productoId: Long, nuevoPrecio: Double): Boolean {
        return try {
            SupabaseProvider.adminClient.postgrest.rpc(
                function = "actualizar_precio_todas_empresas",
                parameters = mapOf(
                    "producto" to productoId,
                    "nuevo_precio" to nuevoPrecio
                )
            )

            println("✅ Precio actualizado globalmente para producto $productoId")
            true
        } catch (e: Exception) {
            println("❌ Error al actualizar precios globales: ${e.message}")
            false
        }
    }

    suspend fun obtenerEmpresasAdmin(): List<EmpresaAdmin> {
        return try {
            client.from("empresas").select().decodeList<EmpresaAdmin>()

        } catch (e: Exception) {
            println("❌ Error al obtener empresas: ${e.message}")
            emptyList()
        }
    }

    suspend fun crearUsuarioEmpresa(email: String, password: String): String? {
        return try {
            val response = SupabaseProvider.adminClient.httpClient.post(
                "${SupabaseEnv.URL}/auth/v1/admin/users"
            ) {
                headers {
                    append("apikey", SupabaseEnv.SERVICE_ROLE_KEY)
                    append("Authorization", "Bearer ${SupabaseEnv.SERVICE_ROLE_KEY}")
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
                setBody("""{"email":"$email","password":"$password","email_confirm":false}""")
            }

            val bodyText = response.bodyAsText()
            println("📬 Respuesta Supabase Auth → ${response.status}: $bodyText")

            if (!response.status.isSuccess()) return null

            val json = Json.parseToJsonElement(bodyText).jsonObject
            val userId = json["id"]?.jsonPrimitive?.content
            println("✅ Usuario empresa creado con ID: $userId")

            userId
        } catch (e: Exception) {
            println("❌ Excepción creando usuario empresa: ${e.message}")
            null
        }
    }

    suspend fun guardarPrecioEmpresa(
        empresaId: Long,
        productoId: Long,
        nuevoPrecio: Double,
        personalizado: Boolean = true,
        destacado: Boolean = false
    ): Boolean {
        return try {

            // 1️⃣ Buscamos si ya existe un precio personalizado para ese producto y empresa
            val existente = SupabaseProvider.adminClient.postgrest["empresa_productos"]
                .select()
                {
                    filter {
                        eq("empresa_id", empresaId)
                        eq("producto_id", productoId)
                    }
                }
                .decodeList<EmpresaProducto>()
                .firstOrNull()

            if (existente == null) {

                // 2️⃣ No existe → INSERT
                SupabaseProvider.adminClient.postgrest["empresa_productos"].insert(
                    EmpresaProducto(
                        empresa_id = empresaId,
                        producto_id = productoId,
                        precio_empresa = nuevoPrecio,
                        personalizado = personalizado,
                        destacado = destacado
                    )
                )

                println("🟢 INSERT empresa_productos → empresaId=$empresaId productoId=$productoId precio=$nuevoPrecio")

            } else {

                // 3️⃣ Sí existe → UPDATE
                SupabaseProvider.adminClient.postgrest["empresa_productos"].update(
                    EmpresaProducto(
                        id = existente.id,
                        empresa_id = empresaId,
                        producto_id = productoId,
                        precio_empresa = nuevoPrecio,
                        personalizado = personalizado,
                        destacado = destacado,
                        actualizado_en = null // lo puede manejar Supabase por trigger si tienes uno
                    )
                ) {
                    filter {
                        eq("id", existente.id!!)
                    }
                }

                println("🟡 UPDATE empresa_producto → id=${existente.id} nuevoPrecio=$nuevoPrecio")
            }

            true

        } catch (e: Exception) {
            println("❌ Error en guardarPrecioEmpresa: ${e.message}")
            false
        }
    }

    suspend fun obtenerEmpresaPorId(empresaId: Long): Empresa? {
        return try {
            supabase.postgrest["empresas"]
                .select()
                {
                    filter {
                        eq("id", empresaId)
                    }
                }
                .decodeSingle<Empresa>()
                .also { println("🏢 Empresa cargada: ${it.nombre_empresa}") }

        } catch (e: Exception) {
            println("❌ Error obteniendo empresa $empresaId: ${e.message}")
            null
        }
    }

    suspend fun obtenerProductoEmpresaPorId(empresaId: Long, productoId: Long): ProductEmpresa? {
        return try {
            supabase.postgrest
                .rpc(
                    function = "obtener_productos_empresa",
                    parameters = mapOf("empresa" to empresaId)
                )
                .decodeList<ProductEmpresa>()
                .firstOrNull { it.id == productoId }
        } catch (e: Exception) {
            println("❌ Error obteniendo producto empresa por id: ${e.message}")
            null
        }
    }


}


