package com.example.carniceria_app

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun HomeAdminScreen(navController: NavHostController, onLogout: () -> Unit) {
    var mostrarFormularioProducto by remember { mutableStateOf(false) }
    var mostrarFormularioPromocion by remember { mutableStateOf(false) }
    var mostrarListaUsuarios by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Panel de Administrador", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    tokenManager.logout()
                    onLogout()
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Cerrar sesión")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            mostrarFormularioProducto = true
            mostrarFormularioPromocion = false
            mostrarListaUsuarios = false
        }) {
            Text("➕ Añadir productos")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            mostrarFormularioProducto = false
            mostrarFormularioPromocion = true
            mostrarListaUsuarios = false
        }) {
            Text("🎁 Añadir promociones")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            mostrarFormularioProducto = false
            mostrarFormularioPromocion = false
            mostrarListaUsuarios = true
        }) {
            Text("👥 Ver usuarios registrados")
        }

        Spacer(modifier = Modifier.height(16.dp))

        /**when {
            mostrarFormularioProducto -> FormularioProducto()
            mostrarFormularioPromocion -> FormularioPromocion()
            mostrarListaUsuarios -> ListaUsuarios()
        }**/
    }
}

