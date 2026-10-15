package ai.sentinel.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    val user: UserDto? = null
)

@Serializable
data class OrganizationDto(
    val id: Int,
    val name: String,
    @SerialName("current_user_role") val currentUserRole: String? = null
)

@Serializable
data class SessionDto(
    @SerialName("session_id") val sessionId: String,
    @SerialName("ip_address") val ipAddress: String? = null,
    @SerialName("user_agent") val userAgent: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val revoked: Boolean = false
)

@Serializable
data class GenericMessageResponse(
    val message: String? = null,
    val detail: String? = null
)
