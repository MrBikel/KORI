package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aistudio.moneydiary.mndytr.domain.model.DebtRecordItem
import com.aistudio.moneydiary.mndytr.domain.model.EarnedReceivableItem
import com.aistudio.moneydiary.mndytr.domain.model.PartyFinancialProfile
import com.aistudio.moneydiary.mndytr.ui.theme.LightKoriExpense
import com.aistudio.moneydiary.mndytr.ui.theme.LightKoriIncome
import com.aistudio.moneydiary.mndytr.ui.theme.ReceivableBlue
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@Composable
fun AddBorrowDebtDialog(
    onConfirm: (partyName: String, principalPaisa: Long, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var partyName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("আমি ঋণ নিয়েছি", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text("কার কাছ থেকে ঋণ নিয়েছেন?") },
                    placeholder = { Text("উদা: রহিম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("borrow_party_input")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("ঋণের পরিমাণ (টাকা)") },
                    placeholder = { Text("উদা: 5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("borrow_amount_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("নোট বা বিবরণ (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (partyName.isNotBlank() && amount > 0L) {
                        onConfirm(partyName.trim(), amount * 100L, notes.trim().takeIf { it.isNotEmpty() })
                    }
                },
                enabled = partyName.isNotBlank() && (amountText.toLongOrNull() ?: 0L) > 0L,
                modifier = Modifier.testTag("borrow_confirm_button")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun AddLendLoanDialog(
    onConfirm: (partyName: String, principalPaisa: Long, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var partyName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("কাউকে ধার দিয়েছি", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text("কাকে ধার দিয়েছেন?") },
                    placeholder = { Text("উদা: করিম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("lend_party_input")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("ধারের পরিমাণ (টাকা)") },
                    placeholder = { Text("উদা: 3000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("lend_amount_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("নোট বা বিবরণ (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (partyName.isNotBlank() && amount > 0L) {
                        onConfirm(partyName.trim(), amount * 100L, notes.trim().takeIf { it.isNotEmpty() })
                    }
                },
                enabled = partyName.isNotBlank() && (amountText.toLongOrNull() ?: 0L) > 0L,
                modifier = Modifier.testTag("lend_confirm_button")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun AddEarnedReceivableDialog(
    onConfirm: (partyName: String, title: String, agreedAmountPaisa: Long, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var partyName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("কাজের পাওনা যোগ করুন", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text("ক্লায়েন্ট বা প্রতিষ্ঠানের নাম") },
                    placeholder = { Text("উদা: ABC Studio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("earned_party_input")
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("কাজের শিরোনাম বা বিবরণ") },
                    placeholder = { Text("উদা: Article Editing") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("earned_title_input")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("চুক্তিবদ্ধ টাকার পরিমাণ") },
                    placeholder = { Text("উদা: 5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("earned_amount_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("নোট (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (partyName.isNotBlank() && title.isNotBlank() && amount > 0L) {
                        onConfirm(partyName.trim(), title.trim(), amount * 100L, notes.trim().takeIf { it.isNotEmpty() })
                    }
                },
                enabled = partyName.isNotBlank() && title.isNotBlank() && (amountText.toLongOrNull() ?: 0L) > 0L,
                modifier = Modifier.testTag("earned_confirm_button")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun RecordDebtPaymentDialog(
    item: DebtRecordItem,
    isLoanRecovery: Boolean = false,
    onConfirm: (amountPaisa: Long, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val remainingTaka = item.remainingPaisa / 100L
    val title = if (isLoanRecovery) "ধারের টাকা ফেরত গ্রহণ" else "ঋণ পরিশোধ"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "পক্ষ: ${item.partyName} (বাকি: ৳$remainingTaka)",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("পরিশোধের পরিমাণ (টাকা)") },
                    placeholder = { Text("সর্বোচ্চ $remainingTaka") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("repay_amount_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("নোট (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount in 1..remainingTaka) {
                        onConfirm(amount * 100L, notes.trim().takeIf { it.isNotEmpty() })
                    }
                },
                enabled = (amountText.toLongOrNull() ?: 0L) in 1..remainingTaka,
                modifier = Modifier.testTag("repay_confirm_button")
            ) {
                Text("পরিশোধ নিশ্চিত করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun RecordReceivablePaymentDialog(
    item: EarnedReceivableItem,
    onConfirm: (amountPaisa: Long, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val remainingTaka = item.remainingPaisa / 100L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("কাজের পাওনা আদায়", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${item.title} (${item.partyName}) — বাকি: ৳$remainingTaka",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("আদায়কৃত পরিমাণ (টাকা)") },
                    placeholder = { Text("সর্বোচ্চ $remainingTaka") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("collect_receivable_amount_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("নোট (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount in 1..remainingTaka) {
                        onConfirm(amount * 100L, notes.trim().takeIf { it.isNotEmpty() })
                    }
                },
                enabled = (amountText.toLongOrNull() ?: 0L) in 1..remainingTaka,
                modifier = Modifier.testTag("collect_receivable_confirm_button")
            ) {
                Text("আদায় নিশ্চিত করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun PartyProfileDialog(
    profile: PartyFinancialProfile,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = profile.party.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (profile.party.partyType == "ORGANIZATION") "প্রতিষ্ঠান" else "ব্যক্তি",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                HorizontalDivider()

                // Balance summary
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (profile.outstandingBorrowedPaisa > 0L) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("বর্তমান ঋণ:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                BengaliFormatter.formatPaisa(profile.outstandingBorrowedPaisa, useBengaliDigits, showDecimals = false),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = LightKoriExpense)
                            )
                        }
                    }

                    if (profile.outstandingLentPaisa > 0L) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ধার দেওয়া থেকে পাওনা:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                BengaliFormatter.formatPaisa(profile.outstandingLentPaisa, useBengaliDigits, showDecimals = false),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = ReceivableBlue)
                            )
                        }
                    }

                    if (profile.outstandingEarnedPaisa > 0L) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("কাজের বকেয়া পাওনা:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                BengaliFormatter.formatPaisa(profile.outstandingEarnedPaisa, useBengaliDigits, showDecimals = false),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = LightKoriIncome)
                            )
                        }
                    }

                    if (profile.outstandingBorrowedPaisa == 0L && profile.outstandingLentPaisa == 0L && profile.outstandingEarnedPaisa == 0L) {
                        Text(
                            text = "কোনো চলমান দেনা বা পাওনা নেই।",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("বন্ধ করুন")
                }
            }
        }
    }
}
