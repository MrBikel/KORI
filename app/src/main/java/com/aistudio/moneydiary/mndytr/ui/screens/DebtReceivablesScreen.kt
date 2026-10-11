package com.aistudio.moneydiary.mndytr.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.moneydiary.mndytr.domain.model.DebtRecordItem
import com.aistudio.moneydiary.mndytr.domain.model.EarnedReceivableItem
import com.aistudio.moneydiary.mndytr.ui.state.DiaryUiState
import com.aistudio.moneydiary.mndytr.ui.theme.*
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

enum class DebtReceivableFilter(val labelBn: String) {
    ALL("সব"),
    BORROWED("ঋণ"),
    LENT("ধার-পাওনা"),
    EARNED("কাজের পাওনা"),
    SETTLED("সম্পূর্ণ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtReceivablesScreen(
    state: DiaryUiState,
    onBackClick: () -> Unit,
    onBorrowClick: () -> Unit,
    onLendClick: () -> Unit,
    onAddEarnedClick: () -> Unit,
    onRepayDebtClick: (DebtRecordItem) -> Unit,
    onCollectLoanClick: (DebtRecordItem) -> Unit,
    onCollectReceivableClick: (EarnedReceivableItem) -> Unit,
    onPartyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var selectedFilter by remember { mutableStateOf(DebtReceivableFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val summary = state.debtReceivablesSummary

    // Filter items
    val filteredDebts = state.debtRecords.filter { debt ->
        val matchesQuery = searchQuery.isBlank() ||
                debt.partyName.contains(searchQuery, ignoreCase = true) ||
                (debt.notes?.contains(searchQuery, ignoreCase = true) == true)

        val matchesFilter = when (selectedFilter) {
            DebtReceivableFilter.ALL -> debt.status != "SETTLED"
            DebtReceivableFilter.BORROWED -> debt.type == "BORROWED" && debt.status != "SETTLED"
            DebtReceivableFilter.LENT -> debt.type == "LENT" && debt.status != "SETTLED"
            DebtReceivableFilter.EARNED -> false
            DebtReceivableFilter.SETTLED -> debt.status == "SETTLED"
        }
        matchesQuery && matchesFilter
    }

    val filteredReceivables = state.earnedReceivables.filter { rec ->
        val matchesQuery = searchQuery.isBlank() ||
                rec.partyName.contains(searchQuery, ignoreCase = true) ||
                rec.title.contains(searchQuery, ignoreCase = true) ||
                (rec.notes?.contains(searchQuery, ignoreCase = true) == true)

        val matchesFilter = when (selectedFilter) {
            DebtReceivableFilter.ALL -> rec.status != "SETTLED"
            DebtReceivableFilter.BORROWED -> false
            DebtReceivableFilter.LENT -> false
            DebtReceivableFilter.EARNED -> rec.status != "SETTLED"
            DebtReceivableFilter.SETTLED -> rec.status == "SETTLED"
        }
        matchesQuery && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ঋণ ও পাওনা",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("debt_screen_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Summary Cards
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("debt_summary_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "বর্তমান আর্থিক দায় ও পাওনার হিসাব",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // আমার বাকি ঋণ
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "আমার বাকি ঋণ",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = BengaliFormatter.formatPaisa(
                                        summary.totalBorrowedOutstandingPaisa,
                                        useBengaliDigits = state.useBengaliDigits,
                                        showDecimals = false
                                    ),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LightKoriExpense
                                    ),
                                    modifier = Modifier.testTag("total_borrowed_value")
                                )
                            }

                            // ধার দেওয়া থেকে পাওনা
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ধার দেওয়া থেকে পাওনা",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = BengaliFormatter.formatPaisa(
                                        summary.totalLentOutstandingPaisa,
                                        useBengaliDigits = state.useBengaliDigits,
                                        showDecimals = false
                                    ),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ReceivableBlue
                                    ),
                                    modifier = Modifier.testTag("total_lent_value")
                                )
                            }

                            // কাজ/সেবা থেকে পাওনা
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "কাজের পাওনা",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = BengaliFormatter.formatPaisa(
                                        summary.totalEarnedOutstandingPaisa,
                                        useBengaliDigits = state.useBengaliDigits,
                                        showDecimals = false
                                    ),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LightKoriIncome
                                    ),
                                    modifier = Modifier.testTag("total_earned_receivable_value")
                                )
                            }
                        }
                    }
                }
            }

            // 2. Action Buttons: [ ঋণ নিয়েছি ] [ কাউকে ধার দিয়েছি ] [ কাজের পাওনা যোগ করুন ]
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onBorrowClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("action_borrow_button")
                    ) {
                        Text("ঋণ নিয়েছি", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                    }

                    FilledTonalButton(
                        onClick = onLendClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("action_lend_button")
                    ) {
                        Text("কাউকে ধার", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                    }

                    FilledTonalButton(
                        onClick = onAddEarnedClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.1f).testTag("action_add_earned_button")
                    ) {
                        Text("কাজের পাওনা", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                    }
                }
            }

            // 3. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ব্যক্তি, পক্ষ বা বিবরণ দিয়ে খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "খুঁজুন") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "মুছুন")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("debt_search_field")
                )
            }

            // 4. Filters Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DebtReceivableFilter.values()) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.labelBn) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // 5. Debt Items List
            if (filteredDebts.isNotEmpty()) {
                item {
                    Text(
                        text = "ঋণ ও ধারের হিসাব",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(filteredDebts, key = { it.id }) { debt ->
                    DebtCardItem(
                        item = debt,
                        useBengaliDigits = state.useBengaliDigits,
                        onPaymentClick = {
                            if (debt.type == "BORROWED") onRepayDebtClick(debt)
                            else onCollectLoanClick(debt)
                        },
                        onPartyClick = { onPartyClick(debt.partyId) }
                    )
                }
            }

            // 6. Earned Receivable Items List
            if (filteredReceivables.isNotEmpty()) {
                item {
                    Text(
                        text = "কাজ বা সেবা থেকে পাওনা",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(filteredReceivables, key = { it.id }) { rec ->
                    EarnedReceivableCardItem(
                        item = rec,
                        useBengaliDigits = state.useBengaliDigits,
                        onCollectClick = { onCollectReceivableClick(rec) },
                        onPartyClick = { onPartyClick(rec.partyId) }
                    )
                }
            }

            if (filteredDebts.isEmpty() && filteredReceivables.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো ঋণ বা পাওনার হিসাব পাওয়া যায়নি।",
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DebtCardItem(
    item: DebtRecordItem,
    useBengaliDigits: Boolean,
    onPaymentClick: () -> Unit,
    onPartyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBorrowed = item.type == "BORROWED"
    val badgeText = if (isBorrowed) "আমি ঋণ নিয়েছি" else "আমার কাছ থেকে ঋণ নেওয়া"
    val badgeColor = if (isBorrowed) LightKoriExpense else ReceivableBlue

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier.fillMaxWidth().testTag("debt_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.partyName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.clickable { onPartyClick() }
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(color = badgeColor, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "মূল পরিমাণ",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = BengaliFormatter.formatPaisa(item.principalPaisa, useBengaliDigits = useBengaliDigits, showDecimals = false),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Column {
                    Text(
                        text = "পরিশোধিত",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = BengaliFormatter.formatPaisa(item.totalRepaidPaisa, useBengaliDigits = useBengaliDigits, showDecimals = false),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "বাকি",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = BengaliFormatter.formatPaisa(item.remainingPaisa, useBengaliDigits = useBengaliDigits, showDecimals = false),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.remainingPaisa == 0L) LightKoriIncome else badgeColor
                        )
                    )
                }
            }

            if (item.isOverdue) {
                Text(
                    text = "নির্ধারিত সময় পেরিয়ে গেছে",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                )
            }

            if (item.remainingPaisa > 0L) {
                Button(
                    onClick = onPaymentClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("repay_button_${item.id}")
                ) {
                    Text(if (isBorrowed) "পরিশোধ লিখুন" else "টাকা আদায় লিখুন")
                }
            }
        }
    }
}

@Composable
fun EarnedReceivableCardItem(
    item: EarnedReceivableItem,
    useBengaliDigits: Boolean,
    onCollectClick: () -> Unit,
    onPartyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier.fillMaxWidth().testTag("earned_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = item.partyName,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.clickable { onPartyClick() }
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LightKoriIncome.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "কাজের প্রাপ্য",
                        style = MaterialTheme.typography.labelSmall.copy(color = LightKoriIncome, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "চুক্তিবদ্ধ টাকা",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = BengaliFormatter.formatPaisa(item.agreedAmountPaisa, useBengaliDigits = useBengaliDigits, showDecimals = false),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Column {
                    Text(
                        text = "আদায় হয়েছে",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = BengaliFormatter.formatPaisa(item.totalReceivedPaisa, useBengaliDigits = useBengaliDigits, showDecimals = false),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "বাকি পাওনা",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = BengaliFormatter.formatPaisa(item.remainingPaisa, useBengaliDigits = useBengaliDigits, showDecimals = false),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.remainingPaisa == 0L) LightKoriIncome else LightKoriExpense
                        )
                    )
                }
            }

            if (item.remainingPaisa > 0L) {
                Button(
                    onClick = onCollectClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("collect_receivable_button_${item.id}")
                ) {
                    Text("পাওনা আদায় লিখুন")
                }
            }
        }
    }
}
