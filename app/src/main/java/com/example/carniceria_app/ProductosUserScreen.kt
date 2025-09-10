package com.example.carniceria_app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.*
import androidx.compose.ui.zIndex
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosUserScreen(navController: NavHostController) {
    var productos by remember { mutableStateOf<List<Product>>(emptyList()) }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var productoSeleccionado by remember { mutableStateOf<Product?>(null) }
    var mostrarCantidadBottomSheet by remember { mutableStateOf(false) }
    var mostrarCarritoLateral by remember { mutableStateOf(false) }
    val carritoViewModel: CarritoViewModel = viewModel()
    val context = LocalContext.current

    var perfilUsuario by remember { mutableStateOf<PerfilUsuario?>(null) }
    val direccionUsuario = perfilUsuario?.direccion ?: ""

    // Cargar productos
    LaunchedEffect(Unit) {
        try {
            productos = obtenerProductos()
            carritoViewModel.cargarCarritoLocal(context)
        } catch (e: Exception) {
            Log.e("ProductosUserScreen", "Error al obtener productos", e)
        }
    }

    val categorias = productos.map { it.categoria_producto }.distinct()
    var textoBusqueda by remember { mutableStateOf("") }
    val productosFiltrados = productos.filter { producto ->
        val coincideCategoria = categoriaSeleccionada == null || producto.categoria_producto == categoriaSeleccionada
        val coincideBusqueda = textoBusqueda.isBlank() ||
                producto.nombre_producto.contains(textoBusqueda, ignoreCase = true) ||
                producto.descripcion_producto?.contains(textoBusqueda, ignoreCase = true) == true

        coincideCategoria && coincideBusqueda
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // 🔹 Cabecera
        UserHeader(
            title = "Productos",
            current = HeaderTab.Productos,
            onNavigateHome = {
                navController.navigate("homeUserScreen") {
                    popUpTo("homeUserScreen") { inclusive = false }
                    launchSingleTop = true
                }
            },
            onNavigationToPerfil = { navController.navigate("perfilUser") },
            onNavigationToProduct = { /* ya estás en productos */ },
            onNavigationToFacture = { navController.navigate("facturas") },
            onAbrirCarrito = { mostrarCarritoLateral = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Barra de búsqueda
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
                focusedContainerColor = Color(0xFFF1F1F1),
                unfocusedContainerColor = Color(0xFFF1F1F1),
                disabledContainerColor = Color(0xFFF1F1F1),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(50)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Filtro de categorías
        FiltroCategorias(categorias) { categoriaSeleccionada = it }

        // 🔹 Grid de productos
        GridProductos(
            productosFiltrados,
            onAddClick = {
                productoSeleccionado = it
                mostrarCantidadBottomSheet = true
            }
        )
    }

    // 🔹 Panel inferior para seleccionar cantidad
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

    // 🔹 Carrito lateral
    if (mostrarCarritoLateral) {
        val scope = rememberCoroutineScope()

        CarritoLateral(
            carrito = carritoViewModel.carrito,
            direccionUsuario = direccionUsuario,
            onCerrar = { mostrarCarritoLateral = false },
            onReservar = {
                // 🚚 Lógica para pedir a domicilio
                mostrarCarritoLateral = false
            },
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
            },
            modifier = Modifier.zIndex(1f)
        )

    }
}
