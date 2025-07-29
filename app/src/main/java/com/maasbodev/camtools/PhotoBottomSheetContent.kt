package com.maasbodev.camtools

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun PhotoBottomSheetContent(
	bitmaps: List<Bitmap>,
	galleryImages: List<GalleryImage>,
	modifier: Modifier = Modifier,
) {
	if (bitmaps.isEmpty() && galleryImages.isEmpty()) {
		Box(
			modifier = modifier
				.padding(16.dp),
			contentAlignment = Alignment.Center,
		) {
			Text("There are no photos yet") //TODO add string resource
		}
	} else {
		LazyVerticalStaggeredGrid(
			columns = StaggeredGridCells.Fixed(2),
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			verticalItemSpacing = 16.dp,
			contentPadding = PaddingValues(16.dp),
			modifier = modifier
		) {
			// Show session photos first
			items(bitmaps) { bitmap ->
				Image(
					bitmap = bitmap.asImageBitmap(),
					contentDescription = null,
					modifier = Modifier
						.clip(RoundedCornerShape(10.dp))
				)
			}

			// Then show gallery images
			items(galleryImages) { galleryImage ->
				SubcomposeAsyncImage(
					model = ImageRequest.Builder(LocalContext.current)
						.data(galleryImage.uri)
						.crossfade(true)
						.build(),
					contentDescription = galleryImage.name,
					loading = {
						CircularProgressIndicator()
					},
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.fillMaxWidth()
						.height(200.dp)
						.clip(RoundedCornerShape(10.dp))
				)
			}
		}
	}
}
