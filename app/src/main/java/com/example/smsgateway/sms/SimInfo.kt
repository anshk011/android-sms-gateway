/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.sms

import android.content.Context
import android.telephony.SubscriptionManager
import androidx.annotation.RequiresPermission

data class SimInfo(
    val subscriptionId: Int,
    val slotIndex: Int,
    val displayName: String,
    val carrierName: String,
    val number: String?
)

object SimManager {

    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    fun getActiveSims(context: Context): List<SimInfo> {
        val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        return try {
            sm.activeSubscriptionInfoList?.map { info ->
                SimInfo(
                    subscriptionId = info.subscriptionId,
                    slotIndex = info.simSlotIndex,
                    displayName = info.displayName?.toString() ?: "SIM ${info.simSlotIndex + 1}",
                    carrierName = info.carrierName?.toString() ?: "",
                    number = info.number
                )
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun resolveSubscriptionId(context: Context, simSlot: Int?, subscriptionId: Int?): Int? {
        if (subscriptionId != null) return subscriptionId
        if (simSlot == null) return null
        return try {
            val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            sm.activeSubscriptionInfoList
                ?.firstOrNull { it.simSlotIndex == simSlot }
                ?.subscriptionId
        } catch (_: Exception) {
            null
        }
    }
}
