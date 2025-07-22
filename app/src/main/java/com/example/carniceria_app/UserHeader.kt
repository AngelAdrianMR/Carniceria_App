package com.example.carniceria_app

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign

@Composable
fun UserHeader(
    title: String,
    onNavigationToPerfil: () -> Unit,
    onNavigationToProduct: () -> Unit,
    onNavigationToFacture: () -> Unit,
    onAbrirCarrito: () -> Unit
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

           // 👉 Este botón ahora llama a la función que recibe desde fuera
           IconButton(onClick = { onAbrirCarrito() }) {
               Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
           }
       }


        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onNavigationToPerfil,
                modifier = Modifier.weight(1f)
            ) {
                Text("Perfil")
            }

            OutlinedButton(
                onClick = onNavigationToProduct,
                modifier = Modifier.weight(1f)
            ) {
                Text("Productos")
            }

            OutlinedButton(
                onClick = onNavigationToFacture,
                modifier = Modifier.weight(1f)
            ) {
                Text("Facturas")
            }
        }
    }
}

