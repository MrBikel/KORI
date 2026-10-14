package com.aistudio.moneydiary.mndytr.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Migration3To4RealDatabaseTest {

    private lateinit var context: Context
    private val dbName = "test_migration_v3_to_v4.db"
    private var appDatabase: AppDatabase? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(dbName)
    }

    @After
    fun tearDown() {
        appDatabase?.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun testRealDatabaseMigrationFromV3ToV4PreservesAllData() {
        // Step 1: Create a real SQLite database file at Version 3 using schema 3.json DDLs
        val helperConfig = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(3) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    createVersion3Tables(db)
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val openHelper = FrameworkSQLiteOpenHelperFactory().create(helperConfig)
        val v3Db = openHelper.writableDatabase

        // Step 2: Insert accounts, categories, Bengali transactions, amounts, settings, and parties into v3
        v3Db.execSQL("""
            INSERT INTO accounts (id, name, type, currency, initialBalancePaisa, colorHex, iconName, displayOrder, isArchived, isDeleted, revisionNumber, createdAtEpochMs, updatedAtEpochMs)
            VALUES ('acc_salary', 'বেতন হিসাব', 'BANK', 'BDT', 5000000, '#0F294D', 'account_balance', 0, 0, 0, 1, 1728000000000, 1728000000000)
        """.trimIndent())

        v3Db.execSQL("""
            INSERT INTO categories (id, nameBn, nameEn, parentCategoryId, iconName, colorHex, isMicroExpense, isIncomeCategory, isSystemDefault, displayOrder, isArchived, isDeleted, revisionNumber, updatedAtEpochMs)
            VALUES ('cat_salary', 'মাসিক বেতন', 'Salary', NULL, 'work', '#059669', 0, 1, 0, 0, 0, 0, 1, 1728000000000)
        """.trimIndent())

        v3Db.execSQL("""
            INSERT INTO categories (id, nameBn, nameEn, parentCategoryId, iconName, colorHex, isMicroExpense, isIncomeCategory, isSystemDefault, displayOrder, isArchived, isDeleted, revisionNumber, updatedAtEpochMs)
            VALUES ('cat_bazar', 'দৈনিক বাজার', 'Groceries', NULL, 'shopping_basket', '#DC2626', 0, 0, 0, 1, 0, 0, 1, 1728000000000)
        """.trimIndent())

        v3Db.execSQL("""
            INSERT INTO parties (id, displayName, partyType, phone, email, note, isArchived, isDeleted, createdAtEpochMs, updatedAtEpochMs)
            VALUES ('party_karim', 'করিম চাচা', 'SUPPLIER', '01711000000', 'karim@example.com', 'দোকানের সরবরাহকারী', 0, 0, 1728000000000, 1728000000000)
        """.trimIndent())

        v3Db.execSQL("""
            INSERT INTO transactions (id, eventCode, eventKind, amountPaisa, currency, sourceAccountId, sourceDescription, necessity, categoryId, partyId, confirmationStatus, eventTimestampEpochMs, description, notes, deviceId, isDeleted, revisionNumber, createdAtEpochMs, updatedAtEpochMs)
            VALUES ('tx_bengali_income', 'TX_01_INCOME', 'INCOME', 5000000, 'BDT', 'acc_salary', 'অফিস বেতন', 'প্রয়োজনীয়', 'cat_salary', NULL, 'CONFIRMED', 1728000000000, 'অক্টোবর মাসের বেতন প্রাপ্তি', 'অক্টোবর মাসের বেতন', 'dev1', 0, 1, 1728000000000, 1728000000000)
        """.trimIndent())

        v3Db.execSQL("""
            INSERT INTO transactions (id, eventCode, eventKind, amountPaisa, currency, sourceAccountId, sourceDescription, necessity, categoryId, partyId, confirmationStatus, eventTimestampEpochMs, description, notes, deviceId, isDeleted, revisionNumber, createdAtEpochMs, updatedAtEpochMs)
            VALUES ('tx_bengali_expense', 'TX_02_EXPENSE', 'EXPENSE', 350000, 'BDT', 'acc_salary', 'নগদ বাজার', 'অপরিহার্য', 'cat_bazar', 'party_karim', 'CONFIRMED', 1728005000000, 'চাল ও ডাল কেনা', 'চাল ডাল তেল', 'dev1', 0, 1, 1728005000000, 1728005000000)
        """.trimIndent())

        v3Db.execSQL("""
            INSERT INTO app_settings (key, value, updatedAtEpochMs)
            VALUES ('currency_symbol', '৳', 1728000000000)
        """.trimIndent())

        v3Db.execSQL("""
            INSERT INTO app_settings (key, value, updatedAtEpochMs)
            VALUES ('theme_mode', 'NAVY_BLUE', 1728000000000)
        """.trimIndent())

        // Ensure version is set to 3
        v3Db.version = 3
        v3Db.close()

        // Step 3: Open and migrate using Room and MIGRATION_3_4
        val db = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
            .allowMainThreadQueries()
            .build()
        appDatabase = db

        // Step 4: Verify database version is 4
        assertEquals(4, db.openHelper.readableDatabase.version)

        // Step 5: Verify original records, IDs, Bengali text, amounts, and relationships remain unchanged
        val account = db.accountDao().getById("acc_salary")
        assertNotNull("Original account must exist", account)
        assertEquals("acc_salary", account?.id)
        assertEquals("বেতন হিসাব", account?.name)
        assertEquals(5000000L, account?.initialBalancePaisa)
        assertEquals("BDT", account?.currency)

        val catSalary = db.categoryDao().getById("cat_salary")
        assertNotNull("Salary category must exist", catSalary)
        assertEquals("মাসিক বেতন", catSalary?.nameBn)

        val catBazar = db.categoryDao().getById("cat_bazar")
        assertNotNull("Bazar category must exist", catBazar)
        assertEquals("দৈনিক বাজার", catBazar?.nameBn)

        val party = db.partyDao().getById("party_karim")
        assertNotNull("Party must exist", party)
        assertEquals("করিম চাচা", party?.displayName)
        assertEquals("01711000000", party?.phone)

        val txIncome = db.ledgerEventDao().getById("tx_bengali_income")
        assertNotNull("Income transaction must exist", txIncome)
        assertEquals("tx_bengali_income", txIncome?.id)
        assertEquals(5000000L, txIncome?.amountPaisa)
        assertEquals("অফিস বেতন", txIncome?.sourceDescription)
        assertEquals("অক্টোবর মাসের বেতন", txIncome?.notes)
        assertEquals(1728000000000L, txIncome?.eventTimestampEpochMs)
        assertEquals("cat_salary", txIncome?.categoryId)

        val txExpense = db.ledgerEventDao().getById("tx_bengali_expense")
        assertNotNull("Expense transaction must exist", txExpense)
        assertEquals("tx_bengali_expense", txExpense?.id)
        assertEquals(350000L, txExpense?.amountPaisa)
        assertEquals("চাল ডাল তেল", txExpense?.notes)
        assertEquals("cat_bazar", txExpense?.categoryId)
        assertEquals("party_karim", txExpense?.partyId)

        val settingCurrency = db.appSettingDao().get("currency_symbol")
        assertNotNull("Currency setting must exist", settingCurrency)
        assertEquals("৳", settingCurrency)

        val settingTheme = db.appSettingDao().get("theme_mode")
        assertNotNull("Theme setting must exist", settingTheme)
        assertEquals("NAVY_BLUE", settingTheme)

        // Step 6: Verify all 12 Version 4 tables exist in sqlite_master
        val cursor = db.openHelper.readableDatabase.query("SELECT name FROM sqlite_master WHERE type='table'")
        val tableNames = mutableSetOf<String>()
        while (cursor.moveToNext()) {
            tableNames.add(cursor.getString(0))
        }
        cursor.close()

        val expectedV4Tables = listOf(
            "transaction_line_items",
            "invoices",
            "invoice_items",
            "life_plans",
            "plan_milestones",
            "ideas",
            "personal_notes",
            "vault_items",
            "shopping_lists",
            "shopping_items",
            "personal_contacts",
            "progress_audits"
        )

        for (table in expectedV4Tables) {
            assertTrue("Version 4 table '$table' must exist after migration", tableNames.contains(table))
        }

        // Step 7: Verify write and read operations on new version 4 tables work
        db.openHelper.writableDatabase.execSQL("""
            INSERT INTO personal_notes (id, title, content, tags, isPinned, isArchived, isDeleted, reminderEpochMs, createdAtEpochMs, updatedAtEpochMs)
            VALUES ('note_1', 'জরুরি নোট', 'কড়ি সংস্করণ ৪ সফলভাবে মাইগ্রেট হয়েছে।', 'মাইগ্রেশন', 1, 0, 0, NULL, 1728000000000, 1728000000000)
        """.trimIndent())

        val noteCursor = db.openHelper.readableDatabase.query("SELECT title FROM personal_notes WHERE id='note_1'")
        assertTrue("Newly created v4 note must be queryable", noteCursor.moveToFirst())
        assertEquals("জরুরি নোট", noteCursor.getString(0))
        noteCursor.close()
    }

    private fun createVersion3Tables(db: SupportSQLiteDatabase) {
        val schemaFile = listOf(
            File("schemas/com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase/3.json"),
            File("app/schemas/com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase/3.json")
        ).firstOrNull { it.exists() } ?: error("3.json schema file not found")

        val json = JSONObject(schemaFile.readText(Charsets.UTF_8))
        val databaseObj = json.getJSONObject("database")
        val entities = databaseObj.getJSONArray("entities")

        for (i in 0 until entities.length()) {
            val entity = entities.getJSONObject(i)
            val tableName = entity.getString("tableName")
            val rawCreateSql = entity.getString("createSql")
            val createSql = rawCreateSql.replace("\${TABLE_NAME}", tableName)
            db.execSQL(createSql)

            if (entity.has("indices")) {
                val indices = entity.getJSONArray("indices")
                for (j in 0 until indices.length()) {
                    val idx = indices.getJSONObject(j)
                    val rawIndexSql = idx.getString("createSql")
                    val indexSql = rawIndexSql.replace("\${TABLE_NAME}", tableName)
                    db.execSQL(indexSql)
                }
            }
        }

        val setupQueries = databaseObj.getJSONArray("setupQueries")
        for (i in 0 until setupQueries.length()) {
            db.execSQL(setupQueries.getString(i))
        }
    }
}
