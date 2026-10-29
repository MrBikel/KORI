package com.aistudio.moneydiary.mndytr.data.local.dao

import androidx.room.*
import com.aistudio.moneydiary.mndytr.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionLineItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TransactionLineItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSync(items: List<TransactionLineItemEntity>)

    @Query("SELECT * FROM `transaction_line_items` WHERE `transactionId` = :txId")
    suspend fun getItemsForTransaction(txId: String): List<TransactionLineItemEntity>

    @Query("SELECT * FROM `transaction_line_items` WHERE `transactionId` = :txId")
    fun getItemsForTransactionSync(txId: String): List<TransactionLineItemEntity>

    @Query("SELECT * FROM `transaction_line_items`")
    fun getAllSync(): List<TransactionLineItemEntity>

    @Query("DELETE FROM `transaction_line_items` WHERE `transactionId` = :txId")
    suspend fun deleteForTransaction(txId: String)
}

@Dao
interface InvoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInvoiceSync(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertInvoiceItemsSync(items: List<InvoiceItemEntity>)

    @Query("SELECT * FROM `invoices` WHERE `isDeleted` = 0 ORDER BY `issueDateEpochMs` DESC")
    fun observeAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM `invoices` WHERE `isDeleted` = 0 ORDER BY `issueDateEpochMs` DESC")
    fun getAllInvoicesSync(): List<InvoiceEntity>

    @Query("SELECT * FROM `invoices` WHERE `id` = :id")
    suspend fun getInvoiceById(id: String): InvoiceEntity?

    @Query("SELECT * FROM `invoices` WHERE `id` = :id")
    fun getInvoiceByIdSync(id: String): InvoiceEntity?

    @Query("SELECT * FROM `invoice_items` WHERE `invoiceId` = :invoiceId")
    suspend fun getInvoiceItems(invoiceId: String): List<InvoiceItemEntity>

    @Query("SELECT * FROM `invoice_items` WHERE `invoiceId` = :invoiceId")
    fun getInvoiceItemsSync(invoiceId: String): List<InvoiceItemEntity>

    @Query("SELECT * FROM `invoice_items`")
    fun getAllInvoiceItemsSync(): List<InvoiceItemEntity>

    @Query("UPDATE `invoices` SET `status` = :status, `updatedAtEpochMs` = :updatedAt WHERE `id` = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface LifePlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: LifePlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlanSync(plan: LifePlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<PlanMilestoneEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMilestonesSync(milestones: List<PlanMilestoneEntity>)

    @Query("SELECT * FROM `life_plans` WHERE `isDeleted` = 0 ORDER BY `createdAtEpochMs` DESC")
    fun observeActivePlans(): Flow<List<LifePlanEntity>>

    @Query("SELECT * FROM `life_plans` WHERE `isDeleted` = 0 ORDER BY `createdAtEpochMs` DESC")
    fun getAllPlansSync(): List<LifePlanEntity>

    @Query("SELECT * FROM `plan_milestones` WHERE `planId` = :planId")
    suspend fun getMilestonesForPlan(planId: String): List<PlanMilestoneEntity>

    @Query("SELECT * FROM `plan_milestones` WHERE `planId` = :planId")
    fun getMilestonesForPlanSync(planId: String): List<PlanMilestoneEntity>

    @Query("SELECT * FROM `plan_milestones`")
    fun getAllMilestonesSync(): List<PlanMilestoneEntity>

    @Query("UPDATE `life_plans` SET `progressPercentage` = :progress, `updatedAtEpochMs` = :time WHERE `id` = :id")
    suspend fun updateProgress(id: String, progress: Int, time: Long = System.currentTimeMillis())
}

@Dao
interface IdeaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: IdeaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIdeaSync(idea: IdeaEntity)

    @Query("SELECT * FROM `ideas` WHERE `isDeleted` = 0 ORDER BY `createdAtEpochMs` DESC")
    fun observeAllIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM `ideas` WHERE `isDeleted` = 0 ORDER BY `createdAtEpochMs` DESC")
    fun getAllIdeasSync(): List<IdeaEntity>

    @Query("UPDATE `ideas` SET `stage` = :stage, `updatedAtEpochMs` = :time WHERE `id` = :id")
    suspend fun updateStage(id: String, stage: String, time: Long = System.currentTimeMillis())
}

