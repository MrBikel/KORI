package com.aistudio.moneydiary.mndytr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import com.aistudio.moneydiary.mndytr.domain.model.DayGroupedDiary
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntry
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntryType
import com.aistudio.moneydiary.mndytr.domain.model.NecessityClassification
import com.aistudio.moneydiary.mndytr.domain.service.CategoryIconResolver
import com.aistudio.moneydiary.mndytr.ui.state.DiaryUiState
import com.aistudio.moneydiary.mndytr.ui.theme.*
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    state: DiaryUiState,
    onSearchChange: (String) -> Unit,
    onTypeFilterChange: (DiaryEntryType?) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onNecessityFilterChange: (NecessityClassification?) -> Unit,
    onEntryClick: (DiaryEntry) -> Unit,
    onAddEntryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Search Box
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "আয়-ব্যয়ের ডায়েরি",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    IconButton(
                        onClick = { isSearchExpanded = !isSearchExpanded },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "অনুসন্ধান",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (isSearchExpanded) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("বিবরণ, উৎস বা খাত খুঁজুন...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("diary_search_input")
                    )
                }

                // Filter Chips (All, Income, Expense, Necessity)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All
                    FilterChip(
                        selected = state.selectedTypeFilter == null && state.selectedNecessityFilter == null,
                        onClick = {
                            onTypeFilterChange(null)
                            onNecessityFilterChange(null)
                            onCategoryFilterChange(null)
                        },
                        label = { Text("সব হিসাব") }
                    )

                    // Expense Filter
                    FilterChip(
                        selected = state.selectedTypeFilter == DiaryEntryType.EXPENSE,
                        onClick = {
                            onTypeFilterChange(
                                if (state.selectedTypeFilter == DiaryEntryType.EXPENSE) null else DiaryEntryType.EXPENSE
                            )
                        },
                        label = { Text("শুধু ব্যয়") }
                    )

                    // Income Filter
                    FilterChip(
                        selected = state.selectedTypeFilter == DiaryEntryType.INCOME,
                        onClick = {
                            onTypeFilterChange(
                                if (state.selectedTypeFilter == DiaryEntryType.INCOME) null else DiaryEntryType.INCOME
                            )
                        },
                        label = { Text("শুধু আয়") }
                    )

                    // Discretionary
                    FilterChip(
                        selected = state.selectedNecessityFilter == NecessityClassification.DISCRETIONARY,
                        onClick = {
                            onNecessityFilterChange(
                                if (state.selectedNecessityFilter == NecessityClassification.DISCRETIONARY) null else NecessityClassification.DISCRETIONARY
                            )
                        },
                        label = { Text("ইচ্ছাধীন ব্যয়") }
                    )
                }
            }
        }

        // Empty state
        if (state.filteredDayGroupedEntries.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (state.searchQuery.isNotBlank() || state.selectedTypeFilter != null) {
                                "কোনো ফলাফল পাওয়া যায়নি।"
                            } else {
                                "ডায়েরিতে এখনো কোনো হিসাব লেখা হয়নি।"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "নতুন হিসাব যোগ করতে 'হিসাব লিখুন' বাটন ব্যবহার করুন।",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }

        // Day Grouped Diary Entries
        items(state.filteredDayGroupedEntries, key = { it.dateEpochMs }) { dayGroup ->
            DayGroupSection(
                dayGroup = dayGroup,
                useBengaliDigits = state.useBengaliDigits,
                onEntryClick = onEntryClick
            )
        }
    }
}

@Composable
fun DayGroupSection(
    dayGroup: DayGroupedDiary,
    useBengaliDigits: Boolean,
    onEntryClick: (DiaryEntry) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("day_group_${dayGroup.dateEpochMs}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Day Header: Date and daily net change
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayGroup.dateLabelBn,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                val netSign = if (dayGroup.netChangePaisa >= 0) "+" else "−"
                val netColor = if (dayGroup.netChangePaisa >= 0) LightKoriIncome else LightKoriExpense
                Text(
                    text = "$netSign${BengaliFormatter.formatPaisa(Math.abs(dayGroup.netChangePaisa), useBengaliDigits = useBengaliDigits, showDecimals = false)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = netColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            // Entries in this day
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dayGroup.entries.forEach { entry ->
                    DiaryRowItem(
                        entry = entry,
                        useBengaliDigits = useBengaliDigits,
                        onClick = { onEntryClick(entry) }
                    )
                }
            }

            // Day Totals Footer
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "মোট আয়: ${BengaliFormatter.formatPaisa(dayGroup.totalIncomePaisa, useBengaliDigits = useBengaliDigits, showDecimals = false)}",
                    style = MaterialTheme.typography.labelMedium.copy(color = LightKoriIncome, fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "মোট ব্যয়: ${BengaliFormatter.formatPaisa(dayGroup.totalExpensePaisa, useBengaliDigits = useBengaliDigits, showDecimals = false)}",
                    style = MaterialTheme.typography.labelMedium.copy(color = LightKoriExpense, fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

@Composable
fun DiaryRowItem(
    entry: DiaryEntry,
    useBengaliDigits: Boolean,
    onClick: () -> Unit
) {
    val isIncome = entry.type == DiaryEntryType.INCOME
    val sign = if (isIncome) "+" else "−"
    val color = if (isIncome) LightKoriIncome else LightKoriExpense
    val formatted = BengaliFormatter.formatPaisa(entry.amountPaisa, useBengaliDigits = useBengaliDigits, showDecimals = false)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = CategoryIconResolver.getIconByKey(entry.categoryIconKey),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = entry.categoryNameBn,
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    if (entry.sourceDescription != null) {
                        Text(
                            text = "• ${entry.sourceDescription}",
                            style = MaterialTheme.typography.labelMedium.copy(color = LightKoriInsight)
                        )
                    }
                }
            }
        }

        Text(
            text = "$sign$formatted",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}
