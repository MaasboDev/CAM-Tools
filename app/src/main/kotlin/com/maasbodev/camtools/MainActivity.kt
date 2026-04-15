package com.maasbodev.camtools

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maasbodev.camtools.ui.theme.CAMToolsTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
	private val permissionsToRequest = arrayOf(
		Manifest.permission.CAMERA,
		Manifest.permission.RECORD_AUDIO,
	)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		WindowCompat.setDecorFitsSystemWindows(window, false)
		WindowInsetsControllerCompat(window, window.decorView).let { controller ->
			controller.hide(WindowInsetsCompat.Type.systemBars())
			controller.systemBarsBehavior =
				WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
		}

		setContent {
			CAMToolsTheme {
				val viewModel = viewModel<MainViewModel>()

				var hasPermissions by remember {
					mutableStateOf(hasRequiredPermissions())
				}

				val lifecycleOwner = LocalLifecycleOwner.current
				DisposableEffect(lifecycleOwner) {
					val observer = LifecycleEventObserver { _, event ->
						if (event == Lifecycle.Event.ON_RESUME) {
							val permissionsGranted = hasRequiredPermissions()
							hasPermissions = permissionsGranted
							// Update the dialog visibility based on the permission status
							viewModel.showPermissionRejectionDialog.value = !permissionsGranted
						}
					}
					lifecycleOwner.lifecycle.addObserver(observer)

					onDispose {
						lifecycleOwner.lifecycle.removeObserver(observer)
					}
				}

				val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
					contract = ActivityResultContracts.RequestMultiplePermissions(),
					onResult = { permissions ->
						val allGranted = permissions.all { it.value }
						hasPermissions = allGranted
						// Directly control the single dialog's visibility
						viewModel.showPermissionRejectionDialog.value = !allGranted
					}
				)

				LaunchedEffect(Unit) {
					if (!hasPermissions) {
						cameraPermissionResultLauncher.launch(permissionsToRequest)
					}
				}

				// The UI is now a simple choice: show the camera or show the permission dialog.
				if (hasPermissions) {
					CameraScreen(viewModel)
				} else if (viewModel.showPermissionRejectionDialog.value) {
					// The single, simplified dialog for handling all permission failures.
					PermissionDialog(
						onConfirm = {
							// The only positive action is to send the user to settings.
							openAppSettings()
						},
						onDismiss = {
							// The only negative action is to close the app.
							finish()
						}
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