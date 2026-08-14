package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.data.SettingsHelper
import com.example.helper.SmsForwardHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "SmsReceiver"
        private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        Log.d(TAG, "Inbound SMS detected.")

        // Fast escape if global switch is disabled
        val settings = SettingsHelper(context)
        if (!settings.isMasterForwardingEnabled) {
            Log.d(TAG, "Master forwarding disabled. Ignoring incoming event.")
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Multi-part message reconstruction
        val sender = messages[0].displayOriginatingAddress ?: "Unknown"
        val bodyBuilder = java.lang.StringBuilder()
        for (msg in messages) {
            bodyBuilder.append(msg.displayMessageBody)
        }
        val fullBody = bodyBuilder.toString()
        val timestamp = System.currentTimeMillis()

        // Extract Dual SIM Slot ID if available
        var simSlot = -1
        val extras = intent.extras
        if (extras != null) {
            val keyCandidates = listOf("slot", "simId", "simSlot", "slot_id", "phone", "subscription")
            for (key in keyCandidates) {
                if (extras.containsKey(key)) {
                    val value = extras.get(key)
                    if (value is Int) {
                        simSlot = value
                        break
                    } else if (value is Long) {
                        simSlot = value.toInt()
                        break
                    } else if (value is String) {
                        simSlot = value.toIntOrNull() ?: -1
                        if (simSlot != -1) break
                    }
                }
            }
        }

        Log.d(TAG, "Executing immediate foreground dispatch -> Sender: $sender, SIM: $simSlot")

        // Intercept and process using goAsync() to keep this receiver's process alive
        // at foreground system priority for up to 10 seconds of reliable execution.
        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                SmsForwardHelper.processAndForward(
                    context = context.applicationContext,
                    sender = sender,
                    body = fullBody,
                    timestamp = timestamp,
                    simSlot = simSlot
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error processing Sms in receiver Scope: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
