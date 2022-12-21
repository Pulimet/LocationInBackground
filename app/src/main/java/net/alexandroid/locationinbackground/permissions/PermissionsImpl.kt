package net.alexandroid.locationinbackground.permissions

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import net.alexandroid.locationinbackground.Permissions
import net.alexandroid.locationinbackground.R
import net.alexandroid.locationinbackground.utils.logD

class PermissionsImpl : Permissions {
    companion object {
        private const val MAX_ATTEMPTS = 3
    }

    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private val runningTiramisuOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    private var isRegistered = false
    private var isRequested = false
    private lateinit var componentActivity: ComponentActivity
    private lateinit var requestPermissions: ActivityResultLauncher<Array<String>>
    private lateinit var onPermissionResult: ((PermissionResult) -> Unit)
    private var attempts = 0

    override fun requestPermission(
        owner: ComponentActivity,
        permission: Permission,
        callback: (PermissionResult) -> Unit
    ) {
        logD("Set attempts to be 0")
        attempts = 0
        componentActivity = owner
        onPermissionResult = callback
        registerForActivityResult()
        checkPermission(permission)
    }

    private fun registerForActivityResult() {
        if (!isRegistered) {
            isRegistered = true
            requestPermissions =
                componentActivity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                    permissions?.let { onPermissionsResult(it) }
                }
        }
    }

    private fun onPermissionsResult(permissions: Map<String, @JvmSuppressWildcards Boolean>) {
        logD()
        printLog(permissions)
        isRequested = false
        val permissionEntry = permissions.entries.first()
        val permission = Permission.from(permissionEntry.key) ?: return
        checkPermission(permission)
    }

    private fun checkPermission(permission: Permission) {
        logD("Attempts: $attempts, Permission: ${permission.value}")
        when {
            isNotRequiredForThisApi(permission) -> onPermissionResult(PermissionResult.NotRequired)
            isRequiresOtherPermissions(permission) -> onPermissionResult(PermissionResult.RequiresOtherPermission)
            isGranted(permission) -> onPermissionResult(PermissionResult.Granted)
            attempts == MAX_ATTEMPTS -> onPermissionResult(PermissionResult.NotGranted)
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