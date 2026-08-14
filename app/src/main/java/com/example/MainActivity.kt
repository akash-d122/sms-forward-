package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ui.SmsViewModel
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.screens.SandboxScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val smsPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

    private val hasPermissionsState = mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        hasPermissionsState.value = hasAllSmsPermissions()
    }

    private val viewModel: SmsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        hasPermissionsState.value = hasAllSmsPermissions()

        setContent {
            MyApplicationTheme {
                val hasPermissions by hasPermissionsState

                if (!hasPermissions) {
                    PermissionOnboardingScreen(
                        onGrantClicked = {
                            requestPermissionLauncher.launch(smsPermissions)
                        }
                    )
                } else {
                    MainAppScaffold(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hasPermissionsState.value = hasAllSmsPermissions()
    }

    private fun hasAllSmsPermissions(): Boolean {
        return smsPermissions.all { perm ->
            ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
fun PermissionOnboardingScreen(onGrantClicked: () -> Unit) {
    var hasAgreedToPolicy by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Icon(
                    Icons.Default.Security,
                    contentDescription = "Security Shield",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Security & Privacy Policy",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Transparent SMS Forwarder",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Prominent Disclosure Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Prominent Disclosure",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "• Local execution of SMS routing actions is fully handled on-device.\n\n" +
                                   "• All SMS permissions (RECEIVE_SMS, READ_SMS, and SEND_SMS) are only used to check sender/body rules you define and send telecom carrier messages to your forwarding targets.\n\n" +
                                   "• 100% Offline by Design: This application has NO Internet permission. Your private messages, verification alerts, and financial OTPs physically cannot be sent over the network, uploaded online, or exfiltrated.\n\n" +
                                   "• Fully Auditable: Your routing rules and historic traces are encrypted under standard SQLite local storage.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // User Acceptance Agreement Box
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Checkbox(
                        checked = hasAgreedToPolicy,
                        onCheckedChange = { hasAgreedToPolicy = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I acknowledge that I am configuring this solely for personal forwarding, and understand all routing remains offline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onGrantClicked,
                    enabled = hasAgreedToPolicy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("grant_permissions_button")
                ) {
                    Text("Grant Permissions & Start", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainAppScaffold(viewModel: SmsViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    val tabs = listOf(
        TabData("Rules", Icons.AutoMirrored.Filled.Rule, "tab_rules"),
        TabData("Logs", Icons.Default.History, "tab_logs"),
        TabData("Sandbox", Icons.Default.Science, "tab_sandbox"),
        TabData("Settings", Icons.Default.Settings, "tab_settings")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_navigation_bar")
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "ScreenTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { targetIndex ->
            when (targetIndex) {
                0 -> RulesScreen(viewModel = viewModel)
                1 -> LogsScreen(viewModel = viewModel)
                2 -> SandboxScreen(viewModel = viewModel)
                3 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

data class TabData(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)
