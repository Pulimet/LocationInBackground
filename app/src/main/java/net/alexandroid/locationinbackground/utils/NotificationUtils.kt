package net.alexandroid.locationinbackground.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import net.alexandroid.locationinbackground.R


object NotificationsUtils {
    @JvmField
    val CHANNEL_NAME: CharSequence = "Name of channel - Notifications"

    @JvmField
    val NOTIFICATION_TITLE: CharSequence = "Notification title"

    private const val CHANNEL_DESCRIPTION = "Channel description"
    private const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
    const val NOTIFICATION_ID = 1

    fun createForegroundNotification(message: String, context: Context, cancellationIntent: PendingIntent? = null): Notification {
        makeChannelIfNecessary(context)

        // Create the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE) // Don't wait 10 sec. before shown
            .setVibrate(LongArray(0))
            .setOngoing(true) // Prevent dismissing

        cancellationIntent?.let {
            builder.addAction(android.R.drawable.ic_delete, "Cancel", it)
        }
        return builder.build()
    }

    private fun makeChannelIfNecessary(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.description = CHANNEL_DESCRIPTION
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            notificationManager?.createNotificationChannel(channel)
        }
    }
}