package com.example.carniceria_app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 🎨 Paleta clara
private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    secondary = Color(0xFFD32F2F),   // rojo carne / acento
    onSecondary = Color.White
)

// 🌙 Paleta oscura
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBA1A1A),
    onPrimary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    secondary = Color(0xFFEF5350),
    onSecondary = Color.White
)

/**
 * 🎨 Tema general de la app Carnicería.
 * Usa modo claro/oscuro del sistema por defecto.
 */
@Composable
fun CarniceriaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // puedes personalizarlo luego
        shapes = Shapes(),         // idem para bordes redondeados
    ) {
        Surface(
            color = colorScheme.background,
            content = content
        )
    }
}
