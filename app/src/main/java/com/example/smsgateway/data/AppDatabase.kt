/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smsgateway.data.dao.MessageDao
import com.example.smsgateway.data.dao.WebhookConfigDao
import com.example.smsgateway.data.entity.MessageEntity
import com.example.smsgateway.data.entity.WebhookConfigEntity

@Database(
    entities = [MessageEntity::class, WebhookConfigEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun webhookConfigDao(): WebhookConfigDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sms_gateway.db"
                ).build().also { INSTANCE = it }
            }
    }
}
