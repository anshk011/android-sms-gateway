/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.net.relay

import kotlinx.serialization.Serializable

@Serializable
data class RelayEnvelope(
    val type: String,          // "request" | "response" | "event"
    val id: String,
    val ttl: Long = 0,         // Unix epoch seconds; 0 = no TTL
    val method: String? = null,
    val path: String? = null,
    val statusCode: Int? = null,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val signature: String? = null
)

object RelayMessageType {
    const val REQUEST = "request"
    const val RESPONSE = "response"
    const val EVENT = "event"
    const val AUTH = "auth"
    const val PING = "ping"
    const val PONG = "pong"
}
