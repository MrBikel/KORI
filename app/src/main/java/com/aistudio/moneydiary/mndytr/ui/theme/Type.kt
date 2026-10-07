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
// Primary UI: Hind Siliguri (Regular, Medium, SemiBold, Bold)
// Editorial Accent: Noto Serif Bengali (Regular, Medium, SemiBold)
// ==========================================

val HindSiliguriFontFamily = FontFamily(
    Font(R.font.hind_siliguri_regular, FontWeight.Normal),
    Font(R.font.hind_siliguri_medium, FontWeight.Medium),
    Font(R.font.hind_siliguri_semibold, FontWeight.SemiBold),
    Font(R.font.hind_siliguri_bold, FontWeight.Bold)
)

val NotoSerifBengaliFontFamily = FontFamily(
    Font(R.font.noto_serif_bengali_regular, FontWeight.Normal),
    Font(R.font.noto_serif_bengali_medium, FontWeight.Medium),
    Font(R.font.noto_serif_bengali_semibold, FontWeight.SemiBold)
)

// Specific Editorial & Amount Typography Tokens
object KoriTypographyTokens {
    // Wordmark: Noto Serif Bengali SemiBold 26sp, Line height 34sp
    val Wordmark = TextStyle(
        fontFamily = NotoSerifBengaliFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    )

    // Primary amount: Hind Siliguri SemiBold 34sp, Line height 42sp
    val PrimaryAmount = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp
    )

    // Secondary amount: Hind Siliguri SemiBold 22sp, Line height 30sp
    val SecondaryAmount = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    )

    // Transaction amount: Hind Siliguri Medium 17sp, Line height 24sp
    val TransactionAmount = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // Diary date: Noto Serif Bengali Medium 21sp, Line height 30sp
    val DiaryDate = TextStyle(
        fontFamily = NotoSerifBengaliFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 21.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    )

    // Screen title: Hind Siliguri SemiBold 22sp, Line height 30sp
    val ScreenTitle = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    )

    // Section title: Hind Siliguri SemiBold 18sp, Line height 26sp
    val SectionTitle = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    )

    // Entry title: Hind Siliguri Medium 16sp, Line height 24sp
    val EntryTitle = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // Body: Hind Siliguri Regular 16sp, Line height 25sp
    val Body = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp
    )

    // Secondary body: Hind Siliguri Regular 14sp, Line height 22sp
    val SecondaryBody = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    )

    // Metadata: Hind Siliguri Regular 13sp, Line height 19sp
    val Metadata = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp
    )

    // Primary button: Hind Siliguri Medium 16sp, Line height 22sp
    val PrimaryButton = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    )

    // Bottom navigation: Hind Siliguri Medium 13sp, Line height 18sp
    val BottomNav = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    // Form input: Hind Siliguri Regular 17sp, Line height 25sp
    val FormInput = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp
    )

    // Form label: Hind Siliguri Medium 14sp, Line height 20sp
    val FormLabel = TextStyle(
        fontFamily = HindSiliguriFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    // Empty-state title: Noto Serif Bengali Medium 20sp, Line height 30sp
    val EmptyStateTitle = TextStyle(
        fontFamily = NotoSerifBengaliFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    )

    // Insight title: Noto Serif Bengali Medium 18sp, Line height 27sp
    val InsightTitle = TextStyle(
        fontFamily = NotoSerifBengaliFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.sp
    )
}

val AppTypography = Typography(
    displayLarge = KoriTypographyTokens.PrimaryAmount,
    headlineMedium = KoriTypographyTokens.Wordmark,
    titleLarge = KoriTypographyTokens.SectionTitle,
    titleMedium = KoriTypographyTokens.EntryTitle,
    bodyLarge = KoriTypographyTokens.Body,
    bodyMedium = KoriTypographyTokens.SecondaryBody,
    labelLarge = KoriTypographyTokens.PrimaryButton,
    labelMedium = KoriTypographyTokens.Metadata,
    labelSmall = KoriTypographyTokens.BottomNav
)
