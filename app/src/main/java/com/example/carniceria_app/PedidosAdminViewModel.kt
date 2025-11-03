package com.example.carniceria_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carniceria.shared.shared.models.utils.SupabaseProvider
import com.carniceria.shared.shared.models.utils.SupabaseService
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PedidosAdminViewModel : ViewModel() {

    private val _pedidos = MutableStateFlow<List<com.carniceria.shared.shared.models.utils.PedidoDetalle>>(emptyList())
    val pedidos = _pedidos.asStateFlow()

    private val service = SupabaseService(SupabaseProvider.client)
    private val _cargandoPedidos = MutableStateFlow<Set<Long>>(emptySet())
    val cargandoPedidos = _cargandoPedidos.asStateFlow()


    init {
        cargarPedidos()
    }

    fun cargarPedidos() {
        viewModelScope.launch {
            try {
                _pedidos.value = service.obtenerTodosPedidos()
            } catch (e: Exception) {
                println("❌ Error al cargar pedidos: ${e.message}")
            }
        }
    }

    fun cambiarEstadoPedido(idPedido: Long, nuevoEstado: String) {
        viewModelScope.launch {
            try {
                // 1️⃣ Actualizar estado en Supabase
                SupabaseProvider.client
                    .from("pedido")
                    .update(mapOf("estado" to nuevoEstado)) {
                        filter { eq("id", idPedido) }
                    }

                println("✅ Pedido #$idPedido actualizado a '$nuevoEstado'")

                // 2️⃣ Enviar notificación
                enviarNotificacionCambioEstado(idPedido, nuevoEstado)

                // 3️⃣ Si el pedido ha sido entregado, generar la factura
                if (nuevoEstado.equals("entregado", ignoreCase = true)) {
                    println("🧾 Generando factura para pedido #$idPedido ...")

                    try {
                        // Llamamos al SupabaseService
                        val facturaId = service.generarFactura(idPedido)
                        println("✅ Factura creada con ID: $facturaId")

                        /** // Generamos el PDF con la Edge Function
                        val urlPdf = service.generarFacturaPdf(idPedido)
                        if (urlPdf != null) {
                            println("📄 PDF generado correctamente: $urlPdf")
                        } else {
                            println("⚠️ No se generó el PDF para la factura $facturaId")
                        }**/
                    } catch (e: Exception) {
                        println("❌ Error al generar factura: ${e.message}")
                    }
                }

                // 4️⃣ Refrescar lista
                cargarPedidos()

            } catch (e: Exception) {
                println("❌ Error cambiando estado del pedido: ${e.message}")
            }
        }
    }


}
