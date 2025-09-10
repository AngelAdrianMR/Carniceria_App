package com.example.carniceria_app

import androidx.compose.ui.text.style.TextAlign
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUserScreen(navController: NavHostController, onLogout: () -> Unit) {
    val carritoViewModel: CarritoViewModel = viewModel()

    var mostrarCarritoLateral by remember { mutableStateOf(false) }

    var promociones by remember { mutableStateOf<List<PromocionConProductos>>(emptyList()) }
    var productos by remember { mutableStateOf<List<Product>>(emptyList()) }

    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var productoSeleccionado by remember { mutableStateOf<Product?>(null) }
    var mostrarCantidadBottomSheet by remember { mutableStateOf(false) }

    var perfilUsuario by remember { mutableStateOf<PerfilUsuario?>(null) }

    val listState = rememberLazyListState()

    val direccionUsuario = perfilUsuario?.direccion ?: ""

    val context = LocalContext.current

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
    val productosFiltrados = categoriaSeleccionada?.let {
        productos.filter { it.categoria_producto == categoriaSeleccionada }
    } ?: productos

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            UserHeader(
                title = "Inicio",
                current = null,
                onNavigateHome = { /* ya estás en Home */ },
                onNavigationToPerfil = { navController.navigate("perfilUser") },
                onNavigationToFacture = { navController.navigate("facturasUser") },
                onNavigationToProduct = { navController.navigate("productosUser") },
                onAbrirCarrito = { mostrarCarritoLateral = true }
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Promociones destacadas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
        }

        // Slider horizontal (como ya lo tienes en SliderPromociones con LazyRow)
        item {
            if (promociones.isNotEmpty()) {
                SliderPromociones(
                    promociones = promociones,
                    onAddClick = { productoSeleccionado = it; mostrarCantidadBottomSheet = true },
                    onAddPromocion = { promoConProductos ->
                        carritoViewModel.agregarPromocionAlCarrito(promoConProductos, context)
                    }
                )
            } else {
                Text("Cargando promociones...", modifier = Modifier.padding(16.dp))
            }
            Spacer(Modifier.height(8.dp))
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

        // Grid con su propio scroll vertical: dale altura finita
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // Usa una altura relativa a la ventana del Lazy (evita hardcodear dp)
                    .fillParentMaxHeight(0.9f)  // puedes ajustar 0.7f..1f según te guste
            ) {
                GridProductos(
                    productos = productosFiltrados,
                    onAddClick = {
                        productoSeleccionado = it
                        mostrarCantidadBottomSheet = true
                    }
                )
            }
        }
    }

    // Panel inferior para seleccionar cantidad
    if (mostrarCantidadBottomSheet && productoSeleccionado != null) {
        ModalBottomSheet(onDismissRequest = {
            mostrarCantidadBottomSheet = false
            productoSeleccionado = null
        }) {
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

    // Carrito lateral
    if (mostrarCarritoLateral) {
        val scope = rememberCoroutineScope()

        CarritoLateral(
            carrito = carritoViewModel.carrito,
            direccionUsuario = direccionUsuario,
            onCerrar = { mostrarCarritoLateral = false },

            // 🚚 Pedir a domicilio
            onReservar = {
                mostrarCarritoLateral = false
                // aquí irá la lógica de envío
            },

            // 🏪 Recoger en tienda
            onRecoger = {
                mostrarCarritoLateral = false
                scope.launch {
                    carritoViewModel.confirmarRecogidaEnTienda(context)
                }
            },

            onEliminarItem = { item ->
                item.producto.id?.let { id ->
                    carritoViewModel.eliminarProducto(id, context)
                }
            }
            ,
            modifier = Modifier.zIndex(1f)
        )

    }

}


