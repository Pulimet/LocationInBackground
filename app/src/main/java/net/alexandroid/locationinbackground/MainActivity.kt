package net.alexandroid.locationinbackground

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.alexandroid.locationinbackground.permissions.Permission
import net.alexandroid.locationinbackground.permissions.PermissionResult
import net.alexandroid.locationinbackground.permissions.Permissions
import net.alexandroid.locationinbackground.service.LocationService
import net.alexandroid.locationinbackground.ui.theme.LocationInBackgroundTheme
import net.alexandroid.locationinbackground.utils.LocationUtils
import net.alexandroid.locationinbackground.utils.logD

class MainActivity : ComponentActivity() {
    private val permissions by Permissions()

    private val requestLocationSettingsOn =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                logD("Location on")
            } else {
                logD("Location off")
                checkDeviceLocation(false)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui()
        getForegroundLocationPermission()
    }

    private fun getForegroundLocationPermission() {
        permissions.request(Permission.LOCATION_FOREGROUND) {
            logD("LOCATION_FOREGROUND Permission result: ${it.javaClass.simpleName}")
            if (it is PermissionResult.Granted) {
                getBackGroundPermission()
            }
        }
    }

    private fun getBackGroundPermission() {
        permissions.request(Permission.LOCATION_BACKGROUND) {
            logD("LOCATION_BACKGROUND Permission result: ${it.javaClass.simpleName}")
            if (it is PermissionResult.Granted) {
                getNotificationPermission()
            }
        }
    }

    private fun getNotificationPermission() {
        permissions.request(Permission.NOTIFICATIONS) {
            logD("NOTIFICATIONS Permission result: ${it.javaClass.simpleName}")
            checkDeviceLocation()
        }
    }

    private fun ui() {
        setContent {
            LocationInBackgroundTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Service")
                }
            }
        }
    }

    private fun checkDeviceLocation(resolve: Boolean = true) {
        logD()
        LocationUtils.checkDeviceLocationSettings(
            this,
            requestLocationSettingsOn,
            onLocationEnabled = {
                logD("Device location enabled")
                openButteryOptimizationSetting()
            },
            resolve
        )
    }

    private fun openButteryOptimizationSetting() {
        logD()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startActivity(Intent().apply {
                action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            })
        }
    }

    // UI
    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Button(
            modifier = modifier,
            onClick = { onBtnClick() }) {
            Text(
                text = "Start $name!",
            )
        }
    }

    private fun onBtnClick() {
        when {
            /*            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                            logD("Trying to start a work. (API 31+)")
                            val request = OneTimeWorkRequest.Builder(LocationWorker::class.java)
                                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                                .build()
                            WorkManager.getInstance(applicationContext).enqueue(request)
                        }*/

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

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        LocationInBackgroundTheme {
            Greeting("Android")
        }
    }
}



