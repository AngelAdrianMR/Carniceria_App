package com.example.carniceria_app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilEmpresaScreen(
    navController: NavHostController,
    empresaId: Long,
    onLogout: () -> Unit
) {
    // ---------------------------------------------------
    // ESTADOS
    // ---------------------------------------------------
    var empresa by remember { mutableStateOf<Empresa?>(null) }
    var pedidos by remember { mutableStateOf<List<PedidoDetalle>>(emptyList()) }
    var cargandoEmpresa by remember { mutableStateOf(true) }
    var cargandoPedidos by remember { mutableStateOf(true) }
    var tabSeleccionada by remember { mutableStateOf(0) }

    val service = remember { SupabaseService(SupabaseProvider.client) }
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    // ---------------------------------------------------
    // CARGAR DATOS DE EMPRESA Y SUS PEDIDOS
    // ---------------------------------------------------
    LaunchedEffect(empresaId) {
        cargandoEmpresa = true
        cargandoPedidos = true

        try {
            empresa = service.obtenerEmpresaPorId(empresaId)
            cargandoEmpresa = false

            scope.launch {
                try {
                    //pedidos = service.obtenerPedidosEmpresa(empresaId)
                } finally {
                    cargandoPedidos = false
                }
            }
        } catch (e: Exception) {
            println("❌ Error cargando empresa: ${e.message}")
            cargandoEmpresa = false
            cargandoPedidos = false
        }
    }

    // ---------------------------------------------------
    // UI
    // ---------------------------------------------------
    Scaffold(
        topBar = {
            EmpresaHeader(
                navController = navController,
                titulo = "Inicio Empresa",
                mostrarCarrito = false,
                onAbrirCarrito = {},
                onLogout = onLogout,

                onNavigateHomeEmpresa = { navController.navigate("homeEmpresaScreen/$empresaId") },
                onNavigateEmpresaProductos = { navController.navigate("productosEmpresaScreen/$empresaId") },
                onNavigateEmpresaPedidos = { navController.navigate("pedidosYFacturas") },
                onNavigateEmpresaPerfil = { navController.navigate("perfilEmpresaScreen/$empresaId") },
                onNavigateEmpresaConfig = { navController.navigate("configEmpresaScreen/$empresaId") },
                onNavigateEmpresaSobreNosotros = { navController.navigate("sobreNosotrosEmpresaScreen/$empresaId") },
                onNavigationToFaqEmpresa = { navController.navigate("FaqEmpresaScreen/$empresaId")}

            )
        }
    ) { padding ->

        when {
            cargandoEmpresa -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            empresa == null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No se pudo cargar la información de la empresa.")
            }

            else -> {
                val emp = empresa!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {

                    // ======================================================
                    // 🏢 DATOS DE EMPRESA
                    // ======================================================

                    // ⭐ Nombre de empresa
                    CampoEmpresa("🏢 Empresa", emp.nombre_empresa)

                    CampoEmpresa("🧾 CIF/NIF", emp.nif_cif)
                    CampoEmpresa("📧 Email", emp.email)
                    CampoEmpresa("📞 Teléfono", emp.telefono)
                    CampoEmpresa("🏡 Dirección fiscal", emp.direccion_fiscal)
                    CampoEmpresa("🏦 IBAN", emp.iban)
                    CampoEmpresa("📅 Creada en", emp.creada_en ?: "-")
                    CampoEmpresa("✔️ Activa", if (emp.activa == true) "Sí" else "No")

                    Spacer(Modifier.height(16.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))

                    // ======================================================
                    // LISTA DE PEDIDOS
                    // ======================================================
                    Text(
                        "Historial de pedidos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))

                    if (cargandoPedidos) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // ▪ Tabs: entregados / rechazados
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

                    val pedidosFiltrados = when (tabSeleccionada) {
                        0 -> pedidos.filter { it.pedido.estado == "entregado" }
                        else -> pedidos.filter { it.pedido.estado == "rechazado" }
                    }

                    if (pedidosFiltrados.isEmpty() && !cargandoPedidos) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (tabSeleccionada == 0)
                                    "No hay pedidos entregados."
                                else "No hay pedidos rechazados."
                            )
                        }
                    } else {
                        LazyColumn {
                            items(pedidosFiltrados) { pedido ->
                                PedidoEmpresaCard(pedido)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CampoEmpresa(label: String, valor: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$label: ",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(valor ?: "-", style = MaterialTheme.typography.bodyMedium)
        }
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
fun PedidoEmpresaCard(pedido: PedidoDetalle) {
    val estadoColor = when (pedido.pedido.estado.lowercase()) {
        "pendiente" -> MaterialTheme.colorScheme.secondary
        "aceptado" -> MaterialTheme.colorScheme.primary
        "entregado" -> Color(0xFF5B8C41)
        "rechazado" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, estadoColor.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Pedido #${pedido.pedido.id}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))

            Surface(
                color = estadoColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    pedido.pedido.estado.replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = estadoColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            Text("Total: ${pedido.pedido.total} €")

            Spacer(Modifier.height(10.dp))
            Text("Productos:", fontWeight = FontWeight.Bold)

            pedido.lineas?.forEach { linea ->
                Text("• ${linea.producto?.nombre_producto ?: "Promo"} x${linea.cantidad}")
            }
        }
    }
}
