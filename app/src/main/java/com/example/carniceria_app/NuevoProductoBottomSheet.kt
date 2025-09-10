package com.example.carniceria_app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.carniceria.shared.shared.models.utils.Product
import kotlinx.coroutines.launch
import com.carniceria.shared.shared.models.utils.insertarProducto
import com.carniceria.shared.shared.models.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoProductoBottomSheet(
    onDismiss: () -> Unit,
    onProductoCreado: (Product) -> Unit
) {
    var nombre by remember { mutableStateOf(TextFieldValue("")) }
    var descripcion by remember { mutableStateOf(TextFieldValue("")) }
    var precio by remember { mutableStateOf(TextFieldValue("")) }
    var precioSinIVA by remember { mutableStateOf(TextFieldValue("")) }
    var precioCompra by remember { mutableStateOf(TextFieldValue("")) }
    var imagen by remember { mutableStateOf(TextFieldValue("")) }
    var stock by remember { mutableStateOf(TextFieldValue("")) }
    var categoriaSeleccionada by remember { mutableStateOf("") }
    var unidadSeleccionada by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var listaCategorias by remember { mutableStateOf<List<String>>(emptyList()) }
    var listaUnidades by remember { mutableStateOf<List<String>>(emptyList()) }

    // Cargar opciones de categorías y unidades desde RPC
    LaunchedEffect(Unit) {
        try {
            listaCategorias = obtenerCategoriasProducto()
            listaUnidades = obtenerUnidadesMedida()
        } catch (e: Exception) {
            Log.e("NuevoProductoBottomSheet", "Error cargando enums", e)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("➕ Nuevo Producto", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(12.dp))

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
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val nombreStr = nombre.text.trim()
                        val precioNum = precio.text.toDoubleOrNull()
                        val precioSinIvaNum = precioSinIVA.text.toDoubleOrNull() ?: 0.0
                        val stockNum = stock.text.toIntOrNull()

                        if (nombreStr.isBlank() || precioNum == null || stockNum == null) {
                            error = "Nombre, precio o stock inválido."
                            return@Button
                        }

                        if (categoriaSeleccionada.isBlank() || unidadSeleccionada.isBlank()) {
                            error = "Selecciona una categoría y una unidad."
                            return@Button
                        }

                        val nuevoProducto = Product(
                            nombre_producto = nombreStr,
                            descripcion_producto = descripcion.text.trim(),
                            precio_venta = precioNum,
                            precio_sin_iva = precioSinIvaNum,
                            categoria_producto = categoriaSeleccionada,
                            imagen_producto = imagen.text.trim(),
                            unidad_medida = unidadSeleccionada,
                            stock_producto = stockNum
                        )

                        scope.launch {
                            try {
                                val creado = insertarProducto(nuevoProducto)
                                if (creado != null) {
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
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorDesplegable(
    label: String,
    opciones: List<String>,
    seleccionada: String,
    onSeleccionar: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = seleccionada,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
