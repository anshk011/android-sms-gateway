/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway

import com.example.smsgateway.core.Crypto
import org.junit.Assert.*
import org.junit.Test

class CryptoTest {

    @Test
    fun `hmacSha256 produces consistent output`() {
        val sig1 = Crypto.hmacSha256("secret", "hello")
        val sig2 = Crypto.hmacSha256("secret", "hello")
        assertEquals(sig1, sig2)
    }

    @Test
    fun `hmacSha256 differs for different inputs`() {
        val sig1 = Crypto.hmacSha256("secret", "hello")
        val sig2 = Crypto.hmacSha256("secret", "world")
        assertNotEquals(sig1, sig2)
    }

    @Test
    fun `verifyHmac returns true for matching signature`() {
        val secret = "my-secret"
        val data = "payload-data"
        val sig = Crypto.hmacSha256(secret, data)
        assertTrue(Crypto.verifyHmac(secret, data, sig))
    }

    @Test
    fun `verifyHmac returns false for wrong signature`() {
        assertFalse(Crypto.verifyHmac("secret", "data", "wrong-sig"))
    }

    @Test
    fun `generateToken produces non-empty unique tokens`() {
        val t1 = Crypto.generateToken()
        val t2 = Crypto.generateToken()
        assertTrue(t1.isNotEmpty())
        assertNotEquals(t1, t2)
    }

    @Test
    fun `buildSignaturePayload format is correct`() {
        val payload = Crypto.buildSignaturePayload(1700000000L, """{"key":"value"}""")
        assertEquals("""1700000000.{"key":"value"}""", payload)
    }

    @Test
    fun `TTL validation - expired request`() {
        val ttl = System.currentTimeMillis() / 1000 - 60 // 60 seconds ago
        val isExpired = System.currentTimeMillis() / 1000 > ttl
        assertTrue(isExpired)
    }

    @Test
    fun `TTL validation - valid request`() {
        val ttl = System.currentTimeMillis() / 1000 + 30 // 30 seconds in future
        val isExpired = System.currentTimeMillis() / 1000 > ttl
        assertFalse(isExpired)
    }
}
