package com.example.carniceria_app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.PerfilConEmail
import com.carniceria.shared.shared.models.utils.obtenerPerfilCompleto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUserScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    refreshTrigger: Boolean = false
) {
    var perfil by remember { mutableStateOf<PerfilConEmail?>(null) }

    // 🔁 Recargar perfil al entrar o si vuelve de edición
    LaunchedEffect(Unit, refreshTrigger) {
        perfil = obtenerPerfilCompleto()
    }

    Scaffold(
        topBar = {
            UserHeader(
                navController = navController,
                titulo = "Mi Perfil",
                onNavigateHome = { navController.navigate("homeUserScreen") },
                onNavigationToPerfil = { navController.navigate("perfilUser") },
                onNavigationToProductos = { navController.navigate("productosUser") },
                onNavigationToPedidos = { navController.navigate("pedidosYFacturas") },
                onNavigationToConfiguracion = { navController.navigate("configuracionScreen") },
                onNavigationToSobreNosotros = { navController.navigate("sobreNosotrosScreen") },
                onLogout = onLogout,
                mostrarCarrito = false,
                mostrarBotonEditar = true,
                onEditarPerfil = {
                    navController.navigate("editarPerfilScreen")
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (perfil == null) {
                CircularProgressIndicator()
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "👤 ${perfil!!.nombre_completo ?: "Usuario"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text("📧 Correo: ${perfil!!.email ?: "-"}")
                        Text("📞 Teléfono: ${perfil!!.telefono ?: "-"}")

                        Text(
                            "🏡 Dirección: ${
                                listOfNotNull(
                                    perfil!!.calle,
                                    perfil!!.piso,
                                    perfil!!.localidad,
                                    perfil!!.provincia,
                                    perfil!!.pais
                                ).joinToString(", ").ifEmpty { "-" }
                            }"
                        )

                        Text("🏙️ Código postal: ${perfil!!.codigoPostal ?: "-"}")

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Gracias por formar parte de nuestra carnicería ❤️",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
