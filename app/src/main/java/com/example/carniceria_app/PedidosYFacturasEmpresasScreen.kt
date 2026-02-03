package com.example.carniceria_app

import SelectorFecha
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ===================================================
// 🧾 PANTALLA — PEDIDOS Y FACTURAS DEL USUARIO
// ===================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosYFacturasEmpresaScreen(
    navController: NavHostController,
    usuarioId: String,
    onLogout: () -> Unit,
    empresaId: Long
) {
    // ---------------------------------------------------
    // 🔹 Estados principales
    // ---------------------------------------------------
    var pedidos by rememberSaveable { mutableStateOf<List<PedidoDetalle>>(emptyList()) }
    var facturas by rememberSaveable { mutableStateOf<List<Factura>>(emptyList()) }
    var perfilUsuario by remember { mutableStateOf<PerfilUsuario?>(null) }
    var cargandoPedidos by remember { mutableStateOf(true) }
    var cargandoFacturas by remember { mutableStateOf(true) }
    var mostrarCarritoLateral by remember { mutableStateOf(false) }
    var tabSeleccionada by remember { mutableStateOf(0) } // 0 = Pedidos, 1 = Facturas

    val carritoViewModel: CarritoViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val service = remember { SupabaseService(SupabaseProvider.client) }
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    // ---------------------------------------------------
    // ⚙️ Cargar datos de manera paralela
    // ---------------------------------------------------
    LaunchedEffect(usuarioId) {
        scope.launch {
            try {
                val pedidosDeferred = async { service.obtenerPedidosUsuarioDesdeVista(usuarioId) }
                val facturasDeferred = async { service.obtenerFacturasUsuario(usuarioId) }
                val perfilDeferred = async { obtenerPerfilUsuarioActual() }
                val carritoDeferred = async { carritoViewModel.cargarCarritoLocal(context) }

                // Esperar cada uno por separado
                scope.launch {
                    pedidos = pedidosDeferred.await()
                    cargandoPedidos = false
                }
                scope.launch {
                    facturas = facturasDeferred.await()
                    cargandoFacturas = false
                }
                perfilUsuario = perfilDeferred.await()
                carritoDeferred.await()
            } catch (e: Exception) {
                println("❌ Error cargando datos: ${e.message}")
            }
        }
    }

    // ===================================================
    // 🧱 INTERFAZ PRINCIPAL
    // ===================================================
    Scaffold(
        topBar = {
            EmpresaHeader(
                navController = navController,
                titulo = "Pedidos Empresa",
                mostrarCarrito = true,
                onAbrirCarrito = {mostrarCarritoLateral = true},
                onLogout = onLogout,

                onNavigateHomeEmpresa = { navController.navigate("homeEmpresaScreen/$empresaId") },

                onNavigateEmpresaProductos = { navController.navigate("productosEmpresaScreen/$empresaId") },

                onNavigateEmpresaPedidos = {
                    navController.navigate("pedidosYFacturasEmpresas/$empresaId")
                },

                onNavigateEmpresaPerfil = { navController.navigate("perfilEmpresaScreen/$empresaId") },

                onNavigateEmpresaConfig = { navController.navigate("configEmpresaScreen/$empresaId") },

                onNavigateEmpresaSobreNosotros = { navController.navigate("sobreNosotrosEmpresaScreen/$empresaId") },
                onNavigationToFaqEmpresa = { navController.navigate("FaqEmpresaScreen/$empresaId")}

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
                        color = colors.primary
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

            // ---------------------------------------------------
            // 🧩 Contenido por pestaña
            // ---------------------------------------------------
            when (tabSeleccionada) {

                // ===================================================
                // 🧾 TAB — PEDIDOS
                // ===================================================
                0 -> {
                    if (cargandoPedidos) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        PedidosTab(pedidos)
                    }
                }

                // ===================================================
                // 💸 TAB — FACTURAS
                // ===================================================
                1 -> {
                    if (cargandoFacturas) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        FacturasTab(navController, facturas, pedidos)
                    }
                }
            }
        }
    }

    // 🧺 Carrito lateral
    if (mostrarCarritoLateral && perfilUsuario != null) {
        CarritoLateral(
            carrito = carritoViewModel.carrito,
            direccionUsuario = perfilUsuario!!.direccionCompleta,
            usuarioId = perfilUsuario!!.id,
            carritoViewModel = carritoViewModel,
            codigoPostalUsuario = perfilUsuario!!.codigo_postal,
            onCerrar = { mostrarCarritoLateral = false },
            onEliminarItem = { item ->
                item.producto?.id?.let { carritoViewModel.eliminarProducto(item, context) }
            },
            modifier = Modifier.zIndex(3f)
        )
    }
}

