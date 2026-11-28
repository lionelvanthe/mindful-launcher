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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val reducedMotion = rememberReducedMotion()
    val targetProgress = ((5 - pending.secondsRemaining).coerceIn(0, 5)) / 5f
    val smoothProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 850, easing = LinearEasing),
        label = "friction_progress"
    )
    val breathTarget = if (reducedMotion) {
        1.0f
    } else {
        when {
            pending.secondsRemaining >= 3 -> 1.16f
            pending.secondsRemaining >= 1 -> 0.88f
            else -> 1.0f
        }
    }
    val breathScale by animateFloatAsState(
        targetValue = breathTarget,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 900, easing = FastOutSlowInEasing),
        label = "friction_breath"
    )
    val breathCue = when {
        pending.secondsRemaining >= 3 -> "Hít vào..."
        pending.secondsRemaining >= 1 -> "Thở ra..."
        else -> "Sẵn sàng"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.25f)),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppIcon(
                        packageName = pending.app.packageName,
                        contentDescription = pending.app.label,
                        size = 40.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Khoảng dừng 5 giây",
                            color = colors.onSurfaceVariant,
                            style = typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Bạn chuẩn bị mở ${pending.app.label}",
                            color = colors.onSurface,
                            style = typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mindful concentric breathing orb
                Box(
                    modifier = Modifier.size(104.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(breathScale)
                            .clip(CircleShape)
                            .background(colors.secondary.copy(alpha = 0.12f))
                    )
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(breathScale)
                            .clip(CircleShape)
                            .background(colors.secondary.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (pending.secondsRemaining > 0) "${pending.secondsRemaining}s" else "✓",
                            color = colors.secondary,
                            style = typography.headlineMedium.copy(
                                fontSize = 24.sp,
                                fontFeatureSettings = "tnum"
                            ),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = breathCue,
                    color = colors.secondary,
                    style = typography.labelLarge
                )
                Spacer(modifier = Modifier.height(10.dp))
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
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = usageLine,
                        color = colors.onSurfaceVariant,
                        style = typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Mục đích mở (tuỳ chọn)",
                    color = colors.onSurfaceVariant,
                    style = typography.labelSmall,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FRICTION_INTENTIONS.forEach { intention ->
                        val selected = pending.selectedIntention == intention
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selected) colors.secondary.copy(alpha = 0.22f) else colors.surfaceVariant.copy(alpha = 0.55f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (selected) colors.secondary else colors.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 36.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { onSelectIntention(intention) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = intention,
                                    color = if (selected) colors.secondary else colors.onSurfaceVariant,
                                    style = typography.labelMedium
                                )
                            }
                        }
                    }
                }

                if (pending.resistedToday > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF81C784).copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "Đã giữ bình tĩnh ${pending.resistedToday} lần hôm nay",
                            color = Color(0xFF81C784),
                            style = typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Text("Thôi, quay lại", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = onConfirmLaunch,
                    enabled = pending.secondsRemaining == 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
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
