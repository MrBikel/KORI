package com.aistudio.moneydiary.mndytr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aistudio.moneydiary.mndytr.domain.model.WorkStatus
import com.aistudio.moneydiary.mndytr.ui.state.ActiveDialog
import com.aistudio.moneydiary.mndytr.ui.state.DiaryUiState
import com.aistudio.moneydiary.mndytr.ui.state.WorkDisplayItem
import com.aistudio.moneydiary.mndytr.ui.theme.AdvanceAmber
import com.aistudio.moneydiary.mndytr.ui.theme.IncomeGreen
import com.aistudio.moneydiary.mndytr.ui.theme.PrimaryEmerald
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@Composable
fun WorksScreen(
    state: DiaryUiState,
    onOpenDialog: (ActiveDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenDialog(ActiveDialog.AddWorkAdvance) },
                containerColor = AdvanceAmber,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_work_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Work")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Explanatory Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "কাজের চুক্তি ও অগ্রিম ব্যবস্থাপনা",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "যেকোনো ফ্রিল্যান্সিং বা গিগ কাজের জন্য অগ্রিম টাকা গ্রহণ করুন। কাজ শেষ না হওয়া পর্যন্ত এটি 'অগ্রিম দায়' হিসেবে থাকবে এবং চূড়ান্ত নিষ্পত্তির পর একবারে আয় হিসেবে যোগ হবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.activeWorks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "বর্তমানে কোনো চলমান কাজের চুক্তি নেই",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onOpenDialog(ActiveDialog.AddWorkAdvance) },
                            colors = ButtonDefaults.buttonColors(containerColor = AdvanceAmber)
                        ) {
                            Text("+ নতুন চুক্তি ও অগ্রিম লিখুন")
                        }
                    }
                }
            } else {
                items(state.activeWorks, key = { it.work.id }) { item ->
                    WorkItemCard(
                        item = item,
                        useBengaliDigits = state.useBengaliDigits,
                        onSettleClick = { onOpenDialog(ActiveDialog.SettleWork(item.work.id)) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun WorkItemCard(
    item: WorkDisplayItem,
    useBengaliDigits: Boolean,
    onSettleClick: () -> Unit
) {
    val work = item.work
    val proj = item.projection
    val isCompleted = work.workStatus == WorkStatus.COMPLETED.name

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("work_card_${work.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = work.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "ক্লায়েন্ট: ${item.clientName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isCompleted) "সম্পন্ন" else "চলমান",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Pricing Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "মোট চুক্তি", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = BengaliFormatter.formatPaisa(proj.agreedTotalPricePaisa, useBengaliDigits, showDecimals = false),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Column {
                    Text(text = "প্রাপ্ত অগ্রিম (দায়)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = BengaliFormatter.formatPaisa(proj.advanceReceivedPaisa, useBengaliDigits, showDecimals = false),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AdvanceAmber)
                    )
                }

                Column {
                    Text(text = "বাকি পাওনা", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = BengaliFormatter.formatPaisa(proj.expectedRemainingPaymentPaisa, useBengaliDigits, showDecimals = false),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = IncomeGreen)
                    )
                }
            }

            if (!isCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSettleClick,
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("কাজ সম্পন্ন ও চূড়ান্ত নিষ্পত্তি করুন")
                }
            }
        }
    }
}
