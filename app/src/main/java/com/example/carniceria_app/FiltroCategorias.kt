package com.example.carniceria_app

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun FiltroCategorias(categorias: List<String>, onSeleccion: (String?) -> Unit) {
    Row(Modifier.padding(8.dp)) {
        Button(onClick = { onSeleccion(null) }) {
            Text("Todos")
        }
        Spacer(Modifier.width(8.dp))
        categorias.forEach { categoria ->
            Button(onClick = { onSeleccion(categoria) }) {
                Text(categoria)
            }
            Spacer(Modifier.width(8.dp))
        }
    }
}
