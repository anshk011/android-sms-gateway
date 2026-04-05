/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smsgateway.ui.MainViewModel

@Composable
fun AuthScreen(viewModel: MainViewModel) {
    val settings by viewModel.uiSettings.collectAsState()
    val context = LocalContext.current
    var showToken by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("API Authentication", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("API Token", style = MaterialTheme.typography.titleMedium)
                Text("Use this token in the Authorization header:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Authorization: Bearer <token>",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary)

                OutlinedTextField(
                    value = if (showToken) settings.apiToken else "•".repeat(minOf(settings.apiToken.length, 32)),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { showToken = !showToken }) {
                                Icon(if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    "Toggle visibility")
                            }
                            IconButton(onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("API Token", settings.apiToken))
                            }) {
                                Icon(Icons.Default.ContentCopy, "Copy token")
                            }
                        }
                    }
                )
            }
        }

        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Regenerate Token")
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Regenerate Token?") },
                text = { Text("This will invalidate the current token. All clients will need to be updated.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.regenerateToken()
                        showConfirmDialog = false
                    }) { Text("Regenerate") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
