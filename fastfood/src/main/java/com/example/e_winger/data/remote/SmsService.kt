package com.example.e_winger.data.remote

import android.util.Base64
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
import java.util.Locale

@Serializable
data class SmsRecipient(
    val recipient_id: Int,
    val dest_addr: String
)

@Serializable
data class SmsRequest(
    val source_addr: String,
    val schedule_time: String = "",
    val encoding: Int = 0,
    val message: String,
    val recipients: List<SmsRecipient>
)

object SmsService {
    private const val API_KEY = "c42a21f7a2123ce4"
    private const val SECRET_KEY = "YzdhMzg3ODI4YTE3NmVkMWM1MGMzMTU0ZTcwYWMyYzJiNTA3OTI5ZGNlODE5N2M5NjJhOTVjMGRlNjRkZmRmMg=="
    private const val BASE_URL = "https://api.beem.africa/public/v1/send"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true 
                encodeDefaults = true
            })
        }
    }

    /**
     * Sends an order confirmation SMS via Beem.
     * destAddr should be in format 255XXXXXXXXX
     */
    suspend fun sendOrderConfirmation(destAddr: String, orderId: String, totalAmount: Double) {
        val formattedNumber = formatPhoneNumber(destAddr)
        val message = "Habari! Agizo lako la Fast Food (#$orderId) la TSH ${String.format(Locale.US, "%,.2f", totalAmount)} limepokelewa na linaandaliwa. Asante!"

        val authString = "$API_KEY:$SECRET_KEY"
        val encodedAuth = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)

        try {
            val response: HttpResponse = client.post(BASE_URL) {
                header(HttpHeaders.Authorization, "Basic $encodedAuth")
                contentType(ContentType.Application.Json)
                setBody(SmsRequest(
                    source_addr = "INFO", // Or your registered Sender ID
                    message = message,
                    recipients = listOf(SmsRecipient(1, formattedNumber))
                ))
            }

            if (response.status.isSuccess()) {
                Log.d("SmsService", "SMS sent successfully to $formattedNumber")
            } else {
                Log.e("SmsService", "Failed to send SMS: ${response.status} - ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            Log.e("SmsService", "Error sending SMS", e)
        }
    }

    private fun formatPhoneNumber(number: String): String {
        val clean = number.replace(Regex("[^0-9]"), "")
        return when {
            clean.startsWith("255") -> clean
            clean.startsWith("0") -> "255" + clean.substring(1)
            else -> "255" + clean
        }
    }
}
