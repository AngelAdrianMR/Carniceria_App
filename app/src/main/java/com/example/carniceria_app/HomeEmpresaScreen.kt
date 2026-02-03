package com.example.carniceria_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.carniceria.shared.shared.models.utils.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeEmpresaScreen(
    navController: NavHostController,
    empresaId: Long,
    onLogout: () -> Unit
) {
    var productoSeleccionado by remember { mutableStateOf<ProductEmpresa?>(null) }
    val empresaViewModel: EmpresaViewModel = viewModel()
    val carritoViewModel: CarritoViewModel = viewModel()
    val productos by empresaViewModel.productos.collectAsState()
    val cargando by empresaViewModel.cargando.collectAsState()
    val error by empresaViewModel.error.collectAsState()
    val context = LocalContext.current

    var mostrarCantidadBottomSheet by remember { mutableStateOf(false) }
    var mostrarCarritoLateral by remember { mutableStateOf(false) }

    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }

    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Cargar datos
    LaunchedEffect(Unit) {
        empresaViewModel.cargarProductosEmpresa(empresaId)
        carritoViewModel.cargarCarritoLocal(context)
    }

    // Solo destacados
    val productosDestacados = productos.filter { it.destacado }

    // Categorías para filtrar
    val categorias = productosDestacados
        .mapNotNull { it.categoria_producto }
        .distinct()

    // Aplicar filtro
    val productosFiltrados = categoriaSeleccionada?.let { cat ->
        productosDestacados.filter { it.categoria_producto == cat }
    } ?: productosDestacados

    // ============================================================
    // 🟦 DRAWER LATERAL — FILTROS
    // ============================================================
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colors.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    Text(
                        "Filtros",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.primary
                    )

                    Divider()

                    Text(
                        "Categorías",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Puedes usar tu componente FiltroCategorias aquí
                    FiltroCategorias(categorias) { categoria ->
                        categoriaSeleccionada = categoria
                        scope.launch { drawerState.close() }
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            categoriaSeleccionada = null
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Limpiar filtros")
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                EmpresaHeader(
                    navController = navController,
                    titulo = "Inicio Empresa",
                    mostrarCarrito = true,
                    onAbrirCarrito = { mostrarCarritoLateral = true },
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

            when {
                cargando -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                error != null -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text(error ?: "Error desconocido") }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        state = rememberLazyListState(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {

                        item {
                            Text(
                                text = "Productos Destacados",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }

                        // 🔹 Botón de filtros (igual estilo que en las demás pantallas)
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssistChip(
                                    onClick = { scope.launch { drawerState.open() } },
                                    label = {
                                        Text("Filtros")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Abrir filtros"
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        labelColor = MaterialTheme.colorScheme.onPrimary,
                                        leadingIconContentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                        }

                        item {
                            GridProductosEmpresa(
                                productos = productosFiltrados,
                                onProductoClick = { prod ->
                                    navController.navigate("productoDetalle/${prod.id}?empresaId=$empresaId")
                                },
                                onAddClick = { prod ->
                                    productoSeleccionado = prod
                                    mostrarCantidadBottomSheet = true
                                }
                            )

                        }
                    }
                }
            }
        }
    }

    if (mostrarCantidadBottomSheet && productoSeleccionado != null) {
        ModalBottomSheet(
            onDismissRequest = {
                mostrarCantidadBottomSheet = false
                productoSeleccionado = null
            }
        ) {
            SeleccionCantidadBottomSheet(
                productoEmpresa = productoSeleccionado!!,
                onDismiss = {
                    mostrarCantidadBottomSheet = false
                    productoSeleccionado = null
                },
                onConfirmar = {
                    mostrarCantidadBottomSheet = false
                    productoSeleccionado = null
                }
            )
        }
    }

    if (mostrarCarritoLateral) {
        CarritoLateral(
            carrito = carritoViewModel.carrito,
            codigoPostalUsuario = null,     // ❗ Empresas NO tienen envío
            direccionUsuario = null,
            usuarioId = null,
            carritoViewModel = carritoViewModel,
            onCerrar = { mostrarCarritoLateral = false },
            onEliminarItem = { item ->
                carritoViewModel.eliminarProducto(item, context)
            },
            modifier = Modifier.zIndex(1f)
        )
    }
}


@Composable
fun GridProductosEmpresa(
    productos: List<ProductEmpresa>,
    onProductoClick: (ProductEmpresa) -> Unit,
    onAddClick: (ProductEmpresa) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        productos.chunked(2).forEach { fila ->

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                fila.forEach { producto ->

                    Card(
                        modifier = Modifier.weight(1f),
                        elevation = CardDefaults.cardElevation(4.dp),
                        onClick = { onProductoClick(producto) }, // 👉 Navega a detalle
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            // 📸 Imagen del producto
                            AsyncImage(
                                model = producto.imagen_producto,
                                contentDescription = producto.nombre_producto,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(Modifier.height(6.dp))

                            // 🥩 Nombre
                            Text(
                                text = producto.nombre_producto,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )

                            // 💰 Precio final empresa
                            Text(
                                text = "${"%.2f".format(producto.precio_final)} €",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(Modifier.height(10.dp))

                            // ➕ Botón para añadir al carrito
                            Button(
                                onClick = { onAddClick(producto) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Añadir")
                            }
                        }
                    }
                }

                // Para cuadrar fila cuando solo hay 1 elemento
                if (fila.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
