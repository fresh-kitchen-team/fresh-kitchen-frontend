package com.freshkitchen.app.network

import android.content.Context
import com.freshkitchen.app.data.ChatRoomSectionMapper
import com.freshkitchen.app.data.auth.AuthTokenStore
import com.freshkitchen.app.logging.ApiLog
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

    /** GET `/api/v1/inquiries` — 로그인 사용자 문의·신고 내역 (최신순). */
    suspend fun getInquiries(): Result<List<InquirySummaryDto>> {
        val sub = "Inquiry:List"
        ApiLog.i(sub, "GET /api/v1/inquiries START")
        return try {
            val response = api.getInquiries()
            ApiLog.i(
                sub,
                "envelope status=${response.status} code=${response.code} count=${response.data?.size ?: 0}",
            )
            if (!isBusinessSuccess(response)) {
                val msg = response.message?.takeIf { it.isNotBlank() }
                    ?: "목록을 불러오지 못했습니다. (${response.code})"
                ApiLog.w(sub, "business failure: $msg")
                return Result.failure(IllegalStateException(msg))
            }
            val sorted = response.data.orEmpty().sortedByDescending { dto ->
                ChatRoomSectionMapper.parseInstant(dto.createdAt)?.toEpochMilli() ?: 0L
            }
            Result.success(sorted)
        } catch (e: HttpException) {
            ApiLog.e(sub, "HTTP ${e.code()}: ${e.message()}", e)
            Result.failure(e)
        } catch (e: Exception) {
            ApiLog.e(sub, "exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    /** GET `/api/v1/inquiries/{inquiryId}` — 문의 본문 + 관리자 답변. */
    suspend fun getInquiryDetail(inquiryId: Long): Result<InquiryDetailDto> {
        val sub = "api/InquiryDetail"
        val path = "/api/v1/inquiries/$inquiryId"
        val hasToken = !AuthTokenStore.getAccessToken().isNullOrBlank()
        ApiLog.i(sub, "GET $path START tokenPresent=$hasToken")
        return try {
            val response = api.getInquiryDetail(inquiryId)
            val detail = response.data
            ApiLog.i(
                sub,
                "GET $path RESPONSE http=200 envelopeStatus=${response.status} " +
                    "envelopeCode=${response.code} envelopeMessage=${response.message} " +
                    "hasData=${detail != null} inquiryStatus=${detail?.status} " +
                    "hasImageUrl=${!detail?.imageUrl.isNullOrBlank()} " +
                    "imageUrl=${detail?.imageUrl.orEmpty().take(120)}",
            )
            if (!isBusinessSuccess(response)) {
                val msg = response.message?.takeIf { it.isNotBlank() }
                    ?: "상세를 불러오지 못했습니다. (${response.code})"
                ApiLog.w(
                    sub,
                    "GET $path BUSINESS_FAIL envelopeStatus=${response.status} " +
                        "envelopeCode=${response.code} message=$msg",
                )
                return Result.failure(IllegalStateException(msg))
            }
            if (detail == null) {
                ApiLog.w(sub, "GET $path FAIL data=null (문의 없음)")
                return Result.failure(IllegalStateException("문의를 찾을 수 없습니다."))
            }
            ApiLog.i(
                sub,
                "GET $path OK id=${detail.id} type=${detail.type} category=${detail.category} " +
                    "contentLen=${detail.content.length} adminReplyLen=${detail.adminReply?.length ?: 0}",
            )
            Result.success(detail)
        } catch (e: HttpException) {
            val httpCode = e.code()
            ApiLog.e(
                sub,
                "GET $path HTTP_FAIL http=$httpCode (${httpStatusLabel(httpCode)}) " +
                    "message=${e.message()}",
                e,
            )
            Result.failure(e)
        } catch (e: Exception) {
            ApiLog.e(sub, "GET $path EXCEPTION ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun httpStatusLabel(code: Int): String = when (code) {
        401 -> "Unauthorized"
        403 -> "Forbidden"
        404 -> "Not Found"
        503 -> "Service Unavailable"
        in 500..599 -> "Server Error"
        else -> "HTTP $code"
    }

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

    private fun isBusinessSuccess(response: ApiResponse<*>): Boolean {
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
