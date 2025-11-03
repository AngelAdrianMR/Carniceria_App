package com.example.carniceria_app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.carniceria.shared.shared.models.utils.Product
import com.carniceria.shared.shared.models.utils.ComentarioConUsuario
import com.carniceria.shared.shared.models.utils.SupabaseService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDetalleScreen(
    navController: NavController,
    productoId: Long,
    usuarioId: String,
    service: SupabaseService
) {
    var producto by remember { mutableStateOf<Product?>(null) }
    var comentarios by remember { mutableStateOf<List<ComentarioConUsuario>>(emptyList()) }
    var nuevoComentario by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // 🔹 Cargar producto y comentarios al abrir
    LaunchedEffect(productoId) {
        scope.launch {
            try {
                producto = service.obtenerProductoPorId(productoId)
                comentarios = service.obtenerComentariosProducto(productoId)
            } catch (e: Exception) {
                Log.e("ProductoDetalleScreen", "❌ Error cargando datos", e)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(producto?.nombre_producto ?: "Producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->

        if (producto == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // 🖼️ Imagen del producto
                producto!!.imagen_producto?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = producto!!.nombre_producto,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(producto!!.descripcion_producto ?: "Sin descripción")
                Spacer(Modifier.height(8.dp))
                Text("💶 Precio: ${producto!!.precio_venta} €")
                Spacer(Modifier.height(16.dp))

                Divider(thickness = 1.dp)
                Text(
                    "Comentarios",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // 🧾 Lista de comentarios
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(comentarios) { comentario ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = comentario.nombre_usuario ?: "Usuario desconocido",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    comentario.comentario,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    comentario.fecha ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 💬 Input para añadir nuevo comentario
                OutlinedTextField(
                    value = nuevoComentario,
                    onValueChange = { nuevoComentario = it },
                    label = { Text("Escribe un comentario") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                BotonTransparenteNegro(
                    onClick = {
                        if (nuevoComentario.isNotBlank() && producto?.id != null) {
                            scope.launch {
                                val nuevo = service.agregarComentario(
                                    productoId = producto!!.id!!,
                                    usuarioId = usuarioId,
                                    texto = nuevoComentario
                                )

                                if (nuevo != null) {
                                    comentarios = listOf(nuevo) + comentarios // 👈 se añade arriba
                                    nuevoComentario = ""

                                    notificarAdmins(
                                        titulo = "Nuevo comentario 💬",
                                        cuerpo = "Un cliente ha comentado en el producto #$productoId."
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    texto = "💬 Añadir comentario"
                )
            }
        }
    }
}
