package com.aistudio.moneydiary.mndytr.domain.service

import android.content.Context
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.data.local.entity.AuditLogEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.TransactionEntity
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class CsvParsedRow(
    val rowNumber: Int,
    val taxonomy: String,
    val type: String, // "INCOME" or "EXPENSE"
    val amountPaisa: Long,
    val categoryName: String,
    val sourceParty: String?,
    val dateTimeEpochMs: Long,
    val description: String,
    val necessity: String,
    val notes: String?
)

data class InvalidRowReport(
    val rowNumber: Int,
    val rawLine: String,
    val reasonBn: String
)

data class CsvImportPreview(
    val totalRows: Int,
    val validRows: List<CsvParsedRow>,
    val invalidRows: List<InvalidRowReport>,
    val duplicateRows: List<CsvParsedRow>,
    val newRows: List<CsvParsedRow>,
    val totalIncomePaisa: Long,
    val totalExpensePaisa: Long
)

sealed class CsvImportResult {
    data class Success(val importedCount: Int, val skippedDuplicates: Int, val invalidRowCount: Int) : CsvImportResult()
    data class Failure(val messageBn: String, val technicalDetails: String? = null) : CsvImportResult()
}

object CsvImportService {

    // Converts Bengali digits (০-৯) to English digits (0-9)
    fun convertBengaliDigitsToEnglish(input: String): String {
        val bengaliDigits = "০১২৩৪৫৬৭৮৯"
        val englishDigits = "0123456789"
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val idx = bengaliDigits.indexOf(ch)
            if (idx != -1) {
                sb.append(englishDigits[idx])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun parseAmountToPaisa(rawAmount: String): Long? {
        val normalized = convertBengaliDigitsToEnglish(rawAmount)
            .replace("৳", "")
            .replace(",", "")
            .replace("BDT", "", ignoreCase = true)
            .trim()
        if (normalized.isEmpty()) return null
        return try {
            val d = normalized.toDouble()
            if (d < 0) null else (Math.round(d * 100))
        } catch (e: Exception) {
            null
        }
    }

    private val supportedDateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US),
        SimpleDateFormat("dd/MM/yyyy", Locale.US),
        SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US),
        SimpleDateFormat("dd-MM-yyyy", Locale.US)
    )

    private fun parseDateToEpochMs(rawDate: String): Long? {
        val trimmed = convertBengaliDigitsToEnglish(rawDate.trim())
        if (trimmed.isEmpty()) return null

        // Try direct timestamp
        trimmed.toLongOrNull()?.let {
            if (it > 946684800000L) return it // after year 2000
        }

        for (format in supportedDateFormats) {
            try {
                format.isLenient = false
                val date = format.parse(trimmed)
                if (date != null) return date.time
            } catch (_: Exception) {
            }
        }
        return null
    }

    // RFC 4180 compliant CSV line tokenizer
    fun parseCsvLines(csvText: String): List<List<String>> {
        val sanitized = if (csvText.startsWith("\uFEFF")) csvText.substring(1) else csvText
        val rows = mutableListOf<List<String>>()
        var currentToken = StringBuilder()
        var currentRow = mutableListOf<String>()
        var inQuotes = false
        var i = 0

        while (i < sanitized.length) {
            val ch = sanitized[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < sanitized.length && sanitized[i + 1] == '"') {
                        currentToken.append('"')
                        i++ // Skip escaped quote
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == ',' && !inQuotes -> {
                    currentRow.add(currentToken.toString().trim())
                    currentToken = StringBuilder()
                }
                (ch == '\n' || ch == '\r') && !inQuotes -> {
                    if (ch == '\r' && i + 1 < sanitized.length && sanitized[i + 1] == '\n') {
                        i++ // CRLF
                    }
                    currentRow.add(currentToken.toString().trim())
                    currentToken = StringBuilder()
                    if (currentRow.any { it.isNotEmpty() }) {
                        rows.add(currentRow)
                    }
                    currentRow = mutableListOf()
                }
                else -> {
                    currentToken.append(ch)
                }
            }
            i++
        }

        if (currentToken.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentToken.toString().trim())
            if (currentRow.any { it.isNotEmpty() }) {
                rows.add(currentRow)
            }
        }

        return rows
    }

    /**
     * Previews a CSV text with Bengali text, Bengali numerals, and duplicate detection.
     */
    fun previewCsv(db: AppDatabase, csvText: String): CsvImportPreview {
        val rawRows = parseCsvLines(csvText)
        if (rawRows.isEmpty()) {
            return CsvImportPreview(0, emptyList(), emptyList(), emptyList(), emptyList(), 0L, 0L)
        }

        // Existing transactions for duplicate detection
        val existingTxs = db.ledgerEventDao().getAllNonDeleted()
        val existingKeys = existingTxs.map { "${it.eventTimestampEpochMs}_${it.amountPaisa}_${it.description.trim()}" }.toSet()

        val validRows = mutableListOf<CsvParsedRow>()
        val invalidRows = mutableListOf<InvalidRowReport>()
        val duplicateRows = mutableListOf<CsvParsedRow>()
        val newRows = mutableListOf<CsvParsedRow>()

        var totalIncome = 0L
        var totalExpense = 0L

        // Header detection
        var startIndex = 0
        val firstRow = rawRows[0].map { it.lowercase() }
        var colTaxonomy = -1
        var colType = -1
        var colAmount = -1
        var colCategory = -1
        var colSource = -1
        var colDate = -1
        var colDesc = -1
        var colNecessity = -1
        var colNotes = -1

        for (idx in firstRow.indices) {
            val h = firstRow[idx]
            when {
                h.contains("taxonomy") -> colTaxonomy = idx
                h.contains("type") || h.contains("ধরন") -> colType = idx
                h.contains("amount") || h.contains("টাকা") || h.contains("পরিমাণ") -> colAmount = idx
                h.contains("category") || h.contains("খাত") -> colCategory = idx
                h.contains("source") || h.contains("উৎস") || h.contains("মাধ্যম") -> colSource = idx
                h.contains("date") || h.contains("তারিখ") || h.contains("time") -> colDate = idx
                h.contains("desc") || h.contains("বিবরণ") -> colDesc = idx
                h.contains("necessity") || h.contains("প্রয়োজনীয়তা") -> colNecessity = idx
                h.contains("notes") || h.contains("নোট") -> colNotes = idx
            }
        }

        val hasHeader = (colAmount != -1 || colType != -1 || colDesc != -1 || colCategory != -1)
        if (hasHeader) {
            startIndex = 1
        } else {
            // Positional fallback for standard Kori CSV: Taxonomy,Type,Amount_BDT,Category,Source_Party,Date_Time,Description,Necessity,Private_Notes
            colTaxonomy = 0
            colType = 1
            colAmount = 2
            colCategory = 3
            colSource = 4
            colDate = 5
            colDesc = 6
            colNecessity = 7
            colNotes = 8
        }

        for (rowIdx in startIndex until rawRows.size) {
            val row = rawRows[rowIdx]
            val rowNum = rowIdx + 1
            val rawLine = row.joinToString(",")

            fun getCol(index: Int): String = if (index in row.indices) row[index] else ""

            val rawAmount = getCol(colAmount)
            val parsedAmount = parseAmountToPaisa(rawAmount)
            if (parsedAmount == null || parsedAmount <= 0) {
                invalidRows.add(InvalidRowReport(rowNum, rawLine, "টাকার পরিমাণ অকার্যকর ('$rawAmount')"))
                continue
            }

            val rawDate = getCol(colDate)
            val parsedEpoch = parseDateToEpochMs(rawDate) ?: System.currentTimeMillis()

            val rawType = getCol(colType).trim().lowercase()
            val parsedType = when {
                rawType == "income" || rawType == "আয়" || rawType.contains("income") -> "INCOME"
                rawType == "expense" || rawType == "ব্যয়" || rawType.contains("expense") -> "EXPENSE"
                getCol(colTaxonomy).contains("income", ignoreCase = true) -> "INCOME"
                else -> "EXPENSE"
            }

            val category = getCol(colCategory).ifEmpty { if (parsedType == "INCOME") "সাধারণ আয়" else "সাধারণ ব্যয়" }
            val source = getCol(colSource).takeIf { it.isNotEmpty() }
            val desc = getCol(colDesc).ifEmpty { category }
            val necessity = getCol(colNecessity).ifEmpty { "প্রয়োজনীয়" }
            val notes = getCol(colNotes).takeIf { it.isNotEmpty() }
            val taxonomy = getCol(colTaxonomy).ifEmpty { parsedType }

            val parsedRow = CsvParsedRow(
                rowNumber = rowNum,
                taxonomy = taxonomy,
                type = parsedType,
                amountPaisa = parsedAmount,
                categoryName = category,
                sourceParty = source,
                dateTimeEpochMs = parsedEpoch,
                description = desc,
                necessity = necessity,
                notes = notes
            )
            validRows.add(parsedRow)

            val duplicateKey = "${parsedEpoch}_${parsedAmount}_${desc.trim()}"
            if (duplicateKey in existingKeys) {
                duplicateRows.add(parsedRow)
            } else {
                newRows.add(parsedRow)
                if (parsedType == "INCOME") totalIncome += parsedAmount else totalExpense += parsedAmount
            }
        }

        return CsvImportPreview(
            totalRows = rawRows.size - startIndex,
            validRows = validRows,
            invalidRows = invalidRows,
            duplicateRows = duplicateRows,
            newRows = newRows,
            totalIncomePaisa = totalIncome,
            totalExpensePaisa = totalExpense
        )
    }

    /**
     * Executes transactional import with safe rollback on any failure.
     */
    fun executeImport(
        context: Context,
        db: AppDatabase,
        preview: CsvImportPreview,
        skipDuplicates: Boolean = true
    ): CsvImportResult {
        if (preview.validRows.isEmpty()) {
            return CsvImportResult.Failure("আমদানি করার মতো কোনো কার্যকর সারি পাওয়া যায়নি।", "NO_VALID_ROWS")
        }

        val rowsToImport = if (skipDuplicates) preview.newRows else preview.validRows
        if (rowsToImport.isEmpty()) {
            return CsvImportResult.Success(
                importedCount = 0,
                skippedDuplicates = preview.duplicateRows.size,
                invalidRowCount = preview.invalidRows.size
            )
        }

        // 1. Create a safe recovery point before import
        try {
            val currentDbPath = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            val safetyBackupFile = File(context.filesDir, "kori_safety_backup_before_csv_import.db")
            if (currentDbPath.exists()) {
                currentDbPath.copyTo(safetyBackupFile, overwrite = true)
            }
        } catch (_: Exception) {
            // Non-blocking safety copy attempt
        }

        // 2. Transactional execution
        return try {
            db.runInTransaction {
                val existingCategories = db.categoryDao().getAll().associateBy { it.nameBn }
                val categoryMap = existingCategories.toMutableMap()

                for (row in rowsToImport) {
                    var category = categoryMap[row.categoryName]
                    if (category == null) {
                        val isIncome = row.type == "INCOME"
                        val newCat = CategoryEntity(
                            id = UUID.randomUUID().toString(),
                            nameBn = row.categoryName,
                            nameEn = row.categoryName,
                            iconName = if (isIncome) "savings" else "category",
                            colorHex = if (isIncome) "#059669" else "#DC2626",
                            isIncomeCategory = isIncome,
                            isArchived = false,
                            displayOrder = categoryMap.size
                        )
                        db.categoryDao().insert(newCat)
                        categoryMap[row.categoryName] = newCat
                        category = newCat
                    }

                    val eventCode = if (row.type == "INCOME") "TX_01_INCOME" else "TX_02_EXPENSE"
                    val txEntity = TransactionEntity(
                        id = UUID.randomUUID().toString(),
                        eventCode = eventCode,
                        eventKind = row.type,
                        amountPaisa = row.amountPaisa,
                        sourceDescription = row.sourceParty,
                        necessity = row.necessity,
                        categoryId = category.id,
                        partyId = null,
                        debtRecordId = null,
                        earnedReceivableId = null,
                        eventTimestampEpochMs = row.dateTimeEpochMs,
                        description = row.description,
                        notes = row.notes
                    )
                    db.ledgerEventDao().insert(txEntity)
                }

                // Insert audit log
                db.auditDao().insert(
                    AuditLogEntity(
                        entityName = "SYSTEM",
                        entityId = "CSV_IMPORT",
                        actionType = "CSV_IMPORT_SUCCESS",
                        newStateJson = "Imported: ${rowsToImport.size}, SkippedDuplicates: ${preview.duplicateRows.size}, InvalidRows: ${preview.invalidRows.size}",
                        timestampEpochMs = System.currentTimeMillis()
                    )
                )
            }

            CsvImportResult.Success(
                importedCount = rowsToImport.size,
                skippedDuplicates = if (skipDuplicates) preview.duplicateRows.size else 0,
                invalidRowCount = preview.invalidRows.size
            )
        } catch (e: Exception) {
            CsvImportResult.Failure("সিএসভি আমদানি ব্যর্থ এবং সম্পূর্ণ রোলব্যাক করা হয়েছে: ${e.localizedMessage}", e.message)
        }
    }
}
