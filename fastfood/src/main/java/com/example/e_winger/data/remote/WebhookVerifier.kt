package com.example.e_winger.data.remote

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
/ EasyPay Webhook Signature Verifier.
 * Verifies that incoming webhook requests from EasyPay are authentic by checking HMAC-SHA256 signatures.
 */
object WebhookVerifier {

    /**
     * Verifies the EasyPay HMAC-SHA256 signature against the raw request body and secret key.
     * Compatible across all Android API levels.
     *
     * @param rawBody The raw JSON string of the incoming HTTP request body.
     * @param signature The hex signature passed in the webhook header.
     * @param secret Your EasyPay webhook secret key.
     * @return true if the signature is valid, false otherwise.
     */
    fun verify(rawBody: String, signature: String, secret: String = EasyPayService.webhookSecret): Boolean {
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
            mac.init(secretKeySpec)
            
            val expectedBytes = mac.doFinal(rawBody.toByteArray(StandardCharsets.UTF_8))
            val providedBytes = hexToByteArray(signature)
            
            MessageDigest.isEqual(expectedBytes, providedBytes)
        } catch (e: Exception) {
            false
        }
    }

    private fun hexToByteArray(hex: String): ByteArray {
        val cleanHex = hex.trim()
        val len = cleanHex.length
        if (len % 2 != 0) return byteArrayOf()
        
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            val h = Character.digit(cleanHex[i], 16)
            val l = Character.digit(cleanHex[i + 1], 16)
            if (h == -1 || l == -1) return byteArrayOf()
            data[i / 2] = ((h shl 4) + l).toByte()
            i += 2
        }
        return data
    }
}
