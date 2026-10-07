package com.aistudio.moneydiary.mndytr.domain.service

import com.aistudio.moneydiary.mndytr.data.local.entity.AccountEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.ReceivableEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.TransactionEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.WorkRecordEntity
import com.aistudio.moneydiary.mndytr.domain.model.ConfirmationStatus
import com.aistudio.moneydiary.mndytr.domain.model.LedgerEventCode
import com.aistudio.moneydiary.mndytr.domain.model.PeriodFinancialSummary
import com.aistudio.moneydiary.mndytr.domain.model.WorkFinancialProjection

object FinancialCalculationEngine {

    fun calculateAccountBalance(
        account: AccountEntity,
        events: List<TransactionEntity>
    ): Long {
        var balance = account.initialBalancePaisa

        for (event in events) {
            if (event.isDeleted || event.confirmationStatus != ConfirmationStatus.CONFIRMED.name) {
                continue
            }
            val amount = event.amountPaisa

            // Inflows
            if (event.destinationAccountId == account.id) {
                when (event.eventCode) {
                    LedgerEventCode.TX_00_OPENING_BALANCE.name,
                    LedgerEventCode.TX_01_INCOME.name,
                    LedgerEventCode.TX_03_ADVANCE_RECEIVED.name,
                    LedgerEventCode.TX_06_RECEIVABLE_COLLECTED.name,
                    LedgerEventCode.TX_07_BORROWED_MONEY.name,
                    LedgerEventCode.TX_11_LOAN_COLLECTED.name,
                    LedgerEventCode.TX_12_REFUND_RECEIVED.name,
                    LedgerEventCode.TX_14_TRANSFER.name,
                    LedgerEventCode.TX_15_SAVINGS_ALLOCATION.name -> {
                        balance = Math.addExact(balance, amount)
                    }
                    LedgerEventCode.TX_16_BALANCE_ADJUSTMENT.name -> {
                        balance = Math.addExact(balance, amount)
                    }
                }
            }

            // Outflows
            if (event.sourceAccountId == account.id) {
                when (event.eventCode) {
                    LedgerEventCode.TX_02_EXPENSE.name,
                    LedgerEventCode.TX_04_ADVANCE_RETURNED.name,
                    LedgerEventCode.TX_09_DEBT_REPAID.name,
                    LedgerEventCode.TX_10_LENT_MONEY.name,
                    LedgerEventCode.TX_13_REFUND_PAID.name,
                    LedgerEventCode.TX_14_TRANSFER.name,
                    LedgerEventCode.TX_14F_TRANSFER_FEE.name,
                    LedgerEventCode.TX_15_SAVINGS_ALLOCATION.name -> {
                        balance = Math.subtractExact(balance, amount)
                    }
                    LedgerEventCode.TX_16_BALANCE_ADJUSTMENT.name -> {
                        balance = Math.subtractExact(balance, amount)
                    }
                }
            }
        }

        return balance
    }

