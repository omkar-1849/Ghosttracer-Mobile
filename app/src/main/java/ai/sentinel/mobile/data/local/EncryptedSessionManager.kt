package ai.sentinel.mobile.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class EncryptedSessionManager(context: Context) : SessionManager {

    private val sharedPreferences: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "sentinel_secure_session_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback for test environments or key invalidation
        context.getSharedPreferences("sentinel_fallback_session_prefs", Context.MODE_PRIVATE)
    }

    private val _sessionStateFlow = MutableStateFlow(hasValidSession())
    override val sessionStateFlow: StateFlow<Boolean> = _sessionStateFlow.asStateFlow()

    private val _organizationStateFlow = MutableStateFlow(selectedOrganizationId)
    override val organizationStateFlow: StateFlow<Int?> = _organizationStateFlow.asStateFlow()

    override var accessToken: String?
        get() = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        set(value) {
            sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, value).apply()
            _sessionStateFlow.value = !value.isNullOrBlank()
        }

    override var selectedOrganizationId: Int?
        get() {
            val id = sharedPreferences.getInt(KEY_SELECTED_ORG_ID, -1)
            return if (id > 0) id else null
        }
        set(value) {
            if (value != null) {
                sharedPreferences.edit().putInt(KEY_SELECTED_ORG_ID, value).apply()
            } else {
                sharedPreferences.edit().remove(KEY_SELECTED_ORG_ID).apply()
            }
            _organizationStateFlow.value = value
        }

    override var selectedOrganizationName: String?
        get() = sharedPreferences.getString(KEY_SELECTED_ORG_NAME, null)
        set(value) = sharedPreferences.edit().putString(KEY_SELECTED_ORG_NAME, value).apply()

    override var currentUserId: Int?
        get() {
            val id = sharedPreferences.getInt(KEY_USER_ID, -1)
            return if (id > 0) id else null
        }
        set(value) {
            if (value != null) {
                sharedPreferences.edit().putInt(KEY_USER_ID, value).apply()
            } else {
                sharedPreferences.edit().remove(KEY_USER_ID).apply()
            }
        }

    override var currentUserEmail: String?
        get() = sharedPreferences.getString(KEY_USER_EMAIL, null)
        set(value) = sharedPreferences.edit().putString(KEY_USER_EMAIL, value).apply()

    override var currentUserName: String?
        get() = sharedPreferences.getString(KEY_USER_NAME, null)
        set(value) = sharedPreferences.edit().putString(KEY_USER_NAME, value).apply()

    override var serverBaseUrl: String
        get() = sharedPreferences.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) {
            val sanitized = if (value.endsWith("/")) value else "$value/"
            sharedPreferences.edit().putString(KEY_BASE_URL, sanitized).apply()
        }

    override var fcmToken: String?
        get() = sharedPreferences.getString(KEY_FCM_TOKEN, null)
        set(value) = sharedPreferences.edit().putString(KEY_FCM_TOKEN, value).apply()

    override var isDeviceRegistered: Boolean
        get() = sharedPreferences.getBoolean(KEY_DEVICE_REGISTERED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_DEVICE_REGISTERED, value).apply()

    override val installationId: String
        get() {
            var id = sharedPreferences.getString(KEY_INSTALLATION_ID, null)
            if (id.isNullOrBlank()) {
                id = UUID.randomUUID().toString()
                sharedPreferences.edit().putString(KEY_INSTALLATION_ID, id).apply()
            }
            return id
        }

    override fun hasValidSession(): Boolean {
        return !accessToken.isNullOrBlank()
    }

    override fun saveSession(
        token: String,
        userId: Int?,
        email: String?,
        name: String?,
        orgId: Int?,
        orgName: String?
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, token)
            if (userId != null) putInt(KEY_USER_ID, userId) else remove(KEY_USER_ID)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            if (orgId != null) putInt(KEY_SELECTED_ORG_ID, orgId)
            if (orgName != null) putString(KEY_SELECTED_ORG_NAME, orgName)
            apply()
        }
        _sessionStateFlow.value = true
        _organizationStateFlow.value = orgId
    }

    override fun clearSession() {
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_SELECTED_ORG_ID)
            remove(KEY_SELECTED_ORG_NAME)
            putBoolean(KEY_DEVICE_REGISTERED, false)
            apply()
        }
        _sessionStateFlow.value = false
        _organizationStateFlow.value = null
    }

    companion object {
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"
        private const val KEY_ACCESS_TOKEN = "sec_access_token"
        private const val KEY_USER_ID = "sec_user_id"
        private const val KEY_USER_EMAIL = "sec_user_email"
        private const val KEY_USER_NAME = "sec_user_name"
        private const val KEY_SELECTED_ORG_ID = "sec_selected_org_id"
        private const val KEY_SELECTED_ORG_NAME = "sec_selected_org_name"
        private const val KEY_BASE_URL = "sec_server_base_url"
        private const val KEY_FCM_TOKEN = "sec_fcm_token"
        private const val KEY_DEVICE_REGISTERED = "sec_device_registered"
        private const val KEY_INSTALLATION_ID = "sec_installation_id"
    }
}
