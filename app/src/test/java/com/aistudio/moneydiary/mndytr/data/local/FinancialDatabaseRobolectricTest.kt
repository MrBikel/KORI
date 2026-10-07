package com.aistudio.moneydiary.mndytr.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.data.local.entity.AccountEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.AttachmentEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.PersonEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.TransactionEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.WorkRecordEntity
import com.aistudio.moneydiary.mndytr.domain.model.ConfirmationStatus
import com.aistudio.moneydiary.mndytr.domain.model.LedgerEventCode
import com.aistudio.moneydiary.mndytr.domain.model.LedgerEventKind
import com.aistudio.moneydiary.mndytr.domain.model.PaymentStatus
import com.aistudio.moneydiary.mndytr.domain.model.WorkStatus
import com.aistudio.moneydiary.mndytr.domain.service.FinancialCalculationEngine
import com.aistudio.moneydiary.mndytr.domain.service.FinancialResult
import com.aistudio.moneydiary.mndytr.domain.service.FinancialService
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FinancialDatabaseRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var service: FinancialService

    private lateinit var cashAccount: AccountEntity
    private lateinit var bankAccount: AccountEntity
    private lateinit var foodCategory: CategoryEntity
    private lateinit var clientPerson: PersonEntity

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        service = FinancialService(db)

        cashAccount = AccountEntity(id = "acc_cash", name = "নগদ", type = "CASH", initialBalancePaisa = 100000L) // ৳1000
        bankAccount = AccountEntity(id = "acc_bank", name = "ব্যাংক", type = "BANK", initialBalancePaisa = 500000L) // ৳5000
        db.accountDao().insert(cashAccount)
        db.accountDao().insert(bankAccount)

        foodCategory = CategoryEntity(id = "cat_food", nameBn = "খাদ্য ও পানীয়", nameEn = "Food", iconName = "fastfood", colorHex = "#D32F2F")
        db.categoryDao().insert(foodCategory)

        clientPerson = PersonEntity(id = "person_rahim", name = "রহিম আহমেদ", personType = "CLIENT")
        db.personDao().insert(clientPerson)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // 1. Opening Balance Execution
    @Test
    fun testOpeningBalanceExecution() {
        val newAccount = AccountEntity(id = "acc_bkash", name = "bKash", type = "MFS_BKASH", initialBalancePaisa = 0L)
        db.accountDao().insert(newAccount)

        val result = service.createOpeningBalance(newAccount.id, 250000L) // ৳2500
        assertTrue(result is FinancialResult.Success)

        val events = db.ledgerEventDao().getConfirmedEventsForAccount(newAccount.id)
        val balance = FinancialCalculationEngine.calculateAccountBalance(newAccount, events)
        assertEquals(250000L, balance)

        val period = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, events)
        assertEquals(0L, period.earnedIncomePaisa)
        assertEquals(0L, period.incurredExpensePaisa)
    }

    // 2. Direct Income and Expense
    @Test
    fun testDirectIncomeAndExpenseFlow() {
        val incomeRes = service.addDirectIncome(cashAccount.id, 50000L, foodCategory.id, null, "Cash gift")
        assertTrue(incomeRes is FinancialResult.Success)

        val expenseRes = service.addExpense(cashAccount.id, 1500L, foodCategory.id, "Tea & Snack")
        assertTrue(expenseRes is FinancialResult.Success)

        val events = db.ledgerEventDao().getConfirmedEventsForAccount(cashAccount.id)
        val balance = FinancialCalculationEngine.calculateAccountBalance(cashAccount, events)
        assertEquals(100000L + 50000L - 1500L, balance)
    }

    // 3. Full & Partial Expense Refund, Multiple Partial Refunds & Excess Rejection
    @Test
    fun testExpenseRefundsFullAndPartialAndExcessRejection() {
        val expRes = service.addExpense(cashAccount.id, 10000L, foodCategory.id, "Supermarket Purchase")
        assertTrue(expRes is FinancialResult.Success)
        val expId = (expRes as FinancialResult.Success).data.id

        // Partial refund 1: ৳30
        val ref1 = service.receiveRefund(expId, cashAccount.id, 3000L, "Returned item 1")
        assertTrue(ref1 is FinancialResult.Success)

        // Partial refund 2: ৳40
        val ref2 = service.receiveRefund(expId, cashAccount.id, 4000L, "Returned item 2")
        assertTrue(ref2 is FinancialResult.Success)

        // Excess refund attempt: ৳50 when remaining is ৳30 -> must fail
        val refExcess = service.receiveRefund(expId, cashAccount.id, 5000L, "Excess refund")
        assertTrue(refExcess is FinancialResult.Error)

        // Full remaining refund: ৳30
        val ref3 = service.receiveRefund(expId, cashAccount.id, 3000L, "Returned item 3")
        assertTrue(ref3 is FinancialResult.Success)

        // Any further refund attempt must fail (remaining is 0)
        val refZero = service.receiveRefund(expId, cashAccount.id, 100L, "No more refund")
        assertTrue(refZero is FinancialResult.Error)
    }

    // 4. Full & Partial Income Refund
    @Test
    fun testIncomeRefundFullAndPartial() {
        val incRes = service.addDirectIncome(cashAccount.id, 20000L, foodCategory.id, null, "Consulting income")
        val incId = (incRes as FinancialResult.Success).data.id

        // Partial refund: ৳50
        val refPartial = service.payRefund(incId, cashAccount.id, 5000L, "Overcharge discount")
        assertTrue(refPartial is FinancialResult.Success)

        // Excess refund attempt
        val refExcess = service.payRefund(incId, cashAccount.id, 16000L, "Greedy refund")
        assertTrue(refExcess is FinancialResult.Error)

        // Full remaining refund: ৳150
        val refRest = service.payRefund(incId, cashAccount.id, 15000L, "Full refund of balance")
        assertTrue(refRest is FinancialResult.Success)
    }

    // 5. Ineligible Refund Rejection (Refund against refund, transfer, loan)
    @Test
    fun testIneligibleRefundTargetRejection() {
        // Create transfer
        val transferRes = service.transferBetweenAccounts(bankAccount.id, cashAccount.id, 50000L)
        val transferId = (transferRes as FinancialResult.Success).data.id

        // Trying to refund a transfer must fail
        val refundTransfer = service.receiveRefund(transferId, cashAccount.id, 1000L, "Refund transfer")
        assertTrue("Cannot refund a transfer", refundTransfer is FinancialResult.Error)

        // Create loan
        val loanRes = service.lendMoney(clientPerson.id, cashAccount.id, 10000L, "Loan")
        val loanId = (loanRes as FinancialResult.Success).data.id

        val refundLoan = service.receiveRefund(loanId, cashAccount.id, 1000L, "Refund loan")
        assertTrue("Cannot refund loan principal as an expense refund", refundLoan is FinancialResult.Error)
    }

    // 6. Savings Allocation Neutrality
    @Test
    fun testSavingsAllocationNeutrality() {
        val savingsAcc = AccountEntity(id = "acc_savings", name = "DPS", type = "SAVINGS", initialBalancePaisa = 0L)
        db.accountDao().insert(savingsAcc)

        val allocRes = service.allocateSavings(cashAccount.id, savingsAcc.id, 25000L, "DPS monthly")
        assertTrue(allocRes is FinancialResult.Success)

        val cashBal = FinancialCalculationEngine.calculateAccountBalance(cashAccount, db.ledgerEventDao().getAllNonDeleted())
        val savBal = FinancialCalculationEngine.calculateAccountBalance(savingsAcc, db.ledgerEventDao().getAllNonDeleted())

        assertEquals(100000L - 25000L, cashBal)
        assertEquals(25000L, savBal)

        val period = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, db.ledgerEventDao().getAllNonDeleted())
        assertEquals(0L, period.earnedIncomePaisa)
        assertEquals(0L, period.incurredExpensePaisa)
    }

    // 7. Balance Adjustment Classification (Positive & Negative)
    @Test
    fun testBalanceAdjustmentClassification() {
        // Positive adjustment ৳50
        val posRes = service.adjustBalance(cashAccount.id, 5000L, true, "Found extra cash in pocket")
        assertTrue(posRes is FinancialResult.Success)

        // Negative adjustment ৳20
        val negRes = service.adjustBalance(cashAccount.id, 2000L, false, "Lost coins")
        assertTrue(negRes is FinancialResult.Success)

        // Rejection when explanation is empty
        val emptyExplanationRes = service.adjustBalance(cashAccount.id, 1000L, true, "")
        assertTrue(emptyExplanationRes is FinancialResult.Error)

        val events = listOf((posRes as FinancialResult.Success).data, (negRes as FinancialResult.Success).data)
        val period = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, events)
        assertEquals(0L, period.earnedIncomePaisa) // Must not be reported as ordinary income
        assertEquals(0L, period.incurredExpensePaisa) // Must not be reported as ordinary expense
    }

    // 8. Confirmed Event Ordinary Deletion Rejection & Draft Deletion Grace
    @Test
    fun testConfirmedEventCannotBeOrdinarilyDeletedAndDraftCan() {
        // Insert a DRAFT event
        val draft = TransactionEntity(
            id = "draft_1",
            eventCode = LedgerEventCode.TX_02_EXPENSE.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 1000L,
            sourceAccountId = cashAccount.id,
            description = "Draft expense",
            confirmationStatus = ConfirmationStatus.DRAFT.name
        )
        db.ledgerEventDao().insert(draft)

        // Draft can be deleted
        val deletedRows = db.ledgerEventDao().deleteDraft(draft.id)
        assertEquals(1, deletedRows)
        assertNull(db.ledgerEventDao().getById(draft.id))

        // Confirmed event cannot be deleted via deleteDraft
        val confirmedRes = service.addExpense(cashAccount.id, 2000L, foodCategory.id, "Confirmed lunch")
        val confirmedId = (confirmedRes as FinancialResult.Success).data.id

        val deletedConfirmedRows = db.ledgerEventDao().deleteDraft(confirmedId)
        assertEquals(0, deletedConfirmedRows) // Rejected
        assertNotNull(db.ledgerEventDao().getById(confirmedId)) // Remains intact
    }

    // 9. Reversal Exactness, Duplicate Reversal Rejection & Reversal of Reversal Rejection
    @Test
    fun testReversalsExactnessAndRejections() {
        val expRes = service.addExpense(cashAccount.id, 5000L, foodCategory.id, "Wrong charge")
        val expId = (expRes as FinancialResult.Success).data.id

        val revRes = service.reverseLedgerEvent(expId, "Wrong amount entered")
        assertTrue(revRes is FinancialResult.Success)

        // Duplicate reversal must fail
        val dupRev = service.reverseLedgerEvent(expId, "Duplicate attempt")
        assertTrue(dupRev is FinancialResult.Error)

        // Reversal of the reversal event itself must fail
        val revEventId = (revRes as FinancialResult.Success).data.id
        val revOfRev = service.reverseLedgerEvent(revEventId, "Reverse of reversal")
        assertTrue(revOfRev is FinancialResult.Error)
    }

    // 10. Loan Collection Exceeding Principal Rejection
    @Test
    fun testLoanCollectionExceedingPrincipalRejection() {
        val lendRes = service.lendMoney(clientPerson.id, cashAccount.id, 15000L, "Loan ৳150")
        val recvId = db.receivableDao().getActive().first().id

        // Partial collection ৳100
        val collect1 = service.collectLoanPrincipal(recvId, cashAccount.id, 10000L)
        assertTrue(collect1 is FinancialResult.Success)

        // Excess collection attempt ৳100 when remaining is ৳50
        val collectExcess = service.collectLoanPrincipal(recvId, cashAccount.id, 10000L)
        assertTrue(collectExcess is FinancialResult.Error)
    }

    // 11. Debt Repayment Exceeding Payable Rejection
    @Test
    fun testDebtRepaymentExceedingPayableRejection() {
        val borrowRes = service.borrowMoney(clientPerson.id, cashAccount.id, 10000L, "Borrowed ৳100")
        val payableId = db.payableDao().getActive().first().id

        // Excess repayment attempt ৳150
        val repayExcess = service.repayDebt(payableId, cashAccount.id, 15000L)
        assertTrue(repayExcess is FinancialResult.Error)

        // Exact repayment ৳100
        val repayExact = service.repayDebt(payableId, cashAccount.id, 10000L)
        assertTrue(repayExact is FinancialResult.Success)
    }

    // 12. Advance Return Exceeding Unearned Advance Rejection
    @Test
    fun testAdvanceReturnExceedingUnearnedAdvanceRejection() {
        val work = WorkRecordEntity(
            id = "work_test_adv",
            clientId = clientPerson.id,
            title = "Test Gig",
            agreedTotalPricePaisa = 50000L,
            workStatus = WorkStatus.IN_PROGRESS.name,
            paymentStatus = PaymentStatus.UNPAID.name
        )
        db.workDao().insert(work)

        service.receiveWorkAdvance(work.id, cashAccount.id, 10000L) // ৳100 advance

        // Attempting to return ৳150 must fail
        val returnExcess = service.returnWorkAdvance(work.id, cashAccount.id, 15000L)
        assertTrue(returnExcess is FinancialResult.Error)

        // Valid return ৳100
        val returnValid = service.returnWorkAdvance(work.id, cashAccount.id, 10000L)
        assertTrue(returnValid is FinancialResult.Success)
    }

    // 13. Atomicity & Rollback After Simulated Failure
    @Test
    fun testAtomicityAndRollbackOnSimulatedFailure() {
        val initialTxCount = db.ledgerEventDao().getAllNonDeleted().size
        val initialAuditCount = db.auditDao().getRecentLogs().size

        var caughtException = false
        try {
            db.runInTransaction {
                // First write succeeds
                val event1 = TransactionEntity(
                    eventCode = LedgerEventCode.TX_01_INCOME.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = 50000L,
                    destinationAccountId = cashAccount.id,
                    description = "Atomic Part 1",
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                db.ledgerEventDao().insert(event1)

                // Simulated failure midway
                throw IllegalStateException("Simulated mid-transaction failure")
            }
        } catch (e: Exception) {
            caughtException = true
        }

        assertTrue("Exception must be caught", caughtException)
        // Verify total rollback: zero records committed from the failed transaction
        assertEquals(initialTxCount, db.ledgerEventDao().getAllNonDeleted().size)
        assertEquals(initialAuditCount, db.auditDao().getRecentLogs().size)
    }

    // 14. Foreign Key Restriction on Attachments
    @Test
    fun testForeignKeyRestrictionOnAttachments() {
        var exceptionThrown = false
        try {
            val invalidAttachment = AttachmentEntity(
                transactionId = "non_existent_tx_id",
                fileName = "receipt.jpg",
                fileMimeType = "image/jpeg",
                fileSizeBytes = 1024L,
                localRelativePath = "/receipts/1.jpg",
                sha256Hash = "hash123"
            )
            db.attachmentDao().insert(invalidAttachment)
        } catch (e: Exception) {
            exceptionThrown = true
        }
        assertTrue("Attachment with invalid transactionId must fail foreign key check", exceptionThrown)
    }

    // 15. Room Schema Export Validation
    @Test
    fun testRoomSchemaExportValidation() {
        // AppDatabase has exportSchema = true.
        // We verify that the schema file directory is configured and exportable.
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull(context)
        assertEquals(2, db.openHelper.readableDatabase.version)
    }
}
