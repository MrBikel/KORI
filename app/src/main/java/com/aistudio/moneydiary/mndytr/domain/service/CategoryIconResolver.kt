package com.aistudio.moneydiary.mndytr.domain.service

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconInfo(
    val key: String,
    val icon: ImageVector,
    val labelBn: String
)

object CategoryIconResolver {

    const val NEUTRAL_FALLBACK_KEY = "category"
    val NEUTRAL_FALLBACK_ICON: ImageVector = Icons.Default.Category

    val ALL_AVAILABLE_ICONS: List<CategoryIconInfo> = listOf(
        CategoryIconInfo("home", Icons.Default.Home, "বাড়ি / বাসা"),
        CategoryIconInfo("shopping_basket", Icons.Default.ShoppingCart, "বাজার / কেনাকাটা"),
        CategoryIconInfo("medication", Icons.Default.LocalHospital, "ওষুধ"),
        CategoryIconInfo("local_hospital", Icons.Default.Healing, "চিকিৎসা"),
        CategoryIconInfo("bolt", Icons.Default.Bolt, "বিদ্যুৎ / বিদ্যুৎ বিল"),
        CategoryIconInfo("local_fire_department", Icons.Default.LocalFireDepartment, "গ্যাস / গ্যাস বিল"),
        CategoryIconInfo("water_drop", Icons.Default.WaterDrop, "পানি"),
        CategoryIconInfo("wifi", Icons.Default.Wifi, "Wi-Fi / ইন্টারনেট"),
        CategoryIconInfo("phone_android", Icons.Default.PhoneAndroid, "মোবাইল / রিচার্জ"),
        CategoryIconInfo("directions_bus", Icons.Default.DirectionsBus, "যাতায়াত / ভ্রমণ"),
        CategoryIconInfo("restaurant", Icons.Default.Restaurant, "খাবার / রেস্তোরাঁ"),
        CategoryIconInfo("local_cafe", Icons.Default.LocalCafe, "চা / কফি"),
        CategoryIconInfo("smoking_rooms", Icons.Default.SmokingRooms, "সিগারেট"),
        CategoryIconInfo("favorite", Icons.Default.Favorite, "স্ত্রী / পরিবার"),
        CategoryIconInfo("groups", Icons.Default.Groups, "পরিবার / সামাজিক"),
        CategoryIconInfo("school", Icons.Default.School, "শিক্ষা / টিউশনি"),
        CategoryIconInfo("menu_book", Icons.AutoMirrored.Filled.MenuBook, "বই / পড়াশোনা"),
        CategoryIconInfo("card_giftcard", Icons.Default.CardGiftcard, "উপহার / সালামি"),
        CategoryIconInfo("checkroom", Icons.Default.Checkroom, "পোশাক / জামাকাপড়"),
        CategoryIconInfo("movie", Icons.Default.Movie, "বিনোদন / সিনেমা"),
        CategoryIconInfo("subscriptions", Icons.Default.Subscriptions, "সাবস্ক্রিপশন"),
        CategoryIconInfo("receipt_long", Icons.AutoMirrored.Filled.ReceiptLong, "ঋণ পরিশোধ / বিল"),
        CategoryIconInfo("payments", Icons.Default.Payments, "বেতন / সম্মানী"),
        CategoryIconInfo("laptop", Icons.Default.Computer, "অনলাইন কাজ / ফ্রিল্যান্সিং"),
        CategoryIconInfo("edit", Icons.Default.Edit, "লেখালেখি / কনটেন্ট"),
        CategoryIconInfo("auto_fix_high", Icons.Default.AutoFixHigh, "সম্পাদনা / ডিজাইন"),
        CategoryIconInfo("store", Icons.Default.Store, "ব্যবসা / দোকান"),
        CategoryIconInfo("price_check", Icons.Default.PriceCheck, "পাওনা আদায়"),
        CategoryIconInfo("assignment_turned_in", Icons.Default.AssignmentTurnedIn, "কাজের অগ্রিম"),
        CategoryIconInfo("savings", Icons.Default.Savings, "সঞ্চয় / আয়"),
        CategoryIconInfo("category", Icons.Default.Category, "সাধারণ / অন্যান্য")
    )

    private val KEY_TO_ICON: Map<String, ImageVector> = ALL_AVAILABLE_ICONS.associate { it.key to it.icon }

    fun getIconByKey(key: String): ImageVector {
        return KEY_TO_ICON[key] ?: NEUTRAL_FALLBACK_ICON
    }

