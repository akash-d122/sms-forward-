package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "sms_forwarder_settings",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_MASTER_FORWARDING = "master_forwarding_enabled"
        private const val KEY_SMTP_HOST = "smtp_host"
        private const val KEY_SMTP_PORT = "smtp_port"
        private const val KEY_SMTP_USER = "smtp_user"
        private const val KEY_SMTP_PASS = "smtp_pass"
        private const val KEY_SMTP_FROM = "smtp_from"
        private const val KEY_SMTP_TO_DEFAULT = "smtp_to_default"
    }

    var isMasterForwardingEnabled: Boolean
        get() = prefs.getBoolean(KEY_MASTER_FORWARDING, true)
        set(value) = prefs.edit().putBoolean(KEY_MASTER_FORWARDING, value).apply()

    var smtpHost: String
        get() = prefs.getString(KEY_SMTP_HOST, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SMTP_HOST, value).apply()

    var smtpPort: String
        get() = prefs.getString(KEY_SMTP_PORT, "587") ?: "587"
        set(value) = prefs.edit().putString(KEY_SMTP_PORT, value).apply()

    var smtpUser: String
        get() = prefs.getString(KEY_SMTP_USER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SMTP_USER, value).apply()

    var smtpPass: String
        get() = prefs.getString(KEY_SMTP_PASS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SMTP_PASS, value).apply()

    var smtpFrom: String
        get() = prefs.getString(KEY_SMTP_FROM, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SMTP_FROM, value).apply()

    var smtpToDefault: String
        get() = prefs.getString(KEY_SMTP_TO_DEFAULT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SMTP_TO_DEFAULT, value).apply()
}
