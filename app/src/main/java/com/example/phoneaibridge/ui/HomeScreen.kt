package com.example.phoneaibridge.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.phoneaibridge.Graph
import com.example.phoneaibridge.server.ServerState
import com.example.phoneaibridge.service.AiBridgeForegroundService
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val state by ServerState.snapshot.collectAsState()
    val count by Graph.aiRequestLogRepository.observeCount().collectAsState(initial = 0)
    val error by Graph.aiRequestLogRepository.observeLatestError().collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Phone AI Bridge MVP")
        Text("서버 실행 상태: ${state.running}")
        Text("모델 로드 상태: MockAiEngine (${false})")
        Text("현재 포트: ${state.port}")
        Text("최근 요청 수: $count")
        Text("최근 오류: ${error ?: "없음"}")
        Text("/health 테스트 결과: ${state.lastHealthResult}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { ContextCompat.startForegroundService(context, Intent(context, AiBridgeForegroundService::class.java)) }) { Text("Service 시작") }
            Button(onClick = { context.stopService(Intent(context, AiBridgeForegroundService::class.java)) }) { Text("Service 중지") }
        }
        Button(onClick = { scope.launch { ServerState.setHealthResult(Graph.apiRoutes.handle("GET", "/health", emptyMap(), "", null).body) } }) { Text("/health 테스트") }
    }
}
