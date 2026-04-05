/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.webhook

import android.content.Context
import androidx.work.*
import com.example.smsgateway.core.Crypto
import com.example.smsgateway.data.AppDatabase
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class WebhookWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val payloadJson = inputData.getString(KEY_PAYLOAD) ?: return Result.failure()
        val db = AppDatabase.getInstance(applicationContext)
        val config = db.webhookConfigDao().get() ?: return Result.success()

        if (!config.enabled || config.url.isBlank()) return Result.success()

        val payload = Json.decodeFromString<WebhookPayload>(payloadJson)

        // Check event filter
        val shouldSend = when (payload.event) {
            WebhookEvent.INCOMING_SMS -> config.onIncomingSms
            WebhookEvent.MESSAGE_SENT -> config.onMessageSent
            WebhookEvent.MESSAGE_DELIVERED -> config.onMessageDelivered
            else -> true
        }
        if (!shouldSend) return Result.success()

        val timestamp = System.currentTimeMillis() / 1000
        val signature = if (config.secret.isNotBlank()) {
            Crypto.hmacSha256(config.secret, Crypto.buildSignaturePayload(timestamp, payloadJson))
        } else ""

        val client = HttpClient(OkHttp)
        return try {
            val response = client.post(config.url) {
                contentType(ContentType.Application.Json)
                header("X-SMSGateway-Event", payload.event)
                header("X-SMSGateway-Timestamp", timestamp.toString())
                if (signature.isNotBlank()) header("X-SMSGateway-Signature", signature)
                setBody(payloadJson)
            }
            if (response.status.isSuccess()) Result.success()
            else if (runAttemptCount < 4) Result.retry()
            else Result.failure()
        } catch (_: Exception) {
            if (runAttemptCount < 4) Result.retry() else Result.failure()
        } finally {
            client.close()
        }
    }

    companion object {
        const val KEY_PAYLOAD = "payload"

        fun enqueue(context: Context, payload: WebhookPayload) {
            val json = Json.encodeToString(payload)
            val data = workDataOf(KEY_PAYLOAD to json)
            val request = OneTimeWorkRequestBuilder<WebhookWorker>()
                .setInputData(data)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
