/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smsgateway.data.entity.MessageDirection
import com.example.smsgateway.data.entity.MessageEntity
import com.example.smsgateway.data.entity.MessageState
import com.example.smsgateway.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogsScreen(viewModel: MainViewModel) {
    val messages by viewModel.messages.collectAsState()
    val fmt = remember { SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Message Logs", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("${messages.size} messages", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages, key = { it.id }) { msg ->
                MessageCard(msg, fmt)
            }
        }
    }
}

@Composable
private fun MessageCard(msg: MessageEntity, fmt: SimpleDateFormat) {
    val stateColor = when (msg.state) {
        MessageState.DELIVERED -> MaterialTheme.colorScheme.primary
        MessageState.SENT -> MaterialTheme.colorScheme.secondary
        MessageState.FAILED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(
                if (msg.direction == MessageDirection.INBOUND) Icons.Default.ArrowDownward
                else Icons.Default.ArrowUpward,
                contentDescription = msg.direction.name,
                tint = if (msg.direction == MessageDirection.INBOUND)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 2.dp, end = 8.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(msg.phoneNumber, style = MaterialTheme.typography.titleSmall)
                    Text(fmt.format(Date(msg.createdAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(msg.body, style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge(containerColor = stateColor) {
                        Text(msg.state.name, style = MaterialTheme.typography.labelSmall)
                    }
                    if (msg.partsTotal > 1) {
                        Text("${msg.partsSent}/${msg.partsTotal} parts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                msg.errorMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
