package com.example.carniceria_app

import android.util.Log
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
                                    val res = registrarUsuario(email, password)

                                    val token = res.access_token

                                    if (token != null) {
                                        val user = obtenerUsuarioActual(token)
                                        if (user != null) {
                                            val ok = guardarPerfilUsuario(
                                                userId = user.id,
                                                accessToken = token,
                                                direccion = address,
                                                telefono = phone,
                                                codigo_postal = postalCode
                                            )
                                            if (ok) {
                                                onRegisterSuccess()
                                            } else {
                                                error = "Error al guardar perfil"
                                            }
                                        } else {
                                            error = "No se pudo obtener el usuario"
                                        }
                                    } else {
                                        error = "Error al registrar: ${res.error ?: "Desconocido"}"
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
                onClick = onGoogleSignInClick,
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
