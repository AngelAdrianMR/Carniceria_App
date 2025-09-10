package com.example.carniceria_app

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.carniceria.shared.shared.models.utils.CarritoItem
import kotlinx.coroutines.launch

@Composable
fun CarritoLateral(
    carrito: List<CarritoItem>,
    direccionUsuario: String,
    onCerrar: () -> Unit,
    onReservar: () -> Unit,            // 🚚 Pedir a domicilio
    onRecoger: () -> Unit,             // 🏪 Recoger en tienda
    onEliminarItem: (CarritoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    val scope = rememberCoroutineScope()

    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Closed) {
            onCerrar()
        }
    }

    var envioValido by remember { mutableStateOf(false) }
    var envioCoste by remember { mutableStateOf(0.0) }

    LaunchedEffect(direccionUsuario) {
        val (valido, coste) = validarEnvio(direccionUsuario)
        envioValido = valido
        envioCoste = coste
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = modifier.zIndex(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Carrito de compra", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (carrito.isEmpty()) {
                        Text("El carrito está vacío.")
                    } else {
                        var totalPrecio = carrito.sumOf { it.producto.precio_venta * it.cantidad }

                        carrito.forEach { item ->
                            val precioUnitario = item.producto.precio_venta
                            val cantidad = item.cantidad
                            val subTotal = precioUnitario * cantidad

                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "- ${item.producto.nombre_producto} x $cantidad",
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { onEliminarItem(item) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Text(
                                    "${"%.2f".format(precioUnitario)} € x $cantidad = ${"%.2f".format(subTotal)} €",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(12.dp))

                        if (envioValido) {
                            Text("Envío: ${"%.2f".format(envioCoste)} €")
                            totalPrecio += envioCoste
                        } else {
                            Text(
                                "Dirección fuera de zona de reparto",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Total: ${"%.2f".format(totalPrecio)} €",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 🚚 PEDIR A DOMICILIO
                        Button(
                            onClick = { if (envioValido) onReservar() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = envioValido
                        ) {
                            Text("Pedir a domicilio")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 🏪 RECOGER EN TIENDA
                        OutlinedButton(
                            onClick = { onRecoger() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Recoger en tienda")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { scope.launch { drawerState.close() } },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        },
        content = {}
    )
}

