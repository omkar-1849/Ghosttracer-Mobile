package ai.sentinel.mobile.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ai.sentinel.mobile.data.model.Severity

class TestAlertBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alertId = intent.getIntExtra("alert_id", 134)
        val severityStr = intent.getStringExtra("severity") ?: "HIGH"
        val severity = Severity.fromString(severityStr)
        val summary = intent.getStringExtra("summary") ?: "Threat alert detected by Sentinel Platform."

        NotificationHelper.showThreatNotification(
            context = context,
            alertId = alertId,
            severity = severity,
            summary = summary
        )
    }
}
