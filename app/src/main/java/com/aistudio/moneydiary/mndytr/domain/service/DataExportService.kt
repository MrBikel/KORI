package com.aistudio.moneydiary.mndytr.domain.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntry
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntryType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DataExportService {

    fun generateCsv(
        entries: List<DiaryEntry>,
        includePrivateNotes: Boolean = false
    ): String {
        val sb = StringBuilder()
        // CSV Header
        sb.append("Type,Amount_BDT,Category,Source,Date_Time,Description,Necessity")
        if (includePrivateNotes) {
            sb.append(",Private_Notes")
        }
        sb.append("\n")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (entry in entries) {
            val typeStr = if (entry.type == DiaryEntryType.INCOME) "Income" else "Expense"
            val amountTaka = entry.amountPaisa / 100.0
            val dateTime = sdf.format(Date(entry.occurrenceDateEpochMs))
            val category = escapeCsv(entry.categoryNameBn)
            val source = escapeCsv(entry.sourceDescription ?: "")
            val desc = escapeCsv(entry.description)
            val necessity = escapeCsv(entry.necessity.banglaLabel)

            sb.append("$typeStr,$amountTaka,$category,$source,$dateTime,$desc,$necessity")
            if (includePrivateNotes) {
                sb.append(",${escapeCsv(entry.notes ?: "")}")
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    fun generateJson(
        entries: List<DiaryEntry>,
        includePrivateNotes: Boolean = false
    ): String {
        val root = JSONObject()
        root.put("app", "কড়ি (Kori)")
        root.put("version", "2.0")
        root.put("exportedAtEpochMs", System.currentTimeMillis())
        root.put("entryCount", entries.size)

        val array = JSONArray()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        for (entry in entries) {
            val obj = JSONObject()
            obj.put("id", entry.id)
            obj.put("type", entry.type.name)
            obj.put("amountPaisa", entry.amountPaisa)
            obj.put("amountTaka", entry.amountPaisa / 100.0)
            obj.put("category", entry.categoryNameBn)
            obj.put("source", entry.sourceDescription ?: "")
            obj.put("dateTime", sdf.format(Date(entry.occurrenceDateEpochMs)))
            obj.put("timestampEpochMs", entry.occurrenceDateEpochMs)
            obj.put("description", entry.description)
            obj.put("necessity", entry.necessity.banglaLabel)
            if (includePrivateNotes && !entry.notes.isNullOrBlank()) {
                obj.put("notes", entry.notes)
            }
            array.put(obj)
        }
        root.put("entries", array)
        return root.toString(2)
    }

    fun exportAndShare(
        context: Context,
        entries: List<DiaryEntry>,
        format: String = "CSV", // "CSV" or "JSON"
        includePrivateNotes: Boolean = false
    ): Intent? {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val isJson = format.equals("JSON", ignoreCase = true)
            val ext = if (isJson) "json" else "csv"
            val mimeType = if (isJson) "application/json" else "text/csv"
            val file = File(exportDir, "kori_export_$timestamp.$ext")

            val content = if (isJson) {
                generateJson(entries, includePrivateNotes)
            } else {
                generateCsv(entries, includePrivateNotes)
            }

            FileOutputStream(file).use {
                it.write(content.toByteArray(Charsets.UTF_8))
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "কড়ি ডেটা এক্সপোর্ট ($timestamp)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            return Intent.createChooser(shareIntent, "কড়ি ডেটা এক্সপোর্ট শেয়ার করুন")
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
