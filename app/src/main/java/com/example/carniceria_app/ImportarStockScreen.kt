package com.example.carniceria_app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.Product
import com.example.carniceria_app.ImportarStockViewModel
import com.example.carniceria_app.UpBarAdmin
import kotlinx.coroutines.launch

@Composable
fun ImportarStockScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    viewModel: ImportarStockViewModel = viewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var mostrarMenuLateral by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                // 🧱 Usamos aquí la nueva barra admin reutilizable
                UpBarAdmin(
                    navController = navController,
                    titulo = "Panel de Administración",
                    onLogout = onLogout
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.guardarCambios()
                            snackbarHostState.showSnackbar("✅ Stock actualizado correctamente")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Actualizar stock total")
                }
            }
        ) { paddingValues ->

            if (productos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    items(productos) { producto ->
                        FilaImportarStock(
                            producto = producto,
                            cantidadActual = viewModel.cantidades[producto.id] ?: 0,
                            onCantidadChange = { nuevaCantidad ->
                                viewModel.actualizarCantidad(producto.id, nuevaCantidad)
                            }
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

    }
}

@Composable
fun FilaImportarStock(
    producto: Product,
    cantidadActual: Int,
    onCantidadChange: (Int) -> Unit
) {
    var textoCantidad by remember(producto.id) {
        mutableStateOf(if (cantidadActual > 0) cantidadActual.toString() else "")
    }

    val stockActual = producto.stock_producto ?: 0.0
    val unidad = producto.unidad_medida ?: "g"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 🏷️ Nombre + stock actual
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = producto.nombre_producto ?: "Producto sin nombre",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = "Stock actual: $stockActual $unidad",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 🔢 Campo + unidad
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = textoCantidad,
                onValueChange = { valor ->
                    textoCantidad = valor
                    onCantidadChange(valor.toIntOrNull() ?: 0)
                },
                label = { Text("+") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(90.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = unidad,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
