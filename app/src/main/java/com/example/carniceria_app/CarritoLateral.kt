package com.example.carniceria_app

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.carniceria.shared.shared.models.utils.CarritoItem
import com.carniceria.shared.shared.models.utils.DireccionEnvioExtra
import com.carniceria.shared.shared.models.utils.PerfilConEmail
import com.carniceria.shared.shared.models.utils.obtenerPerfilCompleto
import kotlinx.coroutines.launch
import com.example.carniceria_app.CheckoutResult
import com.example.carniceria_app.FakePaymentGateway
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
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
    val scrollState = rememberScrollState()

    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Closed) onCerrar()
    }

    // -----------------------------
    // Perfil / Direcciones
    // -----------------------------
    var perfil by remember { mutableStateOf<PerfilConEmail?>(null) }
    LaunchedEffect(Unit) {
        runCatching { obtenerPerfilCompleto() }
            .onSuccess { perfil = it }
            .onFailure { perfil = null }
    }

    data class AddressOption(
        val key: String,
        val titulo: String,
        val direccion: String,
        val codigoPostal: String?
    )

    fun formatDireccion(
        calle: String?,
        piso: String?,
        localidad: String?,
        provincia: String?,
        pais: String?
    ): String {
        return listOfNotNull(
            calle?.takeIf { it.isNotBlank() },
            piso?.takeIf { it.isNotBlank() },
            localidad?.takeIf { it.isNotBlank() },
            provincia?.takeIf { it.isNotBlank() },
            pais?.takeIf { it.isNotBlank() }
        ).joinToString(", ").ifBlank { "-" }
    }

    val opcionesDireccion: List<AddressOption> = remember(perfil, codigoPostalUsuario, direccionUsuario) {
        val result = mutableListOf<AddressOption>()

        // Dirección principal (prioriza perfil si existe)
        val principalDir = if (perfil != null) {
            formatDireccion(perfil!!.calle, perfil!!.piso, perfil!!.localidad, perfil!!.provincia, perfil!!.pais)
        } else {
            direccionUsuario?.takeIf { it.isNotBlank() } ?: "-"
        }

        val principalCp = perfil?.codigoPostal?.takeIf { it.isNotBlank() } ?: codigoPostalUsuario

        result.add(
            AddressOption(
                key = "principal",
                titulo = "Dirección principal",
                direccion = principalDir,
                codigoPostal = principalCp
            )
        )

        // Direcciones extra
        val extras: List<DireccionEnvioExtra> = perfil?.direccionesEnvio ?: emptyList()
        extras.forEach { dir ->
            result.add(
                AddressOption(
                    key = "extra_${dir.id}",
                    titulo = dir.alias?.takeIf { it.isNotBlank() } ?: "Dirección adicional",
                    direccion = formatDireccion(dir.calle, dir.piso, dir.localidad, dir.provincia, dir.pais),
                    codigoPostal = dir.codigoPostal
                )
            )
        }

        result
    }

    var expanded by remember { mutableStateOf(false) }
    var selectedKey by remember { mutableStateOf("principal") }

    val direccionSeleccionada: AddressOption? = opcionesDireccion.firstOrNull { it.key == selectedKey }
        ?: opcionesDireccion.firstOrNull()

    // Sincroniza la selección con el ViewModel (para usarla al confirmar)
    LaunchedEffect(direccionSeleccionada) {
        carritoViewModel.codigoPostalSeleccionado = direccionSeleccionada?.codigoPostal
        carritoViewModel.direccionSeleccionadaTexto = direccionSeleccionada?.direccion
        carritoViewModel.tituloDireccionSeleccionada = direccionSeleccionada?.titulo
    }

    val paymentGateway = remember {
        FakePaymentGateway(mode = FakePaymentGateway.Mode.ALWAYS_SUCCESS) // luego puedes poner RANDOM
    }
    var pagandoEnvio by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showFailDialog by remember { mutableStateOf(false) }
    var lastPedidoId by remember { mutableStateOf<Long?>(null) }
    var failMessage by remember { mutableStateOf("No se pudo realizar el pago.") }

    // Código descuento
    var codigo by remember { mutableStateOf("") }
    var mensajeCodigo by remember { mutableStateOf<String?>(null) }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* no-op o permitir cerrar */ },
            title = { Text("Pedido realizado ✅") },
            text = {
                Text(
                    buildString {
                        append("Tu pedido se ha realizado correctamente.")
                        lastPedidoId?.let { append("\n\nNúmero de pedido: #$it") }
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        scope.launch { drawerState.close() } // esto disparará onCerrar()
                    }
                ) { Text("Aceptar") }
            }
        )
    }

    if (showFailDialog) {
        AlertDialog(
            onDismissRequest = { showFailDialog = false },
            title = { Text("Pago no realizado ❌") },
            text = { Text(failMessage) },
            confirmButton = {
                TextButton(onClick = { showFailDialog = false }) { Text("Entendido") }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = modifier.zIndex(1f),
                drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text("Carrito de compra", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (carrito.isEmpty()) {
                        Text("El carrito está vacío.")
                        Spacer(modifier = Modifier.height(16.dp))

                        BotonRojo(
                            onClick = { scope.launch { drawerState.close() } },
                            modifier = Modifier.fillMaxWidth(),
                            texto = "Cerrar"
                        )
                        return@Column
                    }

                    // -----------------------------------------
                    // Caja estilo “info contacto”
                    // -----------------------------------------
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {

                            // -----------------------------
                            // Dirección (selector)
                            // -----------------------------
                            Text(
                                text = "Dirección de envío",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    readOnly = true,
                                    value = direccionSeleccionada?.titulo ?: "Seleccionar dirección",
                                    onValueChange = {},
                                    label = { Text("Seleccionar") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    opcionesDireccion.forEach { opt ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(opt.titulo, fontWeight = FontWeight.SemiBold)
                                                    Text(
                                                        opt.direccion,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                                    )
                                                    Text(
                                                        "CP: ${opt.codigoPostal ?: "-"}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedKey = opt.key
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = direccionSeleccionada?.direccion ?: "-",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "CP: ${direccionSeleccionada?.codigoPostal ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            // -----------------------------
                            // Líneas del carrito
                            // -----------------------------
                            Text(
                                text = "Resumen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Total base
                            var totalPrecio = carrito.sumOf { item ->
                                when {
                                    item.producto != null ->
                                        (item.producto!!.precio_venta ?: 0.0) * item.cantidad
                                    item.promocion != null ->
                                        (item.promocion!!.promocion.precio_total ?: 0.0) * item.cantidad
                                    else -> 0.0
                                }
                            }

                            carrito.forEach { item ->
                                val precioUnitario = item.producto?.precio_venta
                                    ?: item.promocion?.promocion?.precio_total
                                    ?: 0.0

                                val cantidad = item.cantidad
                                val subTotal = precioUnitario * cantidad

                                Column(modifier = Modifier.padding(bottom = 10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when {
                                                item.promocion != null ->
                                                    "Promo: ${item.promocion!!.promocion.nombre_promocion} x $cantidad"
                                                item.producto != null ->
                                                    "${item.producto!!.nombre_producto} x $cantidad"
                                                else -> "Item"
                                            },
                                            modifier = Modifier.weight(1f)
                                        )

                                        IconButton(onClick = {
                                            // Mantengo tu comportamiento actual
                                            carritoViewModel.eliminarProducto(item, context)
                                            // si quieres usar el callback externo, lo dejamos también:
                                            onEliminarItem(item)
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
                                        if (msg.isNotBlank()) {
                                            Text(
                                                "Nota: $msg",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(top = 2.dp, start = 8.dp),
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            // -----------------------------
                            // Código descuento
                            // -----------------------------
                            OutlinedTextField(
                                value = codigo,
                                onValueChange = { codigo = it },
                                label = { Text("Código descuento") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            BotonTransparenteNegro(
                                onClick = {
                                    scope.launch {
                                        val ok = carritoViewModel.aplicarCodigo(codigo)
                                        mensajeCodigo = if (ok) "Código aplicado" else "Código no válido"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                texto = "Aplicar"
                            )

                            mensajeCodigo?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    it,
                                    color = if (it == "Código aplicado")
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // -----------------------------
                            // Descuento / Envío / Total
                            // -----------------------------
                            if (carritoViewModel.descuentoAplicado > 0) {
                                Text("Descuento: -${"%.2f".format(carritoViewModel.descuentoAplicado)} €")
                                totalPrecio -= carritoViewModel.descuentoAplicado
                            }

                            val cpParaEnvio = direccionSeleccionada?.codigoPostal ?: codigoPostalUsuario
                            val (envioValido, suplemento, mensajeEnvio) = validarEnvio(cpParaEnvio, totalPrecio)

                            if (suplemento > 0) {
                                Text("Suplemento envío: +${"%.2f".format(suplemento)} €")
                                totalPrecio += suplemento
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Total: ${"%.2f".format(totalPrecio)} €",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                mensajeEnvio,
                                color = if (envioValido)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botones
                    val cpParaEnvio = direccionSeleccionada?.codigoPostal ?: codigoPostalUsuario
                    val totalBase = carrito.sumOf { item ->
                        when {
                            item.producto != null ->
                                (item.producto!!.precio_venta ?: 0.0) * item.cantidad
                            item.promocion != null ->
                                (item.promocion!!.promocion.precio_total ?: 0.0) * item.cantidad
                            else -> 0.0
                        }
                    }

                    var totalConDescuento = totalBase
                    if (carritoViewModel.descuentoAplicado > 0) totalConDescuento -= carritoViewModel.descuentoAplicado

                    val (envioValido, _, mensajeEnvio) = validarEnvio(cpParaEnvio, totalConDescuento)

                    BotonAñadir(
                        onClick = {
                            if (!envioValido) {
                                Toast.makeText(context, mensajeEnvio, Toast.LENGTH_SHORT).show()
                                return@BotonAñadir
                            }

                            if (pagandoEnvio) return@BotonAñadir

                            scope.launch {
                                try {
                                    pagandoEnvio = true

                                    // Recalcular total FINAL coherente con lo mostrado:
                                    val totalBase = carrito.sumOf { item ->
                                        when {
                                            item.producto != null ->
                                                (item.producto!!.precio_venta ?: 0.0) * item.cantidad
                                            item.promocion != null ->
                                                (item.promocion!!.promocion.precio_total ?: 0.0) * item.cantidad
                                            else -> 0.0
                                        }
                                    }

                                    var totalFinal = totalBase
                                    if (carritoViewModel.descuentoAplicado > 0) {
                                        totalFinal -= carritoViewModel.descuentoAplicado
                                    }

                                    val cp = direccionSeleccionada?.codigoPostal ?: codigoPostalUsuario
                                    val (envioOk2, suplemento, mensaje2) = validarEnvio(cp, totalFinal)

                                    if (!envioOk2) {
                                        Toast.makeText(context, mensaje2, Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }

                                    totalFinal += suplemento

                                    val amountCents = (totalFinal * 100).roundToLong().coerceAtLeast(0L)
                                    val orderIdFake = "temp_${System.currentTimeMillis()}"

                                    when (val result = paymentGateway.startCheckout(
                                        orderId = orderIdFake,
                                        amountCents = amountCents,
                                        currency = "EUR"
                                    )) {
                                        is CheckoutResult.Success -> {
                                            val pedidoId = carritoViewModel.confirmarEnvio(usuarioId, context)

                                            if (pedidoId != null) {
                                                lastPedidoId = pedidoId
                                                showSuccessDialog = true
                                            } else {
                                                failMessage = "El pago se completó (simulado), pero no se pudo confirmar el pedido. Inténtalo de nuevo."
                                                showFailDialog = true
                                            }
                                        }

                                        is CheckoutResult.Canceled -> {
                                            failMessage = result.reason ?: "Pago cancelado."
                                            showFailDialog = true
                                        }

                                        is CheckoutResult.Failure -> {
                                            failMessage = result.message.ifBlank { "No se pudo realizar el pago." }
                                            showFailDialog = true
                                        }

                                    }
                                } finally {
                                    pagandoEnvio = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { alpha = if (envioValido && !pagandoEnvio) 1f else 0.5f },
                        texto = if (pagandoEnvio) "Procesando pago..." else "Pedir a domicilio"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BotonAñadir(
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

                    BotonRojo(
                        onClick = { scope.launch { drawerState.close() } },
                        modifier = Modifier.fillMaxWidth(),
                        texto = "Cerrar"
                    )
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
        !esZonaValida -> Triple(false, 0.0, "Fuera de la zona de reparto (solo Roquetas, La Mojonera, Urbanización, Aguadulce y Vícar)")
        total < 30.0 -> Triple(false, 0.0, "El pedido mínimo para envío a domicilio es de 30 €")
        total in 30.0..49.99 -> Triple(true, 4.50, "Envío disponible con suplemento de 4,50 €")
        else -> Triple(true, 0.0, "Envío gratuito disponible")
    }
}
