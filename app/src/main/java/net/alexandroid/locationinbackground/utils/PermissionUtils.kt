package net.alexandroid.locationinbackground.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import net.alexandroid.locationinbackground.R

object PermissionUtils {
    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    // Foreground
    fun foregroundLocationPermissionCheckFlow(
        activity: Activity,
        requestPermissions: ActivityResultLauncher<Array<String>>,
        permissionGranted: () -> Unit
    ) {
        logW("")
        when {
            isForegroundLocationPermissionGranted(activity) -> permissionGranted()
            activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ->
                showDialogWithPermissionRationale(activity, requestPermissions)

            else -> requestForegroundPermission(requestPermissions)
        }
    }

    // foreground + background
    @TargetApi(29)
    fun isBackgroundPermissionGranted(context: Context) =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || !runningQOrLater

    @TargetApi(29)
    private fun requestBackgroundPermission(requestPermissions: ActivityResultLauncher<Array<String>>) {
        logD("-")
        requestPermissions.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
    }

    fun locationPermissionCheckFlow(
        activity: Activity,
        requestPermissions: ActivityResultLauncher<Array<String>>,
        permissionGranted: () -> Unit
    ) {
        logW("")
        when {
            isBackgroundPermissionGranted(activity) -> permissionGranted()
            activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ->
                showDialogWithPermissionRationale(activity, requestPermissions)

            runningQOrLater && activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ->
                showDialogWithPermissionRationale(activity, requestPermissions, true)

            else -> requestForegroundAndBackGroundPermissions(activity, requestPermissions)
        }
    }

    private fun requestForegroundAndBackGroundPermissions(
        context: Context,
        requestPermissions: ActivityResultLauncher<Array<String>>
    ) {
        if (runningQOrLater && isForegroundLocationPermissionGranted(context)) {
            requestBackgroundPermission(requestPermissions)
        } else {
            requestForegroundPermission(requestPermissions)
        }
    }

    // Shared
    fun isForegroundLocationPermissionGranted(context: Context) =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED


    private fun requestForegroundPermission(requestPermissions: ActivityResultLauncher<Array<String>>) {
        logD("-")
        requestPermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun showDialogWithPermissionRationale(
        activity: Activity,
        requestPermissions: ActivityResultLauncher<Array<String>>,
        isBackgroundRequested: Boolean = false
    ) {
        logD()
        AlertDialog.Builder(activity).apply {
            setMessage(activity.getString(R.string.location_rationale))
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton(android.R.string.ok) { _, _ ->
                if (isBackgroundRequested) {
                    requestBackgroundPermission(requestPermissions)
                } else {
                    requestForegroundPermission(requestPermissions)
                }
            }
            create().show()
        }
    }

    fun printLog(permissions: Map<String, @JvmSuppressWildcards Boolean>) {
        logD("permissions.size: ${permissions.size}")
        permissions.forEach {
            logD("${it.key} : ${it.value}")
        }
    }

    // Notifications
    fun askNotificationPermission(activity: Activity, requestPermission: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return   // This is only necessary for API level >= 33 (TIRAMISU)
        when {
            isPushNotificationsPermissionGranted(activity) -> {}
            activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                showDialogWithPermissionRationaleNotification(activity, requestPermission)
            }

            else -> requestPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isPushNotificationsPermissionGranted(context: Context) =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    private fun showDialogWithPermissionRationaleNotification(
        context: Context,
        requestPermission: () -> Unit
    ) {
        logD()
        AlertDialog.Builder(context).apply {
            setMessage(context.getString(R.string.permission_notification_rationale))
            setNegativeButton(context.getString(R.string.no_thanks)) { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                requestPermission()
            }
            create().show()
        }
    }


}