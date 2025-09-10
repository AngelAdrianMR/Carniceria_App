package com.example.carniceria_app

import android.content.Context

object ImagenPerfilManager {
    private const val PREFS_NAME = "perfil_prefs"
    private const val KEY_IMAGEN_URI = "imagen_uri"

    fun guardarImagenUri(context: Context, uri: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_IMAGEN_URI, uri)
            .apply()
    }

    fun cargarImagenUri(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_IMAGEN_URI, null)
    }
}
