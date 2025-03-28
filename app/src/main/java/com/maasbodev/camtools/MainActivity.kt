package com.maasbodev.camtools

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maasbodev.camtools.ui.theme.CAMToolsTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
							permissionTextProvider = when (permission) {
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
				if (hasRequiredPermissions()) {
					val scope = rememberCoroutineScope()
					val scaffoldState = rememberBottomSheetScaffoldState()
					val controller = remember {
						LifecycleCameraController(applicationContext).apply {
							setEnabledUseCases(
								CameraController.IMAGE_CAPTURE or
										CameraController.VIDEO_CAPTURE
							)
						}
					}
					val bitmaps by viewModel.bitmaps.collectAsState()
					BottomSheetScaffold(
						scaffoldState = scaffoldState,
						sheetPeekHeight = 0.dp,
						sheetContent = {
							PhotoBottomSheetContent(
								bitmaps = bitmaps,
								modifier = Modifier
									.fillMaxWidth(),
							)
						}
					) { innerPadding ->
						Box(
							modifier = Modifier
								.fillMaxSize()
								.padding(innerPadding)
						) {
							CameraPreview(
								controller = controller,
								modifier = Modifier
									.fillMaxSize(),
							)
							IconButton(
								onClick = {
									controller.cameraSelector =
										if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
											CameraSelector.DEFAULT_FRONT_CAMERA
										else
											CameraSelector.DEFAULT_BACK_CAMERA
								},
								modifier = Modifier
									.offset(16.dp, 16.dp)
							) {
								Icon(
									imageVector = Icons.Default.Cameraswitch,
									contentDescription = "Switch camera", //TODO Localize
								)
							}
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.align(Alignment.BottomCenter)
									.padding(16.dp),
								horizontalArrangement = Arrangement.SpaceAround,
							) {
								IconButton(
									onClick = {
										scope.launch {
											scaffoldState.bottomSheetState.expand()
										}
									}
								) {
									Icon(
										imageVector = Icons.Default.Photo,
										contentDescription = "Open gallery", //TODO Localize
									)
								}
								IconButton(
									onClick = {
										takePhoto(
											controller = controller,
											onPhotoTaken = viewModel::onTakePhoto
										)
									}
								) {
									Icon(
										imageVector = Icons.Default.PhotoCamera,
										contentDescription = "Take photo", //TODO Localize
									)
								}
							}
						}
					}
				}
			}
		}
	}

	private fun takePhoto(
		controller: LifecycleCameraController,
		onPhotoTaken: (Bitmap) -> Unit,
	) {
		controller.takePicture(
			ContextCompat.getMainExecutor(applicationContext),
			object : OnImageCapturedCallback() {
				override fun onCaptureSuccess(image: ImageProxy) {
					super.onCaptureSuccess(image)
					onPhotoTaken(image.toBitmap())
				}

				override fun onError(exception: ImageCaptureException) {
					super.onError(exception)
					Log.e("Camera", "Couldn't take photo", exception)
				}
			}
		)
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