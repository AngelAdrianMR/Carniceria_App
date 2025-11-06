package com.example.carniceria_app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.PedidoDetalle
import com.carniceria.shared.shared.models.utils.PerfilConEmail
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import com.carniceria.shared.shared.models.utils.SupabaseService
import com.carniceria.shared.shared.models.utils.obtenerPerfilCompleto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUserScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    refreshTrigger: Boolean = false
) {
    // ---------------------------------------------------
    // 🔹 Estados principales
    // ---------------------------------------------------
    var perfil by remember { mutableStateOf<PerfilConEmail?>(null) }
    var pedidos by remember { mutableStateOf<List<PedidoDetalle>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var tabSeleccionada by remember { mutableStateOf(0) } // 0 = Entregados, 1 = Rechazados

    val service = remember { SupabaseService(SupabaseProvider.client) }
    val colors = MaterialTheme.colorScheme

    // ---------------------------------------------------
    // ⚙️ Cargar perfil + pedidos
    // ---------------------------------------------------
    LaunchedEffect(Unit, refreshTrigger) {
        try {
            perfil = obtenerPerfilCompleto()

            val idAuth = perfil?.id_usuario
            println("🧩 ID AUTH para pedidos: $idAuth")

            if (!idAuth.isNullOrBlank()) {
                pedidos = service.obtenerPedidosUsuario(idAuth)
                println("✅ Pedidos obtenidos: ${pedidos.size}")
            } else {
                println("⚠️ El perfil no tiene id_usuario válido, no se cargan pedidos.")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            println("❌ Error cargando datos: ${e}")
        } finally {
            cargando = false
        }
    }

    // ===================================================
    // 🧱 INTERFAZ PRINCIPAL
    // ===================================================
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
                onEditarPerfil = { navController.navigate("editarPerfilScreen") }
            )
        }
    ) { padding ->

        when {
            cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            perfil == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se pudo cargar el perfil.")
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {

                    // ---------------------------------------------------
                    // 👤 Información del perfil
                    // ---------------------------------------------------
                    Text(
                        text = "👤 ${perfil!!.nombre_completo ?: "Usuario"}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("📧 Correo: ${perfil!!.email ?: "-"}")
                    Text("📞 Teléfono: ${perfil!!.telefono ?: "-"}")
                    Text(
                        "🏡 Dirección: ${listOfNotNull( perfil!!.calle, perfil!!.piso, perfil!!.localidad, perfil!!.provincia, perfil!!.pais).joinToString(", ").ifEmpty { "-" } }"
                )
                Text("🏙️ Código postal: ${perfil!!.codigoPostal ?: "-"}")

                Spacer(Modifier.height(16.dp))
                Divider()

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Historial de pedidos:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(8.dp))

                // ---------------------------------------------------
                // 🧭 Pestañas (Entregados / Rechazados)
                // ---------------------------------------------------
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
                        text = {
                            Text(
                                "Entregados",
                                color = if (tabSeleccionada == 0)
                                    colors.primary else colors.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    )
                    Tab(
                        selected = tabSeleccionada == 1,
                        onClick = { tabSeleccionada = 1 },
                        text = {
                            Text(
                                "Rechazados",
                                color = if (tabSeleccionada == 1)
                                    colors.primary else colors.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    )
                }

                // ---------------------------------------------------
                // 📦 Contenido de pestañas
                // ---------------------------------------------------
                val pedidosFiltrados = when (tabSeleccionada) {
                    0 -> pedidos.filter { it.pedido.estado == "entregado" }
                    else -> pedidos.filter { it.pedido.estado == "rechazado" }
                }

                if (pedidosFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (tabSeleccionada == 0)
                                "No tienes pedidos entregados."
                            else
                                "No tienes pedidos rechazados."
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(pedidosFiltrados) { pedido ->
                            Card(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        "Pedido #${pedido.pedido.id}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text("Estado: ${pedido.pedido.estado}")
                                    Text("Total: ${"%.2f".format(pedido.pedido.total ?: 0.0)} €")

                                    pedido.lineas?.forEach { linea ->
                                        when {
                                            linea.producto != null -> {
                                                Text(
                                                    "- ${linea.producto!!.nombre_producto} x${linea.cantidad} - ${linea.precio_unitario}€"
                                                )
                                            }

                                            linea.promocion != null -> {
                                                Text(
                                                    "Promoción: ${linea.promocion!!.promocion.nombre_promocion}"
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
