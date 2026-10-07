package com.aistudio.moneydiary.mndytr.domain.service

import android.content.Context
import androidx.room.Room
import com.aistudio.moneydiary.mndytr.BuildConfig
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.data.local.entity.AppSettingEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.AuditLogEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.FrugalityGoalEntity
import com.aistudio.moneydiary.mndytr.data.local.entity.TransactionEntity
import com.aistudio.moneydiary.mndytr.domain.model.BackupSummary
import com.aistudio.moneydiary.mndytr.domain.model.RestoreResult
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.UUID

object LocalBackupRestoreService {

    private const val FORMAT_IDENTIFIER = "KORI_BACKUP_V1"
    private const val BACKUP_FORMAT_VERSION = 1
    private const val MAX_BACKUP_SIZE_BYTES = 5 * 1024 * 1024 // 5MB limit for text-based backup

    private val SENSITIVE_SETTING_KEYS = setOf(
        "app_pin",
        "pin_hash",
        "password_hash",
        "salt",
        "recovery_key",
        "biometric_secret_id",
        "auth_token"
    )

    private fun calculateSha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun exportBackup(context: Context, db: AppDatabase, includeNotes: Boolean): String {
        val payloadObj = JSONObject()

        // 1. Fetch data
        val categories = db.categoryDao().getAll()
        val transactions = db.ledgerEventDao().getAllNonDeleted()
        val goals = db.frugalityGoalDao().getAllActive()
        val settings = db.appSettingDao().getAll()

        // 2. Serialize categories
        val catsArray = JSONArray()
        for (cat in categories) {
            val obj = JSONObject()
            obj.put("id", cat.id)
            obj.put("nameBn", cat.nameBn)
            obj.put("nameEn", cat.nameEn)
            obj.put("iconName", cat.iconName)
            obj.put("colorHex", cat.colorHex)
            obj.put("isIncomeCategory", cat.isIncomeCategory)
            obj.put("isArchived", cat.isArchived)
            obj.put("displayOrder", cat.displayOrder)
            catsArray.put(obj)
        }
        payloadObj.put("categories", catsArray)

        // 3. Serialize transactions
        val txsArray = JSONArray()
        for (tx in transactions) {
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("eventCode", tx.eventCode)
            obj.put("eventKind", tx.eventKind)
            obj.put("amountPaisa", tx.amountPaisa)
            obj.put("sourceDescription", tx.sourceDescription ?: "")
            obj.put("necessity", tx.necessity)
            obj.put("categoryId", tx.categoryId ?: "")
            obj.put("eventTimestampEpochMs", tx.eventTimestampEpochMs)
            obj.put("description", tx.description)
            if (includeNotes) {
                obj.put("notes", tx.notes ?: "")
            }
            txsArray.put(obj)
        }
        payloadObj.put("transactions", txsArray)

        // 4. Serialize goals
        val goalsArray = JSONArray()
        for (goal in goals) {
            val obj = JSONObject()
            obj.put("id", goal.id)
            obj.put("title", goal.title)
            obj.put("goalType", goal.goalType)
            obj.put("categoryId", goal.categoryId ?: "")
            obj.put("targetAmountPaisa", goal.targetAmountPaisa)
            obj.put("periodType", goal.periodType)
            obj.put("status", goal.status)
            if (includeNotes) {
                obj.put("notes", goal.notes ?: "")
            }
            goalsArray.put(obj)
        }
        payloadObj.put("goals", goalsArray)

        // 5. Serialize non-sensitive settings
        val settingsArray = JSONArray()
        for (setting in settings) {
            if (setting.key !in SENSITIVE_SETTING_KEYS) {
                val obj = JSONObject()
                obj.put("key", setting.key)
                obj.put("value", setting.value)
                settingsArray.put(obj)
            }
        }
        payloadObj.put("settings", settingsArray)

        // 6. Wrap in Envelope
        val payloadStr = payloadObj.toString()
        val checksum = calculateSha256(payloadStr)

        val envelope = JSONObject()
        envelope.put("format_identifier", FORMAT_IDENTIFIER)
        envelope.put("format_version", BACKUP_FORMAT_VERSION)
        envelope.put("schema_version", db.openHelper.readableDatabase.version)
        envelope.put("app_version_code", BuildConfig.VERSION_CODE)
        envelope.put("app_version_name", BuildConfig.VERSION_NAME)
        envelope.put("created_timestamp", System.currentTimeMillis())
        envelope.put("entry_count", transactions.size)
        envelope.put("category_count", categories.size)
        envelope.put("goal_count", goals.size)
        envelope.put("includes_notes", includeNotes)
        envelope.put("preferences_present", settingsArray.length() > 0)
        envelope.put("payload", payloadStr)
        envelope.put("checksum", checksum)

        return envelope.toString(2)
    }

