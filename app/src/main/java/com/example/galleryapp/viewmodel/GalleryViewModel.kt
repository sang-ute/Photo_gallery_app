package com.example.galleryapp.viewmodel

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.galleryapp.data.Photo
import com.example.galleryapp.data.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GalleryViewModel(private val photoRepository: PhotoRepository) : ViewModel() {
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    private var _currentPhotoIndex = mutableStateOf(0)
    val currentPhotoIndex: State<Int> = _currentPhotoIndex

    var isDarkTheme by mutableStateOf(false)
        private set

    init {
        loadPhotos()

        // Add sample photos only if the database is empty
        viewModelScope.launch {
            photoRepository.getAllPhotos().collect { photosList ->
                if (photosList.isEmpty()) {
                    for (i in 20..30) {
                        val photo = Photo(
                            id = "photo_$i",
                            url = "https://picsum.photos/id/${i}/800/600",
                            thumbnail = "https://picsum.photos/id/${i}/200/200",
                            isFavourite = false
                        )
                        photoRepository.addPhoto(photo)
                    }
                }
            }
        }
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            photoRepository.getAllPhotos().collect { photosList ->
                _photos.value = photosList
            }
        }
    }

    fun deletePhoto(photo: Photo) {
        viewModelScope.launch {
            photoRepository.deletePhoto(photo)

            // Update the current index if needed
            val currentPhotos = _photos.value
            if (_currentPhotoIndex.value >= currentPhotos.size && currentPhotos.isNotEmpty()) {
                _currentPhotoIndex.value = currentPhotos.size - 1
            } else if (currentPhotos.isEmpty()) {
                _currentPhotoIndex.value = 0
            }
        }
    }

    fun markAsFavorite(photo: Photo) {
        viewModelScope.launch {
            val updatedPhoto = photo.copy(isFavourite = !photo.isFavourite)
            photoRepository.updatePhoto(updatedPhoto)
        }
    }

    fun nextPhoto() {
        val currentPhotos = _photos.value
        if (currentPhotos.isNotEmpty()) {
            _currentPhotoIndex.value = (_currentPhotoIndex.value + 1) % currentPhotos.size
        }
    }

    fun previousPhoto() {
        val currentPhotos = _photos.value
        if (currentPhotos.isNotEmpty()) {
            _currentPhotoIndex.value = (_currentPhotoIndex.value - 1 + currentPhotos.size) % currentPhotos.size
        }
    }

    fun setCurrentPhoto(index: Int) {
        if (index in 0 until _photos.value.size) {
            _currentPhotoIndex.value = index
        }
    }

    fun handlePhotoResult(intent: Intent, context: Context) {
        viewModelScope.launch {
            photoRepository.processPhotoResult(intent, context)
        }
    }

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
    }
}