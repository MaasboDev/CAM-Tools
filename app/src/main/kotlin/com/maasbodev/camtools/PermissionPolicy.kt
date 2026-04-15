package com.maasbodev.camtools

import android.Manifest
import android.os.Build

object PermissionPolicy {

    data class MediaAccess(
        val hasAccess: Boolean,
        val isLimited: Boolean,
    )

    fun requiredMediaPermissions(sdkInt: Int): List<String> {
        return when {
            sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
            sdkInt >= Build.VERSION_CODES.TIRAMISU -> listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
            else -> listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    fun resolveMediaAccess(
        sdkInt: Int,
        isGranted: (String) -> Boolean,
    ): MediaAccess {
        return when {
            sdkInt >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                val hasImages = isGranted(Manifest.permission.READ_MEDIA_IMAGES)
                val hasVisualSelected = isGranted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)

                MediaAccess(
                    hasAccess = hasImages || hasVisualSelected,
                    isLimited = !hasImages && hasVisualSelected
                )
            }
            sdkInt >= Build.VERSION_CODES.TIRAMISU -> {
                val hasImages = isGranted(Manifest.permission.READ_MEDIA_IMAGES)
                MediaAccess(
                    hasAccess = hasImages,
                    isLimited = false
                )
            }
            else -> {
                val hasLegacyRead = isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                MediaAccess(
                    hasAccess = hasLegacyRead,
                    isLimited = false
                )
            }
        }
    }

    fun resolveMediaAccessFromGrantResults(
        sdkInt: Int,
        grantResults: Map<String, Boolean>,
    ): MediaAccess {
        return resolveMediaAccess(sdkInt = sdkInt) { permission ->
            grantResults[permission] == true
        }
    }

    fun isCameraPermanentlyDenied(
        cameraGranted: Boolean,
        shouldShowRationale: Boolean,
    ): Boolean {
        return !cameraGranted && !shouldShowRationale
    }
}
