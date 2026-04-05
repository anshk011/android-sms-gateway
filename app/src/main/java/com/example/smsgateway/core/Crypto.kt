/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.core

import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

object Crypto {

    fun generateToken(byteLength: Int = 32): String {
        val bytes = ByteArray(byteLength)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)
    }

    fun hmacSha256(secret: String, data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val raw = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(raw, Base64.NO_WRAP)
    }

    fun verifyHmac(secret: String, data: String, expected: String): Boolean {
        val computed = hmacSha256(secret, data)
        return computed == expected
    }

    /** Build the signature payload: "<timestamp>.<body>" */
    fun buildSignaturePayload(timestamp: Long, body: String) = "$timestamp.$body"
}
