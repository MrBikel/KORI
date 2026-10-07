package com.aistudio.moneydiary.mndytr.ui.dialogs

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.aistudio.moneydiary.mndytr.BuildConfig
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.domain.service.DiagnosticService
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnosticsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val reports = remember { DiagnosticService.getCrashReports(context) }
    val latestReport = reports.firstOrNull()
    val clipboardManager = LocalClipboardManager.current
    
    var dbStats by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    
    LaunchedEffect(Unit) {
        val db = AppDatabase.getInstance(context)
        val stats = mutableMapOf<String, String>()
        try {
            stats["DB File Exists"] = "Yes"
            stats["DB Version"] = db.openHelper.readableDatabase.version.toString()
            
            val accountsCount = db.accountDao().getAccountCount()
            stats["Account Count"] = accountsCount.toString()
            
            val entriesCount = db.ledgerEventDao().getEventCount()
            stats["Entry Count"] = entriesCount.toString()
            
            val categoriesCount = db.categoryDao().getCategoryCount()
            stats["Category Count"] = categoriesCount.toString()
            
        } catch (e: Exception) {
            stats["Error"] = e.message ?: "Unknown error"
        }
        dbStats = stats
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ডায়াগনস্টিকস (শুধু পরীক্ষামূলক)") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DiagnosticSection("App Info")
                DiagnosticRow("Package", BuildConfig.APPLICATION_ID)
                DiagnosticRow("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                DiagnosticRow("Variant", if (BuildConfig.IS_DIAGNOSTIC) "Diagnostic" else "Normal")
                
                Spacer(modifier = Modifier.height(16.dp))
                DiagnosticSection("Device Info")
                DiagnosticRow("Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                DiagnosticRow("Device", "${Build.MANUFACTURER} ${Build.MODEL}")
                
                Spacer(modifier = Modifier.height(16.dp))
                DiagnosticSection("Database Info")
                dbStats.forEach { (k, v) ->
                    DiagnosticRow(k, v)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                DiagnosticSection("Crash Reports")
                DiagnosticRow("Count", reports.size.toString())
                if (latestReport != null) {
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(latestReport.lastModified()))
                    DiagnosticRow("Last Crash", date)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        },
        dismissButton = {
            if (latestReport != null) {
                Column {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", latestReport)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "প্রতিবেদন শেয়ার করুন"))
                        }
                    ) {
                        Text("শেয়ার করুন")
                    }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            clipboardManager.setText(AnnotatedString(latestReport.readText()))
                        }
                    ) {
                        Text("কপি করুন")
                    }
                }
            }
        }
    )
}

@Composable
fun DiagnosticSection(title: String) {
    Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun DiagnosticRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = "$label: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
    }
}
