package com.example.carniceria_app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carniceria.shared.shared.models.utils.Product
import androidx.compose.ui.text.input.KeyboardType
import com.carniceria.shared.shared.models.utils.actualizarProducto
import com.carniceria.shared.shared.models.utils.obtenerCategoriasProducto
import com.carniceria.shared.shared.models.utils.obtenerUnidadesMedida
import kotlinx.coroutines.launch


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
                .padding(16.dp)
                .fillMaxSize(),
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

            OutlinedTextField(
                value = imagen,
                onValueChange = { imagen = it },
                label = { Text("URL imagen") },
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

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    val productoEditado = producto.copy(
                        nombre_producto = nombre,
                        descripcion_producto = descripcion,
                        categoria_producto = categoriaSeleccionada,
                        precio_venta = precioVenta.toDoubleOrNull() ?: producto.precio_venta,
                        precio_compra = precioCompra.toDoubleOrNull(),
                        imagen_producto = imagen,
                        unidad_medida = unidadSeleccionada,
                        stock_producto = stock.toIntOrNull()
                    )

                    // Guardar en Supabase
                    scope.launch {
                        val ok = actualizarProducto(productoEditado)
                        if (ok) {
                            onGuardar(productoEditado) // actualiza lista local
                            onDismiss()
                        } else {
                            Log.e("EditarProducto", "❌ Error al actualizar en Supabase")
                        }
                    }
                }) {
                    Text("Guardar")
                }


                OutlinedButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    }
}
