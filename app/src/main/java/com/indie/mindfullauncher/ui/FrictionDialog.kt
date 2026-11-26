package com.indie.mindfullauncher.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun GentleFrictionDialog(
    pending: PendingFriction,
    onDismiss: () -> Unit,
    onConfirmLaunch: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val targetProgress = ((5 - pending.secondsRemaining).coerceIn(0, 5)) / 5f
    val smoothProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 850, easing = LinearEasing),
        label = "friction_progress"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Khoảng dừng 5 giây",
                    color = colors.onSurfaceVariant,
                    style = typography.labelMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bạn chuẩn bị mở ${pending.app.label}",
                    color = colors.onSurface,
                    style = typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { smoothProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50)),
                    color = colors.secondary,
                    trackColor = colors.surfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (pending.hasUsagePermission) {
                        "Hôm nay bạn đã dành ${pending.minutesToday} phút cho ứng dụng này. Bạn thực sự cần mở ngay lúc này chứ?"
                    } else {
                        "Hãy hít một hơi thật sâu. Bạn thực sự cần mở ứng dụng này ngay lúc này chứ?"
                    },
                    color = colors.onSurfaceVariant,
                    style = typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Text("Thôi, quay lại", fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onConfirmLaunch,
                        enabled = pending.secondsRemaining == 0
                    ) {
                        Text(
                            text = if (pending.secondsRemaining > 0) {
                                "Tiếp tục mở (${pending.secondsRemaining}s)"
                            } else {
                                "Mở ${pending.app.label}"
                            },
                            color = if (pending.secondsRemaining > 0) {
                                colors.onSurfaceVariant.copy(alpha = 0.5f)
                            } else {
                                colors.onSurface
                            }
                        )
                    }
                }
            }
        }
    }
}
