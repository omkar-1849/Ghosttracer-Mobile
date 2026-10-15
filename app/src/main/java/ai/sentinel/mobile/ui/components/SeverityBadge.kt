package ai.sentinel.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ai.sentinel.mobile.data.model.Severity
import ai.sentinel.mobile.ui.theme.CriticalContainer
import ai.sentinel.mobile.ui.theme.CriticalCrimson
import ai.sentinel.mobile.ui.theme.CriticalOnContainer
import ai.sentinel.mobile.ui.theme.HighAmber
import ai.sentinel.mobile.ui.theme.HighContainer
import ai.sentinel.mobile.ui.theme.HighOnContainer
import ai.sentinel.mobile.ui.theme.LowContainer
import ai.sentinel.mobile.ui.theme.LowOnContainer
import ai.sentinel.mobile.ui.theme.LowSky
import ai.sentinel.mobile.ui.theme.MediumContainer
import ai.sentinel.mobile.ui.theme.MediumOnContainer
import ai.sentinel.mobile.ui.theme.MediumYellow
import ai.sentinel.mobile.ui.theme.ResolvedContainer
import ai.sentinel.mobile.ui.theme.ResolvedEmerald
import ai.sentinel.mobile.ui.theme.ResolvedOnContainer
import ai.sentinel.mobile.ui.theme.UnknownContainer
import ai.sentinel.mobile.ui.theme.UnknownGray
import ai.sentinel.mobile.ui.theme.UnknownOnContainer

data class SeverityColors(
    val background: Color,
    val text: Color,
    val border: Color,
    val icon: ImageVector
)

fun getSeverityColors(severity: Severity): SeverityColors {
    return when (severity) {
        Severity.CRITICAL -> SeverityColors(
            background = CriticalContainer,
            text = CriticalOnContainer,
            border = CriticalCrimson,
            icon = Icons.Default.Error
        )
        Severity.HIGH -> SeverityColors(
            background = HighContainer,
            text = HighOnContainer,
            border = HighAmber,
            icon = Icons.Default.Warning
        )
        Severity.MEDIUM -> SeverityColors(
            background = MediumContainer,
            text = MediumOnContainer,
            border = MediumYellow,
            icon = Icons.Default.Warning
        )
        Severity.LOW, Severity.INFO -> SeverityColors(
            background = LowContainer,
            text = LowOnContainer,
            border = LowSky,
            icon = Icons.Default.Info
        )
        Severity.RESOLVED -> SeverityColors(
            background = ResolvedContainer,
            text = ResolvedOnContainer,
            border = ResolvedEmerald,
            icon = Icons.Default.CheckCircle
        )
        Severity.UNKNOWN -> SeverityColors(
            background = UnknownContainer,
            text = UnknownOnContainer,
            border = UnknownGray,
            icon = Icons.Default.Info
        )
    }
}

@Composable
fun SeverityBadge(
    severity: Severity,
    modifier: Modifier = Modifier,
    customLabel: String? = null
) {
    val config = getSeverityColors(severity)
    val labelText = customLabel ?: severity.name

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .background(config.background, RoundedCornerShape(4.dp))
            .border(1.dp, config.border, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .semantics {
                contentDescription = "Severity level: $labelText"
            }
    ) {
        Icon(
            imageVector = config.icon,
            contentDescription = null,
            tint = config.text,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = labelText,
            color = config.text,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
