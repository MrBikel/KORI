package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aistudio.moneydiary.mndytr.domain.model.ConfirmationStatus
import com.aistudio.moneydiary.mndytr.ui.state.TransactionDisplayItem
import com.aistudio.moneydiary.mndytr.ui.theme.ExpenseRed
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@Composable
fun TransactionDetailDialog(
    item: TransactionDisplayItem,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onReverseRequest: () -> Unit,
    onDeleteDraftRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "লেনদেনের বিস্তারিত তথ্য",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Large Amount Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = BengaliFormatter.formatPaisa(item.amountPaisa, useBengaliDigits, showDecimals = true),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Surface(
                        color = when (item.confirmationStatus) {
                            ConfirmationStatus.CONFIRMED.name -> MaterialTheme.colorScheme.primaryContainer
                            ConfirmationStatus.DRAFT.name -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = when (item.confirmationStatus) {
                                ConfirmationStatus.CONFIRMED.name -> "নিশ্চিত লেনদেন"
                                ConfirmationStatus.DRAFT.name -> "খসড়া"
                                else -> "বাতিলকৃত / রিভার্সড"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider()

                DetailItemRow("বিবরণ", item.description)
                DetailItemRow("ইভেন্ট কোড", "${BengaliFormatter.getEventCodeBn(item.eventCode)} (${item.eventCode})")
                DetailItemRow("তারিখ ও সময়", BengaliFormatter.formatDisplayDate(item.timestampEpochMs, useBengaliDigits))

                if (item.categoryNameBn != null) {
                    DetailItemRow("ক্যাটাগরি", item.categoryNameBn)
                }

                if (item.sourceAccountName != null) {
                    DetailItemRow("উৎস অ্যাকাউন্ট", item.sourceAccountName)
                }

                if (item.destinationAccountName != null) {
                    DetailItemRow("গন্তব্য অ্যাকাউন্ট", item.destinationAccountName)
                }

                if (item.personName != null) {
                    DetailItemRow("ব্যক্তি / ক্লায়েন্ট", item.personName)
                }

                if (item.workTitle != null) {
                    DetailItemRow("কাজের শিরোনাম", item.workTitle)
                }

                if (!item.notes.isNullOrBlank()) {
                    DetailItemRow("নোট / মন্তব্য", item.notes)
                }

                if (item.originalLedgerEventId != null) {
                    DetailItemRow("মূল লেনদেন আইডি", item.originalLedgerEventId.take(12) + "...")
                }

                if (item.reversedTransactionId != null) {
                    DetailItemRow("রিভার্সকৃত লেনদেন আইডি", item.reversedTransactionId.take(12) + "...")
                }

                DetailItemRow("লেনদেন আইডি", item.id.take(12) + "...")
            }
        },
        confirmButton = {
            if (item.isDraft) {
                Button(
                    onClick = onDeleteDraftRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("delete_draft_button")
                ) {
                    Text("খসড়া মুছে ফেলুন")
                }
            } else if (item.isReversible && item.confirmationStatus == ConfirmationStatus.CONFIRMED.name) {
                Button(
                    onClick = onReverseRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("reverse_event_button")
                ) {
                    Text("লেনদেন রিভার্স করুন")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
