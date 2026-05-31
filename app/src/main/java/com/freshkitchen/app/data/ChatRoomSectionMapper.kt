package com.freshkitchen.app.data

import com.freshkitchen.app.data.remote.dto.ChatRoomSummaryDto
import java.time.Instant

/**
 * Sidebar section labels — must match server buckets from GET `/ai/v1/chat/room`
 * (`today` / `last7Days` / `last30Days`). Grouping is **not** derived from dates on the client.
 */
object ChatRoomSectionMapper {

    const val SECTION_TODAY = "오늘"
    const val SECTION_LAST_7 = "지난 7일"
    const val SECTION_LAST_30 = "지난 30일"

    fun parseInstant(iso: String?): Instant? {
        if (iso.isNullOrBlank()) return null
        return try {
            Instant.parse(iso)
        } catch (_: Exception) {
            null
        }
    }

    /** Newer [ChatRoomSummaryDto.updatedAt] first within a bucket. */
    fun compareRooms(a: ChatRoomSummaryDto, b: ChatRoomSummaryDto): Int {
        val ta = parseInstant(a.updatedAt)?.toEpochMilli() ?: 0L
        val tb = parseInstant(b.updatedAt)?.toEpochMilli() ?: 0L
        return tb.compareTo(ta)
    }
}
