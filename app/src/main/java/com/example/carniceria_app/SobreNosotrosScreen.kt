package com.example.carniceria_app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.carniceria_app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SobreNosotrosScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val politicaUrl = "https://sites.google.com/view/carniceriaespinosapp/inicio"
    val contactoEmail = "carniceriacharcuteriaespinosa@gmail.com"

    Scaffold(
        topBar = {
            UserHeader(
                navController = navController,
                titulo = "Nuestra historia",
                onNavigateHome = { navController.navigate("homeUserScreen") },
                onNavigationToPerfil = { navController.navigate("perfilUser") },
                onNavigationToProductos = { navController.navigate("productosUser") },
                onNavigationToPedidos = { navController.navigate("pedidosYFacturas") },
                onNavigationToConfiguracion = { navController.navigate("configuracionScreen") },
                onNavigationToSobreNosotros = { navController.navigate("sobreNosotrosScreen") },
                onLogout = onLogout,
                mostrarCarrito = false,
                mostrarBotonEditar = false,
                onEditarPerfil = { navController.navigate("editarPerfilScreen") },
                onNavigationToFaq = { navController.navigate("faqScreen") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState) // ✅ scroll de toda la pantalla
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val isDarkTheme = isSystemInDarkTheme()
            val logoRes = if (isDarkTheme) R.drawable.logo_white else R.drawable.logo_black

            Image(
                painter = painterResource(id = logoRes),
                contentDescription = "Logo Carnicería",
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Carnicería App",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Versión 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                text = """
                    Somos una aplicación diseñada para acercar los productos de tu carnicería local directamente a tu móvil.
                    Compra con comodidad, revisa tus pedidos, aprovecha nuestras promociones y mantente al tanto de las novedades.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            // ✅ Card único con FAQ + Contacto + Privacidad (3 botones rojos)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Ayuda y contacto",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // 🔴 Botón FAQ (rojo)
                    BotonAñadir(
                        onClick = { navController.navigate("faqScreen") },
                        texto = "❓ Preguntas frecuentes",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider()

                    Text(
                        text = "Contacto",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = contactoEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 🔴 Botón correo (rojo)
                    BotonAñadir(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_SENDTO,
                                Uri.parse("mailto:$contactoEmail")
                            )
                            context.startActivity(intent)
                        },
                        texto = "✉️ Enviar correo",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider()

                    Text(
                        text = "Política de privacidad",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // 🔴 Botón política (rojo)
                    BotonAñadir(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(politicaUrl))
                            context.startActivity(intent)
                        },
                        texto = "🔒 Abrir política de privacidad",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
