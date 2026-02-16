package com.example.carniceria_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import com.carniceria.shared.shared.models.utils.Product
import com.carniceria.shared.shared.models.utils.Promocion
import com.carniceria.shared.shared.models.utils.PromocionConProductos

@Composable
fun SliderPromociones(
    promociones: List<PromocionConProductos>,
    onAddClick: (Product) -> Unit ,
    onAddPromocion: (PromocionConProductos) -> Unit,
    onPromoClick: (Promocion) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        items(promociones) { promoConProductos ->
            val promo = promoConProductos.promocion
            println("Promos obtenidas: $promo")

            val shadowColor = MaterialTheme.colorScheme.error

            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .width(250.dp)
                    .wrapContentHeight()
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = shadowColor.copy(alpha = 0.9f),
                        spotColor = shadowColor.copy(alpha = 1.4f),
                        clip = false
                    )
                    .clickable { onPromoClick(promo) }
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(promo.imagen_promocion ?: ""),
                        contentDescription = promo.nombre_promocion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(promo.nombre_promocion, style = MaterialTheme.typography.titleMedium)
                    Text(promo.descripcion_promocion ?: "", style = MaterialTheme.typography.bodySmall)
                    Text("Total: ${promo.precio_total} €", style = MaterialTheme.typography.bodyMedium)

                    promoConProductos.productos.forEach { producto ->
                        Text(
                            "- ${producto.nombre_producto}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    BotonTransparenteNegro(
                        onClick = { onAddPromocion(promoConProductos) },
                        texto = "+🛒"
                    )
                }
            }
        }
    }
}
