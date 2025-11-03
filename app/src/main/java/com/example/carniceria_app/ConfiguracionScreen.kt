package com.example.carniceria_app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 🔔 Estado de notificaciones (persistente con DataStore)
    var notificacionesActivas by remember { mutableStateOf(true) }

    // Cargar preferencia al entrar
    LaunchedEffect(Unit) {
        NotificationPreferences.getNotificationsEnabled(context).collect { valor ->
            notificacionesActivas = valor
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 🌗 Tema oscuro
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tema oscuro")
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { newValue ->
                        onThemeChange(newValue)
                        Toast.makeText(
                            context,
                            if (newValue) "Tema oscuro activado" else "Tema claro activado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            // 🔔 Notificaciones (persistentes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Notificaciones")
                Switch(
                    checked = notificacionesActivas,
                    onCheckedChange = { checked ->
                        notificacionesActivas = checked
                        scope.launch {
                            NotificationPreferences.setNotificationsEnabled(context, checked)
                        }
                        Toast.makeText(
                            context,
                            if (checked) "Notificaciones activadas" else "Notificaciones desactivadas",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ℹ️ Sobre nosotros
            BotonTransparenteNegro(
                onClick = { navController.navigate("sobreNosotrosScreen") },
                texto = "Sobre nosotros",
                modifier = Modifier.fillMaxWidth()
            )

            // 🚪 Cerrar sesión
            BotonTransparenteNegro(
                onClick = {
                    authViewModel.cerrarSesion()
                    Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    navController.navigate("login") {
                        popUpTo("homeUserScreen") { inclusive = true }
                    }
                },
                texto = "Cerrar sesión",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
