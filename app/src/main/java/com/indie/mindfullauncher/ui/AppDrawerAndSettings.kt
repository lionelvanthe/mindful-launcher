package com.indie.mindfullauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indie.mindfullauncher.model.AppInfo
import com.indie.mindfullauncher.model.ContextProfile

@Composable
fun AppDrawerScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onTogglePin: (ContextProfile, String) -> Unit,
    onToggleFriction: (String) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(search, state.installedApps) {
        if (search.isBlank()) state.installedApps
        else state.installedApps.filter { it.label.contains(search.trim(), ignoreCase = true) }
    }
    val editableProfile = if (state.activeProfile == ContextProfile.ALL) ContextProfile.WORK else state.activeProfile
    val pinnedSet = state.pinnedByProfile[editableProfile].orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
            }
            Text(
                text = "Tất cả ứng dụng (${filtered.size})",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Lọc nhanh tên ứng dụng...", color = Color(0xFF757575)) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF424242),
                unfocusedBorderColor = Color(0xFF212121),
                focusedContainerColor = Color(0xFF101010),
                unfocusedContainerColor = Color(0xFF101010)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Ghim vào ngữ cảnh '${editableProfile.displayName}' hoặc bật khoảng dừng 5s",
            color = Color(0xFF757575),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(filtered, key = { it.packageName }) { app ->
                val isPinned = app.packageName in pinnedSet
                val isFriction = app.packageName in state.frictionPackages
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAppClick(app) }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.label,
                        color = Color(0xFFF5F5F5),
                        fontSize = 17.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onTogglePin(editableProfile, app.packageName) }) {
                        Icon(
                            Icons.Default.PushPin,
                            contentDescription = "Ghim",
                            tint = if (isPinned) Color.White else Color(0xFF424242)
                        )
                    }
                    IconButton(onClick = { onToggleFriction(app.packageName) }) {
                        Icon(
                            Icons.Default.HourglassBottom,
                            contentDescription = "Khoảng dừng 5s",
                            tint = if (isFriction) Color(0xFFFFB74D) else Color(0xFF424242)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onToggleAutoContext: (Boolean) -> Unit,
    onOpenUsageSettings: () -> Unit,
    onOpenDefaultLauncher: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
            }
            Text("Cài đặt Stillness", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF121212),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Zero-Telemetry", color = Color(0xFF81C784), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Ứng dụng không xin quyền Internet. Mọi ghi chú, thói quen và cấu hình chỉ nằm trên điện thoại của bạn.",
                    color = Color(0xFFBDBDBD),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tự động đổi ngữ cảnh theo giờ", color = Color.White, fontSize = 16.sp)
                Text("Làm việc (8h-17h) • Tối (18h-22h) • Ngủ", color = Color(0xFF9E9E9E), fontSize = 12.sp)
            }
            Switch(
                checked = state.autoContextEnabled,
                onCheckedChange = onToggleAutoContext
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF212121))
        Spacer(modifier = Modifier.height(16.dp))

        Text("Đặt làm màn hình chính mặc định", color = Color.White, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = onOpenDefaultLauncher,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121), contentColor = Color.White)
        ) {
            Text("Mở cài đặt Launcher mặc định")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Thống kê thời gian dùng app (Khoảng dừng 5s)", color = Color.White, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (state.hasUsagePermission) "Đã cấp quyền Usage Access" else "Chưa cấp quyền (chỉ hiện nhắc nhở chung)",
            color = if (state.hasUsagePermission) Color(0xFF81C784) else Color(0xFFFFB74D),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onOpenUsageSettings,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121), contentColor = Color.White)
        ) {
            Text("Cấp quyền Usage Access")
        }
    }
}
