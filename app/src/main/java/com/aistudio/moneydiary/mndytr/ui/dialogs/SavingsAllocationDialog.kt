package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aistudio.moneydiary.mndytr.ui.components.BengaliNumberTextField
import com.aistudio.moneydiary.mndytr.ui.state.AccountWithBalance
import com.aistudio.moneydiary.mndytr.ui.theme.IncomeGreen
import com.aistudio.moneydiary.mndytr.ui.theme.PrimaryEmerald
import com.aistudio.moneydiary.mndytr.ui.theme.SecondaryGold
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsAllocationDialog(
    accountsWithBalance: List<AccountWithBalance>,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (sourceAccountId: String, destAccountId: String, amountPaisa: Long, note: String?) -> Unit
) {
    val liquidAccounts = accountsWithBalance.filter { it.account.type != "SAVINGS" }
    val savingsAccounts = accountsWithBalance.filter { it.account.type == "SAVINGS" }

    var selectedSourceId by remember {
        mutableStateOf(liquidAccounts.firstOrNull()?.account?.id ?: accountsWithBalance.firstOrNull()?.account?.id ?: "")
    }
    var selectedDestId by remember {
        mutableStateOf(savingsAccounts.firstOrNull()?.account?.id ?: accountsWithBalance.getOrNull(1)?.account?.id ?: "")
    }

    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val amountPaisa = BengaliFormatter.parseAmountToPaisa(amountText)
    val isValid = amountPaisa > 0L && selectedSourceId.isNotEmpty() && selectedDestId.isNotEmpty() && selectedSourceId != selectedDestId

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("savings_allocation_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SecondaryGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = "সঞ্চয়",
                            tint = PrimaryEmerald
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "সঞ্চয়ে স্থানান্তর",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryEmerald
                        )
                        Text(
                            text = "সাধারণ ব্যালেন্স থেকে সঞ্চয়ে আলাদা করুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Source Account Selection
                Text(
                    text = "উৎস হিসাব (যেখান থেকে কমবে)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accountsWithBalance.filter { it.account.id != selectedDestId }.forEach { acc ->
                        FilterChip(
                            selected = selectedSourceId == acc.account.id,
                            onClick = { selectedSourceId = acc.account.id },
                            label = { Text(acc.account.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Destination Savings Account Selection
                Text(
                    text = "সঞ্চয় হিসাব (যেখানে জমা হবে)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accountsWithBalance.filter { it.account.id != selectedSourceId }.forEach { acc ->
                        FilterChip(
                            selected = selectedDestId == acc.account.id,
                            onClick = { selectedDestId = acc.account.id },
                            label = { Text(acc.account.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount input with Bengali/English support
                BengaliNumberTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        validationError = null
                    },
                    label = "সঞ্চয়ের পরিমাণ (৳)",
                    testTag = "savings_amount_input"
                )

                // Quick presets
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(500L, 1000L, 2000L, 5000L, 10000L).forEach { preset ->
                        FilterChip(
                            selected = false,
                            onClick = { amountText = (preset).toString() },
                            label = { Text("+${BengaliFormatter.formatPaisa(preset * 100L, useBengaliDigits, false)}") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optional Note
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("নোট / সঞ্চয়ের উদ্দেশ্য (ঐচ্ছিক)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_note_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Accounting Separation Preview
                if (amountPaisa > 0L) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "হিসাবের প্রভাব নিশ্চিতকরণ:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryEmerald
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• উৎস থেকে কমবে: ${BengaliFormatter.formatPaisa(amountPaisa, useBengaliDigits)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• সঞ্চয়ে জমা হবে: ${BengaliFormatter.formatPaisa(amountPaisa, useBengaliDigits)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = IncomeGreen
                            )
                            Text(
                                text = "• আয়/ব্যয় প্রভাব: ৳০ (সম্পদ স্থানান্তর, কোনো খরচ নয়)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                validationError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.testTag("cancel_savings_button")
                    ) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (!isValid) {
                                validationError = if (selectedSourceId == selectedDestId) "উৎস ও গন্তব্য ভিন্ন হতে হবে" else "সঠিক টাকার পরিমাণ দিন"
                                return@Button
                            }
                            isSubmitting = true
                            onConfirm(selectedSourceId, selectedDestId, amountPaisa, noteText.ifBlank { null })
                        },
                        enabled = isValid && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                        modifier = Modifier.testTag("confirm_savings_button")
                    ) {
                        Text(if (isSubmitting) "সংরক্ষণ হচ্ছে..." else "সঞ্চয়ে স্থানান্তর করুন")
                    }
                }
            }
        }
    }
}
