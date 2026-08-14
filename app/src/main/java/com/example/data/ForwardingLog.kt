package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forwarding_logs")
data class ForwardingLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ruleId: Int = -1,
    val ruleName: String = "",
    val incomingSender: String,
    val incomingSmsBody: String,
    val simSlot: Int = -1,              // SIM Slot index (e.g., 0 for SIM1, 1 for SIM2, -1 for unknown)
    val targetDestinations: String,     // Human readable string of where we forwarded (e.g. "SMS, Telegram")
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,                  // "SUCCESS", "FAILED", "FILTER_MISMATCH"
    val errorMessage: String? = null
)
