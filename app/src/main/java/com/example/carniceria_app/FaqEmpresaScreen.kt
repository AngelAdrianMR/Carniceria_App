package com.example.carniceria_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import com.carniceria.shared.shared.models.utils.*

@Composable
fun FaqEmpresaScreen(
    navController: NavHostController,
    empresaId: Long,
    onLogout: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val carritoViewModel: CarritoViewModel = viewModel()
    val context = LocalContext.current

    var mostrarCarritoLateral by remember { mutableStateOf(false) }

    // Lista de preguntas frecuentes orientadas a EMPRESAS
    val faqs = listOf(
        FaqItem(
            pregunta = "1. ¿Cómo accede mi empresa a la app?",
            respuesta = "Tu usuario de empresa se crea desde la propia carnicería. " +
                    "Una vez dado de alta, recibirás un correo con los datos de acceso y podrás iniciar sesión " +
                    "en la sección de empresas para ver tus productos y precios personalizados."
        ),
        FaqItem(
            pregunta = "2. ¿Puedo mantener la sesión iniciada como empresa?",
            respuesta = "Sí. Igual que los clientes, puedes usar la opción \"Recuérdame\" al iniciar sesión. " +
                    "Mientras el token siga siendo válido, la app te llevará directamente a tu inicio de empresa " +
                    "sin pedir de nuevo usuario y contraseña."
        ),
        FaqItem(
            pregunta = "3. ¿Qué diferencia hay entre un usuario Cliente y un usuario Empresa?",
            respuesta = "El cliente ve precios estándar y promociones generales. " +
                    "Las empresas ven pantallas específicas (Inicio Empresa, Productos Empresa, Pedidos de empresa) " +
                    "y pueden disponer de precios personalizados por producto."
        ),
        FaqItem(
            pregunta = "4. ¿Cómo se calculan los precios de empresa?",
            respuesta = "Para cada empresa existe una configuración de precios en la tabla interna de `empresa_productos`. " +
                    "En la app se muestra el campo \"precio_final\" que ya incorpora el precio base más los ajustes " +
                    "negociados para tu empresa."
        ),
        FaqItem(
            pregunta = "5. ¿Puedo seguir viendo promociones como empresa?",
            respuesta = "Sí. Como empresa puedes ver productos destacados y, si existen, promociones aplicables a empresas. " +
                    "No obstante, algunas promociones pueden estar limitadas solo a clientes finales."
        ),
        FaqItem(
            pregunta = "6. ¿Cómo filtro los productos de empresa?",
            respuesta = "En las pantallas de productos de empresa verás un botón \"Filtros\". " +
                    "Al pulsarlo se abre un panel lateral con las categorías disponibles. " +
                    "Puedes elegir una categoría concreta o limpiar los filtros para ver todo el catálogo."
        ),
        FaqItem(
            pregunta = "7. ¿Cómo funciona el carrito para empresas?",
            respuesta = "El funcionamiento es muy similar al de cliente: puedes añadir productos, ajustar cantidades " +
                    "y confirmar el pedido desde el carrito. La diferencia principal es que el pedido se registra " +
                    "como pedido de empresa, usando tus datos fiscales y condiciones especiales."
        ),
        FaqItem(
            pregunta = "8. ¿Hay envío a domicilio para empresas?",
            respuesta = "En la lógica actual, los pedidos de empresa no usan el cálculo de envío a domicilio estándar. " +
                    "Las condiciones de entrega (reparto, recogida, horarios) se acuerdan con la carnicería y se aplican " +
                    "según lo pactado."
        ),
        FaqItem(
            pregunta = "9. ¿Dónde puedo consultar mis pedidos y facturas de empresa?",
            respuesta = "Desde el menú de empresa tienes la sección \"Pedidos y Facturas Empresas\". " +
                    "Ahí podrás consultar el histórico de pedidos realizados por tu empresa y las facturas asociadas."
        ),
        FaqItem(
            pregunta = "10. ¿Qué hago si detecto un precio incorrecto?",
            respuesta = "Si ves un precio que no coincide con lo pactado, puedes contactar directamente con la carnicería " +
                    "para revisarlo. Los precios de empresa se gestionan en la configuración de tu cuenta y pueden " +
                    "modificarse desde la administración."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // 🔹 Cabecera de EMPRESA
        EmpresaHeader(
            navController = navController,
            titulo = "Preguntas frecuentes",
            mostrarCarrito = true,
            onAbrirCarrito = { mostrarCarritoLateral = true },
            onLogout = onLogout,
            onNavigateHomeEmpresa = { navController.navigate("homeEmpresaScreen/$empresaId") },
            onNavigateEmpresaProductos = { navController.navigate("productosEmpresaScreen/$empresaId") },
            onNavigateEmpresaPedidos = {
                navController.navigate("pedidosYFacturasEmpresas/$empresaId")
            },
            onNavigateEmpresaPerfil = { navController.navigate("perfilEmpresaScreen/$empresaId") },
            onNavigateEmpresaConfig = { navController.navigate("configEmpresaScreen/$empresaId") },
            onNavigateEmpresaSobreNosotros = { navController.navigate("sobreNosotrosEmpresaScreen/$empresaId") },
            onNavigationToFaqEmpresa = { /* ya estás en FAQ empresa, no hace falta navegar */ }
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
                    text = "Información y dudas frecuentes para empresas que usan la aplicación.",
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

    // 🛒 Carrito lateral para empresa (sin envío a domicilio estándar)
    if (mostrarCarritoLateral) {
        CarritoLateral(
            carrito = carritoViewModel.carrito,
            codigoPostalUsuario = null,     // Empresas no usan lógica de reparto normal
            direccionUsuario = null,
            usuarioId = null,
            carritoViewModel = carritoViewModel,
            onCerrar = { mostrarCarritoLateral = false },
            onEliminarItem = { item ->
                carritoViewModel.eliminarProducto(item, context)
            },
            modifier = Modifier.zIndex(1f)
        )
    }
}
