package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

/**
 * Image processing pipeline - Master PRD Section 9:
 * Gallery/Camera -> Resize (max 512x512) -> Compress WebP/JPEG (target 50-80 KB) -> Validate
 */
object ImageProcessor {
    private const val TAG = "ImageProcessor"
    private const val MAX_DIMENSION = 512
    private const val TARGET_MAX_BYTES = 80 * 1024 // 80 KB
    private const val TARGET_MIN_BYTES = 40 * 1024 // 40 KB

    suspend fun processProfileImage(context: Context, imageUri: Uri): ByteArray? = withContext(Dispatchers.IO) {
        try {
            var inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val srcWidth = options.outWidth
            val srcHeight = options.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) return@withContext null

            // Calculate inSampleSize
            var sampleSize = 1
            val maxSide = max(srcWidth, srcHeight)
            while (maxSide / sampleSize > MAX_DIMENSION * 2) {
                sampleSize *= 2
            }

            inputStream = context.contentResolver.openInputStream(imageUri)
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val sampledBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (sampledBitmap == null) return@withContext null

            // Scale precisely to max dimension 512x512 maintaining aspect ratio
            val scale = MAX_DIMENSION.toFloat() / max(sampledBitmap.width, sampledBitmap.height).toFloat()
            val targetWidth = if (scale < 1.0f) (sampledBitmap.width * scale).toInt() else sampledBitmap.width
            val targetHeight = if (scale < 1.0f) (sampledBitmap.height * scale).toInt() else sampledBitmap.height

            val scaledBitmap = Bitmap.createScaledBitmap(sampledBitmap, targetWidth, targetHeight, true)

            // Compress to target 50-80 KB
            var quality = 85
            var outputBytes: ByteArray
            do {
                val byteStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteStream)
                outputBytes = byteStream.toByteArray()
                quality -= 10
            } while (outputBytes.size > TARGET_MAX_BYTES && quality >= 30)

            Log.d(TAG, "Processed image size: ${outputBytes.size / 1024} KB (Quality: $quality)")
            outputBytes
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image: ${e.message}", e)
            null
        }
    }
}
