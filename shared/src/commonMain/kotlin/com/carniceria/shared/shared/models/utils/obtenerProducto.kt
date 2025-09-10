package com.carniceria.shared.shared.models.utils

import io.github.jan.supabase.postgrest.postgrest

// ------------------ PRODUCTOS ------------------

suspend fun obtenerProductos(): List<Product> {
    return try {
        SupabaseProvider.client.postgrest["productos"]
            .select()
            .decodeList<Product>()
    } catch (e: Exception) {
        println("❌ Error al obtener productos: ${e.message}")
        emptyList()
    }
}

suspend fun insertarProducto(producto: Product): Product? {
    return try {
        SupabaseProvider.client.postgrest["productos"]
            .insert(producto) {select()}
            .decodeSingleOrNull<Product>()
    } catch (e: Exception) {
        println("❌ Error al insertar producto: ${e.message}")
        null
    }
}

suspend fun actualizarProducto(producto: Product): Boolean {
    return try {
        SupabaseProvider.client.postgrest["productos"]
            .update(
                {
                    set("nombre_producto", producto.nombre_producto)
                    set("descripcion_producto", producto.descripcion_producto)
                    set("categoria_producto", producto.categoria_producto)
                    set("precio_venta", producto.precio_venta)
                    set("precio_compra", producto.precio_compra)
                    set("imagen_producto", producto.imagen_producto)
                    set("unidad_medida", producto.unidad_medida)
                    set("stock_producto", producto.stock_producto)
                }
            ) {
                filter { eq("id", producto.id!!) }  // usamos el ID solo en el filtro
                select()
            }
            .decodeSingleOrNull<Product>() != null
    } catch (e: Exception) {
        println("❌ Error al actualizar producto: ${e.message}")
        false
    }
}


suspend fun eliminarProductoPorId(id: Long): Boolean {
    return try {
        SupabaseProvider.client.postgrest["productos"]
            .delete {
                filter {
                    "id" to "eq.$id"
                }
            }
        true
    } catch (e: Exception) {
        println("❌ Error al eliminar producto: ${e.message}")
        false
    }
}

suspend fun actualizarStockProducto(id: Long, nuevoStock: Int): Boolean {
    return try {
        SupabaseProvider.client.postgrest["productos"]
            .update(
                { set("stock_producto", nuevoStock) } // 🔄 actualiza solo el stock
            ) {
                filter { "id" to "eq.$id" }
                select()
            }
        true
    } catch (e: Exception) {
        println("❌ Error al actualizar stock: ${e.message}")
        false
    }
}

// ------------------ PROMOCIONES ------------------

suspend fun obtenerPromociones(): List<PromocionConProductos> {
    return try {
        val promociones = SupabaseProvider.client.postgrest["combos_promociones"]
            .select{
                filter { eq("estado", true) }   // 👈 solo activas
            }
            .decodeList<Promocion>()

        val relaciones = SupabaseProvider.client.postgrest["producto_promociones"]
            .select()
            .decodeList<ProductoPromocion>()

        val productos = obtenerProductos()

        promociones.mapNotNull { promo ->
            val productosDeEstaPromo = relaciones
                .filter { it.promocion_id == promo.id }
                .mapNotNull { rel -> productos.find { it.id == rel.producto_id } }

            if (productosDeEstaPromo.isNotEmpty()) {
                PromocionConProductos(promocion = promo, productos = productosDeEstaPromo)
            } else null
        }
    } catch (e: Exception) {
        println("❌ Error al obtener promociones: ${e.message}")
        emptyList()
    }
}

