package com.example.carniceria_app

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.PerfilUsuario
import com.carniceria.shared.shared.models.utils.Product
import com.carniceria.shared.shared.models.utils.PromocionConProductos
import com.carniceria.shared.shared.models.utils.obtenerPerfilUsuarioActual
import com.carniceria.shared.shared.models.utils.obtenerProductos
import com.carniceria.shared.shared.models.utils.obtenerPromociones
import com.example.carniceria_app.ui.theme.brown
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Icon

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

    // Drawer para filtros
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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

    // Productos destacados
    val productosDestacados = productos.filter { it.destacado == true }

    // Categorías
    val categorias = productosDestacados
        .mapNotNull { it.categoria_producto }
        .distinct()

    // Filtrar por categoría
    val productosFiltrados = categoriaSeleccionada?.let { cat ->
        productosDestacados.filter { it.categoria_producto == cat }
    } ?: productosDestacados

    // ===========================================================
    // 🟦 PANEL LATERAL — FILTROS (Drawer)
    // ===========================================================
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        "Filtros",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Divider()

                    Text(
                        "Categorías",
                        style = MaterialTheme.typography.titleMedium
                    )

                    categorias.forEach { categoria ->
                        FilterButton(
                            categoria = categoria,
                            seleccionada = categoriaSeleccionada == categoria,
                            onClick = {
                                categoriaSeleccionada = categoria
                                scope.launch { drawerState.close() }
                            }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

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

        // ===========================================================
        // 🟥 CONTENIDO PRINCIPAL
        // ===========================================================
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
                    onNavigationToSobreNosotros = { navController.navigate("sobreNosotrosScreen") },
                    onNavigationToFaq = { navController.navigate("faqScreen") }
                )
            }
        ) { paddingValues ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                state = listState,
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {

                // -------------------------------------------------------
                // ⭐ PROMOCIONES
                // -------------------------------------------------------
                item {
                    SectionTitle("Promociones destacadas")
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
                        Text(
                            "Cargando promociones...",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // -------------------------------------------------------
                    //  PRODUCTOS DESTACADOS + botón de filtros
                    // -------------------------------------------------------
                    SectionTitle("Nuestros Productos Destacados")

                    Spacer(Modifier.height(6.dp))

                    // Botón de filtros ancho completo
                    FilledTonalButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = brown,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Abrir filtros",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Filtros",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }

                // -------------------------------------------------------
                //  GRID PRODUCTOS
                // -------------------------------------------------------
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

        // ===========================================================
        // BottomSheet cantidad
        // ===========================================================
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

        // ===========================================================
        // Carrito lateral
        // ===========================================================
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
}

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Divider(
            modifier = Modifier
                .width(120.dp)
                .height(2.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
        )
    }
}

@Composable
fun FilterButton(
    categoria: String,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val color = if (seleccionada) colors.primary else colors.onSurface

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = categoria,
            color = color
        )
    }
}
