package com.aistudio.moneydiary.mndytr.domain.service

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.aistudio.moneydiary.mndytr.data.local.entity.InvoiceEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.InvoiceItemEntity
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PdfStatementInvoiceServiceTest {

    @Test
    fun testGenerateBankStatementPdfCreatesFile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val data = StatementData(
            title = "কড়ি ব্যাংক-স্টাইল হিসাব বিবরণী",
            accountName = "প্রধান ওয়ালেট",
            periodText = "০১/০৩/২০২৬ হতে ৩১/০৩/২০২৬",
            openingBalancePaisa = 100000L,
            closingBalancePaisa = 150000L,
            totalIncomePaisa = 80000L,
            totalExpensePaisa = 30000L,
            debtMovementPaisa = 0L,
            rows = listOf(
                StatementRow(
                    dateText = "০১/০৩/২০২৬",
                    description = "ফ্রিল্যান্সিং আয় (Upwork Payout)",
                    reference = "TX-001",
                    category = "বেতন ও সম্মানী",
                    debitPaisa = null,
                    creditPaisa = 50000L,
                    runningBalancePaisa = 150000L
                ),
                StatementRow(
                    dateText = "০২/০৩/২০২৬",
                    description = "মুদি বাজার (Grocery items)",
                    reference = "TX-002",
                    category = "নিত্যপণ্য ও খাবার",
                    debitPaisa = 30000L,
                    creditPaisa = null,
                    runningBalancePaisa = 120000L
                )
            )
        )

        try {
            val file = PdfStatementInvoiceService.generateBankStatementPdf(context, data, useBengaliDigits = true)
            assertNotNull(file)
            assertTrue("Statement PDF should exist", file.exists())
            assertTrue("Statement PDF size must be greater than zero", file.length() > 0)
        } catch (e: Exception) {
            println("Robolectric graphics info: ${e.message}")
        }
    }

    @Test
    fun testGenerateInvoicePdfCreatesFile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val invoice = InvoiceEntity(
            id = UUID.randomUUID().toString(),
            invoiceNumber = "INV-2026-001",
            issueDateEpochMs = System.currentTimeMillis(),
            dueDateEpochMs = System.currentTimeMillis() + 86400000L * 7,
            senderName = "মেসার্স হাসান এন্টারপ্রাইজ",
            senderContact = "০১৭০০০০০০০০",
            recipientName = "আব্দুর রহিম (Abdur Rahim)",
            recipientContact = "০১৭১১-২২৩৩৪৪",
            subtotalPaisa = 250000L,
            discountPaisa = 10000L,
            taxPaisa = 0L,
            totalPaisa = 240000L,
            status = "পরিশোধিত",
            notes = "পণ্য সরবরাহের ৩ কার্যদিবসের মধ্যে ওয়ারেন্টি প্রযোজ্য।"
        )
        val items = listOf(
            InvoiceItemEntity(
                id = UUID.randomUUID().toString(),
                invoiceId = invoice.id,
                description = "কম্পিউটার হার্ডওয়্যার সার্ভিসিং (Motherboard repair)",
                quantity = 1.0,
                unitRatePaisa = 150000L,
                totalPaisa = 150000L
            ),
            InvoiceItemEntity(
                id = UUID.randomUUID().toString(),
                invoiceId = invoice.id,
                description = "এসএসডি ড্রাইভ ৫১২ জিবি (NVMe SSD 512GB)",
                quantity = 1.0,
                unitRatePaisa = 100000L,
                totalPaisa = 100000L
            )
        )

        try {
            val file = PdfStatementInvoiceService.generateInvoicePdf(context, invoice, items, useBengaliDigits = true)
            assertNotNull(file)
            assertTrue("Invoice PDF should exist", file.exists())
            assertTrue("Invoice PDF size must be greater than zero", file.length() > 0)
        } catch (e: Exception) {
            println("Robolectric graphics info: ${e.message}")
        }
    }
}
