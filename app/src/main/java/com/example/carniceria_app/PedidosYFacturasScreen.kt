package com.example.carniceria_app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.*
import kotlinx.coroutines.launch

// ===================================================
// 🧾 PANTALLA — PEDIDOS Y FACTURAS DEL USUARIO
// ===================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosYFacturasScreen(
    navController: NavHostController,
    usuarioId: String,
    onLogout: () -> Unit
) {
    // ---------------------------------------------------
    // 🔹 Estados principales
    // ---------------------------------------------------
    var pedidos by remember { mutableStateOf<List<PedidoDetalle>>(emptyList()) }
    var facturas by remember { mutableStateOf<List<Factura>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var tabSeleccionada by remember { mutableStateOf(0) } // 0 = Pedidos, 1 = Facturas
    var mostrarCarritoLateral by remember { mutableStateOf(true) }
    var perfilUsuario by remember { mutableStateOf<PerfilUsuario?>(null) }

    val carritoViewModel: CarritoViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val service = remember { SupabaseService(SupabaseProvider.client) }
    val colors = MaterialTheme.colorScheme

    val context = LocalContext.current

    // ---------------------------------------------------
    // ⚙️ Cargar datos del usuario
    // ---------------------------------------------------
    LaunchedEffect(usuarioId) {
        scope.launch {
            try {
                pedidos = service.obtenerPedidosUsuario(usuarioId)
                facturas = service.obtenerFacturasUsuario(usuarioId)
                carritoViewModel.cargarCarritoLocal(context)
                perfilUsuario = obtenerPerfilUsuarioActual()
            } catch (e: Exception) {
                println("❌ Error cargando datos: ${e.message}")
            } finally {
                cargando = false
            }
        }
    }

    // ===================================================
    // 🧱 INTERFAZ PRINCIPAL
    // ===================================================
    Scaffold(
        topBar = {
            UserHeader(
                navController = navController,
                titulo = "Pedidos y facturas.",
                onNavigateHome = { navController.navigate("homeUserScreen") },
                onNavigationToPerfil = { navController.navigate("perfilUser") },
                onNavigationToProductos = { navController.navigate("productosUser") },
                onNavigationToPedidos = { navController.navigate("pedidosYFacturas") },
                onNavigationToConfiguracion = { navController.navigate("configuracionScreen") },
                onNavigationToSobreNosotros = { navController.navigate("sobreNosotrosScreen") },
                onLogout = onLogout,
                mostrarCarrito = true,
                onAbrirCarrito = { mostrarCarritoLateral = true },
                mostrarBotonEditar = false,
                onEditarPerfil = {
                    navController.navigate("editarPerfilScreen")
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ---------------------------------------------------
            // 🧭 Pestañas (Pedidos / Facturas)
            // ---------------------------------------------------
            TabRow(
                selectedTabIndex = tabSeleccionada,
                containerColor = colors.background,
                contentColor = colors.onBackground,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[tabSeleccionada]),
                        color = colors.primary // línea activa (se adapta al tema)
                    )
                }
            ) {
                Tab(
                    selected = tabSeleccionada == 0,
                    onClick = { tabSeleccionada = 0 },
                    text = {
                        Text(
                            "Pedidos",
                            color = if (tabSeleccionada == 0)
                                colors.primary
                            else
                                colors.onBackground.copy(alpha = 0.7f)
                        )
                    }
                )
                Tab(
                    selected = tabSeleccionada == 1,
                    onClick = { tabSeleccionada = 1 },
                    text = {
                        Text(
                            "Facturas",
                            color = if (tabSeleccionada == 1)
                                colors.primary
                            else
                                colors.onBackground.copy(alpha = 0.7f)
                        )
                    }
                )
            }


            when {
                // ---------------------------------------------------
                // ⏳ Cargando datos
                // ---------------------------------------------------
                cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // ---------------------------------------------------
                // 🧾 TAB — PEDIDOS
                // ---------------------------------------------------
                tabSeleccionada == 0 -> {
                    if (pedidos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tienes pedidos activos.")
                        }
                    } else {
                        val pedidosVisibles = pedidos.filter {
                            it.pedido.estado != "entregado" && it.pedido.estado != "rechazado"
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(pedidosVisibles) { pedido ->
                                Card(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            "Pedido #${pedido.pedido.id}",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text("Estado: ${pedido.pedido.estado}")
                                        Text(
                                            "Total original: ${"%.2f".format(pedido.pedido.total ?: 0.0)} €"
                                        )

                                        if (!pedido.pedido.codigo_descuento_aplicado.isNullOrEmpty()) {
                                            Text(
                                                "Código aplicado: ${pedido.pedido.codigo_descuento_aplicado}"
                                            )
                                        }

                                        if ((pedido.pedido.descuento_aplicado ?: 0.0) > 0.0) {
                                            Text(
                                                "Descuento: -${"%.2f".format(pedido.pedido.descuento_aplicado!!)} €",
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        if ((pedido.pedido.total_con_descuento
                                                ?: pedido.pedido.total) != pedido.pedido.total
                                        ) {
                                            Text(
                                                "Total con descuento: ${
                                                    "%.2f".format(
                                                        pedido.pedido.total_con_descuento!!
                                                    )
                                                } €",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                        }

                                        Spacer(Modifier.height(6.dp))

                                        pedido.lineas?.forEach { linea ->
                                            when {
                                                linea.producto != null -> {
                                                    Text(
                                                        "- ${linea.producto!!.nombre_producto} x${linea.cantidad} - ${linea.precio_unitario}€"
                                                    )

                                                    linea.mensaje?.let { mensaje ->
                                                        if (mensaje.isNotBlank()) {
                                                            Text(
                                                                "Nota: $mensaje",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme
                                                                    .onSurface.copy(alpha = 0.7f)
                                                            )
                                                        }
                                                    }
                                                }

                                                linea.promocion != null -> {
                                                    Text(
                                                        "Promoción: ${
                                                            linea.promocion!!.promocion.nombre_promocion
                                                        } x${linea.cantidad} - ${linea.precio_unitario}€"
                                                    )
                                                    linea.promocion!!.productos.forEach { prodPromo ->
                                                        Text(
                                                            " - ${prodPromo.nombre_producto}",
                                                            style = MaterialTheme.typography.bodySmall
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

                // ---------------------------------------------------
                // 💸 TAB — FACTURAS
                // ---------------------------------------------------
                tabSeleccionada == 1 -> {
                    if (facturas.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tienes facturas todavía.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            items(facturas) { factura ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            "Factura #${factura.id}",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text("Fecha: ${factura.fecha}")
                                        Text("Estado: ${factura.estado}")
                                        Text("Total: ${factura.total} €")

                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (!factura.pdf_url.isNullOrEmpty()) {
                                            TextButton(
                                                onClick = {
                                                    val intent = Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse(factura.pdf_url)
                                                    )
                                                    navController.context.startActivity(intent)
                                                }
                                            ) {
                                                Text("📄 Ver PDF")
                                            }
                                        } else {
                                            Text(
                                                "⚠️ Factura aún sin PDF",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
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

    // 🧺 Carrito lateral (igual que admin)
    if (mostrarCarritoLateral) {
        perfilUsuario?.let { perfil ->
            CarritoLateral(
                carrito = carritoViewModel.carrito,
                direccionUsuario = perfil.direccionCompleta,
                usuarioId = perfil.id,
                carritoViewModel = carritoViewModel,
                codigoPostalUsuario = perfil.codigo_postal,
                onCerrar = { mostrarCarritoLateral = false },
                onEliminarItem = { item ->
                    item.producto?.id?.let {
                        carritoViewModel.eliminarProducto(item, context)
                    }
                },
                modifier = Modifier.zIndex(3f)
            )
        }
    }
}
