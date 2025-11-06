package com.example.carniceria_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.carniceria_app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SobreNosotrosScreen(navController: NavHostController, onLogout: () -> Unit,) {
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
                onEditarPerfil = { navController.navigate("editarPerfilScreen") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val isDarkTheme = isSystemInDarkTheme()
            val logoRes = if (isDarkTheme) R.drawable.logo_white else R.drawable.logo_black
            // 🥩 Logo o imagen (usa tu recurso en res/drawable/)
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

            Spacer(Modifier.height(20.dp))

            BotonTransparenteNegro(
                onClick = { navController.popBackStack() },
                texto = "Volver",
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
    }
}
