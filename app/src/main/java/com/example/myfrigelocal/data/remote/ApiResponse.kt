package com.example.myfrigelocal.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Common Swagger wrapper.
 *
 * TODO: Confirm success semantics with backend (`status` values, HTTP codes).
 */
data class ApiResponse<T>(
    @SerializedName("status") val status: Int,
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null,
)
