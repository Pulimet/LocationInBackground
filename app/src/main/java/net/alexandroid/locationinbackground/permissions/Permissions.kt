package net.alexandroid.locationinbackground.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import net.alexandroid.locationinbackground.R
import net.alexandroid.locationinbackground.utils.logD
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@SuppressLint("InlinedApi")
enum class Permission(val value: String) {
    STORAGE(Manifest.permission.READ_EXTERNAL_STORAGE),
    LOCATION_FOREGROUND(Manifest.permission.ACCESS_FINE_LOCATION),
    LOCATION_BACKGROUND(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
    NOTIFICATIONS(Manifest.permission.POST_NOTIFICATIONS);

    companion object {
        infix fun from(value: String?): Permission? = Permission.values().firstOrNull { it.value == value }
    }
}

sealed class PermissionResult {
    object NotRequired : PermissionResult()
    object Granted : PermissionResult()
    object NotGranted : PermissionResult()
    object RequiresOtherPermission : PermissionResult()
}

class Permissions : ReadOnlyProperty<ComponentActivity, Permissions> {
    companion object {
        private const val MAX_ATTEMPTS = 3
    }

    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private val runningTiramisuOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    private var isRegistered = false
    private var isRequested = false
    private lateinit var componentActivity: ComponentActivity
    private lateinit var requestPermissions: ActivityResultLauncher<Array<String>>
    private var onPermissionGranted: ((PermissionResult) -> Unit)? = null
    private var attempts = 0

    override fun getValue(thisRef: ComponentActivity, property: KProperty<*>): Permissions {
        logD("Set attempts to be 0")
        attempts = 0
        componentActivity = thisRef
        if (!isRegistered) {
            isRegistered = true
            requestPermissions = thisRef.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions?.let { onPermissionsResult(it) }
            }
        }
        return this
    }

    private fun onPermissionsResult(permissions: Map<String, @JvmSuppressWildcards Boolean>) {
        logD()
        printLog(permissions)
        isRequested = false
        val permissionEntry = permissions.entries.first()
        val permission = Permission.from(permissionEntry.key) ?: return
        onPermissionGranted?.let {
            request(permission, it)
        }
    }

    fun request(permission: Permission, onPermissionRequestResult: (PermissionResult) -> Unit) {
        this.onPermissionGranted = onPermissionRequestResult
        logD("Attempts: $attempts, Permission: ${permission.value}")
        when {
            isNotRequiredForThisApi(permission) -> onPermissionRequestResult(PermissionResult.NotRequired)
            isRequiresOtherPermissions(permission) -> onPermissionRequestResult(PermissionResult.RequiresOtherPermission)
            isGranted(permission) -> onPermissionRequestResult(PermissionResult.Granted)
            attempts == MAX_ATTEMPTS -> onPermissionRequestResult(PermissionResult.NotGranted)
            shouldShowRationale(permission) -> showRationale(permission)
            else -> launchRequestPermission(permission)
        }
        attempts++
    }

    @SuppressLint("NewApi")
    private fun isNotRequiredForThisApi(permission: Permission) = when (permission) {
        Permission.STORAGE -> false
        Permission.LOCATION_FOREGROUND -> false
        Permission.LOCATION_BACKGROUND -> !runningQOrLater
        Permission.NOTIFICATIONS -> !runningTiramisuOrLater
    }

    private fun isRequiresOtherPermissions(permission: Permission) = when (permission) {
        Permission.LOCATION_BACKGROUND -> !isGranted(Permission.LOCATION_FOREGROUND)
        else -> false
    }


    private fun isGranted(permission: Permission) =
        ContextCompat.checkSelfPermission(componentActivity, permission.value) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowRationale(permission: Permission) =
        componentActivity.shouldShowRequestPermissionRationale(permission.value)

    private fun showRationale(permission: Permission) {
        logD(permission.value)
        AlertDialog.Builder(componentActivity, androidx.appcompat.R.style.Theme_AppCompat).apply {
            setMessage(getRationaleString(permission))
            setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            setPositiveButton(android.R.string.ok) { _, _ -> launchRequestPermission(permission) }
            create().show()
        }
    }

    private fun launchRequestPermission(permission: Permission) {
        logD()
        if (!isRequested) {
            isRequested = true
            requestPermissions.launch(arrayOf(permission.value))
        }
    }

    @SuppressLint("NewApi")
    private fun getRationaleString(permission: Permission): String =
        when (permission) {
            Permission.STORAGE -> componentActivity.getString(R.string.rationale)
            Permission.LOCATION_FOREGROUND -> componentActivity.getString(R.string.rationale)
            Permission.LOCATION_BACKGROUND -> componentActivity.getString(R.string.rationale)
            Permission.NOTIFICATIONS -> componentActivity.getString(R.string.rationale)
        }

    // Logging
    private fun printLog(permissions: Map<String, @JvmSuppressWildcards Boolean>) {
        permissions.forEach {
            logD("${it.key} : ${it.value}")
        }
    }
}