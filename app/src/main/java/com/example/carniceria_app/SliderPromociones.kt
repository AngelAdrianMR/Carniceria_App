package com.example.carniceria_app

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import com.carniceria.shared.shared.models.utils.Product
import com.carniceria.shared.shared.models.utils.PromocionConProductos

@Composable
fun SliderPromociones(
    promociones: List<PromocionConProductos>,
    onAddClick: (Product) -> Unit ,
    onAddPromocion: (PromocionConProductos) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(promociones) { promoConProductos ->
            val promo = promoConProductos.promocion
            println("Promos obtenidas: $promo")

            Card(
                modifier = Modifier
                    .width(250.dp)
                    .wrapContentHeight()
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
                        texto = "Añadir al carrito"
                    )
                }
            }
        }
    }
}
