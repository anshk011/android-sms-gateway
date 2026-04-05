/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.sms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smsgateway.core.Constants
import com.example.smsgateway.data.AppDatabase
import com.example.smsgateway.data.entity.MessageState
import com.example.smsgateway.webhook.WebhookEvent
import com.example.smsgateway.webhook.WebhookWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsDeliveredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getStringExtra(Constants.EXTRA_MESSAGE_ID) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val message = db.messageDao().getById(messageId) ?: return@launch

            if (resultCode == Activity.RESULT_OK) {
                val newDelivered = message.partsDelivered + 1
                db.messageDao().updatePartsDelivered(messageId, newDelivered)
                if (newDelivered >= message.partsTotal) {
                    db.messageDao().updateState(messageId, MessageState.DELIVERED)
                    val updated = db.messageDao().getById(messageId) ?: return@launch
                    WebhookWorker.enqueue(context, WebhookEvent.messageDelivered(updated))
                }
            }
        }
    }
}
