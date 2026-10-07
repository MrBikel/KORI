package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import com.aistudio.moneydiary.mndytr.ui.theme.LightKoriIncome
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFrugalityGoalDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        goalType: String,
        categoryId: String?,
        targetAmountPaisa: Long,
        periodType: String,
        notes: String?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }
    var selectedPeriodType by remember { mutableStateOf("MONTHLY") } // MONTHLY, WEEKLY, DAILY
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }

    val expenseCategories = categories.filter { !it.isIncomeCategory }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_goal_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "নতুন মিতব্যয়ী লক্ষ্য",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("লক্ষ্যের নাম") },
                    placeholder = { Text("যেমন: খাবার খরচ নিয়ন্ত্রণ, যাতায়াত সীমা") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_title_input")
                )

                // Target Amount
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it },
                    label = { Text("সর্বোচ্চ সীমা বা লক্ষ্য (৳)") },
                    placeholder = { Text("যেমন: ৩০০০") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_amount_input")
                )

                // Period Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "সময়কাল",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedPeriodType == "MONTHLY",
                            onClick = { selectedPeriodType = "MONTHLY" },
                            label = { Text("মাসিক") }
                        )
                        FilterChip(
                            selected = selectedPeriodType == "WEEKLY",
                            onClick = { selectedPeriodType = "WEEKLY" },
                            label = { Text("সাপ্তাহিক") }
                        )
                        FilterChip(
                            selected = selectedPeriodType == "DAILY",
                            onClick = { selectedPeriodType = "DAILY" },
                            label = { Text("দৈনিক") }
                        )
                    }
                }

                // Optional Category Limit
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "নির্দিষ্ট খাতের জন্য (ঐচ্ছিক)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { selectedCategoryId = null },
                            label = { Text("সব খাত মিলিয়ে") }
                        )
                        expenseCategories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategoryId == cat.id,
                                onClick = { selectedCategoryId = cat.id },
                                label = { Text(cat.nameBn) }
                            )
                        }
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        val amountPaisa = BengaliFormatter.parseAmountToPaisa(targetAmountText)
                        val goalType = if (selectedCategoryId != null) "CATEGORY_LIMIT" else "TOTAL_LIMIT"
                        onSubmit(
                            title.trim(),
                            goalType,
                            selectedCategoryId,
                            amountPaisa,
                            selectedPeriodType,
                            notes.ifBlank { null }
                        )
                    },
                    enabled = title.isNotBlank() && targetAmountText.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_goal_button")
                ) {
                    Text(
                        text = "লক্ষ্য যুক্ত করুন",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
