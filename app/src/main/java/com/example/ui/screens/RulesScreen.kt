package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ForwardingRule
import com.example.ui.SmsViewModel
import com.example.ui.components.RuleEditDialog
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    viewModel: SmsViewModel,
    modifier: Modifier = Modifier
) {
    val rules by viewModel.rules.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val isMasterEnabled by viewModel.isMasterEnabled.collectAsState()

    var editingRule by remember { mutableStateOf<ForwardingRule?>(null) }
    var isCreating by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Live calculations for the Elegant Dark stats cards
    val todaySuccessCount = remember(logs) {
        val cal = Calendar.getInstance()
        val todayYear = cal.get(Calendar.YEAR)
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)
        logs.count { log ->
            val logCal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
            logCal.get(Calendar.YEAR) == todayYear && 
            logCal.get(Calendar.DAY_OF_YEAR) == todayDay && 
            log.status == "SUCCESS"
        }
    }

    // Determine Whitelist / Battery Exemption live state
    val isBatteryExempt = remember {
        val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager
        if (pm != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreating = true },
                containerColor = MaterialTheme.colorScheme.primary, // #D0BCFF Lavender
                contentColor = MaterialTheme.colorScheme.onPrimary, // #381E72 Deep Violet
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .testTag("add_rule_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Rule", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background, // #1A1C1E Slate Dark
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Elegant Dark Header Component
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "SMS Forwarder",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    // Service Active glowing indicator row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // Glowing Dot Indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isMasterEnabled) MaterialTheme.colorScheme.secondary 
                                    else Color(0xFFFFB4AB)
                                )
                                .shadow(
                                    elevation = if (isMasterEnabled) 6.dp else 0.dp,
                                    shape = CircleShape,
                                    clip = false,
                                    ambientColor = MaterialTheme.colorScheme.secondary,
                                    spotColor = MaterialTheme.colorScheme.secondary
                                )
                        )
                        Text(
                            text = if (isMasterEnabled) "SERVICE ACTIVE" else "SERVICE PAUSED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (isMasterEnabled) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            else Color(0xFFFFB4AB)
                        )
                    }
                }

                // Interactive Custom Toggle representation (or master switch)
                Switch(
                    checked = isMasterEnabled,
                    onCheckedChange = { viewModel.setMasterEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("master_monitor_switch")
                )
            }

            // Stats Double Grid Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today Message Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "TODAY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = todaySuccessCount.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.primary // #D0BCFF
                            )
                        )
                        Text(
                            text = "Messages Sent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Battery Card / Exemption Status Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (!isBatteryExempt) {
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = android.net.Uri.parse("package:${context.packageName}")
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    try {
                                        val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (ex: Exception) {
                                        android.util.Log.e("RulesScreen", "Failed to launch battery settings", ex)
                                    }
                                }
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "BATTERY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBatteryExempt) "0.4%" else "Limited",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Light,
                                color = if (isBatteryExempt) MaterialTheme.colorScheme.secondary else Color(0xFFFFB4AB) // #B6EEA9 pistachio green or error red
                            )
                        )
                        Text(
                            text = if (isBatteryExempt) "Daily Usage" else "Needs Whitelist",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Rules Header Line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE RULES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Rules Total: ${rules.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Rules List
            if (rules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MobileOff,
                            contentDescription = "",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Forwarding Rules Created",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap the '+' button down below to configure your first filter matching criteria or OTP forwarding rule.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(rules, key = { it.id }) { rule ->
                        RuleCard(
                            rule = rule,
                            onToggle = { viewModel.toggleRule(rule) },
                            onEdit = { editingRule = rule },
                            onDelete = { viewModel.deleteRule(rule) }
                        )
                    }
                }
            }
        }
    }

    // Modal dialogue controllers
    if (isCreating) {
        RuleEditDialog(
            rule = null,
            onDismiss = { isCreating = false },
            onSave = { rule ->
                viewModel.saveRule(rule)
                isCreating = false
            }
        )
    }

    if (editingRule != null) {
        RuleEditDialog(
            rule = editingRule,
            onDismiss = { editingRule = null },
            onSave = { rule ->
                viewModel.saveRule(rule)
                editingRule = null
            }
        )
    }
}

@Composable
fun RuleCard(
    rule: ForwardingRule,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("rule_card_${rule.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // #2E3033 Deep variant
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // Forces Box (vertical border indicator) to stretch perfectly
        ) {
            // Elegant vertical stripe highlight (Tailwind border-l-4 style)
            val accentColor = if (rule.enabled) {
                if (rule.matchOtpType != "NONE") MaterialTheme.colorScheme.primary // Lavender #D0BCFF
                else MaterialTheme.colorScheme.secondary // Pistachio Green #B6EEA9
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            }
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            // Primary Card Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Header: Title and active switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rule.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Priority badge
                            SuggestionChip(
                                onClick = {},
                                label = { Text("P${rule.priority}", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                    Switch(
                        checked = rule.enabled,
                        onCheckedChange = { onToggle() },
                        modifier = Modifier.testTag("rule_toggle_${rule.id}")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Destinations details row with elegant alignment
                val destinationsText = remember(rule) {
                    if (rule.forwardToNumbers.isNotBlank()) "Forward via SMS to: ${rule.forwardToNumbers}" else "No Destinations Configured"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "",
                        modifier = Modifier.size(14.dp),
                        tint = if (rule.enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = destinationsText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (rule.enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rule filter tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (rule.matchOtpType != "NONE") {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "OTP: ${rule.matchOtpType}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (rule.senderFilter.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Sender: ${rule.senderFilter}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Schedule and action button toolbar
                val scheduleText = if (rule.startTime.isNotBlank() && rule.endTime.isNotBlank()) {
                    "${rule.startTime}-${rule.endTime}"
                } else {
                    "24h Active"
                }

                val daysMap = mapOf(1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun")
                val activeDaysIndices = rule.weekdays.split(",").mapNotNull { it.trim().toIntOrNull() }
                val daysText = if (activeDaysIndices.size == 7) {
                    "Everyday"
                } else if (activeDaysIndices.sorted() == listOf(1, 2, 3, 4, 5)) {
                    "Mon-Fri"
                } else {
                    activeDaysIndices.sorted().map { daysMap[it] ?: "" }.joinToString()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Schedule",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$daysText ($scheduleText)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Toolbar buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Rule",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Rule",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
