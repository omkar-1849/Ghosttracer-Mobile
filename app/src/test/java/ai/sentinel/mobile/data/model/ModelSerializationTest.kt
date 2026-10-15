package ai.sentinel.mobile.data.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ModelSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Test
    fun testAlertDtoParsing() {
        val jsonString = """
            {
                "id": 133,
                "threat_level": "CRITICAL",
                "message": "[SQLMAP] SQLMAP detected 1 critical vulnerability on http://127.0.0.1:8080/",
                "created_at": "2026-10-15T09:36:43.606399",
                "ip_address": "127.0.0.1",
                "website_id": 1,
                "scan_id": 4
            }
        """.trimIndent()

        val alert = json.decodeFromString<AlertDto>(jsonString)
        assertEquals(133, alert.id)
        assertEquals(Severity.CRITICAL, alert.severity)
        assertEquals("127.0.0.1", alert.ipAddress)
        assertEquals(1, alert.websiteId)
    }

    @Test
    fun testSeverityMapping() {
        assertEquals(Severity.CRITICAL, Severity.fromString("critical"))
        assertEquals(Severity.CRITICAL, Severity.fromString("CRITICAL"))
        assertEquals(Severity.HIGH, Severity.fromString("HIGH"))
        assertEquals(Severity.MEDIUM, Severity.fromString("MEDIUM"))
        assertEquals(Severity.LOW, Severity.fromString("LOW"))
        assertEquals(Severity.UNKNOWN, Severity.fromString("ANOMALOUS_VALUE"))
        assertEquals(Severity.UNKNOWN, Severity.fromString(null))
    }

    @Test
    fun testLogDtoParsing() {
        val jsonString = """
            {
                "id": 658,
                "threat_level": "HIGH",
                "risk_score": 88,
                "ip_address": "192.168.1.105",
                "method": "POST",
                "url": "/api/login",
                "status_code": 401,
                "message": "Multiple authentication attempts",
                "detection_reason": "Brute force attack signature detected",
                "timestamp": "2026-10-15T09:36:45.669596"
            }
        """.trimIndent()

        val log = json.decodeFromString<LogDto>(jsonString)
        assertEquals(658, log.id)
        assertEquals(Severity.HIGH, log.severity)
        assertEquals(88, log.riskScore)
        assertEquals("POST", log.method)
        assertEquals(401, log.statusCode)
    }

    @Test
    fun testPushNotificationPayloadParsing() {
        val jsonString = """
            {
                "version": "1.0",
                "alert_id": "134",
                "severity": "HIGH",
                "organization_id": "18",
                "summary": "[SSL] Identified high severity issue"
            }
        """.trimIndent()

        val payload = json.decodeFromString<PushNotificationPayload>(jsonString)
        assertEquals("134", payload.alertId)
        assertEquals("HIGH", payload.severity)
        assertEquals("18", payload.organizationId)
    }
}
