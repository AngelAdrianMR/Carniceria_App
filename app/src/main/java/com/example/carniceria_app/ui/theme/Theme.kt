package com.example.carniceria_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 🌞 MODO CLARO
private val LightColorScheme = lightColorScheme(
    // Acción principal (botones, top bar…)
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,

    // Acción secundaria / estados OK
    secondary = Color(0xFF2E7D32),
    onSecondary = Color.White,

    // Rojo para errores y botones destructivos
    error = Color(0xFFC62828),
    onError = Color.White,

    // Fondos y texto
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF111111),

    // Tarjetas / cards normales
    surface = Color.White,
    onSurface = Color(0xFF111111),

    // 👉 Paneles / bloques (como los que ves en el screenshot)
    surfaceVariant = Color(0xFFE3ECF9),      // azulito muy suave
    onSurfaceVariant = Color(0xFF111111)
)

// 🌙 MODO OSCURO
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF021019),

    secondary = Color(0xFFA5D6A7),
    onSecondary = Color(0xFF06120A),

    error = Color(0xFFEF9A9A),
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
