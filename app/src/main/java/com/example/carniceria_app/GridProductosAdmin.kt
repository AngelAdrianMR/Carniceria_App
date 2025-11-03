package com.example.carniceria_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.rememberAsyncImagePainter
import com.carniceria.shared.shared.models.utils.Product

@Composable
fun GridProductosAdmin(
    productos: List<Product>,
    onEditarClick: (Product) -> Unit,
    onEliminarClick: (Product) -> Unit,
    onToggleDestacado: (Product, Boolean) -> Unit,
    onProductoClick: (Product) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(productos) { producto ->
            Card(
                modifier = Modifier
                    .padding(6.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {

                    // Imagen clicable
                    Image(
                        painter = rememberAsyncImagePainter(producto.imagen_producto ?: ""),
                        contentDescription = producto.nombre_producto,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onProductoClick(producto) }
                    )

                    // Nombre clicable
                    Text(
                        producto.nombre_producto,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { onProductoClick(producto) },
                        textDecoration = TextDecoration.Underline
                    )

                    // Precio
                    Text(
                        "${producto.precio_venta} € / ${producto.unidad_medida}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // --- Botones de acción (tamaño uniforme) ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val iconSize = 28.dp

                        // ✏️ Editar
                        IconButton(
                            onClick = { onEditarClick(producto) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                modifier = Modifier.size(iconSize)
                            )
                        }

                        // 🗑️ Eliminar
                        IconButton(
                            onClick = { onEliminarClick(producto) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                modifier = Modifier.size(iconSize)
                            )
                        }

                        // ⭐ Destacado con animación
                        val scale by animateFloatAsState(
                            targetValue = if (producto.destacado == true) 1.3f else 1f,
                            label = "Animación estrella"
                        )

                        IconButton(
                            onClick = {
                                producto.destacado?.let { onToggleDestacado(producto, !it) }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (producto.destacado == true)
                                    Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Destacado",
                                tint = if (producto.destacado == true)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .size(iconSize)
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
