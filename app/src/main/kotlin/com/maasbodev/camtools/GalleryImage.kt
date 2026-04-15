package com.maasbodev.camtools

import android.net.Uri

data class GalleryImage(
	val uri: Uri,
	val name: String,
	val dateTaken: Long,
)