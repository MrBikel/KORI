package com.aistudio.moneydiary.mndytr.domain.service

import android.content.Context
import android.util.Base64
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class GoogleAccountProfile(
    val email: String,
    val displayName: String?,
    val isConnected: Boolean
)

enum class AutoBackupFrequency(val labelBn: String) {
    OFF("বন্ধ"),
    WEEKLY("সাপ্তাহিক"),
    DAILY("দৈনিক")
}

data class CloudBackupMetadata(
    val backupId: String,
    val timestampEpochMs: Long,
    val sizeBytes: Long,
    val isEncrypted: Boolean
)

object GoogleCloudService {

    private const val PREFS_NAME = "kori_google_cloud_prefs"
    private const val KEY_CONNECTED = "google_connected"
    private const val KEY_EMAIL = "google_email"
    private const val KEY_DISPLAY_NAME = "google_display_name"
    private const val KEY_DRIVE_AUTHORIZED = "google_drive_authorized"
    private const val KEY_LAST_BACKUP_TIME = "google_drive_last_backup_time"
    private const val KEY_AUTO_BACKUP_FREQ = "google_drive_auto_backup_freq"
    private const val KEY_ENCRYPTION_SECRET = "kori_drive_encryption_secret"

    private const val AES_GCM_TAG_LENGTH = 128
    private const val IV_LENGTH_BYTES = 12

    fun getGoogleProfile(context: Context): GoogleAccountProfile {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isConnected = prefs.getBoolean(KEY_CONNECTED, false)
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val name = prefs.getString(KEY_DISPLAY_NAME, null)
        return GoogleAccountProfile(
            email = if (isConnected) email else "",
            displayName = if (isConnected) name else null,
            isConnected = isConnected
        )
    }

    fun setGoogleAccount(context: Context, email: String, displayName: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_CONNECTED, true)
            .putString(KEY_EMAIL, email)
            .putString(KEY_DISPLAY_NAME, displayName)
            .apply()
    }

    fun disconnectGoogleAccount(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_CONNECTED, false)
            .remove(KEY_EMAIL)
            .remove(KEY_DISPLAY_NAME)
            .putBoolean(KEY_DRIVE_AUTHORIZED, false)
            .apply()
    }

    fun isDriveAuthorized(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_CONNECTED, false) && prefs.getBoolean(KEY_DRIVE_AUTHORIZED, false)
    }

    fun setDriveAuthorized(context: Context, authorized: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DRIVE_AUTHORIZED, authorized).apply()
    }

    fun getLastBackupTimeEpochMs(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_BACKUP_TIME, 0L)
    }

    fun recordBackupSuccess(context: Context, timestamp: Long = System.currentTimeMillis()) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_BACKUP_TIME, timestamp).apply()
    }

    fun getAutoBackupFrequency(context: Context): AutoBackupFrequency {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_AUTO_BACKUP_FREQ, AutoBackupFrequency.OFF.name) ?: AutoBackupFrequency.OFF.name
        return try {
            AutoBackupFrequency.valueOf(name)
        } catch (_: Exception) {
            AutoBackupFrequency.OFF
        }
    }

    fun setAutoBackupFrequency(context: Context, freq: AutoBackupFrequency) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_AUTO_BACKUP_FREQ, freq.name).apply()
    }

    /**
     * Secures a backup payload using AES-256-GCM.
     * Enforces non-negotiable rule: Plaintext financial data is NEVER uploaded to cloud storage.
     */
    fun encryptBackupPayload(context: Context, plaintextJson: String): String? {
        return try {
            val secretKey = getOrCreateSecretKey(context) ?: return null
            val iv = ByteArray(IV_LENGTH_BYTES)
            SecureRandom().nextBytes(iv)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(AES_GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

            val ciphertext = cipher.doFinal(plaintextJson.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + ciphertext.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decrypts an AES-256-GCM encrypted backup payload.
     */
    fun decryptBackupPayload(context: Context, base64Ciphertext: String): String? {
        return try {
            val secretKey = getOrCreateSecretKey(context) ?: return null
            val combined = Base64.decode(base64Ciphertext, Base64.NO_WRAP)
            if (combined.size < IV_LENGTH_BYTES) return null

            val iv = ByteArray(IV_LENGTH_BYTES)
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES)

            val ciphertextLength = combined.size - IV_LENGTH_BYTES
            val ciphertext = ByteArray(ciphertextLength)
            System.arraycopy(combined, IV_LENGTH_BYTES, ciphertext, 0, ciphertextLength)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(AES_GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decrypted = cipher.doFinal(ciphertext)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    private fun getOrCreateSecretKey(context: Context): SecretKey? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encoded = prefs.getString(KEY_ENCRYPTION_SECRET, null)
        if (encoded != null) {
            val raw = Base64.decode(encoded, Base64.NO_WRAP)
            return SecretKeySpec(raw, "AES")
        }

        return try {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256)
            val key = keyGen.generateKey()
            val savedEncoded = Base64.encodeToString(key.encoded, Base64.NO_WRAP)
            prefs.edit().putString(KEY_ENCRYPTION_SECRET, savedEncoded).apply()
            key
        } catch (e: Exception) {
            null
        }
    }
}
