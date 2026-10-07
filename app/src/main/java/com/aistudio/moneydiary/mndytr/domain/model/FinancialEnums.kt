package com.aistudio.moneydiary.mndytr.domain.model

enum class LedgerEventKind {
    CASH_EVENT,
    RECOGNITION_EVENT,
    TRANSFER_EVENT,
    ADJUSTMENT_EVENT,
    REVERSAL_EVENT
}

enum class LedgerEventCode(val kind: LedgerEventKind, val descriptionBn: String, val descriptionEn: String) {
    TX_00_OPENING_BALANCE(LedgerEventKind.CASH_EVENT, "প্রারম্ভিক স্থিতি", "Opening Balance"),
    TX_01_INCOME(LedgerEventKind.CASH_EVENT, "সরাসরি অর্জিত আয়", "Direct Earned Income"),
    TX_02_EXPENSE(LedgerEventKind.CASH_EVENT, "খরচ / ব্যয়", "Direct Expense"),
    TX_03_ADVANCE_RECEIVED(LedgerEventKind.CASH_EVENT, "কাজের অগ্রিম গ্রহণ", "Work Advance Received"),
    TX_04_ADVANCE_RETURNED(LedgerEventKind.CASH_EVENT, "কাজের অগ্রিম ফেরত প্রদান", "Work Advance Returned"),
    TX_05_RECEIVABLE_CREATED(LedgerEventKind.RECOGNITION_EVENT, "বকেয়া পাওনা তৈরি", "Direct Receivable Created"),
    TX_06_RECEIVABLE_COLLECTED(LedgerEventKind.CASH_EVENT, "বকেয়া পাওনা আদায়", "Receivable Collected"),
    TX_07_BORROWED_MONEY(LedgerEventKind.CASH_EVENT, "ঋণ গ্রহণ (হাওলাত)", "Money Borrowed"),
    TX_08_EXPENSE_PAYABLE_CREATED(LedgerEventKind.RECOGNITION_EVENT, "খরচের দেনা তৈরি", "Expense Payable Created"),
    TX_09_DEBT_REPAID(LedgerEventKind.CASH_EVENT, "ঋণ / দেনা পরিশোধ", "Debt Repaid"),
    TX_10_LENT_MONEY(LedgerEventKind.CASH_EVENT, "ঋণ প্রদান (ধার দেওয়া)", "Money Lent"),
    TX_11_LOAN_COLLECTED(LedgerEventKind.CASH_EVENT, "ধারের টাকা ফেরত গ্রহণ", "Loan Collected"),
    TX_12_REFUND_RECEIVED(LedgerEventKind.CASH_EVENT, "ফেরত পাওয়া টাকা", "Refund Received"),
    TX_13_REFUND_PAID(LedgerEventKind.CASH_EVENT, "রিফান্ড প্রদান", "Refund Paid"),
    TX_14_TRANSFER(LedgerEventKind.TRANSFER_EVENT, "নিজের অ্যাকাউন্টে স্থানান্তর", "Internal Transfer"),
    TX_14F_TRANSFER_FEE(LedgerEventKind.CASH_EVENT, "স্থানান্তর ফি", "Transfer Fee"),
    TX_15_SAVINGS_ALLOCATION(LedgerEventKind.TRANSFER_EVENT, "সঞ্চয়ে স্থানান্তর", "Savings Allocation"),
    TX_16_BALANCE_ADJUSTMENT(LedgerEventKind.ADJUSTMENT_EVENT, "ব্যালেন্স সমন্বয়", "Balance Adjustment"),
    TX_17_REVERSAL(LedgerEventKind.REVERSAL_EVENT, "লেনদেন সংশোধন / প্রত্যাহার", "Transaction Reversal"),
    REC_01_ADVANCE_RECOGNITION(LedgerEventKind.RECOGNITION_EVENT, "অগ্রিম আয় হিসেবে স্বীকৃতি", "Advance Recognition"),
    REC_02_RECEIVABLE_RECOGNITION(LedgerEventKind.RECOGNITION_EVENT, "বকেয়া আয় হিসেবে স্বীকৃতি", "Receivable Recognition"),
    REC_03_BAD_DEBT_WRITEOFF(LedgerEventKind.RECOGNITION_EVENT, "অনাদায়ী পাওনা অবলোপন", "Bad Debt Write-off")
}

enum class ConfirmationStatus {
    DRAFT,
    CONFIRMED,
    REVERSED
}

enum class WorkStatus {
    DISCUSSION,
    CONFIRMED,
    IN_PROGRESS,
    SUBMITTED,
    AWAITING_APPROVAL,
    COMPLETED,
    CANCELLED
}

enum class PaymentStatus {
    UNPAID,
    ADVANCE_RECEIVED,
    PARTIALLY_PAID,
    FULLY_PAID,
    REFUND_PENDING,
    ADVANCE_RETURNED,
    REFUNDED
}

enum class AccountType {
    CASH,
    BANK,
    MFS_BKASH,
    MFS_NAGAD,
    MFS_ROCKET,
    MFS_UPAY,
    SAVINGS,
    DEBT,
    CUSTOM
}

enum class ReceivableStatus {
    ACTIVE,
    SETTLED,
    WRITTEN_OFF
}

enum class PayableStatus {
    ACTIVE,
    FULLY_PAID,
    CANCELLED
}

enum class SavingsGoalStatus {
    IN_PROGRESS,
    ACHIEVED,
    ABANDONED
}

enum class BackupJobStatus {
    PENDING,
    PREPARING,
    ENCRYPTED,
    UPLOADING,
    UPLOADED,
    VERIFIED,
    FAILED
}

enum class BackupStatus {
    LOCAL_ONLY,
    UPLOADED,
    VERIFIED,
    CORRUPTED
}

enum class AuditActionType {
    INSERT,
    UPDATE,
    DELETE,
    REVERSAL,
    SOFT_DELETE,
    RESTORE,
    UNDO_DRAFT
}
