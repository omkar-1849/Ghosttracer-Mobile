package ai.sentinel.mobile.data.remote

import ai.sentinel.mobile.data.model.AlertDto
import ai.sentinel.mobile.data.model.DeviceRegistrationRequest
import ai.sentinel.mobile.data.model.DeviceRegistrationResponse
import ai.sentinel.mobile.data.model.GenericMessageResponse
import ai.sentinel.mobile.data.model.LogDto
import ai.sentinel.mobile.data.model.LoginRequest
import ai.sentinel.mobile.data.model.OrganizationDto
import ai.sentinel.mobile.data.model.TokenResponse
import ai.sentinel.mobile.data.model.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SentinelApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<UserDto>

    @POST("sessions/logout")
    suspend fun logout(): Response<GenericMessageResponse>

    @GET("organization/list")
    suspend fun listOrganizations(): Response<List<OrganizationDto>>

    @GET("alerts/recent")
    suspend fun getRecentAlerts(): Response<List<AlertDto>>

    @GET("alerts")
    suspend fun getAllAlerts(): Response<List<AlertDto>>

    @GET("alerts/{alert_id}")
    suspend fun getAlertDetail(
        @Path("alert_id") alertId: Int
    ): Response<AlertDto>

    @GET("logs/recent")
    suspend fun getRecentLogs(
        @Query("limit") limit: Int = 30
    ): Response<List<LogDto>>

    @POST("mobile/devices/register")
    suspend fun registerDevice(
        @Body request: DeviceRegistrationRequest
    ): Response<DeviceRegistrationResponse>
}
