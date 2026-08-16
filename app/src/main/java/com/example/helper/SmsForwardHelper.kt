package com.example.helper

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.example.data.ForwardingLog
import com.example.data.ForwardingRule
import com.example.data.SmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

object SmsForwardHelper {
    private const val TAG = "SmsForwardHelper"

    /**
     * Consolidated method to match and forward an incoming SMS message.
     */
    suspend fun processAndForward(
        context: Context,
        sender: String,
        body: String,
        timestamp: Long,
        simSlot: Int
    ) = withContext(Dispatchers.IO) {
        val repository = SmsRepository(context)

        // Safety bypass
        if (!repository.settings.isMasterForwardingEnabled) {
            Log.d(TAG, "Forwarding aborting: Master Switch is Disabled")
            return@withContext
        }

        val rules = repository.getActiveRules()
        val matchedRules = rules.filter { rule ->
            RuleMatcher.matches(rule, sender, body)
        }

        if (matchedRules.isEmpty()) {
            Log.d(TAG, "No match found for incoming message. Storing mis-match trace.")
            repository.insertLog(
                ForwardingLog(
                    ruleId = -1,
                    ruleName = "No Match",
                    incomingSender = sender,
                    incomingSmsBody = body,
                    simSlot = simSlot,
                    targetDestinations = "None",
                    timestamp = timestamp,
                    status = "FILTER_MISMATCH"
                )
            )
            return@withContext
        }

        Log.d(TAG, "Matched ${matchedRules.size} rules for sender: $sender")
        for (rule in matchedRules) {
            forwardMessageForRule(context, rule, sender, body, timestamp, simSlot, repository)
        }
    }

    private suspend fun forwardMessageForRule(
        context: Context,
        rule: ForwardingRule,
        sender: String,
        body: String,
        timestamp: Long,
        simSlot: Int,
        repository: SmsRepository
    ) {
        val destinations = mutableListOf<String>()
        val errors = mutableListOf<String>()

        // 1. Forward via SMS
        if (rule.forwardToNumbers.isNotBlank()) {
            val targets = rule.forwardToNumbers.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (targets.isNotEmpty()) {
                destinations.add("SMS: ${targets.joinToString()}")
                try {
                    val smsManager: SmsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        context.getSystemService(SmsManager::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsManager.getDefault()
                    }
                    val formattedBody = "📲 Fwd: [$sender]\n$body"
                    for (target in targets) {
                        // Handle potential multiple SMS part segmentation
                        val parts = smsManager.divideMessage(formattedBody)
                        if (parts.size > 1) {
                            smsManager.sendMultipartTextMessage(target, null, parts, null, null)
                        } else {
                            smsManager.sendTextMessage(target, null, formattedBody, null, null)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending SMS forward: ${e.message}")
                    errors.add("SMS fail: ${e.localizedMessage ?: "Unknown"}")
                }
            }
        }

        // 2. Email, Webhook, and Telegram forwarding are removed to focus purely on offline telecom SMS forwarding

        val status = if (errors.isEmpty()) "SUCCESS" else "FAILED"
        val errorSummary = if (errors.isEmpty()) null else errors.joinToString("; ")

        // Save delivery output log trace in DB
        repository.insertLog(
            ForwardingLog(
                ruleId = rule.id,
                ruleName = rule.name,
                incomingSender = sender,
                incomingSmsBody = body,
                simSlot = simSlot,
                targetDestinations = destinations.joinToString().ifEmpty { "None Selected" },
                timestamp = timestamp,
                status = status,
                errorMessage = errorSummary
            )
        )
    }

    private fun htmlEscape(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }
}
