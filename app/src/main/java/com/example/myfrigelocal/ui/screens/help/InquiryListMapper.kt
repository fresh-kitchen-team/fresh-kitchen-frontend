package com.example.myfrigelocal.ui.screens.help

import com.example.myfrigelocal.data.ChatRoomSectionMapper
import com.example.myfrigelocal.network.InquiryDetailDto
import com.example.myfrigelocal.network.InquirySummaryDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class InquiryListItemUi(
    val id: Long,
    val typeLabel: String,
    val categoryLabel: String,
    val contentPreview: String,
    val statusLabel: String,
    val isAnswered: Boolean,
    val isReport: Boolean,
    val createdAtLabel: String,
    val sortKey: Long,
)

data class InquiryDetailUi(
    val id: Long,
    val typeLabel: String,
    val categoryLabel: String,
    val content: String,
    val imageUrl: String?,
    val statusLabel: String,
    val isAnswered: Boolean,
    val isReport: Boolean,
    val adminReply: String,
    val createdAtLabel: String,
    val answeredAtLabel: String?,
)

object InquiryListMapper {

    fun parseCreatedAtMillis(iso: String?): Long =
        ChatRoomSectionMapper.parseInstant(iso)?.toEpochMilli() ?: 0L

    fun toUi(dto: InquirySummaryDto): InquiryListItemUi {
        val type = dto.type.uppercase()
        val category = dto.category.uppercase()
        val status = dto.status.uppercase()
        return InquiryListItemUi(
            id = dto.id,
            typeLabel = typeLabel(type),
            categoryLabel = categoryLabel(category),
            contentPreview = dto.contentPreview.trim(),
            statusLabel = statusLabel(status),
            isAnswered = status == "ANSWERED",
            isReport = type == "REPORT",
            createdAtLabel = formatTimestamp(dto.createdAt),
            sortKey = parseCreatedAtMillis(dto.createdAt),
        )
    }

    fun toDetailUi(dto: InquiryDetailDto): InquiryDetailUi {
        val type = dto.type.uppercase()
        val category = dto.category.uppercase()
        val status = dto.status.uppercase()
        return InquiryDetailUi(
            id = dto.id,
            typeLabel = typeLabel(type),
            categoryLabel = categoryLabel(category),
            content = dto.content.trim(),
            imageUrl = dto.imageUrl?.trim()?.takeIf { it.isNotBlank() },
            statusLabel = statusLabel(status),
            isAnswered = status == "ANSWERED",
            isReport = type == "REPORT",
            adminReply = dto.adminReply?.trim().orEmpty(),
            createdAtLabel = formatTimestamp(dto.createdAt),
            answeredAtLabel = dto.answeredAt?.let { formatTimestamp(it) },
        )
    }

    fun typeLabel(type: String): String = when (type.uppercase()) {
        "REPORT" -> "신고"
        "INQUIRY" -> "문의"
        else -> type
    }

    fun categoryLabel(category: String): String = when (category.uppercase()) {
        "RECIPE" -> "레시피"
        "AI" -> "AI"
        "OTHER" -> "기타"
        else -> category
    }

    fun statusLabel(status: String): String = when (status.uppercase()) {
        "ANSWERED" -> "답변 완료"
        "PENDING" -> "답변 대기"
        else -> status
    }

    fun formatTimestamp(iso: String?): String {
        val instant = ChatRoomSectionMapper.parseInstant(iso) ?: return ""
        val zdt = instant.atZone(ZoneId.systemDefault())
        val now = Instant.now().atZone(ZoneId.systemDefault())
        return when {
            zdt.toLocalDate() == now.toLocalDate() -> {
                zdt.format(DateTimeFormatter.ofPattern("오늘 HH:mm", Locale.KOREAN))
            }
            zdt.year == now.year -> {
                zdt.format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN))
            }
            else -> {
                zdt.format(DateTimeFormatter.ofPattern("yyyy.M.d", Locale.KOREAN))
            }
        }
    }
}
