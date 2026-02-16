package com.example.carniceria_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// MODO CLARO
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF000000),
    onPrimary = Color.White,

    secondary = Color(0xFF3F3F3F),
    onSecondary = Color.White,

    error = Color(0xFFBE2323),
    onError = Color.White,

    // ✅ Fondo general: blanco puro
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111111),

    // ✅ Superficies principales (cards): blanco
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),

    // ✅ “Cajas / bloques”: gris claro (en vez de azulito)
    surfaceVariant = Color(0xFFFFFFFF),      // o #F5F5F5 si lo quieres aún más suave
    onSurfaceVariant = Color(0xFF111111)
)

// MODO OSCURO
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF021019),

    secondary = Color(0xFFA20B0B),
    onSecondary = Color(0xFFFDFDFD),

    error = Color(0xFF7A0E0E),
    onError = Color(0xFF3B0000),

    background = Color(0xFF050608),
    onBackground = Color(0xFFF5F5F5),

    surface = Color(0xFF121212),
    onSurface = Color(0xFFF5F5F5),

    surfaceVariant = Color(0xFF101820),      // un pelín más claro que el fondo
    onSurfaceVariant = Color(0xFFF5F5F5)
)
// ==========================
// 🌗 Tema principal
// ==========================
@Composable
fun CarniceriaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