// ===================================================
// 🧩 SUBCOMPONENTE — TAB PEDIDOS (misma estética)
// ===================================================
@Composable
private fun PedidosTab(pedidos: List<PedidoDetalle>) {
    val estadosPosibles = listOf("pendiente", "aceptado", "enviado")
    var estadoSeleccionado by remember { mutableStateOf<String?>(null) }

    val pedidosVisibles = remember(pedidos, estadoSeleccionado) {
        pedidos.filter { p ->
            val estado = p.pedido.estado.lowercase()
            estadosPosibles.contains(estado)
        }.filter { p ->
            estadoSeleccionado?.let {
                p.pedido.estado.equals(it, ignoreCase = true)
            } ?: true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Filtrar por estado:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val colores = mapOf(
                "pendiente" to Color(0xFFE3C77D),
                "aceptado" to Color(0xFF4CAF50),
                "enviado" to Color(0xFF2196F3)
            )

            estadosPosibles.forEach { estado ->
                val isSelected = estadoSeleccionado == estado
                val color = colores[estado] ?: MaterialTheme.colorScheme.primary

                OutlinedButton(
                    onClick = {
                        estadoSeleccionado = if (estadoSeleccionado == estado) null else estado
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
                        contentColor = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, color.copy(alpha = if (isSelected) 1f else 0.3f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = estado.replaceFirstChar { it.uppercase() },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Divider()
        Spacer(Modifier.height(10.dp))

        if (pedidosVisibles.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes pedidos en los estados seleccionados.")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(pedidosVisibles) { pedido ->
                    PedidoCard(pedido)
                }
            }
        }
    }
}

// ===================================================
// 🧩 SUBCOMPONENTE — TARJETA DE PEDIDO (idéntico estilo)
// ===================================================
@Composable
private fun PedidoCard(pedido: PedidoDetalle) {
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
            Text("🧺 Productos:", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))

            pedido.lineas?.forEach { linea ->
                when {
                    linea.producto != null -> {
                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                text = "• ${linea.producto!!.nombre_producto} x${linea.cantidad} - ${
                                    "%.2f".format(linea.precio_unitario)
                                }€",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!linea.mensaje.isNullOrBlank()) {
                                Text(
                                    text = "     📝 ${linea.mensaje}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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

// ===================================================
// 🧩 SUBCOMPONENTE — TAB Y TARJETA FACTURAS
// ===================================================
@Composable
private fun FacturasTab(
    navController: NavHostController,
    facturas: List<Factura>,
    pedidos: List<PedidoDetalle>
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val hoy = remember { Calendar.getInstance().time }
    val hace30dias = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.time }
    var fechaInicio by remember { mutableStateOf(hace30dias) }
    var fechaFin by remember { mutableStateOf(hoy) }

    val facturasFiltradas by remember {
        derivedStateOf {
            facturas.filter { f ->
                try {
                    val fechaFactura = sdf.parse(f.fecha)
                    fechaFactura != null && !fechaFactura.before(fechaInicio) && !fechaFactura.after(fechaFin)
                } catch (e: Exception) {
                    false
                }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(8.dp)) {
        // Fecha y filtros
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("📅 Mostrando facturas:", fontWeight = FontWeight.Bold)
                Text("${sdf.format(fechaInicio)} → ${sdf.format(fechaFin)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }

            Column(horizontalAlignment = Alignment.End) {
                SelectorFecha("Desde", fechaInicio) { nuevaFecha ->
                    fechaInicio = nuevaFecha
                    if (fechaInicio.after(fechaFin)) fechaFin = nuevaFecha
                }
                SelectorFecha("Hasta", fechaFin) { nuevaFecha ->
                    fechaFin = nuevaFecha
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        if (facturasFiltradas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay facturas en el rango seleccionado.")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(facturasFiltradas) { factura ->
                    FacturaCard(navController, factura, pedidos)
                }
            }
        }
    }
}

@Composable
private fun FacturaCard(
    navController: NavHostController,
    factura: Factura,
    pedidos: List<PedidoDetalle>
) {
    val pedidoRelacionado = pedidos.find { it.pedido.id == factura.id_pedido }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // ---------------- CABECERA ----------------
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Factura #${factura.id}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    factura.fecha ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(6.dp))
            Divider()
            Spacer(Modifier.height(6.dp))

            Text("Estado: ${factura.estado}")
            Text(
                "Total: ${"%.2f".format(factura.total ?: 0.0)} €",
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))
            if (!factura.pdf_url.isNullOrEmpty()) {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(factura.pdf_url))
                    navController.context.startActivity(intent)
                }) { Text("📄 Ver PDF") }
            } else {
                Text("⚠️ Factura aún sin PDF", color = MaterialTheme.colorScheme.error)
            }

            // ---------------- PEDIDO ASOCIADO ----------------
            pedidoRelacionado?.let { pedido ->
                Spacer(Modifier.height(12.dp))
                Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                Spacer(Modifier.height(8.dp))
                Text("🧾 Pedido asociado", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("ID: #${pedido.pedido.id}")
                Text("Estado: ${pedido.pedido.estado}")
                Text("Total pedido: ${"%.2f".format(pedido.pedido.total ?: 0.0)} €")

                // 🧺 Productos y promociones del pedido
                Spacer(Modifier.height(10.dp))
                Text("Productos y promociones:", fontWeight = FontWeight.Bold)
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