    fun calculatePeriodSummary(
        startEpochMs: Long,
        endEpochMs: Long,
        events: List<TransactionEntity>
    ): PeriodFinancialSummary {
        var earnedIncome = 0L
        var incurredExpense = 0L
        var unearnedAdvance = 0L
        var cashInflow = 0L
        var cashOutflow = 0L

        for (event in events) {
            if (event.isDeleted || event.confirmationStatus != ConfirmationStatus.CONFIRMED.name) {
                continue
            }
            if (event.eventTimestampEpochMs !in startEpochMs..endEpochMs) {
                continue
            }

            val amount = event.amountPaisa

            when (event.eventCode) {
                LedgerEventCode.TX_00_OPENING_BALANCE.name -> {
                    cashInflow = Math.addExact(cashInflow, amount)
                }
                LedgerEventCode.TX_01_INCOME.name -> {
                    earnedIncome = Math.addExact(earnedIncome, amount)
                    cashInflow = Math.addExact(cashInflow, amount)
                }
                LedgerEventCode.TX_02_EXPENSE.name -> {
                    incurredExpense = Math.addExact(incurredExpense, amount)
                    cashOutflow = Math.addExact(cashOutflow, amount)
                }
                LedgerEventCode.TX_03_ADVANCE_RECEIVED.name -> {
                    unearnedAdvance = Math.addExact(unearnedAdvance, amount)
                    cashInflow = Math.addExact(cashInflow, amount)
                }
                LedgerEventCode.TX_04_ADVANCE_RETURNED.name -> {
                    unearnedAdvance = Math.subtractExact(unearnedAdvance, amount)
                    cashOutflow = Math.addExact(cashOutflow, amount)
                }
                LedgerEventCode.TX_05_RECEIVABLE_CREATED.name -> {
                    earnedIncome = Math.addExact(earnedIncome, amount)
                }
                LedgerEventCode.TX_06_RECEIVABLE_COLLECTED.name -> {
                    cashInflow = Math.addExact(cashInflow, amount)
                }
                LedgerEventCode.TX_07_BORROWED_MONEY.name -> {
                    cashInflow = Math.addExact(cashInflow, amount)
                }
                LedgerEventCode.TX_08_EXPENSE_PAYABLE_CREATED.name -> {
                    incurredExpense = Math.addExact(incurredExpense, amount)
                }
                LedgerEventCode.TX_09_DEBT_REPAID.name -> {
                    cashOutflow = Math.addExact(cashOutflow, amount)
                }
                LedgerEventCode.TX_10_LENT_MONEY.name -> {
                    cashOutflow = Math.addExact(cashOutflow, amount)
                }
                LedgerEventCode.TX_11_LOAN_COLLECTED.name -> {
                    cashInflow = Math.addExact(cashInflow, amount)
                }
                LedgerEventCode.TX_12_REFUND_RECEIVED.name -> {
                    incurredExpense = Math.subtractExact(incurredExpense, amount)
                    cashInflow = Math.addExact(cashInflow, amount)
                }
                LedgerEventCode.TX_13_REFUND_PAID.name -> {
                    earnedIncome = Math.subtractExact(earnedIncome, amount)
                    cashOutflow = Math.addExact(cashOutflow, amount)
                }
                LedgerEventCode.TX_14_TRANSFER.name -> {
                    // Neutral
                }
                LedgerEventCode.TX_14F_TRANSFER_FEE.name -> {
                    incurredExpense = Math.addExact(incurredExpense, amount)
                    cashOutflow = Math.addExact(cashOutflow, amount)
                }
                LedgerEventCode.TX_15_SAVINGS_ALLOCATION.name -> {
                    // Neutral
                }
                LedgerEventCode.TX_16_BALANCE_ADJUSTMENT.name -> {
                    if (event.destinationAccountId != null) {
                        cashInflow = Math.addExact(cashInflow, amount)
                    } else if (event.sourceAccountId != null) {
                        cashOutflow = Math.addExact(cashOutflow, amount)
                    }
                }
                LedgerEventCode.REC_01_ADVANCE_RECOGNITION.name -> {
                    unearnedAdvance = Math.subtractExact(unearnedAdvance, amount)
                    earnedIncome = Math.addExact(earnedIncome, amount)
                }
                LedgerEventCode.REC_02_RECEIVABLE_RECOGNITION.name -> {
                    earnedIncome = Math.addExact(earnedIncome, amount)
                }
                LedgerEventCode.REC_03_BAD_DEBT_WRITEOFF.name -> {
                    incurredExpense = Math.addExact(incurredExpense, amount)
                }
                LedgerEventCode.TX_17_REVERSAL.name -> {
                    // Counter-entry
                }
            }
        }

        val netSavings = Math.subtractExact(earnedIncome, incurredExpense)

        return PeriodFinancialSummary(
            startEpochMs = startEpochMs,
            endEpochMs = endEpochMs,
            earnedIncomePaisa = earnedIncome,
            incurredExpensePaisa = incurredExpense,
            unearnedAdvancePaisa = unearnedAdvance,
            netPeriodSavingsPaisa = netSavings,
            totalCashInflowPaisa = cashInflow,
            totalCashOutflowPaisa = cashOutflow
        )
    }

    fun calculateWorkFinancials(
        work: WorkRecordEntity,
        linkedEvents: List<TransactionEntity>
    ): WorkFinancialProjection {
        var advanceReceived = 0L
        var advanceEarned = 0L
        var advanceReturned = 0L
        var progressCollections = 0L
        var recognizedReceivable = 0L
        var receivableCollections = 0L

        for (event in linkedEvents) {
            if (event.isDeleted || event.confirmationStatus != ConfirmationStatus.CONFIRMED.name) {
                continue
            }
            val amount = event.amountPaisa
            when (event.eventCode) {
                LedgerEventCode.TX_03_ADVANCE_RECEIVED.name -> {
                    advanceReceived = Math.addExact(advanceReceived, amount)
                }
                LedgerEventCode.TX_04_ADVANCE_RETURNED.name -> {
                    advanceReturned = Math.addExact(advanceReturned, amount)
                }
                LedgerEventCode.REC_01_ADVANCE_RECOGNITION.name -> {
                    advanceEarned = Math.addExact(advanceEarned, amount)
                }
                LedgerEventCode.REC_02_RECEIVABLE_RECOGNITION.name -> {
                    recognizedReceivable = Math.addExact(recognizedReceivable, amount)
                }
                LedgerEventCode.TX_06_RECEIVABLE_COLLECTED.name -> {
                    receivableCollections = Math.addExact(receivableCollections, amount)
                    progressCollections = Math.addExact(progressCollections, amount)
                }
                LedgerEventCode.TX_01_INCOME.name -> {
                    progressCollections = Math.addExact(progressCollections, amount)
                }
            }
        }

        val totalReceivedCash = Math.addExact(
            Math.subtractExact(advanceReceived, advanceReturned),
            progressCollections
        )

        val totalEarnedRevenue = Math.addExact(advanceEarned, recognizedReceivable)

        val remainingReceivable = Math.subtractExact(recognizedReceivable, receivableCollections)

        val expectedRemainingPayment = Math.max(
            0L,
            Math.subtractExact(
                work.agreedTotalPricePaisa,
                Math.addExact(advanceReceived, progressCollections)
            )
        )

        return WorkFinancialProjection(
            workId = work.id,
            agreedTotalPricePaisa = work.agreedTotalPricePaisa,
            advanceReceivedPaisa = advanceReceived,
            advanceEarnedPaisa = advanceEarned,
            advanceReturnedPaisa = advanceReturned,
            progressPaymentsCollectedPaisa = progressCollections,
            totalReceivedCashPaisa = totalReceivedCash,
            totalEarnedRevenuePaisa = totalEarnedRevenue,
            expectedRemainingPaymentPaisa = expectedRemainingPayment,
            recognizedReceivablePaisa = remainingReceivable
        )
    }

