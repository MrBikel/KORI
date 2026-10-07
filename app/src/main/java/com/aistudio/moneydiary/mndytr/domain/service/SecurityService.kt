package com.aistudio.moneydiary.mndytr.domain.service

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

enum class AppLockType {
    NONE,
    PIN,
    PASSWORD
}

enum class NotificationPrivacyLevel {
    FULL,
    HIDE_AMOUNT,
    PRIVATE_ONLY
}

data class AppSecurityConfig(
    val lockType: AppLockType = AppLockType.NONE,
    val isBiometricEnabled: Boolean = false,
    val autoLockTimeoutSeconds: Long = 60L, // Immediately(0), 30s, 60s, 300s, 900s
    val isRecentAppsPreviewHidden: Boolean = true,
    val isScreenshotBlocked: Boolean = false,
    val notificationPrivacy: NotificationPrivacyLevel = NotificationPrivacyLevel.HIDE_AMOUNT,
    val hasRecoveryKeySet: Boolean = false
)

object SecurityService {

    private const val PREFS_NAME = "kori_security_prefs"
    private const val KEY_LOCK_TYPE = "lock_type"
    private const val KEY_SALT = "credential_salt"
    private const val KEY_HASH = "credential_hash"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_AUTO_LOCK_SECONDS = "auto_lock_seconds"
    private const val KEY_HIDE_RECENTS = "hide_recents"
    private const val KEY_BLOCK_SCREENSHOT = "block_screenshot"
    private const val KEY_NOTIF_PRIVACY = "notif_privacy"
    private const val KEY_RECOVERY_HASH = "recovery_hash"
    private const val KEY_RECOVERY_SALT = "recovery_salt"
    private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
    private const val KEY_LOCKOUT_UNTIL_EPOCH = "lockout_until_epoch"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSecurityConfig(context: Context): AppSecurityConfig {
        val prefs = getPrefs(context)
        val lockTypeStr = prefs.getString(KEY_LOCK_TYPE, AppLockType.NONE.name) ?: AppLockType.NONE.name
        val lockType = try { AppLockType.valueOf(lockTypeStr) } catch (_: Exception) { AppLockType.NONE }
        val notifPrivacyStr = prefs.getString(KEY_NOTIF_PRIVACY, NotificationPrivacyLevel.HIDE_AMOUNT.name) ?: NotificationPrivacyLevel.HIDE_AMOUNT.name
        val notifPrivacy = try { NotificationPrivacyLevel.valueOf(notifPrivacyStr) } catch (_: Exception) { NotificationPrivacyLevel.HIDE_AMOUNT }

        return AppSecurityConfig(
            lockType = lockType,
            isBiometricEnabled = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false),
            autoLockTimeoutSeconds = prefs.getLong(KEY_AUTO_LOCK_SECONDS, 60L),
            isRecentAppsPreviewHidden = prefs.getBoolean(KEY_HIDE_RECENTS, true),
            isScreenshotBlocked = prefs.getBoolean(KEY_BLOCK_SCREENSHOT, false),
            notificationPrivacy = notifPrivacy,
            hasRecoveryKeySet = prefs.contains(KEY_RECOVERY_HASH)
        )
    }

    fun isAppLockActive(context: Context): Boolean {
        return getSecurityConfig(context).lockType != AppLockType.NONE
    }

    // --- Credential Derivation & Storage ---

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashCredential(credential: String, salt: String): String {
        // Multi-round slow SHA-256 derivation
        var current = (salt + credential + salt).toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        for (i in 0 until 5000) {
            md.reset()
            current = md.digest(current)
        }
        return current.joinToString("") { "%02x".format(it) }
    }

    // Weak PIN checks
    fun checkPinWeakness(pin: String): String? {
        if (pin.length != 6 || !pin.all { it.isDigit() }) return "PIN অবশ্যই ৬ অঙ্কের হতে হবে।"
        val repeating = pin.all { it == pin[0] }
        if (repeating) return "PIN-এর সব সংখ্যা একই (যেমন ${pin.substring(0, 3)}...)। এটি নিরাপদ নয়।"
        val ascending = "0123456789".contains(pin)
        val descending = "9876543210".contains(pin)
        if (ascending || descending) return "ক্রমিক সংখ্যাযুক্ত PIN (যেমন ১২৩৪৫৬) সহজেই অনুমানযোগ্য।"
        return null
    }

    fun setPin(context: Context, pin: String): Boolean {
        if (pin.length != 6 || !pin.all { it.isDigit() }) return false
        val salt = generateSalt()
        val hash = hashCredential(pin, salt)
        getPrefs(context).edit()
            .putString(KEY_LOCK_TYPE, AppLockType.PIN.name)
            .putString(KEY_SALT, salt)
            .putString(KEY_HASH, hash)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL_EPOCH, 0L)
            .apply()
        return true
    }

    fun setPassword(context: Context, password: String): Boolean {
        if (password.length < 8) return false
        val salt = generateSalt()
        val hash = hashCredential(password, salt)
        getPrefs(context).edit()
            .putString(KEY_LOCK_TYPE, AppLockType.PASSWORD.name)
            .putString(KEY_SALT, salt)
            .putString(KEY_HASH, hash)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL_EPOCH, 0L)
            .apply()
        return true
    }

    fun removeLock(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_LOCK_TYPE)
            .remove(KEY_SALT)
            .remove(KEY_HASH)
            .remove(KEY_RECOVERY_HASH)
            .remove(KEY_RECOVERY_SALT)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL_EPOCH, 0L)
            .apply()
    }

    // --- Verification with progressive delay ---

    fun getRemainingLockoutSeconds(context: Context): Long {
        val prefs = getPrefs(context)
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL_EPOCH, 0L)
        val now = System.currentTimeMillis()
        return if (lockoutUntil > now) (lockoutUntil - now) / 1000L else 0L
    }

    fun verifyCredential(context: Context, input: String): Boolean {
        val lockoutSeconds = getRemainingLockoutSeconds(context)
        if (lockoutSeconds > 0) return false

        val prefs = getPrefs(context)
        val salt = prefs.getString(KEY_SALT, null) ?: return false
        val savedHash = prefs.getString(KEY_HASH, null) ?: return false
        val inputHash = hashCredential(input, salt)

        val matches = (savedHash == inputHash)
        if (matches) {
            // Reset attempts on success
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL_EPOCH, 0L)
                .apply()
            return true
        } else {
            val failed = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, failed)
            if (failed in 6..8) {
                // 30 seconds progressive delay
                editor.putLong(KEY_LOCKOUT_UNTIL_EPOCH, System.currentTimeMillis() + 30_000L)
            } else if (failed > 8) {
                // 60 seconds progressive delay
                editor.putLong(KEY_LOCKOUT_UNTIL_EPOCH, System.currentTimeMillis() + 60_000L)
            }
            editor.apply()
            return false
        }
    }

    // --- High-entropy Recovery Key ---

    fun generateRecoveryKey(context: Context): String {
        val part1 = UUID.randomUUID().toString().substring(0, 8).uppercase()
        val part2 = UUID.randomUUID().toString().substring(0, 8).uppercase()
        val recoveryKey = "KORI-$part1-$part2"

        val salt = generateSalt()
        val hash = hashCredential(recoveryKey, salt)
        getPrefs(context).edit()
            .putString(KEY_RECOVERY_SALT, salt)
            .putString(KEY_RECOVERY_HASH, hash)
            .apply()

        return recoveryKey
    }

    fun verifyRecoveryKey(context: Context, enteredKey: String): Boolean {
        val prefs = getPrefs(context)
        val salt = prefs.getString(KEY_RECOVERY_SALT, null) ?: return false
        val savedHash = prefs.getString(KEY_RECOVERY_HASH, null) ?: return false
        val normalized = enteredKey.trim().uppercase()
        val inputHash = hashCredential(normalized, salt)

        if (savedHash == inputHash) {
            removeLock(context)
            return true
        }
        return false
    }

    // --- Configuration setters ---

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun setAutoLockTimeout(context: Context, timeoutSeconds: Long) {
        getPrefs(context).edit().putLong(KEY_AUTO_LOCK_SECONDS, timeoutSeconds).apply()
    }

    fun setRecentAppsPreviewHidden(context: Context, hidden: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_HIDE_RECENTS, hidden).apply()
    }

    fun setScreenshotBlocked(context: Context, blocked: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BLOCK_SCREENSHOT, blocked).apply()
    }

    fun setNotificationPrivacy(context: Context, level: NotificationPrivacyLevel) {
        getPrefs(context).edit().putString(KEY_NOTIF_PRIVACY, level.name).apply()
    }
}
