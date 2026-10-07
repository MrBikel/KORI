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
import com.aistudio.moneydiary.mndytr.ui.state.WorkDisplayItem
import com.aistudio.moneydiary.mndytr.ui.theme.IncomeGreen
import com.aistudio.moneydiary.mndytr.ui.theme.IncomeGreenContainer
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@Composable
fun SettleWorkDialog(
    workDisplay: WorkDisplayItem,
    accounts: List<AccountEntity>,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (workId: String, destinationAccountId: String, finalPaymentPaisa: Long) -> Unit
) {
    val proj = workDisplay.projection
    val unearnedAdvance = proj.advanceReceivedPaisa - proj.advanceEarnedPaisa - proj.advanceReturnedPaisa
    val remainingExpected = proj.expectedRemainingPaymentPaisa

    var selectedAccountId by remember {
        mutableStateOf(workDisplay.work.preferredAccountId ?: accounts.firstOrNull()?.id ?: "")
    }
    var finalPaymentInput by remember {
        mutableStateOf(
            if (remainingExpected > 0) {
                if (useBengaliDigits) BengaliFormatter.toBengaliDigits((remainingExpected / 100).toString())
                else (remainingExpected / 100).toString()
            } else "0"
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "কাজ সম্পন্ন ও চূড়ান্ত নিষ্পত্তি",
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
                // Info Box explaining settlement
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(IncomeGreenContainer)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "কাজ: ${workDisplay.work.title} (ক্লায়েন্ট: ${workDisplay.clientName})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "মোট চুক্তি: ${BengaliFormatter.formatPaisa(proj.agreedTotalPricePaisa, useBengaliDigits)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "পূর্বে প্রাপ্ত অগ্রিম: ${BengaliFormatter.formatPaisa(proj.advanceReceivedPaisa, useBengaliDigits)} (যা এখন আয় হিসেবে স্বীকৃত হবে)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                BengaliAmountField(
                    value = finalPaymentInput,
                    onValueChange = {
                        finalPaymentInput = it
                        errorMessage = null
                    },
                    label = "অবশিষ্ট চূড়ান্ত পরিশোধের পরিমাণ",
                    useBengaliDigits = useBengaliDigits,
                    quickAmounts = listOf(100L, 200L, 500L, 1000L)
                )

                Text(
                    text = "চূড়ান্ত টাকা কোন অ্যাকাউন্টে গ্রহণ করছেন?",
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
                    val finalPaisa = if (finalPaymentInput.isNotBlank()) {
                        BengaliFormatter.parseAmountToPaisa(finalPaymentInput) ?: 0L
                    } else 0L

                    if (finalPaisa > 0 && selectedAccountId.isBlank()) {
                        errorMessage = "টাকা গ্রহণের অ্যাকাউন্ট নির্বাচন করুন"
                        return@Button
                    }

                    onConfirm(workDisplay.work.id, selectedAccountId, finalPaisa)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                modifier = Modifier.testTag("confirm_settle_work_button")
            ) {
                Text("চূড়ান্ত নিষ্পত্তি ও সম্পন্ন")
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
