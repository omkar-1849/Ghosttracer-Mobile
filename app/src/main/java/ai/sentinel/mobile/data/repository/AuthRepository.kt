package ai.sentinel.mobile.data.repository

import ai.sentinel.mobile.data.local.SessionManager
import ai.sentinel.mobile.data.model.LoginRequest
import ai.sentinel.mobile.data.model.OrganizationDto
import ai.sentinel.mobile.data.model.UserDto
import ai.sentinel.mobile.data.remote.SentinelApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(
    private val apiService: SentinelApiService,
    private val sessionManager: SessionManager
) {

    val isUserLoggedInFlow: StateFlow<Boolean> = sessionManager.sessionStateFlow
    val currentOrgIdFlow: StateFlow<Int?> = sessionManager.organizationStateFlow

    val selectedOrgName: String?
        get() = sessionManager.selectedOrganizationName

    val currentUserEmail: String?
        get() = sessionManager.currentUserEmail

    val currentUserName: String?
        get() = sessionManager.currentUserName

    suspend fun login(email: String, pass: String): Result<UserDto?> {
        return try {
            val response = apiService.login(LoginRequest(email.trim(), pass))
            if (response.isSuccessful && response.body() != null) {
                val tokenResp = response.body()!!
                val user = tokenResp.user

                sessionManager.saveSession(
                    token = tokenResp.accessToken,
                    userId = user?.id,
                    email = user?.email ?: email,
                    name = user?.fullName
                )

                // Fetch memberships immediately to set default tenant
                val orgsResult = fetchOrganizations()
                val orgs = orgsResult.getOrNull()
                if (!orgs.isNullOrEmpty()) {
                    val defaultOrg = orgs.first()
                    sessionManager.selectedOrganizationId = defaultOrg.id
                    sessionManager.selectedOrganizationName = defaultOrg.name
                }

                Result.success(user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Login failed (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchOrganizations(): Result<List<OrganizationDto>> {
        return try {
            val response = apiService.listOrganizations()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch organizations: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun switchOrganization(org: OrganizationDto) {
        sessionManager.selectedOrganizationId = org.id
        sessionManager.selectedOrganizationName = org.name
    }

    suspend fun logout(): Result<Unit> {
        try {
            apiService.logout()
        } catch (_: Exception) {
            // Best effort remote revocation
        } finally {
            sessionManager.clearSession()
        }
        return Result.success(Unit)
    }

    suspend fun getMe(): Result<UserDto> {
        return try {
            val response = apiService.getMe()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                sessionManager.currentUserId = user.id
                sessionManager.currentUserEmail = user.email
                sessionManager.currentUserName = user.fullName
                Result.success(user)
            } else {
                Result.failure(Exception("Session check failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
