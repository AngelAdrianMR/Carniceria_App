package com.example.carniceria_app

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
fun EmpresaHeader(
    navController: NavHostController,
    titulo: String,
    mostrarCarrito: Boolean = true,
    onAbrirCarrito: (() -> Unit)? = null,
    onLogout: () -> Unit,
    onNavigateHomeEmpresa: () -> Unit,
    onNavigateEmpresaProductos: () -> Unit,
    onNavigateEmpresaPedidos: () -> Unit,
    onNavigateEmpresaPerfil: () -> Unit,
    onNavigateEmpresaConfig: () -> Unit,
    onNavigateEmpresaSobreNosotros: () -> Unit,
    onNavigationToFaqEmpresa: () -> Unit
) {
    var mostrarMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 🔹 Barra superior
    TopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            IconButton(onClick = { mostrarMenu = true }) {
                Icon(Icons.Default.Menu, contentDescription = "Menú")
            }
        },
        actions = {
            if (mostrarCarrito && onAbrirCarrito != null) {
                IconButton(onClick = onAbrirCarrito) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )

    // 🔹 Panel lateral
    AnimatedVisibility(
        visible = mostrarMenu,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(3f)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        ) {

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.6f)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(20.dp)
                    .align(Alignment.CenterStart)
            ) {

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    Text(
                        "Menú Empresa",
                        style = MaterialTheme.typography.titleMedium
                            .copy(color = MaterialTheme.colorScheme.primary)
                    )

                    Divider()

                    BotonMenuLateral("🏠 Inicio") {
                        mostrarMenu = false
                        onNavigateHomeEmpresa()
                    }

                    BotonMenuLateral("🥩 Productos") {
                        mostrarMenu = false
                        onNavigateEmpresaProductos()
                    }

                    BotonMenuLateral("🧾 Pedidos") {
                        mostrarMenu = false
                        onNavigateEmpresaPedidos()
                    }

                    BotonMenuLateral("👤 Perfil") {
                        mostrarMenu = false
                        onNavigateEmpresaPerfil()
                    }

                    BotonMenuLateral("⚙️ Configuración") {
                        mostrarMenu = false
                        onNavigateEmpresaConfig()
                    }

                    BotonMenuLateral("ℹ️ Sobre Nosotros") {
                        mostrarMenu = false
                        onNavigateEmpresaSobreNosotros()
                    }
                    BotonMenuLateral("❓ Preguntas frecuentes") {
                        onNavigationToFaqEmpresa(); mostrarMenu = false
                    }

                    Divider()

                    BotonMenuRojo(
                        texto = "🚪 Cerrar sesión",
                        color = MaterialTheme.colorScheme.error,
                    ) {
                        scope.launch {
                            SupabaseProvider.client.auth.signOut()
                            onLogout()
                        }
                    }

                    Spacer(Modifier.height(50.dp))

                    val logoRes =
                        if (isSystemInDarkTheme()) R.drawable.logo_white else R.drawable.logo_black

                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Fit
                    )
                }

                IconButton(
                    onClick = { mostrarMenu = false },
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
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
        ),
        elevation = ButtonDefaults.buttonElevation(2.dp)
    ) {
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BotonMenuRojo(texto: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkRed,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
    ) {
        Text(texto)
    }
}
