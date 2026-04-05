/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.net.local

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val to: String,
    val text: String,
    val simSlot: Int? = null,
    val subscriptionId: Int? = null,
    val clientRef: String? = null
)

@Serializable
data class SendMessageResponse(
    val messageId: String,
    val state: String
)

@Serializable
data class MessageResponse(
    val id: String,
    val direction: String,
    val phoneNumber: String,
    val body: String,
    val state: String,
    val subscriptionId: Int? = null,
    val simSlot: Int? = null,
    val clientRef: String? = null,
    val partsTotal: Int,
    val partsSent: Int,
    val partsDelivered: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val errorMessage: String? = null
)

@Serializable
data class HealthResponse(
    val status: String = "ok",
    val version: String,
    val deviceId: String
)

@Serializable
data class SimResponse(
    val subscriptionId: Int,
    val slotIndex: Int,
    val displayName: String,
    val carrierName: String,
    val number: String?
)

@Serializable
data class WebhookConfigRequest(
    val url: String,
    val secret: String = "",
    val enabled: Boolean = true,
    val onIncomingSms: Boolean = true,
    val onMessageSent: Boolean = true,
    val onMessageDelivered: Boolean = true
)

@Serializable
data class WebhookConfigResponse(
    val url: String,
    val enabled: Boolean,
    val onIncomingSms: Boolean,
    val onMessageSent: Boolean,
    val onMessageDelivered: Boolean
)

@Serializable
data class ErrorResponse(val error: String, val code: Int)
