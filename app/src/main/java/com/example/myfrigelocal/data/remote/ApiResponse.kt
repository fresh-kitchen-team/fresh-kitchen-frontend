package com.example.myfrigelocal.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Common Swagger wrapper.
 */
data class ApiResponse<T>(
    @SerializedName("status") val status: Int,
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null,
)

/** Business success: numeric `status` 0 or HTTP-style 2xx, or `COMMON-200` style code. */
fun <T> ApiResponse<T>.isBusinessSuccess(): Boolean {
    if (status == 0 || status in 200..299) return true
    if (code?.equals("COMMON-200", ignoreCase = true) == true) return true
    return false
}
