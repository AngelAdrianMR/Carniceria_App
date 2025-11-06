package com.example.carniceria_app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(navController: NavController) {
    var nuevaContrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    var mostrarContrasena by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔐 Restablecer contraseña",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                // 🧩 Campo: Nueva contraseña
                OutlinedTextField(
                    value = nuevaContrasena,
                    onValueChange = { nuevaContrasena = it },
                    label = { Text("Nueva contraseña") },
                    singleLine = true,
                    visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                            Icon(
                                imageVector = if (mostrarContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (mostrarContrasena) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // 🧩 Campo: Confirmar contraseña
                OutlinedTextField(
                    value = confirmarContrasena,
                    onValueChange = { confirmarContrasena = it },
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    visualTransformation = if (mostrarConfirmacion) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarConfirmacion = !mostrarConfirmacion }) {
                            Icon(
                                imageVector = if (mostrarConfirmacion) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (mostrarConfirmacion) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // ⚠️ Mensaje de error
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // 🔘 Botón principal
                Button(
                    onClick = {
                        when {
                            nuevaContrasena.isBlank() || confirmarContrasena.isBlank() ->
                                error = "Introduce la nueva contraseña en ambos campos."
                            nuevaContrasena != confirmarContrasena ->
                                error = "Las contraseñas no coinciden."
                            !nuevaContrasena.matches(Regex("^(?=.*[0-9])(?=.*[!@#\$%^&*]).{6,}$")) ->
                                error = "Debe tener al menos 6 caracteres, un número y un símbolo."
                            else -> {
                                error = null
                                cargando = true
                                scope.launch {
                                    try {
                                        SupabaseProvider.client.auth.updateUser {
                                            password = nuevaContrasena
                                        }

                                        Toast.makeText(
                                            context,
                                            "Contraseña actualizada correctamente.",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        navController.navigate("login") {
                                            popUpTo("resetPassword") { inclusive = true }
                                        }
                                    } catch (e: Exception) {
                                        error = "Error al actualizar la contraseña."
                                        println("❌ Error al restablecer contraseña: ${e.message}")
                                    } finally {
                                        cargando = false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !cargando
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar nueva contraseña")
                    }
                }
            }
        }
    }
}
