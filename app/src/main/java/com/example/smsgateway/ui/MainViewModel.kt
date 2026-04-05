/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smsgateway.core.NetworkUtils
import com.example.smsgateway.data.AppDatabase
import com.example.smsgateway.data.SettingsDataStore
import com.example.smsgateway.data.entity.MessageEntity
import com.example.smsgateway.data.entity.WebhookConfigEntity
import com.example.smsgateway.service.GatewayService
import com.example.smsgateway.sms.SimInfo
import com.example.smsgateway.sms.SimManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiSettings(
    val apiToken: String = "",
    val port: Int = 8080,
    val deviceId: String = "",
    val relayUrl: String = "",
    val relayEnabled: Boolean = false,
    val serverRunning: Boolean = false,
    val localIp: String = "",
    val defaultSimSlot: Int = -1
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val settings = SettingsDataStore(app)
    private val db = AppDatabase.getInstance(app)

    val uiSettings: StateFlow<UiSettings> = combine(
        settings.apiToken,
        settings.port,
        settings.deviceId,
        settings.relayUrl,
        settings.relayEnabled,
        settings.serverRunning,
        settings.defaultSimSlot
    ) { values ->
        UiSettings(
            apiToken = values[0] as String,
            port = values[1] as Int,
            deviceId = values[2] as String,
            relayUrl = values[3] as String,
            relayEnabled = values[4] as Boolean,
            serverRunning = values[5] as Boolean,
            localIp = NetworkUtils.getLocalIpAddress(app),
            defaultSimSlot = values[6] as Int
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiSettings())

    val messages: StateFlow<List<MessageEntity>> = db.messageDao()
        .observeRecent(100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val webhookConfig: StateFlow<WebhookConfigEntity> = db.webhookConfigDao()
        .observe()
        .map { it ?: WebhookConfigEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WebhookConfigEntity())

    val sims: StateFlow<List<SimInfo>> = MutableStateFlow<List<SimInfo>>(emptyList()).also { flow ->
        viewModelScope.launch {
            try {
                flow.value = SimManager.getActiveSims(getApplication())
            } catch (_: Exception) {}
        }
    }

    fun startServer() {
        GatewayService.start(getApplication())
    }

    fun stopServer() {
        GatewayService.stop(getApplication())
    }

    fun regenerateToken() = viewModelScope.launch {
        settings.regenerateToken()
    }

    fun setPort(port: Int) = viewModelScope.launch { settings.setPort(port) }
    fun setRelayUrl(url: String) = viewModelScope.launch { settings.setRelayUrl(url) }
    fun setRelayEnabled(enabled: Boolean) = viewModelScope.launch { settings.setRelayEnabled(enabled) }
    fun setDefaultSimSlot(slot: Int) = viewModelScope.launch { settings.setDefaultSimSlot(slot) }

    fun saveWebhookConfig(config: WebhookConfigEntity) = viewModelScope.launch {
        db.webhookConfigDao().upsert(config)
    }
}
