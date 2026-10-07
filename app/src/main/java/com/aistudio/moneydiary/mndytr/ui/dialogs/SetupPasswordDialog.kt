package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aistudio.moneydiary.mndytr.ui.theme.KoriTypographyTokens

@Composable
fun SetupPasswordDialog(
    onSavePassword: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("setup_password_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "পাসওয়ার্ড সেট করুন",
                    style = KoriTypographyTokens.ScreenTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "কমপক্ষে ৮ অক্ষরের বাংলা বা ইংরেজি পাসওয়ার্ড দিন।",
                    style = KoriTypographyTokens.SecondaryBody,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("নতুন পাসওয়ার্ড", style = KoriTypographyTokens.FormLabel) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "লুকান" else "দেখান"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("password_input_1")
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("পাসওয়ার্ড নিশ্চিত করুন", style = KoriTypographyTokens.FormLabel) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("password_input_confirm")
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
                            if (password.length < 8) {
                                errorMessage = "পাসওয়ার্ড কমপক্ষে ৮ অক্ষরের হতে হবে।"
                                return@Button
                            }
                            if (password != confirmPassword) {
                                errorMessage = "উভয় পাসওয়ার্ড মেলেনি।"
                                return@Button
                            }
                            onSavePassword(password)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("save_password_submit_btn")
                    ) {
                        Text("সংরক্ষণ করুন", style = KoriTypographyTokens.PrimaryButton)
                    }
                }
            }
        }
    }
}
