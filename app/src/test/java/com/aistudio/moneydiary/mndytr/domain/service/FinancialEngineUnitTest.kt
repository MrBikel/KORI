package com.aistudio.moneydiary.mndytr.domain.service

import com.aistudio.moneydiary.mndytr.data.local.entity.AccountEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.TransactionEntity
import com.aistudio.moneydiary.mndytr.domain.model.ConfirmationStatus
import com.aistudio.moneydiary.mndytr.domain.model.LedgerEventCode
import com.aistudio.moneydiary.mndytr.domain.model.LedgerEventKind
import org.junit.Assert.assertEquals
import org.junit.Test

class FinancialEngineUnitTest {

    private val testAccount = AccountEntity(
        id = "acc_cash",
        name = "নগদ (Cash)",
        type = "CASH",
        initialBalancePaisa = 50000L
    )

    private val testBank = AccountEntity(
        id = "acc_bank",
        name = "ব্যাংক (Bank)",
        type = "BANK",
        initialBalancePaisa = 200000L
    )

    @Test
    fun testOpeningBalanceExcludedFromPeriodIncome() {
        val openingEvent = TransactionEntity(
            eventCode = LedgerEventCode.TX_00_OPENING_BALANCE.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 100000L,
            destinationAccountId = testAccount.id,
            description = "Opening balance",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )

        val balance = FinancialCalculationEngine.calculateAccountBalance(testAccount, listOf(openingEvent))
        assertEquals(150000L, balance)

        val summary = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, listOf(openingEvent))
        assertEquals(0L, summary.earnedIncomePaisa)
        assertEquals(0L, summary.incurredExpensePaisa)
    }

    @Test
    fun testDirectIncomeAndExpense() {
        val incomeEvent = TransactionEntity(
            eventCode = LedgerEventCode.TX_01_INCOME.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 100000L,
            destinationAccountId = testAccount.id,
            description = "Salary",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )
        val expenseEvent = TransactionEntity(
            eventCode = LedgerEventCode.TX_02_EXPENSE.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 3000L,
            sourceAccountId = testAccount.id,
            description = "Rickshaw",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )

        val balance = FinancialCalculationEngine.calculateAccountBalance(testAccount, listOf(incomeEvent, expenseEvent))
        assertEquals(50000L + 100000L - 3000L, balance)

        val summary = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, listOf(incomeEvent, expenseEvent))
        assertEquals(100000L, summary.earnedIncomePaisa)
        assertEquals(3000L, summary.incurredExpensePaisa)
        assertEquals(97000L, summary.netPeriodSavingsPaisa)
    }

    @Test
    fun testAdvanceIncreasesCashButNotIncome() {
        val advanceEvent = TransactionEntity(
            eventCode = LedgerEventCode.TX_03_ADVANCE_RECEIVED.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 10000L,
            destinationAccountId = testAccount.id,
            description = "Token",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )

        val balance = FinancialCalculationEngine.calculateAccountBalance(testAccount, listOf(advanceEvent))
        assertEquals(60000L, balance)

        val summary = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, listOf(advanceEvent))
        assertEquals(0L, summary.earnedIncomePaisa)
        assertEquals(10000L, summary.unearnedAdvancePaisa)
    }

    @Test
    fun testAdvanceReturn() {
        val advanceEvent = TransactionEntity(
            eventCode = LedgerEventCode.TX_03_ADVANCE_RECEIVED.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 10000L,
            destinationAccountId = testAccount.id,
            description = "Token",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )
        val returnEvent = TransactionEntity(
            eventCode = LedgerEventCode.TX_04_ADVANCE_RETURNED.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 10000L,
            sourceAccountId = testAccount.id,
            description = "Token return",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )

        val balance = FinancialCalculationEngine.calculateAccountBalance(testAccount, listOf(advanceEvent, returnEvent))
        assertEquals(50000L, balance)

        val summary = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, listOf(advanceEvent, returnEvent))
        assertEquals(0L, summary.unearnedAdvancePaisa)
        assertEquals(0L, summary.earnedIncomePaisa)
        assertEquals(0L, summary.incurredExpensePaisa)
    }

    @Test
    fun testAdvanceRecognitionChangesIncomeButNotCash() {
        val recEvent = TransactionEntity(
            eventCode = LedgerEventCode.REC_01_ADVANCE_RECOGNITION.name,
            eventKind = LedgerEventKind.RECOGNITION_EVENT.name,
            amountPaisa = 10000L,
            description = "Advance earned",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )

        val balance = FinancialCalculationEngine.calculateAccountBalance(testAccount, listOf(recEvent))
        assertEquals(50000L, balance)

        val summary = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, listOf(recEvent))
        assertEquals(10000L, summary.earnedIncomePaisa)
    }

    @Test
    fun testReceivableRecognitionAndCollectionZeroNewIncome() {
        val recEvent = TransactionEntity(
            eventCode = LedgerEventCode.REC_02_RECEIVABLE_RECOGNITION.name,
            eventKind = LedgerEventKind.RECOGNITION_EVENT.name,
            amountPaisa = 40000L,
            description = "Receivable recognition",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )
        val summary1 = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, listOf(recEvent))
        assertEquals(40000L, summary1.earnedIncomePaisa)

        val collectEvent = TransactionEntity(
            eventCode = LedgerEventCode.TX_06_RECEIVABLE_COLLECTED.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 40000L,
            destinationAccountId = testAccount.id,
            description = "Receivable collection",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )

        val balance = FinancialCalculationEngine.calculateAccountBalance(testAccount, listOf(recEvent, collectEvent))
        assertEquals(90000L, balance)

        val summary2 = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, listOf(collectEvent))
        assertEquals(0L, summary2.earnedIncomePaisa)

        val totalSummary = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, listOf(recEvent, collectEvent))
        assertEquals(40000L, totalSummary.earnedIncomePaisa)
    }

    @Test
    fun testBorrowedMoneyAndDebtRepaid() {
        val borrow = TransactionEntity(
            eventCode = LedgerEventCode.TX_07_BORROWED_MONEY.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 50000L,
            destinationAccountId = testAccount.id,
            description = "Hawaalat",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )
        val repay = TransactionEntity(
            eventCode = LedgerEventCode.TX_09_DEBT_REPAID.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 50000L,
            sourceAccountId = testAccount.id,
            description = "Repayment",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )

        val summary = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, listOf(borrow, repay))
        assertEquals(0L, summary.earnedIncomePaisa)
        assertEquals(0L, summary.incurredExpensePaisa)
    }

    @Test
    fun testExpensePayableCreatesExpenseWithoutCashMovement() {
        val payableEvent = TransactionEntity(
            eventCode = LedgerEventCode.TX_08_EXPENSE_PAYABLE_CREATED.name,
            eventKind = LedgerEventKind.RECOGNITION_EVENT.name,
            amountPaisa = 15000L,
            description = "Unpaid utility",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )

        val balance = FinancialCalculationEngine.calculateAccountBalance(testAccount, listOf(payableEvent))
        assertEquals(50000L, balance)

        val summary = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, listOf(payableEvent))
        assertEquals(15000L, summary.incurredExpensePaisa)
    }

    @Test
    fun testLendingAndLoanCollection() {
        val lend = TransactionEntity(
            eventCode = LedgerEventCode.TX_10_LENT_MONEY.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 20000L,
            sourceAccountId = testAccount.id,
            description = "Loan to friend",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )
        val collect = TransactionEntity(
            eventCode = LedgerEventCode.TX_11_LOAN_COLLECTED.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 20000L,
            destinationAccountId = testAccount.id,
            description = "Loan returned",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )

        val summary = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, listOf(lend, collect))
        assertEquals(0L, summary.earnedIncomePaisa)
        assertEquals(0L, summary.incurredExpensePaisa)
    }

    @Test
    fun testTransferPlusFeeInvariant() {
        val transfer = TransactionEntity(
            eventCode = LedgerEventCode.TX_14_TRANSFER.name,
            eventKind = LedgerEventKind.TRANSFER_EVENT.name,
            amountPaisa = 100000L,
            sourceAccountId = testBank.id,
            destinationAccountId = testAccount.id,
            description = "Bank to Cash",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )
        val fee = TransactionEntity(
            eventCode = LedgerEventCode.TX_14F_TRANSFER_FEE.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 2000L,
            sourceAccountId = testBank.id,
            description = "Transfer fee",
            confirmationStatus = ConfirmationStatus.CONFIRMED.name
        )

        val bankBalance = FinancialCalculationEngine.calculateAccountBalance(testBank, listOf(transfer, fee))
        val cashBalance = FinancialCalculationEngine.calculateAccountBalance(testAccount, listOf(transfer, fee))

        assertEquals(200000L - 102000L, bankBalance)
        assertEquals(50000L + 100000L, cashBalance)

        val summary = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, listOf(transfer, fee))
        assertEquals(0L, summary.earnedIncomePaisa)
        assertEquals(2000L, summary.incurredExpensePaisa)
        assertEquals(-2000L, summary.netPeriodSavingsPaisa)
    }

    @Test
    fun testDraftsExcludedFromAllFinancialProjections() {
        val draftIncome = TransactionEntity(
            eventCode = LedgerEventCode.TX_01_INCOME.name,
            eventKind = LedgerEventKind.CASH_EVENT.name,
            amountPaisa = 999999L,
            destinationAccountId = testAccount.id,
            description = "Draft not confirmed",
            confirmationStatus = ConfirmationStatus.DRAFT.name
        )

        val balance = FinancialCalculationEngine.calculateAccountBalance(testAccount, listOf(draftIncome))
        assertEquals(50000L, balance)

        val summary = FinancialCalculationEngine.calculatePeriodSummary(0L, Long.MAX_VALUE, listOf(draftIncome))
        assertEquals(0L, summary.earnedIncomePaisa)
        assertEquals(0L, summary.totalCashInflowPaisa)
    }
}
