package com.freshkitchen.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
internal fun ExpiryChipRow(
    expiresAt: String,
    offsetDays: Int?,
    initialExpiresAt: String,
    onResetExpiry: () -> Unit,
    enabled: Boolean,
) {
    val canReset =
        expiresAt != initialExpiresAt || (offsetDays != null && offsetDays > 0)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        ExpiryChip(expiresAt = expiresAt, offsetDays = offsetDays)
        IconButton(
            onClick = onResetExpiry,
            enabled = enabled && canReset,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "유통기한 초기화",
                tint = if (canReset && enabled) ScanPrimaryDark else ScanTextTertiary,
                modifier = Modifier.size(18.dp),
            )
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

/** 스캔 프레임·크롭 후 업로드(640×1024)와 동일한 가로:세로 비율 */
internal const val RECEIPT_PREVIEW_ASPECT_RATIO = 640f / 1024f

/** 냉장고 스캔 업로드(1024×1365) 비율 */
internal const val FRIDGE_PREVIEW_ASPECT_RATIO = 1024f / 1365f

/** 스캔 결과 — 작은 썸네일 카드 (탭 시 [ReceiptFullscreenImageDialog]) */
@Composable
internal fun ReceiptScanPreviewThumbnail(
    previewModel: Any,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageTitle: String = "영수증 이미지",
    previewAspectRatio: Float = RECEIPT_PREVIEW_ASPECT_RATIO,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = ScanCardBg,
        border = BorderStroke(1.dp, ScanDivider),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ScanChipBg,
                border = BorderStroke(1.dp, ScanDivider),
            ) {
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .aspectRatio(previewAspectRatio)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = previewModel),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = imageTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ScanTextPrimary,
                )
                Text(
                    text = "탭하여 크게 보기",
                    fontSize = 12.sp,
                    color = ScanTextTertiary,
                )
            }
            Icon(
                imageVector = Icons.Outlined.ZoomIn,
                contentDescription = null,
                tint = ScanTextTertiary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** 영수증 전체 화면 미리보기 */
@Composable
internal fun ReceiptFullscreenImageDialog(
    previewModel: Any,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = previewModel),
                contentDescription = "영수증 이미지 확대",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 48.dp),
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0x99000000)),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "닫기",
                    tint = Color.White,
                )
            }
        }
    }
}

/** 스캔 결과 화면 공통 하단 취소/저장 바 (시스템 내비게이션 바 inset 포함) */
@Composable
internal fun ScanResultBottomBar(
    cancelEnabled: Boolean,
    onCancel: () -> Unit,
    saveLabel: String,
    saveEnabled: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = ScanCardBg,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ScanChipBg),
                    onClick = onCancel,
                    enabled = cancelEnabled,
                ) {
                    Text(
                        text = "취소",
                        color = ScanTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }
                Button(
                    modifier = Modifier
                        .weight(2f)
                        .height(46.dp),
                    enabled = saveEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScanPrimary,
                        disabledContainerColor = Color(0xFFCBD5E1),
                    ),
                    shape = RoundedCornerShape(14.dp),
                    onClick = onSave,
                ) {
                    Text(
                        text = saveLabel,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}
