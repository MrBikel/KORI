package com.aistudio.moneydiary.mndytr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SmokingRooms
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.moneydiary.mndytr.data.local.entity.FrequentShortcutEntity
import com.aistudio.moneydiary.mndytr.ui.components.TransactionRow
import com.aistudio.moneydiary.mndytr.ui.state.ActiveDialog
import com.aistudio.moneydiary.mndytr.ui.state.AppNavTab
import com.aistudio.moneydiary.mndytr.ui.state.DiaryUiState
import com.aistudio.moneydiary.mndytr.ui.theme.AdvanceAmber
import com.aistudio.moneydiary.mndytr.ui.theme.AdvanceAmberContainer
import com.aistudio.moneydiary.mndytr.ui.theme.ExpenseRed
import com.aistudio.moneydiary.mndytr.ui.theme.ExpenseRedContainer
import com.aistudio.moneydiary.mndytr.ui.theme.IncomeGreen
import com.aistudio.moneydiary.mndytr.ui.theme.IncomeGreenContainer
import com.aistudio.moneydiary.mndytr.ui.theme.PrimaryEmerald
import com.aistudio.moneydiary.mndytr.ui.theme.ReceivableBlue
import com.aistudio.moneydiary.mndytr.ui.theme.ReceivableBlueContainer
import com.aistudio.moneydiary.mndytr.ui.theme.SecondaryGold
import com.aistudio.moneydiary.mndytr.ui.theme.TransferTeal
import com.aistudio.moneydiary.mndytr.ui.theme.TransferTealContainer
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@Composable
fun DashboardScreen(
    state: DiaryUiState,
    onOpenDialog: (ActiveDialog) -> Unit,
    onSelectTab: (AppNavTab) -> Unit,
    onExecuteShortcut: (FrequentShortcutEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // 1. Hero Balance Card
            HeroBalanceCard(
                state = state,
                onOpeningBalanceClick = { onOpenDialog(ActiveDialog.OpeningBalance()) }
            )
        }

        item {
            // 2. Fast Action Buttons
            QuickActionButtons(onOpenDialog = onOpenDialog)
        }

        item {
            // 3. Frequent Micro-Expense Shortcuts ("এক ট্যাপে দ্রুত খরচ")
            FrequentShortcutsSection(
                shortcuts = state.frequentShortcuts,
                useBengaliDigits = state.useBengaliDigits,
                onExecuteShortcut = onExecuteShortcut
            )
        }

        item {
            // 4. Financial Status Carousel / Grid
            FinancialMetricsSection(state = state)
        }

        item {
            // 5. Recent Transactions Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সাম্প্রতিক লেনদেন",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (state.recentTransactions.isNotEmpty()) {
                    Text(
                        text = "সব দেখুন →",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .clickable { onSelectTab(AppNavTab.TRANSACTIONS) }
                            .padding(4.dp)
                    )
                }
            }
        }

        if (state.recentTransactions.isEmpty()) {
            item {
                EmptyTransactionsCard(
                    onAddExpenseClick = { onOpenDialog(ActiveDialog.AddExpense) },
                    onSetOpeningBalanceClick = { onOpenDialog(ActiveDialog.OpeningBalance()) }
                )
            }
        } else {
            items(state.recentTransactions, key = { it.id }) { item ->
                TransactionRow(
                    item = item,
                    useBengaliDigits = state.useBengaliDigits,
                    onClick = { onOpenDialog(ActiveDialog.TransactionDetail(item)) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HeroBalanceCard(
    state: DiaryUiState,
    onOpeningBalanceClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_balance_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryEmerald),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "মোট লিকুইড ব্যালেন্স (হাতে নগদ)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable(onClick = onOpeningBalanceClick)
                ) {
                    Text(
                        text = "+ প্রারম্ভিক স্থিতি",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = BengaliFormatter.formatPaisa(
                    state.dashboardSummary.totalLiquidCashPaisa,
                    useBengaliDigits = state.useBengaliDigits,
                    showDecimals = false
                ),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Accounts mini-chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.accountsWithBalance.forEach { accWithBal ->
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = accWithBal.account.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = BengaliFormatter.formatPaisa(
                                    accWithBal.currentBalancePaisa,
                                    state.useBengaliDigits,
                                    showDecimals = false
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SecondaryGold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButtons(
    onOpenDialog: (ActiveDialog) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                label = "খরচ",
                icon = Icons.Default.ArrowDownward,
                color = ExpenseRed,
                containerColor = ExpenseRedContainer,
                modifier = Modifier.weight(1f),
                testTag = "quick_add_expense_btn",
                onClick = { onOpenDialog(ActiveDialog.AddExpense) }
            )

            ActionButton(
                label = "আয়",
                icon = Icons.Default.ArrowUpward,
                color = IncomeGreen,
                containerColor = IncomeGreenContainer,
                modifier = Modifier.weight(1f),
                testTag = "quick_add_income_btn",
                onClick = { onOpenDialog(ActiveDialog.AddIncome) }
            )

            ActionButton(
                label = "কাজের অগ্রিম",
                icon = Icons.Default.Work,
                color = AdvanceAmber,
                containerColor = AdvanceAmberContainer,
                modifier = Modifier.weight(1.2f),
                testTag = "quick_work_advance_btn",
                onClick = { onOpenDialog(ActiveDialog.AddWorkAdvance) }
            )

            ActionButton(
                label = "স্থানান্তর",
                icon = Icons.Default.SwapHoriz,
                color = TransferTeal,
                containerColor = TransferTealContainer,
                modifier = Modifier.weight(1f),
                testTag = "quick_transfer_btn",
                onClick = { onOpenDialog(ActiveDialog.Transfer) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                label = "সঞ্চয়ে স্থানান্তর",
                icon = Icons.Default.Savings,
                color = PrimaryEmerald,
                containerColor = PrimaryEmerald.copy(alpha = 0.12f),
                modifier = Modifier.weight(1.2f),
                testTag = "quick_savings_btn",
                onClick = { onOpenDialog(ActiveDialog.SavingsAllocation) }
            )

            ActionButton(
                label = "স্মার্ট দ্রুত এন্ট্রি",
                icon = Icons.Default.AutoAwesome,
                color = SecondaryGold,
                containerColor = SecondaryGold.copy(alpha = 0.15f),
                modifier = Modifier.weight(1.2f),
                testTag = "quick_smart_entry_btn",
                onClick = { onOpenDialog(ActiveDialog.SmartEntry) }
            )

            ActionButton(
                label = "প্রারম্ভিক স্থিতি",
                icon = Icons.Default.AccountBalanceWallet,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.weight(1.2f),
                testTag = "quick_opening_balance_btn",
                onClick = { onOpenDialog(ActiveDialog.OpeningBalance()) }
            )
        }
    }
}

@Composable
fun ActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
fun FrequentShortcutsSection(
    shortcuts: List<FrequentShortcutEntity>,
    useBengaliDigits: Boolean,
    onExecuteShortcut: (FrequentShortcutEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "এক ট্যাপে দ্রুত খরচ",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "১-ক্লিক এন্ট্রি",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            shortcuts.forEach { shortcut ->
                val icon = when {
                    shortcut.iconName.contains("cafe") -> Icons.Default.LocalCafe
                    shortcut.iconName.contains("smoking") -> Icons.Default.SmokingRooms
                    shortcut.iconName.contains("bike") -> Icons.AutoMirrored.Filled.DirectionsBike
                    shortcut.iconName.contains("restaurant") -> Icons.Default.Restaurant
                    else -> Icons.Default.PhoneAndroid
                }

                Card(
                    modifier = Modifier
                        .clickable { onExecuteShortcut(shortcut) }
                        .testTag("shortcut_${shortcut.labelBn}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = shortcut.labelBn,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = shortcut.labelBn,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = BengaliFormatter.formatPaisa(
                                    shortcut.defaultAmountPaisa,
                                    useBengaliDigits,
                                    showDecimals = false
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialMetricsSection(state: DiaryUiState) {
    val sum = state.dashboardSummary
    val useDigits = state.useBengaliDigits

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "আজকের খরচ",
                amountPaisa = sum.todayExpensePaisa,
                useBengaliDigits = useDigits,
                color = ExpenseRed,
                containerColor = ExpenseRedContainer,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "চলতি মাসের খরচ",
                amountPaisa = sum.monthExpensePaisa,
                useBengaliDigits = useDigits,
                color = ExpenseRed,
                containerColor = ExpenseRedContainer,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "চলতি মাসের আয়",
                amountPaisa = sum.monthEarnedIncomePaisa,
                useBengaliDigits = useDigits,
                color = IncomeGreen,
                containerColor = IncomeGreenContainer,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "কাজের অগ্রিম দায়",
                amountPaisa = sum.unearnedAdvancePaisa,
                useBengaliDigits = useDigits,
                color = AdvanceAmber,
                containerColor = AdvanceAmberContainer,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "বকেয়া পাওনা",
                amountPaisa = sum.activeReceivablePaisa,
                useBengaliDigits = useDigits,
                color = ReceivableBlue,
                containerColor = ReceivableBlueContainer,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "বকেয়া দেনা",
                amountPaisa = sum.activePayablePaisa,
                useBengaliDigits = useDigits,
                color = ExpenseRed,
                containerColor = ExpenseRedContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    amountPaisa: Long,
    useBengaliDigits: Boolean,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = BengaliFormatter.formatPaisa(amountPaisa, useBengaliDigits, showDecimals = false),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun EmptyTransactionsCard(
    onAddExpenseClick: () -> Unit,
    onSetOpeningBalanceClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "কোনো লেনদেন রেকর্ড করা নেই",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "প্রারম্ভিক নগদ টাকা বসিয়ে নিন অথবা আপনার আজকের খরচ লিখে শুরু করুন।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSetOpeningBalanceClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                ) {
                    Text("প্রারম্ভিক ব্যালেন্স")
                }
                OutlinedButton(onClick = onAddExpenseClick) {
                    Text("+ খরচ লিখুন")
                }
            }
        }
    }
}
