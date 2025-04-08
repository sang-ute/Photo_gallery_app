package com.example.galleryapp.ui.views

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.galleryapp.data.Photo
import com.example.galleryapp.viewmodel.GalleryViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullImageView(
    viewModel: GalleryViewModel,
    onNavigateBack: () -> Unit
) {
    // Collect the photos StateFlow
    val photos by viewModel.photos.collectAsState()
    val currentIndex = viewModel.currentPhotoIndex.value

    // Check if photos list is empty or index is out of bounds
    if (photos.isEmpty() || currentIndex >= photos.size) {
        onNavigateBack()
        return
    }

    val currentPhoto = photos[currentIndex]
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var initialOffsetX by remember { mutableStateOf(0f) }
    var swipeProgress by remember { mutableStateOf(0f) }

    // Add state for long press modal and delete confirmation
    var showActionModal by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // For swipe navigation animation
    val dragProgress by animateFloatAsState(
        targetValue = swipeProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
        // Main photo with gestures
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(scale) {
                    detectDragGestures(
                        onDragStart = { initialOffsetX = offsetX },
                        onDragEnd = {
                            if (scale <= 1.05f && abs(swipeProgress) > 0.3f) {
                                coroutineScope.launch {
                                    if (swipeProgress > 0 && currentIndex > 0) {
                                        viewModel.previousPhoto()
                                    } else if (swipeProgress < 0 && currentIndex < photos.size - 1) {
                                        viewModel.nextPhoto()
                                    }
                                    swipeProgress = 0f
                                    offsetX = 0f
                                }
                            } else {
                                coroutineScope.launch {
                                    swipeProgress = 0f
                                    if (scale <= 1f) offsetX = 0f
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (scale > 1f) {
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                                val maxOffset = (scale - 1f) * size.width / 2f
                                offsetX = offsetX.coerceIn(-maxOffset, maxOffset)
                                offsetY = offsetY.coerceIn(-maxOffset, maxOffset)
                            } else {
                                offsetX += dragAmount.x
                                swipeProgress = offsetX / size.width
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                            val maxOffset = (scale - 1f) * size.width / 2f
                            offsetX = offsetX.coerceIn(-maxOffset, maxOffset)
                            offsetY = offsetY.coerceIn(-maxOffset, maxOffset)
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale = if (scale > 1f) 1f else 2.5f
                            if (scale == 1f) {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        },
                        // Add long press detection
                        onLongPress = {
                            showActionModal = true
                        }
                    )
                }
        ) {
            // Background image (for swipe visual effect)
            if (currentIndex > 0 && swipeProgress > 0) {
                val previousPhoto = photos[currentIndex - 1]
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(previousPhoto.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = -size.width + (dragProgress * size.width)
                        }
                )
            }

            if (currentIndex < photos.size - 1 && swipeProgress < 0) {
                val nextPhoto = photos[currentIndex + 1]
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(nextPhoto.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(Modifier.fillMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = size.width + (dragProgress * size.width)
                        }
                )
            }

            // Current photo
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(currentPhoto.url)
                    .crossfade(true)
                    .build(),
                contentDescription = "Photo",
                contentScale = ContentScale.Fit,
                loading = {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = if (scale > 1f) offsetX else (dragProgress * size.width)
                        translationY = offsetY
                    }
            )

            // Navigation indicators (when swiping)
            if (swipeProgress != 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    if (swipeProgress > 0.05f && currentIndex > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(48.dp)
                                .alpha(swipeProgress * 2f)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(24.dp)
                            )
                        }
                    }

                    if (swipeProgress < -0.05f && currentIndex < photos.size - 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(48.dp)
                                .alpha(abs(swipeProgress) * 2f)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(24.dp)
                            )
                        }
                    }
                }
            }

            // Top app bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    )
                    .align(Alignment.TopCenter)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "${currentIndex + 1} / ${photos.size}",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back to Grid",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Add Favorite indicator in the app bar
                        IconButton(onClick = {
                            viewModel.markAsFavorite(currentPhoto)
                        }) {
                            Icon(
                                imageVector = if (currentPhoto.isFavourite)
                                    Icons.Default.Favorite
                                else
                                    Icons.Default.FavoriteBorder,
                                contentDescription = if (currentPhoto.isFavourite)
                                    "Remove from Favorites"
                                else
                                    "Add to Favorites",
                                tint = if (currentPhoto.isFavourite)
                                    Color(0xFFFF8C00)
                                else
                                    Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }

            // Bottom navigation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.previousPhoto() },
                            enabled = currentIndex > 0,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                disabledContentColor = Color.Gray
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(if (currentIndex > 0) Color.White else Color.Gray)
                            ),
                            modifier = Modifier.width(140.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Previous")
                        }

                        // Zoom button
                        IconButton(
                            onClick = {
                                scale = if (scale > 1f) {
                                    offsetX = 0f
                                    offsetY = 0f
                                    1f
                                } else {
                                    2.5f
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (scale > 1f) Icons.Outlined.ZoomOut else Icons.Outlined.ZoomIn,
                                contentDescription = if (scale > 1f) "Zoom Out" else "Zoom In",
                                tint = Color.White
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.nextPhoto() },
                            enabled = currentIndex < photos.size - 1,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                disabledContentColor = Color.Gray
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(if (currentIndex < photos.size - 1) Color.White else Color.Gray)
                            ),
                            modifier = Modifier.width(140.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }

        // Long press action modal
        if (showActionModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showActionModal = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF333333)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(300.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Photo Options",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center
                        )

                        Divider(color = Color.Gray.copy(alpha = 0.3f))

                        // Favorite button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Toggle favorite status using the ViewModel
                                    viewModel.markAsFavorite(currentPhoto)
                                    showActionModal = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (currentPhoto.isFavourite)
                                    Icons.Default.Favorite
                                else
                                    Icons.Default.FavoriteBorder,
                                contentDescription = if (currentPhoto.isFavourite)
                                    "Remove from Favorites"
                                else
                                    "Add to Favorites",
                                tint = Color(0xFFFF8C00),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (currentPhoto.isFavourite)
                                    "Remove from Favorites"
                                else
                                    "Add to Favorites",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.3f))

                        // Delete button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Show delete confirmation dialog
                                    showDeleteConfirmation = true
                                    showActionModal = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Delete",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.3f))

                        // Cancel button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showActionModal = false }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmation = false
                },
                title = {
                    Text("Delete Photo")
                },
                text = {
                    Text("Are you sure you want to delete this photo? This action cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                // Delete the photo using the ViewModel
                                viewModel.deletePhoto(currentPhoto)
                                showDeleteConfirmation = false
                            }
                        }
                    ) {
                        Text("Delete", color = Color(0xFFFF5252))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation = false
                        }
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Color(0xFF333333),
                textContentColor = Color.White,
                titleContentColor = Color.White
            )
        }
    }
}