    fun parseAndPreviewBackup(context: Context, db: AppDatabase, jsonString: String): BackupSummary? {
        try {
            if (jsonString.isBlank()) return null
            if (jsonString.length > MAX_BACKUP_SIZE_BYTES) return null

            val envelope = JSONObject(jsonString)
            
            // Format & Version checks
            if (envelope.optString("format_identifier") != FORMAT_IDENTIFIER) return null
            val ver = envelope.optInt("format_version")
            if (ver <= 0 || ver > BACKUP_FORMAT_VERSION) return null
            
            // Required fields presence
            if (!envelope.has("payload") || !envelope.has("checksum")) return null

            val payloadStr = envelope.getString("payload")
            val originalChecksum = envelope.getString("checksum")
            
            // Checksum verification
            if (calculateSha256(payloadStr) != originalChecksum) return null

            val payload = JSONObject(payloadStr)
            val txsArray = payload.getJSONArray("transactions")
            val catsArray = payload.getJSONArray("categories")
            val goalsArray = payload.getJSONArray("goals")
            
            // Validate Record IDs (no duplicates)
            val allIds = mutableSetOf<String>()
            
            var incomeCount = 0
            var expenseCount = 0
            var minTimestamp = Long.MAX_VALUE
            var maxTimestamp = Long.MIN_VALUE

            for (i in 0 until txsArray.length()) {
                val tx = txsArray.getJSONObject(i)
                val id = tx.getString("id")
                if (!allIds.add(id)) return null // Duplicate ID
                
                val amount = tx.getLong("amountPaisa")
                if (amount < 0) return null // Invalid monetary value
                
                val kind = tx.getString("eventKind")
                if (kind == "INCOME") incomeCount++ else expenseCount++
                
                val ts = tx.getLong("eventTimestampEpochMs")
                if (ts < minTimestamp) minTimestamp = ts
                if (ts > maxTimestamp) maxTimestamp = ts
            }

            for (i in 0 until catsArray.length()) {
                val id = catsArray.getJSONObject(i).getString("id")
                if (!allIds.add(id)) return null
            }
            
            for (i in 0 until goalsArray.length()) {
                val id = goalsArray.getJSONObject(i).getString("id")
                if (!allIds.add(id)) return null
            }

            val currentCount = db.ledgerEventDao().getAllNonDeleted().size

            return BackupSummary(
                createdTimestamp = envelope.getLong("created_timestamp"),
                appVersionName = envelope.getString("app_version_name"),
                roomSchemaVersion = envelope.getInt("schema_version"),
                entryCount = envelope.getInt("entry_count"),
                incomeCount = incomeCount,
                expenseCount = expenseCount,
                categoryCount = envelope.getInt("category_count"),
                goalCount = envelope.getInt("goal_count"),
                includesNotes = envelope.optBoolean("includes_notes", false),
                dateRangeStart = if (txsArray.length() > 0) minTimestamp else null,
                dateRangeEnd = if (txsArray.length() > 0) maxTimestamp else null,
                currentLocalRecordCount = currentCount,
                rawJson = jsonString
            )
        } catch (e: Exception) {
            return null
        }
    }

