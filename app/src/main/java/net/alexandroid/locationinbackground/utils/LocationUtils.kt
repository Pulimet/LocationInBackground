package net.alexandroid.locationinbackground.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import net.alexandroid.locationinbackground.R

object LocationUtils {
    fun checkDeviceLocationSettings(
        activity: Activity,
        requestLocationSettingsOn: ActivityResultLauncher<IntentSenderRequest>?,
        onLocationEnabled: () -> Unit,
        resolve: Boolean = true
    ) {
        logD()
        val locationSettingsResponseTask = getLocationSettingsTask(activity)
        locationSettingsResponseTask.addOnFailureListener { exception: Exception ->
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                tryToResolveException(exception, requestLocationSettingsOn)
            } else {
                showLocationRequiredError(activity) { // If user clicks OK invoked
                    checkDeviceLocationSettings(activity, requestLocationSettingsOn, onLocationEnabled, true)
                }
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) onLocationEnabled()
        }
    }

    private fun showLocationRequiredError(activity: Activity, onOkClick: () -> Unit) {
        logD()
        val rootView: View = activity.findViewById(android.R.id.content)
        Snackbar.make(
            rootView, R.string.location_rationale, Snackbar.LENGTH_INDEFINITE
        ).setAction(android.R.string.ok) {
            onOkClick()
        }.show()
    }

    private fun tryToResolveException(
        exception: ResolvableApiException,
        requestLocationSettingsOn: ActivityResultLauncher<IntentSenderRequest>?
    ) {
        logD()
        try {
            val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
            requestLocationSettingsOn?.launch(intentSenderRequest)
        } catch (sendEx: IntentSender.SendIntentException) {
            logE("Error getting location settings resolution: " + sendEx.message)
        }
    }

    private fun getLocationSettingsTask(activity: Activity): Task<LocationSettingsResponse> {
        logD()
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(activity)
        return settingsClient.checkLocationSettings(builder.build())
    }
}