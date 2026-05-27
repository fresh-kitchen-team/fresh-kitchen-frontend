package com.example.myfrigelocal.ui.screens.help

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.myfrigelocal.logging.ApiLog
import com.example.myfrigelocal.ui.screens.chat.ChatDesign

@Composable
fun InquiryAttachmentImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(14.dp)
    val logSub = "api/InquiryImage"

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .listener(
                onStart = {
                    ApiLog.i(logSub, "image GET START url=${imageUrl.take(160)}")
                },
                onSuccess = { _: ImageRequest, _: SuccessResult ->
                    ApiLog.i(logSub, "image GET OK url=${imageUrl.take(160)}")
                },
                onError = { _: ImageRequest, result: ErrorResult ->
                    val cause = result.throwable
                    ApiLog.e(
                        logSub,
                        "image GET FAIL url=${imageUrl.take(160)} " +
                            "error=${cause.javaClass.simpleName}: ${cause.message}",
                        cause,
                    )
                },
            )
            .build(),
        contentDescription = "첨부 이미지",
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .border(1.dp, ChatDesign.BorderSoft, shape),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F8F7)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = ChatDesign.ChatPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF6F8F7)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.BrokenImage,
                    contentDescription = null,
                    tint = ChatDesign.TextMuted,
                )
            }
        },
    )
}
