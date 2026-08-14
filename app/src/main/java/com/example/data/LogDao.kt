package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM forwarding_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ForwardingLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ForwardingLog): Long

    @Query("DELETE FROM forwarding_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM forwarding_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)
}
