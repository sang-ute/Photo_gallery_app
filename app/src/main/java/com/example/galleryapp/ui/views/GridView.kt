package com.example.galleryapp.ui.views

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.galleryapp.data.Photo
import com.example.galleryapp.viewmodel.GalleryViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GridView(
    viewModel: GalleryViewModel,
    onPhotoClick: (Int) -> Unit
) {
    // Collect photos as state
    val photos by viewModel.photos.collectAsState()
    var selectedPhotoId by remember { mutableStateOf<String?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }
    val lazyGridState = rememberLazyGridState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("My Gallery", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.shadow(4.dp)
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Fixed(5), // 5 images per row
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = 12.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(photos.size, key = { photos[it].id }) { index ->
                val photo = photos[index]
                GridItem(
                    photo = photo,
                    onPhotoClick = { onPhotoClick(index) },
                    onPhotoLongClick = {
                        selectedPhotoId = photo.id
                        showContextMenu = true
                    },
                    modifier = Modifier.animateItemPlacement(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                )
            }
        }
    }

    if (showContextMenu && selectedPhotoId != null) {
        val photo = photos.find { it.id == selectedPhotoId }
        if (photo != null) {
            AlertDialog(
                onDismissRequest = { showContextMenu = false },
                title = { Text("Photo Options", fontWeight = FontWeight.Bold) },
                text = { Text("Choose an action for image") },
                shape = RectangleShape,
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            viewModel.deletePhoto(photo)
                            showContextMenu = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Photo")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridItem(
    photo: Photo,
    onPhotoClick: () -> Unit,
    onPhotoLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoaded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.98f else 1f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(8.dp, RectangleShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onPhotoClick,
                onLongClick = onPhotoLongClick
            ),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photo.thumbnail)
                    .crossfade(true)
                    .build(),
                contentDescription = "Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RectangleShape),
                onSuccess = { isLoaded = true }
            )
            this@Card.AnimatedVisibility(
                visible = !isLoaded,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}