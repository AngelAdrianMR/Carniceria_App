package com.example.carniceria_app

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
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

    LazyRow(modifier = Modifier.padding(8.dp)) {
        item {
            val isSelected = seleccionada == null
            BotonTransparenteNegro(
                onClick = {
                    seleccionada = null
                    onSeleccion(null)
                },
                texto = "Todos",
                modifier = Modifier,
            ).apply {
                ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) Color.Black else Color.Transparent,
                    contentColor = if (isSelected) Color.White else Color.Black
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        items(categorias) { categoria ->
            val isSelected = seleccionada == categoria
            BotonTransparenteNegro(
                onClick = {
                    seleccionada = categoria
                    onSeleccion(categoria)
                },
                texto = categoria,
                modifier = Modifier,
            ).apply {
                ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) Color.Black else Color.Transparent,
                    contentColor = if (isSelected) Color.White else Color.Black
                )
            }
            Spacer(Modifier.width(8.dp))
        }
    }
}
