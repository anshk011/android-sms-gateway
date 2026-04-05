/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smsgateway.data.entity.WebhookConfigEntity
import com.example.smsgateway.ui.MainViewModel

@Composable
fun WebhookScreen(viewModel: MainViewModel) {
    val config by viewModel.webhookConfig.collectAsState()

    var url by remember(config.url) { mutableStateOf(config.url) }
    var secret by remember(config.secret) { mutableStateOf(config.secret) }
    var enabled by remember(config.enabled) { mutableStateOf(config.enabled) }
    var onIncoming by remember(config.onIncomingSms) { mutableStateOf(config.onIncomingSms) }
    var onSent by remember(config.onMessageSent) { mutableStateOf(config.onMessageSent) }
    var onDelivered by remember(config.onMessageDelivered) { mutableStateOf(config.onMessageDelivered) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Webhook Configuration", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = url, onValueChange = { url = it },
            label = { Text("Webhook URL") },
            placeholder = { Text("https://your-server.com/webhook") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        OutlinedTextField(
            value = secret, onValueChange = { secret = it },
            label = { Text("Secret (for HMAC signing)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Events", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Enabled")
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Incoming SMS")
                    Switch(checked = onIncoming, onCheckedChange = { onIncoming = it })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Message Sent")
                    Switch(checked = onSent, onCheckedChange = { onSent = it })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Message Delivered")
                    Switch(checked = onDelivered, onCheckedChange = { onDelivered = it })
                }
            }
        }

        Button(
            onClick = {
                viewModel.saveWebhookConfig(WebhookConfigEntity(
                    url = url, secret = secret, enabled = enabled,
                    onIncomingSms = onIncoming, onMessageSent = onSent, onMessageDelivered = onDelivered
                ))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Save Webhook Config")
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Signature Verification", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Headers sent with each webhook:\n" +
                    "X-SMSGateway-Event: <event_type>\n" +
                    "X-SMSGateway-Timestamp: <unix_seconds>\n" +
                    "X-SMSGateway-Signature: HMAC-SHA256(secret, timestamp.body)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
