package com.example.carniceria_app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import com.carniceria.shared.shared.models.utils.obtenerPerfilCompleto
import com.carniceria.shared.shared.models.utils.guardarPerfilUsuario
import com.carniceria.shared.shared.models.utils.obtenerUsuarioActual
import com.carniceria.shared.shared.models.utils.PerfilConEmail

import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import io.github.jan.supabase.auth.auth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUserScreen(navController: NavHostController, onLogout: () -> Unit) {
    var perfil by remember { mutableStateOf<PerfilConEmail?>(null) }
    var cargando by remember { mutableStateOf(true) }

    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                imagenUri = it.toString()
                ImagenPerfilManager.guardarImagenUri(context, it.toString())
            }
        }
    )
    val snackbarHostState = remember { SnackbarHostState() }

    var mostrarCarritoLateral by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cargando = true
        val perfilCargado = obtenerPerfilCompleto()
        perfil = perfilCargado
        direccion = perfilCargado?.direccion.orEmpty()
        telefono = perfilCargado?.telefono.orEmpty()
        codigoPostal = perfilCargado?.codigoPostal.orEmpty()
        email = perfilCargado?.email.orEmpty()
        imagenUri = ImagenPerfilManager.cargarImagenUri(context)
        cargando = false
    }

    Column{
        UserHeader(
            title = "Perfil",
            current = HeaderTab.Perfil,
            onNavigateHome = {
                navController.navigate("homeUserScreen") {
                    popUpTo("homeUserScreen") { inclusive = false }
                    launchSingleTop = true
                }
            },
            onNavigationToPerfil = { /* ya estás en perfil */ },
            onNavigationToProduct = { navController.navigate("productosUser") },
            onNavigationToFacture = { navController.navigate("facturas") },
            onAbrirCarrito = { mostrarCarritoLateral = false },
            mostrarCarrito = false
        )


        Spacer(modifier = Modifier.height(16.dp))

        imagenUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Imagen de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        BotonTransparenteNegro(
            onClick = {
                launcher.launch("image/*")
            },
            texto = "Cambiar imagen",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            enabled = false,
            modifier = Modifier.align(Alignment.CenterHorizontally).width(260.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            modifier = Modifier.align(Alignment.CenterHorizontally).width(260.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Dirección") },
            modifier = Modifier.align(Alignment.CenterHorizontally).width(260.dp),
            singleLine = false
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = codigoPostal,
            onValueChange = { codigoPostal = it },
            label = { Text("Código postal") },
            modifier = Modifier.align(Alignment.CenterHorizontally).width(260.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        BotonTransparenteNegro(
            onClick = {
                scope.launch {
                    val userInfo = obtenerUsuarioActual()
                    if (userInfo != null) {

                        println("userInfo.id = ${userInfo.id}")

                        val actualizado = guardarPerfilUsuario(
                            userId = userInfo.id,
                            direccion = direccion,
                            telefono = telefono,
                            codigo_postal = codigoPostal
                        )

                        println("guardarPerfilUsuario = $actualizado")

                        if (actualizado) {
                            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
                        } else {
                            snackbarHostState.showSnackbar("Error al actualizar perfil o email")
                        }
                    } else {
                        snackbarHostState.showSnackbar("No se pudo obtener el usuario")
                    }
                }
            },
            texto = "Guardar cambios",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))


        BotonTransparenteNegro(
            onClick = {
                scope.launch {
                    SupabaseProvider.client.auth.signOut()
                    onLogout()
                }
            },
            texto = "Cerrar sesión",
            modifier = Modifier.fillMaxWidth()
        )

    }
}


