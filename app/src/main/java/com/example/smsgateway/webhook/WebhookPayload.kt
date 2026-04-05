/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.webhook

import com.example.smsgateway.data.entity.MessageEntity
import com.example.smsgateway.data.entity.MessageDirection
import kotlinx.serialization.Serializable

@Serializable
data class WebhookPayload(
    val event: String,
    val messageId: String,
    val direction: String,
    val phoneNumber: String,
    val body: String,
    val state: String,
    val subscriptionId: Int? = null,
    val simSlot: Int? = null,
    val clientRef: String? = null,
    val timestamp: Long
)

object WebhookEvent {
    const val INCOMING_SMS = "incoming_sms"
    const val MESSAGE_SENT = "message_sent"
    const val MESSAGE_DELIVERED = "message_delivered"

    fun incomingSms(msg: MessageEntity) = WebhookPayload(
        event = INCOMING_SMS,
        messageId = msg.id,
        direction = MessageDirection.INBOUND.name,
        phoneNumber = msg.phoneNumber,
        body = msg.body,
        state = msg.state.name,
        subscriptionId = msg.subscriptionId,
        timestamp = msg.createdAt
    )

    fun messageSent(msg: MessageEntity) = WebhookPayload(
        event = MESSAGE_SENT,
        messageId = msg.id,
        direction = MessageDirection.OUTBOUND.name,
        phoneNumber = msg.phoneNumber,
        body = msg.body,
        state = msg.state.name,
        subscriptionId = msg.subscriptionId,
        simSlot = msg.simSlot,
        clientRef = msg.clientRef,
        timestamp = msg.updatedAt
    )

    fun messageDelivered(msg: MessageEntity) = WebhookPayload(
        event = MESSAGE_DELIVERED,
        messageId = msg.id,
        direction = MessageDirection.OUTBOUND.name,
        phoneNumber = msg.phoneNumber,
        body = msg.body,
        state = msg.state.name,
        subscriptionId = msg.subscriptionId,
        simSlot = msg.simSlot,
        clientRef = msg.clientRef,
        timestamp = msg.updatedAt
    )
}
