package com.freshkitchen.app.ui.analysis.util

import com.freshkitchen.app.viewmodel.DisposalItemUi
import com.freshkitchen.app.viewmodel.DisposalWasteTone

internal fun DisposalItemUi.disposalToneEmoji(): String = when (tone) {
    DisposalWasteTone.General -> "🗑️"
    DisposalWasteTone.Food -> "🍽️"
    DisposalWasteTone.Other -> "♻️"
}
