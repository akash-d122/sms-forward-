package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM forwarding_rules ORDER BY priority ASC, id DESC")
    fun getAllRules(): Flow<List<ForwardingRule>>

    @Query("SELECT * FROM forwarding_rules WHERE enabled = 1 ORDER BY priority ASC")
    suspend fun getActiveRules(): List<ForwardingRule>

    @Query("SELECT * FROM forwarding_rules WHERE id = :id")
    suspend fun getRuleById(id: Int): ForwardingRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: ForwardingRule): Long

    @Update
    suspend fun updateRule(rule: ForwardingRule)

    @Delete
    suspend fun deleteRule(rule: ForwardingRule)
}
