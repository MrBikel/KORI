package com.aistudio.moneydiary.mndytr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.moneydiary.mndytr.domain.model.FrugalityGoalProgress
import com.aistudio.moneydiary.mndytr.domain.model.NecessityClassification
import com.aistudio.moneydiary.mndytr.domain.model.PeriodComparison
import com.aistudio.moneydiary.mndytr.domain.service.CategoryIconResolver
import com.aistudio.moneydiary.mndytr.ui.state.DiaryUiState
import com.aistudio.moneydiary.mndytr.ui.theme.*
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter
import java.util.Locale

@Composable
fun AnalysisScreen(
    state: DiaryUiState,
    onAddGoalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriodTab by remember { mutableStateOf(0) } // 0: Month, 1: Week, 2: Day, 3: Year

    val insights = state.analysisInsights

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Title Header
        item {
            Text(
                text = "আয়-ব্যয় বিশ্লেষণ",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Empty state check
        if (state.allDiaryEntries.size < 2 || insights == null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "তুলনা দেখানোর জন্য আরও কিছু হিসাব প্রয়োজন।",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "নিয়মিত কয়েকদিন আয় ও ব্যয়ের হিসাব লিখলে এখানে স্বয়ংক্রিয় তুলনামূলক পর্যালোচনা দেখা যাবে।",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        } else {
            // Period Comparison Selector Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedPeriodTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedPeriodTab == 0,
                        onClick = { selectedPeriodTab = 0 },
                        text = { Text("মাসিক") }
                    )
                    Tab(
                        selected = selectedPeriodTab == 1,
                        onClick = { selectedPeriodTab = 1 },
                        text = { Text("সাপ্তাহিক") }
                    )
                    Tab(
                        selected = selectedPeriodTab == 2,
                        onClick = { selectedPeriodTab = 2 },
                        text = { Text("দৈনিক") }
                    )
                    Tab(
                        selected = selectedPeriodTab == 3,
                        onClick = { selectedPeriodTab = 3 },
                        text = { Text("বার্ষিক") }
                    )
                }
            }

            // Active Period Comparison Card
            val activeComparison = when (selectedPeriodTab) {
                0 -> insights.monthVsLastMonth
                1 -> insights.weekVsLastWeek
                2 -> insights.todayVsYesterday
                else -> insights.yearVsLastYear
            }

            if (activeComparison != null) {
                item {
                    PeriodComparisonCard(
                        comparison = activeComparison,
                        useBengaliDigits = state.useBengaliDigits
                    )
                }
            }

            // Frugality Goals Progress Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "মিতব্যয়ী লক্ষ্য",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                    TextButton(onClick = onAddGoalClick) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("নতুন লক্ষ্য")
                    }
                }
            }

            if (insights.frugalityGoalsProgress.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "কোনো সক্রিয় ব্যয়সীমা বা লক্ষ্য নির্ধারিত নেই।",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "মাসিক বা সাপ্তাহিক নির্দিষ্ট খাতে ব্যয় সীমিত রাখতে লক্ষ্য যোগ করতে পারেন।",
                                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            } else {
                items(insights.frugalityGoalsProgress, key = { it.goal.id }) { goalProg ->
                    GoalProgressCard(progress = goalProg, useBengaliDigits = state.useBengaliDigits)
                }
            }

            // Category Spending Shares Section
            if (insights.categoryShares.isNotEmpty()) {
                item {
                    Text(
                        text = "প্রধান ব্যয়ের খাতসমূহ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            insights.categoryShares.take(5).forEach { share ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = CategoryIconResolver.getIconByKey(share.iconKey),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = share.categoryNameBn,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                            )
                                            val pctStr = String.format(Locale.US, "%.1f%%", share.percentageOfTotal)
                                            Text(
                                                text = if (state.useBengaliDigits) BengaliFormatter.toBengaliDigits(pctStr) else pctStr,
                                                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                        }
                                    }

                                    Text(
                                        text = BengaliFormatter.formatPaisa(share.amountPaisa, useBengaliDigits = state.useBengaliDigits, showDecimals = false),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Repeated Small Expenses Section
            if (insights.repeatedSmallExpenses.isNotEmpty()) {
                item {
                    Text(
                        text = "ছোট খাতের পুনরাবৃত্তি খরচ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "চা, রিকশা বা নাস্তার মতো ছোট খরচগুলো বারবার যুক্ত হয়ে বড় অংক তৈরি করে:",
                                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            insights.repeatedSmallExpenses.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.categoryNameBn,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                    )
                                    Text(
                                        text = BengaliFormatter.formatPaisa(item.amountPaisa, useBengaliDigits = state.useBengaliDigits, showDecimals = false),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = LightKoriExpense,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodComparisonCard(
    comparison: PeriodComparison,
    useBengaliDigits: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("period_comparison_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "${comparison.currentPeriodLabel} বনাম ${comparison.previousPeriodLabel}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${comparison.currentPeriodLabel}র ব্যয়",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = BengaliFormatter.formatPaisa(comparison.currentExpensePaisa, useBengaliDigits = useBengaliDigits, showDecimals = false),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${comparison.previousPeriodLabel}র ব্যয়",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = BengaliFormatter.formatPaisa(comparison.previousExpensePaisa, useBengaliDigits = useBengaliDigits, showDecimals = false),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            // Respectful Bengali Narrative Text
            Text(
                text = comparison.narrative,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun GoalProgressCard(
    progress: FrugalityGoalProgress,
    useBengaliDigits: Boolean
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().testTag("goal_progress_${progress.goal.id}")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = progress.goal.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "লক্ষ্য: ${BengaliFormatter.formatPaisa(progress.goal.targetAmountPaisa, useBengaliDigits = useBengaliDigits, showDecimals = false)}",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { (progress.percentageUsed / 100.0).toFloat().coerceIn(0f, 1f) },
                color = if (progress.isExceeded) LightKoriWarning else LightKoriIncome,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ব্যয়: ${BengaliFormatter.formatPaisa(progress.currentSpendingPaisa, useBengaliDigits = useBengaliDigits, showDecimals = false)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "অবশিষ্ট: ${BengaliFormatter.formatPaisa(progress.remainingAllowancePaisa, useBengaliDigits = useBengaliDigits, showDecimals = false)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                )
            }

            // Non-judgmental guidance message
            Text(
                text = progress.guidanceMessageBn,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (progress.isExceeded) LightKoriWarning else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
