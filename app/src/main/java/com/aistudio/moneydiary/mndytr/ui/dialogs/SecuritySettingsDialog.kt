package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aistudio.moneydiary.mndytr.domain.service.AppLockType
import com.aistudio.moneydiary.mndytr.domain.service.AppSecurityConfig
import com.aistudio.moneydiary.mndytr.domain.service.NotificationPrivacyLevel
import com.aistudio.moneydiary.mndytr.ui.theme.KoriTypographyTokens

@Composable
fun SecuritySettingsDialog(
    securityConfig: AppSecurityConfig,
    onSetupPin: () -> Unit,
    onSetupPassword: () -> Unit,
    onRemoveLock: () -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onSetAutoLockTimeout: (Long) -> Unit,
    onToggleRecentAppsHidden: (Boolean) -> Unit,
    onToggleScreenshotBlocked: (Boolean) -> Unit,
    onSetNotificationPrivacy: (NotificationPrivacyLevel) -> Unit,
    onGenerateRecoveryKey: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("security_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "নিরাপত্তা ও গোপনীয়তা",
                    style = KoriTypographyTokens.ScreenTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // App Lock Section
                Text(
                    text = "অ্যাপ লক সুরক্ষা",
                    style = KoriTypographyTokens.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                when (securityConfig.lockType) {
                    AppLockType.NONE -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onSetupPin,
                                modifier = Modifier.weight(1f).testTag("setup_pin_btn")
                            ) {
                                Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PIN সেট করুন", style = KoriTypographyTokens.PrimaryButton.copy(fontSize = 14.sp))
                            }
                            OutlinedButton(
                                onClick = onSetupPassword,
                                modifier = Modifier.weight(1f).testTag("setup_password_btn")
                            ) {
                                Icon(Icons.Default.Password, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("পাসওয়ার্ড", style = KoriTypographyTokens.PrimaryButton.copy(fontSize = 14.sp))
                            }
                        }
                    }
                    AppLockType.PIN -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("৬ অঙ্কের PIN সক্রিয়", style = KoriTypographyTokens.EntryTitle, color = MaterialTheme.colorScheme.onSurface)
                                    Text("অ্যাপটি খুলতে PIN প্রয়োজন", style = KoriTypographyTokens.Metadata, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = onRemoveLock) {
                                    Text("লক বন্ধ করুন", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                    AppLockType.PASSWORD -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("পাসওয়ার্ড সক্রিয়", style = KoriTypographyTokens.EntryTitle, color = MaterialTheme.colorScheme.onSurface)
                                    Text("অ্যাপটি খুলতে পাসওয়ার্ড প্রয়োজন", style = KoriTypographyTokens.Metadata, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = onRemoveLock) {
                                    Text("লক বন্ধ করুন", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                if (securityConfig.lockType != AppLockType.NONE) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    // Biometrics toggle
                    SecurityToggleItem(
                        icon = Icons.Default.Fingerprint,
                        title = "বায়োমেট্রিক আনলক",
                        subtitle = "ফিঙ্গারপ্রিন্ট বা ফেস দিয়ে দ্রুত খুলুন",
                        checked = securityConfig.isBiometricEnabled,
                        onCheckedChange = onToggleBiometric
                    )

                    // Auto-lock timeout
                    Text(
                        text = "স্বয়ংক্রিয় লক সময়",
                        style = KoriTypographyTokens.SectionTitle,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val timeouts = listOf(0L to "এখনই", 30L to "৩০ সেকেন্ড", 60L to "১ মিনিট", 300L to "৫ মিনিট", 900L to "১৫ মিনিট")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        timeouts.take(3).forEach { (seconds, label) ->
                            FilterChip(
                                selected = (securityConfig.autoLockTimeoutSeconds == seconds),
                                onClick = { onSetAutoLockTimeout(seconds) },
                                label = { Text(label, style = KoriTypographyTokens.Metadata.copy(fontSize = 11.sp)) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        timeouts.drop(3).forEach { (seconds, label) ->
                            FilterChip(
                                selected = (securityConfig.autoLockTimeoutSeconds == seconds),
                                onClick = { onSetAutoLockTimeout(seconds) },
                                label = { Text(label, style = KoriTypographyTokens.Metadata.copy(fontSize = 11.sp)) }
                            )
                        }
                    }

                    // Recovery key action
                    OutlinedButton(
                        onClick = onGenerateRecoveryKey,
                        modifier = Modifier.fillMaxWidth().testTag("generate_recovery_key_btn")
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("রিকভারি কী দেখুন বা তৈরি করুন", style = KoriTypographyTokens.PrimaryButton.copy(fontSize = 14.sp))
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Privacy Protections
                Text(
                    text = "গোপনীয়তা সুরক্ষা",
                    style = KoriTypographyTokens.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                SecurityToggleItem(
                    icon = Icons.Default.VisibilityOff,
                    title = "Recent apps প্রিভিউ লুকান",
                    subtitle = "অ্যাপ পরিবর্তনের তালিকায় আর্থিক হিসাব ঢেকে রাখুন",
                    checked = securityConfig.isRecentAppsPreviewHidden,
                    onCheckedChange = onToggleRecentAppsHidden
                )

                SecurityToggleItem(
                    icon = Icons.Default.Screenshot,
                    title = "স্ক্রিনশট বন্ধ করুন",
                    subtitle = "অ্যাপ চলাকালীন স্ক্রিনশট ও রেকর্ডিং ব্লক করুন",
                    checked = securityConfig.isScreenshotBlocked,
                    onCheckedChange = onToggleScreenshotBlocked
                )

                // Notification Privacy
                Text(
                    text = "নোটিফিকেশন গোপনীয়তা",
                    style = KoriTypographyTokens.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val levels = listOf(
                        NotificationPrivacyLevel.FULL to "সম্পূর্ণ তথ্য (টাকার পরিমাণসহ)",
                        NotificationPrivacyLevel.HIDE_AMOUNT to "পরিমাণ লুকান (সংবেদনশীল তথ্য ছাড়া)",
                        NotificationPrivacyLevel.PRIVATE_ONLY to "শুধু \"কড়ি\" দেখান"
                    )
                    levels.forEach { (level, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSetNotificationPrivacy(level) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (securityConfig.notificationPrivacy == level),
                                onClick = { onSetNotificationPrivacy(level) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(desc, style = KoriTypographyTokens.SecondaryBody, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Storage Boundary Notice
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("নিরাপত্তা পরিধি ও স্টোরেজ:", style = KoriTypographyTokens.Metadata.copy(fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                        Text("• অ্যাপ লক: ${if (securityConfig.lockType != AppLockType.NONE) "সক্রিয়" else "বন্ধ"}", style = KoriTypographyTokens.Metadata.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• ডেটাবেস এনক্রিপশন: এখনো পূর্ণভাবে যুক্ত হয়নি", style = KoriTypographyTokens.Metadata.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• ক্লাউড রিকভারি: এখনো সংযুক্ত নয়", style = KoriTypographyTokens.Metadata.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss) {
                        Text("সম্পন্ন", style = KoriTypographyTokens.PrimaryButton)
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = KoriTypographyTokens.EntryTitle.copy(fontSize = 15.sp), color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = KoriTypographyTokens.Metadata, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
