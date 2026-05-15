package com.example.myfrigelocal.data.scan

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.myfrigelocal.data.auth.AuthTokenStore
import com.example.myfrigelocal.BuildConfig
import com.example.myfrigelocal.logging.ApiLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import java.io.IOException
import java.util.concurrent.TimeUnit

class ScanRepository(context: Context) {

    private val appContext = context.applicationContext

    private val gson =
        GsonBuilder()
            .registerTypeAdapter(
                ReceiptImageScanApiResponse::class.java,
                ReceiptImageScanApiResponseDeserializer(),
            )
            .create()

    private val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.SCAN_API_BASE_URL)
            .client(buildClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    private val api: ScanApiService = retrofit.create(ScanApiService::class.java)
    private val itemsApi: ItemsApiService = retrofit.create(ItemsApiService::class.java)

    init {
        if (BuildConfig.DEBUG) {
            ApiLog.i(
                "Scan",
                "ScanRepository init apiConfigured=${isApiConfigured()} baseUrl=${BuildConfig.SCAN_API_BASE_URL}",
            )
        }
    }

    private fun buildClient(): OkHttpClient {
        val builder =
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(scanAuthInterceptor())
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.HEADERS
                },
            )
        }
        return builder.build()
    }

    /** Spring Security JWT: `Authorization: Bearer <access_token>` */
    private fun scanAuthInterceptor(): Interceptor =
        Interceptor { chain ->
            val original = chain.request()
            val raw = AuthTokenStore.getAccessToken()?.trim().orEmpty()
            val authHeader =
                when {
                    raw.isEmpty() -> null
                    raw.startsWith("Bearer ", ignoreCase = true) -> raw
                    else -> "Bearer $raw"
                }
            val request =
                if (authHeader != null) {
                    original.newBuilder().header("Authorization", authHeader).build()
                } else {
                    original
                }
            chain.proceed(request)
        }

    suspend fun scanIngredientImage(
        imageUri: Uri,
        localPreviewUriString: String?,
    ): Result<ScanResultUiModel> =
        withContext(Dispatchers.IO) {
            ApiLog.i("Scan", "ingredient-image START baseUrl=${BuildConfig.SCAN_API_BASE_URL} uri=$imageUri")
            runCatching {
                val part = imageUri.toImagePart()
                ApiLog.d("Scan", "ingredient-image multipart ready (OkHttp 로그로 요청/응답 헤더 확인)")
                val envelope = api.scanIngredientImage(file = part)
                ApiLog.i(
                    "Scan",
                    "ingredient-image envelope status=${envelope.status} code=${envelope.code} message=${envelope.message}",
                )
                val data = envelope.unwrapIngredientPayload()
                if (data.recognizedItems.isNullOrEmpty()) {
                    throw ScanApiException(RECOGNITION_EMPTY_MESSAGE)
                }
                val model = mapIngredientScanToUiModel(data, localPreviewUriString)
                ApiLog.i(
                    "Scan",
                    "ingredient-image OK items=${model.items.size} sourceType=${model.sourceType} hasRemoteImage=${!model.remotePreviewImageUrl.isNullOrBlank()}",
                )
                model
            }
                .onFailure { e -> ApiLog.e("Scan", "ingredient-image exception: ${e.message}", e) }
                .mapScanFailures()
        }

    suspend fun scanReceiptImage(
        imageUri: Uri,
        localPreviewUriString: String?,
    ): Result<ScanResultUiModel> =
        withContext(Dispatchers.IO) {
            ApiLog.i("Scan", "receipt-image START baseUrl=${BuildConfig.SCAN_API_BASE_URL} uri=$imageUri")
            runCatching {
                val part = imageUri.toImagePart()
                ApiLog.d("Scan", "receipt-image multipart ready (OkHttp 로그로 요청/응답 헤더 확인)")
                val envelope = api.scanReceiptImage(file = part)
                ApiLog.i(
                    "Scan",
                    "receipt-image envelope status=${envelope.status} code=${envelope.code} message=${envelope.message}",
                )
                val data = envelope.unwrapReceiptPayload()
                if (data.recognizedItems.isNullOrEmpty()) {
                    ApiLog.w(
                        "Scan",
                        "receipt-image: recognizedItems 비어 있음 (OCR 결과 없음 또는 응답 스키마 확인). storeName=${data.storeName} ocrLen=${data.ocrText?.length ?: 0}",
                    )
                }
                val model = mapReceiptScanToUiModel(data, localPreviewUriString)
                ApiLog.i(
                    "Scan",
                    "receipt-image OK items=${model.items.size} purchasedAt=${model.purchasedAt} sourceType=${model.purchasedAtSourceType}",
                )
                model
            }
                .onFailure { e -> ApiLog.e("Scan", "receipt-image exception: ${e.message}", e) }
                .mapScanFailures()
        }

    suspend fun fetchItemStorages(): Result<List<StorageListItemDto>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val env = itemsApi.getStorages()
                if (!isScanEnvelopeSuccess(env.status, env.code)) {
                    throw ScanApiException(
                        env.message?.takeIf { it.isNotBlank() }
                            ?: env.code
                            ?: "보관함 목록을 불러오지 못했습니다.",
                    )
                }
                env.data ?: emptyList()
            }
                .onFailure { e -> ApiLog.e("Items", "getStorages exception: ${e.message}", e) }
                .mapScanFailures()
        }

    suspend fun createItem(request: CreateItemRequest): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val env = itemsApi.createItem(request)
                if (!isScanEnvelopeSuccess(env.status, env.code)) {
                    throw ScanApiException(
                        env.message?.takeIf { it.isNotBlank() } ?: env.code ?: "저장에 실패했습니다.",
                    )
                }
                Unit
            }
                .onFailure { e -> ApiLog.e("Items", "createItem exception: ${e.message}", e) }
                .mapScanFailures()
        }

    private fun IngredientImageScanApiResponse.unwrapIngredientPayload(): IngredientImageScanData {
        if (!isScanEnvelopeSuccess(status, code)) {
            throw ScanApiException(
                message?.takeIf { it.isNotBlank() } ?: code ?: "식재료 스캔에 실패했습니다. (status=$status)",
            )
        }
        return data ?: throw ScanApiException("응답 데이터가 없습니다.")
    }

    private fun ReceiptImageScanApiResponse.unwrapReceiptPayload(): ReceiptImageScanData {
        if (!isScanEnvelopeSuccess(status, code)) {
            throw ScanApiException(
                message?.takeIf { it.isNotBlank() } ?: code ?: "영수증 스캔에 실패했습니다. (status=$status)",
            )
        }
        return data ?: throw ScanApiException("응답 데이터가 없습니다.")
    }

    private fun Uri.toImagePart(): MultipartBody.Part {
        val resolver = appContext.contentResolver
        val mime = (resolver.getType(this) ?: "image/jpeg").let {
            if (it.startsWith("image/")) it else "image/jpeg"
        }
        val fileName = displayNameOrFallback()
        val bytes =
            resolver.openInputStream(this)?.use { stream -> stream.readBytes() }
                ?: throw ScanApiException("이미지를 읽을 수 없습니다.")
        if (bytes.isEmpty()) {
            throw ScanApiException("이미지 파일이 비어 있습니다.")
        }
        val body = bytes.toRequestBody(mime.toMediaType())
        return MultipartBody.Part.createFormData("file", fileName, body)
    }

    private fun Uri.displayNameOrFallback(): String {
        if (scheme == android.content.ContentResolver.SCHEME_FILE) {
            lastPathSegment?.takeIf { it.isNotBlank() }?.let { return it }
        }
        val cursor =
            appContext.contentResolver.query(
                this,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) {
                    val n = it.getString(idx)
                    if (!n.isNullOrBlank()) return n
                }
            }
        }
        return "scan.jpg"
    }

    companion object {
        const val RECOGNITION_EMPTY_MESSAGE =
            "인식된 식재료가 없습니다. 다시 촬영하거나 다른 이미지를 선택해주세요."

        /**
         * When false, the UI uses local simulation instead of calling the backend.
         * Treats blank URL or explicit placeholder host as "not configured".
         */
        fun isApiConfigured(): Boolean {
            val u = BuildConfig.SCAN_API_BASE_URL.trim()
            if (u.isEmpty()) return false
            if (u.contains("placeholder.invalid", ignoreCase = true)) return false
            return true
        }
    }
}

