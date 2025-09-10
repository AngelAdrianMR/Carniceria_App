package com.example.carniceria_app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.carniceria.shared.shared.models.utils.PromocionConProductos
import com.carniceria.shared.shared.models.utils.cambiarEstadoPromocion
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocionesAdminScreen(
    viewModel: PromocionesAdminViewModel = viewModel(),
    navController: NavController
) {
    val promociones by viewModel.promociones.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var mostrarFormulario by remember { mutableStateOf(false) }
    var promocionSeleccionada by remember { mutableStateOf<PromocionConProductos?>(null) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {
            UserHeader(
                title = "Promociones",
                current = HeaderTab.Productos,
                onNavigateHome = {
                    navController.navigate("homeAdminScreen") {
                        popUpTo("homeUserScreen") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigationToPerfil = { navController.navigate("perfilUser") },
                onNavigationToProduct = { /* ya estás en promociones */ },
                onNavigationToFacture = { navController.navigate("facturasUser") },
                onAbrirCarrito = { /* aquí no aplica */ },
                mostrarCarrito = false
            )

            Spacer(Modifier.height(16.dp))

            when {
                loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                error != null -> Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(promociones) { promoConProductos ->
                        Card {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        promoConProductos.promocion.nombre_promocion,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    // 🔘 Toggle para activar/desactivar
                                    Switch(
                                        checked = promoConProductos.promocion.estado,
                                        onCheckedChange = { checked ->
                                            scope.launch {
                                                promoConProductos.promocion.id?.let { idReal ->   // 👈 asegura que no sea null
                                                    val ok = cambiarEstadoPromocion(idReal, checked)
                                                    if (ok) {
                                                        viewModel.cargarDatos()
                                                    } else {
                                                        println("❌ Error al cambiar estado")
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }

                                Text(
                                    "Precio: ${promoConProductos.promocion.precio_total} €",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                promoConProductos.productos.forEach {
                                    Text(
                                        "- ${it.nombre_producto}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = {
                                        promocionSeleccionada = promoConProductos
                                        mostrarFormulario = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            println("🗑️ Eliminando promoción ID: ${promoConProductos.promocion.id}")
                                            viewModel.eliminarPromocion(promoConProductos)
                                            viewModel.cargarDatos()
                                        }
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                promocionSeleccionada = null
                mostrarFormulario = true
            },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nueva Promoción")
        }

        if (mostrarFormulario) {
            GestionPromocionBottomSheet(
                productos = productos,
                promocionInicial = promocionSeleccionada,
                onDismiss = {
                    mostrarFormulario = false
                    scope.launch { viewModel.cargarDatos() }
                }
            )
        }
    }
}

