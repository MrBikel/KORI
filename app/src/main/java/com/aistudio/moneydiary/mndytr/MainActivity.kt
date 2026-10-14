package com.aistudio.moneydiary.mndytr

import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.aistudio.moneydiary.mndytr.data.local.database.AppDatabase
import com.aistudio.moneydiary.mndytr.domain.service.AppLockType
import com.aistudio.moneydiary.mndytr.domain.service.SecurityService
import com.aistudio.moneydiary.mndytr.ui.screens.MainAppScreen
import com.aistudio.moneydiary.mndytr.ui.theme.KoriTypographyTokens
import com.aistudio.moneydiary.mndytr.ui.theme.MyApplicationTheme
import com.aistudio.moneydiary.mndytr.ui.viewmodel.DiaryViewModel
import com.aistudio.moneydiary.mndytr.ui.viewmodel.DiaryViewModelFactory
import kotlinx.coroutines.delay

class MainActivity : FragmentActivity() {

    private lateinit var viewModel: DiaryViewModel
    private var lastPausedEpochMs: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(applicationContext)
        viewModel = ViewModelProvider(this, DiaryViewModelFactory(db))[DiaryViewModel::class.java]

        // Check for crash reports on launch only in diagnostic builds
        if (BuildConfig.IS_DIAGNOSTIC) {
            val reports = com.aistudio.moneydiary.mndytr.domain.service.DiagnosticService.getCrashReports(this)
            if (reports.isNotEmpty()) {
                viewModel.openDialog(com.aistudio.moneydiary.mndytr.ui.state.ActiveDialog.CrashRecovery)
            }
        }

        setContent {
            val state by viewModel.uiState.collectAsState()

            // Dynamic screenshot / secure window protection
            if (state.securityConfig.isScreenshotBlocked) {
                window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }

            MyApplicationTheme(
                colorIdentity = state.colorIdentity,
                themeMode = state.themeMode
            ) {
                var isColdSplashVisible by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(1200L)
                    isColdSplashVisible = false
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    MainAppScreen(
                        viewModel = viewModel,
                        onBiometricAuthenticate = { showBiometricPrompt() }
                    )

                    AnimatedVisibility(
                        visible = isColdSplashVisible,
                        enter = fadeIn(tween(200)),
                        exit = fadeOut(tween(400)),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        com.aistudio.moneydiary.mndytr.ui.components.StartupCoinAnimation()
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    viewModel.showSnackbar("বায়োমেট্রিক কাজ করছে না: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.setAppLocked(false)
                    viewModel.showSnackbar("সফলভাবে আনলক করা হয়েছে")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    viewModel.showSnackbar("বায়োমেট্রিক মেলেনি")
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("কড়ি আনলক করুন")
            .setSubtitle("ডিভাইস বায়োমেট্রিক ব্যবহার করুন")
            .setNegativeButtonText("PIN বা পাসওয়ার্ড দিন")
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            viewModel.showSnackbar("বায়োমেট্রিক চালানো যায়নি")
        }
    }

    override fun onPause() {
        super.onPause()
        lastPausedEpochMs = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        val config = SecurityService.getSecurityConfig(this)
        if (config.lockType != AppLockType.NONE && lastPausedEpochMs > 0L) {
            val elapsedSeconds = (System.currentTimeMillis() - lastPausedEpochMs) / 1000L
            if (elapsedSeconds >= config.autoLockTimeoutSeconds) {
                viewModel.setAppLocked(true)
            }
        }
    }
}
