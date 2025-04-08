package com.example.galleryapp.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    fun getAllPhotos(): Flow<List<Photo>>
    suspend fun addPhoto(photo: Photo)
    suspend fun deletePhoto(photo: Photo)
    suspend fun updatePhoto(photo: Photo)
    suspend fun processPhotoResult(intent: Intent, context: Context)
}