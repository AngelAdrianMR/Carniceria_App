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

@Composable
fun SeleccionCantidadBottomSheet(
    producto: Product,
    onDismiss: () -> Unit,
    onConfirmar: () -> Unit
) {
    var cantidadUnidad by remember { mutableStateOf(1) }

    // 🔹 Cambiamos el valor inicial mínimo a 0.5 kg
    var cantidadKilos by remember { mutableStateOf("0.5") }

    var mensajePreparacion by remember { mutableStateOf("") }

    val carritoViewModel: CarritoViewModel = viewModel()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Añadir ${producto.nombre_producto}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        // 🔹 Stock disponible
        Text(
            text = "Stock disponible: ${producto.stock_producto ?: 0} ${producto.unidad_medida?.lowercase() ?: ""}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(12.dp))

        // 👇 Cantidad
        if (producto.unidad_medida.equals("Kilo", ignoreCase = true)) {
            OutlinedTextField(
                value = cantidadKilos,
                onValueChange = { nueva ->
                    if (nueva.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        cantidadKilos = nueva
                    }
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
                BotonTransparenteNegro(
                    onClick = { if (cantidadUnidad > 1) cantidadUnidad-- },
                    modifier = Modifier.weight(1f),
                    texto = "-"
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "$cantidadUnidad",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.width(12.dp))
                BotonTransparenteNegro(
                    onClick = { cantidadUnidad++ },
                    modifier = Modifier.weight(1f),
                    texto = "+"
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = mensajePreparacion,
            onValueChange = { mensajePreparacion = it },
            label = { Text("Mensaje para el carnicero (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        BotonTransparenteNegro(
            onClick = {
                val cantidadFinal = if (producto.unidad_medida.equals("Kilo", true)) {
                    // 🔹 Mínimo 0.5 kg (coerceAtLeast asegura el valor)
                    (cantidadKilos.toDoubleOrNull() ?: 0.5).coerceAtLeast(0.5)
                } else {
                    cantidadUnidad.toDouble()
                }

                val anadido = carritoViewModel.agregarAlCarrito(
                    producto,
                    cantidadFinal,
                    mensajePreparacion.ifBlank { null }
                )

                if (anadido) {
                    carritoViewModel.guardarCarritoLocal(context)
                    onConfirmar()
                } else {
                    Toast.makeText(
                        context,
                        "El pedido mínimo para productos por kilo es de 0.5 kg",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            texto = "Añadir al carrito"
        )
    }
}
