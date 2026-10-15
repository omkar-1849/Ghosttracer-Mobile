package ai.sentinel.mobile.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ai.sentinel.mobile.R
import ai.sentinel.mobile.data.model.Severity
import ai.sentinel.mobile.ui.MainActivity

object NotificationHelper {

    const val CHANNEL_CRITICAL_ID = "sentinel_channel_critical"
    const val CHANNEL_HIGH_ID = "sentinel_channel_high"

    const val EXTRA_ALERT_ID = "ai.sentinel.mobile.EXTRA_ALERT_ID"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Critical Channel (High Importance, Audible, Vibration)
            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL_ID,
                context.getString(R.string.channel_critical_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_critical_desc)
                enableVibration(true)
                setShowBadge(true)
            }

            // High Priority Channel (Default Importance)
            val highChannel = NotificationChannel(
                CHANNEL_HIGH_ID,
                context.getString(R.string.channel_high_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_high_desc)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(criticalChannel)
            notificationManager.createNotificationChannel(highChannel)
        }
    }

    fun showThreatNotification(
        context: Context,
        alertId: Int,
        severity: Severity,
        summary: String? = null
    ) {
        // Check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val channelId = if (severity == Severity.CRITICAL) CHANNEL_CRITICAL_ID else CHANNEL_HIGH_ID

        // Explicit Intent to MainActivity with deep link alert ID
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "ai.sentinel.mobile.ACTION_VIEW_ALERT"
            putExtra(EXTRA_ALERT_ID, alertId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alertId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (severity) {
            Severity.CRITICAL -> "CRITICAL THREAT ALERT #$alertId"
            Severity.HIGH -> "HIGH PRIORITY ALERT #$alertId"
            else -> "SECURITY THREAT ADVISORY #$alertId"
        }

        val bodyText = summary ?: "High-risk threat detected. Tap to inspect forensic telemetry."

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_sentinel_shield)
            .setContentTitle(title)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(
                if (severity == Severity.CRITICAL) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        NotificationManagerCompat.from(context).notify(alertId, notification)
    }
}
