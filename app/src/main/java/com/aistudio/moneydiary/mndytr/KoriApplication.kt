package com.aistudio.moneydiary.mndytr

import android.app.Application
import android.content.Context
import com.aistudio.moneydiary.mndytr.domain.service.DiagnosticService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KoriApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Diagnostic capture is active only in an explicitly approved diagnostic build
        if (BuildConfig.IS_DIAGNOSTIC) {
            // Initialize Diagnostic Service
            DiagnosticService.init(this)
            
            // Install Uncaught Exception Handler
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    DiagnosticService.recordCrash(this, thread, throwable)
                } catch (e: Exception) {
                    // If diagnostic recording fails, we must not crash the crash handler
                } finally {
                    defaultHandler?.uncaughtException(thread, throwable)
                }
            }
        }
    }
}
