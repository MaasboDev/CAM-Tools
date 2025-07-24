package com.maasbodev.camtools

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
	val visiblePermissionDialogQueue = mutableStateListOf<String>()
	private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
	val bitmaps = _bitmaps.asStateFlow()
	fun onTakePhoto(bitmap: Bitmap) {
		_bitmaps.value += bitmap
	}

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