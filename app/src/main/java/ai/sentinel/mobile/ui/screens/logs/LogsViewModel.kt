package ai.sentinel.mobile.ui.screens.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.sentinel.mobile.data.model.LogDto
import ai.sentinel.mobile.data.model.Severity
import ai.sentinel.mobile.data.repository.LogsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LogsUiState(
    val logs: List<LogDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val filterSeverity: Severity? = null,
    val selectedLog: LogDto? = null
)

class LogsViewModel(
    private val logsRepository: LogsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            logsRepository.logsFlow.collect { logs ->
                _uiState.value = _uiState.value.copy(logs = logs)
            }
        }
        loadLogs()
    }

    fun loadLogs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = logsRepository.fetchLogs(limit = 40)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to load logs"
                    )
                }
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSeverityFilter(severity: Severity?) {
        _uiState.value = _uiState.value.copy(filterSeverity = severity)
    }

    fun selectLog(log: LogDto) {
        _uiState.value = _uiState.value.copy(selectedLog = log)
    }

    fun dismissLogDetail() {
        _uiState.value = _uiState.value.copy(selectedLog = null)
    }
}
