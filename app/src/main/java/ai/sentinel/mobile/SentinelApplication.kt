package ai.sentinel.mobile

import android.app.Application
import ai.sentinel.mobile.data.local.EncryptedSessionManager
import ai.sentinel.mobile.data.remote.ApiClient
import ai.sentinel.mobile.data.repository.AlertsRepository
import ai.sentinel.mobile.data.repository.AuthRepository
import ai.sentinel.mobile.data.repository.DeviceRepository
import ai.sentinel.mobile.data.repository.LogsRepository
import ai.sentinel.mobile.service.NotificationHelper

class SentinelApplication : Application() {

    lateinit var sessionManager: EncryptedSessionManager
        private set

    lateinit var apiClient: ApiClient
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var alertsRepository: AlertsRepository
        private set

    lateinit var logsRepository: LogsRepository
        private set

    lateinit var deviceRepository: DeviceRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 1. Initialize encrypted keystore session manager
        sessionManager = EncryptedSessionManager(this)

        // 2. Initialize network layer with interceptors
        apiClient = ApiClient(this, sessionManager)

        // 3. Initialize repositories
        authRepository = AuthRepository(apiClient.apiService, sessionManager)
        alertsRepository = AlertsRepository(apiClient.apiService, sessionManager)
        logsRepository = LogsRepository(apiClient.apiService, sessionManager)
        deviceRepository = DeviceRepository(apiClient.apiService, sessionManager)

        // 4. Initialize notification channels
        NotificationHelper.createNotificationChannels(this)
    }

    companion object {
        lateinit var instance: SentinelApplication
            private set
    }
}
