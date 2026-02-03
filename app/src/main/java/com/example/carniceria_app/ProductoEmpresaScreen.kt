package com.example.carniceria_app

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.ProductEmpresa
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.carniceria.shared.shared.models.utils.*
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosEmpresaScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    empresaId: Long
) {
    val empresaViewModel: EmpresaViewModel = viewModel()
    val carritoViewModel: CarritoViewModel = viewModel()
    val context = LocalContext.current

    // Estados
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var productoSeleccionado by remember { mutableStateOf<ProductEmpresa?>(null) }
    var mostrarCantidadBottomSheet by remember { mutableStateOf(false) }
    var mostrarCarritoLateral by remember { mutableStateOf(false) }

    var textoBusqueda by remember { mutableStateOf("") }

    val productos by empresaViewModel.productos.collectAsState()
    val cargando by empresaViewModel.cargando.collectAsState()
    val error by empresaViewModel.error.collectAsState()

    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // 🔹 Cargar productos de empresa
    LaunchedEffect(Unit) {
        empresaViewModel.cargarProductosEmpresa(empresaId)
        carritoViewModel.cargarCarritoLocal(context)
    }

    // 🔹 Categorías
    val categorias = productos.mapNotNull { it.categoria_producto }.distinct()

    // 🔹 Filtrado general
    val productosFiltrados = productos.filter { prod ->
        val coincideCategoria = categoriaSeleccionada == null || prod.categoria_producto == categoriaSeleccionada
        val coincideBusqueda =
            textoBusqueda.isBlank() ||
                    prod.nombre_producto.contains(textoBusqueda, ignoreCase = true) ||
                    (prod.descripcion_producto?.contains(textoBusqueda, ignoreCase = true) == true)

        coincideCategoria && coincideBusqueda
    }

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
                        text = "Filtros",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.primary
                    )

                    Divider()

                    Text(
                        text = "Categorías",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Usamos tu componente para listar categorías
                    FiltroCategorias(categorias) { categoria ->
                        categoriaSeleccionada = categoria
                        scope.launch { drawerState.close() }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
        //  UI PRINCIPAL
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
        ) {

            EmpresaHeader(
                navController = navController,
                titulo = "Productos Empresa",
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

            Spacer(modifier = Modifier.height(8.dp))

            // 🔍 BARRA DE BÚSQUEDA
            TextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                placeholder = { Text("Buscar productos...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceVariant,
                    unfocusedContainerColor = colors.surfaceVariant,
                    disabledContainerColor = colors.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = colors.primary
                ),
                shape = RoundedCornerShape(50)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 BOTÓN FILTROS (igual estilo que en las otras pantallas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
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

            // 🔹 GRID DE PRODUCTOS
            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                GridProductosEmpresa(
                    productos = productosFiltrados,
                    onProductoClick = { prod ->
                        navController.navigate("productoDetalle/${prod.id}")
                    },
                    onAddClick = { prod ->
                        productoSeleccionado = prod
                        mostrarCantidadBottomSheet = true
                    }
                )
            }
        }
    }

    // ============================================================
    // 🧩 BOTTOM SHEET CANTIDAD
    // ============================================================
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

    // ============================================================
    // 🛒 CARRITO LATERAL EMPRESA
    // ============================================================
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
