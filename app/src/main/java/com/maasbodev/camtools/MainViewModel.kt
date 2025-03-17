package com.maasbodev.camtools

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
	val visiblePermissionDialogQueue = mutableStateListOf<String>()
	fun dismissDialog() {
		visiblePermissionDialogQueue.removeAt(visiblePermissionDialogQueue.lastIndex)
	}
	fun onPermissionResult(
		permission: String,
		isGranted: Boolean,
	) {
		if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
			visiblePermissionDialogQueue.add(permission)
		}
	}
}