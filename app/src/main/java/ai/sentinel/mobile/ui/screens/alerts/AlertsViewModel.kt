package ai.sentinel.mobile.ui.screens.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.sentinel.mobile.data.model.AlertDto
import ai.sentinel.mobile.data.model.Severity
import ai.sentinel.mobile.data.repository.AlertsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlertsUiState(
    val alerts: List<AlertDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isStale: Boolean = false,
    val lastUpdated: Long? = null,
    val filterSeverity: Severity? = null,
    val searchQuery: String = "",
    val selectedAlert: AlertDto? = null,
    val isDetailLoading: Boolean = false,
    val detailErrorMessage: String? = null
)

class AlertsViewModel(
    private val alertsRepository: AlertsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        // Observe repository flows
        viewModelScope.launch {
            combine(
                alertsRepository.alertsFlow,
                alertsRepository.lastUpdatedFlow,
                alertsRepository.isStaleFlow
            ) { alerts, lastUpdated, isStale ->
                Triple(alerts, lastUpdated, isStale)
            }.collect { (alerts, lastUpdated, isStale) ->
                _uiState.value = _uiState.value.copy(
                    alerts = alerts,
                    lastUpdated = lastUpdated,
                    isStale = isStale
                )
            }
        }

        loadAlerts()
    }

    fun loadAlerts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = alertsRepository.fetchAlerts()
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to refresh alerts"
                    )
                }
            )
        }
    }

    fun setSeverityFilter(severity: Severity?) {
        _uiState.value = _uiState.value.copy(filterSeverity = severity)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun openAlertDetail(alertId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDetailLoading = true,
                detailErrorMessage = null,
                selectedAlert = _uiState.value.alerts.find { it.id == alertId }
            )

            val result = alertsRepository.getAlertDetail(alertId)
            result.fold(
                onSuccess = { detailedAlert ->
                    _uiState.value = _uiState.value.copy(
                        selectedAlert = detailedAlert,
                        isDetailLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isDetailLoading = false,
                        detailErrorMessage = error.localizedMessage
                    )
                }
            )
        }
    }

    fun closeAlertDetail() {
        _uiState.value = _uiState.value.copy(
            selectedAlert = null,
            detailErrorMessage = null
        )
    }
}
