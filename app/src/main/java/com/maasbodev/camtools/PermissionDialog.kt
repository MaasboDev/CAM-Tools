package com.maasbodev.camtools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PermissionDialog(
	permissionTextProvider: PermissionTextProvider,
	isPermanentlyDeclined: Boolean,
	onDismiss: () -> Unit,
	onOkClick: () -> Unit,
	onGoToAppSettingsClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		confirmButton = {
			Column(
				modifier = Modifier.fillMaxWidth()
			) {
				HorizontalDivider()
				Text(
					text = if (isPermanentlyDeclined) {
						"Grant permission"
					} else {
						"Ok"
					},
					fontWeight = FontWeight.Bold,
					textAlign = TextAlign.Center,
					modifier = Modifier
						.fillMaxWidth()
						.clickable {
							if (isPermanentlyDeclined) {
								onGoToAppSettingsClick()
							} else {
								onOkClick()
							}
						}
						.padding(16.dp)
				)
			}
		},
		dismissButton = { onDismiss() },
		title = {
			Text(text = "Permission required")
		},
		text = {
			Text(
				text = permissionTextProvider.getDescription(
					isPermanentlyDeclined = isPermanentlyDeclined
				),
			)
		},
		modifier = modifier,
	)
}

interface PermissionTextProvider {
	fun getDescription(isPermanentlyDeclined: Boolean): String
}

class CameraPermissionTextProvider : PermissionTextProvider {
	override fun getDescription(isPermanentlyDeclined: Boolean): String {
		return if (isPermanentlyDeclined) {
			"Camera permission is required to take photos. " +
					"Please grant the permission in the app settings."
		} else {
			"Camera permission is required to take photos. " +
					"Please grant the permission."
		}
	}
}

class RecordAudioPermissionTextProvider : PermissionTextProvider {
	override fun getDescription(isPermanentlyDeclined: Boolean): String {
		return if (isPermanentlyDeclined) {
			"Audio recording permission is required to record audio. " +
					"Please grant the permission in the app settings."
		} else {
			"Audio recording permission is required to record audio. " +
					"Please grant the permission."
		}
	}
}