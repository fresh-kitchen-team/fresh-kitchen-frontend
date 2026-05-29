package com.example.myfrigelocal.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

object ScanImageCropper {
    private const val TAG = "ScanCrop"

    data class CropDebug(
        val bitmapW: Int,
        val bitmapH: Int,
        val frame: RectF,
        val preview: RectF,
        val crop: Rect,
        val croppedW: Int,
        val croppedH: Int,
        val resizedW: Int,
        val resizedH: Int,
        val croppedSavedPath: String?,
        val resizedSavedPath: String?,
    )

    /**
     * Calculates a bitmap crop rect from UI frame/preview bounds.
     *
     * Assumes PreviewView.ScaleType.FILL_CENTER (center-crop):
     * - content is scaled by max(viewW/bitmapW, viewH/bitmapH)
     * - content is centered and may be cropped offscreen
     */
    fun calculateCropRectCenterCrop(
        frameBoundsInWindowPx: RectF,
        previewBoundsInWindowPx: RectF,
        bitmapW: Int,
        bitmapH: Int,
    ): Rect {
        val previewW = previewBoundsInWindowPx.width().coerceAtLeast(1f)
        val previewH = previewBoundsInWindowPx.height().coerceAtLeast(1f)

        val scale = max(previewW / bitmapW.toFloat(), previewH / bitmapH.toFloat())
        val displayedW = bitmapW * scale
        val displayedH = bitmapH * scale
        val offsetX = (previewW - displayedW) / 2f
        val offsetY = (previewH - displayedH) / 2f

        val frameXInPreview = frameBoundsInWindowPx.left - previewBoundsInWindowPx.left
        val frameYInPreview = frameBoundsInWindowPx.top - previewBoundsInWindowPx.top

        val left = ((frameXInPreview - offsetX) / scale).roundToInt()
        val top = ((frameYInPreview - offsetY) / scale).roundToInt()
        val right = (((frameXInPreview + frameBoundsInWindowPx.width()) - offsetX) / scale).roundToInt()
        val bottom = (((frameYInPreview + frameBoundsInWindowPx.height()) - offsetY) / scale).roundToInt()

        return Rect(left, top, right, bottom).clampToBitmap(bitmapW, bitmapH)
    }

    fun cropBitmapSafe(source: Bitmap, crop: Rect): Bitmap {
        val safe = crop.clampToBitmap(source.width, source.height)
        val w = (safe.width()).coerceAtLeast(1)
        val h = (safe.height()).coerceAtLeast(1)
        return Bitmap.createBitmap(source, safe.left, safe.top, w, h)
    }

    fun resizeToSquare(
        source: Bitmap,
        size: Int = 1024,
    ): Bitmap {
        // Fixed output resolution for downstream processing.
        return Bitmap.createScaledBitmap(source, size, size, true)
    }

    fun resize(
        source: Bitmap,
        targetW: Int,
        targetH: Int,
    ): Bitmap {
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    /** 긴 변이 [maxSize] 이하가 되도록 비율 유지 리사이즈 (크롭 없음). */
    fun resizeFitWithinMax(
        source: Bitmap,
        maxSize: Int = 1024,
    ): Bitmap {
        val w = source.width
        val h = source.height
        if (w <= maxSize && h <= maxSize) return source
        val scale = minOf(maxSize.toFloat() / w, maxSize.toFloat() / h)
        val nw = (w * scale).roundToInt().coerceAtLeast(1)
        val nh = (h * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, nw, nh, true)
    }

    fun saveJpegToInternal(
        context: Context,
        bitmap: Bitmap,
        prefix: String,
    ): Uri {
        val dir = File(context.filesDir, "result").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        val file = File(dir, "${prefix}_$stamp.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        Log.d(TAG, "Saved image at: ${file.absolutePath}")
        return file.toUri()
    }

    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val bitmap =
                if (uri.scheme == "file") {
                    BitmapFactory.decodeFile(uri.path)
                } else {
                    if (Build.VERSION.SDK_INT >= 28) {
                        val src = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                        android.graphics.ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                            decoder.isMutableRequired = false
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                } ?: return null

            val rotationDegrees = readExifRotationDegrees(context, uri)
            if (rotationDegrees != 0) {
                Log.d(TAG, "Applying EXIF rotation: $rotationDegrees° for uri=$uri")
                bitmap.rotate(rotationDegrees)
            } else {
                bitmap
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to load bitmap for uri=$uri", t)
            null
        }
    }

    fun logDebug(debug: CropDebug) {
        Log.d(
            TAG,
            buildString {
                appendLine("Original Bitmap: ${debug.bitmapW} x ${debug.bitmapH}")
                appendLine("Frame Bounds: x=${debug.frame.left}, y=${debug.frame.top}, width=${debug.frame.width()}, height=${debug.frame.height()}")
                appendLine("Preview Bounds: x=${debug.preview.left}, y=${debug.preview.top}, width=${debug.preview.width()}, height=${debug.preview.height()}")
                appendLine("Crop Rect: x=${debug.crop.left}, y=${debug.crop.top}, width=${debug.crop.width()}, height=${debug.crop.height()}")
                appendLine("Cropped Bitmap: ${debug.croppedW} x ${debug.croppedH}")
                appendLine("Resized Bitmap: ${debug.resizedW} x ${debug.resizedH}")
                if (debug.croppedSavedPath != null) appendLine("Cropped saved at: ${debug.croppedSavedPath}")
                if (debug.resizedSavedPath != null) appendLine("Resized saved at: ${debug.resizedSavedPath}")
            },
        )
    }
}

private fun readExifRotationDegrees(context: Context, uri: Uri): Int {
    return try {
        val exif = openExif(context, uri) ?: return 0
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } catch (t: Throwable) {
        Log.d("ScanCrop", "Failed to read EXIF for uri=$uri: ${t.message}")
        0
    }
}

private fun openExif(context: Context, uri: Uri): ExifInterface? {
    return try {
        if (uri.scheme == "file") {
            val path = uri.path ?: return null
            ExifInterface(path)
        } else {
            val input: InputStream = context.contentResolver.openInputStream(uri) ?: return null
            input.use { ExifInterface(it) }
        }
    } catch (_: Throwable) {
        null
    }
}

private fun Bitmap.rotate(degrees: Int): Bitmap {
    val m = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, m, true)
}

private fun Rect.clampToBitmap(w: Int, h: Int): Rect {
    val left = left.coerceIn(0, w - 1)
    val top = top.coerceIn(0, h - 1)
    val right = right.coerceIn(left + 1, w)
    val bottom = bottom.coerceIn(top + 1, h)
    return Rect(left, top, right, bottom)
}

