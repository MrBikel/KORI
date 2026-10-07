package com.aistudio.moneydiary.mndytr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aistudio.moneydiary.mndytr.data.local.entity.AccountEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.AppSettingEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.AttachmentEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.AuditLogEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.BackupJobEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.BackupMetadataEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.FrequentShortcutEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.FrugalityGoalEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.PayableEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.PersonEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.ReceivableEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.SavingsGoalEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.TransactionEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.WorkRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(account: AccountEntity)

    @Update
    fun update(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE id = :id AND isDeleted = 0")
    fun getById(id: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE isDeleted = 0 AND isArchived = 0 ORDER BY displayOrder ASC, name ASC")
    fun getAllActive(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE isDeleted = 0 AND isArchived = 0 ORDER BY displayOrder ASC, name ASC")
    fun observeAllActive(): Flow<List<AccountEntity>>

    @Query("UPDATE accounts SET isArchived = 1, updatedAtEpochMs = :timestamp WHERE id = :id")
    fun archiveAccount(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE accounts SET isDeleted = 1, updatedAtEpochMs = :timestamp WHERE id = :id")
    fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM accounts WHERE isDeleted = 0")
    fun getAccountCount(): Int
}

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(category: CategoryEntity)

    @Update
    fun update(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE id = :id AND isDeleted = 0")
    fun getById(id: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE isDeleted = 0 ORDER BY displayOrder ASC, nameBn ASC")
    fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE isDeleted = 0 ORDER BY displayOrder ASC, nameBn ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isDeleted = 0 AND isArchived = 0 ORDER BY displayOrder ASC, nameBn ASC")
    fun getAllActive(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE isDeleted = 0 AND isArchived = 0 ORDER BY displayOrder ASC, nameBn ASC")
    fun observeAllActive(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isDeleted = 0 AND isMicroExpense = 1 ORDER BY displayOrder ASC")
    fun getMicroExpenseCategories(): List<CategoryEntity>

    @Query("UPDATE categories SET isArchived = :isArchived, updatedAtEpochMs = :timestamp WHERE id = :id")
    fun setArchived(id: String, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE categories SET isDeleted = 1, updatedAtEpochMs = :timestamp WHERE id = :id")
    fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM categories WHERE isDeleted = 0")
    fun getCategoryCount(): Int
}

@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(person: PersonEntity)

    @Update
    fun update(person: PersonEntity)

    @Query("SELECT * FROM persons WHERE id = :id AND isDeleted = 0")
    fun getById(id: String): PersonEntity?

    @Query("SELECT * FROM persons WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAll(): List<PersonEntity>

    @Query("SELECT * FROM persons WHERE isDeleted = 0 AND personType = :type ORDER BY name ASC")
    fun getByType(type: String): List<PersonEntity>

    @Query("UPDATE persons SET isDeleted = 1, updatedAtEpochMs = :timestamp WHERE id = :id")
    fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface WorkDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(work: WorkRecordEntity)

    @Update
    fun update(work: WorkRecordEntity)

    @Query("SELECT * FROM work_records WHERE id = :id AND isDeleted = 0")
    fun getById(id: String): WorkRecordEntity?

    @Query("SELECT * FROM work_records WHERE isDeleted = 0 ORDER BY createdAtEpochMs DESC")
    fun getAll(): List<WorkRecordEntity>

    @Query("SELECT * FROM work_records WHERE isDeleted = 0 AND clientId = :clientId ORDER BY createdAtEpochMs DESC")
    fun getByClientId(clientId: String): List<WorkRecordEntity>

    @Query("UPDATE work_records SET workStatus = :status, updatedAtEpochMs = :timestamp WHERE id = :id")
    fun updateWorkStatus(id: String, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE work_records SET paymentStatus = :status, updatedAtEpochMs = :timestamp WHERE id = :id")
    fun updatePaymentStatus(id: String, status: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface ReceivableDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(receivable: ReceivableEntity)

    @Update
    fun update(receivable: ReceivableEntity)

    @Query("SELECT * FROM receivables WHERE id = :id AND isDeleted = 0")
    fun getById(id: String): ReceivableEntity?

    @Query("SELECT * FROM receivables WHERE isDeleted = 0 AND status = 'ACTIVE' ORDER BY createdAtEpochMs DESC")
    fun getActive(): List<ReceivableEntity>

    @Query("SELECT * FROM receivables WHERE isDeleted = 0 AND workId = :workId")
    fun getByWorkId(workId: String): List<ReceivableEntity>

    @Query("UPDATE receivables SET status = :status, updatedAtEpochMs = :timestamp WHERE id = :id")
    fun updateStatus(id: String, status: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface PayableDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(payable: PayableEntity)

    @Update
    fun update(payable: PayableEntity)

    @Query("SELECT * FROM payables WHERE id = :id AND isDeleted = 0")
    fun getById(id: String): PayableEntity?

    @Query("SELECT * FROM payables WHERE isDeleted = 0 AND status = 'ACTIVE' ORDER BY createdAtEpochMs DESC")
    fun getActive(): List<PayableEntity>

    @Query("UPDATE payables SET status = :status, updatedAtEpochMs = :timestamp WHERE id = :id")
    fun updateStatus(id: String, status: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface SavingsGoalDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(goal: SavingsGoalEntity)

    @Update
    fun update(goal: SavingsGoalEntity)

    @Query("SELECT * FROM savings_goals WHERE id = :id AND isDeleted = 0")
    fun getById(id: String): SavingsGoalEntity?

    @Query("SELECT * FROM savings_goals WHERE isDeleted = 0 ORDER BY targetDateEpochMs ASC")
    fun getAll(): List<SavingsGoalEntity>
}

@Dao
interface FrequentShortcutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(shortcuts: List<FrequentShortcutEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(shortcut: FrequentShortcutEntity)

    @Update
    fun update(shortcut: FrequentShortcutEntity)

    @Query("SELECT * FROM frequent_shortcuts WHERE isDeleted = 0 ORDER BY displayOrder ASC, usageCount DESC")
    fun getAll(): List<FrequentShortcutEntity>

    @Query("UPDATE frequent_shortcuts SET usageCount = usageCount + 1, updatedAtEpochMs = :timestamp WHERE id = :id")
    fun incrementUsage(id: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface AttachmentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(attachment: AttachmentEntity)

    @Query("SELECT * FROM attachments WHERE id = :id AND isDeleted = 0")
    fun getById(id: String): AttachmentEntity?

    @Query("SELECT * FROM attachments WHERE transactionId = :txId AND isDeleted = 0")
    fun getByTransactionId(txId: String): List<AttachmentEntity>

    @Query("UPDATE attachments SET isDeleted = 1, updatedAtEpochMs = :timestamp WHERE id = :id")
    fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface BackupJobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(job: BackupJobEntity)

    @Update
    fun update(job: BackupJobEntity)

    @Query("SELECT * FROM backup_jobs ORDER BY createdAtEpochMs DESC LIMIT 1")
    fun getLatestJob(): BackupJobEntity?
}

@Dao
interface BackupMetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(metadata: BackupMetadataEntity)

    @Query("SELECT * FROM backup_metadata ORDER BY backupTimestampEpochMs DESC")
    fun getAll(): List<BackupMetadataEntity>

    @Query("SELECT * FROM backup_metadata WHERE id = :id")
    fun getById(id: String): BackupMetadataEntity?
}

@Dao
interface AppSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun set(setting: AppSettingEntity)

    fun set(key: String, value: String) {
        set(AppSettingEntity(key = key, value = value))
    }

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    fun get(key: String): String?

    @Query("SELECT * FROM app_settings")
    fun getAll(): List<AppSettingEntity>
}

@Dao
interface AuditDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(log: AuditLogEntity)

    @Query("SELECT * FROM audit_logs WHERE entityName = :entityName AND entityId = :entityId ORDER BY timestampEpochMs DESC")
    fun getLogsForEntity(entityName: String, entityId: String): List<AuditLogEntity>

    @Query("SELECT * FROM audit_logs ORDER BY timestampEpochMs DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): List<AuditLogEntity>
}

@Dao
interface LedgerEventDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(event: TransactionEntity)

    @Update
    fun update(event: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY eventTimestampEpochMs DESC, createdAtEpochMs DESC")
    fun getAllNonDeleted(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY eventTimestampEpochMs DESC")
    fun observeLedgerHistory(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE originalLedgerEventId = :originalId AND isDeleted = 0 AND confirmationStatus = 'CONFIRMED'")
    fun getRefundsLinkedTo(originalId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE reversedTransactionId = :targetId AND isDeleted = 0 AND confirmationStatus = 'CONFIRMED' LIMIT 1")
    fun getReversalOf(targetId: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE workId = :workId AND isDeleted = 0 AND confirmationStatus = 'CONFIRMED' ORDER BY eventTimestampEpochMs ASC")
    fun getConfirmedEventsForWork(workId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE receivableId = :receivableId AND isDeleted = 0 AND confirmationStatus = 'CONFIRMED'")
    fun getConfirmedEventsForReceivable(receivableId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE payableId = :payableId AND isDeleted = 0 AND confirmationStatus = 'CONFIRMED'")
    fun getConfirmedEventsForPayable(payableId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND confirmationStatus = 'CONFIRMED' AND (sourceAccountId = :accountId OR destinationAccountId = :accountId)")
    fun getConfirmedEventsForAccount(accountId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND confirmationStatus = 'CONFIRMED' AND eventTimestampEpochMs BETWEEN :startEpochMs AND :endEpochMs")
    fun getConfirmedEventsInPeriod(startEpochMs: Long, endEpochMs: Long): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE id = :id AND confirmationStatus = 'DRAFT'")
    fun deleteDraft(id: String): Int

    @Query("UPDATE transactions SET confirmationStatus = 'REVERSED', updatedAtEpochMs = :timestamp WHERE id = :id")
    fun markAsReversed(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE transactions SET isDeleted = 1, deletedAtEpochMs = :timestamp WHERE id = :id AND confirmationStatus = 'DRAFT'")
    fun softDeleteDraft(id: String, timestamp: Long = System.currentTimeMillis()): Int

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND (description LIKE '%' || :query || '%' OR (notes IS NOT NULL AND notes LIKE '%' || :query || '%') OR (sourceDescription IS NOT NULL AND sourceDescription LIKE '%' || :query || '%')) ORDER BY eventTimestampEpochMs DESC")
    fun searchEntries(query: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 AND categoryId = :categoryId ORDER BY eventTimestampEpochMs DESC")
    fun getByCategoryId(categoryId: String): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE isDeleted = 0")
    fun getEventCount(): Int
}

@Dao
interface FrugalityGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(goal: FrugalityGoalEntity)

    @Update
    fun update(goal: FrugalityGoalEntity)

    @Query("SELECT * FROM frugality_goals WHERE id = :id AND isDeleted = 0")
    fun getById(id: String): FrugalityGoalEntity?

    @Query("SELECT * FROM frugality_goals WHERE isDeleted = 0 ORDER BY createdAtEpochMs DESC")
    fun getAllActive(): List<FrugalityGoalEntity>

    @Query("SELECT * FROM frugality_goals WHERE isDeleted = 0 ORDER BY createdAtEpochMs DESC")
    fun observeAllActive(): Flow<List<FrugalityGoalEntity>>

    @Query("UPDATE frugality_goals SET isDeleted = 1, updatedAtEpochMs = :timestamp WHERE id = :id")
    fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())
}
