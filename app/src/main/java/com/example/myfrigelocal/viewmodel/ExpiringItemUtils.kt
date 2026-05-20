package com.example.myfrigelocal.viewmodel

import com.example.myfrigelocal.network.ExpiringItemDto
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// ───────────────────────────────────────────
// 임박 식재료 dday 계산 유틸
//   - 백엔드 dday 가 비정상(예: 항상 0) 으로 올 수 있어
//     expiresAt 으로 클라이언트에서 직접 계산한다.
//   - expiresAt 이 null 이거나 파싱 실패 시 백엔드 dday 로 폴백.
//   - 시각/타임존 영향 제거를 위해 LocalDate(자정 기준) 으로만 비교.
// ───────────────────────────────────────────
fun ExpiringItemDto.computeDday(today: LocalDate = LocalDate.now()): Int {
    val raw = expiresAt
    if (!raw.isNullOrBlank()) {
        val datePart = raw.substringBefore("T")   // "2026-05-20T09:54:20Z" 같은 포맷도 안전하게 처리
        val parsed = runCatching { LocalDate.parse(datePart) }.getOrNull()
        if (parsed != null) {
            return ChronoUnit.DAYS.between(today, parsed).toInt()
        }
    }
    return dday
}

// ───────────────────────────────────────────
// dday → 화면용 라벨
//   양수: "D-3", 0: "D-DAY", 음수: "D+2"(이미 기한 경과)
// ───────────────────────────────────────────
fun formatDdayLabel(dday: Int): String = when {
    dday > 0 -> "D-$dday"
    dday == 0 -> "D-DAY"
    else -> "D+${-dday}"
}
