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
import com.example.smsgateway.ui.MainViewModel

@Composable
fun CloudScreen(viewModel: MainViewModel) {
    val settings by viewModel.uiSettings.collectAsState()

    var relayUrl by remember(settings.relayUrl) { mutableStateOf(settings.relayUrl) }
    var relayEnabled by remember(settings.relayEnabled) { mutableStateOf(settings.relayEnabled) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Cloud Relay", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Device ID", style = MaterialTheme.typography.titleSmall)
                Text(settings.deviceId, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary)
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Cloud Relay", style = MaterialTheme.typography.titleMedium)
            Switch(checked = relayEnabled, onCheckedChange = { relayEnabled = it })
        }

        OutlinedTextField(
            value = relayUrl, onValueChange = { relayUrl = it },
            label = { Text("Relay Server URL") },
            placeholder = { Text("wss://relay.example.com") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            enabled = relayEnabled
        )

        Button(
            onClick = {
                viewModel.setRelayUrl(relayUrl)
                viewModel.setRelayEnabled(relayEnabled)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Save & Apply")
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("How it works", style = MaterialTheme.typography.titleSmall)
                Text(
                    "The device connects to the relay server via WebSocket. " +
                    "External clients call the relay API; the relay forwards requests to this device. " +
                    "Requests are signed with HMAC-SHA256 and include a TTL to prevent replay attacks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
