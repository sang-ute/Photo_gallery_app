package com.example.galleryapp.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.galleryapp.data.AppDatabase
import com.example.galleryapp.data.PhotoRepositoryImpl

object GalleryViewModelFactory {
    val Factory = viewModelFactory {
        initializer {
            val context = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as android.app.Application)
            val photoDao = AppDatabase.getDatabase(context).photoDao()
            val repository = PhotoRepositoryImpl(photoDao)
            GalleryViewModel(repository)
        }
    }
}