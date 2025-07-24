package com.maasbodev.camtools

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraPreview(
	controller: LifecycleCameraController,
	modifier: Modifier = Modifier,
) {
	AndroidView(
		factory = { context ->
			PreviewView(context).apply {
				this.controller = controller
			}
		},
		modifier = modifier,
		// Update the view when it's laid out
		update = { previewView ->
			previewView.controller = controller
		}
	)
}
