package com.example.carniceria_app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch
import com.example.carniceria_app.registrarTokenFCM
import io.github.jan.supabase.auth.providers.Google
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 🖤 Botón "Entrar"
        BotonTransparenteNegro(
            onClick = {
                errorMessage = ""
                loading = true
                scope.launch {
                    try {
                        SupabaseProvider.client.auth.signInWith(Email) {
                            this.email = email
                            this.password = password
                        }

                        val session = SupabaseProvider.client.auth.currentSessionOrNull()
                        val idUsuario = session?.user?.id

                        if (idUsuario != null) {
                            registrarTokenFCM(idUsuario)
                            println("✅ Token FCM guardado para usuario: $idUsuario")
                        } else {
                            println("⚠️ No se encontró ID de usuario tras el login")
                        }

                        authViewModel.cargarUsuario()
                        onLoginSuccess()

                    } catch (e: RestException) {
                        errorMessage = when (e.statusCode) {
                            400, 401 -> "Usuario o contraseña incorrectos"
                            else -> "Error interno. Contacta con soporte."
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error de conexión. Inténtalo más tarde."
                    } finally {
                        loading = false
                    }
                }
            },
            texto = if (loading) "Cargando..." else "Entrar",
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes cuenta? Regístrate")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                if (email.isNotEmpty()) {
                    scope.launch {
                        try {
                            SupabaseProvider.client.auth.resetPasswordForEmail(
                                email,
                                redirectUrl = "myapp://auth-callback"
                            )
                            errorMessage = "📧 Se ha enviado un enlace para restablecer tu contraseña."
                        } catch (e: Exception) {
                            errorMessage = "Error al enviar el correo. Inténtalo más tarde."
                        }
                    }
                } else {
                    errorMessage = "Introduce tu email primero"
                }
            }
        ) {
            Text("He olvidado mi contraseña")
        }
    }
}
