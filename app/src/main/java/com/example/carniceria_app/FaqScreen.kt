package com.example.carniceria_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.carniceria.shared.shared.models.utils.*

@Composable
fun FaqScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    // Lista de preguntas frecuentes
    val faqs = listOf(
        FaqItem(
            pregunta = "1. ¿Cómo me registro en la app?",
            respuesta = "Puedes registrarte desde la pantalla de \"Registro\" indicando tu correo electrónico, " +
                    "contraseña y tus datos básicos (nombre, dirección, código postal, teléfono, etc.). " +
                    "Una vez creado el usuario, se guarda tu perfil y podrás iniciar sesión con ese correo."
        ),
        FaqItem(
            pregunta = "2. ¿Puedo mantener la sesión iniciada? (\"Recuérdame\")",
            respuesta = "Sí. En la pantalla de inicio de sesión tienes la casilla \"Recuérdame\". " +
                    "Si la marcas, la app intenta mantener tu sesión activa para que no tengas que volver a introducir " +
                    "las credenciales cada vez que abras la aplicación."
        ),
        FaqItem(
            pregunta = "3. ¿Qué tipos de usuario existen?",
            respuesta = "Actualmente la app distingue principalmente entre tres perfiles: Cliente, Empresa y Administrador. " +
                    "Según tu rol en la base de datos (tabla perfil_usuario), la app te mostrará la pantalla de inicio correspondiente."
        ),
        FaqItem(
            pregunta = "4. ¿Cómo funcionan las promociones destacadas?",
            respuesta = "En la pantalla de inicio verás un carrusel con \"Promociones destacadas\". " +
                    "Cada promoción puede incluir varios productos. Puedes añadir una promoción completa al carrito con un solo botón " +
                    "o abrirla para ver más detalles."
        ),
        FaqItem(
            pregunta = "5. ¿Qué son los productos destacados?",
            respuesta = "Los productos destacados son aquellos marcados internamente con el campo \"destacado\". " +
                    "Se muestran en secciones especiales como \"Nuestros Productos Destacados\" tanto para clientes como para empresas."
        ),
        FaqItem(
            pregunta = "6. ¿Cómo uso los filtros de productos?",
            respuesta = "En las pantallas de productos verás un botón \"Filtros\". " +
                    "Al pulsarlo se abre un panel lateral donde puedes elegir una categoría. " +
                    "También puedes pulsar \"Limpiar filtros\" para volver a ver todos los productos."
        ),
        FaqItem(
            pregunta = "7. ¿Cómo funciona el carrito de compra?",
            respuesta = "Puedes añadir productos o promociones al carrito desde las distintas pantallas. " +
                    "El carrito se abre desde el icono de la parte superior. " +
                    "Desde ahí puedes modificar cantidades, eliminar elementos, aplicar códigos de descuento " +
                    "y elegir entre envío a domicilio o recogida en tienda (según disponibilidad)."
        ),
        FaqItem(
            pregunta = "8. ¿Cuáles son las condiciones del envío a domicilio?",
            respuesta = "El envío a domicilio depende de la zona (código postal) y del importe mínimo del pedido. " +
                    "Si el pedido no llega al mínimo o la dirección está fuera de la zona de reparto, " +
                    "la app te mostrará un mensaje explicando el motivo y desactivará el botón de envío."
        ),
        FaqItem(
            pregunta = "9. ¿Cuál es la diferencia entre vista Cliente y vista Empresa?",
            respuesta = "El cliente ve precios estándar y promociones generales. " +
                    "Las empresas pueden ver productos con precios personalizados para su empresa y pantallas específicas " +
                    "como \"Inicio Empresa\" o \"Productos Empresa\"."
        ),
        FaqItem(
            pregunta = "10. ¿Dónde puedo ver mis pedidos y facturas?",
            respuesta = "Desde el menú superior tienes acceso a la sección de \"Pedidos y Facturas\". " +
                    "Ahí puedes consultar los pedidos realizados y sus facturas asociadas. " +
                    "En el caso de empresas, existe una sección específica para pedidos y facturas de empresa."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // 🔹 Cabecera reutilizando tu UserHeader
        UserHeader(
            navController = navController,
            titulo = "Preguntas frecuentes",
            onLogout = onLogout,
            mostrarCarrito = false,
            onAbrirCarrito = null,
            mostrarBotonEditar = false,
            onEditarPerfil = null,
            onNavigateHome = { navController.navigate("homeUserScreen") },
            onNavigationToPerfil = { navController.navigate("perfilUser") },
            onNavigationToProductos = { navController.navigate("productosUser") },
            onNavigationToPedidos = { navController.navigate("pedidosYFacturas") },
            onNavigationToConfiguracion = { navController.navigate("configuracionScreen") },
            onNavigationToSobreNosotros = { navController.navigate("sobreNosotrosScreen") },
            onNavigationToFaq = { navController.navigate("faqScreen") }
        )

        // Contenido FAQ
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Text(
                    text = "Resolvemos las dudas más habituales sobre el uso de la aplicación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onBackground,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(faqs) { faq ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = faq.pregunta,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = faq.respuesta,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

data class FaqItem(
    val pregunta: String,
    val respuesta: String
)
