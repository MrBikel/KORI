package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
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
import com.aistudio.moneydiary.mndytr.domain.service.CategoryIconResolver
import com.aistudio.moneydiary.mndytr.ui.theme.LightKoriIncome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesDialog(
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onCreateCategory: (nameBn: String, isIncome: Boolean, iconName: String?) -> Unit,
    onArchiveCategory: (id: String, isArchived: Boolean) -> Unit
) {
    var isNewCategoryDialogVisible by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var isIncomeType by remember { mutableStateOf(false) }
    var selectedOverrideIconKey by remember { mutableStateOf<String?>(null) }
    var showIconPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(16.dp)
                .testTag("manage_categories_dialog")
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
                        text = "আয় ও ব্যয়ের খাতসমূহ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))

                // Action to Add New
                OutlinedButton(
                    onClick = {
                        newCatName = ""
                        selectedOverrideIconKey = null
                        isNewCategoryDialogVisible = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("open_add_category_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("নতুন খাত যোগ করুন")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List of Categories
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it.id }) { cat ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = CategoryIconResolver.getIconByKey(cat.iconName),
                                        contentDescription = null,
                                        tint = if (cat.isIncomeCategory) LightKoriIncome else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = cat.nameBn,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                        )
                                        Text(
                                            text = if (cat.isIncomeCategory) "আয়ের খাত" else "ব্যয়ের খাত",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }

                                if (!cat.isSystemDefault) {
                                    IconButton(
                                        onClick = { onArchiveCategory(cat.id, true) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Archive,
                                            contentDescription = "আর্কাইভ",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
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

    if (isNewCategoryDialogVisible) {
        val suggestedIconKey = remember(newCatName) { CategoryIconResolver.suggestIconKey(newCatName) }
        val effectiveIconKey = selectedOverrideIconKey ?: suggestedIconKey

        AlertDialog(
            onDismissRequest = { isNewCategoryDialogVisible = false },
            title = { Text("নতুন খাত তৈরি") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = {
                            newCatName = it
                            // Reset override if user types new name
                        },
                        label = { Text("খাতের নাম") },
                        placeholder = { Text("যেমন: ফ্রিল্যান্সিং, বই কেনা") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_category_name_field")
                    )

                    // Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isIncomeType,
                            onClick = { isIncomeType = false },
                            label = { Text("ব্যয়") }
                        )
                        FilterChip(
                            selected = isIncomeType,
                            onClick = { isIncomeType = true },
                            label = { Text("আয়") }
                        )
                    }

                    // Proposed Icon Preview & Override Action
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("প্রস্তাবিত আইকন:")
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { showIconPicker = !showIconPicker }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = CategoryIconResolver.getIconByKey(effectiveIconKey),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        TextButton(onClick = { showIconPicker = !showIconPicker }) {
                            Text(if (showIconPicker) "লুকান" else "আইকন বদলান")
                        }
                    }

                    // Horizontal Icon Picker Row
                    if (showIconPicker) {
                        Text(
                            text = "অন্য একটি আইকন বেছে নিন:",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategoryIconResolver.ALL_AVAILABLE_ICONS.take(16).forEach { iconInfo ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (effectiveIconKey == iconInfo.key) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable {
                                            selectedOverrideIconKey = iconInfo.key
                                            showIconPicker = false
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = iconInfo.icon,
                                            contentDescription = iconInfo.labelBn,
                                            tint = if (effectiveIconKey == iconInfo.key) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            onCreateCategory(newCatName.trim(), isIncomeType, selectedOverrideIconKey)
                            newCatName = ""
                            selectedOverrideIconKey = null
                            isNewCategoryDialogVisible = false
                        }
                    },
                    modifier = Modifier.testTag("save_category_button")
                ) {
                    Text("সংরক্ষণ করুন")
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
