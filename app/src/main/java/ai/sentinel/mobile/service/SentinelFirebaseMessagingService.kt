package ai.sentinel.mobile.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ai.sentinel.mobile.SentinelApplication
import ai.sentinel.mobile.data.model.Severity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SentinelFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val app = application as? SentinelApplication ?: return

        app.sessionManager.fcmToken = token

        // If authenticated, synchronize device registration with Sentinel backend
        if (app.sessionManager.hasValidSession()) {
            serviceScope.launch {
                app.deviceRepository.registerDevice(token)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data

        if (data.isEmpty()) return

        // 1. Validate payload size and schema
        val alertIdStr = data["alert_id"] ?: data["alertId"] ?: return
        val alertId = alertIdStr.toIntOrNull() ?: return

        val severityStr = data["severity"] ?: "UNKNOWN"
        val severity = Severity.fromString(severityStr)

        // Only alert on HIGH and CRITICAL per architecture specification
        if (severity != Severity.CRITICAL && severity != Severity.HIGH) {
            return
        }

        val summary = data["summary"] ?: data["message"] ?: "Security threat detected."

        // 2. Display notification
        NotificationHelper.showThreatNotification(
            context = applicationContext,
            alertId = alertId,
            severity = severity,
            summary = summary
        )

        // 3. Trigger background alerts cache update if initialized
        val app = application as? SentinelApplication ?: return
        serviceScope.launch {
            app.alertsRepository.fetchAlerts()
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        // Provider dropped messages: perform recovery refresh
        val app = application as? SentinelApplication ?: return
        serviceScope.launch {
            app.alertsRepository.fetchAlerts()
        }
    }
}
