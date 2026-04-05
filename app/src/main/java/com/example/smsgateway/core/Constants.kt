/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.core

object Constants {
    const val DEFAULT_PORT = 8080
    const val API_VERSION = "v1"
    const val API_BASE = "/api/$API_VERSION"
    const val APP_VERSION = "1.0.0"
    const val NOTIFICATION_CHANNEL_ID = "sms_gateway_service"
    const val NOTIFICATION_ID = 1001
    const val ACTION_SENT = "com.example.smsgateway.SMS_SENT"
    const val ACTION_DELIVERED = "com.example.smsgateway.SMS_DELIVERED"
    const val EXTRA_MESSAGE_ID = "message_id"
    const val EXTRA_PART_INDEX = "part_index"
    const val WEBHOOK_RETRY_MAX = 5
    const val RELAY_RECONNECT_DELAY_MS = 5000L
    const val REQUEST_TTL_SECONDS = 30L
}
