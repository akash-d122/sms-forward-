package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ForwardingLog
import com.example.data.ForwardingRule
import com.example.data.SmsRepository
import com.example.helper.RuleMatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class SmsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SmsRepository(application)

    // Master switch & settings state
    private val _isMasterEnabled = MutableStateFlow(repository.settings.isMasterForwardingEnabled)
    val isMasterEnabled = _isMasterEnabled.asStateFlow()

    private val _smtpHostState = MutableStateFlow(repository.settings.smtpHost)
    val smtpHostState = _smtpHostState.asStateFlow()

    private val _smtpPortState = MutableStateFlow(repository.settings.smtpPort)
    val smtpPortState = _smtpPortState.asStateFlow()

    private val _smtpUserState = MutableStateFlow(repository.settings.smtpUser)
    val smtpUserState = _smtpUserState.asStateFlow()

    private val _smtpPassState = MutableStateFlow(repository.settings.smtpPass)
    val smtpPassState = _smtpPassState.asStateFlow()

    private val _smtpFromState = MutableStateFlow(repository.settings.smtpFrom)
    val smtpFromState = _smtpFromState.asStateFlow()

    // Database reactive bindings
    val rules: StateFlow<List<ForwardingRule>> = repository.allRules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val logs: StateFlow<List<ForwardingLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Sandbox Sandbox tester states
    val sandboxSender = MutableStateFlow("HDFC Bank")
    val sandboxBody = MutableStateFlow("Your OTP for txn of Rs 5000 is 439012. Do not share.")
    
    private val _sandboxMatchedRules = MutableStateFlow<List<String>>(emptyList())
    val sandboxMatchedRules = _sandboxMatchedRules.asStateFlow()

    init {
        runSandboxTest()
    }

    fun setMasterEnabled(enabled: Boolean) {
        repository.settings.isMasterForwardingEnabled = enabled
        _isMasterEnabled.value = enabled
    }

    fun saveSmtpSettings(host: String, port: String, user: String, pass: String, from: String) {
        repository.settings.smtpHost = host
        repository.settings.smtpPort = port
        repository.settings.smtpUser = user
        repository.settings.smtpPass = pass
        repository.settings.smtpFrom = from
        
        _smtpHostState.value = host
        _smtpPortState.value = port
        _smtpUserState.value = user
        _smtpPassState.value = pass
        _smtpFromState.value = from
    }

    fun saveRule(rule: ForwardingRule) {
        viewModelScope.launch {
            if (rule.id == 0) {
                repository.insertRule(rule)
            } else {
                repository.updateRule(rule)
            }
            // Trigger sandbox refresh after schedule changes
            runSandboxTest()
        }
    }

    fun toggleRule(rule: ForwardingRule) {
        viewModelScope.launch {
            repository.updateRule(rule.copy(enabled = !rule.enabled))
            runSandboxTest()
        }
    }

    fun deleteRule(rule: ForwardingRule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
            runSandboxTest()
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun deleteLog(log: ForwardingLog) {
        viewModelScope.launch {
            repository.deleteLogById(log.id)
        }
    }

    fun onSandboxInputChanged(sender: String, body: String) {
        sandboxSender.value = sender
        sandboxBody.value = body
        runSandboxTest()
    }

    fun runSandboxTest() {
        viewModelScope.launch {
            val sender = sandboxSender.value
            val body = sandboxBody.value
            val activeRulesList = rules.value
            
            val matchedNames = activeRulesList.filter { rule ->
                RuleMatcher.matches(rule, sender, body, Calendar.getInstance())
            }.map { it.name }
            
            _sandboxMatchedRules.value = matchedNames
        }
    }
}
