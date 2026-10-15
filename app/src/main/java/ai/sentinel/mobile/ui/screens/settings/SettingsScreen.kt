package ai.sentinel.mobile.ui.screens.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ai.sentinel.mobile.data.local.EncryptedSessionManager
import ai.sentinel.mobile.data.repository.AuthRepository
import ai.sentinel.mobile.ui.theme.AmberHighlight
import ai.sentinel.mobile.ui.theme.CardElevation1
import ai.sentinel.mobile.ui.theme.CardElevation2
import ai.sentinel.mobile.ui.theme.CriticalCrimson
import ai.sentinel.mobile.ui.theme.DeepObsidian
import ai.sentinel.mobile.ui.theme.ElectricBlue
import ai.sentinel.mobile.ui.theme.ResolvedEmerald
import ai.sentinel.mobile.ui.theme.SurfaceBorder
import ai.sentinel.mobile.ui.theme.TextMuted
import ai.sentinel.mobile.ui.theme.TextPrimary
import ai.sentinel.mobile.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    sessionManager: EncryptedSessionManager,
    authRepository: AuthRepository,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var serverUrlInput by remember { mutableStateOf(sessionManager.serverBaseUrl) }
    var urlSavedMessage by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
                .padding(bottom = 80.dp)
        ) {
            // Screen Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "SOC Field Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Operator Identity
            SectionHeader("ACTIVE OPERATOR")
            CardSection {
                SettingRow(
                    icon = Icons.Default.Person,
                    title = "Operator Email",
                    value = authRepository.currentUserEmail ?: "Not authenticated"
                )
                if (authRepository.currentUserName != null) {
                    SettingRow(
                        icon = Icons.Default.Person,
                        title = "Full Name",
                        value = authRepository.currentUserName!!
                    )
                }
                SettingRow(
                    icon = Icons.Default.Business,
                    title = "Authorized Tenant",
                    value = authRepository.selectedOrgName ?: "Default Organization"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Server API Endpoint Configuration
            SectionHeader("SENTINEL BACKEND ENDPOINT")
            CardSection {
                Text(
                    text = "Configure the Sentinel REST API target. Changes take effect on subsequent network calls.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = serverUrlInput,
                    onValueChange = {
                        serverUrlInput = it
                        urlSavedMessage = false
                    },
                    label = { Text("API Base URL") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedLabelColor = ElectricBlue,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = CardElevation2,
                        unfocusedContainerColor = CardElevation2
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            sessionManager.serverBaseUrl = serverUrlInput
                            urlSavedMessage = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text("Save Endpoint")
                    }

                    if (urlSavedMessage) {
                        Text(
                            text = "✓ Endpoint updated",
                            style = MaterialTheme.typography.labelSmall,
                            color = ResolvedEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Push & Device Enrollment Status
            SectionHeader("DEVICE & PUSH NOTIFICATIONS")
            CardSection {
                SettingRow(
                    icon = Icons.Default.PhoneAndroid,
                    title = "Installation Identifier",
                    value = sessionManager.installationId.take(16) + "...",
                    isMonospace = true
                )
                SettingRow(
                    icon = if (sessionManager.isDeviceRegistered) Icons.Default.CheckCircle else Icons.Default.Warning,
                    title = "FCM Registration",
                    value = if (sessionManager.isDeviceRegistered) "Synchronized with backend" else "Degraded / Unregistered",
                    valueColor = if (sessionManager.isDeviceRegistered) ResolvedEmerald else AmberHighlight
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Notification System Settings")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Security & Storage
            SectionHeader("SECURITY POSTURE")
            CardSection {
                SettingRow(
                    icon = Icons.Default.Lock,
                    title = "Local Vault",
                    value = "AES256-GCM / Android Keystore"
                )
                SettingRow(
                    icon = Icons.Default.Lock,
                    title = "Cloud Backup",
                    value = "Excluded (android:allowBackup=false)"
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 5. Logout Button
            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = CriticalCrimson),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REVOKE SESSION & SIGN OUT",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun CardSection(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardElevation1, RoundedCornerShape(8.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    isMonospace: Boolean = false,
    valueColor: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Text(
            text = value,
            style = if (isMonospace) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}
