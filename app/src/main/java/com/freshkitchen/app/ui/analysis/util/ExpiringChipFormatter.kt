package com.freshkitchen.app.ui.analysis.util

import com.freshkitchen.app.viewmodel.ExpiringChipUi
import com.freshkitchen.app.viewmodel.formatDdayLabel

internal fun ExpiringChipUi.toChipLabel(): String = "$name (${formatDdayLabel(dday)})"
