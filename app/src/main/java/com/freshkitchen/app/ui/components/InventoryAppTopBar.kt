package com.freshkitchen.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshkitchen.app.ui.theme.FreshGreen

@Composable
fun InventoryTopBarActions(
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    IconButton(onClick = onProfileClick) {
        Icon(Icons.Default.Person, contentDescription = "프로필")
    }
    IconButton(onClick = onSearchClick) {
        Icon(Icons.Default.Search, contentDescription = "검색")
    }
    IconButton(onClick = onSettingsClick) {
        Icon(Icons.Default.Settings, contentDescription = "설정")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreenTopBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon ?: {},
        actions = {
            InventoryTopBarActions(
                onProfileClick = onProfileClick,
                onSearchClick = onSearchClick,
                onSettingsClick = onSettingsClick,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeBrandedTopBar(
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    InventoryScreenTopBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(FreshGreen),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🍳", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "주방 인벤토리",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
        },
        onProfileClick = onProfileClick,
        onSearchClick = onSearchClick,
        onSettingsClick = onSettingsClick,
    )
}