@Dao
interface PersonalNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: PersonalNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNoteSync(note: PersonalNoteEntity)

    @Query("SELECT * FROM `personal_notes` WHERE `isDeleted` = 0 ORDER BY `isPinned` DESC, `updatedAtEpochMs` DESC")
    fun observeAllNotes(): Flow<List<PersonalNoteEntity>>

    @Query("SELECT * FROM `personal_notes` WHERE `isDeleted` = 0 ORDER BY `isPinned` DESC, `updatedAtEpochMs` DESC")
    fun getAllNotesSync(): List<PersonalNoteEntity>

    @Query("UPDATE `personal_notes` SET `isPinned` = :isPinned WHERE `id` = :id")
    suspend fun togglePin(id: String, isPinned: Boolean)
}

@Dao
interface ShoppingListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertListSync(list: ShoppingListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItemsSync(items: List<ShoppingItemEntity>)

    @Query("SELECT * FROM `shopping_lists` WHERE `isDeleted` = 0 ORDER BY `createdAtEpochMs` DESC")
    fun observeShoppingLists(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM `shopping_lists` WHERE `isDeleted` = 0 ORDER BY `createdAtEpochMs` DESC")
    fun getAllListsSync(): List<ShoppingListEntity>

    @Query("SELECT * FROM `shopping_items` WHERE `listId` = :listId")
    suspend fun getItemsForList(listId: String): List<ShoppingItemEntity>

    @Query("SELECT * FROM `shopping_items` WHERE `listId` = :listId")
    fun getItemsForListSync(listId: String): List<ShoppingItemEntity>

    @Query("SELECT * FROM `shopping_items`")
    fun getAllShoppingItemsSync(): List<ShoppingItemEntity>

    @Query("UPDATE `shopping_items` SET `isPurchased` = :purchased, `actualPricePaisa` = :actualPrice WHERE `id` = :itemId")
    suspend fun markPurchased(itemId: String, purchased: Boolean, actualPrice: Long)

    @Query("UPDATE `shopping_items` SET `isPurchased` = :purchased, `actualPricePaisa` = :actualPrice WHERE `id` = :itemId")
    fun markPurchasedSync(itemId: String, purchased: Boolean, actualPrice: Long)
}

@Dao
interface PersonalContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: PersonalContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContactSync(contact: PersonalContactEntity)

    @Query("SELECT * FROM `personal_contacts` WHERE `isDeleted` = 0 ORDER BY `name` ASC")
    fun observeAllContacts(): Flow<List<PersonalContactEntity>>

    @Query("SELECT * FROM `personal_contacts` WHERE `isDeleted` = 0 ORDER BY `name` ASC")
    fun getAllContactsSync(): List<PersonalContactEntity>
}

@Dao
interface VaultItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VaultItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(item: VaultItemEntity)

    @Query("SELECT * FROM `vault_items` WHERE `isDeleted` = 0 ORDER BY `createdAtEpochMs` DESC")
    fun observeAll(): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM `vault_items` WHERE `isDeleted` = 0 ORDER BY `createdAtEpochMs` DESC")
    fun getAllSync(): List<VaultItemEntity>

    @Query("UPDATE `vault_items` SET `isDeleted` = 1, `updatedAtEpochMs` = :time WHERE `id` = :id")
    suspend fun softDelete(id: String, time: Long = System.currentTimeMillis())
}

@Dao
interface ProgressAuditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audit: ProgressAuditEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(audit: ProgressAuditEntity)

    @Query("SELECT * FROM `progress_audits` WHERE `isDeleted` = 0 ORDER BY `auditDateEpochMs` DESC")
    fun observeAll(): Flow<List<ProgressAuditEntity>>

    @Query("SELECT * FROM `progress_audits` WHERE `isDeleted` = 0 ORDER BY `auditDateEpochMs` DESC")
    fun getAllSync(): List<ProgressAuditEntity>
}
