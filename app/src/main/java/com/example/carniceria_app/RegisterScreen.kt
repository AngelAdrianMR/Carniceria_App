package com.example.carniceria_app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.imePadding
import com.carniceria.shared.shared.models.utils.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    onGoogleSignInClick: () -> Unit
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
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Registro",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        OutlinedTextField(
            value = nombreCompleto,
            onValueChange = { nombreCompleto = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth()
        )

        // Campos de registro
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // 🏠 Dirección completa
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

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Teléfono") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        // 🔴 Mensaje de error breve
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(8.dp))

        // 🔹 Botones con estilo transparente
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BotonTransparenteNegro(
                onClick = {
                    error = null
                    when {
                        !Validator.isValidEmail(email) -> error = "Email no válido"
                        password.length < 6 -> error = "Contraseña demasiado corta"
                        !Validator.isValidPostalCode(postalCode) -> error = "Código postal inválido"
                        !Validator.isValidPhone(phone) -> error = "Teléfono inválido"
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
                                            error = "No se pudo guardar el perfil."
                                        }
                                    } else {
                                        error = "Error al obtener usuario tras registro."
                                    }
                                } catch (e: Exception) {
                                    val mensaje = when {
                                        e.message?.contains("duplicate", true) == true ->
                                            "El email ya está registrado."
                                        e.message?.contains("network", true) == true ->
                                            "Error de conexión. Inténtalo de nuevo."
                                        else -> "Error al registrar. Revisa los datos."
                                    }
                                    error = mensaje
                                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                                } finally {
                                    cargando = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                texto = if (cargando) "..." else "Registrarse"
            )

            BotonTransparenteNegro(
                onClick = {
                    scope.launch {
                        try {
                            SupabaseProvider.client.auth.signInWith(
                                Google,
                                redirectUrl = "myapp://auth-callback"
                            )
                        } catch (e: Exception) {
                            error = "Error al iniciar con Google"
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                texto = "Google"
            )
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
