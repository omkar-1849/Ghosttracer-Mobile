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

enum class BottomNavTab(
    val route: String,
    val title: String
) {
    Alerts("alerts_tab", "Threat Alerts"),
    Logs("logs_tab", "Event Logs"),
    Settings("settings_tab", "Settings");

    val icon: ImageVector
        get() = when (this) {
            Alerts -> Icons.Default.Shield
            Logs -> Icons.Default.ReceiptLong
            Settings -> Icons.Default.Settings
        }

    companion object {
        val items: List<BottomNavTab> get() = entries
    }
}
