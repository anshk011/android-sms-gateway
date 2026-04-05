/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "webhook_config")
data class WebhookConfigEntity(
    @PrimaryKey val id: Int = 1,
    val url: String = "",
    val secret: String = "",
    val enabled: Boolean = false,
    val onIncomingSms: Boolean = true,
    val onMessageSent: Boolean = true,
    val onMessageDelivered: Boolean = true
)
