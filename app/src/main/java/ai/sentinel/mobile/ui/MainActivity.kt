package ai.sentinel.mobile.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ai.sentinel.mobile.SentinelApplication
import ai.sentinel.mobile.data.local.EncryptedSessionManager
import ai.sentinel.mobile.service.NotificationHelper
import ai.sentinel.mobile.ui.navigation.BottomNavTab
import ai.sentinel.mobile.ui.navigation.Screen
import ai.sentinel.mobile.ui.screens.alerts.AlertsQueueScreen
import ai.sentinel.mobile.ui.screens.alerts.AlertsViewModel
import ai.sentinel.mobile.ui.screens.auth.LoginScreen
import ai.sentinel.mobile.ui.screens.auth.LoginViewModel
import ai.sentinel.mobile.ui.screens.logs.LogsScreen
import ai.sentinel.mobile.ui.screens.logs.LogsViewModel
import ai.sentinel.mobile.ui.screens.orgs.OrganizationSelectionDialog
import ai.sentinel.mobile.ui.screens.settings.SettingsScreen
import ai.sentinel.mobile.ui.theme.CardElevation1
import ai.sentinel.mobile.ui.theme.CardElevation2
import ai.sentinel.mobile.ui.theme.CobaltCore
import ai.sentinel.mobile.ui.theme.DeepObsidian
import ai.sentinel.mobile.ui.theme.ElectricBlue
import ai.sentinel.mobile.ui.theme.SentinelTheme
import ai.sentinel.mobile.ui.theme.SurfaceBorder
import ai.sentinel.mobile.ui.theme.TextMuted
import ai.sentinel.mobile.ui.theme.TextPrimary
import ai.sentinel.mobile.ui.theme.TextSecondary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var pendingAlertId: Int? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // User responded to POST_NOTIFICATIONS permission request
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if opened via notification deep link
        handleNotificationIntent(intent)

        // Request POST_NOTIFICATIONS permission on Android 13+
        checkNotificationPermission()

        setContent {
            val app = application as SentinelApplication
            SentinelTheme {
                MainAppContent(
                    app = app,
                    pendingAlertId = pendingAlertId,
                    onClearPendingAlert = { pendingAlertId = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val alertId = intent?.getIntExtra(NotificationHelper.EXTRA_ALERT_ID, -1) ?: -1
        if (alertId > 0) {
            pendingAlertId = alertId
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun MainAppContent(
    app: SentinelApplication,
    pendingAlertId: Int?,
    onClearPendingAlert: () -> Unit
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val isUserLoggedIn by app.authRepository.isUserLoggedInFlow.collectAsState()
    val currentOrgId by app.authRepository.currentOrgIdFlow.collectAsState()

    var showOrgDialog by remember { mutableStateOf(false) }

    // ViewModels
    val loginViewModel = remember {
        LoginViewModel(app.authRepository, app.deviceRepository)
    }
    val alertsViewModel = remember {
        AlertsViewModel(app.alertsRepository)
    }
    val logsViewModel = remember {
        LogsViewModel(app.logsRepository)
    }

    // Handle deep link alert navigation when logged in
    LaunchedEffect(pendingAlertId, isUserLoggedIn) {
        if (isUserLoggedIn && pendingAlertId != null && pendingAlertId > 0) {
            alertsViewModel.openAlertDetail(pendingAlertId)
            onClearPendingAlert()
        }
    }

    // Tenant Switcher Dialog
    if (showOrgDialog) {
        OrganizationSelectionDialog(
            authRepository = app.authRepository,
            currentOrgId = currentOrgId,
            onDismiss = { showOrgDialog = false },
            onOrganizationSelected = {
                // Refresh alerts and logs for new tenant
                alertsViewModel.loadAlerts()
                logsViewModel.loadLogs()
            }
        )
    }

    if (!isUserLoggedIn) {
        val loginUiState by loginViewModel.uiState.collectAsState()
        LoginScreen(
            uiState = loginUiState,
            sessionManager = app.sessionManager,
            onLoginClick = { email, pass ->
                loginViewModel.login(email, pass)
            }
        )
    } else {
        var currentTab by remember { mutableStateOf<BottomNavTab>(BottomNavTab.Alerts) }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = CardElevation1,
                    modifier = Modifier
                        .height(64.dp)
                        .border(1.dp, SurfaceBorder)
                ) {
                    BottomNavTab.items.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElectricBlue,
                                selectedTextColor = ElectricBlue,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextMuted,
                                indicatorColor = CardElevation2
                            )
                        )
                    }
                }
            },
            containerColor = DeepObsidian
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    BottomNavTab.Alerts -> {
                        val alertsUiState by alertsViewModel.uiState.collectAsState()
                        AlertsQueueScreen(
                            uiState = alertsUiState,
                            organizationName = app.authRepository.selectedOrgName,
                            onOrganizationClick = { showOrgDialog = true },
                            onSettingsClick = { currentTab = BottomNavTab.Settings },
                            onRefresh = { alertsViewModel.loadAlerts() },
                            onAlertClick = { alertId -> alertsViewModel.openAlertDetail(alertId) },
                            onSeverityFilterChange = { severity -> alertsViewModel.setSeverityFilter(severity) },
                            onSearchQueryChange = { query -> alertsViewModel.setSearchQuery(query) },
                            onDismissDetail = { alertsViewModel.closeAlertDetail() }
                        )
                    }

                    BottomNavTab.Logs -> {
                        val logsUiState by logsViewModel.uiState.collectAsState()
                        LogsScreen(
                            uiState = logsUiState,
                            organizationName = app.authRepository.selectedOrgName,
                            onOrganizationClick = { showOrgDialog = true },
                            onSettingsClick = { currentTab = BottomNavTab.Settings },
                            onRefresh = { logsViewModel.loadLogs() },
                            onLogClick = { log -> logsViewModel.selectLog(log) },
                            onSeverityFilterChange = { severity -> logsViewModel.setSeverityFilter(severity) },
                            onSearchQueryChange = { query -> logsViewModel.setSearchQuery(query) },
                            onDismissDetail = { logsViewModel.dismissLogDetail() }
                        )
                    }

                    BottomNavTab.Settings -> {
                        SettingsScreen(
                            sessionManager = app.sessionManager,
                            authRepository = app.authRepository,
                            onLogoutClick = {
                                scope.launch {
                                    app.authRepository.logout()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
