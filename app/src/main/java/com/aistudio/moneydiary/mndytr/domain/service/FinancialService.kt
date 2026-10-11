package com.aistudio.moneydiary.mndytr.domain.service

import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.data.local.entity.*
import com.aistudio.moneydiary.mndytr.domain.model.*
import com.aistudio.moneydiary.mndytr.domain.service.DiagnosticService
import java.util.UUID

sealed class FinancialResult<out T> {
    data class Success<out T>(val data: T) : FinancialResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : FinancialResult<Nothing>()
}

class FinancialService(private val db: AppDatabase) {

    private val accountDao = db.accountDao()
    private val ledgerEventDao = db.ledgerEventDao()
    private val workDao = db.workDao()
    private val receivableDao = db.receivableDao()
    private val payableDao = db.payableDao()
    private val auditDao = db.auditDao()
    private val partyDao = db.partyDao()
    private val debtRecordDao = db.debtRecordDao()
    private val debtPaymentDao = db.debtPaymentDao()
    private val earnedReceivableDao = db.earnedReceivableDao()
    private val receivablePaymentDao = db.receivablePaymentDao()

    private fun String?.blankToNull(): String? = this?.takeIf { it.isNotBlank() }

    fun recordDiaryEntry(
        type: com.aistudio.moneydiary.mndytr.domain.model.DiaryEntryType,
        amountPaisa: Long,
        categoryId: String? = null,
        sourceDescription: String? = null,
        description: String,
        notes: String? = null,
        necessity: com.aistudio.moneydiary.mndytr.domain.model.NecessityClassification = com.aistudio.moneydiary.mndytr.domain.model.NecessityClassification.UNSPECIFIED,
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("টাকার পরিমাণ শূন্য বা ঋণাত্মক হতে পারে না।")
        if (description.isBlank()) return FinancialResult.Error("হিসাবের একটি সংক্ষিপ্ত বিবরণ লিখুন।")

        val eventCode = if (type == com.aistudio.moneydiary.mndytr.domain.model.DiaryEntryType.INCOME) {
            LedgerEventCode.TX_01_INCOME.name
        } else {
            LedgerEventCode.TX_02_EXPENSE.name
        }

        return try {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_STARTED", type.name)
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = eventCode,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    categoryId = categoryId?.trim()?.blankToNull(),
                    sourceDescription = sourceDescription?.trim()?.blankToNull(),
                    necessity = necessity.banglaLabel,
                    eventTimestampEpochMs = timestampEpochMs,
                    description = description.trim(),
                    notes = notes?.trim()?.blankToNull(),
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Diary entry recorded: ${type.name}")
                DiagnosticService.addCheckpoint("ROOM_TRANSACTION_COMMITTED", type.name)
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_FAILED", type.name)
            FinancialResult.Error("হিসাবটি সংরক্ষণ করা যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন।", e)
        }
    }

