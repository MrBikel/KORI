package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aistudio.moneydiary.mndytr.domain.service.AutoBackupFrequency
import com.aistudio.moneydiary.mndytr.ui.state.DiaryUiState
import com.aistudio.moneydiary.mndytr.ui.theme.KoriTypographyTokens
import com.aistudio.moneydiary.mndytr.ui.theme.LightKoriIncome
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@Composable
fun GoogleCloudDialog(
    state: DiaryUiState,
    onConnectAccount: (email: String, displayName: String?) -> Unit,
    onDisconnectAccount: () -> Unit,
    onSetDriveAuthorized: (Boolean) -> Unit,
    onSetAutoBackupFrequency: (AutoBackupFrequency) -> Unit,
    onTriggerDriveBackup: () -> Unit,
    onTriggerDriveRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    var showEmailInputDialog by remember { mutableStateOf(false) }
    var inputEmail by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("google_cloud_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column {
                            Text(
                                text = "গুগল ড্রাইভ ব্যাকআপ",
                                style = KoriTypographyTokens.ScreenTitle.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "ব্যক্তিগত ক্লাউড ডাটা সিঙ্ক",
                                style = KoriTypographyTokens.SecondaryBody.copy(
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_cloud_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Account Status Card
                val profile = state.googleProfile
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (profile.isConnected) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("google_account_status_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (profile.isConnected) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = LightKoriIncome,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = profile.displayName ?: "সংযুক্ত গুগল অ্যাকাউন্ট",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = profile.email,
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = onDisconnectAccount,
                                    modifier = Modifier.testTag("disconnect_google_button")
                                ) {
                                    Text("বিচ্ছিন্ন করুন", color = MaterialTheme.colorScheme.error)
                                }
                            }

                            // Drive Authorization Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "গুগল ড্রাইভ এক্সেস",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                    Text(
                                        text = "গোপন AppData ফোল্ডারে এনক্রিপ্টেড ব্যাকআপ সংরক্ষণ",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                Switch(
                                    checked = state.isDriveAuthorized,
                                    onCheckedChange = onSetDriveAuthorized,
                                    modifier = Modifier.testTag("drive_auth_switch")
                                )
                            }
                        } else {
                            Text(
                                text = "কোনো গুগল অ্যাকাউন্ট সংযুক্ত নেই। আপনার নিজস্ব ড্রাইভে ব্যাকআপ রাখতে গুগল অ্যাকাউন্ট যোগ করুন।",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Button(
                                onClick = { showEmailInputDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("connect_google_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("গুগল অ্যাকাউন্ট যুক্ত করুন")
                            }
                        }
                    }
                }

                // Auto Backup Frequency
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "স্বয়ংক্রিয় ক্লাউড ব্যাকআপ সময়সূচি",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AutoBackupFrequency.entries.forEach { freq ->
                            val isSelected = state.autoBackupFrequency == freq
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetAutoBackupFrequency(freq) },
                                label = { Text(freq.labelBn) },
                                modifier = Modifier.testTag("auto_backup_freq_${freq.name.lowercase()}")
                            )
                        }
                    }
                }

                // Last backup timestamp
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "সর্বশেষ ড্রাইভ ব্যাকআপ:",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            val lastTimeText = if (state.lastCloudBackupTimeEpochMs > 0L) {
                                BengaliFormatter.formatBengaliDate(state.lastCloudBackupTimeEpochMs)
                            } else {
                                "এখনো ব্যাকআপ নেওয়া হয়নি"
                            }
                            Text(
                                text = lastTimeText,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                // Action Buttons: Backup Now & Restore from Drive
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onTriggerDriveBackup,
                        enabled = state.googleProfile.isConnected && state.isDriveAuthorized,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("backup_to_drive_now_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("এখনই ড্রাইভে ব্যাকআপ নিন")
                    }

                    OutlinedButton(
                        onClick = onTriggerDriveRestore,
                        enabled = state.googleProfile.isConnected && state.isDriveAuthorized,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("restore_from_drive_button")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ড্রাইভ থেকে রিস্টোর করুন")
                    }
                }

                // Security Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "AES-256 এনক্রিপশনের মাধ্যমে ডাটা সম্পূর্ণ সুরক্ষিত থাকে।",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }

    // Sub-dialog for email input
    if (showEmailInputDialog) {
        AlertDialog(
            onDismissRequest = { showEmailInputDialog = false },
            title = { Text("গুগল অ্যাকাউন্ট দিন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("আপনার নাম") },
                        placeholder = { Text("উদা: তাহমিদ হাসান") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("google_input_name")
                    )
                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { inputEmail = it },
                        label = { Text("গুগল ইমেইল") },
                        placeholder = { Text("example@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("google_input_email")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputEmail.isNotBlank()) {
                            onConnectAccount(inputEmail.trim(), inputName.trim().ifBlank { null })
                            onSetDriveAuthorized(true)
                            showEmailInputDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_connect_google_button")
                ) {
                    Text("সংযুক্ত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailInputDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
