package com.studygrove.app.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.studygrove.app.MainActivity
import com.studygrove.app.R
import java.time.LocalDateTime
import java.time.ZoneId

private const val TimerChannelId = "study_grove_timer"
private const val ReminderChannelId = "study_grove_reminders"
private const val TimerRequestCode = 2401
private const val ReminderRequestCode = 2402

class NotificationScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        createChannels()
    }

    fun scheduleTimerComplete(triggerAtMillis: Long, enabled: Boolean) {
        cancelTimerComplete()
        if (!enabled) return
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            receiverIntent(
                requestCode = TimerRequestCode,
                action = StudyGroveNotificationReceiver.ActionTimerComplete,
                title = "Study session complete",
                text = "Nice work. Return to Study Grove to collect your reward."
            )
        )
    }

    fun cancelTimerComplete() {
        alarmManager.cancel(
            receiverIntent(
                requestCode = TimerRequestCode,
                action = StudyGroveNotificationReceiver.ActionTimerComplete,
                title = "",
                text = ""
            )
        )
    }

    fun scheduleDailyReminder(enabled: Boolean) {
        cancelDailyReminder()
        if (!enabled) return
        val trigger = LocalDateTime.now()
            .withHour(19)
            .withMinute(0)
            .withSecond(0)
            .let { if (it.isBefore(LocalDateTime.now())) it.plusDays(1) else it }
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            trigger,
            AlarmManager.INTERVAL_DAY,
            receiverIntent(
                requestCode = ReminderRequestCode,
                action = StudyGroveNotificationReceiver.ActionReminder,
                title = "Tend your Study Grove",
                text = "A small focus session can brighten the garden today."
            )
        )
    }

    fun cancelDailyReminder() {
        alarmManager.cancel(
            receiverIntent(
                requestCode = ReminderRequestCode,
                action = StudyGroveNotificationReceiver.ActionReminder,
                title = "",
                text = ""
            )
        )
    }

    private fun receiverIntent(
        requestCode: Int,
        action: String,
        title: String,
        text: String
    ): PendingIntent {
        val intent = Intent(context, StudyGroveNotificationReceiver::class.java).apply {
            this.action = action
            putExtra(StudyGroveNotificationReceiver.ExtraTitle, title)
            putExtra(StudyGroveNotificationReceiver.ExtraText, text)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                TimerChannelId,
                "Timer updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        manager.createNotificationChannel(
            NotificationChannel(
                ReminderChannelId,
                "Study reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }
}

class StudyGroveNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val action = intent.action.orEmpty()
        val channel = if (action == ActionReminder) ReminderChannelId else TimerChannelId
        val title = intent.getStringExtra(ExtraTitle).orEmpty()
        val text = intent.getStringExtra(ExtraText).orEmpty()
        val openIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(action.hashCode(), notification)
    }

    companion object {
        const val ActionTimerComplete = "com.studygrove.app.TIMER_COMPLETE"
        const val ActionReminder = "com.studygrove.app.STUDY_REMINDER"
        const val ExtraTitle = "title"
        const val ExtraText = "text"
    }
}