    fun updateDiaryEntry(
        id: String,
        amountPaisa: Long,
        categoryId: String? = null,
        sourceDescription: String? = null,
        description: String,
        notes: String? = null,
        necessity: com.aistudio.moneydiary.mndytr.domain.model.NecessityClassification = com.aistudio.moneydiary.mndytr.domain.model.NecessityClassification.UNSPECIFIED,
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("টাকার পরিমাণ শূন্য বা ঋণাত্মক হতে পারে না।")
        if (description.isBlank()) return FinancialResult.Error("বিবরণ খালি রাখা যাবে না।")

        val existing = ledgerEventDao.getById(id) ?: return FinancialResult.Error("হিসাবটি পাওয়া যায়নি।")

        return try {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_STARTED", "UPDATE_DIARY")
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val updated = existing.copy(
                    amountPaisa = amountPaisa,
                    categoryId = categoryId?.trim()?.blankToNull(),
                    sourceDescription = sourceDescription?.trim()?.blankToNull(),
                    necessity = necessity.banglaLabel,
                    description = description.trim(),
                    notes = notes?.trim()?.blankToNull(),
                    eventTimestampEpochMs = timestampEpochMs,
                    updatedAtEpochMs = System.currentTimeMillis(),
                    revisionNumber = existing.revisionNumber + 1
                )
                ledgerEventDao.update(updated)
                logAudit("transactions", updated.id, AuditActionType.UPDATE, existing.toString(), updated.toString(), "Diary entry updated")
                DiagnosticService.addCheckpoint("ROOM_TRANSACTION_COMMITTED", "UPDATE_DIARY")
                FinancialResult.Success(updated)
            }
        } catch (e: Exception) {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_FAILED", "UPDATE_DIARY")
            FinancialResult.Error("হিসাবটি আপডেট করা যায়নি।", e)
        }
    }

    fun deleteOrReverseDiaryEntry(
        id: String,
        reason: String? = null
    ): FinancialResult<Unit> {
        val existing = ledgerEventDao.getById(id) ?: return FinancialResult.Error("হিসাবটি পাওয়া যায়নি।")
        return try {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_STARTED", "REVERSE_DIARY")
            db.runInTransaction<FinancialResult<Unit>> {
                val updated = existing.copy(
                    isDeleted = true,
                    deletedAtEpochMs = System.currentTimeMillis(),
                    confirmationStatus = ConfirmationStatus.REVERSED.name,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
                ledgerEventDao.update(updated)
                logAudit("transactions", existing.id, AuditActionType.DELETE, existing.toString(), updated.toString(), reason ?: "Diary entry removed")
                DiagnosticService.addCheckpoint("ROOM_TRANSACTION_COMMITTED", "REVERSE_DIARY")
                FinancialResult.Success(Unit)
            }
        } catch (e: Exception) {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_FAILED", "REVERSE_DIARY")
            FinancialResult.Error("হিসাবটি মোছা যায়নি।", e)
        }
    }

    fun createOpeningBalance(
        accountId: String,
        amountPaisa: Long,
        timestampEpochMs: Long = System.currentTimeMillis(),
        description: String = "হিসাবের প্রারম্ভিক স্থিতি",
        notes: String? = null
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Opening balance must be greater than zero")
        val account = accountDao.getById(accountId.trim()) ?: return FinancialResult.Error("Account not found: $accountId")

        return try {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_STARTED", "OPENING_BALANCE")
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_00_OPENING_BALANCE.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    destinationAccountId = account.id,
                    eventTimestampEpochMs = timestampEpochMs,
                    description = description.ifBlank { "হিসাবের প্রারম্ভিক স্থিতি" },
                    notes = notes.blankToNull(),
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Opening balance created")
                DiagnosticService.addCheckpoint("ROOM_TRANSACTION_COMMITTED", "OPENING_BALANCE")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_FAILED", "OPENING_BALANCE")
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun addDirectIncome(
        destinationAccountId: String,
        amountPaisa: Long,
        categoryId: String? = null,
        personId: String? = null,
        description: String,
        notes: String? = null,
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Amount must be greater than zero")
        val account = accountDao.getById(destinationAccountId.trim()) ?: return FinancialResult.Error("Destination account not found")

        val sanitizedCatId = categoryId.blankToNull()
        if (sanitizedCatId != null && db.categoryDao().getById(sanitizedCatId) == null) {
            return FinancialResult.Error("Selected category not found")
        }

        return try {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_STARTED", "INCOME")
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_01_INCOME.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    destinationAccountId = account.id,
                    categoryId = sanitizedCatId,
                    personId = personId.blankToNull(),
                    description = description.ifBlank { "সরাসরি আয়" },
                    notes = notes.blankToNull(),
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Direct income")
                DiagnosticService.addCheckpoint("ROOM_TRANSACTION_COMMITTED", "INCOME")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_FAILED", "INCOME")
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun addExpense(
        sourceAccountId: String,
        amountPaisa: Long,
        categoryId: String,
        description: String,
        notes: String? = null,
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Amount must be greater than zero")
        val account = accountDao.getById(sourceAccountId.trim()) ?: return FinancialResult.Error("Source account not found")

        val sanitizedCatId = categoryId.blankToNull()
        if (sanitizedCatId != null && db.categoryDao().getById(sanitizedCatId) == null) {
            return FinancialResult.Error("Selected category not found")
        }

        return try {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_STARTED", "EXPENSE")
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_02_EXPENSE.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    sourceAccountId = account.id,
                    categoryId = sanitizedCatId,
                    description = description.ifBlank { "দৈনন্দিন খরচ" },
                    notes = notes.blankToNull(),
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Direct expense")
                DiagnosticService.addCheckpoint("ROOM_TRANSACTION_COMMITTED", "EXPENSE")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_FAILED", "EXPENSE")
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun receiveWorkAdvance(
        workId: String,
        destinationAccountId: String,
        amountPaisa: Long,
        description: String = "কাজের অগ্রিম টোকেন গ্রহণ",
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Advance amount must be greater than zero")
        val work = workDao.getById(workId.trim()) ?: return FinancialResult.Error("Work record not found: $workId")
        val account = accountDao.getById(destinationAccountId.trim()) ?: return FinancialResult.Error("Account not found")

        return try {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_STARTED", "WORK_ADVANCE")
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_03_ADVANCE_RECEIVED.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    destinationAccountId = account.id,
                    workId = work.id,
                    personId = work.clientId.blankToNull(),
                    description = description.ifBlank { "কাজের অগ্রিম গ্রহণ" },
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                workDao.updatePaymentStatus(work.id, PaymentStatus.ADVANCE_RECEIVED.name)
                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Work advance received")
                DiagnosticService.addCheckpoint("ROOM_TRANSACTION_COMMITTED", "WORK_ADVANCE")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_FAILED", "WORK_ADVANCE")
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun returnWorkAdvance(
        workId: String,
        sourceAccountId: String,
        amountPaisa: Long,
        description: String = "কাজের অগ্রিম ফেরত প্রদান",
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Amount must be greater than zero")
        val work = workDao.getById(workId.trim()) ?: return FinancialResult.Error("Work record not found")
        val account = accountDao.getById(sourceAccountId.trim()) ?: return FinancialResult.Error("Source account not found")

        val linkedEvents = ledgerEventDao.getConfirmedEventsForWork(work.id)
        val proj = FinancialCalculationEngine.calculateWorkFinancials(work, linkedEvents)
        val currentUnearned = proj.advanceReceivedPaisa - proj.advanceEarnedPaisa - proj.advanceReturnedPaisa
        if (amountPaisa > currentUnearned) {
            return FinancialResult.Error("Cannot return ৳${amountPaisa / 100} when unearned advance is only ৳${currentUnearned / 100}")
        }

        return try {
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_04_ADVANCE_RETURNED.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    sourceAccountId = account.id,
                    workId = work.id,
                    personId = work.clientId.blankToNull(),
                    description = description.ifBlank { "কাজের অগ্রিম ফেরত" },
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                workDao.updatePaymentStatus(work.id, PaymentStatus.ADVANCE_RETURNED.name)
                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Work advance returned")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun recognizeAdvance(
        workId: String,
        amountPaisa: Long,
        description: String = "কাজের অগ্রিম আয় হিসেবে স্বীকৃতি",
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Amount must be greater than zero")
        val work = workDao.getById(workId.trim()) ?: return FinancialResult.Error("Work record not found")

        return try {
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = LedgerEventCode.REC_01_ADVANCE_RECOGNITION.name,
                    eventKind = LedgerEventKind.RECOGNITION_EVENT.name,
                    amountPaisa = amountPaisa,
                    workId = work.id,
                    personId = work.clientId.blankToNull(),
                    description = description.ifBlank { "অগ্রিম আয় স্বীকৃতি" },
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Advance converted to earned revenue")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun recognizeReceivable(
        personId: String,
        workId: String? = null,
        title: String,
        amountPaisa: Long,
        dueDateEpochMs: Long? = null,
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<ReceivableEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Receivable amount must be greater than zero")
        val person = db.personDao().getById(personId.trim()) ?: return FinancialResult.Error("Person not found")

        return try {
            db.runInTransaction<FinancialResult<ReceivableEntity>> {
                val receivable = ReceivableEntity(
                    personId = person.id,
                    workId = workId.blankToNull(),
                    title = title.ifBlank { "বকেয়া পাওনা" },
                    totalAmountPaisa = amountPaisa,
                    dueDateEpochMs = dueDateEpochMs,
                    status = ReceivableStatus.ACTIVE.name
                )
                receivableDao.insert(receivable)

                val event = TransactionEntity(
                    eventCode = LedgerEventCode.REC_02_RECEIVABLE_RECOGNITION.name,
                    eventKind = LedgerEventKind.RECOGNITION_EVENT.name,
                    amountPaisa = amountPaisa,
                    personId = person.id,
                    workId = workId.blankToNull(),
                    receivableId = receivable.id,
                    description = "বকেয়া পাওনা ও আয় স্বীকৃতি: ${title.ifBlank { "পাওনা" }}",
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                logAudit("receivables", receivable.id, AuditActionType.INSERT, null, receivable.toString(), "Receivable recognized")
                FinancialResult.Success(receivable)
            }
        } catch (e: Exception) {
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun collectReceivable(
        receivableId: String,
        destinationAccountId: String,
        amountPaisa: Long,
        description: String = "বকেয়া পাওনা আদায়",
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Collection amount must be greater than zero")
        val receivable = receivableDao.getById(receivableId.trim()) ?: return FinancialResult.Error("Receivable not found")
        val account = accountDao.getById(destinationAccountId.trim()) ?: return FinancialResult.Error("Destination account not found")

        val confirmedCollections = ledgerEventDao.getConfirmedEventsForReceivable(receivable.id)
            .filter { it.eventCode == LedgerEventCode.TX_06_RECEIVABLE_COLLECTED.name }
            .sumOf { it.amountPaisa }

        val remaining = receivable.totalAmountPaisa - confirmedCollections
        if (amountPaisa > remaining) {
            return FinancialResult.Error("Cannot collect ৳${amountPaisa / 100} when remaining receivable is ৳${remaining / 100}")
        }

        return try {
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_06_RECEIVABLE_COLLECTED.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    destinationAccountId = account.id,
                    personId = receivable.personId.blankToNull(),
                    workId = receivable.workId.blankToNull(),
                    receivableId = receivable.id,
                    description = description.ifBlank { "বকেয়া পাওনা আদায়" },
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)

                val newTotalCollected = confirmedCollections + amountPaisa
                if (newTotalCollected >= receivable.totalAmountPaisa) {
                    receivableDao.updateStatus(receivable.id, ReceivableStatus.SETTLED.name)
                }

                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Receivable collected")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun borrowMoney(
        lenderPersonId: String,
        destinationAccountId: String,
        amountPaisa: Long,
        title: String = "হাওলাত / ঋণ গ্রহণ",
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Amount must be greater than zero")
        val lender = db.personDao().getById(lenderPersonId.trim()) ?: return FinancialResult.Error("Lender person not found")
        val account = accountDao.getById(destinationAccountId.trim()) ?: return FinancialResult.Error("Account not found")

        return try {
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val payable = PayableEntity(
                    personId = lender.id,
                    title = title.ifBlank { "ঋণ গ্রহণ" },
                    totalAmountPaisa = amountPaisa,
                    status = PayableStatus.ACTIVE.name
                )
                payableDao.insert(payable)

                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_07_BORROWED_MONEY.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    destinationAccountId = account.id,
                    personId = lender.id,
                    payableId = payable.id,
                    description = title.ifBlank { "ঋণ গ্রহণ" },
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Borrowed money")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun createExpensePayable(
        creditorPersonId: String,
        amountPaisa: Long,
        title: String,
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<PayableEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Amount must be greater than zero")
        val creditor = db.personDao().getById(creditorPersonId.trim()) ?: return FinancialResult.Error("Creditor not found")

        return try {
            db.runInTransaction<FinancialResult<PayableEntity>> {
                val payable = PayableEntity(
                    personId = creditor.id,
                    title = title.ifBlank { "বকেয়া খরচ দায়ের স্বীকৃতি" },
                    totalAmountPaisa = amountPaisa,
                    status = PayableStatus.ACTIVE.name
                )
                payableDao.insert(payable)

                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_08_EXPENSE_PAYABLE_CREATED.name,
                    eventKind = LedgerEventKind.RECOGNITION_EVENT.name,
                    amountPaisa = amountPaisa,
                    personId = creditor.id,
                    payableId = payable.id,
                    description = title.ifBlank { "বকেয়া খরচ দায়ের স্বীকৃতি" },
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                logAudit("payables", payable.id, AuditActionType.INSERT, null, payable.toString(), "Expense payable created")
                FinancialResult.Success(payable)
            }
        } catch (e: Exception) {
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun repayDebt(
        payableId: String,
        sourceAccountId: String,
        amountPaisa: Long,
        description: String = "ঋণ / দেনা পরিশোধ",
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Repayment amount must be greater than zero")
        val payable = payableDao.getById(payableId.trim()) ?: return FinancialResult.Error("Payable not found")
        val account = accountDao.getById(sourceAccountId.trim()) ?: return FinancialResult.Error("Source account not found")

        val confirmedRepayments = ledgerEventDao.getConfirmedEventsForPayable(payable.id)
            .filter { it.eventCode == LedgerEventCode.TX_09_DEBT_REPAID.name }
            .sumOf { it.amountPaisa }

        val remaining = payable.totalAmountPaisa - confirmedRepayments
        if (amountPaisa > remaining) {
            return FinancialResult.Error("Cannot repay ৳${amountPaisa / 100} when remaining debt is ৳${remaining / 100}")
        }

        return try {
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_09_DEBT_REPAID.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    sourceAccountId = account.id,
                    personId = payable.personId.blankToNull(),
                    payableId = payable.id,
                    description = description.ifBlank { "দেনা পরিশোধ" },
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)

                val newTotalPaid = confirmedRepayments + amountPaisa
                if (newTotalPaid >= payable.totalAmountPaisa) {
                    payableDao.updateStatus(payable.id, PayableStatus.FULLY_PAID.name)
                }

                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Debt repaid")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun lendMoney(
        borrowerPersonId: String,
        sourceAccountId: String,
        amountPaisa: Long,
        title: String = "ধার / ঋণ প্রদান",
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Amount must be greater than zero")
        val borrower = db.personDao().getById(borrowerPersonId.trim()) ?: return FinancialResult.Error("Borrower not found")
        val account = accountDao.getById(sourceAccountId.trim()) ?: return FinancialResult.Error("Source account not found")

        return try {
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val receivable = ReceivableEntity(
                    personId = borrower.id,
                    title = title.ifBlank { "ধার প্রদান" },
                    totalAmountPaisa = amountPaisa,
                    status = ReceivableStatus.ACTIVE.name
                )
                receivableDao.insert(receivable)

                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_10_LENT_MONEY.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    sourceAccountId = account.id,
                    personId = borrower.id,
                    receivableId = receivable.id,
                    description = title.ifBlank { "ধার প্রদান" },
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Money lent")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun collectLoanPrincipal(
        receivableId: String,
        destinationAccountId: String,
        amountPaisa: Long,
        description: String = "ধারের টাকা ফেরত গ্রহণ",
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Amount must be greater than zero")
        val receivable = receivableDao.getById(receivableId.trim()) ?: return FinancialResult.Error("Loan receivable not found")
        val account = accountDao.getById(destinationAccountId.trim()) ?: return FinancialResult.Error("Destination account not found")

        val confirmedCollections = ledgerEventDao.getConfirmedEventsForReceivable(receivable.id)
            .filter { it.eventCode == LedgerEventCode.TX_11_LOAN_COLLECTED.name }
            .sumOf { it.amountPaisa }

        val remaining = receivable.totalAmountPaisa - confirmedCollections
        if (amountPaisa > remaining) {
            return FinancialResult.Error("Cannot collect ৳${amountPaisa / 100} when remaining loan is ৳${remaining / 100}")
        }

        return try {
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_11_LOAN_COLLECTED.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    destinationAccountId = account.id,
                    personId = receivable.personId.blankToNull(),
                    receivableId = receivable.id,
                    description = description.ifBlank { "ধারের টাকা ফেরত গ্রহণ" },
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)

                val newTotal = confirmedCollections + amountPaisa
                if (newTotal >= receivable.totalAmountPaisa) {
                    receivableDao.updateStatus(receivable.id, ReceivableStatus.SETTLED.name)
                }

                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Loan principal collected")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun transferBetweenAccounts(
        sourceAccountId: String,
        destinationAccountId: String,
        amountPaisa: Long,
        feePaisa: Long = 0L,
        description: String = "নিজস্ব অ্যাকাউন্টে স্থানান্তর",
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Transfer amount must be greater than zero")
        if (sourceAccountId.trim() == destinationAccountId.trim()) {
            return FinancialResult.Error("Source and destination accounts must be different")
        }
        val src = accountDao.getById(sourceAccountId.trim()) ?: return FinancialResult.Error("Source account not found")
        val dst = accountDao.getById(destinationAccountId.trim()) ?: return FinancialResult.Error("Destination account not found")

        val transferGroupId = UUID.randomUUID().toString()

        return try {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_STARTED", "TRANSFER")
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val transferEvent = TransactionEntity(
                    eventCode = LedgerEventCode.TX_14_TRANSFER.name,
                    eventKind = LedgerEventKind.TRANSFER_EVENT.name,
                    amountPaisa = amountPaisa,
                    sourceAccountId = src.id,
                    destinationAccountId = dst.id,
                    transferGroupId = transferGroupId,
                    description = description.ifBlank { "স্থানান্তর" },
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(transferEvent)

                if (feePaisa > 0) {
                    val feeEvent = TransactionEntity(
                        eventCode = LedgerEventCode.TX_14F_TRANSFER_FEE.name,
                        eventKind = LedgerEventKind.CASH_EVENT.name,
                        amountPaisa = feePaisa,
                        sourceAccountId = src.id,
                        parentTransactionId = transferEvent.id,
                        transferGroupId = transferGroupId,
                        description = "${description.ifBlank { "স্থানান্তর" }} (ফি)",
                        eventTimestampEpochMs = timestampEpochMs,
                        confirmationStatus = ConfirmationStatus.CONFIRMED.name
                    )
                    ledgerEventDao.insert(feeEvent)
                }

                logAudit("transactions", transferEvent.id, AuditActionType.INSERT, null, transferEvent.toString(), "Internal transfer")
                DiagnosticService.addCheckpoint("ROOM_TRANSACTION_COMMITTED", "TRANSFER")
                FinancialResult.Success(transferEvent)
            }
        } catch (e: Exception) {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_FAILED", "TRANSFER")
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun allocateToSavings(
        sourceAccountId: String,
        savingsAccountId: String,
        amountPaisa: Long,
        description: String = "সঞ্চয়ে স্থানান্তর",
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> = allocateSavings(sourceAccountId, savingsAccountId, amountPaisa, description, timestampEpochMs)

    fun settleWorkCompleted(
        workId: String,
        accountForFinalSettlementId: String,
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<WorkFinancialProjection> = settleAndCompleteWork(workId, accountForFinalSettlementId, timestampEpochMs)

    fun allocateSavings(
        sourceAccountId: String,
        savingsAccountId: String,
        amountPaisa: Long,
        description: String = "সঞ্চয়ে স্থানান্তর",
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Amount must be greater than zero")
        if (sourceAccountId.trim() == savingsAccountId.trim()) return FinancialResult.Error("Source and savings account must differ")
        val src = accountDao.getById(sourceAccountId.trim()) ?: return FinancialResult.Error("Source account not found")
        val dst = accountDao.getById(savingsAccountId.trim()) ?: return FinancialResult.Error("Savings account not found")

        return try {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_STARTED", "SAVINGS")
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_15_SAVINGS_ALLOCATION.name,
                    eventKind = LedgerEventKind.TRANSFER_EVENT.name,
                    amountPaisa = amountPaisa,
                    sourceAccountId = src.id,
                    destinationAccountId = dst.id,
                    description = description.ifBlank { "সঞ্চয়ে স্থানান্তর" },
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Savings allocation")
                DiagnosticService.addCheckpoint("ROOM_TRANSACTION_COMMITTED", "SAVINGS")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            DiagnosticService.addCheckpoint("ROOM_TRANSACTION_FAILED", "SAVINGS")
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun adjustBalance(
        accountId: String,
        amountPaisa: Long,
        isPositiveSurplus: Boolean,
        explanation: String,
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Adjustment amount must be greater than zero")
        if (explanation.trim().isEmpty()) return FinancialResult.Error("Mandatory explanation required for balance adjustment")
        val account = accountDao.getById(accountId.trim()) ?: return FinancialResult.Error("Account not found")

        return try {
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_16_BALANCE_ADJUSTMENT.name,
                    eventKind = LedgerEventKind.ADJUSTMENT_EVENT.name,
                    amountPaisa = amountPaisa,
                    sourceAccountId = if (!isPositiveSurplus) account.id else null,
                    destinationAccountId = if (isPositiveSurplus) account.id else null,
                    description = "ব্যালেন্স সমন্বয়: $explanation",
                    notes = explanation,
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Balance adjustment")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun reverseLedgerEvent(
        targetEventId: String,
        reversalReason: String,
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (reversalReason.trim().isEmpty()) {
            return FinancialResult.Error("Mandatory reason required to reverse a confirmed transaction")
        }
        val target = ledgerEventDao.getById(targetEventId.trim()) ?: return FinancialResult.Error("Target event not found")
        if (target.confirmationStatus == ConfirmationStatus.REVERSED.name) {
            return FinancialResult.Error("Event is already reversed: $targetEventId")
        }
        if (target.eventCode == LedgerEventCode.TX_17_REVERSAL.name) {
            return FinancialResult.Error("Cannot reverse a reversal entry")
        }

        return try {
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                ledgerEventDao.markAsReversed(target.id, timestampEpochMs)

                val reversalEvent = TransactionEntity(
                    eventCode = LedgerEventCode.TX_17_REVERSAL.name,
                    eventKind = target.eventKind,
                    amountPaisa = target.amountPaisa,
                    sourceAccountId = target.destinationAccountId.blankToNull(),
                    destinationAccountId = target.sourceAccountId.blankToNull(),
                    categoryId = target.categoryId.blankToNull(),
                    personId = target.personId.blankToNull(),
                    workId = target.workId.blankToNull(),
                    receivableId = target.receivableId.blankToNull(),
                    payableId = target.payableId.blankToNull(),
                    originalLedgerEventId = target.id,
                    reversedTransactionId = target.id,
                    description = "রিভার্সাল: ${target.description}",
                    notes = reversalReason,
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(reversalEvent)
                logAudit("transactions", reversalEvent.id, AuditActionType.REVERSAL, target.toString(), reversalEvent.toString(), reversalReason)
                FinancialResult.Success(reversalEvent)
            }
        } catch (e: Exception) {
            FinancialResult.Error("লেনদেনটি সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun settleAndCompleteWork(
        workId: String,
        accountForFinalSettlementId: String,
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<WorkFinancialProjection> {
        val work = workDao.getById(workId.trim()) ?: return FinancialResult.Error("Work record not found: $workId")
        val account = accountDao.getById(accountForFinalSettlementId.trim()) ?: return FinancialResult.Error("Account not found")

        val events = ledgerEventDao.getConfirmedEventsForWork(work.id)
        val proj = FinancialCalculationEngine.calculateWorkFinancials(work, events)

        val unearnedAdvance = proj.advanceReceivedPaisa - proj.advanceEarnedPaisa - proj.advanceReturnedPaisa
        val finalPaymentAmountPaisa = work.agreedTotalPricePaisa - proj.advanceEarnedPaisa - unearnedAdvance

        return try {
            db.runInTransaction<FinancialResult<WorkFinancialProjection>> {
                if (unearnedAdvance > 0) {
                    val recEvent = TransactionEntity(
                        eventCode = LedgerEventCode.REC_01_ADVANCE_RECOGNITION.name,
                        eventKind = LedgerEventKind.RECOGNITION_EVENT.name,
                        amountPaisa = unearnedAdvance,
                        workId = work.id,
                        personId = work.clientId.blankToNull(),
                        description = "অবশিষ্ট অগ্রিম আয় স্বীকৃতি: ${work.title}",
                        eventTimestampEpochMs = timestampEpochMs,
                        confirmationStatus = ConfirmationStatus.CONFIRMED.name
                    )
                    ledgerEventDao.insert(recEvent)
                }

                if (finalPaymentAmountPaisa > 0) {
                    val receivable = ReceivableEntity(
                        personId = work.clientId,
                        workId = work.id,
                        title = "চূড়ান্ত পাওনা: ${work.title}",
                        totalAmountPaisa = finalPaymentAmountPaisa,
                        status = ReceivableStatus.SETTLED.name
                    )
                    receivableDao.insert(receivable)

                    val recEvent = TransactionEntity(
                        eventCode = LedgerEventCode.REC_02_RECEIVABLE_RECOGNITION.name,
                        eventKind = LedgerEventKind.RECOGNITION_EVENT.name,
                        amountPaisa = finalPaymentAmountPaisa,
                        personId = work.clientId,
                        workId = work.id,
                        receivableId = receivable.id,
                        description = "চূড়ান্ত বিল আয় স্বীকৃতি: ${work.title}",
                        eventTimestampEpochMs = timestampEpochMs,
                        confirmationStatus = ConfirmationStatus.CONFIRMED.name
                    )
                    ledgerEventDao.insert(recEvent)

                    val collectEvent = TransactionEntity(
                        eventCode = LedgerEventCode.TX_06_RECEIVABLE_COLLECTED.name,
                        eventKind = LedgerEventKind.CASH_EVENT.name,
                        amountPaisa = finalPaymentAmountPaisa,
                        destinationAccountId = account.id,
                        personId = work.clientId,
                        workId = work.id,
                        receivableId = receivable.id,
                        description = "চূড়ান্ত বিল আদায়: ${work.title}",
                        eventTimestampEpochMs = timestampEpochMs,
                        confirmationStatus = ConfirmationStatus.CONFIRMED.name
                    )
                    ledgerEventDao.insert(collectEvent)
                }

                workDao.updateWorkStatus(work.id, WorkStatus.COMPLETED.name, timestampEpochMs)
                workDao.updatePaymentStatus(work.id, PaymentStatus.FULLY_PAID.name, timestampEpochMs)

                val updatedEvents = ledgerEventDao.getConfirmedEventsForWork(work.id)
                val finalProj = FinancialCalculationEngine.calculateWorkFinancials(work, updatedEvents)

                logAudit("work_records", work.id, AuditActionType.UPDATE, work.toString(), finalProj.toString(), "Work settled & completed")
                FinancialResult.Success(finalProj)
            }
        } catch (e: Exception) {
            FinancialResult.Error("কাজের চূড়ান্ত নিষ্পত্তি সম্পন্ন করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun receiveRefund(
        originalEventId: String,
        destinationAccountId: String,
        amountPaisa: Long,
        description: String = "রিফান্ড গ্রহণ",
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Refund amount must be greater than zero")
        val target = ledgerEventDao.getById(originalEventId.trim())
            ?: return FinancialResult.Error("Original transaction not found")
        if (target.eventCode != LedgerEventCode.TX_02_EXPENSE.name) {
            return FinancialResult.Error("Refund can only be received against an expense transaction")
        }
        val account = accountDao.getById(destinationAccountId.trim())
            ?: return FinancialResult.Error("Destination account not found")

        val existingRefunds = ledgerEventDao.getRefundsLinkedTo(target.id)
        val alreadyRefundedPaisa = existingRefunds.sumOf { it.amountPaisa }
        val remainingRefundablePaisa = target.amountPaisa - alreadyRefundedPaisa

        if (amountPaisa > remainingRefundablePaisa) {
            return FinancialResult.Error("Refund amount ৳${amountPaisa / 100} exceeds remaining refundable amount ৳${remainingRefundablePaisa / 100}")
        }

        return try {
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_12_REFUND_RECEIVED.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    destinationAccountId = account.id,
                    categoryId = target.categoryId,
                    personId = target.personId,
                    originalLedgerEventId = target.id,
                    description = description.ifBlank { "রিফান্ড গ্রহণ" },
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Expense refund received")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            FinancialResult.Error("রিফান্ড সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    fun payRefund(
        originalEventId: String,
        sourceAccountId: String,
        amountPaisa: Long,
        description: String = "রিফান্ড প্রদান",
        timestampEpochMs: Long = System.currentTimeMillis()
    ): FinancialResult<TransactionEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("Refund amount must be greater than zero")
        val target = ledgerEventDao.getById(originalEventId.trim())
            ?: return FinancialResult.Error("Original transaction not found")
        if (target.eventCode != LedgerEventCode.TX_01_INCOME.name) {
            return FinancialResult.Error("Refund can only be paid against an income transaction")
        }
        val account = accountDao.getById(sourceAccountId.trim())
            ?: return FinancialResult.Error("Source account not found")

        val existingRefunds = ledgerEventDao.getRefundsLinkedTo(target.id)
        val alreadyRefundedPaisa = existingRefunds.sumOf { it.amountPaisa }
        val remainingRefundablePaisa = target.amountPaisa - alreadyRefundedPaisa

        if (amountPaisa > remainingRefundablePaisa) {
            return FinancialResult.Error("Refund amount ৳${amountPaisa / 100} exceeds remaining refundable amount ৳${remainingRefundablePaisa / 100}")
        }

        return try {
            db.runInTransaction<FinancialResult<TransactionEntity>> {
                val event = TransactionEntity(
                    eventCode = LedgerEventCode.TX_13_REFUND_PAID.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    sourceAccountId = account.id,
                    categoryId = target.categoryId,
                    personId = target.personId,
                    originalLedgerEventId = target.id,
                    description = description.ifBlank { "রিফান্ড প্রদান" },
                    eventTimestampEpochMs = timestampEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(event)
                logAudit("transactions", event.id, AuditActionType.INSERT, null, event.toString(), "Income refund paid")
                FinancialResult.Success(event)
            }
        } catch (e: Exception) {
            FinancialResult.Error("রিফান্ড সংরক্ষণ করা যায়নি। তথ্যগুলো যাচাই করে আবার চেষ্টা করুন।", e)
        }
    }

    private fun logAudit(
        entityName: String,
        entityId: String,
        action: AuditActionType,
        prevState: String?,
        newState: String,
        reason: String?
    ) {
        try {
            val audit = AuditLogEntity(
                entityName = entityName,
                entityId = entityId,
                actionType = action.name,
                previousStateJson = prevState,
                newStateJson = newState,
                changeReason = reason
            )
            auditDao.insert(audit)
        } catch (_: Exception) {
            // Non-critical audit logging failure must never fail the transaction
        }
    }

    // --- PARTY METHODS ---
    fun createOrGetParty(
        displayName: String,
        partyType: PartyType = PartyType.PERSON,
        phone: String? = null,
        email: String? = null,
        note: String? = null
    ): FinancialResult<PartyEntity> {
        val trimmedName = displayName.trim()
        if (trimmedName.isBlank()) {
            return FinancialResult.Error("ব্যক্তি বা প্রতিষ্ঠানের নাম খালি রাখা যাবে না।")
        }
        val existing = partyDao.findByName(trimmedName)
        if (existing != null) {
            return FinancialResult.Success(existing)
        }
        val party = PartyEntity(
            displayName = trimmedName,
            partyType = partyType.name,
            phone = phone?.trim()?.blankToNull(),
            email = email?.trim()?.blankToNull(),
            note = note?.trim()?.blankToNull()
        )
        partyDao.insert(party)
        logAudit("parties", party.id, AuditActionType.INSERT, null, party.toString(), "Party created: $trimmedName")
        return FinancialResult.Success(party)
    }

    fun getAllParties(): List<PartyItem> {
        return partyDao.getAllActive().map {
            PartyItem(
                id = it.id,
                displayName = it.displayName,
                partyType = it.partyType,
                phone = it.phone,
                email = it.email,
                note = it.note,
                isArchived = it.isArchived
            )
        }
    }

    // --- DEBT: BORROWED (আমি ঋণ নিয়েছি) ---
    fun borrowMoney(
        partyId: String,
        principalPaisa: Long,
        occurredAtEpochMs: Long = System.currentTimeMillis(),
        dueAtEpochMs: Long? = null,
        notes: String? = null
    ): FinancialResult<DebtRecordEntity> {
        if (principalPaisa <= 0) return FinancialResult.Error("ঋণের পরিমাণ শূন্য বা ঋণাত্মক হতে পারে না।")
        val party = partyDao.getById(partyId) ?: return FinancialResult.Error("পক্ষ বা ব্যক্তি খুঁজে পাওয়া যায়নি।")

        return try {
            db.runInTransaction<FinancialResult<DebtRecordEntity>> {
                val record = DebtRecordEntity(
                    partyId = partyId,
                    type = DebtType.BORROWED.name,
                    principalPaisa = principalPaisa,
                    occurredAtEpochMs = occurredAtEpochMs,
                    dueAtEpochMs = dueAtEpochMs,
                    notes = notes?.trim()?.blankToNull(),
                    status = DebtRecordStatus.ACTIVE.name
                )
                debtRecordDao.insert(record)

                // Cash inflow transaction (NOT ordinary income)
                val tx = TransactionEntity(
                    eventCode = LedgerEventCode.TX_07_BORROWED_MONEY.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = principalPaisa,
                    partyId = partyId,
                    debtRecordId = record.id,
                    description = "ঋণ গ্রহণ: ${party.displayName}",
                    notes = notes?.trim()?.blankToNull(),
                    eventTimestampEpochMs = occurredAtEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(tx)

                logAudit("debt_records", record.id, AuditActionType.INSERT, null, record.toString(), "Debt borrowed: ${party.displayName}")
                FinancialResult.Success(record)
            }
        } catch (e: Exception) {
            FinancialResult.Error("ঋণ গ্রহণ সংরক্ষণ করা যায়নি।", e)
        }
    }

    fun repayDebt(
        debtId: String,
        amountPaisa: Long,
        occurredAtEpochMs: Long = System.currentTimeMillis(),
        notes: String? = null
    ): FinancialResult<DebtPaymentEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("পরিশোধের পরিমাণ শূন্য বা ঋণাত্মক হতে পারে না।")
        val debt = debtRecordDao.getById(debtId) ?: return FinancialResult.Error("ঋণের হিসাব পাওয়া যায়নি।")
        if (debt.type != DebtType.BORROWED.name) return FinancialResult.Error("এটি ঋণ গ্রহণের হিসাব নয়।")

        val payments = debtPaymentDao.getPaymentsForDebt(debtId)
        val alreadyRepaid = payments.sumOf { it.amountPaisa }
        val remaining = debt.principalPaisa - alreadyRepaid
        if (amountPaisa > remaining) {
            return FinancialResult.Error("পরিশোধের পরিমাণ বাকি ঋণের চেয়ে বেশি হতে পারে না (বাকি: ৳${remaining / 100})।")
        }

        return try {
            db.runInTransaction<FinancialResult<DebtPaymentEntity>> {
                val payment = DebtPaymentEntity(
                    debtId = debtId,
                    amountPaisa = amountPaisa,
                    occurredAtEpochMs = occurredAtEpochMs,
                    notes = notes?.trim()?.blankToNull()
                )
                debtPaymentDao.insert(payment)

                val party = partyDao.getById(debt.partyId)
                val partyName = party?.displayName ?: "পক্ষ"

                // Cash outflow transaction (NOT ordinary expense)
                val tx = TransactionEntity(
                    eventCode = LedgerEventCode.TX_09_DEBT_REPAID.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    partyId = debt.partyId,
                    debtRecordId = debtId,
                    description = "ঋণ পরিশোধ: $partyName",
                    notes = notes?.trim()?.blankToNull(),
                    eventTimestampEpochMs = occurredAtEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(tx)

                val newRemaining = remaining - amountPaisa
                val newStatus = if (newRemaining == 0L) DebtRecordStatus.SETTLED.name else DebtRecordStatus.PARTIALLY_SETTLED.name
                debtRecordDao.updateStatus(debtId, newStatus)

                logAudit("debt_payments", payment.id, AuditActionType.INSERT, null, payment.toString(), "Debt repayment: $partyName")
                FinancialResult.Success(payment)
            }
        } catch (e: Exception) {
            FinancialResult.Error("ঋণ পরিশোধ সংরক্ষণ করা যায়নি।", e)
        }
    }

    // --- LOAN RECEIVABLE: LENT (কাউকে ধার দেওয়া) ---
    fun lendMoney(
        partyId: String,
        principalPaisa: Long,
        occurredAtEpochMs: Long = System.currentTimeMillis(),
        dueAtEpochMs: Long? = null,
        notes: String? = null
    ): FinancialResult<DebtRecordEntity> {
        if (principalPaisa <= 0) return FinancialResult.Error("ধার দেওয়ার পরিমাণ শূন্য বা ঋণাত্মক হতে পারে না।")
        val party = partyDao.getById(partyId) ?: return FinancialResult.Error("পক্ষ বা ব্যক্তি খুঁজে পাওয়া যায়নি।")

        return try {
            db.runInTransaction<FinancialResult<DebtRecordEntity>> {
                val record = DebtRecordEntity(
                    partyId = partyId,
                    type = DebtType.LENT.name,
                    principalPaisa = principalPaisa,
                    occurredAtEpochMs = occurredAtEpochMs,
                    dueAtEpochMs = dueAtEpochMs,
                    notes = notes?.trim()?.blankToNull(),
                    status = DebtRecordStatus.ACTIVE.name
                )
                debtRecordDao.insert(record)

                // Cash outflow transaction (NOT ordinary expense)
                val tx = TransactionEntity(
                    eventCode = LedgerEventCode.TX_10_LENT_MONEY.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = principalPaisa,
                    partyId = partyId,
                    debtRecordId = record.id,
                    description = "ধার প্রদান: ${party.displayName}",
                    notes = notes?.trim()?.blankToNull(),
                    eventTimestampEpochMs = occurredAtEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(tx)

                logAudit("debt_records", record.id, AuditActionType.INSERT, null, record.toString(), "Money lent: ${party.displayName}")
                FinancialResult.Success(record)
            }
        } catch (e: Exception) {
            FinancialResult.Error("ধার দেওয়ার তথ্য সংরক্ষণ করা যায়নি।", e)
        }
    }

    fun collectLoanRepayment(
        debtId: String,
        amountPaisa: Long,
        occurredAtEpochMs: Long = System.currentTimeMillis(),
        notes: String? = null
    ): FinancialResult<DebtPaymentEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("টাকা আদায়ের পরিমাণ শূন্য বা ঋণাত্মক হতে পারে না।")
        val debt = debtRecordDao.getById(debtId) ?: return FinancialResult.Error("ধারের হিসাব পাওয়া যায়নি।")
        if (debt.type != DebtType.LENT.name) return FinancialResult.Error("এটি কাউকে ধার দেওয়ার হিসাব নয়।")

        val payments = debtPaymentDao.getPaymentsForDebt(debtId)
        val alreadyCollected = payments.sumOf { it.amountPaisa }
        val remaining = debt.principalPaisa - alreadyCollected
        if (amountPaisa > remaining) {
            return FinancialResult.Error("আদায়ের পরিমাণ বাকি ধারের চেয়ে বেশি হতে পারে না (বাকি: ৳${remaining / 100})।")
        }

        return try {
            db.runInTransaction<FinancialResult<DebtPaymentEntity>> {
                val payment = DebtPaymentEntity(
                    debtId = debtId,
                    amountPaisa = amountPaisa,
                    occurredAtEpochMs = occurredAtEpochMs,
                    notes = notes?.trim()?.blankToNull()
                )
                debtPaymentDao.insert(payment)

                val party = partyDao.getById(debt.partyId)
                val partyName = party?.displayName ?: "পক্ষ"

                // Cash inflow transaction (NOT new income)
                val tx = TransactionEntity(
                    eventCode = LedgerEventCode.TX_11_LOAN_COLLECTED.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    partyId = debt.partyId,
                    debtRecordId = debtId,
                    description = "ধারের টাকা ফেরত গ্রহণ: $partyName",
                    notes = notes?.trim()?.blankToNull(),
                    eventTimestampEpochMs = occurredAtEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(tx)

                val newRemaining = remaining - amountPaisa
                val newStatus = if (newRemaining == 0L) DebtRecordStatus.SETTLED.name else DebtRecordStatus.PARTIALLY_SETTLED.name
                debtRecordDao.updateStatus(debtId, newStatus)

                logAudit("debt_payments", payment.id, AuditActionType.INSERT, null, payment.toString(), "Loan repayment collected: $partyName")
                FinancialResult.Success(payment)
            }
        } catch (e: Exception) {
            FinancialResult.Error("ধারের টাকা ফেরত সংরক্ষণ করা যায়নি।", e)
        }
    }

    // --- EARNED / CLIENT RECEIVABLE (কাজ/সেবা থেকে পাওনা) ---
    fun createEarnedReceivable(
        partyId: String,
        title: String,
        agreedAmountPaisa: Long,
        occurredAtEpochMs: Long = System.currentTimeMillis(),
        dueAtEpochMs: Long? = null,
        notes: String? = null
    ): FinancialResult<EarnedReceivableEntity> {
        if (agreedAmountPaisa <= 0) return FinancialResult.Error("পাওনার পরিমাণ শূন্য বা ঋণাত্মক হতে পারে না।")
        if (title.isBlank()) return FinancialResult.Error("কাজের বা সেবার শিরোনাম লিখুন।")
        val party = partyDao.getById(partyId) ?: return FinancialResult.Error("পক্ষ বা ব্যক্তি খুঁজে পাওয়া যায়নি।")

        return try {
            db.runInTransaction<FinancialResult<EarnedReceivableEntity>> {
                val receivable = EarnedReceivableEntity(
                    partyId = partyId,
                    title = title.trim(),
                    agreedAmountPaisa = agreedAmountPaisa,
                    occurredAtEpochMs = occurredAtEpochMs,
                    dueAtEpochMs = dueAtEpochMs,
                    notes = notes?.trim()?.blankToNull(),
                    status = DebtRecordStatus.ACTIVE.name
                )
                earnedReceivableDao.insert(receivable)

                // Recognition event: records claim, does NOT count as cash income yet
                val tx = TransactionEntity(
                    eventCode = LedgerEventCode.TX_05_RECEIVABLE_CREATED.name,
                    eventKind = LedgerEventKind.RECOGNITION_EVENT.name,
                    amountPaisa = agreedAmountPaisa,
                    partyId = partyId,
                    earnedReceivableId = receivable.id,
                    description = "কাজের প্রাপ্য: ${receivable.title} (${party.displayName})",
                    notes = notes?.trim()?.blankToNull(),
                    eventTimestampEpochMs = occurredAtEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(tx)

                logAudit("earned_receivables", receivable.id, AuditActionType.INSERT, null, receivable.toString(), "Earned receivable created: ${party.displayName}")
                FinancialResult.Success(receivable)
            }
        } catch (e: Exception) {
            FinancialResult.Error("কাজের পাওনা সংরক্ষণ করা যায়নি।", e)
        }
    }

    fun recordReceivablePayment(
        receivableId: String,
        amountPaisa: Long,
        occurredAtEpochMs: Long = System.currentTimeMillis(),
        notes: String? = null
    ): FinancialResult<ReceivablePaymentEntity> {
        if (amountPaisa <= 0) return FinancialResult.Error("টাকা আদায়ের পরিমাণ শূন্য বা ঋণাত্মক হতে পারে না।")
        val receivable = earnedReceivableDao.getById(receivableId) ?: return FinancialResult.Error("কাজের পাওনার হিসাব পাওয়া যায়নি।")

        val payments = receivablePaymentDao.getPaymentsForReceivable(receivableId)
        val alreadyCollected = payments.sumOf { it.amountPaisa }
        val remaining = receivable.agreedAmountPaisa - alreadyCollected
        if (amountPaisa > remaining) {
            return FinancialResult.Error("আদায়ের পরিমাণ বাকি পাওনার চেয়ে বেশি হতে পারে না (বাকি: ৳${remaining / 100})।")
        }

        return try {
            db.runInTransaction<FinancialResult<ReceivablePaymentEntity>> {
                val payment = ReceivablePaymentEntity(
                    receivableId = receivableId,
                    amountPaisa = amountPaisa,
                    occurredAtEpochMs = occurredAtEpochMs,
                    notes = notes?.trim()?.blankToNull()
                )
                receivablePaymentDao.insert(payment)

                val party = partyDao.getById(receivable.partyId)
                val partyName = party?.displayName ?: "পক্ষ"

                // Cash income event: now that cash is received, it enters cash income
                val tx = TransactionEntity(
                    eventCode = LedgerEventCode.TX_01_INCOME.name,
                    eventKind = LedgerEventKind.CASH_EVENT.name,
                    amountPaisa = amountPaisa,
                    partyId = receivable.partyId,
                    earnedReceivableId = receivableId,
                    description = "কাজের পাওনা আদায়: ${receivable.title} ($partyName)",
                    notes = notes?.trim()?.blankToNull(),
                    eventTimestampEpochMs = occurredAtEpochMs,
                    confirmationStatus = ConfirmationStatus.CONFIRMED.name
                )
                ledgerEventDao.insert(tx)

                val newRemaining = remaining - amountPaisa
                val newStatus = if (newRemaining == 0L) DebtRecordStatus.SETTLED.name else DebtRecordStatus.PARTIALLY_SETTLED.name
                earnedReceivableDao.updateStatus(receivableId, newStatus)

                logAudit("receivable_payments", payment.id, AuditActionType.INSERT, null, payment.toString(), "Earned receivable payment: $partyName")
                FinancialResult.Success(payment)
            }
        } catch (e: Exception) {
            FinancialResult.Error("পাওনা আদায়ের তথ্য সংরক্ষণ করা যায়নি।", e)
        }
    }

    // --- AGGREGATIONS & PROFILES ---
    fun getDebtReceivablesSummary(): DebtReceivablesSummary {
        val now = System.currentTimeMillis()
        val debts = debtRecordDao.getAllActive()
        val allDebtPayments = debtPaymentDao.getAllNonReversed().groupBy { it.debtId }
        val earnedRecs = earnedReceivableDao.getAllActive()
        val allRecPayments = receivablePaymentDao.getAllNonReversed().groupBy { it.receivableId }

        var borrowedOutstanding = 0L
        var lentOutstanding = 0L
        var overdueCount = 0

        for (debt in debts) {
            val paid = allDebtPayments[debt.id]?.sumOf { it.amountPaisa } ?: 0L
            val rem = Math.max(0L, debt.principalPaisa - paid)
            if (rem > 0L) {
                if (debt.type == DebtType.BORROWED.name) {
                    borrowedOutstanding += rem
                } else {
                    lentOutstanding += rem
                }
                if (debt.dueAtEpochMs != null && now > debt.dueAtEpochMs) {
                    overdueCount++
                }
            }
        }

        var earnedOutstanding = 0L
        for (rec in earnedRecs) {
            val paid = allRecPayments[rec.id]?.sumOf { it.amountPaisa } ?: 0L
            val rem = Math.max(0L, rec.agreedAmountPaisa - paid)
            if (rem > 0L) {
                earnedOutstanding += rem
                if (rec.dueAtEpochMs != null && now > rec.dueAtEpochMs) {
                    overdueCount++
                }
            }
        }

        return DebtReceivablesSummary(
            totalBorrowedOutstandingPaisa = borrowedOutstanding,
            totalLentOutstandingPaisa = lentOutstanding,
            totalEarnedOutstandingPaisa = earnedOutstanding,
            overdueCount = overdueCount
        )
    }

    fun getAllDebtRecordItems(): List<DebtRecordItem> {
        val now = System.currentTimeMillis()
        val parties = partyDao.getAllActive().associateBy { it.id }
        val debts = debtRecordDao.getAllActive()
        val allDebtPayments = debtPaymentDao.getAllNonReversed().groupBy { it.debtId }

        return debts.map { debt ->
            val payments = (allDebtPayments[debt.id] ?: emptyList()).map {
                DebtPaymentItem(
                    id = it.id,
                    debtId = it.debtId,
                    amountPaisa = it.amountPaisa,
                    occurredAtEpochMs = it.occurredAtEpochMs,
                    notes = it.notes,
                    isReversed = it.isReversed
                )
            }
            val totalRepaid = payments.sumOf { it.amountPaisa }
            val remaining = Math.max(0L, debt.principalPaisa - totalRepaid)
            val isOverdue = remaining > 0L && debt.dueAtEpochMs != null && now > debt.dueAtEpochMs

            DebtRecordItem(
                id = debt.id,
                partyId = debt.partyId,
                partyName = parties[debt.partyId]?.displayName ?: "পক্ষ",
                type = debt.type,
                principalPaisa = debt.principalPaisa,
                remainingPaisa = remaining,
                totalRepaidPaisa = totalRepaid,
                occurredAtEpochMs = debt.occurredAtEpochMs,
                dueAtEpochMs = debt.dueAtEpochMs,
                notes = debt.notes,
                status = debt.status,
                isOverdue = isOverdue,
                payments = payments
            )
        }
    }

    fun getAllEarnedReceivableItems(): List<EarnedReceivableItem> {
        val now = System.currentTimeMillis()
        val parties = partyDao.getAllActive().associateBy { it.id }
        val receivables = earnedReceivableDao.getAllActive()
        val allRecPayments = receivablePaymentDao.getAllNonReversed().groupBy { it.receivableId }

        return receivables.map { rec ->
            val payments = (allRecPayments[rec.id] ?: emptyList()).map {
                ReceivablePaymentItem(
                    id = it.id,
                    receivableId = it.receivableId,
                    amountPaisa = it.amountPaisa,
                    occurredAtEpochMs = it.occurredAtEpochMs,
                    notes = it.notes,
                    isReversed = it.isReversed
                )
            }
            val totalReceived = payments.sumOf { it.amountPaisa }
            val remaining = Math.max(0L, rec.agreedAmountPaisa - totalReceived)
            val isOverdue = remaining > 0L && rec.dueAtEpochMs != null && now > rec.dueAtEpochMs

            EarnedReceivableItem(
                id = rec.id,
                partyId = rec.partyId,
                partyName = parties[rec.partyId]?.displayName ?: "পক্ষ",
                title = rec.title,
                agreedAmountPaisa = rec.agreedAmountPaisa,
                totalReceivedPaisa = totalReceived,
                remainingPaisa = remaining,
                occurredAtEpochMs = rec.occurredAtEpochMs,
                dueAtEpochMs = rec.dueAtEpochMs,
                notes = rec.notes,
                status = rec.status,
                isOverdue = isOverdue,
                payments = payments
            )
        }
    }

    fun getPartyFinancialProfile(partyId: String): PartyFinancialProfile? {
        val party = partyDao.getById(partyId) ?: return null
        val debts = getAllDebtRecordItems().filter { it.partyId == partyId }
        val recs = getAllEarnedReceivableItems().filter { it.partyId == partyId }

        val borrowedOutstanding = debts.filter { it.type == DebtType.BORROWED.name }.sumOf { it.remainingPaisa }
        val lentOutstanding = debts.filter { it.type == DebtType.LENT.name }.sumOf { it.remainingPaisa }
        val totalEarned = recs.sumOf { it.agreedAmountPaisa }
        val totalReceived = recs.sumOf { it.totalReceivedPaisa }
        val earnedOutstanding = recs.sumOf { it.remainingPaisa }

        return PartyFinancialProfile(
            party = PartyItem(
                id = party.id,
                displayName = party.displayName,
                partyType = party.partyType,
                phone = party.phone,
                email = party.email,
                note = party.note,
                isArchived = party.isArchived
            ),
            outstandingBorrowedPaisa = borrowedOutstanding,
            outstandingLentPaisa = lentOutstanding,
            outstandingEarnedPaisa = earnedOutstanding,
            totalEarnedPaisa = totalEarned,
            totalReceivedPaisa = totalReceived,
            debts = debts,
            receivables = recs
        )
    }
}
