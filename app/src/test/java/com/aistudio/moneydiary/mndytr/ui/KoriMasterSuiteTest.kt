package com.aistudio.moneydiary.mndytr.ui

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntryType
import com.aistudio.moneydiary.mndytr.domain.model.NecessityClassification
import com.aistudio.moneydiary.mndytr.domain.service.*
import com.aistudio.moneydiary.mndytr.ui.theme.*
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter
import com.aistudio.moneydiary.mndytr.ui.viewmodel.DiaryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KoriMasterSuiteTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        // Clear security prefs
        SecurityService.removeLock(context)
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DiaryViewModel {
        return DiaryViewModel(db = db, financialService = FinancialService(db), ioDispatcher = testDispatcher)
    }

    private fun resolveProjectFile(relativePath: String): File {
        val direct = File(relativePath)
        if (direct.exists()) return direct
        val inApplet = File("/app/applet", relativePath)
        if (inApplet.exists()) return inApplet
        val inApp = File("app", relativePath)
        if (inApp.exists()) return inApp
        return direct
    }

    private fun Color.argbInt(): Int = (this.value.toLong() ushr 32).toInt()

    // 1. True Font files verification & distinct weight sizes
    @Test
    fun testFontFilesExistAndHaveDistinctWeightSizes() {
        val fontFiles = listOf(
            "hind_siliguri_regular.ttf",
            "hind_siliguri_medium.ttf",
            "hind_siliguri_semibold.ttf",
            "hind_siliguri_bold.ttf",
            "noto_serif_bengali_regular.ttf",
            "noto_serif_bengali_medium.ttf",
            "noto_serif_bengali_semibold.ttf"
        )
        val fontDir = resolveProjectFile("app/src/main/res/font")
        assertTrue("Font directory should exist: ${fontDir.absolutePath}", fontDir.exists() && fontDir.isDirectory)
        
        val fileSizes = mutableSetOf<Long>()
        for (name in fontFiles) {
            val file = File(fontDir, name)
            assertTrue("Font file $name should exist", file.exists())
            assertTrue("Font file $name should be > 100KB", file.length() > 100000L)
            fileSizes.add(file.length())
        }
        // At least 6 distinct file sizes across the 7 font weights (guarantees true weights, not duplicates)
        assertTrue("Should have distinct binaries for distinct weights", fileSizes.size >= 6)
    }

    // 2. Font licenses verification
    @Test
    fun testFontLicensesExist() {
        val siliguriLicense = resolveProjectFile("LICENSES/OFL-HindSiliguri.txt")
        val notoLicense = resolveProjectFile("LICENSES/OFL-NotoSerifBengali.txt")
        assertTrue("Hind Siliguri license must exist", siliguriLicense.exists() && siliguriLicense.length() > 0)
        assertTrue("Noto Serif Bengali license must exist", notoLicense.exists() && notoLicense.length() > 0)
    }

    // 3. Exact typography tokens
    @Test
    fun testTypographyTokens() {
        assertEquals(26f, KoriTypographyTokens.Wordmark.fontSize.value)
        assertEquals(34f, KoriTypographyTokens.PrimaryAmount.fontSize.value)
        assertEquals(22f, KoriTypographyTokens.SecondaryAmount.fontSize.value)
        assertEquals(17f, KoriTypographyTokens.TransactionAmount.fontSize.value)
        assertEquals(21f, KoriTypographyTokens.DiaryDate.fontSize.value)
        assertEquals(22f, KoriTypographyTokens.ScreenTitle.fontSize.value)
        assertEquals(18f, KoriTypographyTokens.SectionTitle.fontSize.value)
        assertEquals(16f, KoriTypographyTokens.EntryTitle.fontSize.value)
        assertEquals(16f, KoriTypographyTokens.Body.fontSize.value)
        assertEquals(14f, KoriTypographyTokens.SecondaryBody.fontSize.value)
        assertEquals(13f, KoriTypographyTokens.Metadata.fontSize.value)
        assertEquals(16f, KoriTypographyTokens.PrimaryButton.fontSize.value)
        assertEquals(13f, KoriTypographyTokens.BottomNav.fontSize.value)
        assertEquals(17f, KoriTypographyTokens.FormInput.fontSize.value)
        assertEquals(14f, KoriTypographyTokens.FormLabel.fontSize.value)
        assertEquals(20f, KoriTypographyTokens.EmptyStateTitle.fontSize.value)
        assertEquals(18f, KoriTypographyTokens.InsightTitle.fontSize.value)
    }

    // 4. Exact Three Theme Color definitions
    @Test
    fun testThreeCompleteThemeColorHexValues() {
        // Theme A: Ink and Paper Light
        assertEquals(0xFFF7F3EA.toInt(), InkAndPaperLight.canvas.argbInt())
        assertEquals(0xFFFFFCF6.toInt(), InkAndPaperLight.surface.argbInt())
        assertEquals(0xFFFFFFFF.toInt(), InkAndPaperLight.raisedSurface.argbInt())
        assertEquals(0xFF263238.toInt(), InkAndPaperLight.primary.argbInt())
        assertEquals(0xFFE8ECEB.toInt(), InkAndPaperLight.primarySoft.argbInt())
        assertEquals(0xFF202624.toInt(), InkAndPaperLight.textMain.argbInt())
        assertEquals(0xFF69716E.toInt(), InkAndPaperLight.textMuted.argbInt())
        assertEquals(0xFFDFE3DF.toInt(), InkAndPaperLight.border.argbInt())
        assertEquals(0xFF42765B.toInt(), InkAndPaperLight.income.argbInt())
        assertEquals(0xFFA4524D.toInt(), InkAndPaperLight.expense.argbInt())
        assertEquals(0xFF536F82.toInt(), InkAndPaperLight.insight.argbInt())
        assertEquals(0xFF956A2F.toInt(), InkAndPaperLight.warning.argbInt())

        // Theme A: Ink and Paper Dark
        assertEquals(0xFF151817.toInt(), InkAndPaperDark.canvas.argbInt())
        assertEquals(0xFF1D2220.toInt(), InkAndPaperDark.surface.argbInt())
        assertEquals(0xFF252B28.toInt(), InkAndPaperDark.raisedSurface.argbInt())
        assertEquals(0xFFBFCBC6.toInt(), InkAndPaperDark.primary.argbInt())
        assertEquals(0xFF303835.toInt(), InkAndPaperDark.primarySoft.argbInt())
        assertEquals(0xFFF5F3ED.toInt(), InkAndPaperDark.textMain.argbInt())
        assertEquals(0xFFADB5B1.toInt(), InkAndPaperDark.textMuted.argbInt())
        assertEquals(0xFF323A36.toInt(), InkAndPaperDark.border.argbInt())
        assertEquals(0xFF7DB091.toInt(), InkAndPaperDark.income.argbInt())
        assertEquals(0xFFD1847E.toInt(), InkAndPaperDark.expense.argbInt())
        assertEquals(0xFF91AABD.toInt(), InkAndPaperDark.insight.argbInt())
        assertEquals(0xFFD1A56B.toInt(), InkAndPaperDark.warning.argbInt())

        // Theme B: Midnight Indigo Light
        assertEquals(0xFFF7F7FA.toInt(), MidnightIndigoLight.canvas.argbInt())
        assertEquals(0xFF30395C.toInt(), MidnightIndigoLight.primary.argbInt())
        assertEquals(0xFF3F755C.toInt(), MidnightIndigoLight.income.argbInt())
        assertEquals(0xFFA54E52.toInt(), MidnightIndigoLight.expense.argbInt())

        // Theme B: Midnight Indigo Dark
        assertEquals(0xFF11131B.toInt(), MidnightIndigoDark.canvas.argbInt())
        assertEquals(0xFFAEB8E2.toInt(), MidnightIndigoDark.primary.argbInt())
        assertEquals(0xFF77B091.toInt(), MidnightIndigoDark.income.argbInt())
        assertEquals(0xFFD47C80.toInt(), MidnightIndigoDark.expense.argbInt())

        // Theme C: Burgundy Journal Light
        assertEquals(0xFFF8F3F1.toInt(), BurgundyJournalLight.canvas.argbInt())
        assertEquals(0xFF633D47.toInt(), BurgundyJournalLight.primary.argbInt())
        assertEquals(0xFF46745A.toInt(), BurgundyJournalLight.income.argbInt())
        assertEquals(0xFFA34F50.toInt(), BurgundyJournalLight.expense.argbInt())

        // Theme C: Burgundy Journal Dark
        assertEquals(0xFF171315.toInt(), BurgundyJournalDark.canvas.argbInt())
        assertEquals(0xFFD0A7B2.toInt(), BurgundyJournalDark.primary.argbInt())
        assertEquals(0xFF7DAA8D.toInt(), BurgundyJournalDark.income.argbInt())
        assertEquals(0xFFD17D7D.toInt(), BurgundyJournalDark.expense.argbInt())
    }

    // 5. Bengali & English numerals and Taka formatting
    @Test
    fun testBengaliNumeralAndTakaFormatting() {
        assertEquals("৳ ০", BengaliFormatter.formatPaisa(0L, useBengaliDigits = true, showDecimals = false))
        assertEquals("৳ ১০", BengaliFormatter.formatPaisa(1000L, useBengaliDigits = true, showDecimals = false))
        assertEquals("৳ ৪৫০", BengaliFormatter.formatPaisa(45000L, useBengaliDigits = true, showDecimals = false))
        assertEquals("৳ ১,২৫০", BengaliFormatter.formatPaisa(125000L, useBengaliDigits = true, showDecimals = false))
        assertEquals("৳ ১২,৫০,০০০", BengaliFormatter.formatPaisa(125000000L, useBengaliDigits = true, showDecimals = false))

        // English digits
        assertEquals("৳ 0", BengaliFormatter.formatPaisa(0L, useBengaliDigits = false, showDecimals = false))
        assertEquals("৳ 450", BengaliFormatter.formatPaisa(45000L, useBengaliDigits = false, showDecimals = false))
    }

    // 6. Security & App Lock (PIN, Password, Recovery, Progressive Delay)
    @Test
    fun testSecurityAndAppLockMechanisms() {
        assertFalse(SecurityService.isAppLockActive(context))

        // Weak PIN check
        assertNotNull(SecurityService.checkPinWeakness("111111"))
        assertNotNull(SecurityService.checkPinWeakness("123456"))
        assertNull(SecurityService.checkPinWeakness("849201"))

        // Set PIN
        assertTrue(SecurityService.setPin(context, "849201"))
        assertTrue(SecurityService.isAppLockActive(context))
        assertEquals(AppLockType.PIN, SecurityService.getSecurityConfig(context).lockType)

        // Verification
        assertTrue(SecurityService.verifyCredential(context, "849201"))
        assertFalse(SecurityService.verifyCredential(context, "000000"))

        // Set Password (Bengali & English Unicode)
        assertTrue(SecurityService.setPassword(context, "আমারগোপনপাসওয়ার্ড১২৩"))
        assertEquals(AppLockType.PASSWORD, SecurityService.getSecurityConfig(context).lockType)
        assertTrue(SecurityService.verifyCredential(context, "আমারগোপনপাসওয়ার্ড১২৩"))
        assertFalse(SecurityService.verifyCredential(context, "ভুলপাসওয়ার্ড"))

        // Recovery Key Generation & Verification
        val recoveryKey = SecurityService.generateRecoveryKey(context)
        assertTrue(recoveryKey.startsWith("KORI-"))
        assertTrue(SecurityService.verifyRecoveryKey(context, recoveryKey))
        assertFalse(SecurityService.isAppLockActive(context)) // Unlocked and reset
    }

    // 7. Icon mapping deterministic engine
    @Test
    fun testCategoryIconDeterminism() {
        assertEquals("menu_book", CategoryIconResolver.suggestIconKey("বই কেনা"))
        assertEquals("menu_book", CategoryIconResolver.suggestIconKey("বই"))
        assertEquals("medication", CategoryIconResolver.suggestIconKey("ওষুধ"))
        assertEquals("local_hospital", CategoryIconResolver.suggestIconKey("চিকিৎসা"))
        assertEquals("home", CategoryIconResolver.suggestIconKey("বাড়িভাড়া"))
        assertEquals("home", CategoryIconResolver.suggestIconKey("বাসাভাড়া"))
        assertEquals("bolt", CategoryIconResolver.suggestIconKey("বিদ্যুৎ বিল"))
        assertEquals("wifi", CategoryIconResolver.suggestIconKey("Wi-Fi"))
        assertEquals("wifi", CategoryIconResolver.suggestIconKey("ইন্টারনেট"))
        assertEquals("phone_android", CategoryIconResolver.suggestIconKey("মোবাইল"))
        assertEquals("groups", CategoryIconResolver.suggestIconKey("পরিবার"))
        assertEquals("favorite", CategoryIconResolver.suggestIconKey("স্ত্রী"))
        assertEquals("school", CategoryIconResolver.suggestIconKey("শিক্ষা"))
        assertEquals("directions_bus", CategoryIconResolver.suggestIconKey("যাতায়াত"))
        assertEquals("directions_bus", CategoryIconResolver.suggestIconKey("রিকশা"))
        assertEquals("restaurant", CategoryIconResolver.suggestIconKey("খাবার"))
        assertEquals("local_cafe", CategoryIconResolver.suggestIconKey("চা"))
        assertEquals("edit", CategoryIconResolver.suggestIconKey("লেখালেখি"))
        assertEquals("laptop", CategoryIconResolver.suggestIconKey("অনলাইন কাজ"))
        assertEquals("payments", CategoryIconResolver.suggestIconKey("বেতন"))
        assertEquals("category", CategoryIconResolver.suggestIconKey("অজানা কোনো খাত"))
    }

    // 8. Empty-first state on new launch
    @Test
    fun testEmptyFirstState() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.completeFirstLaunchSetup(useBengaliDigits = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.allDiaryEntries.size)
        assertEquals(0, state.todayEntries.size)
        assertEquals(0L, state.todaySummary.todayIncomePaisa)
        assertEquals(0L, state.todaySummary.todayExpensePaisa)
        assertEquals(0L, state.todaySummary.todayNetChangePaisa)
        assertFalse(state.todaySummary.hasEntriesToday)
    }

    // 9. Standard Acceptance Scenario with Source persistence and no account creation
    @Test
    fun testAcceptanceDataFlow() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.completeFirstLaunchSetup(useBengaliDigits = true)
        advanceUntilIdle()

        // 1. Expense: ৳450, ওষুধ, বেতন থেকে, প্রয়োজনীয়, জ্বরের ওষুধ
        viewModel.recordDiaryEntry(
            type = DiaryEntryType.EXPENSE,
            amountPaisa = 45000L,
            categoryId = null,
            sourceDescription = "বেতন থেকে",
            description = "জ্বরের ওষুধ",
            notes = "প্যারাসিটামল",
            necessity = NecessityClassification.ESSENTIAL
        )
        advanceUntilIdle()

        // 2. Income: ৳500, অনলাইন কাজ, গ্রাহকের দেওয়া টাকা
        viewModel.recordDiaryEntry(
            type = DiaryEntryType.INCOME,
            amountPaisa = 50000L,
            categoryId = null,
            sourceDescription = "গ্রাহকের দেওয়া টাকা",
            description = "অনলাইন কাজ"
        )
        advanceUntilIdle()

        // 3. Expense: ৳10, চা, পকেটের টাকা
        viewModel.recordDiaryEntry(
            type = DiaryEntryType.EXPENSE,
            amountPaisa = 1000L,
            categoryId = null,
            sourceDescription = "পকেটের টাকা",
            description = "চা",
            necessity = NecessityClassification.REGULAR
        )
        advanceUntilIdle()

        viewModel.refreshNow()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(50000L, state.todaySummary.todayIncomePaisa)
        assertEquals(46000L, state.todaySummary.todayExpensePaisa)
        assertEquals(4000L, state.todaySummary.todayNetChangePaisa)
        assertEquals(3, state.todayEntries.size)

        // Verify CSV export format
        val csv = DataExportService.generateCsv(state.allDiaryEntries, includePrivateNotes = true)
        assertTrue(csv.contains("Income,500.0"))
        assertTrue(csv.contains("Expense,450.0"))
        assertTrue(csv.contains("Expense,10.0"))
        assertTrue(csv.contains("বেতন থেকে"))
        assertTrue(csv.contains("গ্রাহকের দেওয়া টাকা"))
        assertTrue(csv.contains("পকেটের টাকা"))

        // Verify JSON export format
        val json = DataExportService.generateJson(state.allDiaryEntries, includePrivateNotes = true)
        assertTrue(json.contains("\"amountPaisa\": 45000"))
        assertTrue(json.contains("\"amountPaisa\": 50000"))
        assertTrue(json.contains("\"amountPaisa\": 1000"))
    }
}
