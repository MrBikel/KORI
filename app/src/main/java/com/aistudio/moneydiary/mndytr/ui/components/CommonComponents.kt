package com.aistudio.moneydiary.mndytr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.moneydiary.mndytr.domain.model.ConfirmationStatus
import com.aistudio.moneydiary.mndytr.domain.model.LedgerEventCode
import com.aistudio.moneydiary.mndytr.ui.state.TransactionDisplayItem
import com.aistudio.moneydiary.mndytr.ui.theme.AdvanceAmber
import com.aistudio.moneydiary.mndytr.ui.theme.AdvanceAmberContainer
import com.aistudio.moneydiary.mndytr.ui.theme.ExpenseRed
import com.aistudio.moneydiary.mndytr.ui.theme.ExpenseRedContainer
import com.aistudio.moneydiary.mndytr.ui.theme.IncomeGreen
import com.aistudio.moneydiary.mndytr.ui.theme.IncomeGreenContainer
import com.aistudio.moneydiary.mndytr.ui.theme.ReceivableBlue
import com.aistudio.moneydiary.mndytr.ui.theme.ReceivableBlueContainer
import com.aistudio.moneydiary.mndytr.ui.theme.TransferTeal
import com.aistudio.moneydiary.mndytr.ui.theme.TransferTealContainer
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@Composable
fun TransactionRow(
    item: TransactionDisplayItem,
    useBengaliDigits: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = when (item.eventCode) {
        LedgerEventCode.TX_02_EXPENSE.name,
        LedgerEventCode.TX_04_ADVANCE_RETURNED.name,
        LedgerEventCode.TX_09_DEBT_REPAID.name,
        LedgerEventCode.TX_10_LENT_MONEY.name,
        LedgerEventCode.TX_13_REFUND_PAID.name,
        LedgerEventCode.TX_14F_TRANSFER_FEE.name -> true
        else -> false
    }

    val isIncome = when (item.eventCode) {
        LedgerEventCode.TX_00_OPENING_BALANCE.name,
        LedgerEventCode.TX_01_INCOME.name,
        LedgerEventCode.TX_06_RECEIVABLE_COLLECTED.name,
        LedgerEventCode.TX_11_LOAN_COLLECTED.name,
        LedgerEventCode.TX_12_REFUND_RECEIVED.name -> true
        else -> false
    }

    val isAdvance = item.eventCode == LedgerEventCode.TX_03_ADVANCE_RECEIVED.name
    val isTransfer = item.eventCode == LedgerEventCode.TX_14_TRANSFER.name
    val isReversal = item.eventCode == LedgerEventCode.TX_17_REVERSAL.name

    val amountColor = when {
        isReversal -> MaterialTheme.colorScheme.outline
        isExpense -> ExpenseRed
        isIncome -> IncomeGreen
        isAdvance -> AdvanceAmber
        isTransfer -> TransferTeal
        else -> MaterialTheme.colorScheme.onSurface
    }

    val amountPrefix = when {
        isExpense -> "- "
        isIncome || isAdvance -> "+ "
        else -> ""
    }

    val iconContainerColor = when {
        isReversal -> MaterialTheme.colorScheme.surfaceVariant
        isExpense -> ExpenseRedContainer
        isIncome -> IncomeGreenContainer
        isAdvance -> AdvanceAmberContainer
        isTransfer -> TransferTealContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val iconTintColor = when {
        isReversal -> MaterialTheme.colorScheme.onSurfaceVariant
        isExpense -> ExpenseRed
        isIncome -> IncomeGreen
        isAdvance -> AdvanceAmber
        isTransfer -> TransferTeal
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("transaction_row_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForEvent(item),
                    contentDescription = item.categoryNameBn ?: item.description,
                    tint = iconTintColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "$amountPrefix${BengaliFormatter.formatPaisa(item.amountPaisa, useBengaliDigits, showDecimals = false)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = amountColor
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Account badge
                        val accountLabel = when {
                            item.sourceAccountName != null && item.destinationAccountName != null ->
                                "${item.sourceAccountName} → ${item.destinationAccountName}"
                            item.sourceAccountName != null -> item.sourceAccountName
                            item.destinationAccountName != null -> item.destinationAccountName
                            else -> BengaliFormatter.getEventCodeBn(item.eventCode)
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = accountLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (item.confirmationStatus == ConfirmationStatus.REVERSED.name) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "বাতিলকৃত",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        } else if (item.isDraft) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "খসড়া",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = BengaliFormatter.formatDisplayDate(item.timestampEpochMs, useBengaliDigits),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

fun getIconForEvent(item: TransactionDisplayItem): ImageVector {
    val iconName = item.categoryIcon?.lowercase() ?: ""
    return when {
        iconName.contains("cafe") || iconName.contains("tea") -> Icons.Default.LocalCafe
        iconName.contains("bike") || iconName.contains("rickshaw") -> Icons.AutoMirrored.Filled.DirectionsBike
        iconName.contains("restaurant") || iconName.contains("food") -> Icons.Default.Restaurant
        iconName.contains("smoking") || iconName.contains("cigarette") -> Icons.Default.SmokingRooms
        iconName.contains("phone") || iconName.contains("recharge") -> Icons.Default.PhoneAndroid
        iconName.contains("cart") || iconName.contains("market") -> Icons.Default.ShoppingCart
        iconName.contains("bill") || iconName.contains("receipt") -> Icons.Default.Receipt
        item.eventCode == LedgerEventCode.TX_03_ADVANCE_RECEIVED.name -> Icons.Default.Work
        item.eventCode == LedgerEventCode.TX_14_TRANSFER.name -> Icons.Default.SwapHoriz
        item.eventCode == LedgerEventCode.TX_00_OPENING_BALANCE.name -> Icons.Default.AccountBalanceWallet
        item.eventCode == LedgerEventCode.TX_01_INCOME.name -> Icons.Default.AttachMoney
        else -> Icons.Default.AttachMoney
    }
}

@Composable
fun BengaliNumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "টাকার পরিমাণ (৳)",
    modifier: Modifier = Modifier,
    testTag: String = "bengali_number_text_field"
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Text(
                text = "৳",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
            )
        },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

@Composable
fun BengaliAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "টাকার পরিমাণ",
    useBengaliDigits: Boolean = true,
    modifier: Modifier = Modifier,
    quickAmounts: List<Long> = listOf(10L, 20L, 50L, 100L, 500L)
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = {
                Text(
                    text = "৳",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                )
            },
            placeholder = {
                Text(if (useBengaliDigits) "০" else "0")
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("amount_input_field")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickAmounts.forEach { amount ->
                val display = if (useBengaliDigits) {
                    "+৳" + BengaliFormatter.toBengaliDigits(amount.toString())
                } else {
                    "+৳$amount"
                }
                androidx.compose.material3.SuggestionChip(
                    onClick = {
                        val currentVal = BengaliFormatter.parseAmountToPaisa(value) / 100L
                        val newVal = currentVal + amount
                        onValueChange(
                            if (useBengaliDigits) BengaliFormatter.toBengaliDigits(newVal.toString())
                            else newVal.toString()
                        )
                    },
                    label = { Text(display, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}
