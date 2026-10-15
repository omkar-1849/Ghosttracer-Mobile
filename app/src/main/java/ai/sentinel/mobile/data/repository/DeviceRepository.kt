package ai.sentinel.mobile.data.repository

import android.os.Build
import ai.sentinel.mobile.BuildConfig
import ai.sentinel.mobile.data.local.SessionManager
import ai.sentinel.mobile.data.model.DeviceRegistrationRequest
import ai.sentinel.mobile.data.remote.SentinelApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceRepository(
    private val apiService: SentinelApiService,
    private val sessionManager: SessionManager
) {

    private val _registrationStateFlow = MutableStateFlow(sessionManager.isDeviceRegistered)
    val registrationStateFlow: StateFlow<Boolean> = _registrationStateFlow.asStateFlow()

    suspend fun registerDevice(fcmToken: String): Result<Boolean> {
        sessionManager.fcmToken = fcmToken
        return try {
            val request = DeviceRegistrationRequest(
                fcmToken = fcmToken,
                deviceName = "${Build.MANUFACTURER} ${Build.MODEL}",
                appVersion = BuildConfig.VERSION_NAME,
                installationId = sessionManager.installationId,
                platform = "android"
            )

            val response = apiService.registerDevice(request)
            if (response.isSuccessful) {
                sessionManager.isDeviceRegistered = true
                _registrationStateFlow.value = true
                Result.success(true)
            } else if (response.code() == 404 || response.code() == 405) {
                // Endpoint not yet implemented on this backend version; operate in local degraded mode
                sessionManager.isDeviceRegistered = false
                _registrationStateFlow.value = false
                Result.success(false)
            } else {
                sessionManager.isDeviceRegistered = false
                _registrationStateFlow.value = false
                Result.failure(Exception("Device registration failed (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            // Degraded push client handling
            sessionManager.isDeviceRegistered = false
            _registrationStateFlow.value = false
            Result.failure(e)
        }
    }
}
