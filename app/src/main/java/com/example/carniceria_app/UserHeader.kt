package com.example.carniceria_app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHeader(
    navController: NavHostController,
    titulo: String,
    onLogout: () -> Unit,
    mostrarCarrito: Boolean = true,
    onAbrirCarrito: (() -> Unit)? = null,
    mostrarBotonEditar: Boolean = false,
    onEditarPerfil: (() -> Unit)? = null,
    onNavigateHome: () -> Unit,
    onNavigationToPerfil: () -> Unit,
    onNavigationToProductos: () -> Unit,
    onNavigationToPedidos: () -> Unit,
    onNavigationToConfiguracion: () -> Unit,
    onNavigationToSobreNosotros: () -> Unit
) {
    var mostrarMenuLateral by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 🔹 Barra superior tipo TopAppBar
    TopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            IconButton(onClick = { mostrarMenuLateral = true }) {
                Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
            }
        },
        actions = {
            if (mostrarCarrito && onAbrirCarrito != null) {
                IconButton(onClick = onAbrirCarrito) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                }
            }
            if (mostrarBotonEditar && onEditarPerfil != null) {
                IconButton(onClick = onEditarPerfil) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
                }
            }
        }
    )

    // 🔹 Panel lateral (flotante encima del contenido)
    // 🔹 Panel lateral flotante con overlay
    AnimatedVisibility(
        visible = mostrarMenuLateral,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize() // 👈 cubre toda la pantalla con overlay
                .zIndex(3f)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) // 🔹 fondo semitransparente
        ) {
            // Panel real
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.6f) // 👈 60% como en admin
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
                    .align(Alignment.CenterStart)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Menú de Usuario", style = MaterialTheme.typography.titleMedium)
                    Divider()

                    BotonTransparenteNegro(
                        texto = "🏠 Inicio",
                        onClick = {
                            onNavigateHome()
                            mostrarMenuLateral = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    BotonTransparenteNegro(
                        texto = "🥩 Productos",
                        onClick = {
                            onNavigationToProductos()
                            mostrarMenuLateral = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    BotonTransparenteNegro(
                        texto = "🧾 Pedidos y Facturas",
                        onClick = {
                            onNavigationToPedidos()
                            mostrarMenuLateral = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    BotonTransparenteNegro(
                        texto = "👤 Mi Perfil",
                        onClick = {
                            onNavigationToPerfil()
                            mostrarMenuLateral = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    BotonTransparenteNegro(
                        texto = "⚙️ Configuración",
                        onClick = {
                            onNavigationToConfiguracion()
                            mostrarMenuLateral = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    BotonTransparenteNegro(
                        texto = "ℹ️ Sobre Nosotros",
                        onClick = {
                            onNavigationToSobreNosotros()
                            mostrarMenuLateral = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider()

                    BotonTransparenteNegro(
                        texto = "🚪 Cerrar sesión",
                        onClick = {
                            scope.launch {
                                SupabaseProvider.client.auth.signOut()
                                onLogout()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ❌ Cerrar menú
                IconButton(
                    onClick = { mostrarMenuLateral = false },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar menú")
                }
            }
        }
    }
}
