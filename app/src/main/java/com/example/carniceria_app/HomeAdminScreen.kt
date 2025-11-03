package com.example.carniceria_app

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    val carrito = carritoViewModel.carrito

    var mostrarCarritoLateral by remember { mutableStateOf(false) }
    var mostrarMenuLateral by remember { mutableStateOf(false) }

    var promociones by remember { mutableStateOf<List<PromocionConProductos>>(emptyList()) }
    var productos by remember { mutableStateOf<List<Product>>(emptyList()) }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var productoSeleccionado by remember { mutableStateOf<Product?>(null) }
    var mostrarCantidadBottomSheet by remember { mutableStateOf(false) }
    var perfilUsuario by remember { mutableStateOf<PerfilUsuario?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mensajeError by remember { mutableStateOf<String?>(null) }

    // 🔹 Cargar datos iniciales
    LaunchedEffect(Unit) {
        try {
            promociones = obtenerPromocionesAdmin()
            productos = obtenerProductos()
            perfilUsuario = obtenerPerfilUsuarioActual()
        } catch (e: Exception) {
            Log.e("HomeAdminScreen", "❌ Error al obtener datos", e)
            mensajeError = "Error al cargar datos: ${e.message}"
        }
    }
    val productosDestacados = productos.filter { it.destacado == true }
    val productosFiltrados = categoriaSeleccionada?.let { categoria ->
        productosDestacados.filter { it.categoria_producto == categoria }
    } ?: productosDestacados

    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            // 🧱 Usamos aquí la nueva barra admin reutilizable
            UpBarAdmin(
                navController = navController,
                titulo = "Panel de Administración",
                onLogout = onLogout
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
            // 🔹 Promociones destacadas
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

            // 🔸 Slider de promociones
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
                        onPromoClick = {
                                promo ->
                            promo.id?.let { id ->
                                navController.navigate("promocionDetalle/$id")}
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

            // 🔸 Grid de productos
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

    // 🧩 BottomSheet de cantidad
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

    // 🛒 Carrito lateral
    if (mostrarCarritoLateral) {
        perfilUsuario?.let { perfil ->
            CarritoLateral(
                carrito = carrito,
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

