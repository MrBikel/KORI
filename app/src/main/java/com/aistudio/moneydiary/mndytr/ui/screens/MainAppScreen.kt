package com.aistudio.moneydiary.mndytr.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntry
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntryType
import com.aistudio.moneydiary.mndytr.domain.service.AppLockType
import com.aistudio.moneydiary.mndytr.domain.service.SecurityService
import com.aistudio.moneydiary.mndytr.ui.dialogs.*
import com.aistudio.moneydiary.mndytr.ui.state.ActiveDialog
import com.aistudio.moneydiary.mndytr.ui.state.AppNavTab
import com.aistudio.moneydiary.mndytr.ui.theme.KoriTypographyTokens
import com.aistudio.moneydiary.mndytr.ui.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: DiaryViewModel,
    modifier: Modifier = Modifier,
    onBiometricAuthenticate: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedEntryForDetail by remember { mutableStateOf<DiaryEntry?>(null) }
    var isQuickEntrySheetOpen by remember { mutableStateOf(false) }

    // Sync security config on launch
    LaunchedEffect(Unit) {
        val config = SecurityService.getSecurityConfig(context)
        viewModel.updateSecurityConfig(config)
        if (config.lockType != AppLockType.NONE) {
            viewModel.setAppLocked(true)
        }
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // App Lock Gate
    if (state.isAppLocked && state.securityConfig.lockType != AppLockType.NONE) {
        AppLockScreen(
            securityConfig = state.securityConfig,
            onUnlockSuccess = { viewModel.setAppLocked(false) },
            onOpenRecovery = { viewModel.openDialog(ActiveDialog.VerifyRecoveryKey) },
            onBiometricTrigger = onBiometricAuthenticate
        )

        // Allow recovery verification dialog on top of lock screen
        if (state.activeDialog is ActiveDialog.VerifyRecoveryKey) {
            VerifyRecoveryKeyDialog(
                onVerify = { enteredKey ->
                    val success = SecurityService.verifyRecoveryKey(context, enteredKey)
                    if (success) {
                        viewModel.updateSecurityConfig(SecurityService.getSecurityConfig(context))
                        viewModel.setAppLocked(false)
                    }
                    success
                },
                onSuccess = {
                    viewModel.dismissDialog()
                },
                onDismiss = { viewModel.dismissDialog() }
            )
        }
        return
    }

    if (!state.isSetupCompleted) {
        FirstLaunchSetupScreen(
            onComplete = { useBn, drafts, lang, curr, theme, reminder, name ->
                viewModel.completeFirstLaunchSetup(useBn, drafts, lang, curr, theme, reminder, name)
            }
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // 1. আজ (Today)
                NavigationBarItem(
                    selected = state.selectedTab == AppNavTab.TODAY,
                    onClick = { viewModel.selectTab(AppNavTab.TODAY) },
                    icon = { Icon(Icons.Default.Today, contentDescription = "আজ") },
                    label = { Text("আজ", style = KoriTypographyTokens.BottomNav) },
                    modifier = Modifier.testTag("nav_today")
                )

                // 2. ডায়েরি (Diary)
                NavigationBarItem(
                    selected = state.selectedTab == AppNavTab.DIARY,
                    onClick = { viewModel.selectTab(AppNavTab.DIARY) },
                    icon = { Icon(Icons.Default.AutoStories, contentDescription = "ডায়েরি") },
                    label = { Text("ডায়েরি", style = KoriTypographyTokens.BottomNav) },
                    modifier = Modifier.testTag("nav_diary")
                )

                // 3. বিশ্লেষণ (Analysis)
                NavigationBarItem(
                    selected = state.selectedTab == AppNavTab.ANALYSIS,
                    onClick = { viewModel.selectTab(AppNavTab.ANALYSIS) },
                    icon = { Icon(Icons.Default.Insights, contentDescription = "বিশ্লেষণ") },
                    label = { Text("বিশ্লেষণ", style = KoriTypographyTokens.BottomNav) },
                    modifier = Modifier.testTag("nav_analysis")
                )

                // 4. আরও (More)
                NavigationBarItem(
                    selected = state.selectedTab == AppNavTab.MORE,
                    onClick = { viewModel.selectTab(AppNavTab.MORE) },
                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "আরও") },
                    label = { Text("আরও", style = KoriTypographyTokens.BottomNav) },
                    modifier = Modifier.testTag("nav_more")
                )
            }
        },
        floatingActionButton = {
            if (state.selectedTab == AppNavTab.TODAY || state.selectedTab == AppNavTab.DIARY) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openDialog(ActiveDialog.AddExpense) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("হিসাব লিখুন", style = KoriTypographyTokens.PrimaryButton) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("quick_add_fab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.selectedTab) {
                AppNavTab.TODAY -> {
                    TodayScreen(
                        state = state,
                        onAddExpenseClick = { viewModel.openDialog(ActiveDialog.AddExpense) },
                        onAddIncomeClick = { viewModel.openDialog(ActiveDialog.AddIncome) },
                        onEntryClick = { entry ->
                            selectedEntryForDetail = entry
                            viewModel.openDialog(ActiveDialog.EntryDetail(entry))
                        }
                    )
                }

                AppNavTab.DIARY -> {
                    DiaryScreen(
                        state = state,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onTypeFilterChange = { viewModel.setTypeFilter(it) },
                        onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                        onNecessityFilterChange = { viewModel.setNecessityFilter(it) },
                        onEntryClick = { entry ->
                            selectedEntryForDetail = entry
                            viewModel.openDialog(ActiveDialog.EntryDetail(entry))
                        },
                        onAddEntryClick = { viewModel.openDialog(ActiveDialog.AddExpense) }
                    )
                }

                AppNavTab.ANALYSIS -> {
                    AnalysisScreen(
                        state = state,
                        onAddGoalClick = { viewModel.openDialog(ActiveDialog.AddFrugalityGoal) }
                    )
                }

                AppNavTab.MORE -> {
                    MoreScreen(
                        state = state,
                        onManageCategoriesClick = { viewModel.openDialog(ActiveDialog.ManageCategories) },
                        onFrugalityGoalsClick = { viewModel.openDialog(ActiveDialog.AddFrugalityGoal) },
                        onReminderSettingsClick = { viewModel.openDialog(ActiveDialog.ReminderSettings) },
                        onToggleNumeralSystem = {
                            viewModel.updateSetting("numeral_system", if (it) "bn" else "en")
                        },
                        onThemeSettingsClick = { viewModel.openDialog(ActiveDialog.ThemeSelection) },
                        onSecuritySettingsClick = { viewModel.openDialog(ActiveDialog.SecuritySettings) },
                        onOpenSourceLicensesClick = { viewModel.openDialog(ActiveDialog.OpenSourceLicenses) },
                        onDiagnosticsClick = { viewModel.openDialog(ActiveDialog.Diagnostics) },
                        onOpenBackupOptions = { viewModel.openDialog(ActiveDialog.BackupOptions) },
                        onOpenRestorePreview = { viewModel.previewLocalRestore(context, it) },
                        onConfirmRestore = { viewModel.confirmRestore(context, it) },
                        onExportBackup = { viewModel.exportLocalBackup(context, it) },
                        onPrepareClipboard = { viewModel.prepareBackupForClipboard(context, it) },
                        onShareBackupFile = { viewModel.shareBackupAsFile(context, it) },
                        onDismissDialog = { viewModel.dismissDialog() }
                    )
                }
            }
        }

        // Active Dialogs Routing
        when (val active = state.activeDialog) {
            is ActiveDialog.AddExpense -> {
                AddExpenseDialog(
                    categories = state.activeCategories,
                    useBengaliDigits = state.useBengaliDigits,
                    onDismiss = { viewModel.dismissDialog() },
                    onSubmit = { amount, catId, src, desc, note, nec, ts ->
                        viewModel.recordDiaryEntry(
                            type = DiaryEntryType.EXPENSE,
                            amountPaisa = amount,
                            categoryId = catId,
                            sourceDescription = src,
                            description = desc,
                            notes = note,
                            necessity = nec,
                            occurrenceDateEpochMs = ts
                        )
                    },
                    onCreateCategory = { name ->
                        viewModel.createCategory(name, isIncome = false)
                    }
                )
            }

            is ActiveDialog.AddIncome -> {
                AddIncomeDialog(
                    categories = state.activeCategories,
                    useBengaliDigits = state.useBengaliDigits,
                    onDismiss = { viewModel.dismissDialog() },
                    onSubmit = { amount, catId, src, desc, note, ts ->
                        viewModel.recordDiaryEntry(
                            type = DiaryEntryType.INCOME,
                            amountPaisa = amount,
                            categoryId = catId,
                            sourceDescription = src,
                            description = desc,
                            notes = note,
                            occurrenceDateEpochMs = ts
                        )
                    },
                    onCreateCategory = { name ->
                        viewModel.createCategory(name, isIncome = true)
                    }
                )
            }

            is ActiveDialog.EntryDetail -> {
                EntryDetailDialog(
                    entry = active.entry,
                    useBengaliDigits = state.useBengaliDigits,
                    onDismiss = { viewModel.dismissDialog() },
                    onDelete = { id -> viewModel.deleteDiaryEntry(id) }
                )
            }

            is ActiveDialog.AddFrugalityGoal -> {
                AddFrugalityGoalDialog(
                    categories = state.activeCategories,
                    onDismiss = { viewModel.dismissDialog() },
                    onSubmit = { title, goalType, catId, amount, period, note ->
                        viewModel.addFrugalityGoal(title, goalType, catId, amount, period, note)
                    }
                )
            }

            is ActiveDialog.ManageCategories -> {
                ManageCategoriesDialog(
                    categories = state.activeCategories,
                    onDismiss = { viewModel.dismissDialog() },
                    onCreateCategory = { name, isIncome, iconName ->
                        viewModel.createCategory(name, iconName = iconName, isIncome = isIncome)
                    },
                    onArchiveCategory = { id, isArchived ->
                        viewModel.archiveCategory(id, isArchived)
                    }
                )
            }

            is ActiveDialog.ReminderSettings -> {
                ReminderSettingsDialog(
                    isDailyEnabled = state.isDailyReminderEnabled,
                    onDismiss = { viewModel.dismissDialog() },
                    onUpdateSetting = { k, v -> viewModel.updateSetting(k, v) }
                )
            }

            is ActiveDialog.ThemeSelection -> {
                ThemeSelectionDialog(
                    currentColorIdentity = state.colorIdentity,
                    currentThemeMode = state.themeMode,
                    onApply = { identity, mode ->
                        viewModel.setColorIdentity(identity)
                        viewModel.updateSetting("theme_mode", mode)
                    },
                    onDismiss = { viewModel.dismissDialog() }
                )
            }

            is ActiveDialog.SecuritySettings -> {
                SecuritySettingsDialog(
                    securityConfig = state.securityConfig,
                    onSetupPin = { viewModel.openDialog(ActiveDialog.SetupPin) },
                    onSetupPassword = { viewModel.openDialog(ActiveDialog.SetupPassword) },
                    onRemoveLock = {
                        SecurityService.removeLock(context)
                        viewModel.updateSecurityConfig(SecurityService.getSecurityConfig(context))
                    },
                    onToggleBiometric = { enabled ->
                        SecurityService.setBiometricEnabled(context, enabled)
                        viewModel.updateSecurityConfig(SecurityService.getSecurityConfig(context))
                    },
                    onSetAutoLockTimeout = { timeout ->
                        SecurityService.setAutoLockTimeout(context, timeout)
                        viewModel.updateSecurityConfig(SecurityService.getSecurityConfig(context))
                    },
                    onToggleRecentAppsHidden = { hidden ->
                        SecurityService.setRecentAppsPreviewHidden(context, hidden)
                        viewModel.updateSecurityConfig(SecurityService.getSecurityConfig(context))
                    },
                    onToggleScreenshotBlocked = { blocked ->
                        SecurityService.setScreenshotBlocked(context, blocked)
                        viewModel.updateSecurityConfig(SecurityService.getSecurityConfig(context))
                    },
                    onSetNotificationPrivacy = { level ->
                        SecurityService.setNotificationPrivacy(context, level)
                        viewModel.updateSecurityConfig(SecurityService.getSecurityConfig(context))
                    },
                    onGenerateRecoveryKey = {
                        val key = SecurityService.generateRecoveryKey(context)
                        viewModel.setLatestGeneratedRecoveryKey(key)
                        viewModel.updateSecurityConfig(SecurityService.getSecurityConfig(context))
                        viewModel.openDialog(ActiveDialog.OpenRecoveryKeyShow)
                    },
                    onDismiss = { viewModel.dismissDialog() }
                )
            }

            is ActiveDialog.SetupPin -> {
                SetupPinDialog(
                    onSavePin = { pin ->
                        SecurityService.setPin(context, pin)
                        viewModel.updateSecurityConfig(SecurityService.getSecurityConfig(context))
                    },
                    onDismiss = { viewModel.dismissDialog() }
                )
            }

            is ActiveDialog.SetupPassword -> {
                SetupPasswordDialog(
                    onSavePassword = { password ->
                        SecurityService.setPassword(context, password)
                        viewModel.updateSecurityConfig(SecurityService.getSecurityConfig(context))
                    },
                    onDismiss = { viewModel.dismissDialog() }
                )
            }

            is ActiveDialog.OpenRecoveryKeyShow -> {
                state.latestGeneratedRecoveryKey?.let { key ->
                    ShowRecoveryKeyDialog(
                        recoveryKey = key,
                        onDismiss = { viewModel.dismissDialog() }
                    )
                } ?: viewModel.dismissDialog()
            }

            is ActiveDialog.OpenSourceLicenses -> {
                OpenSourceLicensesDialog(
                    onDismiss = { viewModel.dismissDialog() }
                )
            }

            is ActiveDialog.CrashRecovery -> {
                CrashRecoveryDialog(
                    onDismiss = { viewModel.dismissDialog() },
                    onDelete = { file -> viewModel.deleteCrashReport(file) }
                )
            }

            is ActiveDialog.Diagnostics -> {
                DiagnosticsDialog(
                    onDismiss = { viewModel.dismissDialog() }
                )
            }

            else -> { /* None */ }
        }
    }
}
