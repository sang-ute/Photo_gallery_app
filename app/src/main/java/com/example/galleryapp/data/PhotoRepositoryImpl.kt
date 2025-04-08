package com.example.galleryapp.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream

class PhotoRepositoryImpl(private val photoDao: PhotoDao) : PhotoRepository {
    override fun getAllPhotos(): Flow<List<Photo>> {
        return photoDao.getAllPhotos().map { entities ->
            entities.map { entity ->
                Photo(
                    id = entity.id,
                    url = entity.url,
                    thumbnail = entity.thumbnail,
                    isFavourite = entity.isFavourite
                )
            }
        }
    }

    override suspend fun addPhoto(photo: Photo) {
        photoDao.insertPhoto(
            PhotoEntity(
                id = photo.id,
                url = photo.url,
                thumbnail = photo.thumbnail,
                isFavourite = photo.isFavourite
            )
        )
    }

    override suspend fun deletePhoto(photo: Photo) {
        photoDao.deletePhoto(
            PhotoEntity(
                id = photo.id,
                url = photo.url,
                thumbnail = photo.thumbnail,
                isFavourite = photo.isFavourite
            )
        )
    }

    override suspend fun updatePhoto(photo: Photo) {
        photoDao.updatePhoto(
            PhotoEntity(
                id = photo.id,
                url = photo.url,
                thumbnail = photo.thumbnail,
                isFavourite = photo.isFavourite
            )
        )
    }

    private fun generateUniqueId(): String {
        return "photo_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    override suspend fun processPhotoResult(intent: Intent, context: Context) {
        // Handle camera result
        intent.extras?.get("data")?.let { data ->
            if (data is Bitmap) {
                try {
                    // Save the bitmap to storage and get a Uri
                    val photoFile = createImageFile(context)
                    FileOutputStream(photoFile).use { out ->
                        data.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    }

                    val photoUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile
                    )

                    // Create thumbnail
                    val thumbnailBitmap = createThumbnail(data, 200)
                    val thumbnailFile = createImageFile(context, "thumb_")
                    FileOutputStream(thumbnailFile).use { out ->
                        thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    }
                    val thumbnailUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        thumbnailFile
                    )

                    val photo = Photo(
                        id = generateUniqueId(),  // Use generateUniqueId() instead
                        url = photoUri.toString(),
                        thumbnail = thumbnailUri.toString(),
                        isFavourite = false
                    )
                    addPhoto(photo)
                } catch (e: Exception) {
                    Log.e("PhotoGallery", "Error saving camera photo", e)
                }
            }
        }

        // Handle gallery result
        intent.data?.let { uri ->
            try {
                // Get the bitmap from the gallery URI
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

                // Create thumbnail
                val thumbnailBitmap = createThumbnail(bitmap, 200)
                val thumbnailFile = createImageFile(context, "thumb_")
                FileOutputStream(thumbnailFile).use { out ->
                    thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                val thumbnailUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    thumbnailFile
                )

                val photo = Photo(
                    id = generateUniqueId(),  // Use generateUniqueId() here too
                    url = uri.toString(),
                    thumbnail = thumbnailUri.toString(),
                    isFavourite = false
                )
                addPhoto(photo)
            } catch (e: Exception) {
                Log.e("PhotoGallery", "Error processing gallery photo", e)
            }
        }
    }

    private fun createImageFile(context: Context, prefix: String = ""): File {
        val timeStamp = System.currentTimeMillis().toString()
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "${prefix}JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun createThumbnail(bitmap: Bitmap, size: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val scale = size.toFloat() / maxOf(width, height)
        return Bitmap.createScaledBitmap(bitmap, (width * scale).toInt(), (height * scale).toInt(), true)
    }
}