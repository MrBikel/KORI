package com.aistudio.moneydiary.mndytr.data.local.dao

import androidx.room.*
import com.aistudio.moneydiary.mndytr.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionLineItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TransactionLineItemEntity>)

    @Query("SELECT * FROM `transaction_line_items` WHERE `transactionId` = :txId")
    suspend fun getItemsForTransaction(txId: String): List<TransactionLineItemEntity>

    @Query("DELETE FROM `transaction_line_items` WHERE `transactionId` = :txId")
    suspend fun deleteForTransaction(txId: String)
}

@Dao
interface InvoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Query("SELECT * FROM `invoices` WHERE `isDeleted` = 0 ORDER BY `issueDateEpochMs` DESC")
    fun observeAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM `invoices` WHERE `id` = :id")
    suspend fun getInvoiceById(id: String): InvoiceEntity?

    @Query("SELECT * FROM `invoice_items` WHERE `invoiceId` = :invoiceId")
    suspend fun getInvoiceItems(invoiceId: String): List<InvoiceItemEntity>

    @Query("UPDATE `invoices` SET `status` = :status, `updatedAtEpochMs` = :updatedAt WHERE `id` = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface LifePlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: LifePlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<PlanMilestoneEntity>)

    @Query("SELECT * FROM `life_plans` WHERE `isDeleted` = 0 ORDER BY `createdAtEpochMs` DESC")
    fun observeActivePlans(): Flow<List<LifePlanEntity>>

    @Query("SELECT * FROM `plan_milestones` WHERE `planId` = :planId")
    suspend fun getMilestonesForPlan(planId: String): List<PlanMilestoneEntity>

    @Query("UPDATE `life_plans` SET `progressPercentage` = :progress, `updatedAtEpochMs` = :time WHERE `id` = :id")
    suspend fun updateProgress(id: String, progress: Int, time: Long = System.currentTimeMillis())
}

@Dao
interface IdeaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: IdeaEntity)

    @Query("SELECT * FROM `ideas` WHERE `isDeleted` = 0 ORDER BY `createdAtEpochMs` DESC")
    fun observeAllIdeas(): Flow<List<IdeaEntity>>

    @Query("UPDATE `ideas` SET `stage` = :stage, `updatedAtEpochMs` = :time WHERE `id` = :id")
    suspend fun updateStage(id: String, stage: String, time: Long = System.currentTimeMillis())
}

@Dao
interface PersonalNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: PersonalNoteEntity)

    @Query("SELECT * FROM `personal_notes` WHERE `isDeleted` = 0 ORDER BY `isPinned` DESC, `updatedAtEpochMs` DESC")
    fun observeAllNotes(): Flow<List<PersonalNoteEntity>>

    @Query("UPDATE `personal_notes` SET `isPinned` = :isPinned WHERE `id` = :id")
    suspend fun togglePin(id: String, isPinned: Boolean)
}

@Dao
interface ShoppingListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingItemEntity>)

    @Query("SELECT * FROM `shopping_lists` WHERE `isDeleted` = 0 ORDER BY `createdAtEpochMs` DESC")
    fun observeShoppingLists(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM `shopping_items` WHERE `listId` = :listId")
    suspend fun getItemsForList(listId: String): List<ShoppingItemEntity>

    @Query("UPDATE `shopping_items` SET `isPurchased` = :purchased, `actualPricePaisa` = :actualPrice WHERE `id` = :itemId")
    suspend fun markPurchased(itemId: String, purchased: Boolean, actualPrice: Long)
}

@Dao
interface PersonalContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: PersonalContactEntity)

    @Query("SELECT * FROM `personal_contacts` WHERE `isDeleted` = 0 ORDER BY `name` ASC")
    fun observeAllContacts(): Flow<List<PersonalContactEntity>>
}
