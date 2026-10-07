package com.aistudio.moneydiary.mndytr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntry
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntryType
import com.aistudio.moneydiary.mndytr.domain.service.CategoryIconResolver
import com.aistudio.moneydiary.mndytr.ui.state.DiaryUiState
import com.aistudio.moneydiary.mndytr.ui.theme.*
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    state: DiaryUiState,
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onEntryClick: (DiaryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDateBn = BengaliFormatter.formatBengaliDate(System.currentTimeMillis())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Compact App Header & Bengali Date
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.diaryName,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("app_title_header")
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = "ব্যক্তিগত খাতা",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = currentDateBn,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.testTag("today_date_label")
                )
            }
        }

        // 2. Today's Summary Card (Only when data exists) OR Clean Empty First Card
        item {
            if (state.todaySummary.hasEntriesToday) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth().testTag("today_summary_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "আজকের হিসাব সারসংক্ষেপ",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Today's Income
                            Column {
                                Text(
                                    text = "আজকের আয়",
                                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = BengaliFormatter.formatPaisa(
                                        state.todaySummary.todayIncomePaisa,
                                        useBengaliDigits = state.useBengaliDigits,
                                        showDecimals = false
                                    ),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = LightKoriIncome,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.testTag("today_income_value")
                                )
                            }

                            // Today's Expense
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "আজকের ব্যয়",
                                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = BengaliFormatter.formatPaisa(
                                        state.todaySummary.todayExpensePaisa,
                                        useBengaliDigits = state.useBengaliDigits,
                                        showDecimals = false
                                    ),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = LightKoriExpense,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.testTag("today_expense_value")
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        // Today's Net Change
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "নিট পরিবর্তন",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                            )
                            val sign = if (state.todaySummary.todayNetChangePaisa >= 0) "+" else "−"
                            val netColor = if (state.todaySummary.todayNetChangePaisa >= 0) LightKoriIncome else LightKoriExpense
                            Text(
                                text = "$sign${BengaliFormatter.formatPaisa(Math.abs(state.todaySummary.todayNetChangePaisa), useBengaliDigits = state.useBengaliDigits, showDecimals = false)}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = netColor,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.testTag("today_net_value")
                            )
                        }
                    }
                }
            } else {
                // Empty First Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().testTag("empty_first_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(40.dp)
                        )

                        Text(
                            text = "আজ এখনো কোনো হিসাব লেখা হয়নি।",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("empty_today_message")
                        )

                        Text(
                            text = "প্রথম হিসাবটি লিখলেই কড়ি আপনার ডায়েরি সাজানো শুরু করবে।",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }

        // 3. Action Buttons: ব্যয় লিখুন & আয় লিখুন
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAddExpenseClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightKoriExpense,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("add_expense_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ব্যয় লিখুন",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = onAddIncomeClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightKoriIncome,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("add_income_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "আয় লিখুন",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // 4. Evidence-based single highlight insight (Only when sufficient data exists)
        if (state.todayHighlightInsight != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("insight_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "অন্তর্দৃষ্টি",
                            tint = LightKoriInsight,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = state.todayHighlightInsight,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }
            }
        }

        // 5. Today's Real Entries List
        if (state.todayEntries.isNotEmpty()) {
            item {
                Text(
                    text = "আজকের হিসাবসমূহ",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }

            items(state.todayEntries, key = { it.id }) { entry ->
                TodayEntryItem(
                    entry = entry,
                    useBengaliDigits = state.useBengaliDigits,
                    onClick = { onEntryClick(entry) }
                )
            }
        }
    }
}

@Composable
fun TodayEntryItem(
    entry: DiaryEntry,
    useBengaliDigits: Boolean,
    onClick: () -> Unit
) {
    val timeSdf = SimpleDateFormat("h:mm", Locale.getDefault())
    val timeFormatted = timeSdf.format(Date(entry.occurrenceDateEpochMs))
    val timeStr = if (useBengaliDigits) BengaliFormatter.toBengaliDigits(timeFormatted) else timeFormatted

    val isIncome = entry.type == DiaryEntryType.INCOME
    val amountSign = if (isIncome) "+" else "−"
    val amountColor = if (isIncome) LightKoriIncome else LightKoriExpense
    val amountFormatted = BengaliFormatter.formatPaisa(entry.amountPaisa, useBengaliDigits = useBengaliDigits, showDecimals = false)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("today_entry_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Category Icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isIncome) LightKoriIncome.copy(alpha = 0.12f) else LightKoriExpense.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = CategoryIconResolver.getIconByKey(entry.categoryIconKey),
                        contentDescription = entry.categoryNameBn,
                        tint = if (isIncome) LightKoriIncome else LightKoriExpense,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Description & Metadata
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.description,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${entry.categoryNameBn} • $timeStr",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    if (entry.sourceDescription != null) {
                        Text(
                            text = "(${entry.sourceDescription})",
                            style = MaterialTheme.typography.labelMedium.copy(color = LightKoriInsight),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Amount
            Text(
                text = "$amountSign$amountFormatted",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = amountColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            )
        }
    }
}