    /**
     * Deterministic offline category icon suggestion based on Bengali/English keywords.
     */
    fun suggestIconKey(categoryName: String): String {
        val normalized = normalizeText(categoryName)
        if (normalized.isBlank()) return NEUTRAL_FALLBACK_KEY

        return when {
            // Housing / Rent
            normalized.containsAny("বাড়ি", "ভাড়া", "বাসা", "ঘর", "rent", "home", "house", "flat") -> "home"

            // Medicine & Health
            normalized.containsAny("ওষুধ", "ঔষধ", "ট্যাবলেট", "ফার্মেসি", "medicine", "drug", "pharmacy") -> "medication"
            normalized.containsAny("চিকিৎসা", "ডাক্তার", "হাসপাতাল", "ক্লিনিক", "রোগ", "health", "hospital", "doctor", "medical") -> "local_hospital"

            // Utilities
            normalized.containsAny("বিদ্যুৎ", "কারেন্ট", "electricity", "electric", "current", "power") -> "bolt"
            normalized.containsAny("গ্যাস", "সিলিন্ডার", "gas") -> "local_fire_department"
            normalized.containsAny("পানি", "ওয়াসা", "water") -> "water_drop"
            normalized.containsAny("ওয়াইফাই", "ওয়াইফাই", "নেট", "wifi", "internet", "broadband") -> "wifi"
            normalized.containsAny("মোবাইল", "ফোন", "রিচার্জ", "mobile", "phone", "recharge", "airtime") -> "phone_android"

            // Commute / Transport
            normalized.containsAny("যাতায়াত", "যাতায়াত", "রিকশা", "বাস", "ভ্রমণ", "গাড়ি", "ট্রেন", "সিএনজি", "তেল", "commute", "transport", "bus", "rickshaw", "travel", "uber", "pathao") -> "directions_bus"

            // Food & Drinks
            normalized.containsAny("চা", "কফি", "tea", "coffee") -> "local_cafe"
            normalized.containsAny("সিগারেট", "ধূমপান", "বিড়ি", "cigarette", "smoke") -> "smoking_rooms"
            normalized.containsAny("খাবার", "নাস্তা", "হোটেল", "রেস্তোরাঁ", "দুপুর", "রাত", "সকাল", "food", "meal", "restaurant", "dining", "lunch", "dinner", "breakfast") -> "restaurant"
            normalized.containsAny("বাজার", "সওদা", "মুদি", "গ্রোসারি", "groceries", "bazaar", "bazar", "market", "shopping") -> "shopping_basket"

            // Family & Spouse
            normalized.containsAny("স্ত্রী", "বউ", "wife", "spouse") -> "favorite"
            normalized.containsAny("পরিবার", "মা", "বাবা", "ভাই", "বোন", "ছেলে", "মেয়ে", "সন্তান", "family", "parents", "children") -> "groups"

            // Education & Books
            normalized.containsAny("শিক্ষা", "টিউশনি", "স্কুল", "কলেজ", "ভার্সিটি", "ফি", "education", "tuition", "school", "college") -> "school"
            normalized.containsAny("বই", "খাতা", "কলম", "লাইব্রেরি", "book", "stationery") -> "menu_book"

            // Clothing & Entertainment
            normalized.containsAny("পোশাক", "কাপড়", "জামা", "জুতো", "clothes", "clothing", "dress", "fashion") -> "checkroom"
            normalized.containsAny("বিনোদন", "সিনেমা", "মুভি", "নাটক", "movie", "entertainment", "cinema") -> "movie"
            normalized.containsAny("সাবস্ক্রিপশন", "নেটফ্লিক্স", "subscription", "netflix", "prime") -> "subscriptions"
            normalized.containsAny("উপহার", "সালামি", "উপঢৌকন", "gift", "present") -> "card_giftcard"

            // Debt & Bills
            normalized.containsAny("ঋণ", "ধার", "পরিশোধ", "কিস্তি", "debt", "loan", "installment") -> "receipt_long"

            // Income types
            normalized.containsAny("বেতন", "সম্মানী", "মাহিনা", "salary", "wage", "payroll") -> "payments"
            normalized.containsAny("অনলাইন", "ফ্রিল্যান্স", "রিমোট", "online", "freelance", "upwork", "fiverr") -> "laptop"
            normalized.containsAny("লেখালেখি", "কনটেন্ট", "আর্টিকেল", "writing", "author", "content") -> "edit"
            normalized.containsAny("সম্পাদনা", "ডিজাইন", "ভিডিও", "editing", "editor", "design") -> "auto_fix_high"
            normalized.containsAny("ব্যবসা", "দোকান", "বিক্রি", "লাভ", "business", "trade", "profit", "shop") -> "store"
            normalized.containsAny("পাওনা", "বকেয়া", "আদায়", "receivable", "collection") -> "price_check"
            normalized.containsAny("অগ্রিম", "বায়না", "advance") -> "assignment_turned_in"
            normalized.containsAny("আয়", "ইনকাম", "সঞ্চয়", "income", "earning", "savings") -> "savings"

            else -> NEUTRAL_FALLBACK_KEY
        }
    }

    private fun normalizeText(text: String): String {
        return text.trim().lowercase().replace("-", "").replace("–", "").replace("—", "").replace(" ", "")
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
}
