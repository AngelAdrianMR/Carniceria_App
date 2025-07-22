package com.example.carniceria_app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carniceria.shared.shared.models.utils.CarritoItem
import kotlinx.coroutines.launch

@Composable
fun CarritoLateral(
    carrito: List<CarritoItem>,
    direccionUsuario: String,
    onCerrar: () -> Unit,
    onReservar: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    val scope = rememberCoroutineScope()

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
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Carrito de compra", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (carrito.isEmpty()) {
                        Text("El carrito está vacío.")
                    } else {
                        var totalPrecio = 0.0

                        carrito.forEach { item ->
                            val precioUnitario = item.producto.precio_venta
                            val cantidad = item.cantidad
                            val subTotal = precioUnitario * cantidad
                            totalPrecio += subTotal

                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                Text("- ${item.producto.nombre_producto} x ${item.cantidad}")
                                Text(" ${precioUnitario} € x $cantidad = ${"%.2f".format(subTotal)} €")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(12.dp))

                        if (envioValido) {
                            Text("Envío: ${"%.2f".format(envioCoste)} €")
                            totalPrecio += envioCoste
                        } else {
                            Text("Dirección fuera de zona de reparto", color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Total: ${"%.2f".format(totalPrecio)} €", style = MaterialTheme.typography.titleMedium)

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (envioValido) onReservar()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = envioValido
                        ) {
                            Text("Reservar")
                        }

                        OutlinedButton(
                            onClick = onCerrar,
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
