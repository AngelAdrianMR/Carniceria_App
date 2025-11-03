package com.example.carniceria_app

import androidx.compose.foundation.clickable
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
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.PromocionConProductos
import com.carniceria.shared.shared.models.utils.cambiarEstadoPromocion
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocionesAdminScreen(
    viewModel: PromocionesAdminViewModel = viewModel(),
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val promociones by viewModel.promociones.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    var mostrarFormulario by remember { mutableStateOf(false) }
    var promocionSeleccionada by remember { mutableStateOf<PromocionConProductos?>(null) }

    val scope = rememberCoroutineScope()

    // 🧱 Estructura principal
    Scaffold(
        topBar = {
            UpBarAdmin(
                navController = navController,
                titulo = "Gestión de Promociones",
                onLogout = onLogout
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    promocionSeleccionada = null // 👉 modo nueva promoción
                    mostrarFormulario = true     // 👉 abre el panel
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Promoción")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            when {
                loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                error != null -> Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(promociones) { promoConProductos ->
                        val promo = promoConProductos.promocion

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    promo.id?.let { id ->
                                        navController.navigate("promocionDetalle/$id")
                                    }
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        promo.nombre_promocion,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Switch(
                                        checked = promo.estado,
                                        onCheckedChange = { checked ->
                                            scope.launch {
                                                promo.id?.let { idReal ->
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
                                    "Precio: ${promo.precio_total} €",
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
                                            println("🗑️ Eliminando promoción ID: ${promo.id}")
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

        // 🧩 Panel para crear/editar promoción
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
