package ai.sentinel.mobile.ui.screens.logs

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ai.sentinel.mobile.data.model.LogDto
import ai.sentinel.mobile.data.model.Severity
import ai.sentinel.mobile.ui.components.EmptyStateView
import ai.sentinel.mobile.ui.components.ErrorStateView
import ai.sentinel.mobile.ui.components.LoadingView
import ai.sentinel.mobile.ui.components.LogCard
import ai.sentinel.mobile.ui.components.SOCAppBar
import ai.sentinel.mobile.ui.theme.CardElevation1
import ai.sentinel.mobile.ui.theme.CobaltCore
import ai.sentinel.mobile.ui.theme.DeepObsidian
import ai.sentinel.mobile.ui.theme.ElectricBlue
import ai.sentinel.mobile.ui.theme.SurfaceBorder
import ai.sentinel.mobile.ui.theme.TextMuted
import ai.sentinel.mobile.ui.theme.TextPrimary
import ai.sentinel.mobile.ui.theme.TextSecondary

@Composable
fun LogsScreen(
    uiState: LogsUiState,
    organizationName: String?,
    onOrganizationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRefresh: () -> Unit,
    onLogClick: (LogDto) -> Unit,
    onSeverityFilterChange: (Severity?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onDismissDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredLogs = remember(uiState.logs, uiState.filterSeverity, uiState.searchQuery) {
        uiState.logs.filter { log ->
            val matchesSeverity = uiState.filterSeverity == null || log.severity == uiState.filterSeverity
            val matchesQuery = uiState.searchQuery.isBlank() ||
                    (log.url?.contains(uiState.searchQuery, ignoreCase = true) == true) ||
                    (log.ipAddress?.contains(uiState.searchQuery, ignoreCase = true) == true) ||
                    (log.detectionReason?.contains(uiState.searchQuery, ignoreCase = true) == true) ||
                    (log.message?.contains(uiState.searchQuery, ignoreCase = true) == true) ||
                    log.id.toString().contains(uiState.searchQuery)
            matchesSeverity && matchesQuery
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar
            SOCAppBar(
                title = "Security Event Logs",
                organizationName = organizationName,
                isStale = false,
                onOrganizationClick = onOrganizationClick,
                onSettingsClick = onSettingsClick,
                onRefreshClick = onRefresh
            )

            // Search Bar & Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Filter by URL, IP address, or signature...", style = MaterialTheme.typography.bodySmall, color = TextMuted) },
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LogFilterChip(
                        label = "ALL (${uiState.logs.size})",
                        isSelected = uiState.filterSeverity == null,
                        onClick = { onSeverityFilterChange(null) }
                    )
                    LogFilterChip(
                        label = "CRITICAL",
                        isSelected = uiState.filterSeverity == Severity.CRITICAL,
                        onClick = { onSeverityFilterChange(Severity.CRITICAL) }
                    )
                    LogFilterChip(
                        label = "HIGH",
                        isSelected = uiState.filterSeverity == Severity.HIGH,
                        onClick = { onSeverityFilterChange(Severity.HIGH) }
                    )
                    LogFilterChip(
                        label = "MEDIUM",
                        isSelected = uiState.filterSeverity == Severity.MEDIUM,
                        onClick = { onSeverityFilterChange(Severity.MEDIUM) }
                    )
                    LogFilterChip(
                        label = "LOW / INFO",
                        isSelected = uiState.filterSeverity == Severity.LOW,
                        onClick = { onSeverityFilterChange(Severity.LOW) }
                    )
                }
            }

            // List Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.isLoading && uiState.logs.isEmpty()) {
                    LoadingView(message = "Streaming recent HTTP telemetry...")
                } else if (uiState.errorMessage != null && uiState.logs.isEmpty()) {
                    ErrorStateView(
                        title = "Logs Retrieval Failed",
                        message = uiState.errorMessage,
                        onRetry = onRefresh
                    )
                } else if (filteredLogs.isEmpty()) {
                    EmptyStateView(
                        title = if (uiState.searchQuery.isNotEmpty()) "No Logs Found" else "No Telemetry Ingested",
                        description = if (uiState.searchQuery.isNotEmpty()) "No HTTP requests match the current search filters." else "No security logs recorded for the selected tenant.",
                        icon = Icons.Default.ReceiptLong,
                        actionLabel = "Refresh Logs",
                        onAction = onRefresh
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredLogs, key = { it.id }) { log ->
                            LogCard(
                                log = log,
                                onClick = { onLogClick(log) }
                            )
                        }
                    }
                }
            }
        }

        // Forensic Detail Sheet
        if (uiState.selectedLog != null) {
            LogDetailSheet(
                log = uiState.selectedLog,
                onDismiss = onDismissDetail
            )
        }
    }
}

@Composable
private fun LogFilterChip(
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
