package com.example.e_winger.data.remote

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class EasyPayRequest(
    val payerPhone: String,
    val payerName: String,
    val amount: String? = null
)

@Serializable
data class EasyPayResponseData(
    val reference: String? = null,
    val state: String? = null,
    val amount: String? = null,
    val currency: String? = null,
    val type: String? = null,
    val merchantReference: String? = null,
    val createdAt: String? = null,
    val paymentMethod: String? = null,
    val qrCode: String? = null,
    val tipsQrCode: String? = null,
    val mode: String? = null,
    val link: String? = null,
    val expiresAt: String? = null,
    val returnUrl: String? = null
)

@Serializable
data class EasyPayResponse(
    val success: Boolean = false,
    val data: EasyPayResponseData? = null,
    val message: String? = null
)

@Serializable
data class BackendPaymentRequest(
    val reference: String,
    val payerPhone: String,
    val payerName: String,
    val amount: Double,
    val idempotencyKey: String
)

object EasyPayService {
    var apiKey: String = "epk_80ddccc6c2a71bfb5f20dd9741e5b8f5d8d8b5058141d2f5"
    var secretKey: String = "ep_sk_4bbb5df40f1b770ef96f425bb06a89f6ba3615542952cbe1861500b17eeadf7d"
    var webhookSecret: String = "whsec_2bbfbb3744c94a7efc7f74f8c14a39cd7423beffb3024f915c1f064c9797c5af"
    var defaultReference: String = "COL20260109LINK01XYZ"
    var backendUrl: String = "https://backend-i830.onrender.com"
    private const val BASE_URL = "https://api.easypay.co.tz/v1/payments"

    private val jsonInstance = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonInstance)
        }
    }

    /**
     * Initiates a mobile payment via EasyPay.
     * @param reference Payment collection reference string (e.g., COL20260109LINK01XYZ).
     * @param payerPhone Phone number of the payer (e.g. 0712 XXX XXX).
     * @param payerName Name of the customer/payer.
     * @param amount Amount to collect.
     * @param idempotencyKey Unique key to prevent duplicate requests.
     */
    suspend fun processPayment(
        reference: String,
        payerPhone: String,
        payerName: String,
        amount: Double,
        idempotencyKey: String
    ): EasyPayResponse {
        if (backendUrl.isNotBlank()) {
            return processPaymentViaBackend(backendUrl, reference, payerPhone, payerName, amount, idempotencyKey)
        }

        val targetReference = if (reference.isBlank()) defaultReference else reference
        val url = "$BASE_URL/$targetReference/pay"
        val formattedAmount = amount.toInt().toString()
        val formattedPhone = formatPhoneNumber(payerPhone)

        val requestBody = EasyPayRequest(
            payerPhone = formattedPhone,
            payerName = payerName,
            amount = formattedAmount
        )

        return try {
            val response: HttpResponse = client.post(url) {
                header("Content-Type", "application/json")
                header("X-API-Key", apiKey)
                header("X-Secret-Key", secretKey)
                header("X-Idempotency-Key", idempotencyKey)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val responseText = response.bodyAsText()
            Log.d("EasyPayService", "EasyPay Response Status: ${response.status}, Body: $responseText")

            val parsedResponse = try {
                jsonInstance.decodeFromString<EasyPayResponse>(responseText)
            } catch (_: Exception) {
                null
            }

            if (response.status.isSuccess()) {
                parsedResponse ?: EasyPayResponse(success = true)
            } else if (response.status == HttpStatusCode.Unauthorized) {
                val serverDetails = parsedResponse?.message ?: if (responseText.isNotBlank()) responseText else "Mismatched or invalid API Key / Secret Key pair."
                val serverMessage = "401 Unauthorized: $serverDetails"
                Log.e("EasyPayService", serverMessage)
                EasyPayResponse(success = false, message = serverMessage)
            } else if (response.status == HttpStatusCode.Conflict) {
                // Handle 409 Conflict (e.g. payment already initiated or duplicate idempotency key)
                if (parsedResponse?.data?.state == "INITIATED" || parsedResponse?.success == true) {
                    parsedResponse
                } else {
                    val serverMessage = parsedResponse?.message ?: "409 Conflict: Transaction already initiated or duplicate idempotency key."
                    Log.w("EasyPayService", "409 Conflict received: $serverMessage")
                    EasyPayResponse(success = false, message = serverMessage)
                }
            } else {
                val serverMessage = parsedResponse?.message ?: "Payment failed (${response.status.value}: ${response.status.description})"
                Log.e("EasyPayService", "EasyPay payment failed: $serverMessage")
                EasyPayResponse(success = false, message = serverMessage)
            }
        } catch (e: Exception) {
            Log.e("EasyPayService", "Error calling EasyPay API", e)
            EasyPayResponse(success = false, message = e.localizedMessage ?: "Network error")
        }
    }

    /**
     * Initiates mobile payment via deployed backend server.
     */
    suspend fun processPaymentViaBackend(
        customBackendUrl: String = backendUrl,
        reference: String,
        payerPhone: String,
        payerName: String,
        amount: Double,
        idempotencyKey: String
    ): EasyPayResponse {
        val targetBackend = customBackendUrl.trimEnd('/')
        if (targetBackend.isBlank()) {
            return processPayment(reference, payerPhone, payerName, amount, idempotencyKey)
        }

        val url = when {
            targetBackend.endsWith("/api/payments/pay") -> targetBackend
            targetBackend.endsWith("/payments/pay") -> targetBackend
            targetBackend.endsWith("/pay") -> targetBackend
            targetBackend.endsWith("/api/payments") -> "$targetBackend/pay"
            else -> "$targetBackend/api/payments/pay"
        }

        val requestBody = BackendPaymentRequest(
            reference = if (reference.isBlank()) defaultReference else reference,
            payerPhone = formatPhoneNumber(payerPhone),
            payerName = payerName,
            amount = amount,
            idempotencyKey = idempotencyKey
        )

        return try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val responseText = response.bodyAsText()
            Log.d("EasyPayService", "Backend Payment Response: ${response.status}, Body: $responseText")

            try {
                jsonInstance.decodeFromString<EasyPayResponse>(responseText)
            } catch (_: Exception) {
                EasyPayResponse(success = response.status.isSuccess())
            }
        } catch (e: Exception) {
            Log.e("EasyPayService", "Error connecting to payment backend", e)
            EasyPayResponse(success = false, message = e.localizedMessage ?: "Backend connection error")
        }
    }

    private fun formatPhoneNumber(number: String): String {
        val clean = number.replace(Regex("[^0-9]"), "")
        return when {
            clean.startsWith("255") && clean.length == 12 -> "0" + clean.substring(3)
            else -> clean
        }
    }
}
