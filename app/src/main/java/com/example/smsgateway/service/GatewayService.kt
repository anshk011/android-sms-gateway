/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.smsgateway.R
import com.example.smsgateway.core.Constants
import com.example.smsgateway.data.SettingsDataStore
import com.example.smsgateway.net.local.LocalServer
import com.example.smsgateway.net.relay.RelayClient
import com.example.smsgateway.ui.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class GatewayService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var settings: SettingsDataStore
    private lateinit var localServer: LocalServer
    private lateinit var relayClient: RelayClient

    companion object {
        const val ACTION_START = "com.example.smsgateway.START"
        const val ACTION_STOP = "com.example.smsgateway.STOP"

        fun start(context: Context) {
            val intent = Intent(context, GatewayService::class.java).apply { action = ACTION_START }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, GatewayService::class.java).apply { action = ACTION_STOP }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        settings = SettingsDataStore(this)
        localServer = LocalServer(this)
        relayClient = RelayClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(Constants.NOTIFICATION_ID, buildNotification("Starting..."))
        when (intent?.action) {
            ACTION_STOP -> {
                scope.launch {
                    stopGateway()
                    stopSelf()
                }
            }
            else -> {
                scope.launch { startGateway() }
            }
        }
        return START_STICKY
    }

    private suspend fun startGateway() {
        val port = settings.port.first()
        val token = settings.apiToken.first()
        val relayEnabled = settings.relayEnabled.first()

        localServer.start(port, token)
        settings.setServerRunning(true)
        updateNotification("Running on port $port")

        if (relayEnabled) {
            relayClient.connect(scope)
        }
    }

    private suspend fun stopGateway() {
        localServer.stop()
        relayClient.disconnect()
        settings.setServerRunning(false)
    }

    override fun onDestroy() {
        scope.launch { stopGateway() }
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            "SMS Gateway Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Keeps the SMS gateway running" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(status: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("SMS Gateway")
            .setContentText(status)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(status: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(Constants.NOTIFICATION_ID, buildNotification(status))
    }
}
