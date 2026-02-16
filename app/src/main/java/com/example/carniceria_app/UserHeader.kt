package com.example.carniceria_app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import com.example.carniceria_app.ui.theme.DarkRed
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
    onNavigationToSobreNosotros: () -> Unit,
    onNavigationToFaq: () -> Unit
) {
    var mostrarMenuLateral by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 🔹 Barra superior tipo TopAppBar
    TopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            IconButton(onClick = { mostrarMenuLateral = true }) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Abrir menú",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        actions = {
            if (mostrarCarrito && onAbrirCarrito != null) {
                IconButton(onClick = onAbrirCarrito) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Carrito",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (mostrarBotonEditar && onEditarPerfil != null) {
                IconButton(onClick = onEditarPerfil) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,      // 👈 mismo tono que la pantalla
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )

    // 🔹 Panel lateral flotante con overlay
    AnimatedVisibility(
        visible = mostrarMenuLateral,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(3f)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)),

        ) {
            // Panel real
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.6f)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant) // 🎨 mismo fondo que el carrito
                    .padding(20.dp)
                    .align(Alignment.CenterStart),

            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Menú de Usuario",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Botones con sombra ligera
                    BotonMenuLateral("🏠 Inicio") {
                        onNavigateHome(); mostrarMenuLateral = false
                    }
                    BotonMenuLateral("🥩 Productos") {
                        onNavigationToProductos(); mostrarMenuLateral = false
                    }
                    BotonMenuLateral("🧾 Pedidos y Facturas") {
                        onNavigationToPedidos(); mostrarMenuLateral = false
                    }
                    BotonMenuLateral("👤 Mi Perfil") {
                        onNavigationToPerfil(); mostrarMenuLateral = false
                    }
                    BotonMenuLateral("⚙️ Configuración") {
                        onNavigationToConfiguracion(); mostrarMenuLateral = false
                    }
                    BotonMenuLateral("ℹ️ Sobre Nosotros") {
                        onNavigationToSobreNosotros(); mostrarMenuLateral = false
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    BotonMenuRojo(
                        "🚪 Cerrar sesión",
                        color = MaterialTheme.colorScheme.error
                    ) {
                        scope.launch {
                            SupabaseProvider.client.auth.signOut()
                            onLogout()
                        }
                    }
                    val isDarkTheme = isSystemInDarkTheme()
                    val logoRes = if (isDarkTheme) R.drawable.logo_white else R.drawable.logo_black

                    Spacer(Modifier.height(50.dp))

                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "Logo Carnicería",
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Fit
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

@Composable
private fun BotonMenuLateral(
    texto: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, Color.Black),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Black
        )
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
    }
}

@Composable
private fun BotonMenuRojo(
    texto: String,
    color: androidx.compose.ui.graphics.Color = DarkRed,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
    ) {
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}
