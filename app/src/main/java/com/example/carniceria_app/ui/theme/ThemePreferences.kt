package com.example.carniceria_app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore singleton
val Context.themeDataStore by preferencesDataStore(name = "settings")

class ThemePreferences(private val context: Context) {

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
    }

    // Leer el modo oscuro guardado
    val darkThemeFlow: Flow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY] ?: false
    }

    // Guardar el valor del modo oscuro
    suspend fun saveDarkTheme(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }
}
