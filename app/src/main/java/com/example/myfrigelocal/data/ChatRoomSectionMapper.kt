package com.example.myfrigelocal.data

import com.example.myfrigelocal.data.remote.dto.ChatRoomDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Maps each room to a sidebar section using [ChatRoomDto.updatedAt] (ISO-8601, e.g. …Z).
 *
 * Sections (Swagger returns a flat list; grouping is client-side only):
 * - [SECTION_TODAY] — same calendar day as "now" in [zoneId]
 * - [SECTION_LAST_7] — updated on a day before today but within the last 7 days
 * - [SECTION_LAST_30] — older than 7 days but within 30 days, **or** older than 30 days (still one bucket)
 *
 * TODO: If product wants "오늘 / 어제 / 7일 이전" copy instead of "지난 7일 / 지난 30일", adjust labels in UI only.
 */
object ChatRoomSectionMapper {

    const val SECTION_TODAY = "오늘"
    const val SECTION_LAST_7 = "지난 7일"
    const val SECTION_LAST_30 = "지난 30일"

    private val zoneId: ZoneId = ZoneId.of("Asia/Seoul")

    fun sectionFor(room: ChatRoomDto): String {
        val updated = parseInstant(room.updatedAt) ?: return SECTION_LAST_30
        val today = LocalDate.now(zoneId)
        val updatedDay = updated.atZone(zoneId).toLocalDate()
        val daysAgo = ChronoUnit.DAYS.between(updatedDay, today)
        return when {
            updatedDay == today -> SECTION_TODAY
            daysAgo in 1L..7L -> SECTION_LAST_7
            else -> SECTION_LAST_30
        }
    }

    fun parseInstant(iso: String): Instant? = try {
        Instant.parse(iso)
    } catch (_: Exception) {
        null
    }

    /**
     * Sort key: newer [ChatRoomDto.updatedAt] first.
     */
    fun compareRooms(a: ChatRoomDto, b: ChatRoomDto): Int {
        val ta = parseInstant(a.updatedAt)?.toEpochMilli() ?: 0L
        val tb = parseInstant(b.updatedAt)?.toEpochMilli() ?: 0L
        return tb.compareTo(ta)
    }
}
