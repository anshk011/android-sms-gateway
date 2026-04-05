/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.data.dao

import androidx.room.*
import com.example.smsgateway.data.entity.WebhookConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WebhookConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: WebhookConfigEntity)

    @Query("SELECT * FROM webhook_config WHERE id = 1")
    suspend fun get(): WebhookConfigEntity?

    @Query("SELECT * FROM webhook_config WHERE id = 1")
    fun observe(): Flow<WebhookConfigEntity?>
}
