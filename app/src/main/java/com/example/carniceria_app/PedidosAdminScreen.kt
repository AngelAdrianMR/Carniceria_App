package com.example.carniceria_app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.carniceria.shared.shared.models.utils.PedidoDetalle
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Row
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosAdminScreen(
    navController: NavHostController,
    viewModel: PedidosAdminViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pedidos by viewModel.pedidos.collectAsState()
    val cargandoPedidos by viewModel.cargandoPedidos.collectAsState()

    Scaffold(
        topBar = {
            // 🧱 Usamos aquí la nueva barra admin reutilizable
            UpBarAdmin(
                navController = navController,
                titulo = "Panel de Administración",
                onLogout = onLogout
            )
        }
    ) { padding ->

        // ✅ Spinner mientras carga
        if (pedidos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator() // 🔄 Indicador de carga
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                val pedidosVisibles = pedidos.filter {
                    it.pedido.estado != "entregado" && it.pedido.estado != "rechazado"
                }

                items(pedidosVisibles) { pedidoDetalle ->
                    val pedido = pedidoDetalle
                    val usuario = pedidoDetalle.usuario

                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {

                            val estaCargando = cargandoPedidos.contains(pedido.pedido.id)

                            if (estaCargando) {
                                // 🔄 Mostrar spinner dentro de la tarjeta
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                // 🧾 Contenido normal del pedido
                                Text("Pedido #${pedido.pedido.id}", style = MaterialTheme.typography.titleMedium)
                                Text("Estado: ${pedido.pedido.estado}")
                                Text("Total original: ${"%.2f".format(pedido.pedido.total)} €")

                                if (!pedido.pedido.codigo_descuento_aplicado.isNullOrEmpty()) {
                                    Text("Código aplicado: ${pedido.pedido.codigo_descuento_aplicado}")
                                }

                                if ((pedido.pedido.descuento_aplicado ?: 0.0) > 0.0) {
                                    Text(
                                        "Descuento: -${"%.2f".format(pedido.pedido.descuento_aplicado!!)} €",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if ((pedido.pedido.total_con_descuento ?: pedido.pedido.total) != pedido.pedido.total) {
                                    Text(
                                        "Total con descuento: ${"%.2f".format(pedido.pedido.total_con_descuento!!)} €",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                usuario?.let {
                                    Spacer(Modifier.height(6.dp))
                                    Text("Tipo de entrega: ${pedido.pedido.tipo_entrega}")
                                    Text("Usuario: ${it.id}")
                                    Text("Teléfono: ${it.telefono}")
                                    Text("Dirección: ${it.direccionCompleta}")
                                }

                                Spacer(Modifier.height(8.dp))

                                pedidoDetalle.lineas?.forEach { linea ->
                                    when {
                                        linea.producto != null -> {
                                            Text("- ${linea.producto!!.nombre_producto} x${linea.cantidad} - ${linea.precio_unitario}€")
                                            linea.mensaje?.takeIf { it.isNotBlank() }?.let { mensaje ->
                                                Text(
                                                    "   Nota: $mensaje",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }
                                        }

                                        linea.promocion != null -> {
                                            Text("Promoción: ${linea.promocion!!.promocion.nombre_promocion} x${linea.cantidad} - ${linea.precio_unitario}€")
                                            linea.promocion!!.productos.forEach { prodPromo ->
                                                Text(
                                                    "   - ${prodPromo.nombre_producto}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }

                                        else -> Text("Item desconocido")
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    when (pedido.pedido.estado) {
                                        "pendiente" -> {
                                            OutlinedButton(onClick = {
                                                scope.launch { viewModel.cambiarEstadoPedido(pedido.pedido.id, "aceptado") }
                                            }) { Text("Aceptar") }

                                            OutlinedButton(onClick = {
                                                scope.launch { viewModel.cambiarEstadoPedido(pedido.pedido.id, "rechazado") }
                                            }) { Text("Rechazar") }
                                        }

                                        "aceptado" -> {
                                            OutlinedButton(onClick = {
                                                scope.launch { viewModel.cambiarEstadoPedido(pedido.pedido.id, "enviado") }
                                            }) { Text("Marcar enviado") }

                                            OutlinedButton(onClick = {
                                                scope.launch { viewModel.cambiarEstadoPedido(pedido.pedido.id, "pendiente") }
                                            }) { Text("Volver a pendiente") }
                                        }

                                        "enviado" -> {
                                            OutlinedButton(onClick = {
                                                scope.launch { viewModel.cambiarEstadoPedido(pedido.pedido.id, "entregado") }
                                            }) { Text("Entregado") }

                                            OutlinedButton(onClick = {
                                                scope.launch { viewModel.cambiarEstadoPedido(pedido.pedido.id, "pendiente") }
                                            }) { Text("Volver a pendiente") }

                                            OutlinedButton(onClick = {
                                                scope.launch { viewModel.cambiarEstadoPedido(pedido.pedido.id, "no_entregado") }
                                            }) { Text("No entregado") }
                                        }

                                        "entregado" -> {
                                            Text("✅ Pedido entregado")
                                            pedidoDetalle.factura?.let { factura ->
                                                if (!factura.pdf_url.isNullOrEmpty()) {
                                                    OutlinedButton(onClick = {
                                                        val intent = android.content.Intent(
                                                            android.content.Intent.ACTION_VIEW,
                                                            android.net.Uri.parse(factura.pdf_url)
                                                        )
                                                        navController.context.startActivity(intent)
                                                    }) {
                                                        Text("Ver Factura PDF")
                                                    }
                                                } else {
                                                    Text("⚠️ Factura aún sin PDF")
                                                }
                                            } ?: Text("⚠️ Pedido sin factura asociada")
                                        }

                                        else -> Text("⚠️ Estado: ${pedido.pedido.estado}")
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}
