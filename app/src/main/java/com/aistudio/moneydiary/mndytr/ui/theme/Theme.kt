package com.aistudio.moneydiary.mndytr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun MyApplicationTheme(
    colorIdentity: KoriColorIdentity = KoriColorIdentity.INK_AND_PAPER,
    themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    darkTheme: Boolean = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    },
    content: @Composable () -> Unit,
) {
    val koriColors: KoriThemeColors = when (colorIdentity) {
        KoriColorIdentity.INK_AND_PAPER -> if (darkTheme) InkAndPaperDark else InkAndPaperLight
        KoriColorIdentity.MIDNIGHT_INDIGO -> if (darkTheme) MidnightIndigoDark else MidnightIndigoLight
        KoriColorIdentity.BURGUNDY_JOURNAL -> if (darkTheme) BurgundyJournalDark else BurgundyJournalLight
    }

    val materialColorScheme = if (darkTheme) {
        darkColorScheme(
            primary = koriColors.primary,
            onPrimary = koriColors.canvas,
            primaryContainer = koriColors.primarySoft,
            onPrimaryContainer = koriColors.textMain,
            secondary = koriColors.insight,
            onSecondary = koriColors.canvas,
            secondaryContainer = koriColors.raisedSurface,
            onSecondaryContainer = koriColors.textMain,
            tertiary = koriColors.warning,
            background = koriColors.canvas,
            surface = koriColors.surface,
            surfaceVariant = koriColors.raisedSurface,
            onBackground = koriColors.textMain,
            onSurface = koriColors.textMain,
            onSurfaceVariant = koriColors.textMuted,
            outline = koriColors.border,
            error = koriColors.error
        )
    } else {
        lightColorScheme(
            primary = koriColors.primary,
            onPrimary = koriColors.surface,
            primaryContainer = koriColors.primarySoft,
            onPrimaryContainer = koriColors.textMain,
            secondary = koriColors.insight,
            onSecondary = koriColors.surface,
            secondaryContainer = koriColors.primarySoft,
            onSecondaryContainer = koriColors.textMain,
            tertiary = koriColors.warning,
            background = koriColors.canvas,
            surface = koriColors.surface,
            surfaceVariant = koriColors.primarySoft,
            onBackground = koriColors.textMain,
            onSurface = koriColors.textMain,
            onSurfaceVariant = koriColors.textMuted,
            outline = koriColors.border,
            error = koriColors.error
        )
    }

    CompositionLocalProvider(LocalKoriColors provides koriColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
