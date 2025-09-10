package com.example.carniceria_app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.carniceria.shared.shared.models.utils.*
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email



@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(60.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Registro", style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") })
        OutlinedTextField(
            value = postalCode,
            onValueChange = { postalCode = it },
            label = { Text("Código Postal") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Teléfono") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    error = ""

                    when {
                        !Validator.isValidEmail(email) -> error = "Email no válido"
                        password.length < 6 -> error = "Contraseña demasiado corta"
                        !Validator.isNotEmpty(address) -> error = "Dirección requerida"
                        !Validator.isValidPostalCode(postalCode) -> error = "Código postal inválido"
                        !Validator.isValidPhone(phone) -> error = "Teléfono inválido"
                        else -> {
                            scope.launch {
                                try {
                                    // Registro en Supabase Auth
                                    val result = SupabaseProvider.client.auth.signUpWith(
                                        Email,
                                        redirectUrl = "myapp://auth-callback"
                                    ) {
                                        this.email = email
                                        this.password = password
                                    }

                                    val userId = result?.id

                                    if (!userId.isNullOrBlank()) {
                                        val ok = guardarPerfilUsuario(
                                            userId = userId,
                                            direccion = address,
                                            telefono = phone,
                                            codigo_postal = postalCode,
                                            rol = "Cliente"
                                        )
                                        if (ok) {
                                            onRegisterSuccess()
                                        } else {
                                            error = "Error al guardar perfil"
                                        }
                                    } else {
                                        error = "No se pudo obtener el usuario tras registro"
                                    }

                                    if (error.isNotEmpty()) {
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                    }

                                } catch (e: Exception) {
                                    error = "Excepción: ${e.localizedMessage ?: "Desconocida"}"
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp)
            ) {
                Text("Registrarse")
            }

            Button(
                onClick = {
                    scope.launch {
                        try {
                            SupabaseProvider.client.auth.signInWith(
                                Google,
                                redirectUrl = "myapp://auth-callback"
                            )
                        } catch (e: Exception) {
                            error = "Error en Google Sign-In: ${e.localizedMessage}"
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp)
            ) {
                Text("Google")
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}

