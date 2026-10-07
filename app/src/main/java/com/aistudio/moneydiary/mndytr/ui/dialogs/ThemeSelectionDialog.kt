package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aistudio.moneydiary.mndytr.ui.theme.*

@Composable
fun ThemeSelectionDialog(
    currentColorIdentity: KoriColorIdentity,
    currentThemeMode: String,
    onApply: (KoriColorIdentity, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedIdentity by remember { mutableStateOf(currentColorIdentity) }
    var selectedMode by remember { mutableStateOf(currentThemeMode) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("theme_selection_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = "থিম ও রঙ নির্বাচন",
                    style = KoriTypographyTokens.ScreenTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "কড়ির তিনটি মার্জিত রঙের প্যালেট থেকে আপনার পছন্দের পরিচয় বেছে নিন:",
                    style = KoriTypographyTokens.SecondaryBody,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 3 Theme Choices with Live Mini-Previews
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    KoriColorIdentity.values().forEach { identity ->
                        ThemeOptionCard(
                            identity = identity,
                            isSelected = (selectedIdentity == identity),
                            themeMode = selectedMode,
                            onSelect = { selectedIdentity = identity }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                // Light / Dark / System Mode Picker
                Text(
                    text = "ডিসপ্লে মোড",
                    style = KoriTypographyTokens.SectionTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf("SYSTEM" to "সিস্টেম", "LIGHT" to "লাইট", "DARK" to "ডার্ক")
                    modes.forEach { (modeKey, modeLabel) ->
                        val isChosen = (selectedMode == modeKey)
                        FilterChip(
                            selected = isChosen,
                            onClick = { selectedMode = modeKey },
                            label = { Text(modeLabel, style = KoriTypographyTokens.SecondaryBody) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", style = KoriTypographyTokens.PrimaryButton)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onApply(selectedIdentity, selectedMode)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("apply_theme_btn")
                    ) {
                        Text("প্রয়োগ করুন", style = KoriTypographyTokens.PrimaryButton, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    identity: KoriColorIdentity,
    isSelected: Boolean,
    themeMode: String,
    onSelect: () -> Unit
) {
    val isDark = themeMode == "DARK"
    val colors = when (identity) {
        KoriColorIdentity.INK_AND_PAPER -> if (isDark) InkAndPaperDark else InkAndPaperLight
        KoriColorIdentity.MIDNIGHT_INDIGO -> if (isDark) MidnightIndigoDark else MidnightIndigoLight
        KoriColorIdentity.BURGUNDY_JOURNAL -> if (isDark) BurgundyJournalDark else BurgundyJournalLight
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else colors.border,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .testTag("theme_card_${identity.name}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = identity.titleBn,
                        style = KoriTypographyTokens.EntryTitle,
                        color = colors.textMain,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = identity.descriptionBn,
                        style = KoriTypographyTokens.Metadata,
                        color = colors.textMuted
                    )
                }
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Compact Live Mini Preview containing: Wordmark, income, expense, net, one row, one button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.canvas)
                    .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "কড়ি",
                            style = KoriTypographyTokens.Wordmark.copy(fontSize = 15.sp, lineHeight = 18.sp),
                            color = colors.textMain
                        )
                        Text(
                            text = "আজকের হিসাব",
                            style = KoriTypographyTokens.Metadata.copy(fontSize = 11.sp),
                            color = colors.textMuted
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("আয়: +৳৫০০", style = KoriTypographyTokens.Metadata.copy(fontSize = 11.sp, color = colors.income, fontWeight = FontWeight.Medium))
                        Text("ব্যয়: −৳৪৬০", style = KoriTypographyTokens.Metadata.copy(fontSize = 11.sp, color = colors.expense, fontWeight = FontWeight.Medium))
                        Text("উদ্বৃত্ত: +৳৪০", style = KoriTypographyTokens.Metadata.copy(fontSize = 11.sp, color = colors.textMain, fontWeight = FontWeight.SemiBold))
                    }

                    // Mini diary row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.surface)
                            .border(0.5.dp, colors.border, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("জ্বরের ওষুধ • বেতন থেকে", style = KoriTypographyTokens.Metadata.copy(fontSize = 10.sp, color = colors.textMain))
                            Text("−৳৪৫০", style = KoriTypographyTokens.Metadata.copy(fontSize = 10.sp, color = colors.expense, fontWeight = FontWeight.Medium))
                        }
                    }

                    // Mini button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.primary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("হিসাব লিখুন", style = KoriTypographyTokens.Metadata.copy(fontSize = 10.sp, color = if (isDark) colors.canvas else colors.surface, fontWeight = FontWeight.Medium))
                    }
                }
            }
        }
    }
}
