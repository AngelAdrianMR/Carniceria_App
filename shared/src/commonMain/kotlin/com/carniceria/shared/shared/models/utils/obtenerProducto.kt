package com.carniceria.shared.shared.models.utils

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest

// ========================================================
// 🥩 PRODUCTOS
// ========================================================

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
            .insert(producto) { select() }
            .decodeSingleOrNull<Product>()
    } catch (e: Exception) {
        println("❌ Error al insertar producto: ${e.message}")
        null
    }
}

suspend fun actualizarProducto(producto: Product): Boolean {
    return try {
        SupabaseProvider.client.postgrest["productos"]
            .update({
                set("nombre_producto", producto.nombre_producto)
                set("descripcion_producto", producto.descripcion_producto)
                set("categoria_producto", producto.categoria_producto)
                set("precio_venta", producto.precio_venta)
                set("precio_compra", producto.precio_compra)
                set("imagen_producto", producto.imagen_producto)
                set("unidad_medida", producto.unidad_medida)
                set("stock_producto", producto.stock_producto)
            }) {
                filter { eq("id", producto.id!!) }
                select()
            }
            .decodeSingleOrNull<Product>() != null
    } catch (e: Exception) {
        println("❌ Error al actualizar producto: ${e.message}")
        false
    }
}

/**
 * Importa cantidades de stock sumando al stock actual de cada producto.
 * Recibe un mapa con ID → cantidad a sumar.
 */
suspend fun actualizarStockProductos(cantidades: Map<Long, Int>): Boolean {
    return try {
        for ((id, cantidadASumar) in cantidades) {
            try {
                // 🧩 1️⃣ Obtener el producto actual
                val productoActual = SupabaseProvider.client.postgrest["productos"]
                    .select { filter { eq("id", id) } }
                    .decodeSingle<Product>()

                val nuevoStock = productoActual.stock_producto?.plus(cantidadASumar)

                // 🧩 2️⃣ Actualizar el stock sumado
                SupabaseProvider.client.postgrest["productos"]
                    .update({
                        set("stock_producto", nuevoStock)
                    }) {
                        filter { eq("id", id) }
                    }

                println("✅ Stock actualizado para '${productoActual.nombre_producto}': → $nuevoStock")
            } catch (e: Exception) {
                println("⚠️ Error en producto ID $id: ${e.message}")
            }
        }
        println("✅ Importación de stock completada.")
        true
    } catch (e: Exception) {
        println("❌ Error general al importar stock: ${e.message}")
        false
    }
}

suspend fun eliminarProductoPorId(id: Long): Boolean {
    return try {
        SupabaseProvider.client.postgrest["productos"]
            .delete{ filter{eq("id", id)}}

        println("✅ Producto eliminado correctamente (ID: $id)")
        true
    } catch (e: Exception) {
        println("❌ Error al eliminar producto: ${e.message}")
        false
    }
}

// ========================================================
// 🎟️ PROMOCIONES
// ========================================================

suspend fun obtenerPromociones(): List<PromocionConProductos> {
    return try {
        val promociones = SupabaseProvider.client.postgrest["combos_promociones"]
            .select { filter { eq("estado", true) } } // 👈 Solo activas
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
        // 🧩 Primero eliminamos la promoción
        SupabaseProvider.client.postgrest["combos_promociones"]
            .delete {
                filter { eq("id", id) } // 👈 WHERE id = $id
            }

         //🧩 Luego las relaciones asociadas
        SupabaseProvider.client.postgrest["producto_promociones"]
            .delete {
                filter { eq("promocion_id", id) } // 👈 WHERE promocion_id = $id
            }

        println("✅ Promoción eliminada correctamente (ID: $id)")
        true
    } catch (e: Exception) {
        println("❌ Error al eliminar promoción: ${e.message}")
        false
    }
}

suspend fun eliminarRelacionesPromocion(promoId: Long): Boolean {
    return try {
        SupabaseProvider.client.postgrest["producto_promociones"]
            .delete { filter { eq("promocion_id", promoId) } }
        true
    } catch (e: Exception) {
        println("❌ Error al eliminar relaciones: ${e.message}")
        false
    }
}

suspend fun guardarRelaciones(promoId: Long, productos: List<Product>): Boolean {
    return try {
        // 🧩 Borrar relaciones previas
        SupabaseProvider.client.postgrest["producto_promociones"]
            .delete { filter { eq("promocion_id", promoId) } }

        // 🧩 Insertar nuevas
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

suspend fun cambiarEstadoPromocion(id: Long, nuevoEstado: Boolean): Boolean {
    return try {
        SupabaseProvider.client
            .from("combos_promociones")
            .update(mapOf("estado" to nuevoEstado)) {
                filter { eq("id", id) }
            }
        true
    } catch (e: Exception) {
        println("❌ Error al cambiar estado promoción: ${e.message}")
        false
    }
}

// ========================================================
// ⚙️ RPC (Funciones en Supabase)
// ========================================================

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

suspend fun obtenerPromocionConProductos(promocionId: Long): PromocionConProductos? {
    return try {
        val promo = SupabaseProvider.client.postgrest["combos_promociones"]
            .select { filter { eq("id", promocionId) } }
            .decodeSingle<Promocion>()

        val relaciones = SupabaseProvider.client.postgrest["producto_promociones"]
            .select { filter { eq("promocion_id", promocionId) } }
            .decodeList<ProductoPromocion>()

        val productos = obtenerProductos()

        val productosDeEstaPromo = relaciones.mapNotNull { rel ->
            productos.find { it.id == rel.producto_id }
        }

        PromocionConProductos(promocion = promo, productos = productosDeEstaPromo)
    } catch (e: Exception) {
        println("❌ Error al obtener promoción: ${e.message}")
        null
    }
}
