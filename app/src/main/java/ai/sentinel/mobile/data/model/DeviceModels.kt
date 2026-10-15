package ai.sentinel.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceRegistrationRequest(
    @SerialName("fcm_token") val fcmToken: String,
    @SerialName("device_name") val deviceName: String,
    @SerialName("app_version") val appVersion: String,
    @SerialName("installation_id") val installationId: String,
    @SerialName("platform") val platform: String = "android"
)

@Serializable
data class DeviceRegistrationResponse(
    @SerialName("device_id") val deviceId: String? = null,
    val status: String = "registered",
    val message: String? = null
)
