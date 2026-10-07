package com.aistudio.moneydiary.mndytr.domain.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aistudio.moneydiary.mndytr.MainActivity
import com.aistudio.moneydiary.mndytr.R

object LocalNotificationHelper {

    const val CHANNEL_ID = "kori_diary_reminders"
    const val CHANNEL_NAME = "কড়ি ডায়েরি অনুস্মারক"
    const val NOTIF_ID_DAILY_REMINDER = 101
    const val NOTIF_ID_MISSING_DAY = 102
    const val NOTIF_ID_LIMIT_WARNING = 103
    const val NOTIF_ID_GOAL_PROGRESS = 104

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "কড়ি আয়-ব্যয় ডায়েরি লেখার নিয়মিত নোটিফিকেশন ও লক্ষ্য সতর্কতা"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun showDailyReminder(context: Context) {
        val title = "আজকের হিসাব লেখা হয়েছে?"
        val message = "এক মিনিটে আজকের আয়-ব্যয় লিখে রাখুন।"
        showNotification(context, NOTIF_ID_DAILY_REMINDER, title, message)
    }

    fun showWeeklyReview(context: Context, diffFormatted: String, isLess: Boolean) {
        val title = "সাপ্তাহিক হিসাব পর্যালোচনা"
        val message = if (isLess) {
            "এই সপ্তাহে গত সপ্তাহের তুলনায় $diffFormatted কম ব্যয় হয়েছে।"
        } else {
            "এই সপ্তাহে মোট ব্যয় $diffFormatted হয়েছে। ডায়েরি পর্যালোচনা করে দেখতে পারেন।"
        }
        showNotification(context, NOTIF_ID_DAILY_REMINDER + 1, title, message)
    }

    fun showLimitWarning(context: Context, categoryName: String, pctUsed: Int) {
        val title = "ব্যয়সীমা সতর্কতা"
        val message = "$categoryName খাতের নির্ধারিত সীমার $pctUsed% ব্যবহৃত হয়েছে।"
        showNotification(context, NOTIF_ID_LIMIT_WARNING, title, message)
    }

    fun showGoalProgressNotice(context: Context, goalTitle: String, message: String) {
        showNotification(context, NOTIF_ID_GOAL_PROGRESS, goalTitle, message)
    }

    private fun showNotification(context: Context, notificationId: Int, title: String, message: String) {
        try {
            createNotificationChannel(context)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Handled when notification permission is not granted on Android 13+
        } catch (e: Exception) {
            // Ignore notification delivery failures gracefully
        }
    }
}
