package com.example.carniceria_app

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FiltroCategorias(
    categorias: List<String>,
    onSeleccion: (String?) -> Unit
) {
    var seleccionada by remember { mutableStateOf<String?>(null) }

    // Paleta personalizada (puedes cambiar los tonos)
    val colores = listOf(
        Color(0xFFD45D4C), // 1. Terracota (rojo suave principal)
        Color(0xFFB7C4A1), // 2. Verde oliva suave
        Color(0xFFE3C77D), // 3. Dorado cálido
        Color(0xFF9BB6C1), // 4. Azul grisáceo
        Color(0xFFAD8A64), // 5. Marrón claro / madera
        Color(0xFFA86C5D), // 6. Cobre cálido
        Color(0xFF6C8E7F), // 7. Verde salvia
        Color(0xFFC7A27C), // 8. Arena tostada
        Color(0xFF8C9FA1), // 9. Gris verdoso suave
        Color(0xFFDC9B7A), // 10. Melocotón cálido
        Color(0xFFB9946B), // 11. Camel natural
        Color(0xFF7E6651), // 12. Madera envejecida
        Color(0xFFA7B49E), // 13. Verde gris pálido
        Color(0xFFB46E72), // 14. Rosado terroso
        Color(0xFF7A8DA1)  // 15. Azul pizarra suave
    )


    LazyRow(modifier = Modifier.padding(8.dp)) {
        // 🔹 Botón "Todos"
        item {
            val isSelected = seleccionada == null
            BotonFiltro(
                onClick = {
                    seleccionada = null
                    onSeleccion(null)
                },
                texto = "Todos",
                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                borderColor = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
        }

        // 🔹 Botones de categorías
        itemsIndexed(categorias) { index, categoria ->
            val isSelected = seleccionada == categoria
            val color = colores[index % colores.size] // Repite la paleta si hay más categorías

            BotonFiltro(
                onClick = {
                    seleccionada = categoria
                    onSeleccion(categoria)
                },
                texto = categoria,
                containerColor = if (isSelected) color else Color.Transparent,
                contentColor = if (isSelected) Color.White else color,
                borderColor = color
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}
