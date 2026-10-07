package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aistudio.moneydiary.mndytr.data.local.entity.AccountEntity
import com.aistudio.moneydiary.mndytr.ui.components.BengaliAmountField
import com.aistudio.moneydiary.mndytr.ui.theme.AdvanceAmber
import com.aistudio.moneydiary.mndytr.ui.theme.AdvanceAmberContainer
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@Composable
fun AddWorkAdvanceDialog(
    accounts: List<AccountEntity>,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (clientName: String, workTitle: String, agreedTotalPaisa: Long, advancePaisa: Long, destinationAccountId: String, notes: String?) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var workTitle by remember { mutableStateOf("") }
    var agreedTotalInput by remember { mutableStateOf("") }
    var advanceInput by remember { mutableStateOf("") }
    var selectedAccountId by remember {
        mutableStateOf(accounts.firstOrNull()?.id ?: "")
    }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "কাজের চুক্তি ও অগ্রিম গ্রহণ",
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
                // Info Box explaining strict financial invariant
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AdvanceAmberContainer)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "নীতিমালা: অগ্রিম টাকা ক্যাশে জমা হবে, কিন্তু কাজ শেষ না হওয়া পর্যন্ত এটি আয় নয়—'অগ্রিম দায়' হিসেবে সংরক্ষিত থাকবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedTextField(
                    value = clientName,
                    onValueChange = {
                        clientName = it
                        errorMessage = null
                    },
                    label = { Text("ক্লায়েন্ট / ব্যক্তির নাম") },
                    placeholder = { Text("যেমন: রাকিব ভাই, আপওয়ার্ক ক্লায়েন্ট") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("client_name_input")
                )

                OutlinedTextField(
                    value = workTitle,
                    onValueChange = {
                        workTitle = it
                        errorMessage = null
                    },
                    label = { Text("কাজের শিরোনাম / বিবরণ") },
                    placeholder = { Text("যেমন: ওয়েবসাইট ডিজাইন, ফটো এডিটিং") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("work_title_input")
                )

                BengaliAmountField(
                    value = agreedTotalInput,
                    onValueChange = {
                        agreedTotalInput = it
                        errorMessage = null
                    },
                    label = "মোট চুক্তির মূল্য",
                    useBengaliDigits = useBengaliDigits,
                    quickAmounts = listOf(500L, 1000L, 2000L, 5000L, 10000L)
                )

                BengaliAmountField(
                    value = advanceInput,
                    onValueChange = {
                        advanceInput = it
                        errorMessage = null
                    },
                    label = "প্রাপ্ত অগ্রিম / টোকেন টাকা",
                    useBengaliDigits = useBengaliDigits,
                    quickAmounts = listOf(100L, 200L, 500L, 1000L, 2000L)
                )

                Text(
                    text = "অগ্রিমের টাকা কোন অ্যাকাউন্টে জমা হয়েছে?",
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

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("নোট / শর্তাবলী (ঐচ্ছিক)") },
                    singleLine = false,
                    maxLines = 2,
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
                    if (clientName.isBlank()) {
                        errorMessage = "ক্লায়েন্টের নাম লিখুন"
                        return@Button
                    }
                    if (workTitle.isBlank()) {
                        errorMessage = "কাজের বিবরণ লিখুন"
                        return@Button
                    }
                    val totalPaisa = BengaliFormatter.parseAmountToPaisa(agreedTotalInput)
                    if (totalPaisa <= 0L) {
                        errorMessage = "চুক্তির সঠিক মোট মূল্য লিখুন"
                        return@Button
                    }
                    val advPaisa = if (advanceInput.isNotBlank()) {
                        BengaliFormatter.parseAmountToPaisa(advanceInput)
                    } else 0L

                    if (advPaisa > totalPaisa) {
                        errorMessage = "অগ্রিম টাকা চুক্তির মোট মূল্যের চেয়ে বেশি হতে পারে না"
                        return@Button
                    }
                    if (advPaisa > 0 && selectedAccountId.isBlank()) {
                        errorMessage = "অ্যাকাউন্ট নির্বাচন করুন"
                        return@Button
                    }

                    onConfirm(clientName, workTitle, totalPaisa, advPaisa, selectedAccountId, notes.ifBlank { null })
                },
                colors = ButtonDefaults.buttonColors(containerColor = AdvanceAmber),
                modifier = Modifier.testTag("confirm_advance_button")
            ) {
                Text("চুক্তি ও অগ্রিম সংরক্ষণ")
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
