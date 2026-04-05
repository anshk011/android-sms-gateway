/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.sms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.example.smsgateway.core.Constants
import com.example.smsgateway.data.AppDatabase
import com.example.smsgateway.data.entity.MessageState
import com.example.smsgateway.webhook.WebhookEvent
import com.example.smsgateway.webhook.WebhookWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsSentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getStringExtra(Constants.EXTRA_MESSAGE_ID) ?: return
        val partIndex = intent.getIntExtra(Constants.EXTRA_PART_INDEX, 0)

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val message = db.messageDao().getById(messageId) ?: return@launch

            when (resultCode) {
                Activity.RESULT_OK -> {
                    val newSent = message.partsSent + 1
                    db.messageDao().updatePartsSent(messageId, newSent)
                    if (newSent >= message.partsTotal) {
                        db.messageDao().updateState(messageId, MessageState.SENT)
                        val updated = db.messageDao().getById(messageId) ?: return@launch
                        WebhookWorker.enqueue(context, WebhookEvent.messageSent(updated))
                    }
                }
                SmsManager.RESULT_ERROR_GENERIC_FAILURE,
                SmsManager.RESULT_ERROR_NO_SERVICE,
                SmsManager.RESULT_ERROR_NULL_PDU,
                SmsManager.RESULT_ERROR_RADIO_OFF -> {
                    db.messageDao().updateState(messageId, MessageState.FAILED,
                        error = "Send failed with code $resultCode (part $partIndex)")
                    val updated = db.messageDao().getById(messageId) ?: return@launch
                    WebhookWorker.enqueue(context, WebhookEvent.messageSent(updated))
                }
            }
        }
    }
}
