package ai.sentinel.mobile.data.repository

import ai.sentinel.mobile.data.local.SessionManager
import ai.sentinel.mobile.data.model.LogDto
import ai.sentinel.mobile.data.remote.SentinelApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LogsRepository(
    private val apiService: SentinelApiService,
    private val sessionManager: SessionManager
) {

    private val _logsFlow = MutableStateFlow<List<LogDto>>(emptyList())
    val logsFlow: StateFlow<List<LogDto>> = _logsFlow.asStateFlow()

    suspend fun fetchLogs(limit: Int = 30): Result<List<LogDto>> {
        return try {
            val response = apiService.getRecentLogs(limit)
            if (response.isSuccessful && response.body() != null) {
                val logs = response.body()!!
                _logsFlow.value = logs
                Result.success(logs)
            } else {
                Result.failure(Exception("Failed to load security logs: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
