package com.aistudio.moneydiary.mndytr.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Compound or Grouped transaction line item.
 * Allows purchasing tea and cigarettes together under one parent transaction with separate amounts & categories.
 */
@Entity(
    tableName = "transaction_line_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("transactionId"),
        Index("categoryId")
    ]
)
data class TransactionLineItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val transactionId: String,
    val description: String,
    val amountPaisa: Long,
    val categoryId: String? = null,
    val notes: String? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

/**
 * Invoices for services or personal commerce.
 */
@Entity(
    tableName = "invoices",
    indices = [
        Index("invoiceNumber", unique = true),
        Index("status"),
        Index("partyId"),
        Index("isDeleted")
    ]
)
data class InvoiceEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val invoiceNumber: String,
    val issueDateEpochMs: Long,
    val dueDateEpochMs: Long? = null,
    val senderName: String,
    val senderContact: String? = null,
    val recipientName: String,
    val recipientContact: String? = null,
    val partyId: String? = null,
    val currency: String = "BDT",
    val subtotalPaisa: Long,
    val discountPaisa: Long = 0L,
    val taxPaisa: Long = 0L,
    val totalPaisa: Long,
    val status: String = "DRAFT", // DRAFT, ISSUED, PAID, CANCELLED
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("invoiceId")
    ]
)
data class InvoiceItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val invoiceId: String,
    val description: String,
    val quantity: Double = 1.0,
    val unitRatePaisa: Long,
    val totalPaisa: Long
)

/**
 * Modular Life Management: Future Planning & Goals
 */
@Entity(
    tableName = "life_plans",
    indices = [
        Index("status"),
        Index("isDeleted")
    ]
)
data class LifePlanEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val goalDescription: String? = null,
    val startDateEpochMs: Long = System.currentTimeMillis(),
    val targetDateEpochMs: Long? = null,
    val estimatedBudgetPaisa: Long = 0L,
    val progressPercentage: Int = 0,
    val status: String = "ACTIVE", // ACTIVE, BLOCKED, PAUSED, COMPLETED, CANCELLED
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "plan_milestones",
    foreignKeys = [
        ForeignKey(
            entity = LifePlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("planId"),
        Index("isCompleted")
    ]
)
data class PlanMilestoneEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val planId: String,
    val title: String,
    val targetDateEpochMs: Long? = null,
    val isCompleted: Boolean = false,
    val completedAtEpochMs: Long? = null
)

/**
 * Modular Life Management: Ideas Vault
 */
@Entity(
    tableName = "ideas",
    indices = [
        Index("stage"),
        Index("isDeleted")
    ]
)
data class IdeaEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val details: String? = null,
    val tags: String? = null, // Comma-separated tags
    val nextAction: String? = null,
    val stage: String = "IDEA", // IDEA, RESEARCH, PLANNED, ACTIVE, COMPLETED, ARCHIVED
    val isDeleted: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

/**
 * Modular Life Management: Personal Notes
 */
@Entity(
    tableName = "personal_notes",
    indices = [
        Index("isPinned"),
        Index("isArchived"),
        Index("isDeleted")
    ]
)
data class PersonalNoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val tags: String? = null,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val reminderEpochMs: Long? = null,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

/**
 * Modular Life Management: Personal Info Vault (Separately protected, non-sensitive items)
 */
@Entity(
    tableName = "vault_items",
    indices = [
        Index("category"),
        Index("isDeleted")
    ]
)
data class VaultItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val contentOrValue: String,
    val category: String = "GENERAL", // GENERAL, CONTACT, POLICY, SUBSCRIPTION, ID_RECORD
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

/**
 * Modular Life Management: Shopping & Market Lists
 */
@Entity(
    tableName = "shopping_lists",
    indices = [
        Index("isArchived"),
        Index("isDeleted")
    ]
)
data class ShoppingListEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetDateEpochMs: Long? = null,
    val totalEstimatedPaisa: Long = 0L,
    val totalActualPaisa: Long = 0L,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "shopping_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("listId"),
        Index("isPurchased")
    ]
)
data class ShoppingItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val listId: String,
    val itemName: String,
    val quantity: String? = null,
    val estimatedPricePaisa: Long = 0L,
    val actualPricePaisa: Long = 0L,
    val isPurchased: Boolean = false,
    val linkedTransactionId: String? = null,
    val priority: String = "NORMAL" // LOW, NORMAL, HIGH
)

/**
 * Modular Life Management: Contacts (Local storage)
 */
@Entity(
    tableName = "personal_contacts",
    indices = [
        Index("name"),
        Index("isDeleted")
    ]
)
data class PersonalContactEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val relationshipOrRole: String? = null,
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

/**
 * Modular Life Management: Periodic Progress Audits
 */
@Entity(
    tableName = "progress_audits",
    indices = [
        Index("auditDateEpochMs"),
        Index("isDeleted")
    ]
)
data class ProgressAuditEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val auditDateEpochMs: Long = System.currentTimeMillis(),
    val whatWasPlanned: String,
    val whatWasCompleted: String,
    val whatRemains: String,
    val blockers: String? = null,
    val costIncurredPaisa: Long = 0L,
    val nextAction: String? = null,
    val progressPercentage: Int = 0,
    val isDeleted: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
