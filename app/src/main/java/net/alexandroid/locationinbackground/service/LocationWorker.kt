package net.alexandroid.locationinbackground.service

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.alexandroid.locationinbackground.utils.LocationUtils
import net.alexandroid.locationinbackground.utils.NotificationsUtils
import net.alexandroid.locationinbackground.utils.logD

class LocationWorker(private val context: Context, workerParameters: WorkerParameters) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        logD()
        withContext(Dispatchers.Main) {
            LocationUtils.requestLocationUpdates(context)
        }
        logD("Work is done")
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun getForegroundInfo() = createForegroundInfo()

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createForegroundInfo(): ForegroundInfo {
        val cancellationIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)
        val notification = NotificationsUtils.createForegroundNotification("Test", context, cancellationIntent)
        return ForegroundInfo(NotificationsUtils.NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
    }
}