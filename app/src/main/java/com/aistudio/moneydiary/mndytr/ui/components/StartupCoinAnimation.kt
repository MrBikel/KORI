package com.aistudio.moneydiary.mndytr.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.moneydiary.mndytr.ui.theme.KoriTypographyTokens
import kotlin.math.abs
import kotlin.math.cos

@Composable
fun StartupCoinAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "coin_transition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "coin_rotation"
    )

    val bounceTransition = rememberInfiniteTransition(label = "coin_bounce")
    val bounceY by bounceTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coin_bounce_y"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Elegant Metallic Coin with Spin & Elevation
            val radians = Math.toRadians(rotation.toDouble())
            val scaleX = abs(cos(radians)).toFloat().coerceAtLeast(0.1f)

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        translationY = bounceY
                        this.scaleX = scaleX
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2.2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Coin outer gradient
                    val coinBrush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFDF7A),
                            Color(0xFFD4AF37),
                            Color(0xFF996515)
                        ),
                        center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                        radius = radius * 1.4f
                    )

                    drawCircle(
                        brush = coinBrush,
                        radius = radius,
                        center = center
                    )

                    // Outer Rim
                    drawCircle(
                        color = Color(0xFFFFF6B8),
                        radius = radius - 3.dp.toPx(),
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Inner Rim
                    drawCircle(
                        color = Color(0x66996515),
                        radius = radius - 7.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                Text(
                    text = "ক",
                    style = KoriTypographyTokens.Wordmark.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6B4804)
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Brand Typography
            Text(
                text = "কড়ি",
                style = KoriTypographyTokens.Wordmark.copy(
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "প্রতিটি কড়ির নির্ভুল হিসাব",
                style = KoriTypographyTokens.SecondaryBody.copy(
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Developer Credit at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Developed by Mehedi Hasan",
                style = KoriTypographyTokens.SecondaryBody.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}
