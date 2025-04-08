package com.example.galleryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.example.galleryapp.ui.views.MainView
import com.example.galleryapp.viewmodel.GalleryViewModel
import com.example.galleryapp.viewmodel.GalleryViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: GalleryViewModel by viewModels { GalleryViewModelFactory.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainView(viewModel = viewModel)
            }
        }
    }
}