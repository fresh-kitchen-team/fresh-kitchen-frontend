package com.freshkitchen.app.ui.screens

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/** 스캔 카메라 화면 공용 강조색 (결과 화면의 [ScanPrimary] 와는 별도 톤). */
internal val PrimaryGreen = Color(0xFF00C853)

// ── 상단 탭 ────────────────────────────────────────────────

@Composable
internal fun ScanTopBar(
    selectedTab: ScanTab,
    onSelect: (ScanTab) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        ScanTabSegmented(
            selectedTab = selectedTab,
            onSelect = onSelect,
            modifier = Modifier.widthIn(max = 560.dp),
        )
    }
}

@Composable
private fun ScanTabSegmented(
    selectedTab: ScanTab,
    onSelect: (ScanTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < 340.dp
        Surface(
            shape = CircleShape,
            color = Color(0xFFF1F5F9),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(modifier = Modifier.padding(3.dp)) {
                SegmentChip(
                    modifier = Modifier.weight(1f),
                    selected = selectedTab == ScanTab.Ingredient,
                    icon = Icons.Outlined.PhotoCamera,
                    label = "식재료",
                    compact = compact,
                    onClick = { onSelect(ScanTab.Ingredient) },
                )
                SegmentChip(
                    modifier = Modifier.weight(1f),
                    selected = selectedTab == ScanTab.Fridge,
                    icon = Icons.Outlined.Kitchen,
                    label = "냉장고",
                    compact = compact,
                    onClick = { onSelect(ScanTab.Fridge) },
                )
                SegmentChip(
                    modifier = Modifier.weight(1f),
                    selected = selectedTab == ScanTab.Receipt,
                    icon = Icons.Filled.Receipt,
                    label = "영수증",
                    compact = compact,
                    onClick = { onSelect(ScanTab.Receipt) },
                )
            }
        }
    }
}

@Composable
private fun SegmentChip(
    modifier: Modifier,
    selected: Boolean,
    icon: ImageVector,
    label: String,
    compact: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) PrimaryGreen else Color.Transparent
    val fg = if (selected) Color.White else Color(0xFF64748B)
    Surface(
        shape = CircleShape,
        color = bg,
        modifier = modifier
            .height(if (compact) 34.dp else 36.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 6.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = fg,
                modifier = Modifier.size(if (compact) 14.dp else 15.dp),
            )
            if (!compact) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    color = fg,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
            }
        }
    }
}

// ── 카메라 프레임 가이드 ─────────────────────────────────────

@Composable
internal fun ScanFrameBox(
    frameStyle: ScanFrameStyle,
    widthFraction: Float,
    aspectRatio: Float,
    maxHeightFraction: Float,
    onFrameBoundsInWindow: (RectF) -> Unit,
) {
    // 너비 비율로 프레임 크기를 잡되, 작은 화면에서 하단 영역과 겹치지 않도록
    // 결과 높이를 maxHeight 기준으로 한 번 더 클램프한다.
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val maxW: Dp = maxWidth
        val maxH: Dp = maxHeight

        var frameW: Dp = maxW * widthFraction
        var frameH: Dp = frameW / aspectRatio

        val maxFrameH = maxH * maxHeightFraction
        if (frameH > maxFrameH) {
            frameH = maxFrameH
            frameW = frameH * aspectRatio
        }

        val frameModifier = Modifier
            .size(frameW, frameH)
            .onGloballyPositioned { coordinates ->
                val r = coordinates.boundsInWindow()
                onFrameBoundsInWindow(RectF(r.left, r.top, r.right, r.bottom))
            }

        when (frameStyle) {
            ScanFrameStyle.Corners -> CornerFrame(modifier = frameModifier)
            ScanFrameStyle.FridgeInterior -> FridgeInteriorFrame(modifier = frameModifier)
            ScanFrameStyle.RoundedRect -> RoundedRectFrame(modifier = frameModifier)
        }
    }
}

/** [ScanFrameBox] 에 전달하는 프레임 가이드 스타일. */
enum class ScanFrameStyle { Corners, FridgeInterior, RoundedRect }

