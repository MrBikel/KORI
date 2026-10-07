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
import androidx.compose.material3.ButtonDefaults
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
import com.aistudio.moneydiary.mndytr.ui.theme.TransferTeal
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@Composable
fun TransferDialog(
    accounts: List<AccountEntity>,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (sourceAccountId: String, destinationAccountId: String, amountPaisa: Long, feePaisa: Long, description: String) -> Unit
) {
    var sourceAccountId by remember {
        mutableStateOf(accounts.firstOrNull()?.id ?: "")
    }
    var destinationAccountId by remember {
        mutableStateOf(accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id ?: "")
    }
    var amountInput by remember { mutableStateOf("") }
    var feeInput by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "অ্যাকাউন্ট স্থানান্তর (ট্রান্সফার)",
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
                    text = "কোন অ্যাকাউন্ট থেকে যাবে? (উৎস)",
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
                            selected = sourceAccountId == account.id,
                            onClick = { sourceAccountId = account.id },
                            label = { Text(account.name) }
                        )
                    }
                }

                Text(
                    text = "কোন অ্যাকাউন্টে জমা হবে? (গন্তব্য)",
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
                            selected = destinationAccountId == account.id,
                            onClick = { destinationAccountId = account.id },
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
                    label = "স্থানান্তরের পরিমাণ",
                    useBengaliDigits = useBengaliDigits,
                    quickAmounts = listOf(100L, 500L, 1000L, 2000L, 5000L)
                )

                BengaliAmountField(
                    value = feeInput,
                    onValueChange = { feeInput = it },
                    label = "স্থানান্তর ফি / ক্যাশআউট চার্জ (যদি থাকে)",
                    useBengaliDigits = useBengaliDigits,
                    quickAmounts = listOf(10L, 15L, 20L)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("বিবরণ (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: ব্যাংক থেকে বিকাশে ক্যাশ-ইন") },
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
                    if (sourceAccountId == destinationAccountId) {
                        errorMessage = "উৎস এবং গন্তব্য অ্যাকাউন্ট ভিন্ন হতে হবে"
                        return@Button
                    }
                    val amountPaisa = BengaliFormatter.parseAmountToPaisa(amountInput)
                    if (amountPaisa <= 0L) {
                        errorMessage = "সঠিক টাকার পরিমাণ লিখুন"
                        return@Button
                    }
                    val feePaisa = if (feeInput.isNotBlank()) {
                        BengaliFormatter.parseAmountToPaisa(feeInput)
                    } else 0L

                    val src = accounts.find { it.id == sourceAccountId }?.name ?: "অ্যাকাউন্ট"
                    val dst = accounts.find { it.id == destinationAccountId }?.name ?: "অ্যাকাউন্ট"
                    val finalDesc = description.ifBlank { "$src থেকে $dst-এ স্থানান্তর" }

                    onConfirm(sourceAccountId, destinationAccountId, amountPaisa, feePaisa, finalDesc)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TransferTeal),
                modifier = Modifier.testTag("confirm_transfer_button")
            ) {
                Text("স্থানান্তর নিশ্চিত করুন")
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
