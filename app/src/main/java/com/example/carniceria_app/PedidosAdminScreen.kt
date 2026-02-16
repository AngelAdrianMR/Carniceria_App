package com.example.carniceria_app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
            UpBarAdmin(
                navController = navController,
                titulo = "Gestión de Pedidos",
                onLogout = onLogout
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // CARGA
            if (pedidos.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            // FILTRAR: NO mostrar entregados / rechazados
            val pedidosVisibles = pedidos.filter {
                it.pedido.estado !in listOf("entregado", "rechazado")
            }

            LazyColumn(Modifier.fillMaxSize()) {
                items(pedidosVisibles) { pedidoDetalle ->
                    PedidoCardAdmin(
                        pedido = pedidoDetalle,
                        estaCargando = cargandoPedidos.contains(pedidoDetalle.pedido.id),
                        onCambiarEstado = { nuevoEstado ->
                            scope.launch {
                                viewModel.cambiarEstadoPedido(
                                    idPedido = pedidoDetalle.pedido.id,
                                    nuevoEstado = nuevoEstado
                                )
                            }
                        },
                        onVerFacturaPDF = { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            navController.context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PedidoCardAdmin(
    pedido: PedidoDetalle,
    estaCargando: Boolean,
    onCambiarEstado: (String) -> Unit,
    onVerFacturaPDF: (String) -> Unit
) {
    val estado = pedido.pedido.estado.lowercase()
    val estadoColor = when (estado) {
        "pendiente" -> Color(0xFFFFC107)
        "aceptado" -> Color(0xFF4CAF50)
        "enviado" -> Color(0xFF2196F3)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, estadoColor.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            // CABECERA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pedido #${pedido.pedido.id}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = estadoColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = pedido.pedido.estado.replaceFirstChar { it.uppercase() },
                        color = estadoColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            // DATOS PRINCIPALES
            Text(
                "Total: ${"%.2f".format(pedido.pedido.total ?: 0.0)} €",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            if (!pedido.pedido.codigo_descuento_aplicado.isNullOrEmpty())
                Text("Código aplicado: ${pedido.pedido.codigo_descuento_aplicado}")

            if ((pedido.pedido.descuento_aplicado ?: 0.0) > 0.0)
                Text(
                    "Descuento: -${"%.2f".format(pedido.pedido.descuento_aplicado!!)} €",
                    color = MaterialTheme.colorScheme.primary
                )

            if ((pedido.pedido.total_con_descuento ?: pedido.pedido.total) != pedido.pedido.total)
                Text(
                    "Total con descuento: ${"%.2f".format(pedido.pedido.total_con_descuento!!)} €",
                    fontWeight = FontWeight.Bold
                )

            // INFO DE ENVÍO Y USUARIO
            pedido.usuario?.let { u ->
                Spacer(Modifier.height(8.dp))
                Text("Tipo entrega: ${pedido.pedido.tipo_entrega}")
                Text(
                    text = "Nombre: ${u.nombre_completo ?: "Sin nombre"}",
                )
                Text("Rol: ${u.rol}")
                Text("Contacto: ${u.telefono}")
                Text("Dirección: ${u.direccionCompleta}")
            }

            Spacer(Modifier.height(12.dp))

            // PRODUCTOS
            Text("🧺 Productos:", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))

            pedido.lineas?.forEach { linea ->
                when {
                    linea.producto != null -> {
                        Text("• ${linea.producto!!.nombre_producto} x${linea.cantidad} - ${linea.precio_unitario}€")
                        if (!linea.mensaje.isNullOrBlank()) {
                            Text("     📝 ${linea.mensaje}")
                        }
                    }

                    linea.promocion != null -> {
                        Text(
                            "• Promoción: ${linea.promocion!!.promocion.nombre_promocion}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        linea.promocion!!.productos.forEach { p ->
                            Text("     ↳ ${p.nombre_producto}")
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ACCIONES ADMIN
            if (estaCargando) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    when (estado) {
                        "pendiente" -> {
                            AdminButton("Aceptar") { onCambiarEstado("aceptado") }
                            AdminButton("Rechazar") { onCambiarEstado("rechazado") }
                        }

                        "aceptado" -> {
                            AdminButton("Marcar enviado") { onCambiarEstado("enviado") }
                            AdminButton("Volver a pendiente") { onCambiarEstado("pendiente") }
                        }

                        "enviado" -> {
                            AdminButton("Entregado") { onCambiarEstado("entregado") }
                            AdminButton("No entregado") { onCambiarEstado("no_entregado") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminButton(texto: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(texto)
    }
}
