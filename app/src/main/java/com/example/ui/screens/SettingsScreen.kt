package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.SmsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SmsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val host by viewModel.smtpHostState.collectAsState()
    val port by viewModel.smtpPortState.collectAsState()
    val user by viewModel.smtpUserState.collectAsState()
    val pass by viewModel.smtpPassState.collectAsState()
    val from by viewModel.smtpFromState.collectAsState()

    var hostInput by remember(host) { mutableStateOf(host) }
    var portInput by remember(port) { mutableStateOf(port) }
    var userInput by remember(user) { mutableStateOf(user) }
    var passInput by remember(pass) { mutableStateOf(pass) }
    var fromInput by remember(from) { mutableStateOf(from) }

    var passwordVisible by remember { mutableStateOf(false) }
    var isBatteryExempt by remember { mutableStateOf(checkBatteryOptimizationExempt(context)) }

    // Recheck battery optimization when entering screen
    LaunchedEffect(Unit) {
        isBatteryExempt = checkBatteryOptimizationExempt(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("App Settings & SMTP Setup") })
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
            
            // Battery Optimization Control Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isBatteryExempt) Icons.Default.CheckCircle else Icons.Default.BatteryAlert,
                            contentDescription = "",
                            tint = if (isBatteryExempt) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Battery Optimization Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = if (isBatteryExempt) {
                            "Status: EXEMPT (Optimal)\nThe system will not suspend or delay SMS forwarding tasks even if the app receives multiple incoming triggers."
                        } else {
                            "Status: CONTROLLED (Sub-optimal)\nAndroid may sleep or delay network requests when the device is idle or screen is off. We recommend whitelisting the app."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isBatteryExempt) {
                        Button(
                            onClick = {
                                requestBatteryExemption(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth().testTag("optimize_battery_button")
                        ) {
                            Icon(Icons.Default.BatterySaver, contentDescription = "")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Disable Battery Optimization")
                        }
                    }
                }
            }

            // Offline SMS Forwarding Guidelines Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline SMS Forwarding Guide",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "This app is engineered to operate 100% offline, guaranteeing robust security. It forwards your messages using cellular SMS capabilities and requires no internet access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text("1. ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Active SIM Card Fitted", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text("The device must have an active SIM card inserted with cellular coverage.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(verticalAlignment = Alignment.Top) {
                            Text("2. ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Telecom Outgoing SMS Balance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text("Make sure your cellular subscription has an active SMS pack or standard credit balance for sending local context SMS forwards.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(verticalAlignment = Alignment.Top) {
                            Text("3. ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Reliable Background Execution", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text("If background forwarding fails while the screen is off, confirm that battery optimizations have been disabled using the exemption toggle above.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

private const val PasswordTransformation = '*'

// Helper functions for Power battery whitelisting
private fun checkBatteryOptimizationExempt(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        pm.isIgnoringBatteryOptimizations(context.packageName)
    } else {
        true
    }
}

private fun requestBatteryExemption(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to normal settings screen if intent fails on some manufacturers
            try {
                val settingsIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Could not open battery settings manually.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
