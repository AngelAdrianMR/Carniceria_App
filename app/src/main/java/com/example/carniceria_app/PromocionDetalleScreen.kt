package com.example.carniceria_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.carniceria.shared.shared.models.utils.PromocionConProductos
import com.carniceria.shared.shared.models.utils.obtenerPromocionConProductos
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocionDetalleScreen(
    navController: NavHostController,
    promoId: Long,
    carritoViewModel: CarritoViewModel
) {
    var promo by remember { mutableStateOf<PromocionConProductos?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 🔄 Cargar promoción y sus productos desde Supabase al entrar
    LaunchedEffect(promoId) {
        scope.launch {
            try {
                promo = obtenerPromocionConProductos(promoId)
            } catch (e: Exception) {
                println("❌ Error al cargar promoción: ${e.message}")
            }
        }
    }

    if (promo == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        PromocionDetalleContent(
            promocionConProductos = promo!!,
            carritoViewModel = carritoViewModel,
            onVolver = { navController.popBackStack() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocionDetalleContent(
    promocionConProductos: PromocionConProductos,
    carritoViewModel: CarritoViewModel,
    onVolver: () -> Unit
) {
    val promo = promocionConProductos.promocion
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(promo.nombre_promocion) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Image(
                    painter = rememberAsyncImagePainter(promo.imagen_promocion ?: ""),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    promo.nombre_promocion,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    "${promo.precio_total} €",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    promo.descripcion_promocion ?: "Sin descripción disponible.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Incluye los siguientes productos:",
                    style = MaterialTheme.typography.titleMedium
                )

                // 🔹 Slider horizontal de productos
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    items(promocionConProductos.productos) { producto ->
                        Card(
                            modifier = Modifier
                                .width(180.dp)
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(producto.imagen_producto ?: ""),
                                    contentDescription = producto.nombre_producto,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    producto.nombre_producto,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )

                                Text(
                                    "${producto.precio_venta} €",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                BotonTransparenteNegro(
                    onClick = {
                        carritoViewModel.agregarPromocionAlCarrito(promocionConProductos, context)
                    },
                    texto = "🛒 Añadir promoción al carrito",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Las promociones no permiten modificaciones individuales en los productos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
