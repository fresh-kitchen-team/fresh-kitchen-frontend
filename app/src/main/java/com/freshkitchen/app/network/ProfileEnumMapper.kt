package com.freshkitchen.app.network

// ───────────────────────────────────────────
// UI 표시 문자열 ↔ 백엔드 enum 변환
// ───────────────────────────────────────────
object ProfileEnumMapper {

    // ── 알러지 ──
    private val allergyUiToEnum = mapOf(
        "🥛 유제품" to "MILK",
        "🥚 계란"   to "EGG",
        "🌾 밀·글루텐" to "WHEAT",
        "🦐 갑각류" to "SHELLFISH",
        "🥜 땅콩"   to "PEANUT",
        "🫘 콩·대두" to "SOY"
    )
    private val allergyEnumToUi = allergyUiToEnum.entries.associate { (k, v) -> v to k }

    // ── 음식 스타일 ──
    private val foodStyleUiToEnum = mapOf(
        "🍚 한식"    to "KOREAN",
        "🍣 일식"    to "JAPANESE",
        "🥢 중식"    to "CHINESE",
        "🍝 양식"    to "WESTERN",
        "🥗 채식·비건" to "VEGAN",
        "🍲 담백한 것" to "DIET"
    )
    private val foodStyleEnumToUi = foodStyleUiToEnum.entries.associate { (k, v) -> v to k }

    // ── 조리 도구 ──
    private val cookingToolUiToEnum = mapOf(
        "🍳 프라이팬"   to "PAN",
        "🥘 냄비"      to "POT",
        "🔥 오븐"      to "OVEN",
        "⚡ 전자레인지" to "MICROWAVE",
        "💨 에어프라이어" to "AIR_FRYER",
        "🧊 블렌더"    to "BLENDER"
    )
    private val cookingToolEnumToUi = cookingToolUiToEnum.entries.associate { (k, v) -> v to k }

    // ── UI → enum 변환 (매핑 없는 항목은 제외) ──
    fun allergiesToEnum(uiSet: Collection<String>): List<String> =
        uiSet.mapNotNull { allergyUiToEnum[it] }

    fun foodStylesToEnum(uiSet: Collection<String>): List<String> =
        uiSet.mapNotNull { foodStyleUiToEnum[it] }

    fun cookingToolsToEnum(uiSet: Collection<String>): List<String> =
        uiSet.mapNotNull { cookingToolUiToEnum[it] }

    // ── enum → UI 변환 (모르는 값은 제외) ──
    fun allergiesFromEnum(enumList: List<String>?): Set<String> =
        enumList?.mapNotNull { allergyEnumToUi[it] }?.toSet() ?: emptySet()

    fun foodStylesFromEnum(enumList: List<String>?): Set<String> =
        enumList?.mapNotNull { foodStyleEnumToUi[it] }?.toSet() ?: emptySet()

    /** e.g. `KOREAN` → `한식` (emoji stripped for compact chips). */
    fun foodStyleShortLabelFromEnum(enum: String): String? =
        foodStyleEnumToUi[enum]
            ?.substringAfter(' ')
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    fun cookingToolsFromEnum(enumList: List<String>?): Set<String> =
        enumList?.mapNotNull { cookingToolEnumToUi[it] }?.toSet() ?: emptySet()
}
