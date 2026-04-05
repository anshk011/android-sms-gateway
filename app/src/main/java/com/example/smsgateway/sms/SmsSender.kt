/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.sms

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.example.smsgateway.core.Constants
import com.example.smsgateway.data.AppDatabase
import com.example.smsgateway.data.entity.MessageDirection
import com.example.smsgateway.data.entity.MessageEntity
import com.example.smsgateway.data.entity.MessageState
import java.util.UUID

data class SendSmsRequest(
    val to: String,
    val text: String,
    val simSlot: Int? = null,
    val subscriptionId: Int? = null,
    val clientRef: String? = null
)

data class SendSmsResult(
    val messageId: String,
    val state: MessageState,
    val partsCount: Int
)

class SmsSender(private val context: Context) {

    private val db = AppDatabase.getInstance(context)

    @Suppress("DEPRECATION")
    suspend fun send(request: SendSmsRequest): SendSmsResult {
        val messageId = UUID.randomUUID().toString()
        val resolvedSubId = SimManager.resolveSubscriptionId(context, request.simSlot, request.subscriptionId)

        val smsManager: SmsManager = if (resolvedSubId != null) {
            SmsManager.getSmsManagerForSubscriptionId(resolvedSubId)
        } else {
            SmsManager.getDefault()
        }

        val parts = smsManager.divideMessage(request.text)
        val partsCount = parts.size

        // Persist message
        val entity = MessageEntity(
            id = messageId,
            direction = MessageDirection.OUTBOUND,
            phoneNumber = request.to,
            body = request.text,
            state = MessageState.QUEUED,
            subscriptionId = resolvedSubId,
            simSlot = request.simSlot,
            clientRef = request.clientRef,
            partsTotal = partsCount
        )
        db.messageDao().insert(entity)

        // Build PendingIntents per part
        val sentIntents = ArrayList<PendingIntent>()
        val deliveredIntents = ArrayList<PendingIntent>()

        for (i in 0 until partsCount) {
            val sentIntent = Intent(Constants.ACTION_SENT).apply {
                setPackage(context.packageName)
                putExtra(Constants.EXTRA_MESSAGE_ID, messageId)
                putExtra(Constants.EXTRA_PART_INDEX, i)
            }
            val deliveredIntent = Intent(Constants.ACTION_DELIVERED).apply {
                setPackage(context.packageName)
                putExtra(Constants.EXTRA_MESSAGE_ID, messageId)
                putExtra(Constants.EXTRA_PART_INDEX, i)
            }
            sentIntents.add(
                PendingIntent.getBroadcast(context, messageId.hashCode() + i * 1000,
                    sentIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
            deliveredIntents.add(
                PendingIntent.getBroadcast(context, messageId.hashCode() + i * 1000 + 500,
                    deliveredIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
        }

        db.messageDao().updateState(messageId, MessageState.SENDING)

        try {
            if (partsCount == 1) {
                smsManager.sendTextMessage(request.to, null, parts[0], sentIntents[0], deliveredIntents[0])
            } else {
                smsManager.sendMultipartTextMessage(request.to, null, parts, sentIntents, deliveredIntents)
            }
        } catch (e: Exception) {
            db.messageDao().updateState(messageId, MessageState.FAILED, error = e.message)
            return SendSmsResult(messageId, MessageState.FAILED, partsCount)
        }

        return SendSmsResult(messageId, MessageState.SENDING, partsCount)
    }
}
