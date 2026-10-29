package com.aistudio.moneydiary.mndytr.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aistudio.moneydiary.mndytr.data.local.dao.*
import com.aistudio.moneydiary.mndytr.data.local.entity.*

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        PersonEntity::class,
        WorkRecordEntity::class,
        TransactionEntity::class,
        ReceivableEntity::class,
        PayableEntity::class,
        SavingsGoalEntity::class,
        FrequentShortcutEntity::class,
        AttachmentEntity::class,
        BackupJobEntity::class,
        BackupMetadataEntity::class,
        AppSettingEntity::class,
        AuditLogEntity::class,
        FrugalityGoalEntity::class,
        PartyEntity::class,
        DebtRecordEntity::class,
        DebtPaymentEntity::class,
        EarnedReceivableEntity::class,
        ReceivablePaymentEntity::class,
        TransactionLineItemEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        LifePlanEntity::class,
        PlanMilestoneEntity::class,
        IdeaEntity::class,
        PersonalNoteEntity::class,
        VaultItemEntity::class,
        ShoppingListEntity::class,
        ShoppingItemEntity::class,
        PersonalContactEntity::class,
        ProgressAuditEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun personDao(): PersonDao
    abstract fun workDao(): WorkDao
    abstract fun ledgerEventDao(): LedgerEventDao
    abstract fun receivableDao(): ReceivableDao
    abstract fun payableDao(): PayableDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun frequentShortcutDao(): FrequentShortcutDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun backupJobDao(): BackupJobDao
    abstract fun backupMetadataDao(): BackupMetadataDao
    abstract fun appSettingDao(): AppSettingDao
    abstract fun auditDao(): AuditDao
    abstract fun frugalityGoalDao(): FrugalityGoalDao
    abstract fun partyDao(): PartyDao
    abstract fun debtRecordDao(): DebtRecordDao
    abstract fun debtPaymentDao(): DebtPaymentDao
    abstract fun earnedReceivableDao(): EarnedReceivableDao
    abstract fun receivablePaymentDao(): ReceivablePaymentDao
    abstract fun transactionLineItemDao(): TransactionLineItemDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun lifePlanDao(): LifePlanDao
    abstract fun ideaDao(): IdeaDao
    abstract fun personalNoteDao(): PersonalNoteDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun personalContactDao(): PersonalContactDao
    abstract fun vaultItemDao(): VaultItemDao
    abstract fun progressAuditDao(): ProgressAuditDao

    companion object {
        const val DATABASE_NAME = "my_money_diary.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `transactions` ADD COLUMN `sourceDescription` TEXT")
                db.execSQL("ALTER TABLE `transactions` ADD COLUMN `necessity` TEXT NOT NULL DEFAULT 'অনির্ধারিত'")
                db.execSQL("ALTER TABLE `categories` ADD COLUMN `isArchived` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `frugality_goals` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `goalType` TEXT NOT NULL,
                        `categoryId` TEXT,
                        `targetAmountPaisa` INTEGER NOT NULL,
                        `targetReductionPercentage` INTEGER,
                        `periodType` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `notes` TEXT,
                        `isDeleted` INTEGER NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        `updatedAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_frugality_goals_goalType` ON `frugality_goals` (`goalType`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_frugality_goals_categoryId` ON `frugality_goals` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_frugality_goals_status` ON `frugality_goals` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_frugality_goals_isDeleted` ON `frugality_goals` (`isDeleted`)")

                // Migrate legacy account names into sourceDescription
                db.execSQL("UPDATE `transactions` SET `sourceDescription` = (SELECT `name` FROM `accounts` WHERE `accounts`.`id` = `transactions`.`sourceAccountId`) WHERE `sourceAccountId` IS NOT NULL AND `sourceDescription` IS NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Parties table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `parties` (
                        `id` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `partyType` TEXT NOT NULL,
                        `phone` TEXT,
                        `email` TEXT,
                        `note` TEXT,
                        `isArchived` INTEGER NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        `updatedAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_parties_displayName` ON `parties` (`displayName`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_parties_partyType` ON `parties` (`partyType`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_parties_isArchived` ON `parties` (`isArchived`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_parties_isDeleted` ON `parties` (`isDeleted`)")

                // 2. Debt Records table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `debt_records` (
                        `id` TEXT NOT NULL,
                        `partyId` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `principalPaisa` INTEGER NOT NULL,
                        `occurredAtEpochMs` INTEGER NOT NULL,
                        `dueAtEpochMs` INTEGER,
                        `notes` TEXT,
                        `status` TEXT NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        `updatedAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`partyId`) REFERENCES `parties`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_debt_records_partyId` ON `debt_records` (`partyId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_debt_records_type` ON `debt_records` (`type`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_debt_records_status` ON `debt_records` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_debt_records_occurredAtEpochMs` ON `debt_records` (`occurredAtEpochMs`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_debt_records_isDeleted` ON `debt_records` (`isDeleted`)")

                // 3. Debt Payments table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `debt_payments` (
                        `id` TEXT NOT NULL,
                        `debtId` TEXT NOT NULL,
                        `amountPaisa` INTEGER NOT NULL,
                        `occurredAtEpochMs` INTEGER NOT NULL,
                        `notes` TEXT,
                        `isReversed` INTEGER NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        `updatedAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`debtId`) REFERENCES `debt_records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_debt_payments_debtId` ON `debt_payments` (`debtId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_debt_payments_occurredAtEpochMs` ON `debt_payments` (`occurredAtEpochMs`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_debt_payments_isReversed` ON `debt_payments` (`isReversed`)")

                // 4. Earned Receivables table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `earned_receivables` (
                        `id` TEXT NOT NULL,
                        `partyId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `agreedAmountPaisa` INTEGER NOT NULL,
                        `occurredAtEpochMs` INTEGER NOT NULL,
                        `dueAtEpochMs` INTEGER,
                        `notes` TEXT,
                        `status` TEXT NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        `updatedAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`partyId`) REFERENCES `parties`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_earned_receivables_partyId` ON `earned_receivables` (`partyId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_earned_receivables_status` ON `earned_receivables` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_earned_receivables_occurredAtEpochMs` ON `earned_receivables` (`occurredAtEpochMs`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_earned_receivables_isDeleted` ON `earned_receivables` (`isDeleted`)")

                // 5. Receivable Payments table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `receivable_payments` (
                        `id` TEXT NOT NULL,
                        `receivableId` TEXT NOT NULL,
                        `amountPaisa` INTEGER NOT NULL,
                        `occurredAtEpochMs` INTEGER NOT NULL,
                        `notes` TEXT,
                        `isReversed` INTEGER NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        `updatedAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`receivableId`) REFERENCES `earned_receivables`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_receivable_payments_receivableId` ON `receivable_payments` (`receivableId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_receivable_payments_occurredAtEpochMs` ON `receivable_payments` (`occurredAtEpochMs`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_receivable_payments_isReversed` ON `receivable_payments` (`isReversed`)")

                // 6. Alter transactions table
                db.execSQL("ALTER TABLE `transactions` ADD COLUMN `partyId` TEXT")
                db.execSQL("ALTER TABLE `transactions` ADD COLUMN `debtRecordId` TEXT")
                db.execSQL("ALTER TABLE `transactions` ADD COLUMN `earnedReceivableId` TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_partyId` ON `transactions` (`partyId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_debtRecordId` ON `transactions` (`debtRecordId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_earnedReceivableId` ON `transactions` (`earnedReceivableId`)")

                // 7. Migrate existing persons to parties
                db.execSQL("""
                    INSERT OR IGNORE INTO `parties` (`id`, `displayName`, `partyType`, `phone`, `email`, `note`, `isArchived`, `isDeleted`, `createdAtEpochMs`, `updatedAtEpochMs`)
                    SELECT `id`, `name`, `personType`, `phoneNumber`, `email`, `notes`, 0, `isDeleted`, `createdAtEpochMs`, `updatedAtEpochMs` FROM `persons`
                """.trimIndent())

                // 8. Link existing personId on transactions to partyId
                db.execSQL("UPDATE `transactions` SET `partyId` = `personId` WHERE `personId` IS NOT NULL AND `partyId` IS NULL")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Transaction line items
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `transaction_line_items` (
                        `id` TEXT NOT NULL,
                        `transactionId` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `amountPaisa` INTEGER NOT NULL,
                        `categoryId` TEXT,
                        `notes` TEXT,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`transactionId`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_line_items_transactionId` ON `transaction_line_items` (`transactionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_line_items_categoryId` ON `transaction_line_items` (`categoryId`)")

                // 2. Invoices & Items
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `invoices` (
                        `id` TEXT NOT NULL,
                        `invoiceNumber` TEXT NOT NULL,
                        `issueDateEpochMs` INTEGER NOT NULL,
                        `dueDateEpochMs` INTEGER,
                        `senderName` TEXT NOT NULL,
                        `senderContact` TEXT,
                        `recipientName` TEXT NOT NULL,
                        `recipientContact` TEXT,
                        `partyId` TEXT,
                        `currency` TEXT NOT NULL,
                        `subtotalPaisa` INTEGER NOT NULL,
                        `discountPaisa` INTEGER NOT NULL,
                        `taxPaisa` INTEGER NOT NULL,
                        `totalPaisa` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `notes` TEXT,
                        `isDeleted` INTEGER NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        `updatedAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_invoices_invoiceNumber` ON `invoices` (`invoiceNumber`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoices_status` ON `invoices` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoices_partyId` ON `invoices` (`partyId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoices_isDeleted` ON `invoices` (`isDeleted`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `invoice_items` (
                        `id` TEXT NOT NULL,
                        `invoiceId` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `quantity` REAL NOT NULL,
                        `unitRatePaisa` INTEGER NOT NULL,
                        `totalPaisa` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`invoiceId`) REFERENCES `invoices`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_invoice_items_invoiceId` ON `invoice_items` (`invoiceId`)")

                // 3. Life Plans & Milestones
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `life_plans` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `goalDescription` TEXT,
                        `startDateEpochMs` INTEGER NOT NULL,
                        `targetDateEpochMs` INTEGER,
                        `estimatedBudgetPaisa` INTEGER NOT NULL,
                        `progressPercentage` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `notes` TEXT,
                        `isDeleted` INTEGER NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        `updatedAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_life_plans_status` ON `life_plans` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_life_plans_isDeleted` ON `life_plans` (`isDeleted`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `plan_milestones` (
                        `id` TEXT NOT NULL,
                        `planId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `targetDateEpochMs` INTEGER,
                        `isCompleted` INTEGER NOT NULL,
                        `completedAtEpochMs` INTEGER,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`planId`) REFERENCES `life_plans`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_plan_milestones_planId` ON `plan_milestones` (`planId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_plan_milestones_isCompleted` ON `plan_milestones` (`isCompleted`)")

                // 4. Ideas
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ideas` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `details` TEXT,
                        `tags` TEXT,
                        `nextAction` TEXT,
                        `stage` TEXT NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        `updatedAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ideas_stage` ON `ideas` (`stage`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ideas_isDeleted` ON `ideas` (`isDeleted`)")

                // 5. Personal Notes
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `personal_notes` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `tags` TEXT,
                        `isPinned` INTEGER NOT NULL,
                        `isArchived` INTEGER NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `reminderEpochMs` INTEGER,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        `updatedAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_notes_isPinned` ON `personal_notes` (`isPinned`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_notes_isArchived` ON `personal_notes` (`isArchived`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_notes_isDeleted` ON `personal_notes` (`isDeleted`)")

                // 6. Vault Items
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `vault_items` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `contentOrValue` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `notes` TEXT,
                        `isDeleted` INTEGER NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        `updatedAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_vault_items_category` ON `vault_items` (`category`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_vault_items_isDeleted` ON `vault_items` (`isDeleted`)")

                // 7. Shopping Lists & Items
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `shopping_lists` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `targetDateEpochMs` INTEGER,
                        `totalEstimatedPaisa` INTEGER NOT NULL,
                        `totalActualPaisa` INTEGER NOT NULL,
                        `isArchived` INTEGER NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_shopping_lists_isArchived` ON `shopping_lists` (`isArchived`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_shopping_lists_isDeleted` ON `shopping_lists` (`isDeleted`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `shopping_items` (
                        `id` TEXT NOT NULL,
                        `listId` TEXT NOT NULL,
                        `itemName` TEXT NOT NULL,
                        `quantity` TEXT,
                        `estimatedPricePaisa` INTEGER NOT NULL,
                        `actualPricePaisa` INTEGER NOT NULL,
                        `isPurchased` INTEGER NOT NULL,
                        `linkedTransactionId` TEXT,
                        `priority` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`listId`) REFERENCES `shopping_lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_shopping_items_listId` ON `shopping_items` (`listId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_shopping_items_isPurchased` ON `shopping_items` (`isPurchased`)")

                // 8. Personal Contacts
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `personal_contacts` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `phoneNumber` TEXT,
                        `email` TEXT,
                        `relationshipOrRole` TEXT,
                        `notes` TEXT,
                        `isDeleted` INTEGER NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_contacts_name` ON `personal_contacts` (`name`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_personal_contacts_isDeleted` ON `personal_contacts` (`isDeleted`)")

                // 9. Progress Audits
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `progress_audits` (
                        `id` TEXT NOT NULL,
                        `auditDateEpochMs` INTEGER NOT NULL,
                        `whatWasPlanned` TEXT NOT NULL,
                        `whatWasCompleted` TEXT NOT NULL,
                        `whatRemains` TEXT NOT NULL,
                        `blockers` TEXT,
                        `costIncurredPaisa` INTEGER NOT NULL,
                        `nextAction` TEXT,
                        `progressPercentage` INTEGER NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `createdAtEpochMs` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_progress_audits_auditDateEpochMs` ON `progress_audits` (`auditDateEpochMs`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_progress_audits_isDeleted` ON `progress_audits` (`isDeleted`)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
