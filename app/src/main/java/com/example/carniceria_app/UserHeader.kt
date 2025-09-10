package com.example.carniceria_app

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class HeaderTab { Perfil, Productos, Facturas }

/**
 * Si `current` es, por ejemplo, HeaderTab.Perfil, el botón "Perfil" se mostrará como "Inicio"
 * y su onClick llamará a `onNavigateHome`.
 */
@Composable
fun UserHeader(
    title: String,
    current: HeaderTab? = null,
    onNavigateHome: () -> Unit,
    onNavigationToPerfil:  () -> Unit,
    onNavigationToProduct: () -> Unit,
    onNavigationToFacture: () -> Unit,
    onAbrirCarrito: () -> Unit,
    mostrarCarrito: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (mostrarCarrito) { // 👈 solo mostramos si es true
                IconButton(onClick = onAbrirCarrito) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ---- Botón Perfil / Inicio ----
            val perfilIsActive = current == HeaderTab.Perfil
            OutlinedButton(
                onClick = if (perfilIsActive) onNavigateHome else onNavigationToPerfil,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (perfilIsActive) "Inicio" else "Perfil")
            }

            // ---- Botón Productos / Inicio ----
            val productosIsActive = current == HeaderTab.Productos
            OutlinedButton(
                onClick = if (productosIsActive) onNavigateHome else onNavigationToProduct,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (productosIsActive) "Inicio" else "Productos")
            }

            // ---- Botón Facturas / Inicio ----
            val facturasIsActive = current == HeaderTab.Facturas
            OutlinedButton(
                onClick = if (facturasIsActive) onNavigateHome else onNavigationToFacture,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (facturasIsActive) "Inicio" else "Facturas")
            }
        }
    }
}
