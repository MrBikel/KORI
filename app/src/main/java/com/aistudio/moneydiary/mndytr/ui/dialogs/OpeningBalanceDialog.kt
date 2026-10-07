package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aistudio.moneydiary.mndytr.data.local.entity.AccountEntity
import com.aistudio.moneydiary.mndytr.ui.components.BengaliAmountField
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@Composable
fun OpeningBalanceDialog(
    accounts: List<AccountEntity>,
    preSelectedAccountId: String?,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (accountId: String, amountPaisa: Long, description: String) -> Unit
) {
    var selectedAccountId by remember {
        mutableStateOf(preSelectedAccountId ?: accounts.firstOrNull()?.id ?: "")
    }
    var amountInput by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "প্রারম্ভিক স্থিতি (শুরুর ব্যালেন্স)",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "কোন অ্যাকাউন্টে প্রারম্ভিক ব্যালেন্স বসাবেন?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.forEach { account ->
                        FilterChip(
                            selected = selectedAccountId == account.id,
                            onClick = { selectedAccountId = account.id },
                            label = { Text(account.name) }
                        )
                    }
                }

                BengaliAmountField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it
                        errorMessage = null
                    },
                    label = "শুরুর ব্যালেন্স / প্রারম্ভিক টাকা",
                    useBengaliDigits = useBengaliDigits,
                    quickAmounts = listOf(500L, 1000L, 5000L, 10000L, 50000L)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("বিবরণ") },
                    placeholder = { Text("হিসাবের প্রারম্ভিক স্থিতি") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountPaisa = BengaliFormatter.parseAmountToPaisa(amountInput)
                    if (amountPaisa <= 0L) {
                        errorMessage = "সঠিক পরিমাণ টাকা লিখুন"
                        return@Button
                    }
                    if (selectedAccountId.isBlank()) {
                        errorMessage = "অ্যাকাউন্ট নির্বাচন করুন"
                        return@Button
                    }

                    val finalDesc = description.ifBlank { "হিসাবের প্রারম্ভিক স্থিতি" }
                    onConfirm(selectedAccountId, amountPaisa, finalDesc)
                },
                modifier = Modifier.testTag("confirm_opening_balance_button")
            ) {
                Text("ব্যালেন্স সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
