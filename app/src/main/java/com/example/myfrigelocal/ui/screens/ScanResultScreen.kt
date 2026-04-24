package com.example.myfrigelocal.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myfrigelocal.navigation.ScanNav

private val PrimaryGreen = Color(0xFF00C853)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    navController: NavController,
) {
    val imageUriString =
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<String?>(ScanNav.keyImageUri)

    var name by rememberSaveable { mutableStateOf("신선한 우유") }
    var storage by rememberSaveable { mutableStateOf("냉장실") }
    var expiration by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("유제품") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F9FC),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Small top grabber like the reference.
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 2.dp, bottom = 10.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFE5E7EB)),
            )

            Text(
                text = "스캔 결과 확인",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "인식된 정보를 확인하고 수정해주세요",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header card with preview + chips + check.
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEFF3F8)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (imageUriString.isNullOrBlank()) {
                            Image(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                            )
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = Uri.parse(imageUriString),
                                ),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp, end = 12.dp),
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Chip(text = category, tint = Color(0xFF2563EB))
                            Spacer(modifier = Modifier.size(8.dp))
                            Chip(text = storage, tint = PrimaryGreen)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            FieldLabel(text = "이름", required = true)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel(text = "보관장소", required = true)
            OutlinedTextField(
                value = storage,
                onValueChange = { storage = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel(text = "유통기한", required = true)
            OutlinedTextField(
                value = expiration,
                onValueChange = { expiration = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                placeholder = { Text("YYYY.MM.DD") },
            )
            Text(
                text = "10일 남음",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 6.dp, start = 4.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel(text = "카테고리", required = true)
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F5F9)),
                    onClick = {
                        // Tell ScanScreen to reset its SUCCESS state before returning.
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(ScanNav.keyReset, true)
                        navController.popBackStack()
                    },
                ) {
                    Text(
                        text = "취소",
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        // UI-only: no persistence requested.
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                ) {
                    Text(
                        text = "저장",
                        color = Color(0xFF062115),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(
    text: String,
    required: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 2.dp, bottom = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827),
        )
        if (required) {
            Text(
                text = " *",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFEF4444),
            )
        }
    }
}

@Composable
private fun Chip(
    text: String,
    tint: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .border(width = 1.dp, color = tint.copy(alpha = 0.18f), shape = RoundedCornerShape(999.dp))
            .background(tint.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = tint,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