    fun executeSafeRestore(context: Context, currentDb: AppDatabase, jsonString: String): RestoreResult {
        try {
            val envelope = JSONObject(jsonString)
            val payloadStr = envelope.getString("payload")
            val payload = JSONObject(payloadStr)

            // 1. Temporary Staging Import & Validation
            val stagingDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()

            try {
                // Isolated Staging Pass
                try {
                    stagingDb.runInTransaction {
                        val catsArray = payload.getJSONArray("categories")
                        for (i in 0 until catsArray.length()) {
                            stagingDb.categoryDao().insert(parseCategory(catsArray.getJSONObject(i)))
                        }

                        val txsArray = payload.getJSONArray("transactions")
                        for (i in 0 until txsArray.length()) {
                            stagingDb.ledgerEventDao().insert(parseTransaction(txsArray.getJSONObject(i)))
                        }

                        val goalsArray = payload.getJSONArray("goals")
                        for (i in 0 until goalsArray.length()) {
                            stagingDb.frugalityGoalDao().insert(parseGoal(goalsArray.getJSONObject(i)))
                        }

                        val settingsArray = payload.getJSONArray("settings")
                        for (i in 0 until settingsArray.length()) {
                            val obj = settingsArray.getJSONObject(i)
                            stagingDb.appSettingDao().set(AppSettingEntity(obj.getString("key"), obj.getString("value")))
                        }
                    }
                } catch (e: Exception) {
                    return RestoreResult.Failure("ডেটা ইম্পোর্ট ব্যর্থ: ${e.localizedMessage}", "JSON_IMPORT_FAILED")
                }

                // 2. Comprehensive Validation
                val stagedTxs = stagingDb.ledgerEventDao().getAllNonDeleted()
                val stagedCats = stagingDb.categoryDao().getAll()
                val stagedGoals = stagingDb.frugalityGoalDao().getAllActive()
                
                if (stagedTxs.size != envelope.getInt("entry_count")) {
                    return RestoreResult.Failure("রেকর্ড সংখ্যা মেলেনি।", "ENTRY_COUNT_MISMATCH")
                }

                // Foreign Key Check
                val catIds = stagedCats.map { it.id }.toSet()
                for (tx in stagedTxs) {
                    if (tx.categoryId != null && tx.categoryId !in catIds) {
                        return RestoreResult.Failure("অজানা ক্যাটাগরি রেফারেন্স পাওয়া গেছে।", "FOREIGN_KEY_VIOLATION_TX_${tx.id}")
                    }
                }
                for (goal in stagedGoals) {
                    if (goal.categoryId != null && goal.categoryId !in catIds) {
                         return RestoreResult.Failure("লক্ষ্যের জন্য অজানা ক্যাটাগরি পাওয়া গেছে।", "FOREIGN_KEY_VIOLATION_GOAL_${goal.id}")
                    }
                }

                // 3. Create Safety Backup
                val currentDbPath = context.getDatabasePath(AppDatabase.DATABASE_NAME)
                val safetyBackupFile = File(context.filesDir, "kori_safety_backup_before_restore.db")
                try {
                    if (currentDbPath.exists()) {
                        currentDbPath.copyTo(safetyBackupFile, overwrite = true)
                    }
                } catch (e: Exception) {
                    return RestoreResult.Failure("সিস্টেম ব্যাকআপ নিতে ব্যর্থ।", "SAFETY_BACKUP_FAILED")
                }

                // 4. Atomic Replacement Transaction
                try {
                    currentDb.runInTransaction {
                        currentDb.clearAllTables()
                        
                        stagedCats.forEach { currentDb.categoryDao().insert(it) }
                        stagedTxs.forEach { currentDb.ledgerEventDao().insert(it) }
                        stagedGoals.forEach { currentDb.frugalityGoalDao().insert(it) }
                        
                        stagingDb.appSettingDao().getAll().forEach { currentDb.appSettingDao().set(it) }

                        // Verification Audit (No private payload)
                        currentDb.auditDao().insert(
                            AuditLogEntity(
                                entityName = "SYSTEM",
                                entityId = "RESTORE",
                                actionType = "RESTORE_SUCCESS",
                                newStateJson = "AppVersion: ${envelope.getString("app_version_name")}, Schema: ${envelope.getInt("schema_version")}, Entries: ${stagedTxs.size}",
                                timestampEpochMs = System.currentTimeMillis()
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Transaction failed - existing data should be preserved by Room
                    // but we also have our manual safety backup if needed.
                    return RestoreResult.Failure("ডেটাবেস আপডেট ব্যর্থ হয়েছে।", "TRANSACTION_FAILURE: ${e.localizedMessage}")
                }

                return RestoreResult.Success

            } finally {
                stagingDb.close()
            }

        } catch (e: Exception) {
            return RestoreResult.Failure("একটি অপ্রত্যাশিত ত্রুটি ঘটেছে।", e.message ?: "UNKNOWN")
        }
    }

    private fun parseCategory(obj: JSONObject) = CategoryEntity(
        id = obj.getString("id"),
        nameBn = obj.getString("nameBn"),
        nameEn = obj.getString("nameEn"),
        iconName = obj.getString("iconName"),
        colorHex = obj.getString("colorHex"),
        isIncomeCategory = obj.getBoolean("isIncomeCategory"),
        isArchived = obj.optBoolean("isArchived", false),
        displayOrder = obj.optInt("displayOrder", 0)
    )

    private fun parseTransaction(obj: JSONObject) = TransactionEntity(
        id = obj.getString("id"),
        eventCode = obj.optString("eventCode", "TXN"),
        eventKind = obj.getString("eventKind"),
        amountPaisa = obj.getLong("amountPaisa"),
        sourceDescription = obj.optString("sourceDescription").takeIf { it.isNotEmpty() },
        necessity = obj.optString("necessity", "অনির্ধারিত"),
        categoryId = obj.optString("categoryId").takeIf { it.isNotEmpty() },
        eventTimestampEpochMs = obj.getLong("eventTimestampEpochMs"),
        description = obj.getString("description"),
        notes = obj.optString("notes").takeIf { it.isNotEmpty() }
    )

    private fun parseGoal(obj: JSONObject) = FrugalityGoalEntity(
        id = obj.getString("id"),
        title = obj.getString("title"),
        goalType = obj.getString("goalType"),
        categoryId = obj.optString("categoryId").takeIf { it.isNotEmpty() },
        targetAmountPaisa = obj.getLong("targetAmountPaisa"),
        periodType = obj.getString("periodType"),
        status = obj.getString("status"),
        notes = obj.optString("notes").takeIf { it.isNotEmpty() },
        createdAtEpochMs = obj.optLong("createdAtEpochMs", System.currentTimeMillis()),
        updatedAtEpochMs = obj.optLong("updatedAtEpochMs", System.currentTimeMillis())
    )
}

