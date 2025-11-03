package com.example.carniceria_app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.carniceria.shared.shared.models.utils.*
import com.example.carniceria_app.data.ThemePreferences
import com.example.carniceria_app.ui.theme.CarniceriaAppTheme
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var navControllerRef: NavHostController? = null

    private fun startGoogleOAuth() {
        lifecycleScope.launch {
            SupabaseProvider.client.auth.signInWith(Google, redirectUrl = "myapp://auth-callback")
        }
    }

    private fun navigateAfterLogin(navController: NavHostController) {
        lifecycleScope.launch {
            val perfil = obtenerPerfilUsuarioActual()
            val dest = if (perfil?.rol == "Administrador") "homeAdminScreen" else "homeUserScreen"
            navController.navigate(dest) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.google.firebase.FirebaseApp.initializeApp(this)
        val startIntent = intent

        // 🔔 Permiso de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }

        setContent {
            val navController = rememberNavController()
            navControllerRef = navController

            // 🧠 DataStore para recordar el tema
            val themePrefs = remember { ThemePreferences(applicationContext) }
            val darkThemeFlow = themePrefs.darkThemeFlow.collectAsState(initial = false)
            var darkTheme by rememberSaveable { mutableStateOf(darkThemeFlow.value) }

            // 🔄 Sincronizar el estado Compose con DataStore
            LaunchedEffect(darkThemeFlow.value) {
                darkTheme = darkThemeFlow.value
            }

            // 💾 Función para guardar el tema
            fun cambiarTema(value: Boolean) {
                darkTheme = value
                lifecycleScope.launch { themePrefs.saveDarkTheme(value) }
            }

            // ✅ Deep Link inicial
            LaunchedEffect(Unit) {
                startIntent?.data?.let { uri ->
                    handleAuthRedirectUri(uri, navController)
                }
            }

            var startDestination by rememberSaveable { mutableStateOf<String?>(null) }

            // 🧾 Detectar sesión activa
            LaunchedEffect(Unit) {
                val session = SupabaseProvider.client.auth.currentSessionOrNull()
                startDestination = if (session != null) {
                    val perfil = obtenerPerfilUsuarioActual()
                    when (perfil?.rol) {
                        "Administrador" -> "homeAdminScreen"
                        "Cliente" -> "homeUserScreen"
                        else -> "login"
                    }
                } else {
                    "login"
                }
            }

            // 🎨 Aplicar tema global
            CarniceriaAppTheme(darkTheme = darkTheme) {
                if (startDestination != null) {
                    NavHost(navController = navController, startDestination = startDestination!!) {

                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    lifecycleScope.launch {
                                        val perfil = obtenerPerfilUsuarioActual()
                                        val dest = if (perfil?.rol == "Administrador")
                                            "homeAdminScreen"
                                        else
                                            "homeUserScreen"
                                        navController.navigate(dest) {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }

                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onBackToLogin = { navController.popBackStack() },
                                onGoogleSignInClick = { startGoogleOAuth() }
                            )
                        }

                        composable("homeUserScreen") {
                            HomeUserScreen(navController = navController, onLogout = {
                                lifecycleScope.launch {
                                    SupabaseProvider.client.auth.signOut()
                                    navController.navigate("login") {
                                        popUpTo("homeUserScreen") { inclusive = true }
                                    }
                                }
                            })
                        }

                        composable("homeAdminScreen") {
                            HomeAdminScreen(navController = navController, onLogout = {
                                lifecycleScope.launch {
                                    SupabaseProvider.client.auth.signOut()
                                    navController.navigate("login") {
                                        popUpTo("homeAdminScreen") { inclusive = true }
                                    }
                                }
                            })
                        }

                        composable("productosAdmin") { ProductosAdminScreen(navController, onLogout = {
                            lifecycleScope.launch {
                                SupabaseProvider.client.auth.signOut()
                                navController.navigate("login") {
                                    popUpTo("homeAdminScreen") { inclusive = true }
                                }
                            }
                        }) }
                        composable("productosUser") { ProductosUserScreen(navController,onLogout = {
                            lifecycleScope.launch {
                                SupabaseProvider.client.auth.signOut()
                                navController.navigate("login") {
                                    popUpTo("homeUserScreen") { inclusive = true }
                                }
                            }
                        }) }
                        composable("perfilUser") { backStackEntry ->
                            val navController = rememberNavController()

                            val refreshTrigger = remember {
                                mutableStateOf(false)
                            }

                            // 🔹 Detectar si se ha guardado un cambio al volver
                            val savedStateHandle = backStackEntry.savedStateHandle
                            val updateSignal = savedStateHandle?.getLiveData<Boolean>("perfilActualizado")

                            LaunchedEffect(updateSignal?.value) {
                                if (updateSignal?.value == true) {
                                    refreshTrigger.value = true
                                    savedStateHandle["perfilActualizado"] = false // Reset
                                }
                            }

                            PerfilUserScreen(
                                navController = navController,
                                onLogout = {
                                    lifecycleScope.launch {
                                        SupabaseProvider.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("perfilUser") { inclusive = true }
                                        }
                                    }
                                },
                                refreshTrigger = refreshTrigger.value
                            )
                        }

                        composable("editarPerfilScreen") { backStackEntry ->
                            EditPerfilUserScreen(navController = navController, onLogout = { /* tu lógica */ })
                        }

                        composable("perfilUser") {
                            PerfilUserScreen(
                                navController = navController,
                                onLogout = {
                                    lifecycleScope.launch {
                                        SupabaseProvider.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("perfilUser") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("promocionesAdmin") {
                            val viewModel: PromocionesAdminViewModel = viewModel()
                            PromocionesAdminScreen(viewModel = viewModel, navController = navController, onLogout = {
                                lifecycleScope.launch {
                                    SupabaseProvider.client.auth.signOut()
                                    navController.navigate("login") {
                                        popUpTo("homeAdminScreen") { inclusive = true }
                                    }
                                }
                            })
                        }

                        composable("importarStock") {
                            ImportarStockScreen(navController = navController, onLogout = {
                                lifecycleScope.launch {
                                    SupabaseProvider.client.auth.signOut()
                                    navController.navigate("login") {
                                        popUpTo("homeAdminScreen") { inclusive = true }
                                    }
                                }
                            })
                        }

                        composable("usuariosAdmin") { UsuariosAdminScreen(navController, onLogout = {
                            lifecycleScope.launch {
                                SupabaseProvider.client.auth.signOut()
                                navController.navigate("login") {
                                    popUpTo("homeAdminScreen") { inclusive = true }
                                }
                            }
                        }) }
                        composable("facturasAdmin") { FacturasAdminScreen(navController, onLogout = {
                            lifecycleScope.launch {
                                SupabaseProvider.client.auth.signOut()
                                navController.navigate("login") {
                                    popUpTo("homeAdminScreen") { inclusive = true }
                                }
                            }
                        }) }

                        composable(
                            route = "productoDetalle/{productoId}",
                            arguments = listOf(navArgument("productoId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val productoId = backStackEntry.arguments?.getLong("productoId") ?: return@composable
                            val usuario = obtenerUsuarioActual()
                            if (usuario != null) {
                                ProductoDetalleScreen(
                                    navController = navController,
                                    productoId = productoId,
                                    usuarioId = usuario.id,
                                    service = SupabaseService(SupabaseProvider.client)
                                )
                            }
                        }

                        composable(
                            route = "promocionDetalle/{promoId}",
                            arguments = listOf(navArgument("promoId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val promoId = backStackEntry.arguments?.getLong("promoId") ?: 0L

                            // 🧩 Usa el mismo CarritoViewModel compartido con homeUserScreen
                            val parentEntry = remember(backStackEntry) {
                                navController.getBackStackEntry("homeUserScreen")
                            }
                            val carritoViewModel: CarritoViewModel = viewModel(parentEntry)

                            PromocionDetalleScreen(
                                navController = navController,
                                promoId = promoId,
                                carritoViewModel = carritoViewModel
                            )
                        }

                        composable("codigosAdmin") {
                            var codigos by remember { mutableStateOf<List<CodigoDescuento>>(emptyList()) }
                            val scope = rememberCoroutineScope()
                            val service = remember { SupabaseService(SupabaseProvider.client) }

                            LaunchedEffect(Unit) {
                                scope.launch {
                                    try {
                                        codigos = service.obtenerCodigosDescuento()
                                    } catch (e: Exception) {
                                        println("❌ Error al cargar códigos: ${e.message}")
                                    }
                                }
                            }

                            CodigosDescuentoAdminScreen(
                                navController = navController,
                                codigos = codigos,
                                onToggleActivo = { codigo ->
                                    scope.launch {
                                        try {
                                            codigo.id?.let { service.toggleActivoCodigo(it, !codigo.activo) }
                                            codigos = service.obtenerCodigosDescuento()
                                        } catch (e: Exception) {
                                            println("❌ Error al actualizar código: ${e.message}")
                                        }
                                    }
                                },
                                onCrearNuevo = { nuevoCodigo ->
                                    scope.launch {
                                        try {
                                            service.crearCodigoDescuento(nuevoCodigo)
                                            codigos = service.obtenerCodigosDescuento()
                                        } catch (e: Exception) {
                                            println("❌ Error al crear código: ${e.message}")
                                        }
                                    }
                                },
                                onLogout = {
                                    lifecycleScope.launch {
                                        SupabaseProvider.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("homeAdminScreen") { inclusive = true }
                                        }
                                    }
                                },
                                onEliminarCodigo = { codigo ->
                                    scope.launch {
                                        try {
                                            codigo.id?.let { service.eliminarCodigoDescuento(it) }
                                            codigos = service.obtenerCodigosDescuento() // refrescar lista
                                        } catch (e: Exception) {
                                            println("❌ Error al eliminar código: ${e.message}")
                                        }
                                    }
                                },
                            )
                        }

                        composable("pedidosYFacturas") {
                            val usuario = obtenerUsuarioActual()
                            usuario?.id?.let { id ->
                                PedidosYFacturasScreen(navController = navController, usuarioId = id,onLogout = {
                                    lifecycleScope.launch {
                                        SupabaseProvider.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("homeUserScreen") { inclusive = true }
                                        }
                                    }
                                })
                            }
                        }

                        composable("pedidosAdmin") { PedidosAdminScreen(navController = navController, onLogout = {
                            lifecycleScope.launch {
                                SupabaseProvider.client.auth.signOut()
                                navController.navigate("login") {
                                    popUpTo("homeAdminScreen") { inclusive = true }
                                }
                            }
                        }) }

                        // ⚙️ Configuración persistente
                        composable("configuracionScreen") {
                            ConfiguracionScreen(
                                navController = navController,
                                isDarkTheme = darkTheme,
                                onThemeChange = { cambiarTema(it) }
                            )
                        }

                        composable("sobreNosotrosScreen") {
                            SobreNosotrosScreen(navController)
                        }
                    }
                } else {
                    LoadingScreen()
                }
            }
        }
    }

    private fun handleAuthRedirectUri(uri: Uri, navController: NavHostController) {
        lifecycleScope.launch {
            try {
                SupabaseProvider.client.auth.exchangeCodeForSession(uri.toString())
                val perfil = obtenerPerfilUsuarioActual()
                val destino = when (uri.getQueryParameter("type")) {
                    "recovery" -> "reset_password"
                    else -> if (perfil?.rol == "Administrador") "homeAdminScreen" else "homeUserScreen"
                }
                navController.navigate(destino) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            } catch (e: Exception) {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri ->
            navControllerRef?.let { handleAuthRedirectUri(uri, it) }
        }
    }
}

// 📦 Pantalla de carga
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// Botón reutilizable
@Composable
fun BotonTransparenteNegro(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    texto: String
) {
    val colors = MaterialTheme.colorScheme
    val borderColor = colors.onBackground
    val contentColor = colors.onBackground

    OutlinedButton(
        onClick = onClick as () -> Unit,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(texto, color = contentColor)
    }
}

