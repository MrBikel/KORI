package com.aistudio.moneydiary.mndytr.ui.dialogs

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

@Composable
fun ReminderSettingsDialog(
    isDailyEnabled: Boolean,
    onDismiss: () -> Unit,
    onUpdateSetting: (key: String, value: String) -> Unit
) {
    var dailyEnabled by remember { mutableStateOf(isDailyEnabled) }
    var missingDayEnabled by remember { mutableStateOf(true) }
    var weeklyReviewEnabled by remember { mutableStateOf(true) }
    var monthlyReviewEnabled by remember { mutableStateOf(true) }
    var limitWarningEnabled by remember { mutableStateOf(true) }
    var goalProgressEnabled by remember { mutableStateOf(true) }
    var positiveNoticeEnabled by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("reminder_settings_dialog")
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
                        text = "অনুস্মারক ও নোটিফিকেশন",
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

                // 1. Daily Reminder
                ReminderToggleRow(
                    title = "দৈনিক ডায়েরি অনুস্মারক",
                    subtitle = "প্রতিদিন রাত ৯টায় হিসাব লেখার রিমাইন্ডার",
                    checked = dailyEnabled,
                    onCheckedChange = {
                        dailyEnabled = it
                        onUpdateSetting("reminder_daily_enabled", it.toString())
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // 2. Missing Day Reminder
                ReminderToggleRow(
                    title = "ছুট যাওয়া দিনের নোটিফিকেশন",
                    subtitle = "কোনোদিন হিসাব না লিখলে পরদিন সকালে মনে করিয়ে দেওয়া হবে",
                    checked = missingDayEnabled,
                    onCheckedChange = {
                        missingDayEnabled = it
                        onUpdateSetting("reminder_missing_day_enabled", it.toString())
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // 3. Weekly Review
                ReminderToggleRow(
                    title = "সাপ্তাহিক পর্যালোচনা",
                    subtitle = "সপ্তাহান্তে খরচের তুলনামূলক সংক্ষিপ্ত হিসাব",
                    checked = weeklyReviewEnabled,
                    onCheckedChange = {
                        weeklyReviewEnabled = it
                        onUpdateSetting("reminder_weekly_review_enabled", it.toString())
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // 4. Monthly Review
                ReminderToggleRow(
                    title = "মাসিক পর্যালোচনা",
                    subtitle = "মাসের শেষে সামগ্রিক আয়-ব্যয়ের সারাংশ",
                    checked = monthlyReviewEnabled,
                    onCheckedChange = {
                        monthlyReviewEnabled = it
                        onUpdateSetting("reminder_monthly_review_enabled", it.toString())
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // 5. Category Limit Warning
                ReminderToggleRow(
                    title = "ব্যয়সীমা সতর্কতা",
                    subtitle = "কোনো খাতের বাজেট ৮০% পৌঁছালে নোটিফিকেশন",
                    checked = limitWarningEnabled,
                    onCheckedChange = {
                        limitWarningEnabled = it
                        onUpdateSetting("reminder_limit_warning_enabled", it.toString())
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // 6. Goal Progress Notice
                ReminderToggleRow(
                    title = "মিতব্যয়ী লক্ষ্যের অগ্রগতি",
                    subtitle = "লক্ষ্য অর্জনের নিয়মিত নোটিশ",
                    checked = goalProgressEnabled,
                    onCheckedChange = {
                        goalProgressEnabled = it
                        onUpdateSetting("reminder_goal_progress_enabled", it.toString())
                    }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // 7. Positive Progress Notice
                ReminderToggleRow(
                    title = "ইতিবাচক অগ্রগতির নোটিশ",
                    subtitle = "ব্যয় কমলে বা লক্ষ্য বজায় থাকলে উৎসাহব্যঞ্জক বার্তা",
                    checked = positiveNoticeEnabled,
                    onCheckedChange = {
                        positiveNoticeEnabled = it
                        onUpdateSetting("reminder_positive_notice_enabled", it.toString())
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("সম্পন্ন")
                }
            }
        }
    }
}

@Composable
fun ReminderToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
