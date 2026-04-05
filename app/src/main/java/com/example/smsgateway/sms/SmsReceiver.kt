/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.smsgateway.data.AppDatabase
import com.example.smsgateway.data.entity.MessageDirection
import com.example.smsgateway.data.entity.MessageEntity
import com.example.smsgateway.data.entity.MessageState
import com.example.smsgateway.webhook.WebhookEvent
import com.example.smsgateway.webhook.WebhookWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Group parts by originating address
        val grouped = messages.groupBy { it.originatingAddress }

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            grouped.forEach { (address, parts) ->
                val body = parts.joinToString("") { it.messageBody }
                val timestamp = parts.first().timestampMillis
                val subscriptionId = intent.getIntExtra("subscription", -1).takeIf { it != -1 }

                val entity = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    direction = MessageDirection.INBOUND,
                    phoneNumber = address ?: "unknown",
                    body = body,
                    state = MessageState.DELIVERED,
                    subscriptionId = subscriptionId,
                    createdAt = timestamp,
                    updatedAt = timestamp
                )
                db.messageDao().insert(entity)

                // Enqueue webhook delivery
                WebhookWorker.enqueue(context, WebhookEvent.incomingSms(entity))
            }
        }
    }
}