class ScanApiException(message: String) : Exception(message)

/**
 * 백엔드가 Swagger 예시처럼 `status: 0` 이거나, 실서버처럼 `status: 200` + `code: COMMON-200` 형태로 성공을 줄 수 있음.
 */
private fun isScanEnvelopeSuccess(status: Int, code: String?): Boolean {
    if (status == 0) return true
    if (status in 200..299) return true
    val c = code?.trim().orEmpty()
    if (c.equals("COMMON-200", ignoreCase = true)) return true
    return false
}

private fun <T> Result<T>.mapScanFailures(): Result<T> =
    fold(
        onSuccess = { Result.success(it) },
        onFailure = { e ->
            val msg =
                when (e) {
                    is ScanApiException -> e.message
                    is HttpException -> {
                        when (e.code()) {
                            401 -> "로그인이 필요하거나 권한이 없습니다."
                            else -> {
                                val body = e.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }
                                body ?: e.message() ?: "요청에 실패했습니다. (${e.code()})"
                            }
                        }
                    }
                    is IOException -> "네트워크 연결을 확인해 주세요."
                    else -> e.message ?: "스캔 요청에 실패했습니다."
                }
            ApiLog.w("Scan", "API 실패(사용자 메시지): $msg | cause=${e.javaClass.simpleName}")
            Result.failure(Exception(msg, e))
        },
    )
