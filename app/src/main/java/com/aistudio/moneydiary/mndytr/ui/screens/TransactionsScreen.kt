package com.aistudio.moneydiary.mndytr.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aistudio.moneydiary.mndytr.domain.model.ConfirmationStatus
import com.aistudio.moneydiary.mndytr.domain.model.LedgerEventCode
import com.aistudio.moneydiary.mndytr.ui.components.TransactionRow
import com.aistudio.moneydiary.mndytr.ui.state.ActiveDialog
import com.aistudio.moneydiary.mndytr.ui.state.DiaryUiState
import com.aistudio.moneydiary.mndytr.ui.theme.PrimaryEmerald
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

enum class TransactionFilter(val labelBn: String) {
    ALL("সব"),
    EXPENSE("খরচ"),
    INCOME("আয়"),
    ADVANCE("কাজের অগ্রিম"),
    TRANSFER("স্থানান্তর"),
    SAVINGS("সঞ্চয়"),
    REVERSED("বাতিল/রিভার্সড")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    state: DiaryUiState,
    onOpenDialog: (ActiveDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(TransactionFilter.ALL) }
    var selectedAccountId by remember { mutableStateOf<String?>(null) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    val filteredTransactions = remember(
        state.allTransactions,
        searchQuery,
        selectedFilter,
        selectedAccountId,
        selectedCategoryId
    ) {
        state.allTransactions.filter { item ->
            // Filter by type
            val matchesFilter = when (selectedFilter) {
                TransactionFilter.ALL -> true
                TransactionFilter.EXPENSE -> item.eventCode in listOf(
                    LedgerEventCode.TX_02_EXPENSE.name,
                    LedgerEventCode.TX_04_ADVANCE_RETURNED.name,
                    LedgerEventCode.TX_09_DEBT_REPAID.name,
                    LedgerEventCode.TX_10_LENT_MONEY.name,
                    LedgerEventCode.TX_14F_TRANSFER_FEE.name
                )
                TransactionFilter.INCOME -> item.eventCode in listOf(
                    LedgerEventCode.TX_00_OPENING_BALANCE.name,
                    LedgerEventCode.TX_01_INCOME.name,
                    LedgerEventCode.TX_06_RECEIVABLE_COLLECTED.name,
                    LedgerEventCode.TX_11_LOAN_COLLECTED.name
                )
                TransactionFilter.ADVANCE -> item.eventCode in listOf(
                    LedgerEventCode.TX_03_ADVANCE_RECEIVED.name,
                    LedgerEventCode.TX_04_ADVANCE_RETURNED.name
                )
                TransactionFilter.TRANSFER -> item.eventCode in listOf(
                    LedgerEventCode.TX_14_TRANSFER.name,
                    LedgerEventCode.TX_14F_TRANSFER_FEE.name
                )
                TransactionFilter.SAVINGS -> item.eventCode == LedgerEventCode.TX_15_SAVINGS_ALLOCATION.name
                TransactionFilter.REVERSED -> item.confirmationStatus == ConfirmationStatus.REVERSED.name ||
                        item.eventCode == LedgerEventCode.TX_17_REVERSAL.name
            }

            // Filter by account
            val matchesAccount = if (selectedAccountId == null) true else {
                item.sourceAccountId == selectedAccountId || item.destinationAccountId == selectedAccountId
            }

            // Filter by category
            val matchesCategory = if (selectedCategoryId == null) true else {
                item.categoryId == selectedCategoryId
            }

            // Search query across description, person, work title, notes, and category
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.description.contains(searchQuery, ignoreCase = true) ||
                        (item.categoryNameBn?.contains(searchQuery, ignoreCase = true) == true) ||
                        (item.personName?.contains(searchQuery, ignoreCase = true) == true) ||
                        (item.workTitle?.contains(searchQuery, ignoreCase = true) == true) ||
                        (item.sourceAccountName?.contains(searchQuery, ignoreCase = true) == true) ||
                        (item.destinationAccountName?.contains(searchQuery, ignoreCase = true) == true) ||
                        (item.notes?.contains(searchQuery, ignoreCase = true) == true)
            }

            matchesFilter && matchesAccount && matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("transactions_screen")
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("লেনদেন খুঁজুন (বিবরণ, ব্যক্তি, কাজ, মন্তব্য)...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("transaction_search_input")
        )

        // Event-Type Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TransactionFilter.values().forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.labelBn) },
                    modifier = Modifier.testTag("filter_${filter.name.lowercase()}")
                )
            }
        }

        // Account & Category Sub-Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Account chips
            state.accountsWithBalance.forEach { accWithBal ->
                val isSelected = selectedAccountId == accWithBal.account.id
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedAccountId = if (isSelected) null else accWithBal.account.id },
                    label = { Text(accWithBal.account.name) },
                    leadingIcon = {
                        if (isSelected) Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.padding(2.dp))
                    }
                )
            }

            // Reset button if any filter is active
            if (searchQuery.isNotEmpty() || selectedFilter != TransactionFilter.ALL || selectedAccountId != null || selectedCategoryId != null) {
                TextButton(
                    onClick = {
                        searchQuery = ""
                        selectedFilter = TransactionFilter.ALL
                        selectedAccountId = null
                        selectedCategoryId = null
                    },
                    modifier = Modifier.testTag("reset_filters_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    Text("রিসেট", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        // Count summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val countStr = if (state.useBengaliDigits) {
                BengaliFormatter.toBengaliDigits(filteredTransactions.size.toString())
            } else {
                filteredTransactions.size.toString()
            }
            Text(
                text = "মোট লেনদেন: $countStr টি",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Transaction List / Empty State
        if (filteredTransactions.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .testTag("empty_transactions_state"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "কোনো লেনদেন পাওয়া যায়নি",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ফিল্টার পরিবর্তন করুন অথবা নতুন লেনদেন যোগ করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("transactions_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { item ->
                    TransactionRow(
                        item = item,
                        useBengaliDigits = state.useBengaliDigits,
                        onClick = { onOpenDialog(ActiveDialog.TransactionDetail(item)) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
