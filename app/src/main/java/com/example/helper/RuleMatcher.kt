package com.example.helper

import android.util.Log
import com.example.data.ForwardingRule
import java.util.Calendar
import java.util.regex.Pattern

object RuleMatcher {
    private const val TAG = "RuleMatcher"

    /**
     * Checks if a rule matches the incoming SMS details.
     * Returns true if indeed matched, false otherwise.
     */
    fun matches(
        rule: ForwardingRule,
        sender: String,
        body: String,
        calendar: Calendar = Calendar.getInstance()
    ): Boolean {
        // 1. Check Schedule
        if (!rule.enabled) return false
        if (!isScheduleActive(rule, calendar)) {
            Log.d(TAG, "Rule '${rule.name}' skipped: Schedule not active currently.")
            return false
        }

        val hasSender = rule.senderFilter.isNotBlank()
        val hasKeyword = rule.keywordFilter.isNotBlank()
        val hasOtp = rule.matchOtpType != "NONE"

        // 2. Sender filter check
        val senderMatched = if (hasSender) checkSenderMatch(rule, sender) else false

        // 3. Keyword filter check
        val keywordMatched = if (hasKeyword) checkKeywordMatch(rule, body) else false

        // 4. Regex / OTP Match
        val regexMatched = if (hasOtp) checkRegexMatch(rule, body) else false

        // 5. Combine results based on logical operators (AND/OR)
        val basicFilterMatched = when {
            hasSender && hasKeyword -> {
                if (rule.filterLogic == "OR") {
                    senderMatched || keywordMatched
                } else {
                    senderMatched && keywordMatched
                }
            }
            hasSender -> senderMatched
            hasKeyword -> keywordMatched
            else -> true // both basic filters (sender & keyword) are empty, so matches basic filter checks
        }

        return if (hasOtp) {
            if (rule.filterLogic == "OR") {
                basicFilterMatched || regexMatched
            } else {
                basicFilterMatched && regexMatched
            }
        } else {
            basicFilterMatched
        }
    }

    /**
     * Parse calendar and check is within rules active hour window and weekday
     */
    fun isScheduleActive(rule: ForwardingRule, calendar: Calendar): Boolean {
        // Validate Weekday
        val calendarDay = calendar.get(Calendar.DAY_OF_WEEK)
        // Map SUNDAY = 1, MONDAY = 2 -> ISO values: 1=Mon, 7=Sun
        val isoWeekday = when (calendarDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }

        val activeDays = rule.weekdays.split(",").mapNotNull { it.trim().toIntOrNull() }
        if (activeDays.isNotEmpty() && !activeDays.contains(isoWeekday)) {
            return false
        }

        // Validate Clock/Time active window
        val startStr = rule.startTime.trim()
        val endStr = rule.endTime.trim()
        if (startStr.isEmpty() || endStr.isEmpty()) {
            return true // Always active if time window details are blank
        }

        try {
            val startParts = startStr.split(":")
            val endParts = endStr.split(":")
            if (startParts.size < 2 || endParts.size < 2) return true

            val startMin = startParts[0].toInt() * 60 + startParts[1].toInt()
            val endMin = endParts[0].toInt() * 60 + endParts[1].toInt()
            
            val currentMin = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

            return if (startMin < endMin) {
                currentMin in startMin..endMin
            } else {
                // Overnight window (e.g. 21:00 to 06:00)
                currentMin >= startMin || currentMin <= endMin
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing rule schedule time for '${rule.name}': ${e.message}")
            return true
        }
    }

    private fun checkSenderMatch(rule: ForwardingRule, incomingSender: String): Boolean {
        if (rule.senderFilter.isBlank()) return true
        val senders = rule.senderFilter.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (senders.isEmpty()) return true

        return senders.any { filter ->
            incomingSender.contains(filter, ignoreCase = true)
        }
    }

    private fun checkKeywordMatch(rule: ForwardingRule, body: String): Boolean {
        if (rule.keywordFilter.isBlank()) return true
        val keywords = rule.keywordFilter.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (keywords.isEmpty()) return true

        if (rule.filterLogic == "OR") {
            return keywords.any { kw -> body.contains(kw, ignoreCase = true) }
        } else {
            return keywords.all { kw -> body.contains(kw, ignoreCase = true) }
        }
    }

    fun checkRegexMatch(rule: ForwardingRule, body: String): Boolean {
        val patternStr = getRegexPatternForType(rule.matchOtpType, rule.regexPattern) ?: return true
        return try {
            val pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            val matcher = pattern.matcher(body)
            matcher.find()
        } catch (e: Exception) {
            Log.e(TAG, "Invalid Regex logic in rule '${rule.name}': ${e.message}")
            false
        }
    }

    /**
     * Resolves key regex string values corresponding to selected predefined types or custom
     */
    fun getRegexPatternForType(type: String, customPattern: String): String? {
        return when (type) {
            "OTP_4" -> "\\b\\d{4}\\b"
            "OTP_6" -> "\\b\\d{6}\\b"
            "OTP_8" -> "\\b\\d{8}\\b"
            "OTP_GENERIC" -> "(?is)(otp|verification|authentication|security|one-time|code)[\\s\\S]*?\\b\\d{4,8}\\b"
            "CUSTOM_REGEX" -> if (customPattern.isNotBlank()) customPattern else null
            else -> null
        }
    }
}
