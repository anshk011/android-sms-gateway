/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.smsgateway.core.Constants
import com.example.smsgateway.core.Crypto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_API_TOKEN = stringPreferencesKey("api_token")
        val KEY_PORT = intPreferencesKey("port")
        val KEY_DEVICE_ID = stringPreferencesKey("device_id")
        val KEY_RELAY_URL = stringPreferencesKey("relay_url")
        val KEY_RELAY_ENABLED = booleanPreferencesKey("relay_enabled")
        val KEY_SERVER_RUNNING = booleanPreferencesKey("server_running")
        val KEY_DEFAULT_SIM_SLOT = intPreferencesKey("default_sim_slot")
    }

    val apiToken: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_API_TOKEN] ?: ""
    }

    val port: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_PORT] ?: Constants.DEFAULT_PORT
    }

    val deviceId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEVICE_ID] ?: ""
    }

    val relayUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_RELAY_URL] ?: ""
    }

    val relayEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_RELAY_ENABLED] ?: false
    }

    val serverRunning: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SERVER_RUNNING] ?: false
    }

    val defaultSimSlot: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_SIM_SLOT] ?: -1
    }

    suspend fun setApiToken(token: String) = context.dataStore.edit { it[KEY_API_TOKEN] = token }
    suspend fun setPort(port: Int) = context.dataStore.edit { it[KEY_PORT] = port }
    suspend fun setDeviceId(id: String) = context.dataStore.edit { it[KEY_DEVICE_ID] = id }
    suspend fun setRelayUrl(url: String) = context.dataStore.edit { it[KEY_RELAY_URL] = url }
    suspend fun setRelayEnabled(enabled: Boolean) = context.dataStore.edit { it[KEY_RELAY_ENABLED] = enabled }
    suspend fun setServerRunning(running: Boolean) = context.dataStore.edit { it[KEY_SERVER_RUNNING] = running }
    suspend fun setDefaultSimSlot(slot: Int) = context.dataStore.edit { it[KEY_DEFAULT_SIM_SLOT] = slot }

    /** Ensure device ID and API token are initialized on first run */
    suspend fun ensureDefaults() {
        context.dataStore.edit { prefs ->
            if (prefs[KEY_DEVICE_ID].isNullOrEmpty()) {
                prefs[KEY_DEVICE_ID] = UUID.randomUUID().toString()
            }
            if (prefs[KEY_API_TOKEN].isNullOrEmpty()) {
                prefs[KEY_API_TOKEN] = Crypto.generateToken()
            }
        }
    }

    suspend fun regenerateToken(): String {
        val token = Crypto.generateToken()
        setApiToken(token)
        return token
    }
}
