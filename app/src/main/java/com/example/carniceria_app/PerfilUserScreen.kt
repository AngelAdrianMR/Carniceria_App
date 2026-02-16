package com.example.carniceria_app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.PedidoDetalle
import com.carniceria.shared.shared.models.utils.PerfilConEmail
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import com.carniceria.shared.shared.models.utils.SupabaseService
import com.carniceria.shared.shared.models.utils.obtenerPerfilCompleto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUserScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    refreshTrigger: Boolean = false
) {
    // Estados principales
    var perfil by remember { mutableStateOf<PerfilConEmail?>(null) }
    var pedidos by remember { mutableStateOf<List<PedidoDetalle>>(emptyList()) }
    var cargandoPerfil by remember { mutableStateOf(true) }
    var cargandoPedidos by remember { mutableStateOf(true) }
    var tabSeleccionada by remember { mutableStateOf(0) } // 0 = Entregados, 1 = Rechazados

    val service = remember { SupabaseService(SupabaseProvider.client) }
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    // Cargar perfil + pedidos
    LaunchedEffect(Unit, refreshTrigger) {
        cargandoPerfil = true
        cargandoPedidos = true

        try {
            perfil = obtenerPerfilCompleto()
            cargandoPerfil = false

            val userId = perfil?.id_usuario
            if (userId != null) {
                scope.launch {
                    try {
                        pedidos = service.obtenerPedidosUsuarioDesdeVista(userId)
                        println("✅ Pedidos obtenidos: ${pedidos.size}")
                    } catch (e: Exception) {
                        println("❌ Error al cargar pedidos: ${e.message}")
                    } finally {
                        cargandoPedidos = false
                    }
                }
            } else {
                cargandoPedidos = false
            }
        } catch (e: Exception) {
            println("❌ Error cargando perfil: ${e.message}")
            cargandoPerfil = false
            cargandoPedidos = false
        }
    }

    // INTERFAZ PRINCIPAL
    Scaffold(
        topBar = {
            UserHeader(
                navController = navController,
                titulo = "Mi Perfil",
                onNavigateHome = { navController.navigate("homeUserScreen") },
                onNavigationToPerfil = { navController.navigate("perfilUser") },
                onNavigationToProductos = { navController.navigate("productosUser") },
                onNavigationToPedidos = { navController.navigate("pedidosYFacturas") },
                onNavigationToConfiguracion = { navController.navigate("configuracionScreen") },
                onNavigationToSobreNosotros = { navController.navigate("sobreNosotrosScreen") },
                onLogout = onLogout,
                mostrarCarrito = false,
                mostrarBotonEditar = true,
                onEditarPerfil = { navController.navigate("editarPerfilScreen") },
                onNavigationToFaq = { navController.navigate("faqScreen") }
            )
        }
    ) { padding ->

        when {
            cargandoPerfil -> {
                BoxFullScreenCentered(padding) { CircularProgressIndicator() }
            }

            perfil == null -> {
                BoxFullScreenCentered(padding) { Text("No se pudo cargar el perfil.") }
            }

            else -> {
                val pedidosFiltrados = when (tabSeleccionada) {
                    0 -> pedidos.filter { it.pedido.estado.trim().lowercase() == "entregado" }
                    else -> pedidos.filter { it.pedido.estado.trim().lowercase() == "rechazado" }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // 📌 Información de contacto
                    item {
                        SeccionCard(
                            titulo = "Información de contacto",
                            borderColor = colors.primary.copy(alpha = 0.35f)
                        ) {
                            FilaInfo(etiqueta = "Nombre:", valor = perfil!!.nombre_completo ?: "-")
                            Spacer(Modifier.height(8.dp))
                            FilaInfo(etiqueta = "Correo:", valor = perfil!!.email)
                            Spacer(Modifier.height(8.dp))
                            FilaInfo(etiqueta = "Teléfono:", valor = perfil!!.telefono ?: "-")
                        }
                    }

                    // 🏠 Direcciones de envío
                    item {
                        SeccionCard(
                            titulo = "Direcciones de envío",
                            borderColor = colors.primary.copy(alpha = 0.35f)
                        ) {
                            Text(
                                text = "Principal",
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Spacer(Modifier.height(6.dp))

                            val principalLinea = listOfNotNull(
                                perfil!!.calle?.takeIf { it.isNotBlank() },
                                perfil!!.piso?.takeIf { it.isNotBlank() },
                                perfil!!.localidad?.takeIf { it.isNotBlank() },
                                perfil!!.provincia?.takeIf { it.isNotBlank() },
                                perfil!!.pais?.takeIf { it.isNotBlank() }
                            ).joinToString(", ").ifEmpty { "-" }

                            Text(text = principalLinea, style = MaterialTheme.typography.bodyMedium)

                            Text(
                                text = "CP: ${perfil!!.codigoPostal ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface.copy(alpha = 0.75f)
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Otras direcciones",
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Spacer(Modifier.height(6.dp))

                            val extras = perfil!!.direccionesEnvio
                            if (extras.isEmpty()) {
                                Text(
                                    text = "No tienes direcciones adicionales.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurface.copy(alpha = 0.7f)
                                )
                            } else {
                                extras.forEachIndexed { idx, dir ->
                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Text(
                                            text = dir.alias?.takeIf { it.isNotBlank() } ?: "Dirección ${idx + 1}",
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.primary
                                        )

                                        Text(
                                            text = listOfNotNull(
                                                dir.calle,
                                                dir.piso?.takeIf { it.isNotBlank() },
                                                dir.localidad,
                                                dir.provincia,
                                                dir.pais
                                            ).joinToString(", "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.onSurface.copy(alpha = 0.8f)
                                        )

                                        Text(
                                            text = "CP: ${dir.codigoPostal}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                    if (idx != extras.lastIndex) Spacer(Modifier.height(6.dp))
                                }
                            }
                        }
                    }

                    item { Divider() }

                    // 📦 Historial de pedidos
                    item {
                        Text(
                            text = "Historial de pedidos:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (cargandoPedidos) {
                        item {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                            )
                        }
                    }

                    // 🧭 Pestañas
                    item {
                        TabRow(
                            selectedTabIndex = tabSeleccionada,
                            containerColor = colors.background,
                            contentColor = colors.onBackground,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    Modifier.tabIndicatorOffset(tabPositions[tabSeleccionada]),
                                    color = colors.primary
                                )
                            }
                        ) {
                            Tab(
                                selected = tabSeleccionada == 0,
                                onClick = { tabSeleccionada = 0 },
                                text = { Text("Entregados") }
                            )
                            Tab(
                                selected = tabSeleccionada == 1,
                                onClick = { tabSeleccionada = 1 },
                                text = { Text("Rechazados") }
                            )
                        }
                    }

                    // 🧾 Lista pedidos / vacío
                    if (pedidosFiltrados.isEmpty() && !cargandoPedidos) {
                        item {
                            Text(
                                text = if (tabSeleccionada == 0) "No tienes pedidos entregados."
                                else "No tienes pedidos rechazados.",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        items(pedidosFiltrados) { pedido ->
                            val estadoColor = when (pedido.pedido.estado.trim().lowercase()) {
                                "pendiente" -> MaterialTheme.colorScheme.secondary
                                "aceptado" -> MaterialTheme.colorScheme.primary
                                "entregado" -> Color(0xFF5B8C41)
                                "rechazado" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(2.dp, estadoColor.copy(alpha = 0.3f)),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {

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
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))
                                    Divider()
                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = "Total: ${"%.2f".format(pedido.pedido.total ?: 0.0)} €",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )

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
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(Modifier.height(10.dp))
                                    Text("Productos:", fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))

                                    pedido.lineas?.forEach { linea ->
                                        when {
                                            linea.producto != null -> {
                                                Text(
                                                    text = "• ${linea.producto!!.nombre_producto} x${linea.cantidad} - ${
                                                        "%.2f".format(linea.precio_unitario)
                                                    }€",
                                                    style = MaterialTheme.typography.bodySmall
                                                )

                                                linea.mensaje?.let { mensaje ->
                                                    if (mensaje.isNotBlank()) {
                                                        Text(
                                                            text = "   Nota: $mensaje",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                        )
                                                    }
                                                }
                                            }

                                            linea.promocion != null -> {
                                                val promo = linea.promocion!!.promocion
                                                val productosPromo = linea.promocion!!.productos

                                                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                                    Text(
                                                        text = "• Promoción: ${promo.nombre_promocion} x${linea.cantidad} - ${
                                                            "%.2f".format(linea.precio_unitario)
                                                        }€",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )

                                                    productosPromo.forEach { prod ->
                                                        Text(
                                                            text = "     ↳ ${prod.nombre_producto}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                        )
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
            }
        }
    }
}

@Composable
private fun BoxFullScreenCentered(
    padding: PaddingValues,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun SeccionCard(
    titulo: String,
    borderColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun FilaInfo(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface
        )
    }
}