@Composable
private fun CornerFrame(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cornerLen = minOf(w, h) * 0.14f
        val stroke = 3.dp.toPx()

        drawCornerL(0f, 0f, cornerLen, stroke, isTopLeft = true)
        drawCornerL(w, 0f, cornerLen, stroke, isTopRight = true)
        drawCornerL(0f, h, cornerLen, stroke, isBottomLeft = true)
        drawCornerL(w, h, cornerLen, stroke, isBottomRight = true)
    }
}

private fun DrawScope.drawCornerL(
    anchorX: Float,
    anchorY: Float,
    length: Float,
    strokeWidth: Float,
    isTopLeft: Boolean = false,
    isTopRight: Boolean = false,
    isBottomLeft: Boolean = false,
    isBottomRight: Boolean = false,
) {
    val xDir = if (isTopLeft || isBottomLeft) 1f else -1f
    val yDir = if (isTopLeft || isTopRight) 1f else -1f
    drawLine(
        color = PrimaryGreen,
        start = Offset(anchorX, anchorY),
        end = Offset(anchorX + xDir * length, anchorY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = PrimaryGreen,
        start = Offset(anchorX, anchorY),
        end = Offset(anchorX, anchorY + yDir * length),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
}

@Composable
private fun FridgeInteriorFrame(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .border(width = 2.dp, color = PrimaryGreen, shape = RoundedCornerShape(16.dp)),
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 1.dp.toPx()
            val guideColor = PrimaryGreen.copy(alpha = 0.35f)
            val thirdH = size.height / 3f
            drawLine(
                color = guideColor,
                start = Offset(12.dp.toPx(), thirdH),
                end = Offset(size.width - 12.dp.toPx(), thirdH),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = guideColor,
                start = Offset(12.dp.toPx(), thirdH * 2f),
                end = Offset(size.width - 12.dp.toPx(), thirdH * 2f),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun RoundedRectFrame(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(width = 2.dp, color = PrimaryGreen, shape = RoundedCornerShape(20.dp)),
    )
}

// ── 오버레이 ─────────────────────────────────────────────────

@Composable
internal fun ScanAiRecognitionLoadingOverlay(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(Color(0xCC0B1220)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(72.dp),
                color = PrimaryGreen,
                trackColor = PrimaryGreen.copy(alpha = 0.22f),
                strokeWidth = 5.dp,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "AI 인식중",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "잠시 기다려 주세요.",
                color = Color(0xFFE5E7EB),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun ScanSuccessOverlay() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Text(
            text = "스캔 완료!",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(PrimaryGreen),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✓",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** 스캔 결과가 비어 있을 때(영수증/냉장고) 다시 촬영을 유도하는 다이얼로그. */
@Composable
internal fun EmptyScanResultDialog(
    icon: ImageVector,
    subtitle: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 320.dp)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "인식된 품목이 없습니다",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = "확인",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ── 하단 촬영/갤러리 영역 ────────────────────────────────────

@Composable
internal fun ScanBottomSection(
    modifier: Modifier = Modifier,
    selectedTab: ScanTab,
    controlsEnabled: Boolean = true,
    onPrimaryAction: () -> Unit,
    onPickFromGallery: () -> Unit,
) {
    val innerIcon = when (selectedTab) {
        ScanTab.Ingredient -> Icons.Outlined.PhotoCamera
        ScanTab.Fridge -> Icons.Outlined.Kitchen
        ScanTab.Receipt -> Icons.Outlined.QrCodeScanner
    }

    Box(modifier = modifier) {
        ShutterButton(
            enabled = controlsEnabled,
            innerIcon = innerIcon,
            onClick = onPrimaryAction,
            modifier = Modifier.align(Alignment.Center),
        )
        FloatingGalleryButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
            enabled = controlsEnabled,
            onClick = onPickFromGallery,
        )
    }
}

@Composable
private fun FloatingGalleryButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFFF1F5F9))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = "Gallery",
            tint = Color(0xFF475569),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ShutterButton(
    enabled: Boolean = true,
    innerIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val innerBg = if (enabled) PrimaryGreen else PrimaryGreen.copy(alpha = 0.45f)
    Box(
        modifier = modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color(0xFFDCFCE7))
            .border(width = 2.dp, color = PrimaryGreen.copy(alpha = 0.4f), shape = CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(innerBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = innerIcon,
                contentDescription = "Capture",
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
