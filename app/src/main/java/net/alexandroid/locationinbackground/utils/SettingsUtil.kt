package net.alexandroid.locationinbackground.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity

object SettingsUtil {
    fun openButteryOptimizationSetting(context: Context) {
        logD("isIgnoringBatteryOptimizations: ${isIgnoringBatteryOptimizations(context)}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isIgnoringBatteryOptimizations(context)) {
            context.startActivity(Intent().apply {
                action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            })
        }
    }

    private fun isIgnoringBatteryOptimizations(context: Context) =
        (context.getSystemService(ComponentActivity.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(context.packageName)

}