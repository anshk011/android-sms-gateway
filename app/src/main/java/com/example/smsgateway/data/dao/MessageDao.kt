/*
 * Copyright 2024 SMS Gateway for Android
 * Licensed under the Apache License, Version 2.0
 */
package com.example.smsgateway.data.dao

import androidx.room.*
import com.example.smsgateway.data.entity.MessageEntity
import com.example.smsgateway.data.entity.MessageState
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getById(id: String): MessageEntity?

    @Query("SELECT * FROM messages ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 100): List<MessageEntity>

    @Query("UPDATE messages SET state = :state, updatedAt = :now, errorMessage = :error WHERE id = :id")
    suspend fun updateState(id: String, state: MessageState, now: Long = System.currentTimeMillis(), error: String? = null)

    @Query("UPDATE messages SET partsSent = :sent, updatedAt = :now WHERE id = :id")
    suspend fun updatePartsSent(id: String, sent: Int, now: Long = System.currentTimeMillis())

    @Query("UPDATE messages SET partsDelivered = :delivered, updatedAt = :now WHERE id = :id")
    suspend fun updatePartsDelivered(id: String, delivered: Int, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM messages WHERE createdAt < :before")
    suspend fun deleteOlderThan(before: Long)
}
