package com.aistudio.moneydiary.mndytr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aistudio.moneydiary.mndytr.R

// ==========================================
// KORI TYPOGRAPHY SYSTEM
// Primary Bengali Typeface: Adorsholipi (bundled locally)
// Primary English Typeface: Times New Roman (Serif)
// ==========================================

val AdorsholipiFontFamily = FontFamily(
    Font(R.font.adorsholipi, FontWeight.Normal),
    Font(R.font.adorsholipi, FontWeight.Medium),
    Font(R.font.adorsholipi, FontWeight.SemiBold),
    Font(R.font.adorsholipi, FontWeight.Bold)
)

val TimesNewRomanFontFamily = FontFamily.Serif

// Backward-compatible aliases mapped to Adorsholipi
val HindSiliguriFontFamily = AdorsholipiFontFamily
val NotoSerifBengaliFontFamily = AdorsholipiFontFamily

// Specific Editorial & Amount Typography Tokens
object KoriTypographyTokens {
    // Wordmark: Adorsholipi SemiBold 26sp
    val Wordmark = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    )

    // Primary amount: Adorsholipi SemiBold 34sp
    val PrimaryAmount = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp
    )

    // Secondary amount: Adorsholipi SemiBold 22sp
    val SecondaryAmount = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    )

    // Transaction amount: Adorsholipi Medium 17sp
    val TransactionAmount = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // Diary date: Adorsholipi Medium 21sp
    val DiaryDate = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 21.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    )

    // Screen title: Adorsholipi SemiBold 22sp
    val ScreenTitle = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    )

    // Section title: Adorsholipi SemiBold 18sp
    val SectionTitle = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    )

    // Entry title: Adorsholipi Medium 16sp
    val EntryTitle = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    )

    // Body: Adorsholipi Regular 16sp
    val Body = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // Primary body alias
    val PrimaryBody = Body

    // Secondary body / Caption: Adorsholipi Regular 14sp
    val SecondaryBody = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    // Metadata: Adorsholipi Regular 13sp
    val Metadata = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    // Primary button: Adorsholipi SemiBold 16sp
    val PrimaryButton = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    )

    // Bottom nav: Adorsholipi Medium 13sp
    val BottomNav = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    // Form input: Adorsholipi Regular 17sp
    val FormInput = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // Form label: Adorsholipi Medium 14sp
    val FormLabel = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    // Empty state title: Adorsholipi SemiBold 20sp
    val EmptyStateTitle = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )

    // Insight title: Adorsholipi SemiBold 18sp
    val InsightTitle = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    )

    // Chip / Tag: Adorsholipi Medium 13sp
    val Chip = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    // TitleBn: Adorsholipi SemiBold 20sp
    val TitleBn = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )

    // English Statement / Invoice Body: Times New Roman
    val EnglishSerifBody = TextStyle(
        fontFamily = TimesNewRomanFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    val EnglishSerifTitle = TextStyle(
        fontFamily = TimesNewRomanFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    )
}

// Complete Material 3 Typography Scale using Adorsholipi
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AdorsholipiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)

val AppTypography = Typography
