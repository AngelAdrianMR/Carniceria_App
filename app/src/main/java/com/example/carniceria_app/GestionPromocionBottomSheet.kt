package com.example.carniceria_app

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.carniceria.shared.shared.models.utils.*
import kotlinx.coroutines.launch

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
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
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
    val context = LocalContext.current

    // 🖼️ Selector de imagen desde galería
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenUri = uri
        imagenUrl = uri?.toString() ?: ""
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                if (promocionInicial == null) "Nueva Promoción" else "Editar Promoción",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotonTransparenteNegro(
                    onClick = { launcher.launch("image/*") },
                    texto = if (imagenUri == null && imagenUrl.isBlank()) "Seleccionar imagen" else "Cambiar imagen",
                    modifier = Modifier.weight(1f)
                )
            }

            if (imagenUrl.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(imagenUrl),
                    contentDescription = "Vista previa",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio Total") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(8.dp))
            Text("Productos incluidos:", style = MaterialTheme.typography.bodyMedium)
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

            BotonTransparenteNegro(
                onClick = {
                    scope.launch {
                        if (nombre.isBlank() || descripcion.isBlank() || precio.isBlank()) {
                            println("⚠️ Faltan campos obligatorios")
                            return@launch
                        }

                        // 🧩 Subir imagen solo si es nueva (URI local)
                        val urlFinal = when {
                            imagenUri != null -> subirImagenPromocion(
                                context,
                                imagenUri!!,
                                "promo_${System.currentTimeMillis()}.jpg"
                            )
                            imagenUrl.startsWith("https://") -> imagenUrl
                            else -> ""
                        }

                        val productosSeleccionados = productos.filter { seleccionados[it.id] == true }

                        if (promocionInicial == null) {
                            // ➕ Crear promoción nueva
                            val nuevaPromo = insertarPromocion(
                                Promocion(
                                    nombre_promocion = nombre,
                                    descripcion_promocion = descripcion,
                                    imagen_promocion = urlFinal,
                                    precio_total = precio.toDoubleOrNull() ?: 0.0,
                                    estado = false
                                )
                            )
                            val ok = nuevaPromo?.let {
                                guardarRelaciones(it.id!!, productosSeleccionados)
                            } ?: false

                            if (ok) {
                                viewModel.crearPromocionYNotificar(nuevaPromo)
                                viewModel.cargarDatos()
                                onDismiss()
                            }
                        } else {
                            // ✏️ Editar existente
                            val promoEditada = Promocion(
                                id = promocionInicial.promocion.id,
                                nombre_promocion = nombre,
                                descripcion_promocion = descripcion,
                                imagen_promocion = urlFinal,
                                precio_total = precio.toDoubleOrNull() ?: 0.0,
                                estado = promocionInicial.promocion.estado
                            )

                            val updated = actualizarPromocion(promoEditada)
                            if (updated) {
                                eliminarRelacionesPromocion(promoEditada.id!!)
                                guardarRelaciones(promoEditada.id!!, productosSeleccionados)
                                viewModel.cargarDatos()
                                onDismiss()
                            }
                        }
                    }
                },
                texto = "Guardar",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Cancelar")
            }
        }
    }
}
