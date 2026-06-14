package com.freshkitchen.app.ui.analysis.util

import androidx.compose.ui.graphics.Color
import com.freshkitchen.app.viewmodel.StorageTipCategoryType

internal fun StorageTipCategoryType.headerEmoji(): String = when (this) {
    StorageTipCategoryType.VEGETABLE_FRUIT -> "🥦"
    StorageTipCategoryType.DAIRY_DRINK -> "🥛"
    StorageTipCategoryType.MEAT_SEAFOOD -> "🍖"
    StorageTipCategoryType.ETC -> "🍱"
}

internal fun StorageTipCategoryType.headerBackgroundColor(): Color = when (this) {
    StorageTipCategoryType.VEGETABLE_FRUIT -> Color(0xFFEAF7F2)
    StorageTipCategoryType.DAIRY_DRINK -> Color(0xFFFFF8E1)
    StorageTipCategoryType.MEAT_SEAFOOD -> Color(0xFFFFE9EA)
    StorageTipCategoryType.ETC -> Color(0xFFEAF2FF)
}

internal fun StorageTipCategoryType.iconBackgroundColor(): Color = headerBackgroundColor()
