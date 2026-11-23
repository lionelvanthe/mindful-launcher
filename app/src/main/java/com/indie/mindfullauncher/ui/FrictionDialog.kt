package com.indie.mindfullauncher.ui

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun GentleFrictionDialog(
    pending: PendingFriction,
    onDismiss: () -> Unit,
    onConfirmLaunch: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF141414),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Khoảng dừng 5 giây",
                    color = Color(0xFF9E9E9E),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bạn chuẩn bị mở ${pending.app.label}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (pending.hasUsagePermission) {
                        "Hôm nay bạn đã dành ${pending.minutesToday} phút cho ứng dụng này. Bạn thực sự cần mở ngay lúc này chứ?"
                    } else {
                        "Hãy hít một hơi thật sâu. Bạn thực sự cần mở ứng dụng này ngay lúc này chứ?"
                    },
                    color = Color(0xFFBDBDBD),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Thôi, quay lại", fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(8.dp))
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
                            color = if (pending.secondsRemaining > 0) Color(0xFF616161) else Color(0xFFBDBDBD)
                        )
                    }
                }
            }
        }
    }
}
