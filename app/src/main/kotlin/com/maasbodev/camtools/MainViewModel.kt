package com.maasbodev.camtools

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
	private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
	val bitmaps = _bitmaps.asStateFlow()

	private val _galleryImages = MutableStateFlow<List<GalleryImage>>(emptyList())
	val galleryImages = _galleryImages.asStateFlow()

	val isRecording = mutableStateOf(false)
	val videoUri = mutableStateOf<Uri?>(null)

	// This is the only state we need for the dialog now.
	val showPermissionRejectionDialog = mutableStateOf(false)

	val selectedTabIndex = mutableIntStateOf(0)

	fun onTakePhoto(bitmap: Bitmap) {
		_bitmaps.value += bitmap
	}

	fun savePhotoToGallery(context: Context, bitmap: Bitmap): Uri? {
		val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
		val filename = "CAMTools_$timestamp.jpg"
		var uri: Uri? = null

		try {
			val contentValues = ContentValues().apply {
				put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
				put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
					put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
					put(MediaStore.Images.Media.IS_PENDING, 1)
				}
			}

			val resolver = context.contentResolver
			uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

			uri?.let { imageUri ->
				resolver.openOutputStream(imageUri)?.use { outputStream ->
					bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
				}

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
					contentValues.clear()
					contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
					resolver.update(imageUri, contentValues, null, null)
				}

				// Refresh gallery images after saving a new photo
				loadGalleryImages(context)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}

		return uri
	}

	fun loadGalleryImages(context: Context) {
		viewModelScope.launch {
			val images = withContext(Dispatchers.IO) {
				val imageList = mutableListOf<GalleryImage>()

				val projection = arrayOf(
					MediaStore.Images.Media._ID,
					MediaStore.Images.Media.DISPLAY_NAME,
					MediaStore.Images.Media.DATE_TAKEN
				)

				val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

				val query = context.contentResolver.query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					projection,
					null,
					null,
					sortOrder
				)

				query?.use { cursor ->
					val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
					val nameColumn =
						cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
					val dateTakenColumn =
						cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

					while (cursor.moveToNext()) {
						val id = cursor.getLong(idColumn)
						val name = cursor.getString(nameColumn)
						val dateTaken = cursor.getLong(dateTakenColumn)

						val contentUri = ContentUris.withAppendedId(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							id
						)

						imageList.add(GalleryImage(contentUri, name, dateTaken))
					}
				}

				imageList
			}

			_galleryImages.value = images
		}
	}
}