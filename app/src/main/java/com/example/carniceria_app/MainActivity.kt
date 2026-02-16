package com.example.carniceria_app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.example.carniceria_app.ui.theme.DarkGreen
import com.example.carniceria_app.ui.theme.DarkRed
import com.example.carniceria_app.ui.theme.brown
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private val deepLinkUri = MutableStateFlow<Uri?>(null)

class MainActivity : ComponentActivity() {

    //private var navControllerRef: NavHostController? = null

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
        deepLinkUri.value = intent?.data
        com.google.firebase.FirebaseApp.initializeApp(this)
        //val startIntent = intent

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
            //navControllerRef = navController
            val authViewModel: AuthViewModel = viewModel()

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

            var startDestination by rememberSaveable { mutableStateOf<String?>(null) }
            val pendingUri by deepLinkUri.collectAsState()

            LaunchedEffect(startDestination, pendingUri) {
                if (startDestination != null && pendingUri != null) {
                    handleAuthRedirectUri(pendingUri!!, navController)
                    deepLinkUri.value = null // importante: evitar procesarlo 2 veces
                }
            }

            // ✅ Deep Link inicial
            /**LaunchedEffect(Unit) {
                startIntent?.data?.let { uri ->
                    handleAuthRedirectUri(uri, navController)
                }
            }**/

