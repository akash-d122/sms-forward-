package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forwarding_rules")
data class ForwardingRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val enabled: Boolean = true,
    
    // Filters
    val senderFilter: String = "",       // Comma-separated sender phone numbers or contact names, empty matches all
    val keywordFilter: String = "",      // Comma-separated keywords in text, empty matches all
    val regexPattern: String = "",       // Custom regex pattern, empty matches all
    val filterLogic: String = "AND",     // "AND" or "OR" logic combined with OTP filters
    
    // Predefined OTP configuration
    val matchOtpType: String = "NONE",   // "NONE", "OTP_4", "OTP_6", "OTP_8", "OTP_GENERIC", "CUSTOM_REGEX"
    
    // Forwarding Destinations
    val forwardToNumbers: String = "",   // Comma-separated target phone numbers
    val forwardToEmails: String = "",    // Comma-separated recipient emails
    val forwardToWebhook: String = "",   // HTTP Webhook URL
    val forwardToTelegram: String = "",  // Telegram Bot integration format: "bot_token|chat_id"
    
    // Order/Priority
    val priority: Int = 1,
    
    // Schedule/Timing (e.g. Mon-Fri or 09:00 - 18:00)
    val weekdays: String = "1,2,3,4,5,6,7", // Comma-separated week indices (1=Mon, 7=Sun)
    val startTime: String = "",          // Format "HH:mm", empty means always active
    val endTime: String = ""             // Format "HH:mm", empty means always active
)
