package com.aistudio.moneydiary.mndytr.domain.service

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.aistudio.moneydiary.mndytr.R
import com.aistudio.moneydiary.mndytr.data.local.entity.InvoiceEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.InvoiceItemEntity
import com.aistudio.moneydiary.mndytr.domain.model.DiaryEntry
import com.aistudio.moneydiary.mndytr.ui.util.BengaliFormatter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StatementRow(
    val dateText: String,
    val description: String,
    val reference: String,
    val category: String,
    val debitPaisa: Long?, // Expense / Money Out
    val creditPaisa: Long?, // Income / Money In
    val runningBalancePaisa: Long
)

data class StatementData(
    val title: String = "কড়ি ব্যাংক-স্টাইল হিসাব বিবরণী",
    val accountName: String = "সকল হিসাব (একত্রিত)",
    val periodText: String,
    val openingBalancePaisa: Long,
    val closingBalancePaisa: Long,
    val totalIncomePaisa: Long,
    val totalExpensePaisa: Long,
    val debtMovementPaisa: Long = 0L,
    val rows: List<StatementRow>
)

object PdfStatementInvoiceService {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f

    private fun getAdorsholipiTypeface(context: Context): Typeface {
        return try {
            ResourcesCompat.getFont(context, R.font.adorsholipi) ?: Typeface.DEFAULT
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
    }

    /**
     * Generates a multi-page Bank-Style Statement PDF.
     */
    fun generateBankStatementPdf(
        context: Context,
        data: StatementData,
        useBengaliDigits: Boolean = true
    ): File {
        val document = PdfDocument()
        val typeface = getAdorsholipiTypeface(context)

        val textPaint = Paint().apply {
            this.typeface = typeface
            isAntiAlias = true
            color = Color.rgb(20, 30, 45)
            textSize = 9.5f
        }

        val headerPaint = Paint().apply {
            this.typeface = typeface
            isAntiAlias = true
            color = Color.rgb(15, 41, 77) // Navy Blue
            textSize = 15f
            isFakeBoldText = true
        }

        val subHeaderPaint = Paint().apply {
            this.typeface = typeface
            isAntiAlias = true
            color = Color.rgb(90, 105, 120)
            textSize = 9f
        }

        val tableHeaderPaint = Paint().apply {
            this.typeface = typeface
            isAntiAlias = true
            color = Color.rgb(255, 255, 255)
            textSize = 9f
            isFakeBoldText = true
        }

        val linePaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(220, 226, 235)
            strokeWidth = 0.8f
        }

        val navyBgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(15, 41, 77)
        }

