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
import androidx.compose.ui.window.DialogProperties
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import com.aistudio.moneydiary.mndytr.domain.service.CategoryIconResolver
import com.aistudio.moneydiary.mndytr.ui.theme.LightKoriIncome
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeDialog(
    categories: List<CategoryEntity>,
    useBengaliDigits: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        amountPaisa: Long,
        categoryId: String?,
        sourceDescription: String?,
        description: String,
        notes: String?,
        timestampEpochMs: Long
    ) -> Unit,
    onCreateCategory: (String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var sourceText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var isNewCategoryDialogVisible by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val incomeCategories = categories.filter { it.isIncomeCategory }
    val commonSources = listOf("বেতন থেকে", "গ্রাহকের দেওয়া টাকা", "ব্যাংক থেকে", "bKash", "Nagad", "পরিবার থেকে", "হাতে নগদ")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 12.dp)
                .testTag("add_income_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "নতুন আয় লিখুন",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LightKoriIncome
                        )
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Inline Error Message
                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // 1. Amount Input
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            errorMessage = null
                        },
                        label = { Text("টাকার পরিমাণ (৳)") },
                        placeholder = { Text("যেমন: ১০০০") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LightKoriIncome
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("income_amount_input")
                    )

                    // 2. Short Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            errorMessage = null
                            if (selectedCategoryId == null && it.isNotBlank()) {
                                val match = incomeCategories.firstOrNull { cat -> it.contains(cat.nameBn, ignoreCase = true) }
                                if (match != null) selectedCategoryId = match.id
                            }
                        },
                        label = { Text("বিবরণ (কী বাবদ আয়)") },
                        placeholder = { Text("যেমন: মাসিক বেতন, টিউশনির ফি, ফ্রিল্যান্সিং") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("income_description_input")
                    )

                    // 3. Income Category Selector Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "আয়ের খাত (ক্যাটাগরি)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            incomeCategories.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategoryId == cat.id,
                                    onClick = { selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id },
                                    label = { Text(cat.nameBn) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = CategoryIconResolver.getIconByKey(cat.iconName),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                            AssistChip(
                                onClick = { isNewCategoryDialogVisible = true },
                                label = { Text("+ নতুন খাত") }
                            )
                        }
                    }

                    // 4. Optional Free-Text Source
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "টাকার উৎস, ঐচ্ছিক",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            commonSources.forEach { src ->
                                FilterChip(
                                    selected = sourceText == src,
                                    onClick = { sourceText = if (sourceText == src) "" else src },
                                    label = { Text(src) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = sourceText,
                            onValueChange = { sourceText = it },
                            label = { Text("টাকার উৎস, ঐচ্ছিক") },
                            placeholder = { Text("যেমন: বেতন থেকে, পকেটের টাকা") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("income_source_input")
                        )
                    }

                    // 5. Optional Detailed Diary Note
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("বিস্তারিত ডায়েরি নোট (ঐচ্ছিক)") },
                        placeholder = { Text("এই আয় সংক্রান্ত কোনো বিশেষ বিবরণ থাকলে লিখে রাখুন...") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (isSubmitting) return@Button
                        val amountPaisa = BengaliFormatter.parseAmountToPaisa(amountText)
                        if (amountPaisa <= 0) {
                            errorMessage = "টাকার সঠিক পরিমাণ লিখুন (যেমন: ১০০০)"
                            return@Button
                        }

                        isSubmitting = true
                        val finalDesc = description.ifBlank {
                            selectedCategoryId?.let { id -> categories.firstOrNull { it.id == id }?.nameBn } ?: "সাধারণ আয়"
                        }
                        onSubmit(
                            amountPaisa,
                            selectedCategoryId,
                            sourceText.ifBlank { null },
                            finalDesc,
                            notes.ifBlank { null },
                            System.currentTimeMillis()
                        )
                    },
                    enabled = amountText.isNotBlank() && !isSubmitting,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightKoriIncome,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_income_button")
                ) {
                    Text(
                        text = if (isSubmitting) "সংরক্ষণ করা হচ্ছে..." else "আয় সংরক্ষণ করুন",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (isNewCategoryDialogVisible) {
        AlertDialog(
            onDismissRequest = { isNewCategoryDialogVisible = false },
            title = { Text("নতুন আয়ের খাত যোগ করুন") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("খাতের নাম") },
                    placeholder = { Text("যেমন: পাওনা আদায়, উপহার") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            onCreateCategory(newCategoryName.trim())
                            newCategoryName = ""
                            isNewCategoryDialogVisible = false
                        }
                    }
                ) {
                    Text("যোগ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { isNewCategoryDialogVisible = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
