package ai.sentinel.mobile.data.repository

import ai.sentinel.mobile.data.local.SessionManager
import ai.sentinel.mobile.data.model.AlertDto
import ai.sentinel.mobile.data.remote.SentinelApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlertsRepository(
    private val apiService: SentinelApiService,
    private val sessionManager: SessionManager
) {

    private val _alertsFlow = MutableStateFlow<List<AlertDto>>(emptyList())
    val alertsFlow: StateFlow<List<AlertDto>> = _alertsFlow.asStateFlow()

    private val _lastUpdatedFlow = MutableStateFlow<Long?>(null)
    val lastUpdatedFlow: StateFlow<Long?> = _lastUpdatedFlow.asStateFlow()

    private val _isStaleFlow = MutableStateFlow(false)
    val isStaleFlow: StateFlow<Boolean> = _isStaleFlow.asStateFlow()

    suspend fun fetchAlerts(): Result<List<AlertDto>> {
        return try {
            val response = apiService.getRecentAlerts()
            if (response.isSuccessful && response.body() != null) {
                val alerts = response.body()!!
                // Deduplicate by ID and sort descending
                val deduplicated = alerts.distinctBy { it.id }.sortedByDescending { it.id }
                _alertsFlow.value = deduplicated
                _lastUpdatedFlow.value = System.currentTimeMillis()
                _isStaleFlow.value = false
                Result.success(deduplicated)
            } else {
                _isStaleFlow.value = _alertsFlow.value.isNotEmpty()
                Result.failure(Exception("Failed to load alerts: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            _isStaleFlow.value = _alertsFlow.value.isNotEmpty()
            Result.failure(e)
        }
    }

    suspend fun getAlertDetail(alertId: Int): Result<AlertDto> {
        return try {
            // First try remote single-alert endpoint
            val response = apiService.getAlertDetail(alertId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Fallback to cached alert if available
                val cached = _alertsFlow.value.find { it.id == alertId }
                if (cached != null) {
                    Result.success(cached)
                } else {
                    Result.failure(Exception("Alert #$alertId not found (HTTP ${response.code()})"))
                }
            }
        } catch (e: Exception) {
            val cached = _alertsFlow.value.find { it.id == alertId }
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }
}