        val cardBgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(244, 247, 251)
        }

        val rowsPerPage = 18
        val totalRows = data.rows.size
        val totalPages = Math.max(1, Math.ceil(totalRows.toDouble() / rowsPerPage).toInt())

        val nowStr = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(Date())
        val generatedDateText = if (useBengaliDigits) BengaliFormatter.toBengaliDigits(nowStr) else nowStr

        for (pageIdx in 0 until totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIdx + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Page Header (Bank Style)
            canvas.drawText("কড়ি • প্রতিটি কড়ির নির্ভুল হিসাব", MARGIN, 45f, headerPaint)
            canvas.drawText("অফিসিয়াল আর্থিক হিসাব বিবরণী (Statement of Accounts)", MARGIN, 58f, subHeaderPaint)
            canvas.drawText("প্রস্তুতের সময়: $generatedDateText | পৃষ্ঠা: ${pageIdx + 1} / $totalPages", MARGIN, 70f, subHeaderPaint)

            linePaint.color = Color.rgb(15, 41, 77)
            linePaint.strokeWidth = 2f
            canvas.drawLine(MARGIN, 76f, PAGE_WIDTH - MARGIN, 76f, linePaint)

            var currentY = 92f

            // Show summary card only on first page
            if (pageIdx == 0) {
                val cardRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 62f)
                canvas.drawRoundRect(cardRect, 8f, 8f, cardBgPaint)

                textPaint.textSize = 9f
                textPaint.isFakeBoldText = true
                textPaint.color = Color.rgb(15, 41, 77)
                canvas.drawText("হিসাব: ${data.accountName}", MARGIN + 12f, currentY + 16f, textPaint)
                canvas.drawText("বিবরণী সময়কাল: ${data.periodText}", MARGIN + 12f, currentY + 30f, textPaint)

                textPaint.isFakeBoldText = false
                val openBalStr = formatMoney(data.openingBalancePaisa, useBengaliDigits)
                val closeBalStr = formatMoney(data.closingBalancePaisa, useBengaliDigits)
                val incStr = formatMoney(data.totalIncomePaisa, useBengaliDigits)
                val expStr = formatMoney(data.totalExpensePaisa, useBengaliDigits)

                canvas.drawText("প্রারম্ভিক স্থিতি: $openBalStr", MARGIN + 12f, currentY + 48f, textPaint)
                canvas.drawText("মোট জমা (+): $incStr", MARGIN + 180f, currentY + 48f, textPaint)
                canvas.drawText("মোট খরচ (-): $expStr", MARGIN + 310f, currentY + 48f, textPaint)
                textPaint.isFakeBoldText = true
                canvas.drawText("সমাপনী স্থিতি: $closeBalStr", MARGIN + 420f, currentY + 48f, textPaint)

                currentY += 76f
            }

            // Table Header Bar
            val tableTop = currentY
            val headerHeight = 22f
            val headerRect = RectF(MARGIN, tableTop, PAGE_WIDTH - MARGIN, tableTop + headerHeight)
            canvas.drawRect(headerRect, navyBgPaint)

            val colXDate = MARGIN + 8f
            val colXDesc = MARGIN + 68f
            val colXCat = MARGIN + 215f
            val colXDebit = MARGIN + 320f
            val colXCredit = MARGIN + 390f
            val colXBalance = MARGIN + 465f

            canvas.drawText("তারিখ", colXDate, tableTop + 15f, tableHeaderPaint)
            canvas.drawText("বিবরণ / সূত্র", colXDesc, tableTop + 15f, tableHeaderPaint)
            canvas.drawText("খাত", colXCat, tableTop + 15f, tableHeaderPaint)
            canvas.drawText("খরচ (ডেবিট)", colXDebit, tableTop + 15f, tableHeaderPaint)
            canvas.drawText("জমা (ক্রেডিট)", colXCredit, tableTop + 15f, tableHeaderPaint)
            canvas.drawText("স্থিতি (ব্যালেন্স)", colXBalance, tableTop + 15f, tableHeaderPaint)

            currentY = tableTop + headerHeight

            // Table Rows
            val startRow = pageIdx * rowsPerPage
            val endRow = Math.min(startRow + rowsPerPage, totalRows)

            linePaint.color = Color.rgb(225, 230, 238)
            linePaint.strokeWidth = 0.6f

            for (i in startRow until endRow) {
                val row = data.rows[i]
                val rowHeight = 24f

                if (i % 2 == 1) {
                    val rowBg = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight)
                    canvas.drawRect(rowBg, cardBgPaint)
                }

                textPaint.textSize = 8.5f
                textPaint.color = Color.rgb(30, 40, 55)
                textPaint.isFakeBoldText = false

                canvas.drawText(truncate(row.dateText, 12), colXDate, currentY + 16f, textPaint)
                canvas.drawText(truncate(row.description, 26), colXDesc, currentY + 16f, textPaint)
                canvas.drawText(truncate(row.category, 18), colXCat, currentY + 16f, textPaint)

                // Debit (Expense)
                if (row.debitPaisa != null && row.debitPaisa > 0) {
                    textPaint.color = Color.rgb(200, 30, 30) // Red
                    canvas.drawText(formatMoney(row.debitPaisa, useBengaliDigits), colXDebit, currentY + 16f, textPaint)
                } else {
                    textPaint.color = Color.GRAY
                    canvas.drawText("-", colXDebit, currentY + 16f, textPaint)
                }

                // Credit (Income)
                if (row.creditPaisa != null && row.creditPaisa > 0) {
                    textPaint.color = Color.rgb(10, 130, 60) // Green
                    canvas.drawText(formatMoney(row.creditPaisa, useBengaliDigits), colXCredit, currentY + 16f, textPaint)
                } else {
                    textPaint.color = Color.GRAY
                    canvas.drawText("-", colXCredit, currentY + 16f, textPaint)
                }

                // Balance
                textPaint.color = Color.rgb(15, 41, 77)
                textPaint.isFakeBoldText = true
                canvas.drawText(formatMoney(row.runningBalancePaisa, useBengaliDigits), colXBalance, currentY + 16f, textPaint)

                currentY += rowHeight
                canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
            }

            // Footer
            val footerY = PAGE_HEIGHT - 35f
            linePaint.color = Color.rgb(200, 210, 220)
            canvas.drawLine(MARGIN, footerY, PAGE_WIDTH - MARGIN, footerY, linePaint)

            subHeaderPaint.textSize = 8f
            canvas.drawText("কড়ি স্বয়ংক্রিয়ভাবে তৈরি ডিজিটাল আর্থিক বিবরণী • গোপনীয় ও সুরক্ষিত • Developed by Mehedi Hasan", MARGIN, footerY + 16f, subHeaderPaint)

            document.finishPage(page)
        }

        // Save PDF
        val outputDir = File(context.cacheDir, "pdf_reports")
        if (!outputDir.exists()) outputDir.mkdirs()

        val fileName = "Kori_Statement_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(outputDir, fileName)

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    /**
     * Generates a clean, professional Invoice PDF.
     */
    fun generateInvoicePdf(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        useBengaliDigits: Boolean = true
    ): File {
        val document = PdfDocument()
        val typeface = getAdorsholipiTypeface(context)

        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val headerPaint = Paint().apply {
            this.typeface = typeface
            isAntiAlias = true
            color = Color.rgb(15, 41, 77)
            textSize = 22f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            this.typeface = typeface
            isAntiAlias = true
            color = Color.rgb(30, 40, 55)
            textSize = 9.5f
        }

        val labelPaint = Paint().apply {
            this.typeface = typeface
            isAntiAlias = true
            color = Color.rgb(100, 115, 130)
            textSize = 9f
        }

        val subHeaderPaint = Paint().apply {
            this.typeface = typeface
            isAntiAlias = true
            color = Color.rgb(90, 105, 120)
            textSize = 9f
        }

        val navyBgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(15, 41, 77)
        }

        val tableHeaderPaint = Paint().apply {
            this.typeface = typeface
            isAntiAlias = true
            color = Color.rgb(255, 255, 255)
            textSize = 9f
            isFakeBoldText = true
        }

        val linePaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(220, 226, 235)
            strokeWidth = 0.8f
        }

        val cardBgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(246, 248, 252)
        }

        // 1. Top Brand Header
        canvas.drawText("কড়ি", MARGIN, 55f, headerPaint)

        val brandSubPaint = Paint().apply {
            this.typeface = typeface
            isAntiAlias = true
            color = Color.rgb(100, 115, 130)
            textSize = 10f
        }
        canvas.drawText("ব্যক্তিগত ও বাণিজ্যিক ইনভয়েস", MARGIN, 70f, brandSubPaint)

        // Invoice Number Badge on right
        val invNoText = "ইনভয়েস নং: ${invoice.invoiceNumber}"
        val invNoWidth = headerPaint.measureText(invNoText)
        textPaint.textSize = 13f
        textPaint.isFakeBoldText = true
        textPaint.color = Color.rgb(15, 41, 77)
        canvas.drawText(invNoText, PAGE_WIDTH - MARGIN - 180f, 55f, textPaint)

        val statusText = "স্ট্যাটাস: ${invoice.status}"
        textPaint.textSize = 10f
        textPaint.color = if (invoice.status == "PAID" || invoice.status == "পরিশোধিত") Color.rgb(10, 140, 60) else Color.rgb(210, 80, 20)
        canvas.drawText(statusText, PAGE_WIDTH - MARGIN - 180f, 70f, textPaint)

        linePaint.color = Color.rgb(15, 41, 77)
        linePaint.strokeWidth = 2f
        canvas.drawLine(MARGIN, 82f, PAGE_WIDTH - MARGIN, 82f, linePaint)

        // 2. Sender and Recipient Info Box
        var currentY = 102f
        val infoBox = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + 70f)
        canvas.drawRoundRect(infoBox, 8f, 8f, cardBgPaint)

        textPaint.textSize = 9.5f
        textPaint.color = Color.rgb(20, 30, 45)
        textPaint.isFakeBoldText = true
        canvas.drawText("প্রেরক (কাউন্টার / প্রতিষ্ঠান):", MARGIN + 12f, currentY + 18f, labelPaint)
        canvas.drawText(invoice.senderName, MARGIN + 12f, currentY + 34f, textPaint)
        invoice.senderContact?.let {
            textPaint.isFakeBoldText = false
            canvas.drawText("যোগাযোগ: $it", MARGIN + 12f, currentY + 48f, textPaint)
        }

        canvas.drawText("প্রাপক (গ্রাহক / ক্লায়েন্ট):", MARGIN + 280f, currentY + 18f, labelPaint)
        textPaint.isFakeBoldText = true
        canvas.drawText(invoice.recipientName, MARGIN + 280f, currentY + 34f, textPaint)
        invoice.recipientContact?.let {
            textPaint.isFakeBoldText = false
            canvas.drawText("যোগাযোগ: $it", MARGIN + 280f, currentY + 48f, textPaint)
        }

        val issueDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(invoice.issueDateEpochMs))
        val issueDateBn = if (useBengaliDigits) BengaliFormatter.toBengaliDigits(issueDateStr) else issueDateStr
        textPaint.isFakeBoldText = false
        canvas.drawText("ইস্যু তারিখ: $issueDateBn", MARGIN + 12f, currentY + 62f, textPaint)

        currentY += 86f

        // 3. Itemized Table
        val headerHeight = 22f
        val tableHeaderRect = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + headerHeight)
        canvas.drawRect(tableHeaderRect, navyBgPaint)

        val colSl = MARGIN + 8f
        val colDesc = MARGIN + 40f
        val colQty = MARGIN + 280f
        val colRate = MARGIN + 360f
        val colTotal = MARGIN + 440f

        canvas.drawText("#", colSl, currentY + 15f, tableHeaderPaint)
        canvas.drawText("আইটেম / সেবার বিবরণ", colDesc, currentY + 15f, tableHeaderPaint)
        canvas.drawText("পরিমাণ", colQty, currentY + 15f, tableHeaderPaint)
        canvas.drawText("দর (টাকা)", colRate, currentY + 15f, tableHeaderPaint)
        canvas.drawText("মোট (টাকা)", colTotal, currentY + 15f, tableHeaderPaint)

        currentY += headerHeight
        linePaint.color = Color.rgb(225, 230, 238)
        linePaint.strokeWidth = 0.6f

        for (idx in items.indices) {
            val item = items[idx]
            val rowHeight = 24f

            if (idx % 2 == 1) {
                val rowBg = RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight)
                canvas.drawRect(rowBg, cardBgPaint)
            }

            textPaint.textSize = 9f
            textPaint.color = Color.rgb(30, 40, 55)
            textPaint.isFakeBoldText = false

            val slText = if (useBengaliDigits) BengaliFormatter.toBengaliDigits((idx + 1).toString()) else (idx + 1).toString()
            canvas.drawText(slText, colSl, currentY + 16f, textPaint)
            canvas.drawText(truncate(item.description, 35), colDesc, currentY + 16f, textPaint)

            val qtyText = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
            val qtyBn = if (useBengaliDigits) BengaliFormatter.toBengaliDigits(qtyText) else qtyText
            canvas.drawText(qtyBn, colQty, currentY + 16f, textPaint)

            canvas.drawText(formatMoney(item.unitRatePaisa, useBengaliDigits), colRate, currentY + 16f, textPaint)

            textPaint.isFakeBoldText = true
            textPaint.color = Color.rgb(15, 41, 77)
            canvas.drawText(formatMoney(item.totalPaisa, useBengaliDigits), colTotal, currentY + 16f, textPaint)

            currentY += rowHeight
            canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
        }

        // 4. Totals and Summary Box
        currentY += 15f
        val summaryLeft = PAGE_WIDTH - MARGIN - 210f

        textPaint.textSize = 9.5f
        textPaint.isFakeBoldText = false
        textPaint.color = Color.rgb(50, 60, 75)
        canvas.drawText("উপমোট (Subtotal):", summaryLeft, currentY, textPaint)
        canvas.drawText(formatMoney(invoice.subtotalPaisa, useBengaliDigits), summaryLeft + 120f, currentY, textPaint)

        currentY += 16f
        if (invoice.discountPaisa > 0) {
            canvas.drawText("ছাড় / ডিসকাউন্ট (-):", summaryLeft, currentY, textPaint)
            canvas.drawText(formatMoney(invoice.discountPaisa, useBengaliDigits), summaryLeft + 120f, currentY, textPaint)
            currentY += 16f
        }

        if (invoice.taxPaisa > 0) {
            canvas.drawText("ভ্যাট / ট্যাক্স (+):", summaryLeft, currentY, textPaint)
            canvas.drawText(formatMoney(invoice.taxPaisa, useBengaliDigits), summaryLeft + 120f, currentY, textPaint)
            currentY += 16f
        }

        // Grand Total Box
        val totalBox = RectF(summaryLeft - 10f, currentY, PAGE_WIDTH - MARGIN, currentY + 30f)
        canvas.drawRoundRect(totalBox, 6f, 6f, navyBgPaint)

        tableHeaderPaint.textSize = 12f
        canvas.drawText("সর্বমোট প্রদেয়:", summaryLeft, currentY + 20f, tableHeaderPaint)
        canvas.drawText(formatMoney(invoice.totalPaisa, useBengaliDigits), summaryLeft + 115f, currentY + 20f, tableHeaderPaint)

        // Notes and Signature
        currentY += 50f
        invoice.notes?.takeIf { it.isNotEmpty() }?.let { notes ->
            textPaint.isFakeBoldText = true
            canvas.drawText("বিশেষ দ্রষ্টব্য / শর্তাবলী:", MARGIN, currentY, textPaint)
            textPaint.isFakeBoldText = false
            canvas.drawText(notes, MARGIN, currentY + 15f, textPaint)
        }

        // Signature lines at bottom
        val sigY = PAGE_HEIGHT - 70f
        linePaint.strokeWidth = 0.8f
        linePaint.color = Color.rgb(150, 160, 175)
        canvas.drawLine(MARGIN + 20f, sigY, MARGIN + 140f, sigY, linePaint)
        canvas.drawLine(PAGE_WIDTH - MARGIN - 140f, sigY, PAGE_WIDTH - MARGIN - 20f, sigY, linePaint)

        subHeaderPaint.textSize = 8.5f
        canvas.drawText("গ্রাহকের স্বাক্ষর", MARGIN + 50f, sigY + 15f, subHeaderPaint)
        canvas.drawText("অনুমোদিত স্বাক্ষর", PAGE_WIDTH - MARGIN - 110f, sigY + 15f, subHeaderPaint)

        // Footer
        val footerY = PAGE_HEIGHT - 25f
        canvas.drawText("কড়ি • প্রতিটি কড়ির নির্ভুল হিসাব • Developed by Mehedi Hasan", MARGIN, footerY, subHeaderPaint)

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "pdf_reports")
        if (!outputDir.exists()) outputDir.mkdirs()

        val fileName = "Kori_Invoice_${invoice.invoiceNumber}.pdf"
        val pdfFile = File(outputDir, fileName)

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    private fun formatMoney(paisa: Long, useBengali: Boolean): String {
        val taka = paisa.toDouble() / 100.0
        val formatted = "৳ %.2f".format(Locale.US, taka)
        return if (useBengali) BengaliFormatter.toBengaliDigits(formatted) else formatted
    }

    private fun truncate(str: String, maxLen: Int): String {
        return if (str.length > maxLen) str.substring(0, maxLen - 1) + "…" else str
    }

    fun getSharePdfIntent(context: Context, pdfFile: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "কড়ি আর্থিক রিপোর্ট: ${pdfFile.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
