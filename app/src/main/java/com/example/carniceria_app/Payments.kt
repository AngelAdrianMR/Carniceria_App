package com.example.carniceria_app

import kotlinx.coroutines.delay

/**
 * Contrato único para iniciar un checkout/pago desde la app.
 * - En DEV: FakePaymentGateway
 * - En PROD: RealPaymentGateway (más adelante)
 */
interface PaymentGateway {
    suspend fun startCheckout(
        orderId: String,
        amountCents: Long,
        currency: String = "EUR"
    ): CheckoutResult
}

/**
 * Resultado del "checkout" (simulado o real).
 */
sealed class CheckoutResult {
    data class Success(val providerPaymentId: String) : CheckoutResult()
    data class Canceled(val reason: String? = null) : CheckoutResult()
    data class Failure(val message: String, val cause: Throwable? = null) : CheckoutResult()
}

/**
 * Implementación fake para desarrollo:
 * - No cobra nada
 * - Simula latencia y outcomes típicos (success/cancel/fail)
 */
class FakePaymentGateway(
    private val mode: Mode = Mode.ALWAYS_SUCCESS,
    private val delayMs: Long = 800L
) : PaymentGateway {

    enum class Mode {
        ALWAYS_SUCCESS,
        ALWAYS_CANCEL,
        ALWAYS_FAIL,
        RANDOM
    }

    override suspend fun startCheckout(
        orderId: String,
        amountCents: Long,
        currency: String
    ): CheckoutResult {
        // Simula el tiempo de red / interacción con pasarela
        delay(delayMs)

        return when (mode) {
            Mode.ALWAYS_SUCCESS ->
                CheckoutResult.Success(providerPaymentId = "fake_$orderId")

            Mode.ALWAYS_CANCEL ->
                CheckoutResult.Canceled(reason = "Usuario canceló (simulado)")

            Mode.ALWAYS_FAIL ->
                CheckoutResult.Failure(message = "Fallo de pago (simulado)")

            Mode.RANDOM -> listOf(
                CheckoutResult.Success(providerPaymentId = "fake_$orderId"),
                CheckoutResult.Canceled(reason = "Cancelado (simulado)"),
                CheckoutResult.Failure(message = "Error de pago (simulado)")
            ).random()
        }
    }
}
