package com.example.carniceria_app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carniceria.shared.shared.models.utils.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var nombreCompleto by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var calle by remember { mutableStateOf("") }
    var piso by remember { mutableStateOf("") }
    var localidad by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }
    var pais by remember { mutableStateOf("") }

    var cargando by remember { mutableStateOf(false) }
    var mostrarPassword by remember { mutableStateOf(false) }
    var errorGlobal by remember { mutableStateOf<String?>(null) }
    var isPasswordFocused by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "📝 Registro de usuario",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center
                )

                // Nombre completo
                OutlinedTextField(
                    value = nombreCompleto,
                    onValueChange = { nombreCompleto = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                if (email.isNotEmpty() && !Validator.isValidEmail(email)) {
                    Text(
                        "Formato de email no válido",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Contraseña
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isPasswordFocused = focusState.isFocused
                        }
                )

                // 🧾 Requisitos visibles solo al enfocar el campo
                if (isPasswordFocused) {
                    Text(
                        text = "Debe tener al menos 6 caracteres, un número y un símbolo (ej: @, #, $).",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                } else if (password.isNotEmpty() && password.length < 6) {
                    Text(
                        "La contraseña es demasiado corta",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Dirección completa
                OutlinedTextField(value = calle, onValueChange = { calle = it }, label = { Text("Calle y número") })
                OutlinedTextField(value = piso, onValueChange = { piso = it }, label = { Text("Piso / Puerta") })
                OutlinedTextField(value = localidad, onValueChange = { localidad = it }, label = { Text("Localidad") })
                OutlinedTextField(value = provincia, onValueChange = { provincia = it }, label = { Text("Provincia") })
                OutlinedTextField(value = pais, onValueChange = { pais = it }, label = { Text("País") })

                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    label = { Text("Código Postal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (postalCode.isNotEmpty() && !Validator.isValidPostalCode(postalCode)) {
                    Text(
                        "Código postal inválido",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                if (phone.isNotEmpty() && !Validator.isValidPhone(phone)) {
                    Text(
                        "Número de teléfono inválido",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // 🔴 Error global
                errorGlobal?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 🔘 Botón principal
                Button(
                    onClick = {
                        errorGlobal = null
                        when {
                            !Validator.isValidEmail(email) -> errorGlobal = "Email no válido"
                            password.length < 6 -> errorGlobal = "Contraseña demasiado corta"
                            !Validator.isValidPostalCode(postalCode) -> errorGlobal = "Código postal inválido"
                            !Validator.isValidPhone(phone) -> errorGlobal = "Teléfono inválido"
                            else -> {
                                scope.launch {
                                    cargando = true
                                    try {
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
                                                nombre_completo = nombreCompleto,
                                                calle = calle,
                                                piso = piso,
                                                localidad = localidad,
                                                provincia = provincia,
                                                pais = pais,
                                                telefono = phone,
                                                codigo_postal = postalCode,
                                                rol = "Cliente"
                                            )
                                            if (ok) {
                                                Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                                onRegisterSuccess()
                                            } else {
                                                errorGlobal = "No se pudo guardar el perfil."
                                            }
                                        } else {
                                            errorGlobal = "Error al crear usuario."
                                        }
                                    } catch (e: Exception) {
                                        errorGlobal = when {
                                            e.message?.contains("duplicate", true) == true -> "El email ya está registrado."
                                            e.message?.contains("network", true) == true -> "Error de conexión."
                                            else -> "Error al registrar. Intenta nuevamente."
                                        }
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
                        Text("Registrarse")
                    }
                }

                TextButton(
                    onClick = onBackToLogin,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("¿Ya tienes cuenta? Inicia sesión")
                }
            }
        }
    }
}
