package com.maasbodev.camtools

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.ui.res.stringResource
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

				var hasCameraPermission by remember {
					mutableStateOf(hasPermission(Manifest.permission.CAMERA))
				}
				var hasAudioPermission by remember {
					mutableStateOf(hasPermission(Manifest.permission.RECORD_AUDIO))
				}
				var mediaAccess by remember {
					mutableStateOf(currentMediaAccess())
				}

				val lifecycleOwner = LocalLifecycleOwner.current
				DisposableEffect(lifecycleOwner) {
					val observer = LifecycleEventObserver { _, event ->
						if (event == Lifecycle.Event.ON_RESUME) {
							hasCameraPermission = hasPermission(Manifest.permission.CAMERA)
							hasAudioPermission = hasPermission(Manifest.permission.RECORD_AUDIO)
							mediaAccess = currentMediaAccess()

							viewModel.setMediaAccessDenied(!mediaAccess.hasAccess)
							viewModel.setMediaAccessLimited(mediaAccess.isLimited)
							viewModel.showPermissionRejectionDialog.value =
								!hasCameraPermission && isCameraPermanentlyDenied()
						}
					}
					lifecycleOwner.lifecycle.addObserver(observer)
					onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
				}

				val cameraPermissionLauncher = rememberLauncherForActivityResult(
					contract = ActivityResultContracts.RequestPermission(),
					onResult = { granted ->
						hasCameraPermission = granted
						viewModel.showPermissionRejectionDialog.value =
							!granted && isCameraPermanentlyDenied()
					}
				)

				val audioPermissionLauncher = rememberLauncherForActivityResult(
					contract = ActivityResultContracts.RequestPermission(),
					onResult = { granted ->
						hasAudioPermission = granted
					}
				)

				val mediaPermissionLauncher = rememberLauncherForActivityResult(
					contract = ActivityResultContracts.RequestMultiplePermissions(),
					onResult = { results ->
						mediaAccess = PermissionPolicy.resolveMediaAccessFromGrantResults(
							sdkInt = Build.VERSION.SDK_INT,
							grantResults = results
						)

						viewModel.setMediaAccessDenied(!mediaAccess.hasAccess)
						viewModel.setMediaAccessLimited(mediaAccess.isLimited)

						if (mediaAccess.hasAccess) {
							viewModel.requestOpenGallerySheet()
							viewModel.loadGalleryImages(this@MainActivity)
						}
					}
				)

				LaunchedEffect(Unit) {
					if (!hasCameraPermission) {
						cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
					}
				}

				if (hasCameraPermission) {
					CameraScreen(
						viewModel = viewModel,
						canRecordAudio = hasAudioPermission,
						hasMediaAccess = mediaAccess.hasAccess,
						isMediaAccessLimited = mediaAccess.isLimited,
						onOpenGalleryRequested = {
							if (mediaAccess.hasAccess) {
								viewModel.requestOpenGallerySheet()
								viewModel.loadGalleryImages(this@MainActivity)
							} else {
								mediaPermissionLauncher.launch(
									PermissionPolicy.requiredMediaPermissions(
										sdkInt = Build.VERSION.SDK_INT
									).toTypedArray()
								)
							}
						},
						onRequestAudioPermission = {
							if (!hasAudioPermission) {
								audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
							}
						}
					)
				} else if (viewModel.showPermissionRejectionDialog.value) {
					PermissionDialog(
						title = stringResource(R.string.permission_dialog_title),
						message = stringResource(R.string.camera_permission_permanently_declined),
						confirmText = stringResource(R.string.all_or_nothing_dialog_settings_button),
						dismissText = stringResource(R.string.all_or_nothing_dialog_close_button),
						onConfirm = { openAppSettings() },
						onDismiss = { finish() }
					)
				}
			}
		}
	}

	private fun currentMediaAccess(): PermissionPolicy.MediaAccess {
		return PermissionPolicy.resolveMediaAccess(
			sdkInt = Build.VERSION.SDK_INT,
			isGranted = { permission -> hasPermission(permission) }
		)
	}

	private fun isCameraPermanentlyDenied(): Boolean {
		val granted = hasPermission(Manifest.permission.CAMERA)
		val shouldShowRationale =
			shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)

		return PermissionPolicy.isCameraPermanentlyDenied(
			cameraGranted = granted,
			shouldShowRationale = shouldShowRationale
		)
	}

	private fun hasPermission(permission: String): Boolean {
		return ContextCompat.checkSelfPermission(
			applicationContext,
			permission
		) == PackageManager.PERMISSION_GRANTED
	}
}