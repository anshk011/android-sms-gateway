/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smsgateway.data.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        CoroutineScope(Dispatchers.IO).launch {
            val settings = SettingsDataStore(context)
            if (settings.serverRunning.first()) {
                GatewayService.start(context)
            }
        }
    }
}
