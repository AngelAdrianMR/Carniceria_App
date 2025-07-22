package com.example.carniceria_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch
import com.carniceria.shared.shared.models.utils.obtenerPerfilUsuarioActual

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(this)

        // Configurar Google Sign-In (aunque no se esté usando ahora)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            var isLoggedIn by remember { mutableStateOf(false) }
            var isAdmin by remember { mutableStateOf(false) }
            var perfilCargado by remember { mutableStateOf(false) }
            val navController = rememberNavController()

            LaunchedEffect(Unit) {
                val token = tokenManager.getAccessToken()
                if (token != null) {
                    isLoggedIn = true
                }
            }

            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    val token = tokenManager.getAccessToken()
                    if (token != null) {
                        val perfil = obtenerPerfilUsuarioActual(token)
                        println("ROL DETECTADO: ${perfil?.rol}")
                        isAdmin = perfil?.rol == "Administrador"
                        perfilCargado = true
                    }
                }
            }

            // 3. Mostrar la pantalla adecuada
            CarniceriaAppTheme(darkTheme = false) {
                when {
                    isLoggedIn && perfilCargado && isAdmin -> {
                        HomeAdminScreen(
                            navController = navController,
                            onLogout = {
                                lifecycleScope.launch {
                                    tokenManager.clearAccessToken()
                                    isLoggedIn = false
                                    isAdmin = false
                                    perfilCargado = false
                                }
                            }
                        )
                    }
                    isLoggedIn && perfilCargado && !isAdmin -> {
                        HomeUserScreen(
                            navController = navController,
                            onLogout = {
                                lifecycleScope.launch {
                                    tokenManager.clearAccessToken()
                                    isLoggedIn = false
                                    isAdmin = false
                                    perfilCargado = false
                                }
                            }
                        )
                    }
                    else -> {
                        LoginScreen(
                            onLoginSuccess = {
                                isLoggedIn = true
                                // El rol se detectará luego
                            },
                            onNavigateToRegister = {
                                navController.navigate("register")
                            }
                        )
                    }
                }
            }
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account?.email
            val nombre = account?.displayName
            println("LOGIN OK: $nombre <$email>")
        } catch (e: ApiException) {
            println("ERROR al iniciar sesión con Google: ${e.statusCode}")
        }
    }
}
