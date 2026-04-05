/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.net.local

import android.content.Context
import com.example.smsgateway.core.Constants
import com.example.smsgateway.data.AppDatabase
import com.example.smsgateway.data.SettingsDataStore
import com.example.smsgateway.data.entity.WebhookConfigEntity
import com.example.smsgateway.sms.SimManager
import com.example.smsgateway.sms.SmsSender
import com.example.smsgateway.sms.SendSmsRequest
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class LocalServer(private val context: Context) {

    private var engine: ApplicationEngine? = null
    private val db = AppDatabase.getInstance(context)
    private val settings = SettingsDataStore(context)
    private val smsSender = SmsSender(context)

    suspend fun start(port: Int, apiToken: String) {
        if (engine != null) return
        engine = embeddedServer(CIO, port = port, host = "0.0.0.0") {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; prettyPrint = false })
            }
            install(StatusPages) {
                exception<Throwable> { call, cause ->
                    call.respond(HttpStatusCode.InternalServerError,
                        ErrorResponse(cause.message ?: "Internal error", 500))
                }
            }
            install(Authentication) {
                bearer("api-auth") {
                    authenticate { credential ->
                        if (credential.token == apiToken) UserIdPrincipal("client") else null
                    }
                }
            }
            routing {
                get("/") {
                    call.respondText("SMS Gateway for Android™ — API running on port $port")
                }
                route(Constants.API_BASE) {
                    // Health (public)
                    get("/health") {
                        val deviceId = settings.deviceId.first()
                        call.respond(HealthResponse(
                            version = Constants.APP_VERSION,
                            deviceId = deviceId
                        ))
                    }

                    authenticate("api-auth") {
                        // Send SMS
                        post("/messages") {
                            val req = call.receive<SendMessageRequest>()
                            if (req.to.isBlank() || req.text.isBlank()) {
                                call.respond(HttpStatusCode.BadRequest,
                                    ErrorResponse("'to' and 'text' are required", 400))
                                return@post
                            }
                            val result = smsSender.send(SendSmsRequest(
                                to = req.to,
                                text = req.text,
                                simSlot = req.simSlot,
                                subscriptionId = req.subscriptionId,
                                clientRef = req.clientRef
                            ))
                            call.respond(HttpStatusCode.Accepted,
                                SendMessageResponse(result.messageId, result.state.name))
                        }

                        // Get message by ID
                        get("/messages/{id}") {
                            val id = call.parameters["id"] ?: return@get call.respond(
                                HttpStatusCode.BadRequest, ErrorResponse("Missing id", 400))
                            val msg = db.messageDao().getById(id)
                                ?: return@get call.respond(HttpStatusCode.NotFound,
                                    ErrorResponse("Message not found", 404))
                            call.respond(msg.toResponse())
                        }

                        // List recent messages
                        get("/messages") {
                            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                            val messages = db.messageDao().getRecent(limit.coerceIn(1, 200))
                            call.respond(messages.map { it.toResponse() })
                        }

                        // List SIMs
                        get("/sims") {
                            val sims = try {
                                SimManager.getActiveSims(context).map {
                                    SimResponse(it.subscriptionId, it.slotIndex,
                                        it.displayName, it.carrierName, it.number)
                                }
                            } catch (_: SecurityException) {
                                emptyList()
                            }
                            call.respond(sims)
                        }

                        // Get webhook config
                        get("/webhooks") {
                            val config = db.webhookConfigDao().get()
                                ?: WebhookConfigEntity()
                            call.respond(config.toResponse())
                        }

                        // Update webhook config
                        put("/webhooks") {
                            val req = call.receive<WebhookConfigRequest>()
                            val entity = WebhookConfigEntity(
                                url = req.url,
                                secret = req.secret,
                                enabled = req.enabled,
                                onIncomingSms = req.onIncomingSms,
                                onMessageSent = req.onMessageSent,
                                onMessageDelivered = req.onMessageDelivered
                            )
                            db.webhookConfigDao().upsert(entity)
                            call.respond(entity.toResponse())
                        }
                    }
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        engine?.stop(1000, 3000)
        engine = null
    }

    val isRunning get() = engine != null
}

// Extension mappers
private fun com.example.smsgateway.data.entity.MessageEntity.toResponse() = MessageResponse(
    id = id, direction = direction.name, phoneNumber = phoneNumber, body = body,
    state = state.name, subscriptionId = subscriptionId, simSlot = simSlot,
    clientRef = clientRef, partsTotal = partsTotal, partsSent = partsSent,
    partsDelivered = partsDelivered, createdAt = createdAt, updatedAt = updatedAt,
    errorMessage = errorMessage
)

private fun WebhookConfigEntity.toResponse() = WebhookConfigResponse(
    url = url, enabled = enabled, onIncomingSms = onIncomingSms,
    onMessageSent = onMessageSent, onMessageDelivered = onMessageDelivered
)
