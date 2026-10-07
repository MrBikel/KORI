package com.aistudio.moneydiary.mndytr.data.local

import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.data.local.entity.CategoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object DatabaseInitializer {

    suspend fun seedCategoriesIfEmpty(db: AppDatabase) {
        val categoryDao = db.categoryDao()
        if (categoryDao.getAll().isEmpty()) {
            val expenseCategories = listOf(
                "বাড়িভাড়া" to "home",
                "বাজার" to "shopping_basket",
                "ওষুধ" to "medication",
                "চিকিৎসা" to "local_hospital",
                "বিদ্যুৎ বিল" to "bolt",
                "গ্যাস বিল" to "local_fire_department",
                "পানি" to "water_drop",
                "Wi-Fi" to "wifi",
                "মোবাইল" to "phone_android",
                "যাতায়াত" to "directions_bus",
                "খাবার" to "restaurant",
                "চা" to "local_cafe",
                "সিগারেট" to "smoking_rooms",
                "স্ত্রী" to "favorite",
                "পরিবার" to "groups",
                "শিক্ষা" to "school",
                "উপহার" to "card_giftcard",
                "পোশাক" to "checkroom",
                "বিনোদন" to "movie",
                "সাবস্ক্রিপশন" to "subscriptions",
                "ঋণ পরিশোধ" to "receipt_long",
                "অন্যান্য" to "category"
            )

            val incomeCategories = listOf(
                "বেতন" to "payments",
                "টিউশনি" to "school",
                "অনলাইন কাজ" to "laptop",
                "লেখালেখি" to "edit",
                "সম্পাদনা" to "auto_fix_high",
                "ব্যবসা" to "store",
                "উপহার" to "card_giftcard",
                "পাওনা আদায়" to "price_check",
                "কাজের অগ্রিম" to "assignment_turned_in",
                "অন্যান্য আয়" to "savings"
            )

            val entities = mutableListOf<CategoryEntity>()
            var order = 1
            for ((name, icon) in expenseCategories) {
                entities.add(
                    CategoryEntity(
                        id = UUID.randomUUID().toString(),
                        nameBn = name,
                        nameEn = name,
                        iconName = icon,
                        colorHex = "#234E45",
                        isMicroExpense = (name == "চা" || name == "সিগারেট" || name == "যাতায়াত"),
                        isIncomeCategory = false,
                        isSystemDefault = true,
                        displayOrder = order++
                    )
                )
            }
            for ((name, icon) in incomeCategories) {
                entities.add(
                    CategoryEntity(
                        id = UUID.randomUUID().toString(),
                        nameBn = name,
                        nameEn = name,
                        iconName = icon,
                        colorHex = "#2F7556",
                        isMicroExpense = false,
                        isIncomeCategory = true,
                        isSystemDefault = true,
                        displayOrder = order++
                    )
                )
            }
            categoryDao.insertAll(entities)
        }
    }

    suspend fun seedDefaultsIfEmpty(
        db: AppDatabase,
        dispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
    ) = withContext(dispatcher) {
        // Seed Categories on first launch (no forced default shortcuts)
        seedCategoriesIfEmpty(db)
    }
}
