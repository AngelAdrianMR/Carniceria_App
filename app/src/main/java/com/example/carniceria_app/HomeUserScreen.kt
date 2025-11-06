package com.example.carniceria_app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUserScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val carritoViewModel: CarritoViewModel = viewModel()

    var mostrarCarritoLateral by remember { mutableStateOf(false) }
    var promociones by remember { mutableStateOf<List<PromocionConProductos>>(emptyList()) }
    var productos by remember { mutableStateOf<List<Product>>(emptyList()) }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var productoSeleccionado by remember { mutableStateOf<Product?>(null) }
    var mostrarCantidadBottomSheet by remember { mutableStateOf(false) }
    var perfilUsuario by remember { mutableStateOf<PerfilUsuario?>(null) }

    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        try {
            promociones = obtenerPromociones()
            productos = obtenerProductos()
            carritoViewModel.cargarCarritoLocal(context)
            perfilUsuario = obtenerPerfilUsuarioActual()
        } catch (e: Exception) {
            Log.e("HomeUserScreen", "Error al obtener datos", e)
        }
    }

    val categorias = productos.map { it.categoria_producto }.distinct()
    val productosDestacados = productos.filter { it.destacado == true }
    val productosFiltrados = categoriaSeleccionada?.let { categoria ->
        productosDestacados.filter { it.categoria_producto == categoria }
    } ?: productosDestacados

    // 🔹 Estructura principal con Scaffold (como en Admin)
    Scaffold(
        topBar = {
            UserHeader(
                navController = navController,
                titulo = "Inicio",
                onLogout = onLogout,
                mostrarCarrito = true,
                onAbrirCarrito = { mostrarCarritoLateral = true },
                mostrarBotonEditar = false,
                onEditarPerfil = { navController.navigate("editarPerfilScreen") },
                onNavigateHome = { navController.navigate("homeUserScreen") },
                onNavigationToPerfil = { navController.navigate("perfilUser") },
                onNavigationToProductos = { navController.navigate("productosUser") },
                onNavigationToPedidos = { navController.navigate("pedidosYFacturas") },
                onNavigationToConfiguracion = { navController.navigate("configuracionScreen") },
                onNavigationToSobreNosotros = { navController.navigate("sobreNosotrosScreen") }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(paddingValues),
            state = listState,
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Promociones destacadas",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            item {
                if (promociones.isNotEmpty()) {
                    SliderPromociones(
                        promociones = promociones,
                        onAddClick = {
                            productoSeleccionado = it
                            mostrarCantidadBottomSheet = true
                        },
                        onAddPromocion = { promoConProductos ->
                            carritoViewModel.agregarPromocionAlCarrito(promoConProductos, context)
                        },
                        onPromoClick = { promo ->
                            promo.id?.let { id ->
                                navController.navigate("promocionDetalle/$id")
                            }
                        }
                    )
                } else {
                    Text("Cargando promociones...", modifier = Modifier.padding(16.dp))
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Nuestros Productos Destacados",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(6.dp))
                FiltroCategorias(categorias) { categoriaSeleccionada = it }
                Spacer(Modifier.height(8.dp))
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp)
                ) {
                    GridProductos(
                        productos = productosFiltrados,
                        onAddClick = {
                            productoSeleccionado = it
                            mostrarCantidadBottomSheet = true
                        },
                        onProductoClick = { producto ->
                            producto.id?.let { id ->
                                navController.navigate("productoDetalle/$id")
                            }
                        }
                    )
                }
            }

        }
    }

    // 🧩 BottomSheet cantidad
    if (mostrarCantidadBottomSheet && productoSeleccionado != null) {
        ModalBottomSheet(
            onDismissRequest = {
                mostrarCantidadBottomSheet = false
                productoSeleccionado = null
            }
        ) {
            SeleccionCantidadBottomSheet(
                producto = productoSeleccionado!!,
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
