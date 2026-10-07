package com.aistudio.moneydiary.mndytr.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// KORI VERSION 4.0 THREE COMPLETE PREMIUM COLOUR THEMES
// Semantic Palette & Color Tokens
// ==========================================

enum class KoriColorIdentity(val id: String, val titleBn: String, val descriptionBn: String) {
    INK_AND_PAPER("INK_AND_PAPER", "কালি ও কাগজ", "শান্ত ডিফল্ট থিম • ক্লাসিক ফরেস্ট ও অফ-হোয়াইট"),
    MIDNIGHT_INDIGO("MIDNIGHT_INDIGO", "মধ্যরাতের নীল", "গম্ভীর ইন্ডিগো ও পরিচ্ছন্ন নীলাভ আভা"),
    BURGUNDY_JOURNAL("BURGUNDY_JOURNAL", "বারগান্ডি ডায়েরি", "উষ্ণ বারগান্ডি ও মার্জিত ডায়েরির অনুভূতি")
}

data class KoriThemeColors(
    val canvas: Color,
    val surface: Color,
    val raisedSurface: Color,
    val primary: Color,
    val primarySoft: Color,
    val textMain: Color,
    val textMuted: Color,
    val border: Color,
    val income: Color,
    val expense: Color,
    val insight: Color,
    val warning: Color,
    val error: Color,
    val success: Color,
    val disabledText: Color,
    val disabledSurface: Color
)

// ------------------------------------------
// THEME A: INK AND PAPER (Default)
// ------------------------------------------
val InkAndPaperLight = KoriThemeColors(
    canvas = Color(0xFFF7F3EA),
    surface = Color(0xFFFFFCF6),
    raisedSurface = Color(0xFFFFFFFF),
    primary = Color(0xFF263238),
    primarySoft = Color(0xFFE8ECEB),
    textMain = Color(0xFF202624),
    textMuted = Color(0xFF69716E),
    border = Color(0xFFDFE3DF),
    income = Color(0xFF42765B),
    expense = Color(0xFFA4524D),
    insight = Color(0xFF536F82),
    warning = Color(0xFF956A2F),
    error = Color(0xFFA4524D),
    success = Color(0xFF42765B),
    disabledText = Color(0xFF9EA7A3),
    disabledSurface = Color(0xFFEEEBE2)
)

val InkAndPaperDark = KoriThemeColors(
    canvas = Color(0xFF151817),
    surface = Color(0xFF1D2220),
    raisedSurface = Color(0xFF252B28),
    primary = Color(0xFFBFCBC6),
    primarySoft = Color(0xFF303835),
    textMain = Color(0xFFF5F3ED),
    textMuted = Color(0xFFADB5B1),
    border = Color(0xFF323A36),
    income = Color(0xFF7DB091),
    expense = Color(0xFFD1847E),
    insight = Color(0xFF91AABD),
    warning = Color(0xFFD1A56B),
    error = Color(0xFFD1847E),
    success = Color(0xFF7DB091),
    disabledText = Color(0xFF5C6561),
    disabledSurface = Color(0xFF232926)
)

// ------------------------------------------
// THEME B: MIDNIGHT INDIGO
// ------------------------------------------
val MidnightIndigoLight = KoriThemeColors(
    canvas = Color(0xFFF7F7FA),
    surface = Color(0xFFFFFFFF),
    raisedSurface = Color(0xFFFCFCFF),
    primary = Color(0xFF30395C),
    primarySoft = Color(0xFFE9EBF4),
    textMain = Color(0xFF222535),
    textMuted = Color(0xFF696D7E),
    border = Color(0xFFE1E3EA),
    income = Color(0xFF3F755C),
    expense = Color(0xFFA54E52),
    insight = Color(0xFF526F9D),
    warning = Color(0xFF9A6C2E),
    error = Color(0xFFA54E52),
    success = Color(0xFF3F755C),
    disabledText = Color(0xFF9EA2B4),
    disabledSurface = Color(0xFFECEEF5)
)

val MidnightIndigoDark = KoriThemeColors(
    canvas = Color(0xFF11131B),
    surface = Color(0xFF191C27),
    raisedSurface = Color(0xFF222635),
    primary = Color(0xFFAEB8E2),
    primarySoft = Color(0xFF2D3245),
    textMain = Color(0xFFF4F5FA),
    textMuted = Color(0xFFADB1C2),
    border = Color(0xFF313648),
    income = Color(0xFF77B091),
    expense = Color(0xFFD47C80),
    insight = Color(0xFF92A9D4),
    warning = Color(0xFFD4A364),
    error = Color(0xFFD47C80),
    success = Color(0xFF77B091),
    disabledText = Color(0xFF5B6073),
    disabledSurface = Color(0xFF202331)
)

