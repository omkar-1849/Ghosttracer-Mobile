package ai.sentinel.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class Severity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    INFO,
    RESOLVED,
    UNKNOWN;

    companion object {
        fun fromString(value: String?): Severity {
            return when (value?.uppercase()?.trim()) {
                "CRITICAL" -> CRITICAL
                "HIGH" -> HIGH
                "MEDIUM" -> MEDIUM
                "LOW" -> LOW
                "INFO" -> INFO
                "RESOLVED" -> RESOLVED
                else -> UNKNOWN
            }
        }
    }
}

@Serializable
data class AlertDto(
    val id: Int,
    @SerialName("threat_level") val threatLevel: String,
    val message: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("ip_address") val ipAddress: String? = null,
    @SerialName("website_id") val websiteId: Int? = null,
    @SerialName("event_id") val eventId: Int? = null,
    @SerialName("log_id") val logId: Int? = null,
    @SerialName("incident_id") val incidentId: Int? = null,
    @SerialName("source_type") val sourceType: String? = null,
    @SerialName("finding_id") val findingId: Int? = null,
    @SerialName("investigation_id") val investigationId: Int? = null,
    @SerialName("scan_id") val scanId: Int? = null
) {
    val severity: Severity
        get() = Severity.fromString(threatLevel)
}

@Serializable
data class PushNotificationPayload(
    val version: String = "1.0",
    @SerialName("alert_id") val alertId: String,
    val severity: String,
    @SerialName("organization_id") val organizationId: String? = null,
    @SerialName("opaque_event_id") val opaqueEventId: String? = null,
    val title: String? = null,
    val summary: String? = null
)
