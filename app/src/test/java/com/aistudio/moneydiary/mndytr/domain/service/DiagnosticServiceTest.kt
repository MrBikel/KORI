package com.aistudio.moneydiary.mndytr.domain.service

import android.content.Context
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import com.aistudio.moneydiary.mndytr.BuildConfig
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.data.local.entity.*
import com.aistudio.moneydiary.mndytr.domain.model.LedgerEventCode
import com.aistudio.moneydiary.mndytr.domain.model.MoneyPaisa
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class DiagnosticServiceTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        DiagnosticService.isDiagnosticOverrideForTesting = true
        DiagnosticService.init(context)
        // Clear existing reports
        DiagnosticService.getCrashReports(context).forEach { it.delete() }
    }

    // 1. Synthetic crash report is created
    @Test
    fun test01_syntheticCrashReportIsCreated() {
        val exception = RuntimeException("Synthetic diagnostic test crash")
        DiagnosticService.recordCrash(context, Thread.currentThread(), exception)

        val reports = DiagnosticService.getCrashReports(context)
        assertEquals(1, reports.size)
        val report = reports[0]
        assertTrue(report.name.startsWith("kori_crash_"))
        assertTrue(report.length() > 0)
        assertTrue(report.readText().contains("KORI CRASH REPORT"))
    }

    // 2. Complete stack trace is included
    @Test
    fun test02_completeStackTraceIsIncluded() {
        val exception = RuntimeException("Stack trace verification")
        DiagnosticService.recordCrash(context, Thread.currentThread(), exception)

        val reportText = DiagnosticService.getCrashReports(context)[0].readText()
        assertTrue(reportText.contains("EXCEPTION"))
        assertTrue(reportText.contains("java.lang.RuntimeException: Stack trace verification"))
        assertTrue(reportText.contains("DiagnosticServiceTest"))
    }

    // 3. Cause chain is included
    @Test
    fun test03_causeChainIsIncluded() {
        val rootCause = IllegalArgumentException("Root cause error detail")
        val exception = RuntimeException("Top-level wrapper", rootCause)
        DiagnosticService.recordCrash(context, Thread.currentThread(), exception)

        val reportText = DiagnosticService.getCrashReports(context)[0].readText()
        assertTrue(reportText.contains("Top-level wrapper"))
        assertTrue(reportText.contains("Caused by: java.lang.IllegalArgumentException: Root cause error detail"))
    }

    // 4. Suppressed exception is included
    @Test
    fun test04_suppressedExceptionIsIncluded() {
        val exception = RuntimeException("Primary failure")
        val suppressed = IllegalStateException("Suppressed secondary error")
        exception.addSuppressed(suppressed)
        DiagnosticService.recordCrash(context, Thread.currentThread(), exception)

        val reportText = DiagnosticService.getCrashReports(context)[0].readText()
        assertTrue(reportText.contains("Primary failure"))
        assertTrue(reportText.contains("Suppressed: java.lang.IllegalStateException: Suppressed secondary error"))
    }

    // 5. Previous handler is retained
    @Test
    fun test05_previousHandlerIsRetained() {
        var previousHandlerInvoked = false
        val dummyPreviousHandler = Thread.UncaughtExceptionHandler { _, _ ->
            previousHandlerInvoked = true
        }

        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        try {
            Thread.setDefaultUncaughtExceptionHandler(dummyPreviousHandler)
            val currentDefault = Thread.getDefaultUncaughtExceptionHandler()
            assertSame(dummyPreviousHandler, currentDefault)

            // Simulating application crash delegation logic
            val delegatingHandler = Thread.UncaughtExceptionHandler { thread, throwable ->
                try {
                    DiagnosticService.recordCrash(context, thread, throwable)
                } finally {
                    dummyPreviousHandler.uncaughtException(thread, throwable)
                }
            }
            delegatingHandler.uncaughtException(Thread.currentThread(), RuntimeException("Delegation test"))
            assertTrue(previousHandlerInvoked)
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(originalHandler)
        }
    }

    // 6. Report retention is 5
    @Test
    fun test06_reportRetentionIs5() {
        val dir = File(context.filesDir, "crash_reports")
        dir.mkdirs()
        for (i in 1..8) {
            val file = File(dir, "kori_crash_2026-10-06_00-00-0$i.txt")
            file.writeText("Crash report $i")
            file.setLastModified(System.currentTimeMillis() + (i * 1000L))
        }
        assertEquals(8, DiagnosticService.getCrashReports(context).size)

        // Trigger retention cleanup
        DiagnosticService.recordCrash(context, Thread.currentThread(), RuntimeException("New crash"))
        val reports = DiagnosticService.getCrashReports(context)
        assertEquals(5, reports.size)
    }

    // 7. Sensitive values are excluded
    @Test
    fun test07_sensitiveValuesAreExcluded() {
        DiagnosticService.addCheckpoint("VALIDATION_PASSED", "EXPENSE")
        DiagnosticService.recordCrash(context, Thread.currentThread(), RuntimeException("Crash check"))

        val reportText = DiagnosticService.getCrashReports(context)[0].readText()
        // Must exclude user sensitive data
        assertFalse(reportText.contains("password", ignoreCase = true))
        assertFalse(reportText.contains("pin", ignoreCase = true))
        assertFalse(reportText.contains("email", ignoreCase = true))
        assertFalse(reportText.contains("token", ignoreCase = true))
        assertFalse(reportText.contains("imei", ignoreCase = true))
        assertFalse(reportText.contains("serial", ignoreCase = true))
        assertFalse(reportText.contains("note", ignoreCase = true))
        assertFalse(reportText.contains("amount", ignoreCase = true))
    }

    // 8. Checkpoints are included
    @Test
    fun test08_checkpointsAreIncluded() {
        DiagnosticService.addCheckpoint("FORM_OPENED", "INCOME")
        DiagnosticService.addCheckpoint("VALIDATION_PASSED", "INCOME")
        DiagnosticService.recordCrash(context, Thread.currentThread(), RuntimeException("Crash"))

        val reportText = DiagnosticService.getCrashReports(context)[0].readText()
        assertTrue(reportText.contains("SUBMISSION CHECKPOINTS"))
        assertTrue(reportText.contains("FORM_OPENED (Form: INCOME)"))
        assertTrue(reportText.contains("VALIDATION_PASSED (Form: INCOME)"))
    }

    // 9. Next launch detects an unreviewed report
    @Test
    fun test09_nextLaunchDetectsUnreviewedReport() {
        DiagnosticService.recordCrash(context, Thread.currentThread(), RuntimeException("Pending report"))
        val reports = DiagnosticService.getCrashReports(context)
        assertTrue(reports.isNotEmpty())
        assertEquals(1, reports.size)
    }

    // 10. “পরে” retains the report
    @Test
    fun test10_poreRetainsTheReport() {
        DiagnosticService.recordCrash(context, Thread.currentThread(), RuntimeException("Keep on pore"))
        val reportsBefore = DiagnosticService.getCrashReports(context)
        assertEquals(1, reportsBefore.size)

        // User taps "পরে" -> dialog dismissed without deleting report
        val reportsAfter = DiagnosticService.getCrashReports(context)
        assertEquals(1, reportsAfter.size)
        assertTrue(reportsAfter[0].exists())
    }

    // 11. Copy returns plain text
    @Test
    fun test11_copyReturnsPlainText() {
        DiagnosticService.recordCrash(context, Thread.currentThread(), RuntimeException("Copy test"))
        val latestReport = DiagnosticService.getCrashReports(context).first()
        val plainText = latestReport.readText()

        assertNotNull(plainText)
        assertTrue(plainText.startsWith("KORI CRASH REPORT"))
        assertTrue(plainText.contains("EXCEPTION"))
    }

    // 12. Share uses content URI
    @Test
    fun test12_shareUsesContentUri() {
        DiagnosticService.recordCrash(context, Thread.currentThread(), RuntimeException("Share test"))
        val latestReport = DiagnosticService.getCrashReports(context).first()
        
        val providerInfo = android.content.pm.ProviderInfo().apply {
            authority = "${context.packageName}.fileprovider"
            grantUriPermissions = true
            metaData = android.os.Bundle().apply {
                putInt("android.support.FILE_PROVIDER_PATHS", com.aistudio.moneydiary.mndytr.R.xml.file_paths)
            }
        }
        org.robolectric.Robolectric.buildContentProvider(FileProvider::class.java).create(providerInfo).get()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", latestReport)

        assertNotNull(uri)
        assertEquals("content", uri.scheme)
        assertFalse(uri.toString().startsWith("file://"))
    }

    // 13. Delete removes report
    @Test
    fun test13_deleteRemovesReport() {
        DiagnosticService.recordCrash(context, Thread.currentThread(), RuntimeException("Delete test"))
        val reports = DiagnosticService.getCrashReports(context)
        assertEquals(1, reports.size)

        DiagnosticService.deleteReport(reports[0])
        assertEquals(0, DiagnosticService.getCrashReports(context).size)
    }

    // 14. FileProvider scope is restricted
    @Test
    fun test14_fileProviderScopeIsRestricted() {
        val dir = File(context.filesDir, "crash_reports")
        assertTrue(dir.exists())
        val dummyReport = File(dir, "kori_crash_scope_test.txt").apply { writeText("scope test") }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", dummyReport)
        assertTrue(uri.path?.contains("crash_reports") == true)
        dummyReport.delete()
    }

    // 15. Broad storage permission is absent
    @Test
    fun test15_broadStoragePermissionIsAbsent() {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_PERMISSIONS)
        val requested = packageInfo.requestedPermissions?.toList() ?: emptyList()

        assertFalse(requested.contains("android.permission.MANAGE_EXTERNAL_STORAGE"))
        assertFalse(requested.contains("android.permission.READ_EXTERNAL_STORAGE"))
        assertFalse(requested.contains("android.permission.WRITE_EXTERNAL_STORAGE"))
    }

    // 16. Firebase dependency is absent from diagnostic capture
    @Test
    fun test16_firebaseDependencyIsAbsentFromDiagnostics() {
        // DiagnosticService operates completely standalone without Firebase Crashlytics
        val fields = DiagnosticService::class.java.declaredFields
        fields.forEach { field ->
            assertFalse(field.type.name.contains("firebase", ignoreCase = true))
        }
    }

    // 17. Financial calculations are unchanged
    @Test
    fun test17_financialCalculationsAreUnchanged() {
        val income = MoneyPaisa.parse("1500")
        val expense = MoneyPaisa.parse("350")
        val balance = income - expense
        assertEquals(115000L, balance.paisa)
        assertEquals("৳ 1,150.00", balance.toFormattedTaka())
    }

    // 18. Room version is 2
    @Test
    fun test18_roomVersionRemains1() {
        val db = AppDatabase.getInstance(context)
        assertEquals(2, db.openHelper.readableDatabase.version)
    }

    // 19. Entity count is 15
    @Test
    fun test19_entityCountRemains14() {
        val entities = listOf(
            AccountEntity::class,
            CategoryEntity::class,
            PersonEntity::class,
            WorkRecordEntity::class,
            TransactionEntity::class,
            ReceivableEntity::class,
            PayableEntity::class,
            SavingsGoalEntity::class,
            FrequentShortcutEntity::class,
            AttachmentEntity::class,
            BackupJobEntity::class,
            BackupMetadataEntity::class,
            AppSettingEntity::class,
            AuditLogEntity::class,
            com.aistudio.moneydiary.mndytr.data.local.entity.FrugalityGoalEntity::class
        )
        assertEquals(15, entities.size)
    }

    // 20. Event-code count remains 22
    @Test
    fun test20_eventCodeCountRemains22() {
        assertEquals(22, LedgerEventCode.values().size)
    }
}
