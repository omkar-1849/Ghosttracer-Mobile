package ai.sentinel.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ai.sentinel.mobile.data.model.LogDto
import ai.sentinel.mobile.ui.theme.CardElevation1
import ai.sentinel.mobile.ui.theme.CardElevation2
import ai.sentinel.mobile.ui.theme.CobaltCore
import ai.sentinel.mobile.ui.theme.CriticalCrimson
import ai.sentinel.mobile.ui.theme.HighAmber
import ai.sentinel.mobile.ui.theme.MediumYellow
import ai.sentinel.mobile.ui.theme.ResolvedEmerald
import ai.sentinel.mobile.ui.theme.SurfaceBorder
import ai.sentinel.mobile.ui.theme.TextMuted
import ai.sentinel.mobile.ui.theme.TextPrimary
import ai.sentinel.mobile.ui.theme.TextSecondary

@Composable
fun LogCard(
    log: LogDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CardElevation1),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = "Log entry ${log.id}, method ${log.method}, path ${log.url}, status code ${log.statusCode}. Threat ${log.threatLevel}."
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Method + Status Code + Risk Chip + Severity Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val method = log.method ?: "HTTP"
                    Text(
                        text = method,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                if (method == "POST") CobaltCore else CardElevation2,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    val status = log.statusCode ?: 0
                    val statusColor = when (status) {
                        in 200..299 -> ResolvedEmerald
                        in 400..499 -> HighAmber
                        in 500..599 -> CriticalCrimson
                        else -> TextMuted
                    }
                    Text(
                        text = "$status",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier
                            .background(CardElevation2, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Risk ${log.riskScore}/100",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (log.riskScore >= 80) CriticalCrimson else if (log.riskScore >= 50) HighAmber else ResolvedEmerald
                    )
                    SeverityBadge(severity = log.severity)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // URL path in monospace
            Text(
                text = log.url ?: "(no url)",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Detection reason or message if available
            val reason = log.detectionReason ?: log.message
            if (!reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer row: Source IP and timestamp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SRC: ${log.ipAddress ?: "unknown"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Text(
                    text = log.timestamp?.take(19)?.replace("T", " ") ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}
