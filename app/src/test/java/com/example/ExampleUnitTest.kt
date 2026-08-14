package com.example

import com.example.data.ForwardingRule
import com.example.helper.RuleMatcher
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/**
 * Robust Unit Tests for the SMS Forwarder OTP Detection, Regex Filtering,
 * and Scheduling Engine.
 */
class ExampleUnitTest {

    @Test
    fun testFourDigitOtpMatching() {
        val rule = ForwardingRule(
            name = "Test 4-Digit OTP",
            matchOtpType = "OTP_4"
        )
        // Cases that should match
        assertTrue(RuleMatcher.checkRegexMatch(rule, "Your code is 1234"))
        assertTrue(RuleMatcher.checkRegexMatch(rule, "Verification prompt 9821."))
        
        // Cases that should NOT match (not 4 digits)
        assertFalse(RuleMatcher.checkRegexMatch(rule, "Your code is 123"))
        assertFalse(RuleMatcher.checkRegexMatch(rule, "Verification prompt 54321."))
    }

    @Test
    fun testSixDigitOtpMatching() {
        val rule = ForwardingRule(
            name = "Test 6-Digit OTP",
            matchOtpType = "OTP_6"
        )
        // Cases that should match
        assertTrue(RuleMatcher.checkRegexMatch(rule, "Your HDFC bank OTP is 738210. Valid for 5 mins."))
        assertTrue(RuleMatcher.checkRegexMatch(rule, "Code: 109283"))
        
        // Cases that should NOT match
        assertFalse(RuleMatcher.checkRegexMatch(rule, "OTP is 12345"))
        assertFalse(RuleMatcher.checkRegexMatch(rule, "The transaction reference is 912837482."))
    }

    @Test
    fun testEightDigitOtpMatching() {
        val rule = ForwardingRule(
            name = "Test 8-Digit OTP",
            matchOtpType = "OTP_8"
        )
        // Cases that should match
        assertTrue(RuleMatcher.checkRegexMatch(rule, "Security activation code: 82910293."))
        
        // Cases that should NOT match
        assertFalse(RuleMatcher.checkRegexMatch(rule, "Code is 123456"))
    }

    @Test
    fun testGenericOtpDetectionMatching() {
        val rule = ForwardingRule(
            name = "Test Generic OTP Rules",
            matchOtpType = "OTP_GENERIC"
        )
        
        // Messages containing common key words and a 4-8 digit number
        assertTrue(RuleMatcher.checkRegexMatch(rule, "Your verification code is: 5543"))
        assertTrue(RuleMatcher.checkRegexMatch(rule, "One-Time Password is 920192"))
        assertTrue(RuleMatcher.checkRegexMatch(rule, "A security code has been sent: 94021"))
        assertTrue(RuleMatcher.checkRegexMatch(rule, "Your auth code is 88310"))

        // Does not contain OTP context, should fail generic check
        assertFalse(RuleMatcher.checkRegexMatch(rule, "Hi, can you meet me at 1234 Park Street?"))
    }

    @Test
    fun testCustomRegexMatching() {
        val rule = ForwardingRule(
            name = "Test Custom Regex",
            matchOtpType = "CUSTOM_REGEX",
            regexPattern = "(?i)ref-[a-z]{3}-\\d{3}"
        )

        assertTrue(RuleMatcher.checkRegexMatch(rule, "Please find your item REF-abc-123 in standard list."))
        assertFalse(RuleMatcher.checkRegexMatch(rule, "Please find your item REF-ab-123 in list."))
    }

    @Test
    fun testFilterLogicCombinations() {
        // Test AND logical combination
        val andRule = ForwardingRule(
            name = "AND Filter Rule",
            senderFilter = "HDFC",
            keywordFilter = "Alert, OTP",
            filterLogic = "AND",
            matchOtpType = "OTP_6"
        )

        // Perfect match
        val matched = RuleMatcher.matches(andRule, "HDFC Bank", "ALERT: Your OTP code is 192801")
        assertTrue(matched)

        // Missing keyword Alert -> should fail AND
        val failKeyword = RuleMatcher.matches(andRule, "HDFC Bank", "Your OTP code is 192801")
        assertFalse(failKeyword)

        // Test OR logical combination
        val orRule = ForwardingRule(
            name = "OR Filter Rule",
            senderFilter = "Amazon",
            keywordFilter = "Package",
            filterLogic = "OR",
            matchOtpType = "NONE"
        )

        // Match sender only
        assertTrue(RuleMatcher.matches(orRule, "Amazon Inc", "Your order has been shipped."))
        // Match keyword only
        assertTrue(RuleMatcher.matches(orRule, "FedEx Courier", "Your FedEx Package is near."))
    }

    @Test
    fun testActiveTimeSchedules() {
        val rule = ForwardingRule(
            name = "Working Hours Rule",
            startTime = "09:00",
            endTime = "18:00",
            weekdays = "1,2,3,4,5" // Mon-Fri
        )

        // Test Monday 10:00 -> within schedule
        val mondayTen = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
        }
        assertTrue(RuleMatcher.isScheduleActive(rule, mondayTen))

        // Test Monday 20:00 -> after hours
        val mondayEightPm = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
        }
        assertFalse(RuleMatcher.isScheduleActive(rule, mondayEightPm))

        // Test Saturday 10:00 -> wrong day
        val saturdayTen = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
        }
        assertFalse(RuleMatcher.isScheduleActive(rule, saturdayTen))
    }

    @Test
    fun testOvernightActiveTimeSchedules() {
        val rule = ForwardingRule(
            name = "Overnight Rule",
            startTime = "21:00",
            endTime = "06:00",
            weekdays = "1,2,3,4,5,6,7"
        )

        // Test 23:00 -> active overnight
        val elevenPm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 0)
        }
        assertTrue(RuleMatcher.isScheduleActive(rule, elevenPm))

        // Test 03:00 -> active overnight
        val threeAm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 0)
        }
        assertTrue(RuleMatcher.isScheduleActive(rule, threeAm))

        // Test 12:00 -> inactive overnight
        val noon = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }
        assertFalse(RuleMatcher.isScheduleActive(rule, noon))
    }
}
