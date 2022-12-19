package net.alexandroid.locationinbackground.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import net.alexandroid.locationinbackground.utils.LocationUtils
import net.alexandroid.locationinbackground.utils.NotificationsUtils

// Start Service => adb shell am start-foreground-service net.alexandroid.locationinbackground/.service.LocationService
// Stop Service  => adb shell am stopservice net.alexandroid.locationinbackground/.service.LocationService

// Shows alert when app tries to launch an foreground service from background:
// adb shell device_config put activity_manager default_fgs_starts_restriction_notification_enabled true

class LocationService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationsUtils.createForegroundNotification("Test", this)
        startForeground(NotificationsUtils.NOTIFICATION_ID, notification)
        LocationUtils.requestLocationUpdates(this)
        return START_STICKY
    }
}