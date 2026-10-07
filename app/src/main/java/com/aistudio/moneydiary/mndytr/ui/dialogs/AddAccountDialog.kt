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
import com.aistudio.moneydiary.mndytr.domain.model.AccountType
import com.aistudio.moneydiary.mndytr.ui.components.BengaliAmountField
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@Composable
fun AddAccountDialog(
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, colorHex: String, initialBalancePaisa: Long) -> Unit
) {
    var accountName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.MFS_BKASH.name) }
    var initialBalanceInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val accountTypes: List<Pair<String, String>> = listOf(
        Pair(AccountType.CASH.name, "ক্যাশ / নগদ"),
        Pair(AccountType.MFS_BKASH.name, "বিকাশ (bKash)"),
        Pair(AccountType.MFS_NAGAD.name, "নগদ (Nagad)"),
        Pair(AccountType.BANK.name, "ব্যাংক অ্যাকাউন্ট"),
        Pair(AccountType.SAVINGS.name, "সঞ্চয় হিসাব")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "নতুন অ্যাকাউন্ট যোগ করুন",
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
                OutlinedTextField(
                    value = accountName,
                    onValueChange = {
                        accountName = it
                        errorMessage = null
                    },
                    label = { Text("অ্যাকাউন্টের নাম") },
                    placeholder = { Text("যেমন: রকেট, সিটি ব্যাংক, পার্সোনাল ক্যাশ") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_name_input")
                )

                Text(
                    text = "অ্যাকাউন্টের ধরন",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accountTypes.forEach { (typeKey, label) ->
                        FilterChip(
                            selected = selectedType == typeKey,
                            onClick = { selectedType = typeKey },
                            label = { Text(label) }
                        )
                    }
                }

                BengaliAmountField(
                    value = initialBalanceInput,
                    onValueChange = { initialBalanceInput = it },
                    label = "প্রারম্ভিক ব্যালেন্স (যদি থাকে)",
                    useBengaliDigits = useBengaliDigits,
                    quickAmounts = listOf(0L, 500L, 1000L, 5000L)
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
                    if (accountName.isBlank()) {
                        errorMessage = "অ্যাকাউন্টের নাম লিখুন"
                        return@Button
                    }
                    val balancePaisa = if (initialBalanceInput.isNotBlank()) {
                        BengaliFormatter.parseAmountToPaisa(initialBalanceInput) ?: 0L
                    } else 0L

                    val color = when {
                        selectedType == AccountType.CASH.name -> "#1B5E20"
                        selectedType.startsWith("MFS") -> "#D81B60"
                        selectedType == AccountType.BANK.name -> "#0288D1"
                        else -> "#00796B"
                    }

                    onConfirm(accountName.trim(), selectedType, color, balancePaisa)
                },
                modifier = Modifier.testTag("confirm_create_account_button")
            ) {
                Text("তৈরি করুন")
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
