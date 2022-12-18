package net.alexandroid.locationinbackground

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import net.alexandroid.locationinbackground.service.LocationService
import net.alexandroid.locationinbackground.ui.theme.LocationInBackgroundTheme
import net.alexandroid.locationinbackground.utils.LocationUtils
import net.alexandroid.locationinbackground.utils.PermissionUtils
import net.alexandroid.locationinbackground.utils.logD
import net.alexandroid.locationinbackground.utils.logW

class MainActivity : ComponentActivity() {
    private val requestLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            PermissionUtils.printLog(permissions)
            checkForegroundPermission()
        }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            logD("isGranted: $isGranted")
        }


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
        checkForegroundPermission()
        PermissionUtils.askNotificationPermission(this) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
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

    private fun checkForegroundPermission() {
        if (!PermissionUtils.isForegroundLocationPermissionGranted(this)) {
            logW("Location permissions not granted")
            PermissionUtils.foregroundLocationPermissionCheckFlow(
                this,
                requestLocationPermissions,
                permissionGranted = { checkDeviceLocation() }
            )
            return
        }
        logD("Location permissions granted")
        checkDeviceLocation()
    }

    // TODO Not sure I need it, leaving it here for now
    private fun checkBackGroundPermission() {
        if (!PermissionUtils.isBackgroundPermissionGranted(this)) {
            logW("Location permissions not granted")
            PermissionUtils.locationPermissionCheckFlow(
                this,
                requestLocationPermissions,
                permissionGranted = { checkDeviceLocation() }
            )
            return
        }
        logD("Location permissions granted")
        checkDeviceLocation()
    }

    private fun checkDeviceLocation(resolve: Boolean = true) {
        logD()
        LocationUtils.checkDeviceLocationSettings(
            this,
            requestLocationSettingsOn,
            onLocationEnabled = { logD("Device location enabled") },
            resolve
        )
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, LocationService::class.java))
        } else {
            startService(Intent(this, LocationService::class.java))
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



