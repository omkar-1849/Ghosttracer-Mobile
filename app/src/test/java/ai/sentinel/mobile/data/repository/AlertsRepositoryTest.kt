package ai.sentinel.mobile.data.repository

import ai.sentinel.mobile.data.local.SessionManager
import ai.sentinel.mobile.data.model.AlertDto
import ai.sentinel.mobile.data.remote.SentinelApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class FakeSessionManager : SessionManager {
    private val _sessionState = MutableStateFlow(true)
    override val sessionStateFlow: StateFlow<Boolean> = _sessionState.asStateFlow()

    private val _orgState = MutableStateFlow<Int?>(18)
    override val organizationStateFlow: StateFlow<Int?> = _orgState.asStateFlow()

    override var accessToken: String? = "mock_token"
    override var selectedOrganizationId: Int? = 18
    override var selectedOrganizationName: String? = "Sentinel SOC"
    override var currentUserId: Int? = 1
    override var currentUserEmail: String? = "admin@sentinel.ai"
    override var currentUserName: String? = "Admin"
    override var serverBaseUrl: String = "http://127.0.0.1:8000/"
    override var fcmToken: String? = "mock_fcm_token"
    override var isDeviceRegistered: Boolean = true
    override val installationId: String = "test-uuid"

    override fun hasValidSession(): Boolean = true
    override fun saveSession(token: String, userId: Int?, email: String?, name: String?, orgId: Int?, orgName: String?) {}
    override fun clearSession() {}
}

class AlertsRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: SentinelApiService
    private lateinit var alertsRepository: AlertsRepository

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        apiService = retrofit.create(SentinelApiService::class.java)
        alertsRepository = AlertsRepository(apiService, FakeSessionManager())
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testFetchAlertsSuccessAndDeduplication() = runBlocking {
        val mockJson = """
            [
                {
                    "id": 101,
                    "threat_level": "HIGH",
                    "message": "First alert",
                    "created_at": "2026-10-15T09:00:00"
                },
                {
                    "id": 102,
                    "threat_level": "CRITICAL",
                    "message": "Second alert",
                    "created_at": "2026-10-15T09:05:00"
                },
                {
                    "id": 101,
                    "threat_level": "HIGH",
                    "message": "Duplicate first alert",
                    "created_at": "2026-10-15T09:00:00"
                }
            ]
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockJson)
                .addHeader("Content-Type", "application/json")
        )

        val result = alertsRepository.fetchAlerts()
        assertTrue(result.isSuccess)
        val alerts = result.getOrNull()!!

        // Deduplication test: 3 items in response -> 2 unique alerts
        assertEquals(2, alerts.size)
        // Descending order: 102 first, then 101
        assertEquals(102, alerts[0].id)
        assertEquals(101, alerts[1].id)
        assertFalse(alertsRepository.isStaleFlow.value)
    }

    @Test
    fun testOfflineStaleFallbackWhenServerErrors() = runBlocking {
        // 1. Initial success
        val mockJson = """
            [
                {
                    "id": 105,
                    "threat_level": "CRITICAL",
                    "message": "Cached alert",
                    "created_at": "2026-10-15T09:00:00"
                }
            ]
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockJson)
                .addHeader("Content-Type", "application/json")
        )

        val initial = alertsRepository.fetchAlerts()
        assertTrue(initial.isSuccess)
        assertEquals(1, alertsRepository.alertsFlow.value.size)

        // 2. Subsequent server failure (HTTP 500)
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )

        val failResult = alertsRepository.fetchAlerts()
        assertTrue(failResult.isFailure)
        // Previous alerts remain in cache
        assertEquals(1, alertsRepository.alertsFlow.value.size)
        // Stale flag is active
        assertTrue(alertsRepository.isStaleFlow.value)
    }
}
