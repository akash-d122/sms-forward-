package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class SmsRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val ruleDao = db.ruleDao()
    private val logDao = db.logDao()
    val settings = SettingsHelper(context)

    // Rules
    val allRules: Flow<List<ForwardingRule>> = ruleDao.getAllRules()

    suspend fun getRuleById(id: Int): ForwardingRule? {
        return ruleDao.getRuleById(id)
    }

    suspend fun getActiveRules(): List<ForwardingRule> {
        return ruleDao.getActiveRules()
    }

    suspend fun insertRule(rule: ForwardingRule): Long {
        return ruleDao.insertRule(rule)
    }

    suspend fun updateRule(rule: ForwardingRule) {
        ruleDao.updateRule(rule)
    }

    suspend fun deleteRule(rule: ForwardingRule) {
        ruleDao.deleteRule(rule)
    }

    // Logs
    val allLogs: Flow<List<ForwardingLog>> = logDao.getAllLogs()

    suspend fun insertLog(log: ForwardingLog): Long {
        return logDao.insertLog(log)
    }

    suspend fun clearLogs() {
        logDao.clearAllLogs()
    }

    suspend fun deleteLogById(id: Int) {
        logDao.deleteLogById(id)
    }
}
