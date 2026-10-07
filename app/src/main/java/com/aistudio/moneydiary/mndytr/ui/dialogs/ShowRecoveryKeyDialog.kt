package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aistudio.moneydiary.mndytr.ui.theme.KoriTypographyTokens

@Composable
fun ShowRecoveryKeyDialog(
    recoveryKey: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("show_recovery_key_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "গোপন রিকভারি কী",
                    style = KoriTypographyTokens.ScreenTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "PIN বা পাসওয়ার্ড ভুলে গেলে এই কী দিয়ে অ্যাপ আনলক করতে পারবেন। এটি লিখে বা সুরক্ষিত জায়গায় সংরক্ষণ করে রাখুন।",
                    style = KoriTypographyTokens.SecondaryBody,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = recoveryKey,
                            style = KoriTypographyTokens.ScreenTitle.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = "সতর্কবার্তা: রিকভারি কী হারালে এবং PIN বা পাসওয়ার্ড ভুলে গেলে স্থানীয় তথ্য পুনরুদ্ধার করা সম্ভব নাও হতে পারে।",
                    style = KoriTypographyTokens.Metadata,
                    color = MaterialTheme.colorScheme.error
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, modifier = Modifier.testTag("dismiss_recovery_key_btn")) {
                        Text("সংরক্ষণ করেছি", style = KoriTypographyTokens.PrimaryButton)
                    }
                }
            }
        }
    }
}
