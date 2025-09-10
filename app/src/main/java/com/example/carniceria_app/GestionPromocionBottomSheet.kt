package com.example.carniceria_app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carniceria.shared.shared.models.utils.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionPromocionBottomSheet(
    productos: List<Product>,
    promocionInicial: PromocionConProductos?,
    onDismiss: () -> Unit,
    viewModel: PromocionesAdminViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf(promocionInicial?.promocion?.nombre_promocion ?: "") }
    var descripcion by remember { mutableStateOf(promocionInicial?.promocion?.descripcion_promocion ?: "") }
    var imagenUrl by remember { mutableStateOf(promocionInicial?.promocion?.imagen_promocion ?: "") }
    var precio by remember { mutableStateOf(promocionInicial?.promocion?.precio_total?.toString() ?: "") }

    val seleccionados = remember {
        mutableStateMapOf<Long, Boolean>().apply {
            promocionInicial?.productos?.forEach { p ->
                p.id?.let { this[it] = true }
            }
        }
    }

    val scope = rememberCoroutineScope()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                if (promocionInicial == null) "Nueva Promoción" else "Editar Promoción",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
            OutlinedTextField(value = imagenUrl, onValueChange = { imagenUrl = it }, label = { Text("URL Imagen") })
            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio Total") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(8.dp))
            Text("Productos:", style = MaterialTheme.typography.bodyMedium)
            LazyColumn(modifier = Modifier.height(200.dp)) {
                items(productos) { producto ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = seleccionados[producto.id] == true,
                            onCheckedChange = { checked ->
                                producto.id?.let { seleccionados[it] = checked }
                            }
                        )
                        Text(producto.nombre_producto)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val productosSeleccionados = productos.filter { seleccionados[it.id] == true }

                    scope.launch {
                        if (promocionInicial == null) {
                            // ➕ Crear nueva promoción (sin id)
                            val nuevaPromo = insertarPromocion(
                                Promocion(
                                    nombre_promocion = nombre,
                                    descripcion_promocion = descripcion,
                                    imagen_promocion = imagenUrl,
                                    precio_total = precio.toDoubleOrNull() ?: 0.0,
                                    estado = false // 👈 siempre empieza desactivada
                                )
                            )
                            val ok = nuevaPromo?.let {
                                guardarRelaciones(it.id!!, productosSeleccionados)
                            } ?: false

                            if (ok) {
                                viewModel.cargarDatos()
                                onDismiss()
                            } else {
                                println("❌ No se pudo guardar la promoción")
                            }

                        } else {
                            // ✏️ Editar existente (sí tiene id)
                            val promo = Promocion(
                                id = promocionInicial.promocion.id,
                                nombre_promocion = nombre,
                                descripcion_promocion = descripcion,
                                imagen_promocion = imagenUrl,
                                precio_total = precio.toDoubleOrNull() ?: 0.0,
                                estado = promocionInicial.promocion.estado
                            )

                            val updated = actualizarPromocion(promo)
                            val ok = if (updated) {
                                val idReal = promo.id ?: return@launch
                                eliminarRelacionesPromocion(idReal)
                                guardarRelaciones(idReal, productosSeleccionados)
                            } else false

                            if (ok) {
                                viewModel.cargarDatos()
                                onDismiss()
                            } else {
                                println("❌ No se pudo guardar la promoción")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }


            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Cancelar")
            }
        }
    }
}
