package com.example.carniceria_app

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.carniceria.shared.shared.models.utils.Product
import com.carniceria.shared.shared.models.utils.insertarProducto
import com.carniceria.shared.shared.models.utils.obtenerCategoriasProducto
import com.carniceria.shared.shared.models.utils.obtenerUnidadesMedida
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri


// ===================================================
// 🧾 FORMULARIO: CREAR NUEVO PRODUCTO
// ===================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoProductoBottomSheet(
    onDismiss: () -> Unit,
    onProductoCreado: (Product) -> Unit
) {
    // ---------------------------------------------------
    // 🔹 Estados locales
    // ---------------------------------------------------
    var nombre by remember { mutableStateOf(TextFieldValue("")) }
    var descripcion by remember { mutableStateOf(TextFieldValue("")) }
    var precio by remember { mutableStateOf(TextFieldValue("")) }
    var precioSinIVA by remember { mutableStateOf(TextFieldValue("")) }
    var precioCompra by remember { mutableStateOf(TextFieldValue("")) }
    var imagen by remember { mutableStateOf(TextFieldValue("")) }
    var stock by remember { mutableStateOf(TextFieldValue("")) }
    var categoriaSeleccionada by remember { mutableStateOf("") }
    var unidadSeleccionada by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var listaCategorias by remember { mutableStateOf<List<String>>(emptyList()) }
    var listaUnidades by remember { mutableStateOf<List<String>>(emptyList()) }
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    // ---------------------------------------------------
    // 🖼️ Selección de imagen desde galería
    // ---------------------------------------------------
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imagen = TextFieldValue(it.toString()) }
    }

    // ---------------------------------------------------
    // ⚙️ Cargar opciones desde Supabase (RPC)
    // ---------------------------------------------------
    LaunchedEffect(Unit) {
        try {
            listaCategorias = obtenerCategoriasProducto()
            listaUnidades = obtenerUnidadesMedida()
        } catch (e: Exception) {
            Log.e("NuevoProductoBottomSheet", "Error cargando enums", e)
        }
    }

    // ---------------------------------------------------
    // 🧱 Contenedor principal
    // ---------------------------------------------------
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(scrollState)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("➕ Nuevo Producto", style = MaterialTheme.typography.titleLarge)

            // ---------------------------------------------------
            // 🧩 Campos del formulario
            // ---------------------------------------------------
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precioSinIVA,
                onValueChange = { precioSinIVA = it },
                label = { Text("Precio sin IVA") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precioCompra,
                onValueChange = { precioCompra = it },
                label = { Text("Precio compra") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            SelectorDesplegable(
                label = "Categoría",
                opciones = listaCategorias,
                seleccionada = categoriaSeleccionada,
                onSeleccionar = { categoriaSeleccionada = it }
            )

            OutlinedTextField(
                value = imagen,
                onValueChange = { imagen = it },
                label = { Text("URL Imagen") },
                modifier = Modifier.fillMaxWidth()
            )

            BotonTransparenteNegro(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                texto = "Seleccionar imagen de la galería"
            )

            SelectorDesplegable(
                label = "Unidad de medida",
                opciones = listaUnidades,
                seleccionada = unidadSeleccionada,
                onSeleccionar = { unidadSeleccionada = it }
            )

            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Stock") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---------------------------------------------------
            // 🎛️ Botones de acción
            // ---------------------------------------------------
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonTransparenteNegro(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    texto = "Cancelar"
                )

                BotonTransparenteNegro(
                    onClick = {
                        val nombreStr = nombre.text.trim()
                        val precioNum = precio.text.toDoubleOrNull()
                        val precio_compra = precioCompra.text.toDoubleOrNull()
                        val precioSinIvaNum = precioSinIVA.text.toDoubleOrNull() ?: 0.0
                        val stockNum = stock.text.toDoubleOrNull()

                        if (nombreStr.isBlank() || precioNum == null || stockNum == null) {
                            error = "Nombre, precio o stock inválido."
                            return@BotonTransparenteNegro
                        }

                        if (categoriaSeleccionada.isBlank() || unidadSeleccionada.isBlank()) {
                            error = "Selecciona una categoría y una unidad."
                            return@BotonTransparenteNegro
                        }

                        scope.launch {
                            try {
                                val imagenFinal = if (imagen.text.startsWith("content://")) {
                                    subirImagenProducto(context, imagen.text.toUri()) ?: ""
                                } else {
                                    imagen.text.takeIf { it.startsWith("http") } ?: ""
                                }

                                val nuevoProducto = Product(
                                    nombre_producto = nombreStr,
                                    descripcion_producto = descripcion.text.trim(),
                                    precio_venta = precioNum,
                                    precio_compra = precio_compra,
                                    precio_sin_iva = precioSinIvaNum,
                                    categoria_producto = categoriaSeleccionada,
                                    imagen_producto = imagenFinal, // ✅ URL pública final
                                    unidad_medida = unidadSeleccionada,
                                    stock_producto = stockNum
                                )

                                val creado = insertarProducto(nuevoProducto)
                                if (creado != null) {
                                    notificarClientes(
                                        titulo = "🆕 Nuevo producto disponible",
                                        cuerpo = "${creado.nombre_producto} por solo ${String.format("%.2f", creado.precio_venta)} € 🛒"
                                    )
                                    onProductoCreado(creado)
                                    onDismiss()
                                } else {
                                    error = "Error al guardar producto."
                                }
                            } catch (e: Exception) {
                                Log.e("NuevoProducto", "Error al insertar", e)
                                error = "Error: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    texto = "Guardar"
                )

            }
        }
    }
}

// ===================================================
// ⬇️ SELECTOR DESPLEGABLE REUTILIZABLE
// ===================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDesplegable(
    label: String,
    opciones: List<String>,
    seleccionada: String,
    onSeleccionar: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = seleccionada,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccionar(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}
