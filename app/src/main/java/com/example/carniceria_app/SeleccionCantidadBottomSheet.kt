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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carniceria.shared.shared.models.utils.Product
import com.carniceria.shared.shared.models.utils.ProductEmpresa

@Composable
fun SeleccionCantidadBottomSheet(
    producto: Product? = null,
    productoEmpresa: ProductEmpresa? = null,
    onDismiss: () -> Unit,
    onConfirmar: () -> Unit
) {
    // ------------------------------------------------------------
    // 🔒 SEGURIDAD: siempre debe venir un producto válido
    // ------------------------------------------------------------
    if (producto == null && productoEmpresa == null) {
        onDismiss()
        return
    }

    val context = LocalContext.current
    val carritoViewModel: CarritoViewModel = viewModel()

    // ------------------------------------------------------------
    // 📌 Datos comunes extraídos automáticamente
    // ------------------------------------------------------------
    val nombreProducto = producto?.nombre_producto ?: productoEmpresa!!.nombre_producto
    val unidad = producto?.unidad_medida ?: productoEmpresa!!.unidad_medida ?: "Unidad"
    val stock = producto?.stock_producto ?: productoEmpresa!!.stock_producto ?: 0.0

    // ------------------------------------------------------------
    // 🔢 Estados
    // ------------------------------------------------------------
    var cantidadUnidad by remember { mutableStateOf(1) }
    var cantidadKilos by remember { mutableStateOf("0.5") }
    var mensajePreparacion by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ------------------------------------------------------------
        // 📝 Título
        // ------------------------------------------------------------
        Text("Añadir $nombreProducto", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Stock disponible: $stock ${unidad.lowercase()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))

        // ------------------------------------------------------------
        // ⚖️ Cantidad según unidad
        // ------------------------------------------------------------
        if (unidad.equals("Kilo", ignoreCase = true)) {
            OutlinedTextField(
                value = cantidadKilos,
                onValueChange = { nueva ->
                    if (nueva.matches(Regex("^\\d*\\.?\\d*\$")))
                        cantidadKilos = nueva
                },
                label = { Text("Cantidad en Kg (mínimo 0.5)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                BotonRojo(
                    onClick = { if (cantidadUnidad > 1) cantidadUnidad-- },
                    modifier = Modifier.weight(1f),
                    texto = "-"
                )
                Spacer(Modifier.width(12.dp))

                Text("$cantidadUnidad", style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.width(12.dp))
                BotonAñadir(
                    onClick = { cantidadUnidad++ },
                    modifier = Modifier.weight(1f),
                    texto = "+"
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ------------------------------------------------------------
        // 📝 Mensaje opcional para preparar
        // ------------------------------------------------------------
        OutlinedTextField(
            value = mensajePreparacion,
            onValueChange = { mensajePreparacion = it },
            label = { Text("Mensaje para el carnicero (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // ------------------------------------------------------------
        // 🛒 BOTÓN: Añadir al carrito
        // ------------------------------------------------------------
        BotonTransparenteNegro(
            onClick = {
                val cantidadFinal = if (unidad.equals("Kilo", true)) {
                    (cantidadKilos.toDoubleOrNull() ?: 0.5).coerceAtLeast(0.5)
                } else cantidadUnidad.toDouble()

                // ❌ Validaciones
                when {
                    cantidadFinal <= 0 ->
                        Toast.makeText(context, "Cantidad no válida.", Toast.LENGTH_SHORT).show()

                    cantidadFinal > stock ->
                        Toast.makeText(
                            context,
                            "❌ Solo hay $stock ${unidad.lowercase()}(s) disponibles.",
                            Toast.LENGTH_LONG
                        ).show()

                    else -> {
                        // ------------------------------------------------------------
                        // 📌 Selección según tipo producto
                        // ------------------------------------------------------------
                        val añadido = when {
                            productoEmpresa != null -> carritoViewModel.agregarProductoEmpresaAlCarrito(
                                productoEmpresa,
                                cantidadFinal,
                                mensajePreparacion.ifBlank { null }
                            )

                            producto != null -> carritoViewModel.agregarAlCarrito(
                                producto,
                                cantidadFinal,
                                mensajePreparacion.ifBlank { null }
                            )

                            else -> false
                        }

                        if (añadido) {
                            carritoViewModel.guardarCarritoLocal(context)
                            Toast.makeText(
                                context,
                                "✅ $nombreProducto añadido al carrito.",
                                Toast.LENGTH_SHORT
                            ).show()
                            onConfirmar()
                        } else {
                            Toast.makeText(context, "⚠️ No se pudo añadir el producto.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            texto = "Añadir al carrito"
        )
    }
}
