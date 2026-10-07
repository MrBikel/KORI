package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aistudio.moneydiary.mndytr.ui.theme.KoriTypographyTokens

@Composable
fun VerifyRecoveryKeyDialog(
    onVerify: (String) -> Boolean,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var recoveryKeyInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("verify_recovery_key_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "রিকভারি কী দিয়ে আনলক",
                    style = KoriTypographyTokens.ScreenTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "আপনার পূর্বে সংরক্ষিত রিকভারি কী (যেমন: KORI-XXXX-XXXX) প্রবেশ করান:",
                    style = KoriTypographyTokens.SecondaryBody,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = recoveryKeyInput,
                    onValueChange = { recoveryKeyInput = it },
                    label = { Text("রিকভারি কী", style = KoriTypographyTokens.FormLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("recovery_key_input")
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = KoriTypographyTokens.Metadata)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", style = KoriTypographyTokens.PrimaryButton)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val success = onVerify(recoveryKeyInput)
                            if (success) {
                                onSuccess()
                                onDismiss()
                            } else {
                                errorMessage = "ভুল রিকভারি কী। অনুগ্রহ করে সঠিক কী প্রবেশ করান।"
                            }
                        },
                        modifier = Modifier.testTag("submit_recovery_key_btn")
                    ) {
                        Text("আনলক করুন", style = KoriTypographyTokens.PrimaryButton)
                    }
                }
            }
        }
    }
}
