package net.alexandroid.locationinbackground.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import net.alexandroid.locationinbackground.utils.NotificationsUtils
import net.alexandroid.locationinbackground.utils.logD

// adb shell am start-foreground-service net.alexandroid.locationinbackground/.service.LocationService
// adb shell am stopservice net.alexandroid.locationinbackground/.service.LocationService
class LocationService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationsUtils.createForegroundNotification("Test", this)
        startForeground(NotificationsUtils.NOTIFICATION_ID, notification);
        requestLocationUpdates()
        return START_STICKY
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest()
        request.interval = 10000
        request.fastestInterval = 5000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val client: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED) { // Request location updates and when an update is
            logD()
            // received, store the location in Firebase
            client.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location: Location? = locationResult.lastLocation
                    if (location != null) {
                        logD("Location latitude: ${location.latitude}, longitude: ${location.longitude}")
                    }
                }
            }, null)
        }
    }
}