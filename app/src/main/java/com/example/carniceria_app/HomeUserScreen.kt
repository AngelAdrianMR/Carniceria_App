package com.example.carniceria_app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUserScreen(navController: NavHostController, onLogout: () -> Unit) {
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
    val tokenManager = remember { TokenManager(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            promociones = obtenerPromociones()
            productos = obtenerProductos()
            carritoViewModel.inicializarStock(productos)

            val accessToken = authViewModel.accessToken.value
            if (accessToken != null) {
                perfilUsuario = obtenerPerfilUsuarioActual(accessToken)
            }


        } catch (e: Exception) {
            Log.e("HomeUserScreen", "Error al obtener datos", e)
        }
    }

    val categorias = productos.map { it.categoria_producto }.distinct()
    val productosFiltrados = categoriaSeleccionada?.let {
        productos.filter { it.categoria_producto == categoriaSeleccionada }
    } ?: productos

    Column {
        UserHeader(
            title = "Carnicería",
            onNavigationToPerfil = { navController.navigate("perfilUser") },
            onNavigationToFacture = { navController.navigate("facturasUser") },
            onNavigationToProduct = { navController.navigate("productosUser") },
            onAbrirCarrito = { mostrarCarritoLateral = true } // 👈 Aquí lo añadimos
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    tokenManager.logout()
                    onLogout()
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Cerrar sesión")
        }

        if (promociones.isNotEmpty()) {
            SliderPromociones(promociones = promociones)
        } else {
            Text("Cargando promociones...", modifier = Modifier.padding(16.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        FiltroCategorias(categorias) {
            categoriaSeleccionada = it
        }

        Spacer(modifier = Modifier.height(8.dp))

        GridProductos(productosFiltrados) {
            productoSeleccionado = it
            mostrarCantidadBottomSheet = true
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
        CarritoLateral(
            carrito = carritoViewModel.carrito,
            direccionUsuario = direccionUsuario, // ← AÑADE ESTO
            onCerrar = { mostrarCarritoLateral = false },
            onReservar = {
                // TODO: Acción al reservar
                mostrarCarritoLateral = false
            }
        )
    }

}


