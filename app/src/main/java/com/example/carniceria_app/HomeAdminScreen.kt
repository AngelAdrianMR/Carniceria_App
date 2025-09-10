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
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAdminScreen(navController: NavHostController, onLogout: () -> Unit) {
    val carritoViewModel: CarritoViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val carrito = carritoViewModel.carrito

    var mostrarCarritoLateral by remember { mutableStateOf(false) }
    var promociones by remember { mutableStateOf<List<PromocionConProductos>>(emptyList()) }
    var productos by remember { mutableStateOf<List<Product>>(emptyList()) }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var productoSeleccionado by remember { mutableStateOf<Product?>(null) }
    var mostrarCantidadBottomSheet by remember { mutableStateOf(false) }
    var perfilUsuario by remember { mutableStateOf<PerfilUsuario?>(null) }
    val direccionUsuario = perfilUsuario?.direccion ?: ""
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Cargar datos
    LaunchedEffect(Unit) {
        try {
            promociones = obtenerPromocionesAdmin()
            productos = obtenerProductos()

            perfilUsuario = obtenerPerfilUsuarioActual()

        } catch (e: Exception) {
            Log.e("HomeAdminScreen", "Error al obtener datos", e)
        }
    }

    val productosFiltrados = categoriaSeleccionada?.let {
        productos.filter { it.categoria_producto == categoriaSeleccionada }
    } ?: productos

    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        state = listState,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header + Logout + Botón admin
        item {
            UserHeader(
                title = "Inicio",
                current = null,
                onNavigateHome = { /* ya estás en Home */ },
                onNavigationToPerfil = { navController.navigate("perfilUser") },
                onNavigationToFacture = { navController.navigate("facturasUser") },
                onNavigationToProduct = { navController.navigate("productosAdmin") },
                onAbrirCarrito = { mostrarCarritoLateral = true },
                mostrarCarrito = false
            )
            Spacer(Modifier.height(8.dp))

            BotonTransparenteNegro(
                onClick = {
                    scope.launch {
                        SupabaseProvider.client.auth.signOut()
                        onLogout()
                    }
                },
                modifier = Modifier.fillMaxWidth(), // ocupar ancho (weight no aplica aquí)
                texto = "Cerrar sesión"
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate("promocionesAdmin") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestionar promociones")
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Promociones destacadas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
        }

        // Slider horizontal (tu composable ya usa LazyRow)
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
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Nuestros Productos Destacados",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))

            FiltroCategorias(categorias = productos.map { it.categoria_producto }.distinct()) {
                categoriaSeleccionada = it
            }
            Spacer(Modifier.height(8.dp))
        }

        // Grid con su propio scroll vertical (altura acotada)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillParentMaxHeight(0.9f) // ocupa gran parte de la ventana y permite scroll interno del grid
            ) {
                GridProductos(productosFiltrados) { p ->
                    productoSeleccionado = p
                    mostrarCantidadBottomSheet = true
                }
            }
        }
    }

    // BottomSheet para seleccionar cantidad
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