suspend fun obtenerPromocionesAdmin(): List<PromocionConProductos> {
    return try {
        val promociones = SupabaseProvider.client.postgrest["combos_promociones"]
            .select()
            .decodeList<Promocion>()

        val relaciones = SupabaseProvider.client.postgrest["producto_promociones"]
            .select()
            .decodeList<ProductoPromocion>()

        val productos = obtenerProductos()

        promociones.mapNotNull { promo ->
            val productosDeEstaPromo = relaciones
                .filter { it.promocion_id == promo.id }
                .mapNotNull { rel -> productos.find { it.id == rel.producto_id } }

            if (productosDeEstaPromo.isNotEmpty()) {
                PromocionConProductos(promocion = promo, productos = productosDeEstaPromo)
            } else null
        }
    } catch (e: Exception) {
        println("❌ Error al obtener promociones: ${e.message}")
        emptyList()
    }
}

suspend fun insertarPromocion(promocion: Promocion): Promocion? {
    return try {
        val promoSinId = promocion.copy(id = null)
        SupabaseProvider.client.postgrest["combos_promociones"]
            .insert(promoSinId) { select() }
            .decodeSingleOrNull<Promocion>()
    } catch (e: Exception) {
        println("❌ Error al insertar promoción: ${e.message}")
        null
    }
}


suspend fun actualizarPromocion(promocion: Promocion): Boolean {
    val id = promocion.id ?: return false
    return try {
        SupabaseProvider.client.postgrest["combos_promociones"]
            .update(promocion) {
                filter { eq("id", id) }
                select()
            }
        true
    } catch (e: Exception) {
        println("❌ Error al actualizar promoción: ${e.message}")
        false
    }
}

suspend fun eliminarPromocionPorId(id: Long): Boolean {
    return try {
        SupabaseProvider.client.postgrest["combos_promociones"]
            .delete {
                filter {
                    "id" to "eq.$id"
                }
            }

        SupabaseProvider.client.postgrest["producto_promociones"]
            .delete {
                filter {
                    "promocion_id" to "eq.$id"
                }
            }

        true
    } catch (e: Exception) {
        println("❌ Error al eliminar promoción: ${e.message}")
        false
    }
}

suspend fun eliminarRelacionesPromocion(promoId: Long): Boolean {
    return try {
        SupabaseProvider.client.postgrest["producto_promociones"]
            .delete {
                filter { eq("promocion_id", promoId) }
            }
        true
    } catch (e: Exception) {
        println("❌ Error al eliminar relaciones: ${e.message}")
        false
    }
}


suspend fun guardarRelaciones(promoId: Long, productos: List<Product>): Boolean {
    return try {
        // Borrar relaciones previas
        SupabaseProvider.client.postgrest["producto_promociones"]
            .delete {
                filter { eq("promocion_id", promoId) }
            }

        // Insertar nuevas
        val nuevasRelaciones = productos.map {
            mapOf("promocion_id" to promoId, "producto_id" to it.id)
        }

        SupabaseProvider.client.postgrest["producto_promociones"]
            .insert(nuevasRelaciones)

        true
    } catch (e: Exception) {
        println("❌ Error al guardar relaciones: ${e.message}")
        false
    }
}

suspend fun cambiarEstadoPromocion(id: Long, estado: Boolean): Boolean {
    return try {
        SupabaseProvider.client.postgrest["combos_promociones"]
            .update({ set("estado", estado) }) {
                filter { eq("id", id) }
                select()
            }
        true
    } catch (e: Exception) {
        println("❌ Error al cambiar estado promoción: ${e.message}")
        false
    }
}

// ------------------ RPC ------------------

suspend fun obtenerCategoriasProducto(): List<String> {
    return try {
        SupabaseProvider.client.postgrest.rpc("get_categorias_producto")
            .decodeList<String>()
    } catch (e: Exception) {
        println("❌ Error al obtener categorías: ${e.message}")
        emptyList()
    }
}

suspend fun obtenerUnidadesMedida(): List<String> {
    return try {
        SupabaseProvider.client.postgrest.rpc("get_unidades_medida")
            .decodeList<String>()
    } catch (e: Exception) {
        println("❌ Error al obtener unidades: ${e.message}")
        emptyList()
    }
}
