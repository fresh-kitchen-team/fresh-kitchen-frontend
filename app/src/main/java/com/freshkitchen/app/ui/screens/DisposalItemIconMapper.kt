package com.freshkitchen.app.ui.screens

import androidx.annotation.DrawableRes
import com.freshkitchen.app.R

/**
 * 폐기 가이드 품목명 → 로컬 아이콘.
 * API에 이미지 필드가 없어 [name] 키워드로 매핑하고, 없으면 null(이모지 fallback).
 */
@DrawableRes
fun recyclingItemIconRes(name: String): Int? {
    val n = name.trim()
    if (n.isEmpty()) return null

    return when {
        n.contains("달걀") || n.contains("계란") -> R.drawable.ic_disposal_egg
        n.contains("뼈") -> R.drawable.ic_disposal_bone
        n.contains("조개") || n.contains("게") -> R.drawable.ic_disposal_shellfish
        n.contains("씨") || n.contains("씨앗") -> R.drawable.ic_disposal_seed
        n.contains("양파") ||
            n.contains("마늘") ||
            n.contains("옥수수") ||
            n.contains("고춧대") ||
            n.contains("채소") ||
            n.contains("껍질") -> R.drawable.ic_disposal_veg
        else -> null
    }
}
