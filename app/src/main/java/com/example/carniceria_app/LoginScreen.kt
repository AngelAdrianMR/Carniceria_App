package com.example.carniceria_app

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import com.carniceria.shared.shared.models.utils.obtenerPerfilUsuarioActual
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var mostrarPassword by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val rememberMe by authViewModel.rememberMe.collectAsState()
    val savedEmail by authViewModel.savedEmail.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.loadLastEmail()
        if (email.isBlank()) email = savedEmail
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "🔐 Iniciar sesión",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        authViewModel.saveLastEmail(it)
                    },
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                            Icon(
                                imageVector = if (mostrarPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (mostrarPassword) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { authViewModel.setRememberMe(it) }
                    )
                    Text("Recuérdame", style = MaterialTheme.typography.bodyMedium)
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                BotonTransparenteNegro(
                    onClick = {
                        errorMessage = ""
                        loading = true

                        scope.launch {
                            try {
                                authViewModel.saveLastEmail(email)

                                // 1) Login Auth
                                SupabaseProvider.client.auth.signInWith(Email) {
                                    this.email = email
                                    this.password = password
                                }

                                // 2) Traer perfil y cortar si está bloqueado
                                val perfil = obtenerPerfilUsuarioActual()
                                val estaBloqueado = perfil?.bloqueado == true

                                if (estaBloqueado) {
                                    SupabaseProvider.client.auth.signOut()
                                    authViewModel.setRememberMe(false)

                                    errorMessage = "Tu cuenta está bloqueada. Contacta con soporte."
                                    return@launch
                                }

                                // 3) Normal
                                val session = SupabaseProvider.client.auth.currentSessionOrNull()
                                val idUsuario = session?.user?.id

                                if (idUsuario != null) {
                                    registrarTokenFCM(idUsuario)
                                    println("✅ Token FCM guardado para usuario: $idUsuario")
                                }

                                authViewModel.saveSession()
                                authViewModel.cargarUsuario()

                                Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()

                            } catch (e: RestException) {
                                errorMessage = when (e.statusCode) {
                                    400, 401 -> "Usuario o contraseña incorrectos"
                                    else -> "Error interno. Contacta con soporte."
                                }
                            } catch (_: Exception) {
                                errorMessage = "Error de conexión. Inténtalo más tarde."
                            } finally {
                                loading = false
                            }
                        }
                    },
                    texto = if (loading) "Cargando..." else "Entrar",
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                )

                TextButton(onClick = onNavigateToRegister) {
                    Text("¿No tienes cuenta? Regístrate")
                }

                TextButton(
                    onClick = {
                        if (email.isNotEmpty()) {
                            scope.launch {
                                try {
                                    SupabaseProvider.client.auth.resetPasswordForEmail(
                                        email,
                                        redirectUrl = "myapp://auth-callback"
                                    )
                                    errorMessage =
                                        "Asegurate de desmarcar la casilla \"Recuerdame\". Busca \"Supabase Auth\" en tu correo (o spam) y abre el enlace."
                                } catch (_: Exception) {
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

                Spacer(modifier = Modifier.height(12.dp))

                val isDarkTheme = isSystemInDarkTheme()
                val logoRes = if (isDarkTheme) R.drawable.logo_white else R.drawable.logo_black

                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Logo Carnicería",
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
