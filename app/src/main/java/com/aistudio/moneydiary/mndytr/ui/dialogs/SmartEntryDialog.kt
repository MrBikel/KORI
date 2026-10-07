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
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.runtime.LaunchedEffect
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
import com.aistudio.moneydiary.mndytr.data.local.entity.AccountEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import com.aistudio.moneydiary.mndytr.domain.model.LedgerEventCode
import com.aistudio.moneydiary.mndytr.ui.components.BengaliNumberTextField
import com.aistudio.moneydiary.mndytr.ui.theme.ExpenseRed
import com.aistudio.moneydiary.mndytr.ui.theme.IncomeGreen
import com.aistudio.moneydiary.mndytr.ui.theme.PrimaryEmerald
import com.aistudio.moneydiary.mndytr.ui.theme.SecondaryGold
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter
import com.aistudio.moneydiary.mndytr.ui.util.ParsedSmartEntry
import com.aistudio.moneydiary.mndytr.ui.util.SmartTextParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartEntryDialog(
    availableAccounts: List<AccountEntity>,
    availableCategories: List<CategoryEntity>,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onConfirmExpense: (accountId: String, amountPaisa: Long, categoryId: String, desc: String) -> Unit,
    onConfirmIncome: (accountId: String, amountPaisa: Long, categoryId: String, desc: String) -> Unit,
    onConfirmWorkAdvance: (clientName: String, workTitle: String, totalPaisa: Long, advancePaisa: Long, destAccountId: String) -> Unit
) {
    var rawInput by remember { mutableStateOf("") }
    var parsedResult by remember { mutableStateOf<ParsedSmartEntry?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Editable fields for user adjustments
    var editableAmountText by remember { mutableStateOf("") }
    var editableDesc by remember { mutableStateOf("") }
    var editableAccountId by remember { mutableStateOf("") }
    var editableCategoryId by remember { mutableStateOf("") }
    var editableClientName by remember { mutableStateOf("") }
    var editableWorkTitle by remember { mutableStateOf("") }

    // Parse whenever rawInput changes
    LaunchedEffect(rawInput) {
        if (rawInput.isNotBlank()) {
            val result = SmartTextParser.parse(rawInput, availableAccounts, availableCategories)
            parsedResult = result
            if (result.isValid) {
                editableAmountText = (result.amountPaisa / 100L).toString()
                editableDesc = result.description
                editableAccountId = result.suggestedAccount?.id ?: availableAccounts.firstOrNull()?.id ?: ""
                editableCategoryId = result.suggestedCategory?.id ?: availableCategories.firstOrNull()?.id ?: ""
                editableClientName = result.personName ?: "ক্লায়েন্ট"
                editableWorkTitle = result.workTitle ?: "অনলাইন কাজ"
            }
        } else {
            parsedResult = null
        }
    }

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("smart_entry_dialog")
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
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "স্মার্ট এন্ট্রি",
                            tint = PrimaryEmerald
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "স্মার্ট দ্রুত এন্ট্রি",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryEmerald
                        )
                        Text(
                            text = "এক লাইনে লিখে স্বয়ংক্রিয় হিসাব সাজান",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Shorthand examples chips
                Text(
                    text = "দ্রুত উদাহরণে ট্যাপ করুন:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("চা ১০ নগদ", "সিগারেট ২০", "রিকশা ৩০ বিকাশ", "রহিম অনলাইন কাজের টোকেন ১০০ বিকাশ", "Salary 25000 Bank").forEach { example ->
                        FilterChip(
                            selected = rawInput == example,
                            onClick = { rawInput = example },
                            label = { Text(example, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Field
                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { rawInput = it },
                    label = { Text("এখানে লিখুন (যেমন: চা ১০ নগদ)") },
                    placeholder = { Text("চা ১০ নগদ / রিকশা ৩০ বিকাশ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("smart_text_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = false,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Preview Section
                val result = parsedResult
                if (result != null) {
                    if (result.isValid) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("smart_entry_preview_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "শনাক্তকৃত লেনদেন প্রিভিউ",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryEmerald
                                    )
                                    val badgeColor = when (result.eventCode) {
                                        LedgerEventCode.TX_01_INCOME.name -> IncomeGreen
                                        LedgerEventCode.TX_03_ADVANCE_RECEIVED.name -> SecondaryGold
                                        else -> ExpenseRed
                                    }
                                    val badgeText = when (result.eventCode) {
                                        LedgerEventCode.TX_01_INCOME.name -> "আয় (Income)"
                                        LedgerEventCode.TX_03_ADVANCE_RECEIVED.name -> "কাজের অগ্রিম (Advance)"
                                        else -> "ব্যয় (Expense)"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(badgeColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Editable Amount
                                BengaliNumberTextField(
                                    value = editableAmountText,
                                    onValueChange = { editableAmountText = it },
                                    label = "টাকার পরিমাণ (৳)",
                                    testTag = "smart_editable_amount"
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Description
                                OutlinedTextField(
                                    value = editableDesc,
                                    onValueChange = { editableDesc = it },
                                    label = { Text("বিবরণ") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                if (result.isWorkAdvance) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = editableClientName,
                                        onValueChange = { editableClientName = it },
                                        label = { Text("ক্লায়েন্টের নাম") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = editableWorkTitle,
                                        onValueChange = { editableWorkTitle = it },
                                        label = { Text("কাজের শিরোনাম") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "⚠️ অ্যাকাউন্টিং নিয়ম: অগ্রিম টাকা ক্যাশে বাড়বে কিন্তু কাজ সম্পন্ন না হওয়া পর্যন্ত অর্জিত আয় হবে না।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SecondaryGold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Account Selector
                                Text(text = "হিসাব নির্বাচন:", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    availableAccounts.forEach { acc ->
                                        FilterChip(
                                            selected = editableAccountId == acc.id,
                                            onClick = { editableAccountId = acc.id },
                                            label = { Text(acc.name, fontSize = 12.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Ambiguous or invalid input message
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚠️ ${result.validationError ?: "ইনপুট বোঝা যায়নি"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "সঠিক ফরম্যাট: [বিবরণ] [টাকা] [হিসাব] (যেমন: চা ১০ নগদ বা Salary 25000 Bank)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.testTag("cancel_smart_entry_button")
                    ) {
                        Text("বাতিল")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val curResult = parsedResult ?: return@Button
                            if (!curResult.isValid) return@Button
                            val finalAmountPaisa = BengaliFormatter.parseAmountToPaisa(editableAmountText)
                            if (finalAmountPaisa <= 0L) return@Button

                            isSubmitting = true
                            when (curResult.eventCode) {
                                LedgerEventCode.TX_01_INCOME.name -> {
                                    val catId = editableCategoryId.ifEmpty { availableCategories.firstOrNull { it.isIncomeCategory }?.id ?: "" }
                                    onConfirmIncome(editableAccountId, finalAmountPaisa, catId, editableDesc)
                                }
                                LedgerEventCode.TX_03_ADVANCE_RECEIVED.name -> {
                                    // By default advance = contract value unless specified
                                    onConfirmWorkAdvance(editableClientName, editableWorkTitle, finalAmountPaisa, finalAmountPaisa, editableAccountId)
                                }
                                else -> {
                                    val catId = editableCategoryId.ifEmpty { availableCategories.firstOrNull { !it.isIncomeCategory }?.id ?: "" }
                                    onConfirmExpense(editableAccountId, finalAmountPaisa, catId, editableDesc)
                                }
                            }
                        },
                        enabled = parsedResult?.isValid == true && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                        modifier = Modifier.testTag("confirm_smart_entry_button")
                    ) {
                        Text(if (isSubmitting) "সংরক্ষণ হচ্ছে..." else "যাচাই করে সংরক্ষণ")
                    }
                }
            }
        }
    }
}