    fun calculateRefundStatus(
        originalEvent: TransactionEntity,
        linkedRefunds: List<TransactionEntity>
    ): Long {
        var refunded = 0L
        for (refund in linkedRefunds) {
            if (refund.isDeleted || refund.confirmationStatus != ConfirmationStatus.CONFIRMED.name) {
                continue
            }
            if (refund.eventCode == LedgerEventCode.TX_12_REFUND_RECEIVED.name ||
                refund.eventCode == LedgerEventCode.TX_13_REFUND_PAID.name
            ) {
                refunded = Math.addExact(refunded, refund.amountPaisa)
            }
        }
        return Math.subtractExact(originalEvent.amountPaisa, refunded)
    }

    fun calculateTotalUnearnedAdvances(
        works: List<WorkRecordEntity>,
        events: List<TransactionEntity>
    ): Long {
        var total = 0L
        for (work in works) {
            val proj = calculateWorkFinancials(work, events.filter { it.workId == work.id })
            val unearnedForWork = Math.max(0L, proj.advanceReceivedPaisa - proj.advanceEarnedPaisa - proj.advanceReturnedPaisa)
            total = Math.addExact(total, unearnedForWork)
        }
        val unlinkedEvents = events.filter { it.workId == null }
        val unlinkedAdvancesReceived = unlinkedEvents
            .filter { it.eventCode == LedgerEventCode.TX_03_ADVANCE_RECEIVED.name && !it.isDeleted && it.confirmationStatus == ConfirmationStatus.CONFIRMED.name }
            .sumOf { it.amountPaisa }
        val unlinkedAdvancesEarned = unlinkedEvents
            .filter { it.eventCode == LedgerEventCode.REC_01_ADVANCE_RECOGNITION.name && !it.isDeleted && it.confirmationStatus == ConfirmationStatus.CONFIRMED.name }
            .sumOf { it.amountPaisa }
        val unlinkedAdvancesReturned = unlinkedEvents
            .filter { it.eventCode == LedgerEventCode.TX_04_ADVANCE_RETURNED.name && !it.isDeleted && it.confirmationStatus == ConfirmationStatus.CONFIRMED.name }
            .sumOf { it.amountPaisa }
        val unlinkedUnearned = Math.max(0L, unlinkedAdvancesReceived - unlinkedAdvancesEarned - unlinkedAdvancesReturned)

        return Math.addExact(total, unlinkedUnearned)
    }

    fun calculateTotalRecognizedReceivables(
        receivables: List<ReceivableEntity>,
        events: List<TransactionEntity>
    ): Long {
        var total = 0L
        for (receivable in receivables) {
            if (receivable.isDeleted) continue
            val linkedEvents = events.filter { it.receivableId == receivable.id }
            val collected = linkedEvents
                .filter { it.eventCode == LedgerEventCode.TX_06_RECEIVABLE_COLLECTED.name && !it.isDeleted && it.confirmationStatus == ConfirmationStatus.CONFIRMED.name }
                .sumOf { it.amountPaisa }
            val remaining = Math.max(0L, receivable.totalAmountPaisa - collected)
            total = Math.addExact(total, remaining)
        }
        return total
    }

    fun calculateAccountBalances(
        accounts: List<AccountEntity>,
        events: List<TransactionEntity>
    ): Map<String, Long> {
        return accounts.associate { it.id to calculateAccountBalance(it, events) }
    }

    fun calculatePeriodFinancialSummary(
        startEpochMs: Long,
        endEpochMs: Long,
        events: List<TransactionEntity>
    ): PeriodFinancialSummary {
        return calculatePeriodSummary(startEpochMs, endEpochMs, events)
    }
}
