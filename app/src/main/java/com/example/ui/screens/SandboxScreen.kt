package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SmsViewModel
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandboxScreen(
    viewModel: SmsViewModel,
    modifier: Modifier = Modifier
) {
    val sender by viewModel.sandboxSender.collectAsState()
    val body by viewModel.sandboxBody.collectAsState()
    val matchedRules by viewModel.sandboxMatchedRules.collectAsState()
    val rules by viewModel.rules.collectAsState()

    // Standalone Custom Regex Tester State
    var testerRegex by remember { mutableStateOf("(?i)code is (\\d+)") }
    var testerText by remember { mutableStateOf("Your login authorization code is 950211. Valid for 10 minutes.") }
    var testResultMatches by remember { mutableStateOf(false) }
    var testResultToken by remember { mutableStateOf("") }
    var testResultError by remember { mutableStateOf<String?>(null) }

    // Run custom regex evaluation
    LaunchedEffect(testerRegex, testerText) {
        if (testerRegex.isBlank()) {
            testResultMatches = false
            testResultToken = ""
            testResultError = null
            return@LaunchedEffect
        }
        try {
            val pattern = Pattern.compile(testerRegex, Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            val matcher = pattern.matcher(testerText)
            if (matcher.find()) {
                testResultMatches = true
                testResultError = null
                // Extract group 1 if present, otherwise complete match
                testResultToken = if (matcher.groupCount() >= 1) {
                    matcher.group(1) ?: matcher.group()
                } else {
                    matcher.group()
                }
            } else {
                testResultMatches = false
                testResultToken = ""
                testResultError = null
            }
        } catch (e: Exception) {
            testResultMatches = false
            testResultToken = ""
            testResultError = e.message
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Filter Sandbox & Sandbox") })
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General matching card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Science, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SMS Match Tester", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Simulate an incoming SMS message structure downward to debug whether any of your current forwarding rules will activate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = sender,
                        onValueChange = { viewModel.onSandboxInputChanged(it, body) },
                        label = { Text("Simulated Origin Sender") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = body,
                        onValueChange = { viewModel.onSandboxInputChanged(sender, it) },
                        label = { Text("Simulated Message Body") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Matching Rules Outcomes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    
                    if (rules.isEmpty()) {
                        Text(
                            "No rules saved. Go to Rules tab to configure a template.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            rules.forEach { rule ->
                                val matches = matchedRules.contains(rule.name)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .background(if (matches) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (matches) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = "",
                                            tint = if (matches) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            rule.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (matches) FontWeight.Bold else FontWeight.Normal,
                                            color = if (matches) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = if (matches) "Will Forward" else "Skipped",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (matches) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Standalone customRegex utility
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("custom_regex_tester")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Rule, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Custom Regex Playground", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        "Perfect for formulating, previewing and testing complex regular descriptions and groups to extract numeric codes prior to writing actual rules.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = testerRegex,
                        onValueChange = { testerRegex = it },
                        label = { Text("Regex Expression Pattern") },
                        placeholder = { Text("e.g. \\b\\d{6}\\b") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = testerText,
                        onValueChange = { testerText = it },
                        label = { Text("Sandbox Evaluation Text") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // Error output if regex compiled fails
                    if (testResultError != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Syntax Error: ${testResultError}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        // Success display
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .background(if (testResultMatches) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (testResultMatches) Icons.Default.AssignmentTurnedIn else Icons.Default.Cancel,
                                contentDescription = "",
                                tint = if (testResultMatches) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (testResultMatches) "Regex Matched Successfully!" else "No Match Found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (testResultMatches) Color(0xFF1B5E20) else Color(0xFFC62828)
                                )
                                if (testResultMatches && testResultToken.isNotEmpty()) {
                                    Text(
                                        text = "Extracted Code/Result: $testResultToken",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
