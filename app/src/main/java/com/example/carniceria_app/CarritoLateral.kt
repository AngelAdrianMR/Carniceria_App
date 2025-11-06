package com.example.carniceria_app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.carniceria.shared.shared.models.utils.CarritoItem
import kotlinx.coroutines.launch

@Composable
fun CarritoLateral(
    carrito: List<CarritoItem>,
    codigoPostalUsuario: String?,
    direccionUsuario: String?,
    usuarioId: String?,
    carritoViewModel: CarritoViewModel,
    onCerrar: () -> Unit,
    onEliminarItem: (CarritoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Closed) {
            onCerrar()
        }
    }

    var codigo by remember { mutableStateOf("") }
    var mensajeCodigo by remember { mutableStateOf<String?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = modifier.zIndex(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Carrito de compra", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (carrito.isEmpty()) {
                        Text("El carrito está vacío.")
                    } else {

                        // 🔹 Cálculo del total base
                        var totalPrecio = carrito.sumOf { item ->
                            when {
                                item.producto != null -> (item.producto!!.precio_venta ?: 0.0) * item.cantidad
                                item.promocion != null -> (item.promocion!!.promocion.precio_total ?: 0.0) * item.cantidad
                                else -> 0.0
                            }
                        }

                        // 🧾 Mostrar cada línea del carrito
                        carrito.forEach { item ->
                            val precioUnitario = item.producto?.precio_venta
                                ?: item.promocion?.promocion?.precio_total ?: 0.0
                            val cantidad = item.cantidad
                            val subTotal = precioUnitario * cantidad

                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when {
                                            item.producto != null -> "- ${item.producto!!.nombre_producto} x $cantidad"
                                            item.promocion != null -> "⭐ Promo: ${item.promocion!!.promocion.nombre_promocion} x $cantidad"
                                            else -> "- Item desconocido"
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(onClick = {
                                        carritoViewModel.eliminarProducto(item, context)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Text(
                                    "${"%.2f".format(precioUnitario)} € x $cantidad = ${"%.2f".format(subTotal)} €",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                item.mensaje?.let { msg ->
                                    Text(
                                        "📝 Nota: $msg",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp, start = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // 🔹 Campo para código descuento
                        OutlinedTextField(
                            value = codigo,
                            onValueChange = { codigo = it },
                            label = { Text("Código descuento") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // 🖤 Botón aplicar código
                        BotonTransparenteNegro(
                            onClick = {
                                scope.launch {
                                    val ok = carritoViewModel.aplicarCodigo(codigo)
                                    mensajeCodigo = if (ok) "Código aplicado ✅" else "Código no válido ❌"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            texto = "Aplicar"
                        )

                        mensajeCodigo?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                it,
                                color = if (it.contains("✅"))
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 🔹 Aplicar descuento si lo hay
                        if (carritoViewModel.descuentoAplicado > 0) {
                            Text("Descuento: -${"%.2f".format(carritoViewModel.descuentoAplicado)} €")
                            totalPrecio -= carritoViewModel.descuentoAplicado
                        }

                        // ===================================================
                        // 🚚 VALIDACIÓN DE ENVÍO
                        // ===================================================
                        val (envioValido, suplemento, mensajeEnvio) =
                            validarEnvio(codigoPostalUsuario, totalPrecio)

                        if (suplemento > 0) {
                            Text("Suplemento envío: +${"%.2f".format(suplemento)} €")
                            totalPrecio += suplemento
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Total: ${"%.2f".format(totalPrecio)} €",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            mensajeEnvio,
                            color = if (envioValido)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 🚚 BOTÓN “PEDIR A DOMICILIO”
                        BotonTransparenteNegro(
                            onClick = {
                                if (envioValido) {
                                    scope.launch {
                                        carritoViewModel.confirmarEnvio(usuarioId, context)
                                        onCerrar()
                                    }
                                } else {
                                    Toast.makeText(context, mensajeEnvio, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { alpha = if (envioValido) 1f else 0.5f },
                            texto = "Pedir a domicilio"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 🏪 Recoger en tienda (siempre disponible)
                        BotonTransparenteNegro(
                            onClick = {
                                scope.launch {
                                    carritoViewModel.confirmarRecogidaEnTienda(usuarioId, context)
                                    onCerrar()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            texto = "Recoger en tienda"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // ❌ Cerrar
                        BotonTransparenteNegro(
                            onClick = { scope.launch { drawerState.close() } },
                            modifier = Modifier.fillMaxWidth(),
                            texto = "Cerrar"
                        )
                    }
                }
            }
        },
        content = {}
    )
}

/**
 * ✅ Valida si el envío está permitido según zona, precio y suplemento.
 */
fun validarEnvio(codigoPostal: String?, total: Double): Triple<Boolean, Double, String> {
    val zonasPermitidas = listOf("04740", "04720", "04738", "04700", "04710") // Roquetas, La Mojonera, Urbanización, Aguadulce, Vícar

    val esZonaValida = codigoPostal != null && zonasPermitidas.any { codigoPostal.contains(it) }

    return when {
        !esZonaValida -> Triple(false, 0.0, "❌ Fuera de la zona de reparto (solo Roquetas, La Mojonera, Urbanización, Aguadulce y Vícar)")
        total < 30.0 -> Triple(false, 0.0, "⚠️ El pedido mínimo para envío a domicilio es de 30 €")
        total in 30.0..49.99 -> Triple(true, 4.50, "🚚 Envío disponible con suplemento de 4,50 €")
        else -> Triple(true, 0.0, "✅ Envío gratuito disponible")
    }
}
