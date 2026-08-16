package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ForwardingRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditDialog(
    rule: ForwardingRule?,
    onDismiss: () -> Unit,
    onSave: (ForwardingRule) -> Unit
) {
    var name by remember { mutableStateOf(rule?.name ?: "") }
    var enabled by remember { mutableStateOf(rule?.enabled ?: true) }
    
    var senderFilter by remember { mutableStateOf(rule?.senderFilter ?: "") }
    var keywordFilter by remember { mutableStateOf(rule?.keywordFilter ?: "") }
    var regexPattern by remember { mutableStateOf(rule?.regexPattern ?: "") }
    var filterLogic by remember { mutableStateOf(rule?.filterLogic ?: "AND") }
    var matchOtpType by remember { mutableStateOf(rule?.matchOtpType ?: "NONE") }
    
    var forwardToNumbers by remember { mutableStateOf(rule?.forwardToNumbers ?: "") }
    var forwardToEmails by remember { mutableStateOf(rule?.forwardToEmails ?: "") }
    var forwardToWebhook by remember { mutableStateOf(rule?.forwardToWebhook ?: "") }
    var forwardToTelegram by remember { mutableStateOf(rule?.forwardToTelegram ?: "") }
    
    var priorityStr by remember { mutableStateOf(rule?.priority?.toString() ?: "1") }
    
    // Schedule state: 1 to 7 (Mon=1, Sun=7)
    val defaultDays = rule?.weekdays?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: listOf(1, 2, 3, 4, 5, 6, 7)
    val selectedWeekdays = remember { mutableStateListOf<Int>().apply { addAll(defaultDays) } }
    
    var startTime by remember { mutableStateOf(rule?.startTime ?: "") }
    var endTime by remember { mutableStateOf(rule?.endTime ?: "") }

    var showError by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    // Dropdown expanded states
    var otpDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(if (rule == null) "Create Forwarding Rule" else "Edit Forwarding Rule") },
                        actions = {
                            Row(
                                modifier = Modifier.padding(end = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Enabled", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = enabled,
                                    onCheckedChange = { enabled = it },
                                    modifier = Modifier.testTag("rule_enable_switch")
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    Surface(
                        tonalElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    if (name.isBlank()) {
                                        errorMsg = "Rule Name is required"
                                        showError = true
                                        return@Button
                                    }
                                    if (forwardToNumbers.isBlank()) {
                                        errorMsg = "Configure at least one destination phone number for SMS forwarding."
                                        showError = true
                                        return@Button
                                    }
                                    
                                    val compiledRule = ForwardingRule(
                                        id = rule?.id ?: 0,
                                        name = name.trim(),
                                        enabled = enabled,
                                        senderFilter = senderFilter.trim(),
                                        keywordFilter = keywordFilter.trim(),
                                        regexPattern = regexPattern.trim(),
                                        filterLogic = filterLogic,
                                        matchOtpType = matchOtpType,
                                        forwardToNumbers = forwardToNumbers.trim(),
                                        forwardToEmails = "",
                                        forwardToWebhook = "",
                                        forwardToTelegram = "",
                                        priority = priorityStr.toIntOrNull() ?: 1,
                                        weekdays = selectedWeekdays.sorted().joinToString(","),
                                        startTime = startTime.trim(),
                                        endTime = endTime.trim()
                                    )
                                    onSave(compiledRule)
                                },
                                modifier = Modifier.testTag("rule_save_button")
                            ) {
                                Text("Save Rule")
                            }
                        }
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (showError) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "Error", tint = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(errorMsg, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Section: Basic Details
                    Text("Basic Information", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Rule Name *") },
                        placeholder = { Text("e.g. HDFC 6-Digit OTP Forwarder") },
                        modifier = Modifier.fillMaxWidth().testTag("rule_name_input"),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = priorityStr,
                        onValueChange = { priorityStr = it },
                        label = { Text("Priority (Lower executes first)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    HorizontalDivider()

                    // Section: Filter Details
                    Text("Trigger Filter Criteria", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Leave blank to match all incoming messages, or configure combined conditions below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = senderFilter,
                        onValueChange = { senderFilter = it },
                        label = { Text("Sender Names or Numbers") },
                        placeholder = { Text("e.g. HDFC Bank, +919000101") },
                        supportingText = { Text("Comma-separated. Matches anyway if any contains input") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = keywordFilter,
                        onValueChange = { keywordFilter = it },
                        label = { Text("Keywords in Message") },
                        placeholder = { Text("e.g. Transaction, Amount, OTP") },
                        supportingText = { Text("Comma-separated keywords") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Filter logical operator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Combination Logic", style = MaterialTheme.typography.bodyMedium)
                            Text("How to combine basic filter elements", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row {
                            FilterChip(
                                selected = filterLogic == "AND",
                                onClick = { filterLogic = "AND" },
                                label = { Text("AND") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = filterLogic == "OR",
                                onClick = { filterLogic = "OR" },
                                label = { Text("OR") }
                            )
                        }
                    }

                    // Predefined OTP/Regex dropdown
                    Column {
                        Text("OTP / Pattern Matching", style = MaterialTheme.typography.bodyMedium)
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            ExposedDropdownMenuBox(
                                expanded = otpDropdownExpanded,
                                onExpandedChange = { otpDropdownExpanded = !otpDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = when (matchOtpType) {
                                        "NONE" -> "None (No regex constraint)"
                                        "OTP_4" -> "4-Digit OTP Indicator (\\b\\d{4}\\b)"
                                        "OTP_6" -> "6-Digit OTP Indicator (\\b\\d{6}\\b)"
                                        "OTP_8" -> "8-Digit OTP Indicator (\\b\\d{8}\\b)"
                                        "OTP_GENERIC" -> "Generic Smart OTP Pattern"
                                        "CUSTOM_REGEX" -> "Custom regular expression (Regex)"
                                        else -> matchOtpType
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Maturity / OTP Template") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = otpDropdownExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = otpDropdownExpanded,
                                    onDismissRequest = { otpDropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("None") },
                                        onClick = { matchOtpType = "NONE"; otpDropdownExpanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("4-Digit OTP") },
                                        onClick = { matchOtpType = "OTP_4"; otpDropdownExpanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("6-Digit OTP") },
                                        onClick = { matchOtpType = "OTP_6"; otpDropdownExpanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("8-Digit OTP") },
                                        onClick = { matchOtpType = "OTP_8"; otpDropdownExpanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Generic Smart OTP Word Filter") },
                                        onClick = { matchOtpType = "OTP_GENERIC"; otpDropdownExpanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Custom Regular Expression (Regex)") },
                                        onClick = { matchOtpType = "CUSTOM_REGEX"; otpDropdownExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    if (matchOtpType == "CUSTOM_REGEX") {
                        OutlinedTextField(
                            value = regexPattern,
                            onValueChange = { regexPattern = it },
                            label = { Text("Custom Regex Pattern") },
                            placeholder = { Text("e.g. (?i)charge.*\\b\\d+\\b") },
                            supportingText = { Text("Make sure to input a valid JVM regex syntax.") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    HorizontalDivider()

                    // Section: Action forward Destinations
                    Text("Forwarding Destination", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Supply target phone numbers for forwarding. This application is completely offline and forwards messages directly via cellular SMS using your SIM telecom balance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = forwardToNumbers,
                        onValueChange = { forwardToNumbers = it },
                        label = { Text("Forward to SMS phone numbers") },
                        placeholder = { Text("e.g. +919999988888, +14155552671") },
                        supportingText = { Text("Comma-separated standard numbers") },
                        modifier = Modifier.fillMaxWidth().testTag("rule_forward_numbers_input"),
                        singleLine = true
                    )

                    HorizontalDivider()

                    // Section: Scheduling / Calendar
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Forwarding Active Window", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }

                    // Weekdays selection
                    Text("Active Days of the Week", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val weekdaysMap = listOf(
                            "M" to 1, "T" to 2, "W" to 3, "T" to 4, "F" to 5, "S" to 6, "S" to 7
                        )
                        weekdaysMap.forEach { (label, dayIdx) ->
                            val isSelected = selectedWeekdays.contains(dayIdx)
                            ElevatedCard(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable {
                                        if (isSelected) {
                                            selectedWeekdays.remove(dayIdx)
                                        } else {
                                            selectedWeekdays.add(dayIdx)
                                        }
                                    },
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Start & End active hours
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time (Format HH:mm)") },
                            placeholder = { Text("09:00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End Time (Format HH:mm)") },
                            placeholder = { Text("18:00") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Text(
                        "Leave both times blank for 24-hours forwarding. Schedules are checked against device local time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
