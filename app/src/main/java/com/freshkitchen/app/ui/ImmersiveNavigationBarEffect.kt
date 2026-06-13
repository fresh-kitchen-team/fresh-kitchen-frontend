package com.freshkitchen.app.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 시스템 내비게이션 바(3버튼/제스처 바)를 숨기고, 화면 하단에서 위로 스와이프하면
 * 일시적으로 다시 표시합니다.
 *
 * 하단 고정 UI에는 [androidx.compose.foundation.layout.navigationBarsPadding]을 함께 적용하면
 * 바가 나타날 때 inset에 맞춰 자연스럽게 위로 올라갑니다.
 */
@Composable
fun ImmersiveNavigationBarEffect(enabled: Boolean = true) {
    if (!enabled) return

    val view = LocalView.current
    if (view.isInEditMode) return

    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        val previousBehavior = controller.systemBarsBehavior

        WindowCompat.setDecorFitsSystemWindows(window, false)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.navigationBars())

        onDispose {
            WindowCompat.setDecorFitsSystemWindows(window, true)
            controller.systemBarsBehavior = previousBehavior
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }
}
