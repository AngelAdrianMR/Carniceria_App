package com.example.carniceria_app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.PerfilUsuario
import com.carniceria.shared.shared.models.utils.Product
import com.carniceria.shared.shared.models.utils.obtenerPerfilUsuarioActual
import com.carniceria.shared.shared.models.utils.obtenerProductos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosUserScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    var productos by remember { mutableStateOf<List<Product>>(emptyList()) }
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }
    var productoSeleccionado by remember { mutableStateOf<Product?>(null) }
    var mostrarCantidadBottomSheet by remember { mutableStateOf(false) }
    var mostrarCarritoLateral by remember { mutableStateOf(false) }

    val carritoViewModel: CarritoViewModel = viewModel()
    val context = LocalContext.current

    var perfilUsuario by remember { mutableStateOf<PerfilUsuario?>(null) }

    // Colores adaptativos del tema
    val colors = MaterialTheme.colorScheme

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

    // Categorías (limpia nulos/vacíos, distinct)
    val categorias = productos
        .mapNotNull { it.categoria_producto?.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

    var textoBusqueda by remember { mutableStateOf("") }

    val productosFiltrados = productos.filter { producto ->
        val coincideCategoria =
            categoriaSeleccionada == null || producto.categoria_producto == categoriaSeleccionada

        val coincideBusqueda =
            textoBusqueda.isBlank() ||
                    producto.nombre_producto.contains(textoBusqueda, ignoreCase = true) ||
                    producto.descripcion_producto?.contains(textoBusqueda, ignoreCase = true) == true

        coincideCategoria && coincideBusqueda
    }

    // CONTENIDO PRINCIPAL
    Scaffold(
        topBar = {
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
                onEditarPerfil = { navController.navigate("editarPerfilScreen") },
                onNavigationToFaq = { navController.navigate("faqScreen") }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Barra de búsqueda
            TextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                placeholder = { Text("Buscar productos...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceVariant,
                    unfocusedContainerColor = colors.surfaceVariant,
                    disabledContainerColor = colors.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = colors.primary,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface,
                    focusedPlaceholderColor = colors.onSurface.copy(alpha = 0.5f),
                    unfocusedPlaceholderColor = colors.onSurface.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(50)
            )

            // FILTRO IGUAL QUE EN HOME ADMIN/USER
            FiltroCategorias(
                categorias = categorias
            ) { nueva ->
                // Toggle: si pulsas la misma, vuelve a "Todos" (null)
                categoriaSeleccionada = if (categoriaSeleccionada == nueva) null else nueva
            }

            Spacer(Modifier.height(6.dp))

            // Grid de productos
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
    }

    // PANEL INFERIOR — Selección de cantidad
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

    // CARRITO LATERAL
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
                modifier = Modifier.zIndex(1f)
            )
        }
    }
}
