package com.aistudio.moneydiary.mndytr

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    val appNameBn = context.getString(R.string.app_name_bn)
    assertEquals("কড়ি", appNameBn)
    org.junit.Assert.assertTrue("App name should be either Kori or কড়ি", appName == "Kori" || appName == "কড়ি")
  }
}
