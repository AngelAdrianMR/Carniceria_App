package com.example.carniceria_app

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carniceria.shared.shared.models.utils.Product

@Composable
fun SeleccionCantidadBottomSheet(
    producto: Product,
    onDismiss: () -> Unit,
    onConfirmar: () -> Unit
) {
    var cantidad by remember { mutableStateOf(1) }
    val carritoViewModel: CarritoViewModel = viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(Modifier.padding(16.dp)) {
        Text("Añadir ${producto.nombre_producto}", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { if (cantidad > 1) cantidad-- }) { Text("-") }
            Spacer(Modifier.width(8.dp))
            Text("$cantidad")
            Spacer(Modifier.width(8.dp))
            Button(onClick = { cantidad++ }) { Text("+") }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val anadido = carritoViewModel.agregarAlCarrito(producto, cantidad)
                if (anadido) {
                    onConfirmar()
                } else {
                    Toast.makeText(context, "No hay suficiente stock", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Añadir $cantidad al carrito")
        }
    }
}
