/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway

import android.app.Application
import androidx.work.Configuration
import com.example.smsgateway.data.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsGatewayApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            SettingsDataStore(this@SmsGatewayApp).ensureDefaults()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setMinimumLoggingLevel(android.util.Log.INFO).build()
}
