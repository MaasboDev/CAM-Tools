package com.maasbodev.camtools

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.SideEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maasbodev.camtools.ui.theme.CAMToolsTheme

class MainActivity : ComponentActivity() {
	private val permissionsToRequest = arrayOf(
		Manifest.permission.CAMERA,
		Manifest.permission.RECORD_AUDIO,
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			CAMToolsTheme {
				val viewModel = viewModel<MainViewModel>()
				val dialogQueue = viewModel.visiblePermissionDialogQueue
				val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
					contract = ActivityResultContracts.RequestMultiplePermissions(),
					onResult = { permissions ->
						permissionsToRequest.forEach { permission ->
							viewModel.onPermissionResult(
								permission = permission,
								isGranted = permissions[permission] == true,
							)
						}
					}
				)
				SideEffect {
					cameraPermissionResultLauncher.launch(permissionsToRequest)
				}
				dialogQueue
					.reversed()
					.forEach { permission ->
						PermissionDialog(
							permissionTextProvider = when(permission) {
								Manifest.permission.CAMERA -> CameraPermissionTextProvider()
								Manifest.permission.RECORD_AUDIO -> RecordAudioPermissionTextProvider()
								else -> return@forEach
							},
							isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
								permission
							),
							onDismiss = viewModel::dismissDialog,
							onOkClick = {
								viewModel.dismissDialog()
								cameraPermissionResultLauncher.launch(
									arrayOf(permission)
								)
							},
							onGoToAppSettingsClick = ::openAppSettings,
						)
					}
			}
		}
	}

	private fun hasRequiredPermissions(): Boolean {
		return permissionsToRequest.all {
			ContextCompat.checkSelfPermission(
				applicationContext,
				it
			) == PackageManager.PERMISSION_GRANTED
		}
	}
}

fun Activity.openAppSettings() {
	Intent(
		Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
		Uri.fromParts("package", packageName, null)
	).also(::startActivity)
}