package com.maasbodev.camtools

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign

@Composable
fun PermissionDialog(
	title: String,
	message: String,
	confirmText: String,
	dismissText: String,
	onConfirm: () -> Unit,
	onDismiss: () -> Unit,
) {
	AlertDialog(
		onDismissRequest = { /* Prevent dismissal by clicking outside */ },
		title = { Text(title) },
		text = {
			Text(
				text = message,
				textAlign = TextAlign.Center
			)
		},
		confirmButton = {
			TextButton(onClick = onConfirm) {
				Text(confirmText)
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(dismissText)
			}
		}
	)
}