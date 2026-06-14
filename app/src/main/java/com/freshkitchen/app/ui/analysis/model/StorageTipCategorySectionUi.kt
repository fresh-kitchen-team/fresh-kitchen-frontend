package com.freshkitchen.app.ui.analysis.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.freshkitchen.app.viewmodel.StorageTipCategoryType
import com.freshkitchen.app.viewmodel.StorageTipUi

@Immutable
internal data class StorageTipCategorySectionUi(
    val type: StorageTipCategoryType,
    val title: String,
    val headerEmoji: String,
    val headerBackground: Color,
    val previewTip: StorageTipUi?,
)
