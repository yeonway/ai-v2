package com.example.phoneaibridge.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.phoneaibridge.Graph
import com.example.phoneaibridge.settings.AppSettings

@Composable
fun SettingsScreen() {
    val initial = remember { Graph.settings.read() }
    var port by remember { mutableStateOf(initial.port.toString()) }
    var token by remember { mutableStateOf(initial.apiToken) }
    var modelPath by remember { mutableStateOf(initial.modelPath) }
    var allowedIp by remember { mutableStateOf(initial.allowedRaspberryPiIp) }
    var saved by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Settings")
        OutlinedTextField(port, { port = it }, label = { Text("서버 포트") })
        OutlinedTextField(token, { token = it }, label = { Text("API Token") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { token = Graph.settings.regenerateToken() }) { Text("토큰 재생성") } }
        OutlinedTextField(modelPath, { modelPath = it }, label = { Text("모델 파일 경로") })
        OutlinedTextField(allowedIp, { allowedIp = it }, label = { Text("허용 Raspberry Pi IP (비우면 토큰만)") })
        Button(onClick = { Graph.settings.save(AppSettings(port.toIntOrNull() ?: 8765, token, modelPath, allowedIp)); saved = "저장됨" }) { Text("설정 저장") }
        Text(saved)
    }
}
