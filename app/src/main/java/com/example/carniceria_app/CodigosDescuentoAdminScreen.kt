package com.example.carniceria_app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.CodigoDescuento
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodigosDescuentoAdminScreen(
    navController: NavHostController,
    codigos: List<CodigoDescuento>,
    onToggleActivo: (CodigoDescuento) -> Unit,
    onCrearNuevo: (CodigoDescuento) -> Unit,
    onEliminarCodigo: (CodigoDescuento) -> Unit,
    onLogout: () -> Unit
) {
    var mostrarDialogoNuevo by remember { mutableStateOf(false) }

    // Campos del nuevo código
    var codigo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("porcentaje") }
    var valor by remember { mutableStateOf("") }
    var usoMaximo by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoNuevo = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Código")
            }
        },
        topBar = {
            // 🧱 Usamos aquí la nueva barra admin reutilizable
            UpBarAdmin(
                navController = navController,
                titulo = "Panel de Administración",
                onLogout = onLogout
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (codigos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay códigos de descuento aún.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(codigos) { codigoDescuento ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Código: ${codigoDescuento.codigo}", style = MaterialTheme.typography.titleMedium)
                                Text("Tipo: ${codigoDescuento.tipo}")
                                Text("Valor: ${codigoDescuento.valor}")
                                Text("Inicio: ${codigoDescuento.fecha_inicio}")
                                Text("Fin: ${codigoDescuento.fecha_fin}")
                                Text("Usos: ${codigoDescuento.uso_actual}/${codigoDescuento.uso_maximo}")
                                Text("Activo: ${if (codigoDescuento.activo) "Sí" else "No"}")

                                Spacer(Modifier.height(8.dp))

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 🗑️ Icono para eliminar
                                    IconButton(onClick = { onEliminarCodigo(codigoDescuento) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar código",
                                            tint = MaterialTheme.colorScheme.error // 🔴 Rojo
                                        )
                                    }

                                    Spacer(Modifier.width(8.dp))

                                    // 🔘 Botón de activar/desactivar
                                    Button(
                                        onClick = { onToggleActivo(codigoDescuento) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (codigoDescuento.activo)
                                                MaterialTheme.colorScheme.error
                                            else
                                                MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text(if (codigoDescuento.activo) "Desactivar" else "Activar")
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    // 🆕 Diálogo de creación de código
    if (mostrarDialogoNuevo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoNuevo = false },
            title = { Text("Crear nuevo código de descuento") },
            text = {
                Column {
                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it },
                        label = { Text("Código") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    // Selector tipo
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = tipo == "porcentaje",
                            onClick = { tipo = "porcentaje" }
                        )
                        Text("Porcentaje")
                        Spacer(Modifier.width(16.dp))
                        RadioButton(
                            selected = tipo == "fijo",
                            onClick = { tipo = "fijo" }
                        )
                        Text("Fijo")
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = valor,
                        onValueChange = { valor = it },
                        label = { Text("Valor (${if (tipo == "porcentaje") "%" else "€"})") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = usoMaximo,
                        onValueChange = { usoMaximo = it },
                        label = { Text("Usos máximos") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                        val nuevo = CodigoDescuento(
                            id = "", // se genera en Supabase
                            codigo = codigo.trim(),
                            tipo = tipo,
                            valor = valor.toDoubleOrNull() ?: 0.0,
                            fecha_inicio = now,
                            fecha_fin = now.plus(30, kotlinx.datetime.DateTimeUnit.DAY),
                            uso_maximo = usoMaximo.toIntOrNull() ?: 1,
                            uso_actual = 0,
                            activo = true
                        )
                        onCrearNuevo(nuevo)
                        mostrarDialogoNuevo = false
                        codigo = ""
                        valor = ""
                        usoMaximo = ""
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNuevo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
