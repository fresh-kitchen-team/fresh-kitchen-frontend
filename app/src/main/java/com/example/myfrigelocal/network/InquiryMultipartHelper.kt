package com.example.myfrigelocal.network

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object InquiryMultipartHelper {

    /**
     * 이미지 URI가 있으면 파일 파트, 없으면 서버/Retrofit용 빈 `image` 파트.
     * (서버: multipart 필수 / Retrofit: part 1개 이상 필요)
     */
    fun resolveImagePart(context: Context, imageUri: String?): MultipartBody.Part {
        if (!imageUri.isNullOrBlank()) {
            return uriToImagePart(context, imageUri)
        }
        return emptyImagePart()
    }

    /** 선택 이미지 없음 — 빈 바이너리 파트 (필드명 `image`) */
    fun emptyImagePart(): MultipartBody.Part {
        val body = ByteArray(0).toRequestBody("application/octet-stream".toMediaType())
        return MultipartBody.Part.createFormData("image", "", body)
    }

    /** Swagger multipart field name: `image` */
    fun uriToImagePart(context: Context, uriString: String): MultipartBody.Part {
        val uri = Uri.parse(uriString)
        val resolver = context.contentResolver
        val mime = (resolver.getType(uri) ?: "image/jpeg").let {
            if (it.startsWith("image/")) it else "image/jpeg"
        }
        val fileName = uri.displayNameOrFallback(resolver)
        val bytes =
            resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalArgumentException("이미지를 읽을 수 없습니다.")
        if (bytes.isEmpty()) {
            throw IllegalArgumentException("이미지 파일이 비어 있습니다.")
        }
        val body = bytes.toRequestBody(mime.toMediaType())
        return MultipartBody.Part.createFormData("image", fileName, body)
    }

    private fun Uri.displayNameOrFallback(resolver: ContentResolver): String {
        if (scheme == ContentResolver.SCHEME_FILE) {
            lastPathSegment?.takeIf { it.isNotBlank() }?.let { return it }
        }
        resolver.query(this, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) {
                    c.getString(idx)?.takeIf { it.isNotBlank() }?.let { return it }
                }
            }
        }
        return "inquiry_image.jpg"
    }
}
