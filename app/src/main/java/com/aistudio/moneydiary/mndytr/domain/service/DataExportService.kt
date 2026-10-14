package com.aistudio.moneydiary.mndytr.domain.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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

    fun mapToTaxonomy(entry: DiaryEntry): String {
        return when (entry.eventCode) {
            "TX_01_INCOME" -> "INCOME"
            "TX_02_EXPENSE" -> "EXPENSE"
            "TX_07_BORROWED_MONEY" -> "DEBT_BORROWED"
            "TX_09_DEBT_REPAID" -> "DEBT_REPAYMENT"
            "TX_10_LENT_MONEY" -> "LOAN_RECEIVABLE_CREATED"
            "TX_11_LOAN_COLLECTED" -> "LOAN_RECEIVABLE_REPAYMENT"
            "TX_05_RECEIVABLE_CREATED" -> "EARNED_RECEIVABLE_CREATED"
            "TX_06_RECEIVABLE_COLLECTED" -> "EARNED_RECEIVABLE_PAYMENT"
            "TX_16_BALANCE_ADJUSTMENT" -> "ADJUSTMENT"
            "TX_17_REVERSAL" -> "REVERSAL"
            else -> if (entry.type == DiaryEntryType.INCOME) "INCOME" else "EXPENSE"
        }
    }

    fun generateCsv(
        entries: List<DiaryEntry>,
        includePrivateNotes: Boolean = false
    ): String {
        val sb = StringBuilder()
        // CSV Header with full taxonomy
        sb.append("Taxonomy,Type,Amount_BDT,Category,Source_Party,Date_Time,Description,Necessity")
        if (includePrivateNotes) {
            sb.append(",Private_Notes")
        }
        sb.append("\n")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (entry in entries) {
            val taxonomy = mapToTaxonomy(entry)
            val typeStr = if (entry.type == DiaryEntryType.INCOME) "Income" else "Expense"
            val amountTaka = entry.amountPaisa / 100.0
            val dateTime = sdf.format(Date(entry.occurrenceDateEpochMs))
            val category = escapeCsv(entry.categoryNameBn)
            val source = escapeCsv(entry.sourceDescription ?: "")
            val desc = escapeCsv(entry.description)
            val necessity = escapeCsv(entry.necessity.banglaLabel)

            sb.append("$taxonomy,$typeStr,$amountTaka,$category,$source,$dateTime,$desc,$necessity")
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
        root.put("version", "1.0")
        root.put("exportedAtEpochMs", System.currentTimeMillis())
        root.put("entryCount", entries.size)

        val array = JSONArray()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        for (entry in entries) {
            val obj = JSONObject()
            obj.put("id", entry.id)
            obj.put("taxonomy", mapToTaxonomy(entry))
            obj.put("type", entry.type.name)
            obj.put("eventCode", entry.eventCode)
            obj.put("amountPaisa", entry.amountPaisa)
            obj.put("amountTaka", entry.amountPaisa / 100.0)
            obj.put("category", entry.categoryNameBn)
            obj.put("sourceParty", entry.sourceDescription ?: "")
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

    fun generatePrintableHtmlStatement(
        entries: List<DiaryEntry>,
        title: String = "কড়ি - আর্থিক বিবরণী",
        includePrivateNotes: Boolean = false
    ): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        val dateOnlySdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val nowStr = sdf.format(Date())

        val totalIncomePaisa = entries.filter { it.type == DiaryEntryType.INCOME }.sumOf { it.amountPaisa }
        val totalExpensePaisa = entries.filter { it.type == DiaryEntryType.EXPENSE }.sumOf { it.amountPaisa }
        val netPaisa = totalIncomePaisa - totalExpensePaisa

        val sb = StringBuilder()
        sb.append("""
<!DOCTYPE html>
<html lang="bn">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>$title</title>
<style>
  body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Hind Siliguri", sans-serif; background-color: #FAF8F5; color: #1C1B1A; margin: 0; padding: 24px; line-height: 1.5; }
  .container { max-width: 800px; margin: 0 auto; background: #FFFFFF; border-radius: 12px; padding: 32px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); border: 1px solid #EAE6DF; }
  .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #2D4A3E; padding-bottom: 16px; margin-bottom: 24px; }
  .brand { font-size: 28px; font-weight: bold; color: #2D4A3E; }
  .meta { text-align: right; font-size: 13px; color: #666; }
  .summary-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 28px; }
  .card { padding: 16px; border-radius: 8px; border: 1px solid #EAE6DF; background: #FAF9F6; }
  .card-title { font-size: 13px; color: #555; margin-bottom: 4px; }
  .card-value { font-size: 20px; font-weight: bold; }
  .income-val { color: #2D4A3E; }
  .expense-val { color: #A8382B; }
  .net-val { color: ${if (netPaisa >= 0) "#2D4A3E" else "#A8382B"}; }
  table { width: 100%; border-collapse: collapse; margin-top: 16px; font-size: 13px; }
  th { background: #F2EFE9; text-align: left; padding: 10px 12px; border-bottom: 2px solid #DDD; font-weight: 600; color: #333; }
  td { padding: 10px 12px; border-bottom: 1px solid #EFECE6; }
  tr:nth-child(even) { background-color: #FCFBF9; }
  .badge { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 500; }
  .badge-income { background: #E2ECE5; color: #2D4A3E; }
  .badge-expense { background: #F7E7E5; color: #A8382B; }
  .text-right { text-align: right; }
  @media print {
    body { background: #FFF; padding: 0; }
    .container { box-shadow: none; border: none; padding: 0; }
  }
</style>
</head>
<body>
<div class="container">
  <div class="header">
    <div>
      <div class="brand">কড়ি (KORI)</div>
      <div style="font-size: 14px; color: #666; margin-top: 4px;">ব্যক্তিগত আর্থিক বিবরণী</div>
    </div>
    <div class="meta">
      <div>তৈরির সময়: $nowStr</div>
      <div>মোট লেনদেন: ${entries.size} টি</div>
    </div>
  </div>

  <div class="summary-cards">
    <div class="card">
      <div class="card-title">মোট আয়</div>
      <div class="card-value income-val">৳${String.format(Locale.US, "%,.2f", totalIncomePaisa / 100.0)}</div>
    </div>
    <div class="card">
      <div class="card-title">মোট ব্যয়</div>
      <div class="card-value expense-val">৳${String.format(Locale.US, "%,.2f", totalExpensePaisa / 100.0)}</div>
    </div>
    <div class="card">
      <div class="card-title">নীট স্থিতি</div>
      <div class="card-value net-val">৳${String.format(Locale.US, "%,.2f", netPaisa / 100.0)}</div>
    </div>
  </div>

  <table>
    <thead>
      <tr>
        <th>তারিখ</th>
        <th>বিবরণ</th>
        <th>খাত</th>
        <th>উৎস / মাধ্যম</th>
        <th>ধরণ</th>
        <th class="text-right">পরিমাণ (৳)</th>
      </tr>
    </thead>
    <tbody>
        """.trimIndent())

        for (entry in entries) {
            val dateStr = dateOnlySdf.format(Date(entry.occurrenceDateEpochMs))
            val isIncome = entry.type == DiaryEntryType.INCOME
            val typeBadge = if (isIncome) "<span class=\"badge badge-income\">আয়</span>" else "<span class=\"badge badge-expense\">ব্যয়</span>"
            val amtClass = if (isIncome) "income-val" else "expense-val"
            val amtStr = String.format(Locale.US, "%,.2f", entry.amountPaisa / 100.0)

            sb.append("""
      <tr>
        <td>$dateStr</td>
        <td><strong>${entry.description}</strong>${if (includePrivateNotes && !entry.notes.isNullOrBlank()) "<br><small style=\"color:#777;\">${entry.notes}</small>" else ""}</td>
        <td>${entry.categoryNameBn}</td>
        <td>${entry.sourceDescription ?: "—"}</td>
        <td>$typeBadge</td>
        <td class="text-right $amtClass">৳$amtStr</td>
      </tr>
            """.trimIndent())
        }

        sb.append("""
    </tbody>
  </table>
  <div style="margin-top: 32px; font-size: 11px; color: #888; text-align: center; border-top: 1px solid #EEE; padding-top: 12px;">
    কড়ি (KORI) অ্যাপ দ্বারা স্বয়ংক্রিয়ভাবে প্রস্তুতকৃত। প্রতিটি কড়ির নির্ভুল হিসাব।
  </div>
</div>
</body>
</html>
        """.trimIndent())

        return sb.toString()
    }

    fun exportAndShare(
        context: Context,
        entries: List<DiaryEntry>,
        format: String = "CSV", // "CSV", "JSON", or "HTML"
        includePrivateNotes: Boolean = false
    ): Intent? {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val isJson = format.equals("JSON", ignoreCase = true)
            val isHtml = format.equals("HTML", ignoreCase = true)
            val ext = if (isJson) "json" else if (isHtml) "html" else "csv"
            val mimeType = if (isJson) "application/json" else if (isHtml) "text/html" else "text/csv"
            val file = File(exportDir, "kori_statement_$timestamp.$ext")

            val content = if (isJson) {
                generateJson(entries, includePrivateNotes)
            } else if (isHtml) {
                generatePrintableHtmlStatement(entries, includePrivateNotes = includePrivateNotes)
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
                putExtra(Intent.EXTRA_SUBJECT, "কড়ি আর্থিক স্টেটমেন্ট ($timestamp)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            return Intent.createChooser(shareIntent, "কড়ি আর্থিক স্টেটমেন্ট শেয়ার করুন")
        } catch (e: Exception) {
            Log.e("DataExportService", "Export failed", e)
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
