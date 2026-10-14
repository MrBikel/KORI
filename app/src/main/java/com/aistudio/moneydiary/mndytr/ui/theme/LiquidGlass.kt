package com.aistudio.moneydiary.mndytr.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable Liquid Glass UI styling tokens and container for KORI.
 * Combines subtle translucency, layered depth, and soft light reflection.
 */
object LiquidGlassTokens {
    val DefaultShape: Shape = RoundedCornerShape(20.dp)
    val SmallShape: Shape = RoundedCornerShape(14.dp)
    val LargeShape: Shape = RoundedCornerShape(26.dp)

    // Light Theme Glass Brushes
    val LightGlassBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xE6FFFFFF),
            Color(0xD9F8FAFC)
        )
    )
    val LightGlassBorder = Brush.verticalGradient(
        colors = listOf(
            Color(0x66FFFFFF),
            Color(0x2638BDF8)
        )
    )

    // Dark Theme Glass Brushes
    val DarkGlassBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xCC111C38),
            Color(0xB30B132B)
        )
    )
    val DarkGlassBorder = Brush.verticalGradient(
        colors = listOf(
            Color(0x4038BDF8),
            Color(0x101E293B)
        )
    )
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassTokens.DefaultShape,
    elevation: Dp = 3.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val bgBrush = if (isDark) LiquidGlassTokens.DarkGlassBackground else LiquidGlassTokens.LightGlassBackground
    val borderBrush = if (isDark) LiquidGlassTokens.DarkGlassBorder else LiquidGlassTokens.LightGlassBorder

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (isDark) Color(0x33000000) else Color(0x140F294D),
                spotColor = if (isDark) Color(0x2238BDF8) else Color(0x0F0F294D)
            )
            .clip(shape)
            .background(brush = bgBrush)
            .border(
                border = BorderStroke(1.dp, borderBrush),
                shape = shape
            ),
        content = content
    )
}
