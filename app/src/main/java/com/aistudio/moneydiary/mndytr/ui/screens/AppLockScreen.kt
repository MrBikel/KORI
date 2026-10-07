package com.aistudio.moneydiary.mndytr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.moneydiary.mndytr.domain.service.AppLockType
import com.aistudio.moneydiary.mndytr.domain.service.AppSecurityConfig
import com.aistudio.moneydiary.mndytr.domain.service.SecurityService
import com.aistudio.moneydiary.mndytr.ui.theme.KoriTypographyTokens
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter
import kotlinx.coroutines.delay

@Composable
fun AppLockScreen(
    securityConfig: AppSecurityConfig,
    onUnlockSuccess: () -> Unit,
    onOpenRecovery: () -> Unit,
    modifier: Modifier = Modifier,
    onBiometricTrigger: () -> Unit = {}
) {
    val context = LocalContext.current
    var credentialInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var remainingDelaySeconds by remember { mutableStateOf(SecurityService.getRemainingLockoutSeconds(context)) }

    LaunchedEffect(remainingDelaySeconds) {
        if (remainingDelaySeconds > 0) {
            delay(1000L)
            remainingDelaySeconds = SecurityService.getRemainingLockoutSeconds(context)
        }
    }

    // Auto trigger biometric prompt on launch for smooth UX
    LaunchedEffect(securityConfig.isBiometricEnabled) {
        if (securityConfig.isBiometricEnabled && remainingDelaySeconds == 0L) {
            onBiometricTrigger()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("app_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Minimal Kori Symbol & Wordmark
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "কড়ি",
                style = KoriTypographyTokens.Wordmark.copy(fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (securityConfig.lockType == AppLockType.PIN) "ডায়েরি খুলতে PIN দিন" else "ডায়েরি খুলতে পাসওয়ার্ড দিন",
                style = KoriTypographyTokens.ScreenTitle.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (remainingDelaySeconds > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "অতিরিক্ত ভুল চেষ্টা। অনুগ্রহ করে ${BengaliFormatter.formatNumber(remainingDelaySeconds.toInt())} সেকেন্ড পর চেষ্টা করুন।",
                        style = KoriTypographyTokens.Metadata,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                if (securityConfig.lockType == AppLockType.PIN) {
                    // PIN input
                    OutlinedTextField(
                        value = credentialInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                credentialInput = it
                                errorMessage = null
                                if (it.length == 6) {
                                    val ok = SecurityService.verifyCredential(context, it)
                                    if (ok) {
                                        onUnlockSuccess()
                                    } else {
                                        errorMessage = "ভুল PIN দেওয়া হয়েছে।"
                                        credentialInput = ""
                                        remainingDelaySeconds = SecurityService.getRemainingLockoutSeconds(context)
                                    }
                                }
                            }
                        },
                        label = { Text("৬ অঙ্কের PIN", style = KoriTypographyTokens.FormLabel) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("lock_pin_input")
                    )
                } else {
                    // Password input
                    OutlinedTextField(
                        value = credentialInput,
                        onValueChange = {
                            credentialInput = it
                            errorMessage = null
                        },
                        label = { Text("পাসওয়ার্ড", style = KoriTypographyTokens.FormLabel) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("lock_password_input")
                    )

                    Button(
                        onClick = {
                            val ok = SecurityService.verifyCredential(context, credentialInput)
                            if (ok) {
                                onUnlockSuccess()
                            } else {
                                errorMessage = "ভুল পাসওয়ার্ড দেওয়া হয়েছে।"
                                credentialInput = ""
                                remainingDelaySeconds = SecurityService.getRemainingLockoutSeconds(context)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("submit_unlock_password_btn")
                    ) {
                        Text("আনলক করুন", style = KoriTypographyTokens.PrimaryButton)
                    }
                }

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = KoriTypographyTokens.Metadata)
                }
            }

            // Biometric Action
            if (securityConfig.isBiometricEnabled) {
                OutlinedButton(
                    onClick = {
                        onBiometricTrigger()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("biometric_unlock_btn")
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("বায়োমেট্রিক দিয়ে আনলক করুন", style = KoriTypographyTokens.PrimaryButton)
                }
            }

            // Recovery key button
            TextButton(
                onClick = onOpenRecovery,
                modifier = Modifier.testTag("open_recovery_key_screen_btn")
            ) {
                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("PIN বা পাসওয়ার্ড ভুলে গেছেন?", style = KoriTypographyTokens.Metadata.copy(fontSize = 13.sp))
            }
        }
    }
}
