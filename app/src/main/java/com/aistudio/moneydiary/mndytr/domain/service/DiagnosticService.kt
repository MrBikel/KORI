package com.aistudio.moneydiary.mndytr.domain.service

import android.content.Context
import android.os.Build
import com.aistudio.moneydiary.mndytr.BuildConfig
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class Checkpoint(
    val name: String,
    val timestamp: Long,
    val formType: String? = null,
    val threadName: String = Thread.currentThread().name
)

object DiagnosticService {
    private val checkpoints = mutableListOf<Checkpoint>()
    private const val MAX_CHECKPOINTS = 30
    private const val MAX_CRASH_REPORTS = 5
    private val isHandlingCrash = java.util.concurrent.atomic.AtomicBoolean(false)

    var isDiagnosticOverrideForTesting: Boolean? = null
    val isDiagnosticActive: Boolean
        get() = isDiagnosticOverrideForTesting ?: BuildConfig.IS_DIAGNOSTIC

    fun init(context: Context) {
        // Ensure crash reports directory exists
        getCrashReportsDir(context).mkdirs()
        cleanOldReports(context)
    }

    fun addCheckpoint(name: String, formType: String? = null) {
        if (!isDiagnosticActive) return
        synchronized(checkpoints) {
            checkpoints.add(Checkpoint(name, System.currentTimeMillis(), formType))
            if (checkpoints.size > MAX_CHECKPOINTS) {
                checkpoints.removeAt(0)
            }
        }
    }

    fun recordCrash(context: Context, thread: Thread, throwable: Throwable) {
        if (!isDiagnosticActive) return
        if (!isHandlingCrash.compareAndSet(false, true)) {
            // Prevent recursive crash-report generation
            return
        }

        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val iso8601 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val dir = getCrashReportsDir(context)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val reportFile = File(dir, "kori_crash_$timestamp.txt")
            
            reportFile.bufferedWriter().use { writer ->
                writer.write("KORI CRASH REPORT\n")
                writer.write("=================\n")
                writer.write("Timestamp: $iso8601\n")
                writer.write("App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n")
                writer.write("Package: ${context.packageName}\n")
                writer.write("Diagnostic Build: $isDiagnosticActive\n")
                writer.write("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
                writer.write("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
                writer.write("Crashing Thread: ${thread.name}\n")
                writer.write("\n")
                
                writer.write("EXCEPTION\n")
                writer.write("---------\n")
                val sw = StringWriter()
                PrintWriter(sw).use { pw ->
                    throwable.printStackTrace(pw)
                }
                writer.write(sw.toString())
                writer.write("\n")

                writer.write("SUBMISSION CHECKPOINTS\n")
                writer.write("----------------------\n")
                synchronized(checkpoints) {
                    checkpoints.forEach { cp ->
                        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(cp.timestamp))
                        writer.write("[$time] ${cp.name}${if (cp.formType != null) " (Form: ${cp.formType})" else ""} [Thread: ${cp.threadName}]\n")
                    }
                }
                writer.write("\n")
                
                writer.write("DATABASE SUMMARY\n")
                writer.write("----------------\n")
                // Technical metadata only as requested
                val dbFile = context.getDatabasePath("money_diary.db")
                writer.write("DB File Exists: ${dbFile.exists()}\n")
                if (dbFile.exists()) {
                    writer.write("DB File Size: ${dbFile.length()} bytes\n")
                }
                writer.flush()
            }
            cleanOldReports(context)
        } catch (e: Exception) {
            // Ignore failures during reporting
        } finally {
            isHandlingCrash.set(false)
        }
    }

    fun getCrashReports(context: Context): List<File> {
        return getCrashReportsDir(context).listFiles()?.filter { it.extension == "txt" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun deleteReport(file: File) {
        if (file.exists()) file.delete()
    }

    private fun getCrashReportsDir(context: Context): File {
        return File(context.filesDir, "crash_reports")
    }

    private fun cleanOldReports(context: Context) {
        val reports = getCrashReports(context)
        if (!isDiagnosticActive) {
            reports.forEach { it.delete() }
            return
        }
        if (reports.size > MAX_CRASH_REPORTS) {
            reports.subList(MAX_CRASH_REPORTS, reports.size).forEach { it.delete() }
        }
    }
}
