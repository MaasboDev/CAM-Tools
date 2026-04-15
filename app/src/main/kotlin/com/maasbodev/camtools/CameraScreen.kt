package com.maasbodev.camtools

import android.app.Activity
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(viewModel: MainViewModel) {
	val scope = rememberCoroutineScope()
	val scaffoldState = rememberBottomSheetScaffoldState()
	val lifecycleOwner = LocalLifecycleOwner.current
	val context = lifecycleOwner as Activity

	val controller = remember {
		LifecycleCameraController(context).apply {
			setEnabledUseCases(
				CameraController.IMAGE_CAPTURE or
						CameraController.VIDEO_CAPTURE
			)
			bindToLifecycle(lifecycleOwner)
		}
	}
	val bitmaps by viewModel.bitmaps.collectAsState()
	val galleryImages by viewModel.galleryImages.collectAsState()

	LaunchedEffect(Unit) {
		viewModel.loadGalleryImages(context)
	}

	BottomSheetScaffold(
		scaffoldState = scaffoldState,
		sheetPeekHeight = 0.dp,
		sheetContent = {
			PhotoBottomSheetContent(
				bitmaps = bitmaps,
				galleryImages = galleryImages,
				modifier = Modifier
					.fillMaxWidth(),
			)
		}
	) { innerPadding ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
				.padding(WindowInsets.safeDrawing.asPaddingValues())
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
					contentDescription = stringResource(R.string.switch_camera),
					tint = Color.White
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
						contentDescription = stringResource(R.string.open_gallery),
						tint = Color.White
					)
				}
				IconButton(
					onClick = {
						takePhoto(
							context = context,
							controller = controller,
							onPhotoTaken = viewModel::onTakePhoto,
							onPhotoSaved = { bitmap ->
								viewModel.savePhotoToGallery(context, bitmap)
							}
						)
					}
				) {
					Icon(
						imageVector = Icons.Default.PhotoCamera,
						contentDescription = stringResource(R.string.take_photo),
						tint = Color.White
					)
				}
				IconButton(
					onClick = {
						viewModel.isRecording.value = !viewModel.isRecording.value
					}
				) {
					Icon(
						imageVector = Icons.Default.Videocam,
						contentDescription = stringResource(R.string.record_video),
						tint = Color.White
					)
				}
			}
		}
	}
}