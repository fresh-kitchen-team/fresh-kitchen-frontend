package com.example.myfrigelocal.network

import android.content.Context
import com.example.myfrigelocal.logging.ApiLog
import okhttp3.MultipartBody
import retrofit2.HttpException

/** Swagger `type` — 문의 vs 신고 */
enum class InquiryApiType(val apiValue: String) {
    INQUIRY("INQUIRY"),
    REPORT("REPORT"),
}

/** Swagger `category` */
enum class InquiryApiCategory(val apiValue: String) {
    RECIPE("RECIPE"),
    AI("AI"),
    OTHER("OTHER"),
}

class InquiryRepository(
    private val api: InquiryApiService = RetrofitClient.inquiriesApi,
) {

    /**
     * @param imageUri optional content Uri string; null이면 이미지 파트 없이 전송
     * @return 성공 시 화면에 표시할 완료 문구
     */
    suspend fun send(
        context: Context,
        type: InquiryApiType,
        category: InquiryApiCategory,
        content: String,
        imageUri: String? = null,
    ): Result<String> {
        val sub = "Inquiry:Send"
        val hasRealImage = !imageUri.isNullOrBlank()
        val imagePart =
            try {
                InquiryMultipartHelper.resolveImagePart(context, imageUri)
            } catch (e: Exception) {
                ApiLog.e(sub, "image part build failed: ${e.message}", e)
                return Result.failure(e)
            }

        ApiLog.i(
            sub,
            "POST /api/v1/inquiries START type=${type.apiValue} category=${category.apiValue} " +
                "contentLen=${content.length} hasImage=$hasRealImage multipart=true",
        )
        return try {
            val response = api.sendInquiry(
                type = type.apiValue,
                category = category.apiValue,
                content = content,
                image = imagePart,
            )
            ApiLog.i(
                sub,
                "envelope status=${response.status} code=${response.code} message=${response.message}",
            )
            if (isBusinessSuccess(response)) {
                val display = completionMessage(type, response.message)
                ApiLog.i(sub, "OK displayMessage=$display")
                Result.success(display)
            } else {
                val msg = response.message?.takeIf { it.isNotBlank() }
                    ?: "요청에 실패했습니다. (${response.code})"
                ApiLog.w(sub, "business failure: $msg")
                Result.failure(IllegalStateException(msg))
            }
        } catch (e: HttpException) {
            ApiLog.e(sub, "HTTP ${e.code()}: ${e.message()}", e)
            Result.failure(e)
        } catch (e: Exception) {
            ApiLog.e(sub, "exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun categoryFromUiLabel(label: String): InquiryApiCategory =
        when (label.trim()) {
            "레시피 관련" -> InquiryApiCategory.RECIPE
            "AI 관련" -> InquiryApiCategory.AI
            else -> InquiryApiCategory.OTHER
        }

    private fun isBusinessSuccess(response: ApiResponse<Unit?>): Boolean {
        if (response.message.equals("Success", ignoreCase = true)) return true
        if (response.code == "COMMON-200") return true
        if (response.status == 0 || response.status in 200..299) return true
        return false
    }

    private fun completionMessage(type: InquiryApiType, serverMessage: String?): String {
        if (serverMessage.equals("Success", ignoreCase = true)) {
            return when (type) {
                InquiryApiType.INQUIRY -> "문의 접수가 완료되었습니다."
                InquiryApiType.REPORT -> "신고 접수가 완료되었습니다."
            }
        }
        return when (type) {
            InquiryApiType.INQUIRY -> "문의 접수가 완료되었습니다."
            InquiryApiType.REPORT -> "신고 접수가 완료되었습니다."
        }
    }
}
