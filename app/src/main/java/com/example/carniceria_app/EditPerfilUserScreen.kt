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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.clickable
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.font.FontWeight
import java.util.UUID


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

    val direccionesExtra = remember { mutableStateListOf<DireccionEnvioExtra>() }
    var mostrarSheetDireccion by remember { mutableStateOf(false) }
    var direccionEditandoId by remember { mutableStateOf<String?>(null) }

    // Campos del formulario de dirección extra
    var dAlias by remember { mutableStateOf("") }
    var dCalle by remember { mutableStateOf("") }
    var dPiso by remember { mutableStateOf("") }
    var dLocalidad by remember { mutableStateOf("") }
    var dProvincia by remember { mutableStateOf("") }
    var dPais by remember { mutableStateOf("España") }
    var dCodigoPostal by remember { mutableStateOf("") }
    var dTelefono by remember { mutableStateOf("") }
    var dInstrucciones by remember { mutableStateOf("") }

    fun abrirNuevaDireccion() {
        direccionEditandoId = null
        dAlias = ""
        dCalle = ""
        dPiso = ""
        dLocalidad = ""
        dProvincia = ""
        dPais = "España"
        dCodigoPostal = ""
        dTelefono = ""
        dInstrucciones = ""
        mostrarSheetDireccion = true
    }

    fun abrirEdicionDireccion(dir: DireccionEnvioExtra) {
        direccionEditandoId = dir.id
        dAlias = dir.alias.orEmpty()
        dCalle = dir.calle
        dPiso = dir.piso.orEmpty()
        dLocalidad = dir.localidad
        dProvincia = dir.provincia
        dPais = dir.pais
        dCodigoPostal = dir.codigoPostal
        dTelefono = dir.telefono.orEmpty()
        dInstrucciones = dir.instrucciones.orEmpty()
        mostrarSheetDireccion = true
    }


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
        direccionesExtra.clear()
        perfilCargado?.direccionesEnvio?.let { direccionesExtra.addAll(it) }

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

            Spacer(Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(14.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Direcciones de envío",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { abrirNuevaDireccion() }) {
                            Icon(Icons.Default.Add, contentDescription = "Nueva dirección")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Principal (campos actuales)
                    Text("Principal", fontWeight = FontWeight.Bold)
                    Text(
                        text = listOfNotNull(
                            calle.takeIf { it.isNotBlank() },
                            piso.takeIf { it.isNotBlank() },
                            localidad.takeIf { it.isNotBlank() },
                            provincia.takeIf { it.isNotBlank() },
                            pais.takeIf { it.isNotBlank() }
                        ).joinToString(", ").ifEmpty { "-" }
                    )
                    if (codigoPostal.isNotBlank()) Text("CP: $codigoPostal", style = MaterialTheme.typography.bodySmall)

                    Spacer(Modifier.height(10.dp))
                    Divider()
                    Spacer(Modifier.height(10.dp))

                    Text("Otras direcciones", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))

                    if (direccionesExtra.isEmpty()) {
                        Text(
                            text = "No tienes direcciones adicionales.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    } else {
                        direccionesExtra.forEach { dir ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { abrirEdicionDireccion(dir) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dir.alias?.takeIf { it.isNotBlank() } ?: "Dirección", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = listOfNotNull(
                                            dir.calle,
                                            dir.piso?.takeIf { it.isNotBlank() },
                                            dir.localidad,
                                            dir.provincia,
                                            dir.pais
                                        ).joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                    Text("CP: ${dir.codigoPostal}", style = MaterialTheme.typography.bodySmall)
                                }

                                IconButton(
                                    onClick = {
                                        direccionesExtra.removeAll { it.id == dir.id }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                            Divider()
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

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
                                rol = "Cliente",
                                direcciones_envio = direccionesExtra.toList()
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

        @OptIn(ExperimentalMaterial3Api::class)
        if (mostrarSheetDireccion) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { mostrarSheetDireccion = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (direccionEditandoId == null) "Nueva dirección" else "Editar dirección",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { mostrarSheetDireccion = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(dAlias, { dAlias = it }, label = { Text("Alias (Casa, Trabajo...)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(dCalle, { dCalle = it }, label = { Text("Calle y número") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(dPiso, { dPiso = it }, label = { Text("Piso / Puerta") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(dLocalidad, { dLocalidad = it }, label = { Text("Localidad") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(dProvincia, { dProvincia = it }, label = { Text("Provincia") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(dPais, { dPais = it }, label = { Text("País") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(dCodigoPostal, { dCodigoPostal = it }, label = { Text("Código postal") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(dTelefono, { dTelefono = it }, label = { Text("Teléfono (opcional)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(dInstrucciones, { dInstrucciones = it }, label = { Text("Instrucciones (opcional)") }, modifier = Modifier.fillMaxWidth())

                    Spacer(Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { mostrarSheetDireccion = false },
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancelar") }

                        Button(
                            onClick = {
                                if (dCalle.isBlank() || dLocalidad.isBlank() || dProvincia.isBlank() || dPais.isBlank() || dCodigoPostal.isBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar("⚠️ Rellena Calle, Localidad, Provincia, País y CP") }
                                    return@Button
                                }

                                val idFinal = direccionEditandoId ?: UUID.randomUUID().toString()
                                val nueva = DireccionEnvioExtra(
                                    id = idFinal,
                                    alias = dAlias.takeIf { it.isNotBlank() },
                                    calle = dCalle.trim(),
                                    piso = dPiso.takeIf { it.isNotBlank() },
                                    localidad = dLocalidad.trim(),
                                    provincia = dProvincia.trim(),
                                    pais = dPais.trim(),
                                    codigoPostal = dCodigoPostal.trim(),
                                    telefono = dTelefono.takeIf { it.isNotBlank() },
                                    instrucciones = dInstrucciones.takeIf { it.isNotBlank() }
                                )

                                val idx = direccionesExtra.indexOfFirst { it.id == idFinal }
                                if (idx >= 0) direccionesExtra[idx] = nueva else direccionesExtra.add(nueva)

                                mostrarSheetDireccion = false
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Guardar") }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

