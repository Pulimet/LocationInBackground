package net.alexandroid.locationinbackground

import androidx.activity.ComponentActivity
import net.alexandroid.locationinbackground.permissions.Permission
import net.alexandroid.locationinbackground.permissions.PermissionResult

interface Permissions {
    fun requestPermission(owner: ComponentActivity, permission: Permission, callback: (PermissionResult) -> Unit)
}