package com.aistudio.moneydiary.mndytr.ui

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntryType
import com.aistudio.moneydiary.mndytr.domain.model.RestoreResult
import com.aistudio.moneydiary.mndytr.domain.service.LocalBackupRestoreService
import com.aistudio.moneydiary.mndytr.ui.viewmodel.DiaryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BackupHardeningTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DiaryViewModel(db, ioDispatcher = testDispatcher)

    @Test
    fun test_BackupCreationAndFullRoundTrip() = runTest {
        val viewModel = createViewModel()
        viewModel.completeFirstLaunchSetup(useBengaliDigits = true)
        
        // Seed data
        viewModel.recordDiaryEntry(DiaryEntryType.INCOME, 50000, null, "Salary", "Monthly Salary", "Private note")
        viewModel.addFrugalityGoal("Save More", "EXPENSE_LIMIT", null, 20000)
        advanceUntilIdle()

        // 1. Backup with notes
        val backupWithNotes = viewModel.exportLocalBackup(context, includeNotes = true)
        assertTrue(backupWithNotes.contains("Monthly Salary"))
        assertTrue(backupWithNotes.contains("Private note"))
        assertTrue(backupWithNotes.contains("Save More"))

        // 2. Backup WITHOUT notes
        val backupNoNotes = viewModel.exportLocalBackup(context, includeNotes = false)
        assertTrue(backupNoNotes.contains("Monthly Salary"))
        assertFalse(backupNoNotes.contains("Private note"))

        // 3. Restore round-trip
        val targetDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val result = LocalBackupRestoreService.executeSafeRestore(context, targetDb, backupWithNotes)
        assertTrue(result is RestoreResult.Success)
        
        val restoredEntries = targetDb.ledgerEventDao().getAllNonDeleted()
        assertEquals(1, restoredEntries.size)
        assertEquals("Private note", restoredEntries.first().notes)
        assertEquals(1, targetDb.frugalityGoalDao().getAllActive().size)
        targetDb.close()
    }

    @Test
    fun test_RejectCorruptedChecksum() {
        val backup = LocalBackupRestoreService.exportBackup(context, db, true)
        val envelope = JSONObject(backup)
        envelope.put("checksum", "wrong_checksum")
        
        val summary = LocalBackupRestoreService.parseAndPreviewBackup(context, db, envelope.toString())
        assertNull(summary)
    }

    @Test
    fun test_RejectUnsupportedVersion() {
        val backup = LocalBackupRestoreService.exportBackup(context, db, true)
        val envelope = JSONObject(backup)
        envelope.put("format_version", 999)
        
        val summary = LocalBackupRestoreService.parseAndPreviewBackup(context, db, envelope.toString())
        assertNull(summary)
    }

    @Test
    fun test_RejectDuplicateIDs() {
        val backup = LocalBackupRestoreService.exportBackup(context, db, true)
        val envelope = JSONObject(backup)
        val payload = JSONObject(envelope.getString("payload"))
        val txs = payload.getJSONArray("transactions")
        
        if (txs.length() > 0) {
            val duplicate = JSONObject(txs.getJSONObject(0).toString())
            txs.put(duplicate)
            envelope.put("payload", payload.toString())
            // Recalculate checksum to pass initial check but fail ID check
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val sum = md.digest(payload.toString().toByteArray()).joinToString("") { "%02x".format(it) }
            envelope.put("checksum", sum)

            val summary = LocalBackupRestoreService.parseAndPreviewBackup(context, db, envelope.toString())
            assertNull("Should reject duplicate IDs in payload", summary)
        }
    }

    @Test
    fun test_RejectInvalidMonetaryValue() {
        val backup = LocalBackupRestoreService.exportBackup(context, db, true)
        val envelope = JSONObject(backup)
        val payload = JSONObject(envelope.getString("payload"))
        val txs = payload.getJSONArray("transactions")
        
        if (txs.length() > 0) {
            txs.getJSONObject(0).put("amountPaisa", -100)
            envelope.put("payload", payload.toString())
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val sum = md.digest(payload.toString().toByteArray()).joinToString("") { "%02x".format(it) }
            envelope.put("checksum", sum)

            val summary = LocalBackupRestoreService.parseAndPreviewBackup(context, db, envelope.toString())
            assertNull("Should reject negative amounts", summary)
        }
    }

    @Test
    fun test_RestoreRollbackOnFailure() = runTest {
        val viewModel = createViewModel()
        viewModel.completeFirstLaunchSetup(useBengaliDigits = true)
        viewModel.recordDiaryEntry(DiaryEntryType.INCOME, 1000, null, "KeepMe", "Original")
        advanceUntilIdle()
        
        val backup = viewModel.exportLocalBackup(context, true)
        val envelope = JSONObject(backup)
        val payload = JSONObject(envelope.getString("payload"))
        
        // Corrupt payload by removing a required field 'description' to fail during parsing
        payload.getJSONArray("transactions").getJSONObject(0).remove("description")
        envelope.put("payload", payload.toString())
        
        // Re-sign so it passes the initial checksum but fails during staging parsing
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val sum = md.digest(payload.toString().toByteArray()).joinToString("") { "%02x".format(it) }
        envelope.put("checksum", sum)

        val result = LocalBackupRestoreService.executeSafeRestore(context, db, envelope.toString())
        assertTrue("Restore should fail due to missing description", result is RestoreResult.Failure)
        
        // Verify original data still exists
        val entries = db.ledgerEventDao().getAllNonDeleted()
        assertEquals(1, entries.size)
        assertEquals("Original", entries.first().description)
    }

    @Test
    fun test_RejectTruncatedText() {
        val backup = LocalBackupRestoreService.exportBackup(context, db, true)
        val truncated = backup.substring(0, backup.length / 2)
        val summary = LocalBackupRestoreService.parseAndPreviewBackup(context, db, truncated)
        assertNull("Should reject truncated JSON", summary)
    }

    @Test
    fun test_RejectMissingCategoryReference() = runTest {
        val viewModel = createViewModel()
        viewModel.completeFirstLaunchSetup(useBengaliDigits = true)
        
        // Create category and transaction
        db.categoryDao().insert(com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity(
            id = "CAT_1", 
            nameBn = "Cat", 
            nameEn = "Cat", 
            iconName = "icon", 
            colorHex = "#000", 
            isIncomeCategory = false
        ))
        db.ledgerEventDao().insert(com.aistudio.moneydiary.mndytr.data.local.entity.TransactionEntity(
            id = "TX_1", 
            categoryId = "CAT_1", 
            amountPaisa = 100, 
            description = "Test", 
            eventKind = "EXPENSE",
            eventCode = "TX_02_EXPENSE"
        ))
        
        val backup = LocalBackupRestoreService.exportBackup(context, db, true)
        val envelope = JSONObject(backup)
        val payload = JSONObject(envelope.getString("payload"))
        
        // Remove the category from payload
        payload.put("categories", org.json.JSONArray())
        envelope.put("payload", payload.toString())
        
        // Recalculate checksum
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val sum = md.digest(payload.toString().toByteArray()).joinToString("") { "%02x".format(it) }
        envelope.put("checksum", sum)

        val targetDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        val result = LocalBackupRestoreService.executeSafeRestore(context, targetDb, envelope.toString())
        assertTrue("Should fail due to missing category reference (Foreign Key)", result is RestoreResult.Failure)
        targetDb.close()
    }

    @Test
    fun test_PreferencesRestored() = runTest {
        db.appSettingDao().set("theme_mode", "DARK")
        db.appSettingDao().set("numeral_system", "en")
        db.appSettingDao().set("reminder_daily_enabled", "true")
        
        val backup = LocalBackupRestoreService.exportBackup(context, db, true)
        
        val targetDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        LocalBackupRestoreService.executeSafeRestore(context, targetDb, backup)
        
        val settings = targetDb.appSettingDao().getAll().associate { it.key to it.value }
        assertEquals("DARK", settings["theme_mode"])
        assertEquals("en", settings["numeral_system"])
        assertEquals("true", settings["reminder_daily_enabled"])
        targetDb.close()
    }

    @Test
    fun test_ArchivedCategoriesRestored() = runTest {
        db.categoryDao().insert(com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity(
            id = "CAT_ARC", 
            nameBn = "Archived", 
            nameEn = "Archived", 
            iconName = "icon", 
            colorHex = "#000", 
            isIncomeCategory = false, 
            isArchived = true
        ))
        
        val backup = LocalBackupRestoreService.exportBackup(context, db, true)
        
        val targetDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        LocalBackupRestoreService.executeSafeRestore(context, targetDb, backup)
        
        val cat = targetDb.categoryDao().getById("CAT_ARC")
        assertNotNull(cat)
        assertTrue(cat!!.isArchived)
        targetDb.close()
    }

    @Test
    fun test_SafetyBackupFileCreated() = runTest {
        // Ensure the database file exists on disk
        val dbPath = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        dbPath.parentFile?.mkdirs()
        dbPath.writeText("fake sqlite content")
        
        val backup = LocalBackupRestoreService.exportBackup(context, db, true)
        LocalBackupRestoreService.executeSafeRestore(context, db, backup)
        
        val safetyFile = File(context.filesDir, "kori_safety_backup_before_restore.db")
        assertTrue("Safety backup file should be created in filesDir", safetyFile.exists())
    }

    @Test
    fun test_SecretsExcludedFromBackup() = runTest {
        db.appSettingDao().set("pin_hash", "secret_hash")
        db.appSettingDao().set("theme_mode", "DARK")
        
        val backup = LocalBackupRestoreService.exportBackup(context, db, true)
        assertFalse("Backup should not contain pin_hash", backup.contains("pin_hash"))
        assertFalse("Backup should not contain secret_hash", backup.contains("secret_hash"))
        assertTrue("Backup should contain theme_mode", backup.contains("theme_mode"))
    }
}
