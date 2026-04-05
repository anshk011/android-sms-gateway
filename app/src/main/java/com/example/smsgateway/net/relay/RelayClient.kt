/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.net.relay

import android.content.Context
import android.util.Log
import com.example.smsgateway.core.Constants
import com.example.smsgateway.core.Crypto
import com.example.smsgateway.data.AppDatabase
import com.example.smsgateway.data.SettingsDataStore
import com.example.smsgateway.net.local.SendMessageRequest
import com.example.smsgateway.net.local.WebhookConfigRequest
import com.example.smsgateway.sms.SendSmsRequest
import com.example.smsgateway.sms.SimManager
import com.example.smsgateway.sms.SmsSender
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

private const val TAG = "RelayClient"

class RelayClient(private val context: Context) {

    private val settings = SettingsDataStore(context)
    private val db = AppDatabase.getInstance(context)
    private val smsSender = SmsSender(context)
    private var job: Job? = null
    private val json = Json { ignoreUnknownKeys = true }

    val isConnected get() = job?.isActive == true

    fun connect(scope: CoroutineScope) {
        job?.cancel()
        job = scope.launch {
            while (isActive) {
                try {
                    val relayUrl = settings.relayUrl.first()
                    val deviceId = settings.deviceId.first()
                    val token = settings.apiToken.first()
                    if (relayUrl.isBlank()) { delay(Constants.RELAY_RECONNECT_DELAY_MS); continue }

                    val wsUrl = buildWsUrl(relayUrl, deviceId)
                    Log.d(TAG, "Connecting to relay: $wsUrl")

                    val client = HttpClient(OkHttp) { install(WebSockets) }
                    client.webSocket(wsUrl) {
                        // Authenticate
                        val authEnvelope = RelayEnvelope(
                            type = RelayMessageType.AUTH,
                            id = UUID.randomUUID().toString(),
                            headers = mapOf("Authorization" to "Bearer $token")
                        )
                        send(json.encodeToString(authEnvelope))

                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                handleFrame(frame.readText(), this)
                            }
                        }
                    }
                    client.close()
                } catch (e: CancellationException) {
                    break
                } catch (e: Exception) {
                    Log.w(TAG, "Relay error: ${e.message}, reconnecting...")
                }
                delay(Constants.RELAY_RECONNECT_DELAY_MS)
            }
        }
    }

    fun disconnect() {
        job?.cancel()
        job = null
    }

    private suspend fun handleFrame(text: String, session: DefaultClientWebSocketSession) {
        val envelope = try { json.decodeFromString<RelayEnvelope>(text) } catch (_: Exception) { return }

        when (envelope.type) {
            RelayMessageType.PING -> {
                session.send(json.encodeToString(envelope.copy(type = RelayMessageType.PONG)))
                return
            }
            RelayMessageType.REQUEST -> handleRequest(envelope, session)
        }
    }

    private suspend fun handleRequest(req: RelayEnvelope, session: DefaultClientWebSocketSession) {
        // TTL check
        if (req.ttl > 0 && System.currentTimeMillis() / 1000 > req.ttl) {
            sendResponse(session, req.id, 408, """{"error":"Request TTL expired"}""")
            return
        }

        val path = req.path ?: ""
        val method = req.method?.uppercase() ?: "GET"

        val (statusCode, responseBody) = try {
            routeRequest(method, path, req.body)
        } catch (e: Exception) {
            500 to """{"error":"${e.message}"}"""
        }

        sendResponse(session, req.id, statusCode, responseBody)
    }

    private suspend fun routeRequest(method: String, path: String, body: String?): Pair<Int, String> {
        val apiBase = "/api/${Constants.API_VERSION}"
        return when {
            method == "GET" && path == "$apiBase/health" -> {
                val deviceId = settings.deviceId.first()
                200 to """{"status":"ok","version":"${Constants.APP_VERSION}","deviceId":"$deviceId"}"""
            }
            method == "POST" && path == "$apiBase/messages" -> {
                val req = json.decodeFromString<SendMessageRequest>(body ?: "{}")
                val result = smsSender.send(SendSmsRequest(req.to, req.text, req.simSlot, req.subscriptionId, req.clientRef))
                202 to """{"messageId":"${result.messageId}","state":"${result.state.name}"}"""
            }
            method == "GET" && path.startsWith("$apiBase/messages/") -> {
                val id = path.removePrefix("$apiBase/messages/")
                val msg = db.messageDao().getById(id)
                if (msg != null) 200 to json.encodeToString(msg.id)
                else 404 to """{"error":"Not found"}"""
            }
            method == "GET" && path == "$apiBase/sims" -> {
                val sims = SimManager.getActiveSims(context)
                200 to json.encodeToString(sims.map { it.subscriptionId })
            }
            method == "PUT" && path == "$apiBase/webhooks" -> {
                val req = json.decodeFromString<WebhookConfigRequest>(body ?: "{}")
                db.webhookConfigDao().upsert(
                    com.example.smsgateway.data.entity.WebhookConfigEntity(
                        url = req.url, secret = req.secret, enabled = req.enabled,
                        onIncomingSms = req.onIncomingSms, onMessageSent = req.onMessageSent,
                        onMessageDelivered = req.onMessageDelivered
                    )
                )
                200 to """{"status":"ok"}"""
            }
            else -> 404 to """{"error":"Not found"}"""
        }
    }

    private suspend fun sendResponse(session: DefaultClientWebSocketSession, requestId: String, status: Int, body: String) {
        val token = settings.apiToken.first()
        val timestamp = System.currentTimeMillis() / 1000
        val sig = Crypto.hmacSha256(token, Crypto.buildSignaturePayload(timestamp, body))
        val envelope = RelayEnvelope(
            type = RelayMessageType.RESPONSE,
            id = requestId,
            statusCode = status,
            body = body,
            signature = sig,
            headers = mapOf("X-SMSGateway-Timestamp" to timestamp.toString())
        )
        session.send(json.encodeToString(envelope))
    }

    private fun buildWsUrl(relayUrl: String, deviceId: String): String {
        val base = relayUrl.trimEnd('/')
        return "$base/ws?deviceId=$deviceId"
    }
}
