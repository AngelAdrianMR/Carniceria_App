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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.carniceria.shared.shared.models.utils.Product
import com.carniceria.shared.shared.models.utils.actualizarProducto
import com.carniceria.shared.shared.models.utils.obtenerCategoriasProducto
import com.carniceria.shared.shared.models.utils.obtenerUnidadesMedida
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarProductoBottomSheet(
    producto: Product,
    onDismiss: () -> Unit,
    onGuardar: (Product) -> Unit
) {
    var nombre by remember { mutableStateOf(producto.nombre_producto) }
    var descripcion by remember { mutableStateOf(producto.descripcion_producto ?: "") }
    var categoriaSeleccionada by remember { mutableStateOf(producto.categoria_producto) }
    var precioVenta by remember { mutableStateOf(producto.precio_venta.toString()) }
    var precioCompra by remember { mutableStateOf(producto.precio_compra?.toString() ?: "") }
    var imagen by remember { mutableStateOf(producto.imagen_producto ?: "") }
    var unidadSeleccionada by remember { mutableStateOf(producto.unidad_medida ?: "") }
    var stock by remember { mutableStateOf(producto.stock_producto?.toString() ?: "") }

    var listaCategorias by remember { mutableStateOf<List<String>>(emptyList()) }
    var listaUnidades by remember { mutableStateOf<List<String>>(emptyList()) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 🚩 Picker de imagen
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagenUri = uri
            imagen = uri.toString()
        }
    }

    LaunchedEffect(Unit) {
        try {
            listaCategorias = obtenerCategoriasProducto()
            listaUnidades = obtenerUnidadesMedida()
        } catch (e: Exception) {
            Log.e("EditarProducto", "Error cargando enums", e)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Editar Producto", style = MaterialTheme.typography.titleLarge)

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

            SelectorDesplegable(
                label = "Categoría",
                opciones = listaCategorias,
                seleccionada = categoriaSeleccionada,
                onSeleccionar = { categoriaSeleccionada = it }
            )

            OutlinedTextField(
                value = precioVenta,
                onValueChange = { precioVenta = it },
                label = { Text("Precio venta") },
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

            // 🚩 URL o imagen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = imagen,
                    onValueChange = { imagen = it },
                    label = { Text("URL o ruta de imagen") },
                    modifier = Modifier.weight(1f)
                )

                BotonTransparenteNegro(
                    onClick = { launcher.launch("image/*") },
                    texto = "Galería"
                )
            }

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

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ Botones fijos al final (ahora visibles con scroll)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BotonTransparenteNegro(
                    onClick = {
                        scope.launch {
                            try {
                                val imagenFinal = if (imagen.startsWith("content://")) {
                                    subirImagenProducto(context, imagen.toUri()) ?: producto.imagen_producto
                                } else {
                                    imagen.takeIf { it.startsWith("http") } ?: producto.imagen_producto
                                }

                                val productoEditado = producto.copy(
                                    nombre_producto = nombre,
                                    descripcion_producto = descripcion,
                                    categoria_producto = categoriaSeleccionada,
                                    precio_venta = precioVenta.toDoubleOrNull() ?: producto.precio_venta,
                                    precio_compra = precioCompra.toDoubleOrNull(),
                                    imagen_producto = imagenFinal,
                                    unidad_medida = unidadSeleccionada,
                                    stock_producto = stock.toDoubleOrNull()
                                )

                                val ok = actualizarProducto(productoEditado)
                                if (ok) {
                                    onGuardar(productoEditado)
                                    onDismiss()
                                } else {
                                    Log.e("EditarProducto", "❌ Error al actualizar en Supabase")
                                }
                            } catch (e: Exception) {
                                Log.e("EditarProducto", "Error al actualizar", e)
                            }
                        }
                    },
                    texto = "Guardar"
                )

                BotonTransparenteNegro(
                    onClick = onDismiss,
                    texto = "Cancelar"
                )
            }
        }
    }
}
