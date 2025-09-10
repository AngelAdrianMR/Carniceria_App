package com.example.carniceria_app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosAdminScreen(navController: NavHostController) {
    var productos by remember { mutableStateOf<List<Product>>(emptyList()) }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var productoSeleccionado by remember { mutableStateOf<Product?>(null) }
    var mostrarBottomSheet by remember { mutableStateOf(false) }
    var mostrarNuevoProductoBottomSheet by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Cargar productos desde Supabase
    LaunchedEffect(Unit) {
        try {
            productos = obtenerProductos()
        } catch (e: Exception) {
            Log.e("ProductosAdminScreen", "Error al obtener productos", e)
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

        // Header
        UserHeader(
            title = "Productos",
            current = HeaderTab.Productos,
            onNavigateHome = {
                navController.navigate("homeAdminScreen") {
                    popUpTo("homeUserScreen") { inclusive = false }
                    launchSingleTop = true
                }
            },
            onNavigationToPerfil = { navController.navigate("perfilUser") },
            onNavigationToProduct = { /* ya estás en productos */ },
            onNavigationToFacture = { navController.navigate("facturas") },
            onAbrirCarrito = { /* Admin no usa carrito */ },
            mostrarCarrito = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botón Añadir
        Button(
            onClick = { mostrarNuevoProductoBottomSheet = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("➕ Añadir producto")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Barra de búsqueda
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
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filtro de categorías
        FiltroCategorias(categorias) { categoriaSeleccionada = it }

        // Grid de productos con acciones
        GridProductosAdmin(
            productos = productosFiltrados,
            onEditarClick = {
                productoSeleccionado = it
                mostrarBottomSheet = true
            },
            onEliminarClick = {
                scope.launch {
                    it.id?.let { id ->
                        eliminarProductoPorId(id)
                        productos = productos.filterNot { p -> p.id == id }
                    }
                }
            }
        )
    }

    // BottomSheet editar producto
    if (mostrarBottomSheet && productoSeleccionado != null) {
        EditarProductoBottomSheet(
            producto = productoSeleccionado!!,
            onDismiss = {
                mostrarBottomSheet = false
                productoSeleccionado = null
            },
            onGuardar = { productoEditado ->
                productos = productos.map {
                    if (it.id == productoEditado.id) productoEditado else it
                }
                mostrarBottomSheet = false
                productoSeleccionado = null
            }
        )
    }

    // BottomSheet nuevo producto
    if (mostrarNuevoProductoBottomSheet) {
        NuevoProductoBottomSheet(
            onDismiss = { mostrarNuevoProductoBottomSheet = false },
            onProductoCreado = { nuevoProducto ->
                productos = productos + nuevoProducto
                mostrarNuevoProductoBottomSheet = false
            }
        )
    }
}
