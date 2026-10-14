package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aistudio.moneydiary.mndytr.ui.state.DiaryUiState
import com.aistudio.moneydiary.mndytr.ui.theme.KoriTypographyTokens

@Composable
fun GoogleCloudDialog(
    state: DiaryUiState,
    onConnectAccount: (String, String) -> Unit = { _, _ -> },
    onDisconnectAccount: () -> Unit = {},
    onSetDriveAuthorized: (Boolean) -> Unit = {},
    onSetAutoBackupFrequency: (com.aistudio.moneydiary.mndytr.domain.service.AutoBackupFrequency) -> Unit = {},
    onTriggerDriveBackup: () -> Unit = {},
    onTriggerDriveRestore: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp)
                .testTag("google_cloud_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column {
                            Text(
                                text = "ক্লাউড সিঙ্ক অনুপলব্ধ",
                                style = KoriTypographyTokens.ScreenTitle.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "অফলাইন-ফার্স্ট ডেটা সুরক্ষা",
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

                // Information Notice Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("cloud_unavailable_notice_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "তথ্য সুরক্ষা ও স্বচ্ছতা নীতি",
                                style = KoriTypographyTokens.SectionTitle.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Text(
                            text = "এই সংস্করণে কোনো রিমোট গুগল ড্রাইভ ক্লাউড আপলোড বা অনলাইন সার্ভার সিঙ্ক অন্তর্ভুক্ত নেই। কড়ি কোনো ভুয়া বা সিমুলেটেড ক্লাউড সংযোগ প্রদর্শন করে না।",
                            style = KoriTypographyTokens.SecondaryBody.copy(
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            lineHeight = 19.sp
                        )
                    }
                }

                // Recommended Action Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "স্থানীয় সুরক্ষিত ব্যাকআপ ব্যবহার করুন",
                                style = KoriTypographyTokens.SectionTitle.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Text(
                            text = "• সম্পূর্ণ অফলাইনে JSON ফাইল এক্সপোর্ট ও রিস্টোর করুন।\n• লেনদেন আদান-প্রদানের জন্য UTF-8 CSV ইম্পোর্ট ও এক্সপোর্ট ব্যবহার করুন।\n• ফোন পরিবর্তন বা অ্যাপ রি-ইনস্টলের সময় ব্যাকআপ ফাইল নির্বাচন করে মুহূর্তেই হিসাব উদ্ধার করুন।",
                            style = KoriTypographyTokens.SecondaryBody.copy(
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            lineHeight = 19.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_cloud_dialog_action")
                ) {
                    Text(
                        text = "ঠিক আছে, বুঝেছি",
                        style = KoriTypographyTokens.PrimaryButton
                    )
                }
            }
        }
    }
}
