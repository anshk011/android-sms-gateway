/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smsgateway.ui.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val settings by viewModel.uiSettings.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("SMS Gateway", style = MaterialTheme.typography.headlineMedium)

        // Status card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (settings.serverRunning) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (settings.serverRunning) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (settings.serverRunning) "Server Running" else "Server Stopped",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (settings.serverRunning) {
                    Text("Address: http://${settings.localIp}:${settings.port}",
                        style = MaterialTheme.typography.bodyMedium)
                    Text("API Base: /api/v1", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Device ID: ${settings.deviceId.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Port config
        var portText by remember(settings.port) { mutableStateOf(settings.port.toString()) }
        OutlinedTextField(
            value = portText,
            onValueChange = { portText = it },
            label = { Text("Port") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    portText.toIntOrNull()?.let { viewModel.setPort(it) }
                }) { Icon(Icons.Default.Save, "Save port") }
            }
        )

        // Start / Stop button
        Button(
            onClick = { if (settings.serverRunning) viewModel.stopServer() else viewModel.startServer() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (settings.serverRunning) MaterialTheme.colorScheme.error
                                 else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                if (settings.serverRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(if (settings.serverRunning) "Stop Server" else "Start Server")
        }
    }
}
