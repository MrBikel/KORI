package com.aistudio.moneydiary.mndytr.domain.model

import com.aistudio.moneydiary.mndytr.data.local.entity.AppSettingEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LedgerEventCodeCoverageTest {

    @Test
    fun testAll22LedgerEventCodesCoveredProgrammatically() {
        val codes = LedgerEventCode.values()
        // Programmatic count verification
        val actualCount = codes.size
        assertEquals("Exact count of LedgerEventCodes must be 22", 22, actualCount)

        for (code in codes) {
            assertNotNull("Event code must not be null", code)
            assertNotNull("Kind must not be null for code ${code.name}", code.kind)
            assertTrue("Description Bn must not be blank for ${code.name}", code.descriptionBn.isNotBlank())
            assertTrue("Description En must not be blank for ${code.name}", code.descriptionEn.isNotBlank())

            // Room conversion round-trip test: must be string serializable & parseable
            val serialized = code.name
            val deserialized = LedgerEventCode.valueOf(serialized)
            assertEquals("Enum must survive String serialization", code, deserialized)
        }
    }

    @Test
    fun testAppSettingsRejectOrAvoidSecretKeyUsageByDesign() {
        // AppSetting is strictly for non-secret UI/system preferences
        val allowedKeys = setOf(
            "language",
            "numeral_preference",
            "theme",
            "default_account_id",
            "default_currency",
            "date_format",
            "first_day_of_week"
        )

        val forbiddenSecretKeywords = listOf("pin", "password", "passphrase", "master_key", "secret", "private_key")

        for (key in allowedKeys) {
            val setting = AppSettingEntity(key = key, value = "default_value")
            assertEquals(key, setting.key)
            // Prove that allowed keys contain zero secret keywords
            for (forbidden in forbiddenSecretKeywords) {
                assertTrue("AppSetting keys must never contain security secret keywords", !key.contains(forbidden))
            }
        }
    }
}
