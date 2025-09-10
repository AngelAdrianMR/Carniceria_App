package com.example.carniceria_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.PerfilUsuario
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import com.carniceria.shared.shared.models.utils.obtenerPerfilUsuarioActual
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.postgrest.postgrest


class MainActivity : ComponentActivity() {

    private var navControllerRef: NavHostController? = null

    // GOOGLE OAUTH
    private fun startGoogleOAuth() {
        lifecycleScope.launch {
            SupabaseProvider.client.auth.signInWith(Google, redirectUrl = "myapp://auth-callback")
        }
    }


    // SIGN UP (envía correo de verificación con el deep link)
    fun signUpWithEmail(email: String, password: String) {
        lifecycleScope.launch {
            try {
                SupabaseProvider.client.auth.signUpWith(
                    Email,
                    redirectUrl = "myapp://auth-callback"
                ) {
                    this.email = email
                    this.password = password
                }
                // TODO: mostrar "Revisa tu correo para confirmar tu cuenta"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // SIGN IN directo (usuario ya verificado)
    fun signInWithEmail(email: String, password: String, navController: NavHostController) {
        lifecycleScope.launch {
            try {
                SupabaseProvider.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                navigateAfterLogin(navController)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun navigateAfterLogin(navController: NavHostController) {
        lifecycleScope.launch {
            val perfil = obtenerPerfilUsuarioActual()
            val dest =
                if (perfil?.rol == "Administrador") "homeAdminScreen" else "homeUserScreen"
            navController.navigate(dest) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            navControllerRef = navController

            // Si la app se abre por deep link (arranque en frío)
            LaunchedEffect(Unit) {
                intent?.data?.let { uri -> handleAuthRedirectUri(uri, navController) }
            }

            var startDestination by rememberSaveable { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                println("🚀 LaunchedEffect ejecutado")
                val session = SupabaseProvider.client.auth.currentSessionOrNull()
                if (session != null) {
                    val perfil = obtenerPerfilUsuarioActual()
                    println("👤 Rol detectado: ${perfil?.rol}")
                    startDestination = when (perfil?.rol) {
                        "Administrador" -> "homeAdminScreen"
                        "Cliente" -> "homeUserScreen"
                        else -> "login"
                    }
                } else {
                    startDestination = "login"
                }
            }

            if (startDestination != null) {
                NavHost(navController = navController, startDestination = startDestination!!) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                lifecycleScope.launch {
                                    val perfil = obtenerPerfilUsuarioActual()
                                    val dest =
                                        if (perfil?.rol == "Administrador") "homeAdminScreen" else "homeUserScreen"
                                    navController.navigate(dest) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate("register")
                            }
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

                    composable("productosAdmin") { ProductosAdminScreen(navController) }
                    composable("productosUser") { ProductosUserScreen(navController) }
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
                        PromocionesAdminScreen(viewModel = viewModel, navController = navController)
                    }

                }
            } else {
                LoadingScreen()
            }
        }
    }

    private fun handleAuthRedirectUri(uri: Uri, navController: NavHostController) {
        lifecycleScope.launch {
            try {
                // Completa la sesión desde el deep link
                SupabaseProvider.client.auth.exchangeCodeForSession(uri.toString())

                val perfil = obtenerPerfilUsuarioActual()
                val destino = when (uri.getQueryParameter("type")) {
                    "recovery" -> "reset_password" // usa una ruta que exista
                    else -> if (perfil?.rol == "Administrador") {
                        "homeAdminScreen"
                    } else {
                        "homeUserScreen"
                    }
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.data?.let { uri -> navControllerRef?.let { handleAuthRedirectUri(uri, it) } }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun BotonTransparenteNegro(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    texto: String
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Black
        ),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Text(texto)
    }
}
