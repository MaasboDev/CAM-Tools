package com.maasbodev.camtools

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign

@Composable
fun PermissionDialog(
	onConfirm: () -> Unit,
	onDismiss: () -> Unit
) {
	AlertDialog(
		onDismissRequest = { /* Prevent dismissal by clicking outside */ },
		title = { Text(stringResource(R.string.permission_dialog_title)) },
		text = {
			Text(
				stringResource(R.string.all_or_nothing_dialog_text),
				textAlign = TextAlign.Center
			)
		},
		confirmButton = {
			TextButton(onClick = onConfirm) {
				Text(stringResource(R.string.all_or_nothing_dialog_settings_button))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.all_or_nothing_dialog_close_button))
			}
		}
	)
}