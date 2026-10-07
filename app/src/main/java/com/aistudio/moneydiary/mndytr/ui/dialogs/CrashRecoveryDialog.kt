package com.aistudio.moneydiary.mndytr.ui.dialogs

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.aistudio.moneydiary.mndytr.BuildConfig
import com.aistudio.moneydiary.mndytr.domain.service.DiagnosticService
import java.io.File

@Composable
fun CrashRecoveryDialog(
    onDismiss: () -> Unit,
    onDelete: (File) -> Unit
) {
    val context = LocalContext.current
    val reports = remember { DiagnosticService.getCrashReports(context) }
    val latestReport = reports.firstOrNull()
    val clipboardManager = LocalClipboardManager.current

    if (latestReport == null) {
        onDismiss()
        return
    }

    val reportText = remember(latestReport) { latestReport.readText() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("কড়ি অপ্রত্যাশিতভাবে বন্ধ হয়েছিল") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "সমস্যাটি শনাক্ত করার জন্য একটি প্রযুক্তিগত প্রতিবেদন সংরক্ষিত হয়েছে। প্রতিবেদনে আপনার পাসওয়ার্ড বা সম্পূর্ণ ডায়েরি সংরক্ষণ করা হয়নি।",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "সংক্ষিপ্ত বিবরণ:",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = reportText.lines().take(15).joinToString("\n"),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", latestReport)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "প্রতিবেদন শেয়ার করুন"))
            }) {
                Text("শেয়ার করুন")
            }
        },
        dismissButton = {
            Column {
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(reportText))
                }) {
                    Text("কপি করুন")
                }
                TextButton(onClick = onDismiss) {
                    Text("পরে")
                }
                TextButton(
                    onClick = { onDelete(latestReport) },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("মুছে ফেলুন")
                }
            }
        }
    )
}
