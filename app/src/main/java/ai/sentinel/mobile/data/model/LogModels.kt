package ai.sentinel.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogDto(
    val id: Int,
    @SerialName("threat_level") val threatLevel: String,
    @SerialName("risk_score") val riskScore: Int,
    @SerialName("ip_address") val ipAddress: String? = null,
    val method: String? = null,
    val url: String? = null,
    @SerialName("status_code") val statusCode: Int? = null,
    @SerialName("user_agent") val userAgent: String? = null,
    val message: String? = null,
    @SerialName("detection_reason") val detectionReason: String? = null,
    val timestamp: String? = null,
    @SerialName("website_id") val websiteId: Int? = null,
    @SerialName("integration_id") val integrationId: Int? = null
) {
    val severity: Severity
        get() = Severity.fromString(threatLevel)
}
