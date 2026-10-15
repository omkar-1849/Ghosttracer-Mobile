package ai.sentinel.mobile.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ai.sentinel.mobile.data.model.AlertDto
import ai.sentinel.mobile.data.model.Severity
import ai.sentinel.mobile.ui.components.AlertCard
import ai.sentinel.mobile.ui.components.EmptyStateView
import ai.sentinel.mobile.ui.components.ErrorStateView
import ai.sentinel.mobile.ui.components.LoadingView
import ai.sentinel.mobile.ui.components.OfflineBanner
import ai.sentinel.mobile.ui.components.SOCAppBar
import ai.sentinel.mobile.ui.theme.CardElevation1
import ai.sentinel.mobile.ui.theme.CardElevation2
import ai.sentinel.mobile.ui.theme.CobaltCore
import ai.sentinel.mobile.ui.theme.DeepObsidian
import ai.sentinel.mobile.ui.theme.ElectricBlue
import ai.sentinel.mobile.ui.theme.SurfaceBorder
import ai.sentinel.mobile.ui.theme.TextMuted
import ai.sentinel.mobile.ui.theme.TextPrimary
import ai.sentinel.mobile.ui.theme.TextSecondary

@Composable
fun AlertsQueueScreen(
    uiState: AlertsUiState,
    organizationName: String?,
    onOrganizationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRefresh: () -> Unit,
    onAlertClick: (Int) -> Unit,
    onSeverityFilterChange: (Severity?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onDismissDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter alerts locally based on query and severity chip
    val filteredAlerts = remember(uiState.alerts, uiState.filterSeverity, uiState.searchQuery) {
        uiState.alerts.filter { alert ->
            val matchesSeverity = uiState.filterSeverity == null || alert.severity == uiState.filterSeverity
            val matchesQuery = uiState.searchQuery.isBlank() ||
                    alert.message.contains(uiState.searchQuery, ignoreCase = true) ||
                    (alert.ipAddress?.contains(uiState.searchQuery, ignoreCase = true) == true) ||
                    alert.id.toString().contains(uiState.searchQuery)
            matchesSeverity && matchesQuery
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            SOCAppBar(
                title = "Alerts Queue",
                organizationName = organizationName,
                isStale = uiState.isStale,
                onOrganizationClick = onOrganizationClick,
                onSettingsClick = onSettingsClick,
                onRefreshClick = onRefresh
            )

            // Offline / Stale banner if applicable
            if (uiState.isStale) {
                OfflineBanner(lastUpdatedTimestamp = uiState.lastUpdated)
            }

            // Search Bar & Filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Search Input
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search threat reasons, IP, or #ID...", style = MaterialTheme.typography.bodySmall, color = TextMuted) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedContainerColor = CardElevation1,
                        unfocusedContainerColor = CardElevation1
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Severity Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChipPill(
                        label = "ALL (${uiState.alerts.size})",
                        isSelected = uiState.filterSeverity == null,
                        onClick = { onSeverityFilterChange(null) }
                    )
                    FilterChipPill(
                        label = "CRITICAL",
                        isSelected = uiState.filterSeverity == Severity.CRITICAL,
                        onClick = { onSeverityFilterChange(Severity.CRITICAL) }
                    )
                    FilterChipPill(
                        label = "HIGH",
                        isSelected = uiState.filterSeverity == Severity.HIGH,
                        onClick = { onSeverityFilterChange(Severity.HIGH) }
                    )
                    FilterChipPill(
                        label = "MEDIUM",
                        isSelected = uiState.filterSeverity == Severity.MEDIUM,
                        onClick = { onSeverityFilterChange(Severity.MEDIUM) }
                    )
                    FilterChipPill(
                        label = "LOW / INFO",
                        isSelected = uiState.filterSeverity == Severity.LOW,
                        onClick = { onSeverityFilterChange(Severity.LOW) }
                    )
                }
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.isLoading && uiState.alerts.isEmpty()) {
                    LoadingView(message = "Querying live threat intelligence...")
                } else if (uiState.errorMessage != null && uiState.alerts.isEmpty()) {
                    ErrorStateView(
                        title = "Alerts Synchronization Error",
                        message = uiState.errorMessage,
                        onRetry = onRefresh
                    )
                } else if (filteredAlerts.isEmpty()) {
                    EmptyStateView(
                        title = if (uiState.searchQuery.isNotEmpty()) "No Matching Threats" else "No Alerts in Queue",
                        description = if (uiState.searchQuery.isNotEmpty()) "Adjust your search filter or clear severity selections." else "No active security threats reported for this organization.",
                        icon = Icons.Default.Shield,
                        actionLabel = "Refresh Queue",
                        onAction = onRefresh
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredAlerts, key = { it.id }) { alert ->
                            AlertCard(
                                alert = alert,
                                onClick = { onAlertClick(alert.id) }
                            )
                        }
                    }
                }
            }
        }

        // Forensic Detail Bottom Sheet
        if (uiState.selectedAlert != null) {
            AlertDetailSheet(
                alert = uiState.selectedAlert,
                isLoading = uiState.isDetailLoading,
                onDismiss = onDismissDetail
            )
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) CobaltCore else CardElevation1,
                RoundedCornerShape(6.dp)
            )
            .border(
                1.dp,
                if (isSelected) ElectricBlue else SurfaceBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}
