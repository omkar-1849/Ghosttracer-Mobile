package ai.sentinel.mobile.data.local

import kotlinx.coroutines.flow.StateFlow

interface SessionManager {
    val sessionStateFlow: StateFlow<Boolean>
    val organizationStateFlow: StateFlow<Int?>

    var accessToken: String?
    var selectedOrganizationId: Int?
    var selectedOrganizationName: String?
    var currentUserId: Int?
    var currentUserEmail: String?
    var currentUserName: String?
    var serverBaseUrl: String
    var fcmToken: String?
    var isDeviceRegistered: Boolean
    val installationId: String

    fun hasValidSession(): Boolean
    fun saveSession(
        token: String,
        userId: Int?,
        email: String?,
        name: String?,
        orgId: Int? = null,
        orgName: String? = null
    )
    fun clearSession()
}
