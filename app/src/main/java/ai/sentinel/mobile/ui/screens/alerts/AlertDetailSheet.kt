package ai.sentinel.mobile.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ai.sentinel.mobile.data.model.AlertDto
import ai.sentinel.mobile.data.model.Severity
import ai.sentinel.mobile.ui.components.SeverityBadge
import ai.sentinel.mobile.ui.theme.CardElevation1
import ai.sentinel.mobile.ui.theme.CardElevation2
import ai.sentinel.mobile.ui.theme.CobaltCore
import ai.sentinel.mobile.ui.theme.CriticalContainer
import ai.sentinel.mobile.ui.theme.CriticalOnContainer
import ai.sentinel.mobile.ui.theme.DeepObsidian
import ai.sentinel.mobile.ui.theme.HighContainer
import ai.sentinel.mobile.ui.theme.HighOnContainer
import ai.sentinel.mobile.ui.theme.SurfaceBorder
import ai.sentinel.mobile.ui.theme.TextMuted
import ai.sentinel.mobile.ui.theme.TextPrimary
import ai.sentinel.mobile.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailSheet(
    alert: AlertDto,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardElevation1,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(scrollState)
        ) {
            // Header: #ID + Severity + Close Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ALERT #${alert.id}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    SeverityBadge(severity = alert.severity)
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close details",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Threat message block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardElevation2, RoundedCornerShape(8.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Forensic Telemetry Table
            Text(
                text = "FORENSIC TELEMETRY",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepObsidian, RoundedCornerShape(8.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Incident Code
                ForensicRow(
                    label = "Incident Code",
                    value = if (alert.incidentId != null) "INC-${String.format("%06d", alert.id)}" else "INC-${alert.id}",
                    onCopy = { clipboardManager.setText(AnnotatedString("INC-${alert.id}")) }
                )

                // Source IP Address
                if (!alert.ipAddress.isNullOrBlank()) {
                    ForensicRow(
                        label = "Source IP",
                        value = alert.ipAddress,
                        isMonospace = true,
                        onCopy = { clipboardManager.setText(AnnotatedString(alert.ipAddress)) }
                    )
                }

                // Target Website ID
                if (alert.websiteId != null) {
                    ForensicRow(
                        label = "Target Asset ID",
                        value = "#${alert.websiteId}"
                    )
                }

                // Log Record ID
                if (alert.logId != null) {
                    ForensicRow(
                        label = "Trigger Log Record",
                        value = "Log #${alert.logId}"
                    )
                }

                // Scan Reference
                if (alert.scanId != null) {
                    ForensicRow(
                        label = "Associated Scan",
                        value = "Scan #${alert.scanId}"
                    )
                }

                // Created Timestamp
                ForensicRow(
                    label = "Recorded Timestamp",
                    value = alert.createdAt.replace("T", " "),
                    isMonospace = true
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SOC Containment Recommendations
            Text(
                text = "SOC TRIAGE GUIDELINES",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            val recommendation = when (alert.severity) {
                Severity.CRITICAL -> "CRITICAL THREAT: Immediate active defense containment recommended. Inspect upstream reverse proxy access logs, verify database integrity for injection payloads, and consider rate-limiting or blocking IP '${alert.ipAddress ?: "unknown"}'."
                Severity.HIGH -> "HIGH HAZARD: Elevated attack density detected. Review authenticated sessions and verify whether subsequent requests produced anomalous 5xx server errors or unauthorized access."
                else -> "MONITORING ADVISORY: Standard threat event. Telemetry has been correlated into the organization incident timeline."
            }

            val bannerBg = when (alert.severity) {
                Severity.CRITICAL -> CriticalContainer
                Severity.HIGH -> HighContainer
                else -> CardElevation2
            }

            val bannerText = when (alert.severity) {
                Severity.CRITICAL -> CriticalOnContainer
                Severity.HIGH -> HighOnContainer
                else -> TextSecondary
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bannerBg, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = bannerText
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CobaltCore),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Acknowledge & Close")
            }
        }
    }
}

@Composable
private fun ForensicRow(
    label: String,
    value: String,
    isMonospace: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = if (isMonospace) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            if (onCopy != null) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy $label",
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
