package net.alexandroid.locationinbackground.permissions

sealed class PermissionResult {
    object NotRequired : PermissionResult()
    object Granted : PermissionResult()
    object NotGranted : PermissionResult()
    object RequiresOtherPermission : PermissionResult()
}