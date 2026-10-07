package com.aistudio.moneydiary.mndytr.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.moneydiary.mndytr.BuildConfig
import com.aistudio.moneydiary.mndytr.domain.service.AppLockType
import com.aistudio.moneydiary.mndytr.domain.service.DataExportService
import com.aistudio.moneydiary.mndytr.ui.state.DiaryUiState
import com.aistudio.moneydiary.mndytr.ui.theme.*

@Composable
fun MoreScreen(
    state: DiaryUiState,
    onManageCategoriesClick: () -> Unit,
    onFrugalityGoalsClick: () -> Unit,
    onReminderSettingsClick: () -> Unit,
    onToggleNumeralSystem: (Boolean) -> Unit,
    onThemeSettingsClick: () -> Unit,
    onSecuritySettingsClick: () -> Unit,
    onOpenSourceLicensesClick: () -> Unit,
    onDiagnosticsClick: () -> Unit,
    onOpenBackupOptions: () -> Unit,
    onOpenRestorePreview: (String) -> Unit,
    onConfirmRestore: (com.aistudio.moneydiary.mndytr.domain.model.BackupSummary) -> Unit,
    onExportBackup: (Boolean) -> String,
    onPrepareClipboard: (String) -> Unit,
    onShareBackupFile: (String) -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("CSV") }
    var exportIncludeNotes by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showCloudNotice by remember { mutableStateOf(false) }

    // Backup Options State
    var backupIncludeNotes by remember { mutableStateOf(false) }
    var backupCodeResult by remember { mutableStateOf("") }
    var showPrivacyWarningBeforeCopy by remember { mutableStateOf(false) }

    // Restore State
    var restoreTextInput by remember { mutableStateOf("") }

    val createDocumentLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val json = onExportBackup(backupIncludeNotes)
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
                // showSnackbar through viewmodel would be better but we don't have direct access here easily
                // We'll rely on the ViewModel's snackbar state
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ... (Header and Sections 1 remains same)
        item {
            Text(
                text = "আরও সেটিংস ও অপশন",
                style = KoriTypographyTokens.ScreenTitle.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    MoreMenuItem(
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        iconTint = MaterialTheme.colorScheme.error,
                        title = "ব্যয়ের খাত",
                        subtitle = "খরচের খাতসমূহ পরিচালনা ও নতুন খাত যোগ",
                        onClick = onManageCategoriesClick,
                        tag = "expense_categories_item"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    MoreMenuItem(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = LightKoriIncome,
                        title = "আয়ের খাত",
                        subtitle = "উপার্জনের খাতসমূহ সাজানো ও যোগ করা",
                        onClick = onManageCategoriesClick,
                        tag = "income_categories_item"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    MoreMenuItem(
                        icon = Icons.Default.Category,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "খাত ও দ্রুত লেখা",
                        subtitle = "আইকন নির্ধারণ ও পুনরাবৃত্তি খাতের সাজসজ্জা",
                        onClick = onManageCategoriesClick,
                        tag = "categories_quick_write_item"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    MoreMenuItem(
                        icon = Icons.Default.TrackChanges,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = "মিতব্যয়িতার লক্ষ্য",
                        subtitle = "বাজেট এবং খরচ কমানোর পরিকল্পনা ও সীমা",
                        onClick = onFrugalityGoalsClick,
                        tag = "frugality_goals_item"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    MoreMenuItem(
                        icon = Icons.Default.Notifications,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        title = "নোটিফিকেশন",
                        subtitle = if (state.isDailyReminderEnabled) "দৈনিক ও নিয়মিত অনুস্মারক চালু রয়েছে" else "অনুস্মারক বন্ধ রয়েছে",
                        onClick = onReminderSettingsClick,
                        tag = "reminder_settings_item"
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleNumeralSystem(!state.useBengaliDigits) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Numbers,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(text = "ভাষা ও অঙ্ক", style = KoriTypographyTokens.EntryTitle)
                                Text(
                                    text = if (state.useBengaliDigits) "বাংলা সংখ্যা (১, ২, ৩...)" else "ইংরেজি সংখ্যা (1, 2, 3...)",
                                    style = KoriTypographyTokens.Metadata,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = state.useBengaliDigits,
                            onCheckedChange = onToggleNumeralSystem,
                            modifier = Modifier.testTag("numeral_toggle_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    MoreMenuItem(
                        icon = Icons.Default.Palette,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "থিম ও রঙ",
                        subtitle = "${state.colorIdentity.titleBn} • ${when (state.themeMode) { "LIGHT" -> "লাইট"; "DARK" -> "ডার্ক"; else -> "স্বয়ংক্রিয়" }}",
                        onClick = onThemeSettingsClick,
                        tag = "theme_and_color_item"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    MoreMenuItem(
                        icon = Icons.Default.Lock,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "নিরাপত্তা ও গোপনীয়তা",
                        subtitle = if (state.securityConfig.lockType != AppLockType.NONE) "অ্যাপ লক সক্রিয় (${if (state.securityConfig.lockType == AppLockType.PIN) "PIN" else "পাসওয়ার্ড"})" else "অ্যাপ লক বন্ধ",
                        onClick = onSecuritySettingsClick,
                        tag = "security_settings_item"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    MoreMenuItem(
                        icon = Icons.Default.FileDownload,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        title = "ডেটা Export",
                        subtitle = "CSV বা JSON ফরম্যাটে স্থানীয় ডিভাইসে ব্যাকআপ নিন",
                        onClick = { showExportDialog = true },
                        tag = "export_data_item"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    MoreMenuItem(
                        icon = Icons.Default.SettingsBackupRestore,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        title = "স্থানীয় ব্যাকআপ ও পুনরুদ্ধার",
                        subtitle = "ডিভাইসের মেমরিতে ব্যাকআপ সংরক্ষণ ও রিস্টোর করুন",
                        onClick = onOpenBackupOptions,
                        tag = "local_backup_restore_item"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    MoreMenuItem(
                        icon = Icons.Default.CloudQueue,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        title = "Cloud ও Backup",
                        subtitle = "এখনো সংযুক্ত নয়",
                        onClick = { showCloudNotice = true },
                        tag = "cloud_sync_item"
                    )
                }
            }
        }

        // Section 3: Privacy & About
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    MoreMenuItem(
                        icon = Icons.Default.Security,
                        iconTint = LightKoriIncome,
                        title = "গোপনীয়তা",
                        subtitle = "আপনার সমস্ত ডেটা সম্পূর্ণ অফলাইন ও এই ফোনেই সংরক্ষিত",
                        onClick = { showPrivacyDialog = true },
                        tag = "privacy_policy_item"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    MoreMenuItem(
                        icon = Icons.Default.Info,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "কড়ি সম্পর্কে",
                        subtitle = "সংস্করণ ২.০ • শান্ত ও ব্যক্তিগত আয়-ব্যয় ডায়েরি",
                        onClick = { showAboutDialog = true },
                        tag = "about_kori_item"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    MoreMenuItem(
                        icon = Icons.AutoMirrored.Filled.Article,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "ওপেন-সোর্স লাইসেন্স",
                        subtitle = "Hind Siliguri এবং Noto Serif Bengali ফন্ট লাইসেন্স",
                        onClick = onOpenSourceLicensesClick,
                        tag = "open_source_licenses_item"
                    )
                }
            }
        }

        if (BuildConfig.IS_DIAGNOSTIC) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MoreMenuItem(
                        icon = Icons.Default.BugReport,
                        iconTint = MaterialTheme.colorScheme.error,
                        title = "ডায়াগনস্টিকস (শুধু ডায়াগনস্টিক বিল্ড)",
                        subtitle = "সিস্টেম তথ্য, ডেটাবেস সাইজ ও ক্র্যাশ রিপোর্ট",
                        onClick = onDiagnosticsClick,
                        tag = "diagnostics_menu_item"
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "কড়ি — ব্যক্তিগত আর্থিক ডায়েরি",
                    style = KoriTypographyTokens.Wordmark.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "সংস্করণ ২.০ (বিল্ড ২০২৩-২০২৬)",
                    style = KoriTypographyTokens.Metadata,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("ডেটা Export করুন", style = KoriTypographyTokens.ScreenTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("কড়ির সমস্ত এন্ট্রি আপনার ডিভাইসে ডাউনলোড বা শেয়ার করুন:", style = KoriTypographyTokens.SecondaryBody)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = exportFormat == "CSV",
                            onClick = { exportFormat = "CSV" },
                            label = { Text("CSV ফরম্যাট (Excel)") }
                        )
                        FilterChip(
                            selected = exportFormat == "JSON",
                            onClick = { exportFormat = "JSON" },
                            label = { Text("JSON ফরম্যাট") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { exportIncludeNotes = !exportIncludeNotes }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = exportIncludeNotes, onCheckedChange = { exportIncludeNotes = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ব্যক্তিগত নোট যুক্ত করুন", style = KoriTypographyTokens.SecondaryBody)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportDialog = false
                        val intent = DataExportService.exportAndShare(
                            context = context,
                            entries = state.allDiaryEntries,
                            format = exportFormat,
                            includePrivateNotes = exportIncludeNotes
                        )
                        if (intent != null) context.startActivity(intent)
                    },
                    modifier = Modifier.testTag("confirm_export_btn")
                ) {
                    Text("শেয়ার / ডাউনলোড", style = KoriTypographyTokens.PrimaryButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("বাতিল", style = KoriTypographyTokens.PrimaryButton)
                }
            }
        )
    }

    if (showCloudNotice) {
        AlertDialog(
            onDismissRequest = { showCloudNotice = false },
            title = { Text("ক্লাউড ব্যাকআপ") },
            text = { Text("ক্লাউড ব্যাকআপ এবং সিঙ্ক সুবিধা এখনো সংযুক্ত নয়। আপনার সমস্ত তথ্য নিরাপদে এই ফোনেই সংরক্ষিত রয়েছে।") },
            confirmButton = { Button(onClick = { showCloudNotice = false }) { Text("ঠিক আছে") } }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("গোপনীয়তার নিশ্চয়তা") },
            text = { Text("কড়ি কোনো ব্যক্তিগত তথ্য (পূর্ণ নাম, ইমেইল, পাসওয়ার্ড, ফোন নম্বর, জন্মতারিখ, ঠিকানা বা প্রোফাইল ছবি) সংগ্রহ বা ইন্টারনেটে প্রেরণ করে না। আপনার সমস্ত আর্থিক হিসাব শুধুমাত্র আপনার ডিভাইসে Room ডেটাবেসে থাকে।") },
            confirmButton = { Button(onClick = { showPrivacyDialog = false }) { Text("বন্ধ করুন") } }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("কড়ি সম্পর্কে") },
            text = { Text("কড়ি হলো বাংলা ভাষায় তৈরি একটি মার্জিত ব্যক্তিগত আর্থিক ডায়েরি।\nপ্রতিটি কড়ির নির্ভুল হিসাব রাখার মাধ্যমে ব্যয় সচেতনতা ও মিতব্যয়িতা গড়ে তোলাই এর মূল উদ্দেশ্য।") },
            confirmButton = { Button(onClick = { showAboutDialog = false }) { Text("ঠিক আছে") } }
        )
    }

    // Hardened Backup Options Dialog
    if (state.activeDialog is com.aistudio.moneydiary.mndytr.ui.state.ActiveDialog.BackupOptions) {
        AlertDialog(
            onDismissRequest = { onOpenBackupOptions() /* Using it to toggle or close */ },
            title = { Text("স্থানীয় ব্যাকআপ ও পুনরুদ্ধার") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { backupIncludeNotes = !backupIncludeNotes }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = backupIncludeNotes, onCheckedChange = { backupIncludeNotes = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ব্যাকআপে ব্যক্তিগত নোট যুক্ত করুন", style = KoriTypographyTokens.SecondaryBody)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    Text("১. ব্যাকআপ কোড (অন্য ডিভাইসে নিতে কপি করুন)", style = KoriTypographyTokens.EntryTitle.copy(fontSize = 14.sp))
                    Button(
                        onClick = { showPrivacyWarningBeforeCopy = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("কপি ব্যাকআপ কোড")
                    }

                    Text("২. ব্যাকআপ ফাইল (অন্য অ্যাপে শেয়ার বা সেভ করুন)", style = KoriTypographyTokens.EntryTitle.copy(fontSize = 14.sp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onShareBackupFile(onExportBackup(backupIncludeNotes)) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("শেয়ার করুন")
                        }
                        Button(
                            onClick = { 
                                val fileName = "Kori_Backup_${System.currentTimeMillis()}.kori"
                                createDocumentLauncher.launch(fileName)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("সেভ করুন")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    Text("৩. পুনরুদ্ধার (পুনরুদ্ধার করতে কোডটি নিচে দিন)", style = KoriTypographyTokens.EntryTitle.copy(fontSize = 14.sp))
                    OutlinedTextField(
                        value = restoreTextInput,
                        onValueChange = { restoreTextInput = it },
                        label = { Text("ব্যাকআপ কোড পেস্ট করুন") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        textStyle = KoriTypographyTokens.Metadata.copy(fontSize = 11.sp)
                    )
                    Button(
                        onClick = { 
                            if (restoreTextInput.isNotBlank()) onOpenRestorePreview(restoreTextInput)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("কোড যাচাই করুন")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onOpenBackupOptions() }) { Text("বন্ধ করুন") }
            }
        )
    }

    if (showPrivacyWarningBeforeCopy) {
        AlertDialog(
            onDismissRequest = { showPrivacyWarningBeforeCopy = false },
            title = { Text("গোপনীয়তা সতর্কতা") },
            text = { Text("এই ব্যাকআপে আপনার ব্যক্তিগত আর্থিক তথ্য থাকতে পারে। বিশ্বস্ত স্থানে সংরক্ষণ করুন।") },
            confirmButton = {
                Button(onClick = {
                    showPrivacyWarningBeforeCopy = false
                    onPrepareClipboard(onExportBackup(backupIncludeNotes))
                }) {
                    Text("বুঝেছি, কপি করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrivacyWarningBeforeCopy = false }) { Text("বাতিল") }
            }
        )
    }

    // Restore Preview Dialog
    val activeDialog = state.activeDialog
    if (activeDialog is com.aistudio.moneydiary.mndytr.ui.state.ActiveDialog.RestorePreview) {
        val summary = activeDialog.summary
        AlertDialog(
            onDismissRequest = { onDismissDialog() },
            title = { Text("ব্যাকআপ প্রিভিউ", style = KoriTypographyTokens.ScreenTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ব্যাকআপের তথ্য:", style = KoriTypographyTokens.EntryTitle)
                    Text("তারিখ: ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(summary.createdTimestamp))}", style = KoriTypographyTokens.Metadata)
                    Text("অ্যাপ সংস্করণ: ${summary.appVersionName}", style = KoriTypographyTokens.Metadata)
                    Text("স্কিমা সংস্করণ: ${summary.roomSchemaVersion}", style = KoriTypographyTokens.Metadata)
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    
                    Text("রেকর্ড সংখ্যা:", style = KoriTypographyTokens.EntryTitle)
                    Text("আয়: ${summary.incomeCount}", style = KoriTypographyTokens.Metadata)
                    Text("ব্যয়: ${summary.expenseCount}", style = KoriTypographyTokens.Metadata)
                    Text("ক্যাটাগরি: ${summary.categoryCount}", style = KoriTypographyTokens.Metadata)
                    Text("লক্ষ্য: ${summary.goalCount}", style = KoriTypographyTokens.Metadata)
                    Text("ব্যক্তিগত নোট: ${if (summary.includesNotes) "আছে" else "নেই"}", style = KoriTypographyTokens.Metadata)

                    if (summary.dateRangeStart != null && summary.dateRangeEnd != null) {
                        val fmt = java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault())
                        Text("সময়সীমা: ${fmt.format(java.util.Date(summary.dateRangeStart))} - ${fmt.format(java.util.Date(summary.dateRangeEnd))}", style = KoriTypographyTokens.Metadata)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("সতর্কতা!", color = MaterialTheme.colorScheme.onErrorContainer, style = KoriTypographyTokens.EntryTitle)
                            Text(
                                "এটি আপনার বর্তমান ${summary.currentLocalRecordCount}টি এন্ট্রি মুছে দিয়ে এই ব্যাকআপের তথ্য দিয়ে প্রতিস্থাপন করবে।",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = KoriTypographyTokens.Metadata
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onConfirmRestore(summary) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("পুনরুদ্ধার করুন")
                    }
                    OutlinedButton(
                        onClick = { 
                            val fileName = "Kori_Safety_Backup_${System.currentTimeMillis()}.kori"
                            createDocumentLauncher.launch(fileName)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("বর্তমান তথ্যের নিরাপত্তা কপি নিন")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissDialog() }) {
                    Text("বাতিল")
                }
            }
        )
    }
}


@Composable
fun MoreMenuItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = KoriTypographyTokens.EntryTitle
                )
                Text(
                    text = subtitle,
                    style = KoriTypographyTokens.Metadata,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}
