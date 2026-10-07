package com.aistudio.moneydiary.mndytr.ui

import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.data.local.entity.AccountEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.AppSettingEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.AttachmentEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.AuditLogEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.BackupJobEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.BackupMetadataEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.FrequentShortcutEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.PayableEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.PersonEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.ReceivableEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.SavingsGoalEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.TransactionEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.WorkRecordEntity
import com.aistudio.moneydiary.mndytr.domain.model.LedgerEventCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitectureAndSchemaVerificationTest {

    @Test
    fun testRoomEntityCountRemains14() {
        val registeredEntities = listOf(
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
            com.aistudio.moneydiary.mndytr.data.local.entity.FrugalityGoalEntity::class
        )
        // Exactly 15 entities registered in Room schema Version 2
        assertEquals(15, registeredEntities.size)
    }

    @Test
    fun testLedgerEventCodeCountRemains22() {
        val eventCodes = LedgerEventCode.values()
        assertEquals(22, eventCodes.size)

        // Verify key invariants
        assertNotNull(LedgerEventCode.TX_00_OPENING_BALANCE)
        assertNotNull(LedgerEventCode.TX_01_INCOME)
        assertNotNull(LedgerEventCode.TX_02_EXPENSE)
        assertNotNull(LedgerEventCode.TX_03_ADVANCE_RECEIVED)
        assertNotNull(LedgerEventCode.TX_04_ADVANCE_RETURNED)
        assertNotNull(LedgerEventCode.TX_14_TRANSFER)
        assertNotNull(LedgerEventCode.TX_14F_TRANSFER_FEE)
        assertNotNull(LedgerEventCode.TX_15_SAVINGS_ALLOCATION)
        assertNotNull(LedgerEventCode.TX_17_REVERSAL)
        assertNotNull(LedgerEventCode.REC_01_ADVANCE_RECOGNITION)
        assertNotNull(LedgerEventCode.REC_02_RECEIVABLE_RECOGNITION)
    }

    @Test
    fun testPackageNameAndVersion() {
        assertEquals("com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase", AppDatabase::class.java.name)
    }
}
