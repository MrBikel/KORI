package com.aistudio.moneydiary.mndytr.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aistudio.moneydiary.mndytr.data.local.dao.AccountDao
import com.aistudio.moneydiary.mndytr.data.local.dao.AppSettingDao
import com.aistudio.moneydiary.mndytr.data.local.dao.AttachmentDao
import com.aistudio.moneydiary.mndytr.data.local.dao.AuditDao
import com.aistudio.moneydiary.mndytr.data.local.dao.BackupJobDao
import com.aistudio.moneydiary.mndytr.data.local.dao.BackupMetadataDao
import com.aistudio.moneydiary.mndytr.data.local.dao.CategoryDao
import com.aistudio.moneydiary.mndytr.data.local.dao.FrequentShortcutDao
import com.aistudio.moneydiary.mndytr.data.local.dao.FrugalityGoalDao
import com.aistudio.moneydiary.mndytr.data.local.dao.LedgerEventDao
import com.aistudio.moneydiary.mndytr.data.local.dao.PayableDao
import com.aistudio.moneydiary.mndytr.data.local.dao.PersonDao
import com.aistudio.moneydiary.mndytr.data.local.dao.ReceivableDao
import com.aistudio.moneydiary.mndytr.data.local.dao.SavingsGoalDao
import com.aistudio.moneydiary.mndytr.data.local.dao.WorkDao
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
        FrugalityGoalEntity::class
    ],
    version = 2,
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

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
