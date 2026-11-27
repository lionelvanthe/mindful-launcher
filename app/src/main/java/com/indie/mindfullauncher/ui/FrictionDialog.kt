package com.indie.mindfullauncher.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

private val FRICTION_INTENTIONS = listOf("Nhắn tin", "Tra cứu", "Giải trí 5p", "Công việc")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GentleFrictionDialog(
    pending: PendingFriction,
    onDismiss: () -> Unit,
    onConfirmLaunch: () -> Unit,
    onSelectIntention: (String?) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val targetProgress = ((5 - pending.secondsRemaining).coerceIn(0, 5)) / 5f
    val smoothProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 850, easing = LinearEasing),
        label = "friction_progress"
    )
    val breathTarget = when {
        pending.secondsRemaining >= 3 -> 1.16f
        pending.secondsRemaining >= 1 -> 0.88f
        else -> 1.0f
    }
    val breathScale by animateFloatAsState(
        targetValue = breathTarget,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "friction_breath"
    )
    val breathCue = when {
        pending.secondsRemaining >= 3 -> "Hít vào..."
        pending.secondsRemaining >= 1 -> "Thở ra..."
        else -> "Sẵn sàng"
    }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Khoảng dừng 5 giây",
                            color = colors.onSurfaceVariant,
                            style = typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bạn chuẩn bị mở ${pending.app.label}",
                            color = colors.onSurface,
                            style = typography.headlineSmall
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .scale(breathScale)
                            .clip(CircleShape)
                            .background(colors.secondary.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (pending.secondsRemaining > 0) "${pending.secondsRemaining}s" else "✓",
                            color = colors.secondary,
                            style = typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = breathCue,
                    color = colors.secondary,
                    style = typography.labelMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { smoothProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50)),
                    color = colors.secondary,
                    trackColor = colors.surfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                val usageLine = when {
                    pending.hasUsagePermission && pending.opensToday > 0 ->
                        "Hôm nay: ${pending.minutesToday} phút · Lần mở thứ ${pending.opensToday}"
                    pending.hasUsagePermission ->
                        "Hôm nay bạn đã dành ${pending.minutesToday} phút cho ứng dụng này."
                    pending.opensToday > 0 ->
                        "Lần mở thứ ${pending.opensToday} hôm nay. Bạn thực sự cần mở ngay lúc này chứ?"
                    else ->
                        "Hãy hít một hơi thật sâu. Bạn thực sự cần mở ứng dụng này ngay lúc này chứ?"
                }
                Text(
                    text = usageLine,
                    color = colors.onSurfaceVariant,
                    style = typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Mục đích mở (tuỳ chọn):",
                    color = colors.onSurfaceVariant,
                    style = typography.labelSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FRICTION_INTENTIONS.forEach { intention ->
                        val selected = pending.selectedIntention == intention
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selected) colors.secondary.copy(alpha = 0.2f) else colors.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (selected) colors.secondary else colors.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onSelectIntention(intention) }
                        ) {
                            Text(
                                text = intention,
                                color = if (selected) colors.secondary else colors.onSurfaceVariant,
                                style = typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (pending.resistedToday > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Đã giữ bình tĩnh ${pending.resistedToday} lần hôm nay",
                        color = colors.primary,
                        style = typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
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
                        val confirmLabel = when {
                            pending.secondsRemaining > 0 -> "Tiếp tục mở (${pending.secondsRemaining}s)"
                            pending.selectedIntention != null -> "Mở ${pending.app.label} (${pending.selectedIntention})"
                            else -> "Mở ${pending.app.label}"
                        }
                        Text(
                            text = confirmLabel,
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
