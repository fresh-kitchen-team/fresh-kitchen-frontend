package com.example.myfrigelocal.network

import com.google.gson.annotations.SerializedName

/** GET `/api/v1/inquiries` — one row in `data`. */
data class InquirySummaryDto(
    @SerializedName("id") val id: Long,
    @SerializedName("type") val type: String,
    @SerializedName("category") val category: String,
    @SerializedName("contentPreview") val contentPreview: String,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String,
)

/** GET `/api/v1/inquiries/{inquiryId}` — `data` payload. */
data class InquiryDetailDto(
    @SerializedName("id") val id: Long,
    @SerializedName("type") val type: String,
    @SerializedName("category") val category: String,
    @SerializedName("content") val content: String,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("adminReply") val adminReply: String? = null,
    @SerializedName("answeredAt") val answeredAt: String? = null,
    @SerializedName("createdAt") val createdAt: String,
)
