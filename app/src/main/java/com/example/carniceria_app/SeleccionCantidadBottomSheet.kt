package com.example.carniceria_app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    val stockMaximo = producto.stock_producto ?: Int.MAX_VALUE

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = "Añadir ${producto.nombre_producto}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(12.dp))

        // Controles de cantidad
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            BotonTransparenteNegro(
                onClick = { if (cantidad > 1) cantidad-- },
                modifier = Modifier.weight(1f),
                texto = "-"
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = "$cantidad",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.width(12.dp))

            BotonTransparenteNegro(
                onClick = { if (cantidad < stockMaximo) cantidad++ },
                modifier = Modifier.weight(1f),
                texto = "+"
            )
        }

        Spacer(Modifier.height(20.dp))

        // Botón para confirmar
        BotonTransparenteNegro(
            onClick = {
                val anadido = carritoViewModel.agregarAlCarrito(producto, cantidad)
                if (anadido) {
                    carritoViewModel.guardarCarritoLocal(context)
                    onConfirmar()
                } else {
                    Toast.makeText(
                        context,
                        "No hay suficiente stock disponible",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            texto = "Añadir $cantidad al carrito"
        )
    }
}
