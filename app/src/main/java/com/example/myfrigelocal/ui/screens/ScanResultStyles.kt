package com.example.myfrigelocal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

// 공용 컬러 토큰
internal val ScanPrimary = Color(0xFF22C55E)
internal val ScanPrimaryDark = Color(0xFF16A34A)
internal val ScanPrimarySoft = Color(0xFFDCFCE7)
internal val ScanPrimaryTint = Color(0xFFF0FDF4)

internal val ScanTextPrimary = Color(0xFF0F172A)
internal val ScanTextSecondary = Color(0xFF64748B)
internal val ScanTextTertiary = Color(0xFF94A3B8)
internal val ScanDivider = Color(0xFFE2E8F0)
internal val ScanChipBg = Color(0xFFF1F5F9)
internal val ScanCardBg = Color(0xFFFFFFFF)
internal val ScanScreenBg = Color(0xFFF8FAFC)

@Composable
internal fun QuickDayChip(
    days: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (enabled) ScanPrimarySoft else ScanChipBg
    val fg = if (enabled) ScanPrimaryDark else ScanTextTertiary
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = modifier
            .height(44.dp)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "+${days}일",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = fg,
            )
        }
    }
}

@Composable
internal fun CompactNameField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    placeholder: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = ScanTextPrimary,
            fontSize = 15.sp,
        ),
        placeholder = {
            Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = ScanTextTertiary)
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ScanPrimary,
            unfocusedBorderColor = ScanDivider,
            disabledBorderColor = ScanDivider,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
        ),
    )
}

@Composable
internal fun StorageDropdownChip(
    storageType: String,
    onChange: (String) -> Unit,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            shape = RoundedCornerShape(100.dp),
            color = ScanChipBg,
            modifier = Modifier
                .height(30.dp)
                .clickable(enabled = enabled) { expanded = true },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 10.dp, end = 6.dp),
            ) {
                Text(text = storageTypeEmoji(storageType), fontSize = 12.sp)
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = storageTypeToDisplay(storageType),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ScanTextPrimary,
                )
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = "보관장소",
                    tint = ScanTextSecondary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            StorageTypeOptions.forEach { (code, label) ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(storageTypeEmoji(code))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(label)
                        }
                    },
                    onClick = {
                        onChange(code)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
internal fun ExpiryChip(
    expiresAt: String,
    offsetDays: Int?,
) {
    val parsed = runCatching { LocalDate.parse(expiresAt) }.getOrNull()
    val today = LocalDate.now()
    val daysFromToday = parsed?.let { ChronoUnit.DAYS.between(today, it).toInt() }
    val (bg, fg) = when {
        parsed == null -> ScanChipBg to ScanTextSecondary
        daysFromToday != null && daysFromToday < 0 -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
        daysFromToday != null && daysFromToday <= 3 -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        else -> ScanPrimarySoft to ScanPrimaryDark
    }

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = bg,
        modifier = Modifier.height(30.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(12.dp),
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = if (parsed != null) formatExpiryLabel(parsed, daysFromToday) else "미설정",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = fg,
            )
            if (offsetDays != null && offsetDays > 0) {
                Spacer(modifier = Modifier.size(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.7f))
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                ) {
                    Text(
                        text = "+${offsetDays}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = fg,
                    )
                }
            }
        }
    }
}

private fun formatExpiryLabel(date: LocalDate, daysFromToday: Int?): String {
    val md = date.format(DateTimeFormatter.ofPattern("M.d", Locale.KOREAN))
    val dow = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
    val tail = when {
        daysFromToday == null -> ""
        daysFromToday == 0 -> " · D-Day"
        daysFromToday > 0 -> " · D-${daysFromToday}"
        else -> " · D+${-daysFromToday}"
    }
    return "$md($dow)$tail"
}

internal fun storageTypeEmoji(code: String): String = when (code.uppercase()) {
    "FRIDGE" -> "🧊"
    "FREEZER" -> "❄️"
    "PANTRY" -> "🌡️"
    else -> "📦"
}
