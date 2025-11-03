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
import androidx.compose.material.icons.filled.Menu
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
fun UpBarAdmin(
    navController: NavHostController,
    titulo: String,
    onLogout: () -> Unit
) {
    var mostrarMenuLateral by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 🔹 Barra superior
    TopAppBar(
        title = { Text(titulo) },
        navigationIcon = {
            IconButton(onClick = { mostrarMenuLateral = true }) {
                Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
            }
        }
    )

    // 🔹 Panel lateral (60% del ancho)
    AnimatedVisibility(
        visible = mostrarMenuLateral,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.6f)
                .zIndex(2f)
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text("Menú de Administración", style = MaterialTheme.typography.titleMedium)
                Divider()

                // 🏠 Home principal del panel admin
                BotonTransparenteNegro(
                    texto = "🏠 Inicio",
                    onClick = {
                        navController.navigate("homeAdminScreen") {
                            popUpTo("homeAdminScreen") { inclusive = true }
                        }
                        mostrarMenuLateral = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                BotonTransparenteNegro(
                    texto = "📦 Pedidos",
                    onClick = {
                        navController.navigate("pedidosAdmin")
                        mostrarMenuLateral = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                BotonTransparenteNegro(
                    texto = "🧾 Facturas",
                    onClick = {
                        navController.navigate("facturasAdmin")
                        mostrarMenuLateral = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                BotonTransparenteNegro(
                    texto = "👥 Usuarios",
                    onClick = {
                        navController.navigate("usuariosAdmin")
                        mostrarMenuLateral = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                BotonTransparenteNegro(
                    texto = "🥩 Productos",
                    onClick = {
                        navController.navigate("productosAdmin")
                        mostrarMenuLateral = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                BotonTransparenteNegro(
                    texto = "🎁 Promociones",
                    onClick = {
                        navController.navigate("promocionesAdmin")
                        mostrarMenuLateral = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                BotonTransparenteNegro(
                    texto = "🎟️ Códigos Descuento",
                    onClick = {
                        navController.navigate("codigosAdmin")
                        mostrarMenuLateral = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                BotonTransparenteNegro(
                    texto = "📤 Importar Stock",
                    onClick = {
                        navController.navigate("importarStock")
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

            // 🔘 Botón de cerrar menú
            IconButton(
                onClick = { mostrarMenuLateral = false },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar menú")
            }
        }
    }
}
