/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageDirection { OUTBOUND, INBOUND }
enum class MessageState { QUEUED, SENDING, SENT, DELIVERED, FAILED }

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val direction: MessageDirection,
    val phoneNumber: String,
    val body: String,
    val state: MessageState = MessageState.QUEUED,
    val subscriptionId: Int? = null,
    val simSlot: Int? = null,
    val clientRef: String? = null,
    val partsTotal: Int = 1,
    val partsSent: Int = 0,
    val partsDelivered: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)
