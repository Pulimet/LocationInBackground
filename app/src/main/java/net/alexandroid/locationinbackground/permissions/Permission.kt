package net.alexandroid.locationinbackground.permissions

import android.Manifest
import android.annotation.SuppressLint

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