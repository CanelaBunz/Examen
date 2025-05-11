package com.example.examen_2ndo.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: Message)

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): List<Message> // Cambiado a función suspend si prefieres

    // Alternativa recomendada usando Flow
    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<Message>>

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}