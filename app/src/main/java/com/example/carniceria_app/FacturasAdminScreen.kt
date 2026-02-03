package com.example.carniceria_app

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.*
import kotlinx.coroutines.launch

/**@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturasAdminScreen(navController: NavHostController,onLogout: () -> Unit) {

    var facturas by remember { mutableStateOf<List<Factura>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    val service = remember { SupabaseService(SupabaseProvider.client) }
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    // 🔄 Cargar todas las facturas al abrir
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                facturas = service.obtenerTodasFacturas()
            } catch (e: Exception) {
                Log.e("FacturasAdminScreen", "❌ Error al cargar facturas", e)
            } finally {
                cargando = false
            }
        }
    }

    Scaffold(
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
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                cargando -> CircularProgressIndicator()
                facturas.isEmpty() -> Text("No hay facturas registradas.")
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(facturas) { factura ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Factura #${factura.id}", style = MaterialTheme.typography.titleMedium)
                                    Text("Usuario ID: ${factura.id_usuario}")
                                    Text("Fecha: ${factura.fecha}")
                                    Text("Estado: ${factura.estado}")
                                    Text("Total: ${"%.2f".format(factura.total)} €")

                                    Spacer(Modifier.height(8.dp))

                                    if (!factura.pdf_url.isNullOrEmpty()) {
                                        TextButton(onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(factura.pdf_url))
                                            navController.context.startActivity(intent)
                                        }) {
                                            Text("📄 Ver PDF")
                                        }
                                    } else {
                                        Text(
                                            "⚠️ Sin PDF disponible",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
**/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturasAdminScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    var facturas by remember { mutableStateOf<List<Factura>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    val service = remember { SupabaseService(SupabaseProvider.client) }
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    // 🔄 Cargar todas las facturas al abrir
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                facturas = service.obtenerTodasFacturas()
            } catch (e: Exception) {
                Log.e("FacturasAdminScreen", "❌ Error al cargar facturas", e)
            } finally {
                cargando = false
            }
        }
    }

    Scaffold(
        topBar = {
            UpBarAdmin(
                navController = navController,
                titulo = "Facturas",
                onLogout = onLogout
            )
        }
    ) { padding ->

        if (cargando) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (facturas.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay facturas registradas.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(facturas) { factura ->
                FacturaAdminCard(
                    factura = factura,
                    onVerPDF = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        navController.context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun FacturaAdminCard(
    factura: Factura,
    onVerPDF: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(Modifier.padding(16.dp)) {

            // -------- CABECERA --------
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Factura #${factura.id}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    factura.fecha ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            // -------- DETALLES --------
            Text("Usuario ID: ${factura.id_usuario}")
            Text("Estado: ${factura.estado}")
            Text(
                "Total: ${"%.2f".format(factura.total)} €",
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            // -------- BOTÓN PDF --------
            if (!factura.pdf_url.isNullOrEmpty()) {
                TextButton(
                    onClick = { onVerPDF(factura.pdf_url!!) }
                ) { Text("📄 Ver PDF") }
            } else {
                Text(
                    "⚠️ Factura aún sin PDF",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
