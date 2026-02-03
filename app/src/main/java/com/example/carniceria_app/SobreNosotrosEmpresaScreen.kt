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
fun SobreNosotrosEmpresaScreen(navController: NavHostController, onLogout: () -> Unit,empresaId: Long) {
    Scaffold(
        topBar = {
            EmpresaHeader(
                navController = navController,
                titulo = "Pedidos Empresa",
                mostrarCarrito = false,
                onLogout = onLogout,

                onNavigateHomeEmpresa = { navController.navigate("homeEmpresaScreen/$empresaId") },

                onNavigateEmpresaProductos = { navController.navigate("productosEmpresaScreen/$empresaId") },

                onNavigateEmpresaPedidos = {
                    navController.navigate("pedidosYFacturasEmpresas/$empresaId")
                },

                onNavigateEmpresaPerfil = { navController.navigate("perfilEmpresaScreen/$empresaId") },

                onNavigateEmpresaConfig = { navController.navigate("configEmpresaScreen/$empresaId") },

                onNavigateEmpresaSobreNosotros = { navController.navigate("sobreNosotrosEmpresaScreen/$empresaId") },
                onNavigationToFaqEmpresa = { navController.navigate("FaqEmpresaScreen/$empresaId")}

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

        }
    }
}
