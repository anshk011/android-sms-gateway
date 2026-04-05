/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.core

import android.content.Context
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkUtils {

    fun getLocalIpAddress(context: Context): String {
        try {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ip = wifiManager.connectionInfo.ipAddress
            if (ip != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    ip and 0xff, ip shr 8 and 0xff, ip shr 16 and 0xff, ip shr 24 and 0xff
                )
            }
        } catch (_: Exception) {}

        // Fallback: enumerate interfaces
        try {
            NetworkInterface.getNetworkInterfaces()?.toList()?.forEach { iface ->
                iface.inetAddresses?.toList()?.forEach { addr ->
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "0.0.0.0"
                    }
                }
            }
        } catch (_: Exception) {}

        return "0.0.0.0"
    }
}