// ------------------------------------------
// THEME C: BURGUNDY JOURNAL
// ------------------------------------------
val BurgundyJournalLight = KoriThemeColors(
    canvas = Color(0xFFF8F3F1),
    surface = Color(0xFFFFFDFC),
    raisedSurface = Color(0xFFFFFFFF),
    primary = Color(0xFF633D47),
    primarySoft = Color(0xFFF0E5E7),
    textMain = Color(0xFF2C2527),
    textMuted = Color(0xFF73676A),
    border = Color(0xFFE8DDDF),
    income = Color(0xFF46745A),
    expense = Color(0xFFA34F50),
    insight = Color(0xFF5A7188),
    warning = Color(0xFF986B30),
    error = Color(0xFFA34F50),
    success = Color(0xFF46745A),
    disabledText = Color(0xFFA5999C),
    disabledSurface = Color(0xFFEFE7E9)
)

val BurgundyJournalDark = KoriThemeColors(
    canvas = Color(0xFF171315),
    surface = Color(0xFF211A1D),
    raisedSurface = Color(0xFF2A2225),
    primary = Color(0xFFD0A7B2),
    primarySoft = Color(0xFF3A2930),
    textMain = Color(0xFFF7F1F3),
    textMuted = Color(0xFFBDAEB2),
    border = Color(0xFF3C3034),
    income = Color(0xFF7DAA8D),
    expense = Color(0xFFD17D7D),
    insight = Color(0xFF91AABD),
    warning = Color(0xFFD2A16A),
    error = Color(0xFFD17D7D),
    success = Color(0xFF7DAA8D),
    disabledText = Color(0xFF67585C),
    disabledSurface = Color(0xFF282024)
)

val LocalKoriColors = staticCompositionLocalOf { InkAndPaperLight }

object KoriTheme {
    val colors: KoriThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKoriColors.current
}

// Backward compatibility aliases for existing references
val LightKoriBackground get() = InkAndPaperLight.canvas
val LightKoriSurface get() = InkAndPaperLight.surface
val LightKoriPrimary get() = InkAndPaperLight.primary
val LightKoriPrimarySoft get() = InkAndPaperLight.primarySoft
val LightKoriTextPrimary get() = InkAndPaperLight.textMain
val LightKoriTextSecondary get() = InkAndPaperLight.textMuted
val LightKoriBorder get() = InkAndPaperLight.border
val LightKoriIncome get() = InkAndPaperLight.income
val LightKoriExpense get() = InkAndPaperLight.expense
val LightKoriInsight get() = InkAndPaperLight.insight
val LightKoriWarning get() = InkAndPaperLight.warning

val DarkKoriBackground get() = InkAndPaperDark.canvas
val DarkKoriSurface get() = InkAndPaperDark.surface
val DarkKoriRaisedSurface get() = InkAndPaperDark.raisedSurface
val DarkKoriPrimary get() = InkAndPaperDark.primary
val DarkKoriPrimarySoft get() = InkAndPaperDark.primarySoft
val DarkKoriTextPrimary get() = InkAndPaperDark.textMain
val DarkKoriTextSecondary get() = InkAndPaperDark.textMuted
val DarkKoriBorder get() = InkAndPaperDark.border
val DarkKoriIncome get() = InkAndPaperDark.income
val DarkKoriExpense get() = InkAndPaperDark.expense
val DarkKoriInsight get() = InkAndPaperDark.insight
val DarkKoriWarning get() = InkAndPaperDark.warning

val PrimaryEmerald get() = LightKoriPrimary
val IncomeGreen get() = LightKoriIncome
val ExpenseRed get() = LightKoriExpense
val SecondaryGold get() = LightKoriWarning
val LightBackground get() = LightKoriBackground
val LightSurface get() = LightKoriSurface
val DarkBackground get() = DarkKoriBackground
val DarkSurface get() = DarkKoriSurface

val IncomeGreenContainer = Color(0xFFE8F5E9)
val ExpenseRedContainer = Color(0xFFFFEBEE)
val AdvanceAmber = Color(0xFFE65100)
val AdvanceAmberContainer = Color(0xFFFFF3E0)
val TransferTeal = Color(0xFF00695C)
val TransferTealContainer = Color(0xFFE0F2F1)
val ReceivableBlue = Color(0xFF1565C0)
val ReceivableBlueContainer = Color(0xFFE3F2FD)

