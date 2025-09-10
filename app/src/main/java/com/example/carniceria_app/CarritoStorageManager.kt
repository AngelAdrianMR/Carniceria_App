package com.example.carniceria_app

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.carniceria.shared.shared.models.utils.CarritoItem

object CarritoStorageManager {
    private const val PREFS_NAME = "carrito_prefs"
    private const val KEY_CARRITO = "carrito_items"

    fun guardarCarrito(context: Context, carrito: List<CarritoItem>) {
        val json = Json.encodeToString(carrito)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CARRITO, json)
            .apply()
    }

    fun cargarCarrito(context: Context): List<CarritoItem> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CARRITO, null) ?: return emptyList()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

