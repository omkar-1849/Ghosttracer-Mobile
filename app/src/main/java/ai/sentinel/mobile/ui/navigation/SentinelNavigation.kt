package ai.sentinel.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
}

sealed class BottomNavTab(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Alerts : BottomNavTab("alerts_tab", "Threat Alerts", Icons.Default.Shield)
    object Logs : BottomNavTab("logs_tab", "Event Logs", Icons.Default.ReceiptLong)
    object Settings : BottomNavTab("settings_tab", "Settings", Icons.Default.Settings)

    companion object {
        val items = listOf(Alerts, Logs, Settings)
    }
}
