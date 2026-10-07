package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aistudio.moneydiary.mndytr.domain.service.SecurityService
import com.aistudio.moneydiary.mndytr.ui.theme.KoriTypographyTokens

@Composable
fun SetupPinDialog(
    onSavePin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var weakPinWarning by remember { mutableStateOf<String?>(null) }
    var forceAllowWeak by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("setup_pin_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "৬ অঙ্কের PIN সেট করুন",
                    style = KoriTypographyTokens.ScreenTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "কড়ি খোলার জন্য ৬ অঙ্কের একটি সুরক্ষিত PIN নির্বাচন করুন।",
                    style = KoriTypographyTokens.SecondaryBody,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            pin = it
                            weakPinWarning = SecurityService.checkPinWeakness(it)
                            forceAllowWeak = false
                        }
                    },
                    label = { Text("৬ অঙ্কের PIN", style = KoriTypographyTokens.FormLabel) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("pin_input_1")
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            confirmPin = it
                        }
                    },
                    label = { Text("PIN নিশ্চিত করুন", style = KoriTypographyTokens.FormLabel) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("pin_input_confirm")
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = KoriTypographyTokens.Metadata)
                }

                if (weakPinWarning != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(weakPinWarning!!, style = KoriTypographyTokens.Metadata, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", style = KoriTypographyTokens.PrimaryButton)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (pin.length != 6) {
                                errorMessage = "PIN অবশ্যই ৬ অঙ্কের হতে হবে।"
                                return@Button
                            }
                            if (pin != confirmPin) {
                                errorMessage = "উভয় PIN মেলেনি।"
                                return@Button
                            }
                            val weak = SecurityService.checkPinWeakness(pin)
                            if (weak != null && !forceAllowWeak) {
                                weakPinWarning = "$weak তবুও এই PIN ব্যবহার করতে চাইলে আবার চাপুন।"
                                forceAllowWeak = true
                                return@Button
                            }
                            onSavePin(pin)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("save_pin_submit_btn")
                    ) {
                        Text("সংরক্ষণ করুন", style = KoriTypographyTokens.PrimaryButton)
                    }
                }
            }
        }
    }
}
