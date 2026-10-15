package ai.sentinel.mobile.data.repository

import ai.sentinel.mobile.data.remote.SentinelApiService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class AuthRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: SentinelApiService
    private lateinit var authRepository: AuthRepository
    private lateinit var sessionManager: FakeSessionManager

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
        sessionManager = FakeSessionManager()
        authRepository = AuthRepository(apiService, sessionManager)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testLoginSuccessAndOrgBootstrap() = runBlocking {
        val loginJson = """
            {
                "access_token": "jwt_secret_token_12345",
                "token_type": "bearer",
                "user": {
                    "id": 18,
                    "email": "admin@sentinel.ai",
                    "full_name": "SOC Administrator",
                    "is_active": true
                }
            }
        """.trimIndent()

        val orgsJson = """
            [
                {
                    "id": 17,
                    "name": "Sentinel SOC",
                    "current_user_role": "owner"
                }
            ]
        """.trimIndent()

        // 1. Enqueue login response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(loginJson)
                .addHeader("Content-Type", "application/json")
        )

        // 2. Enqueue listOrganizations response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(orgsJson)
                .addHeader("Content-Type", "application/json")
        )

        val result = authRepository.login("admin@sentinel.ai", "Password123!")
        assertTrue(result.isSuccess)
        val user = result.getOrNull()!!
        assertEquals(18, user.id)
        assertEquals("admin@sentinel.ai", user.email)
    }

    @Test
    fun testFetchOrganizations() = runBlocking {
        val orgsJson = """
            [
                {
                    "id": 17,
                    "name": "Sentinel SOC",
                    "current_user_role": "owner"
                },
                {
                    "id": 18,
                    "name": "QA Security Org",
                    "current_user_role": "analyst"
                }
            ]
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(orgsJson)
                .addHeader("Content-Type", "application/json")
        )

        val result = authRepository.fetchOrganizations()
        assertTrue(result.isSuccess)
        val list = result.getOrNull()!!
        assertEquals(2, list.size)
        assertEquals("Sentinel SOC", list[0].name)
    }
}
