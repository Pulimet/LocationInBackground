package net.alexandroid.locationinbackground

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import net.alexandroid.locationinbackground.service.LocationService
import net.alexandroid.locationinbackground.service.LocationWorker
import net.alexandroid.locationinbackground.utils.logD

class App : Application(), LifecycleEventObserver {
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                //logD("onStop event. Waiting 5 seconds.")
                //Thread.sleep(5000)
                //launch()
            }

            else -> {
                // do nothing
            }
        }
    }

    @Suppress("unused")
    private fun launch() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                logD("Trying to start a work. (API 31+)")
                val request = OneTimeWorkRequest.Builder(LocationWorker::class.java)
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()
                WorkManager.getInstance(applicationContext).enqueue(request)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                logD("Trying to start the foreground service (API 26-30")
                this.startForegroundService(Intent(this, LocationService::class.java))
            }

            else -> {
                logD("Trying to start the foreground service (Before Oreo - API 26)")
                this.startService(Intent(this, LocationService::class.java))
            }
        }
    }
}