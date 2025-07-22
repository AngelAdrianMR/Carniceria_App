package com.example.carniceria_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.carniceria.shared.shared.models.utils.Product

@Composable
fun GridProductos(productos: List<Product>, onAddClick: (Product) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(productos) { producto ->
            Column(modifier = Modifier.padding(4.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(producto.imagen_producto ?: ""),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                )
                Text(producto.nombre_producto, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${producto.precio_venta} € / ${producto.unidad_medida}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = { onAddClick(producto) }) {
                    Text("Añadir")
                }
            }
        }
    }
}