            // 🧾 Detectar sesión activa
            LaunchedEffect(Unit) {
                val remember = authViewModel.rememberMe.value
                val session = SupabaseProvider.client.auth.currentSessionOrNull()
                startDestination = if (session != null && remember) {
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
                                        Log.i("DEBUG_LOGIN", "Perfil usuario → rol=${perfil?.rol}, empresa_id=${perfil?.empresa_id}")
                                        val dest = if (perfil?.rol == "Administrador")
                                            "homeAdminScreen"
                                        else if (perfil?.rol == "Empresa")
                                            "homeEmpresaScreen/${perfil.empresa_id}"
                                        else
                                            "homeUserScreen"
                                        navController.navigate(dest) {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        }
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("register") },
                                authViewModel = authViewModel
                            )
                        }

                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onBackToLogin = { navController.popBackStack() })
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

                        composable(
                            "homeEmpresaScreen/{empresaId}",
                            arguments = listOf(navArgument("empresaId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val empresaId = backStackEntry.arguments!!.getLong("empresaId")
                            HomeEmpresaScreen(
                                navController = navController,
                                empresaId = empresaId,
                                onLogout = {
                                    lifecycleScope.launch {
                                        SupabaseProvider.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("homeEmpresaScreen/$empresaId") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable(
                            route = "productosEmpresaScreen/{empresaId}",
                            arguments = listOf(navArgument("empresaId") { type = NavType.LongType })
                        ) { entry ->
                            val empresaId = entry.arguments!!.getLong("empresaId")
                            ProductosEmpresaScreen(
                                navController = navController,
                                onLogout = {
                                    lifecycleScope.launch {
                                        SupabaseProvider.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("productosEmpresaScreen/$empresaId") { inclusive = true }
                                        }
                                    }
                                },
                                empresaId = empresaId
                            )
                        }

                        composable(
                            route = "perfilEmpresaScreen/{empresaId}",
                            arguments = listOf(navArgument("empresaId") { type = NavType.LongType })
                        ) { entry ->
                            val empresaId = entry.arguments!!.getLong("empresaId")
                            PerfilEmpresaScreen(
                                navController = navController,
                                empresaId = empresaId,
                                onLogout = {
                                    lifecycleScope.launch {
                                        SupabaseProvider.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("perfilEmpresaScreen/$empresaId") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable(
                            route = "configEmpresaScreen/{empresaId}",
                            arguments = listOf(navArgument("empresaId") { type = NavType.LongType })
                        ) { entry ->

                            val empresaId = entry.arguments!!.getLong("empresaId")

                            val authViewModel: AuthViewModel = viewModel()

                            // 🔥 Importante: leer el tema actual desde tus preferencias
                            val themePrefs = remember { ThemePreferences(applicationContext) }
                            val darkThemeFlow = themePrefs.darkThemeFlow.collectAsState(initial = false)
                            val isDarkTheme = darkThemeFlow.value

                            // función para guardar el cambio de tema
                            fun cambiarTema(value: Boolean) {
                                lifecycleScope.launch { themePrefs.saveDarkTheme(value) }
                            }

                            ConfiguracionEmpresaScreen(
                                navController = navController,
                                empresaId = empresaId,
                                authViewModel = authViewModel,
                                isDarkTheme = isDarkTheme,
                                onThemeChange = { cambiarTema(it) },
                                onLogout = {
                                    lifecycleScope.launch {
                                        SupabaseProvider.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("configEmpresaScreen/$empresaId") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }


                        composable(
                            route = "sobreNosotrosEmpresaScreen/{empresaId}",
                            arguments = listOf(navArgument("empresaId") { type = NavType.LongType })
                        ) { entry ->
                            val empresaId = entry.arguments!!.getLong("empresaId")
                            SobreNosotrosEmpresaScreen(
                                navController = navController,
                                empresaId = empresaId,
                                onLogout = {
                                    lifecycleScope.launch {
                                        SupabaseProvider.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("sobreNosotrosEmpresaScreen/$empresaId") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("faqScreen") {
                            FaqScreen(
                                navController = navController,
                                onLogout = {
                                    lifecycleScope.launch {
                                        SupabaseProvider.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("faqScreen") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable(
                            route = "faqEmpresaScreen/{empresaId}",
                            arguments = listOf(navArgument("empresaId") { type = NavType.LongType })
                        ) { entry ->
                            val empresaId = entry.arguments!!.getLong("empresaId")
                            FaqEmpresaScreen(
                                navController = navController,
                                empresaId = empresaId,
                                onLogout = {
                                    lifecycleScope.launch {
                                        SupabaseProvider.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("faqEmpresaScreen/$empresaId") { inclusive = true }
                                        }
                                    }
                                }
                            )
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
                            route = "productoDetalle/{productoId}?empresaId={empresaId}",
                            arguments = listOf(
                                navArgument("productoId") { type = NavType.LongType },
                                navArgument("empresaId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val productoId = backStackEntry.arguments?.getLong("productoId") ?: return@composable

                            val empresaId: Long? = backStackEntry.arguments
                                ?.getString("empresaId")
                                ?.toLongOrNull()

                            val usuario = obtenerUsuarioActual()
                            if (usuario != null) {
                                ProductoDetalleScreen(
                                    navController = navController,
                                    productoId = productoId,
                                    usuarioId = usuario.id,
                                    service = SupabaseService(SupabaseProvider.client),
                                    empresaId = empresaId
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

                        composable(
                            route = "pedidosYFacturasEmpresas/{empresaId}",
                            arguments = listOf(navArgument("empresaId") { type = NavType.LongType })
                        ) { entry ->
                            val empresaId = entry.arguments!!.getLong("empresaId")
                            val usuario = obtenerUsuarioActual()
                            usuario?.id?.let { id ->
                                PedidosYFacturasEmpresaScreen(
                                    empresaId = empresaId,
                                    usuarioId = id,
                                    navController = navController,
                                    onLogout = {
                                        lifecycleScope.launch {
                                            SupabaseProvider.client.auth.signOut()
                                            navController.navigate("login") {
                                                popUpTo("homeEmpresaScreen/$empresaId") { inclusive = true }
                                            }
                                        }
                                    }
                                )
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
                                onThemeChange = { cambiarTema(it) },
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

                        composable("sobreNosotrosScreen") {
                            SobreNosotrosScreen(navController = navController,onLogout = {
                                lifecycleScope.launch {
                                    SupabaseProvider.client.auth.signOut()
                                    navController.navigate("login") {
                                        popUpTo("perfilUser") { inclusive = true }
                                    }
                                }
                            })
                        }

                        composable("resetPassword") {
                            ResetPasswordScreen(navController)
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
                val fullUri = uri.toString()
                val fragment = uri.fragment ?: ""

                val params = fragment.split("&").mapNotNull {
                    val parts = it.split("=")
                    if (parts.size == 2) parts[0] to parts[1] else null
                }.toMap()

                val type = params["type"]
                val accessToken = params["access_token"]
                val refreshToken = params["refresh_token"]

                when (type) {
                    "recovery" -> {
                        if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                            SupabaseProvider.client.auth.importAuthToken(
                                accessToken = accessToken,
                                refreshToken = refreshToken
                            )
                            println("🔑 Sesión restaurada desde enlace de recuperación.")
                        } else {
                            println("⚠️ Tokens de recuperación inválidos o ausentes.")
                        }

                        navController.navigate("resetPassword") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }

                    else -> {
                        SupabaseProvider.client.auth.exchangeCodeForSession(fullUri)
                        val perfil = obtenerPerfilUsuarioActual()
                        val destino = if (perfil?.rol == "Administrador") "homeAdminScreen" else if (perfil?.rol == "Empresa") "homeEmpresaScreen/${perfil.empresa_id}" else "homeUserScreen"
                        navController.navigate(destino) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ Error procesando redirección: ${e.message}")
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    }

    /**override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri ->
            navControllerRef?.let { handleAuthRedirectUri(uri, it) }
        }
    }**/
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkUri.value = intent.data
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

private val BotonMinHeight = 42.dp
private val BotonPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)

// Botón reutilizable
@Composable
fun BotonTransparenteNegro(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    texto: String
) {
    val colors = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = BotonMinHeight),
        shape = MaterialTheme.shapes.medium,
        contentPadding = BotonPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.error,
            contentColor = colors.onSecondary
        )
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Botón reutilizable
@Composable
fun BotonAñadir(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    texto: String
) {
    val colors = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = BotonMinHeight),
        shape = MaterialTheme.shapes.medium,
        contentPadding = BotonPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.error,
            contentColor = colors.onSecondary
        )
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BotonRojo(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    texto: String
) {
    val colors = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = BotonMinHeight),
        shape = MaterialTheme.shapes.medium,
        contentPadding = BotonPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkRed,
            contentColor = colors.onSecondary
        )
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BotonFiltro(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    texto: String,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    borderColor: Color = MaterialTheme.colorScheme.onBackground
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = BotonMinHeight),
        shape = MaterialTheme.shapes.medium,
        contentPadding = BotonPadding,
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = texto,
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
