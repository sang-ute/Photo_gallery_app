package com.example.galleryapp.ui.views

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*

import com.example.galleryapp.viewmodel.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(viewModel: GalleryViewModel) {
    val navController = rememberNavController()
    var fabExpanded by remember { mutableStateOf(false) }
    val isDark = viewModel.isDarkTheme
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                viewModel.handlePhotoResult(intent, context)
            }
        }
    }

    MaterialTheme(
        colorScheme = if (isDark) darkColorScheme() else lightColorScheme()
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingMenu(
                    expanded = fabExpanded,
                    onExpandedChange = { fabExpanded = it },
                    onCameraClick = {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        launcher.launch(intent)
                    },
                    onGalleryClick = {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        launcher.launch(intent)
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                startDestination = "grid"
            ) {
                composable("grid") {
                    GridView(
                        viewModel = viewModel,
                        onPhotoClick = { index ->
                            viewModel.setCurrentPhoto(index)
                            navController.navigate("fullPhoto")
                        }
                    )
                }
                composable("fullPhoto") {
                    FullImageView(
                        viewModel = viewModel,
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
                composable("settings") {
                    SettingsView(
                        viewModel = viewModel,
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
            }
        }
    }
}

@Composable
fun SmallActionButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    SmallFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description
        )
    }
}

@Composable
fun FloatingMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SmallActionButton(
                        icon = Icons.Default.PhotoCamera,
                        description = "Camera",
                        onClick = {
                            onCameraClick()
                            onExpandedChange(false)
                        }
                    )
                    SmallActionButton(
                        icon = Icons.Default.PhotoLibrary,
                        description = "Library",
                        onClick = {
                            onGalleryClick()
                            onExpandedChange(false)
                        }
                    )
                    SmallActionButton(
                        icon = Icons.Default.Settings,
                        description = "Settings",
                        onClick = {
                            onSettingsClick()
                            onExpandedChange(false)
                        }
                    )
                }
            }

            val rotation by animateFloatAsState(targetValue = if (expanded) 45f else 0f)

            FloatingActionButton(
                onClick = { onExpandedChange(!expanded) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Photo",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}
