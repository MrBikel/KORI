package com.aistudio.moneydiary.mndytr.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aistudio.moneydiary.mndytr.ui.theme.KoriTypographyTokens

@Composable
fun OpenSourceLicensesDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("open_source_licenses_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "ওপেন-সোর্স লাইসেন্স",
                    style = KoriTypographyTokens.ScreenTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "কড়িতে ব্যবহৃত টাইপোগ্রাফি ও ফন্ট লাইসেন্সসমূহ:",
                    style = KoriTypographyTokens.SecondaryBody,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("১. Hind Siliguri (হিন্দ শিলিগুড়ি)", style = KoriTypographyTokens.EntryTitle, color = MaterialTheme.colorScheme.onSurface)
                        Text("Copyright (c) 2015 Indian Type Foundry (info@indiantypefoundry.com)", style = KoriTypographyTokens.Metadata, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Licensed under the SIL Open Font License, Version 1.1 (LICENSES/OFL-HindSiliguri.txt)", style = KoriTypographyTokens.Metadata.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp), color = MaterialTheme.colorScheme.primary)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("২. Noto Serif Bengali (নোটো সেরিফ বাংলা)", style = KoriTypographyTokens.EntryTitle, color = MaterialTheme.colorScheme.onSurface)
                        Text("Copyright 2022 The Noto Project Authors (https://github.com/notofonts/bengali)", style = KoriTypographyTokens.Metadata, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Licensed under the SIL Open Font License, Version 1.1 (LICENSES/OFL-NotoSerifBengali.txt)", style = KoriTypographyTokens.Metadata.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp), color = MaterialTheme.colorScheme.primary)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss) {
                        Text("বন্ধ করুন", style = KoriTypographyTokens.PrimaryButton)
                    }
                }
            }
        }
    }
}
