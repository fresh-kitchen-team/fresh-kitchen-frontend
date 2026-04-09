package com.example.myfrigelocal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = BottomNavSelected,
    onPrimary = Color.White,
    surface = BottomNavBarBackground,
)

private val DarkScheme = darkColorScheme(
    primary = BottomNavSelected,
    onPrimary = Color(0xFF003822),
)

@Composable
fun MyFrigeLocalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
