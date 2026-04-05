/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway

import com.example.smsgateway.data.entity.MessageDirection
import com.example.smsgateway.data.entity.MessageEntity
import com.example.smsgateway.data.entity.MessageState
import com.example.smsgateway.webhook.WebhookEvent
import com.example.smsgateway.webhook.WebhookPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class WebhookPayloadTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun sampleMessage(direction: MessageDirection = MessageDirection.INBOUND) = MessageEntity(
        id = "test-id-123",
        direction = direction,
        phoneNumber = "+1234567890",
        body = "Hello world",
        state = MessageState.DELIVERED,
        subscriptionId = 1,
        createdAt = 1700000000000L,
        updatedAt = 1700000000000L
    )

    @Test
    fun `incomingSms payload has correct event type`() {
        val payload = WebhookEvent.incomingSms(sampleMessage())
        assertEquals(WebhookEvent.INCOMING_SMS, payload.event)
    }

    @Test
    fun `messageSent payload has correct event type`() {
        val msg = sampleMessage(MessageDirection.OUTBOUND).copy(state = MessageState.SENT)
        val payload = WebhookEvent.messageSent(msg)
        assertEquals(WebhookEvent.MESSAGE_SENT, payload.event)
    }

    @Test
    fun `payload serializes and deserializes correctly`() {
        val payload = WebhookEvent.incomingSms(sampleMessage())
        val serialized = json.encodeToString(payload)
        val deserialized = json.decodeFromString<WebhookPayload>(serialized)
        assertEquals(payload.messageId, deserialized.messageId)
        assertEquals(payload.phoneNumber, deserialized.phoneNumber)
        assertEquals(payload.body, deserialized.body)
        assertEquals(payload.event, deserialized.event)
    }

    @Test
    fun `payload contains required fields`() {
        val payload = WebhookEvent.incomingSms(sampleMessage())
        val serialized = json.encodeToString(payload)
        assertTrue(serialized.contains("messageId"))
        assertTrue(serialized.contains("phoneNumber"))
        assertTrue(serialized.contains("body"))
        assertTrue(serialized.contains("event"))
        assertTrue(serialized.contains("timestamp"))
    }
}
