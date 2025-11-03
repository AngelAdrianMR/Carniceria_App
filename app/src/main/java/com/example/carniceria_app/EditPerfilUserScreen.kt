package com.example.carniceria_app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.carniceria.shared.shared.models.utils.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPerfilUserScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    var perfil by remember { mutableStateOf<PerfilConEmail?>(null) }
    var cargando by remember { mutableStateOf(true) }

    var calle by remember { mutableStateOf("") }
    var piso by remember { mutableStateOf("") }
    var localidad by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }
    var pais by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<String?>(null) }
    var nombreCompleto by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                imagenUri = it.toString()
                ImagenPerfilManager.guardarImagenUri(context, it.toString())
            }
        }
    )

    // 🔹 Cargar perfil del usuario
    LaunchedEffect(Unit) {
        cargando = true
        val perfilCargado = obtenerPerfilCompleto()
        perfil = perfilCargado

        email = perfilCargado?.email.orEmpty()
        calle = perfilCargado?.calle.orEmpty()
        piso = perfilCargado?.piso.orEmpty()
        localidad = perfilCargado?.localidad.orEmpty()
        provincia = perfilCargado?.provincia.orEmpty()
        pais = perfilCargado?.pais.orEmpty()
        telefono = perfilCargado?.telefono.orEmpty()
        codigoPostal = perfilCargado?.codigoPostal.orEmpty()
        imagenUri = ImagenPerfilManager.cargarImagenUri(context)
        nombreCompleto = perfilCargado?.nombre_completo.orEmpty()

        cargando = false
    }

    if (cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔹 Imagen de perfil
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

            Spacer(Modifier.height(6.dp))

            BotonTransparenteNegro(
                onClick = { launcher.launch("image/*") },
                texto = "Cambiar imagen",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(12.dp))

            // 🔹 Campos del perfil
            val anchoCampos = Modifier
                .fillMaxWidth(0.9f)

            OutlinedTextField(
                value = nombreCompleto,
                onValueChange = { nombreCompleto = it },
                label = { Text("Nombre completo") },
                modifier = anchoCampos
            )

            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Correo electrónico") },
                enabled = false,
                modifier = anchoCampos
            )

            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = calle, onValueChange = { calle = it }, label = { Text("Calle y número") }, modifier = anchoCampos)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = piso, onValueChange = { piso = it }, label = { Text("Piso / Puerta") }, modifier = anchoCampos)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = localidad, onValueChange = { localidad = it }, label = { Text("Localidad") }, modifier = anchoCampos)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = provincia, onValueChange = { provincia = it }, label = { Text("Provincia") }, modifier = anchoCampos)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = pais, onValueChange = { pais = it }, label = { Text("País") }, modifier = anchoCampos)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = anchoCampos)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = codigoPostal, onValueChange = { codigoPostal = it }, label = { Text("Código postal") }, modifier = anchoCampos)

            Spacer(Modifier.height(16.dp))

            // 🔹 Botón guardar cambios
            Button(
                onClick = {
                    scope.launch {
                        val userInfo = obtenerUsuarioActual()
                        if (userInfo != null) {
                            val actualizado = guardarPerfilUsuario(
                                userId = userInfo.id,
                                nombre_completo = nombreCompleto,
                                calle = calle,
                                piso = piso,
                                localidad = localidad,
                                provincia = provincia,
                                pais = pais,
                                telefono = telefono,
                                codigo_postal = codigoPostal,
                                rol = "Cliente"
                            )

                            val mensaje = if (actualizado)
                                "✅ Perfil actualizado correctamente"
                            else
                                "❌ Error al actualizar el perfil"

                            snackbarHostState.showSnackbar(mensaje)

                            if (actualizado) {
                                // 🔹 Enviar señal de actualización a la pantalla anterior
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("perfilActualizado", true)
                                navController.popBackStack()
                            }
                        } else {
                            snackbarHostState.showSnackbar("⚠️ No se pudo obtener el usuario actual")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Guardar cambios")
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
