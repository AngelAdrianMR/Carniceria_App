package com.example.carniceria_app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.*
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosUserScreen(navController: NavHostController, onLogout: () -> Unit) {
    var productos by remember { mutableStateOf<List<Product>>(emptyList()) }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var productoSeleccionado by remember { mutableStateOf<Product?>(null) }
    var mostrarCantidadBottomSheet by remember { mutableStateOf(false) }
    var mostrarCarritoLateral by remember { mutableStateOf(false) }
    val carritoViewModel: CarritoViewModel = viewModel()
    val context = LocalContext.current

    var perfilUsuario by remember { mutableStateOf<PerfilUsuario?>(null) }

    // Colores adaptativos
    val isDarkTheme = isSystemInDarkTheme()
    val fondoClaro = Color(0xFFF1F1F1)
    val fondoOscuro = Color(0xFF2B2B2B)
    val grisFiltroOscuro = Color(0xFF383838)

    // Cargar productos
    
    LaunchedEffect(Unit) {
        try {
            perfilUsuario = obtenerPerfilUsuarioActual()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) fondoOscuro else Color.White)
    ) {

        // 🔹 Cabecera
        UserHeader(
            navController = navController,
            titulo = "Productos",
            onNavigateHome = { navController.navigate("homeUserScreen") },
            onNavigationToPerfil = { navController.navigate("perfilUser") },
            onNavigationToProductos = { navController.navigate("productosUser") },
            onNavigationToPedidos = { navController.navigate("pedidosYFacturas") },
            onNavigationToConfiguracion = { navController.navigate("configuracionScreen") },
            onNavigationToSobreNosotros = { navController.navigate("sobreNosotrosScreen") },
            onLogout = onLogout,
            mostrarCarrito = true,
            onAbrirCarrito = { mostrarCarritoLateral = true },
            mostrarBotonEditar = false,
            onEditarPerfil = {
                navController.navigate("editarPerfilScreen")
            }
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
                focusedContainerColor = if (isDarkTheme) grisFiltroOscuro else fondoClaro,
                unfocusedContainerColor = if (isDarkTheme) grisFiltroOscuro else fondoClaro,
                disabledContainerColor = if (isDarkTheme) grisFiltroOscuro else fondoClaro,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(50)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Filtro de categorías
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDarkTheme) fondoOscuro else Color.White)
                .padding(vertical = 4.dp)
        ) {
            FiltroCategorias(categorias) { categoriaSeleccionada = it }
        }

        // 🔹 Grid de productos
        GridProductos(
            productosFiltrados,
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
        perfilUsuario?.let { perfil ->
            CarritoLateral(
                carrito = carritoViewModel.carrito,
                direccionUsuario = perfil.direccionCompleta,
                usuarioId = perfil.id,
                carritoViewModel = carritoViewModel,
                codigoPostalUsuario = perfil.codigo_postal,
                onCerrar = { mostrarCarritoLateral = false },
                onEliminarItem = { item ->
                    item.producto?.id?.let { id ->
                        carritoViewModel.eliminarProducto(item, context)
                    }
                },
                modifier = Modifier.zIndex(1f)
            )
        }
    }
}
