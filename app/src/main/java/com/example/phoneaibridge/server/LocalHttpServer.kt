package com.example.phoneaibridge.server

import com.example.phoneaibridge.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class LocalHttpServer(private val settingsStore: SettingsStore, private val routes: ApiRoutes) {
    private val running = AtomicBoolean(false)
    private var serverSocket: ServerSocket? = null
    private var scope: CoroutineScope? = null

    fun start(): Boolean {
        if (!running.compareAndSet(false, true)) return false
        val port = settingsStore.read().port
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope?.launch {
            try {
                serverSocket = ServerSocket(port)
                ServerState.markRunning(port)
                while (running.get()) {
                    val socket = serverSocket?.accept() ?: break
                    launch { handleClient(socket) }
                }
            } catch (_: Exception) {
                running.set(false)
                ServerState.markStopped()
            }
        }
        return true
    }

    fun stop() {
        if (!running.compareAndSet(true, false)) return
        runCatching { serverSocket?.close() }
        scope?.cancel()
        ServerState.markStopped()
    }

    fun isRunning(): Boolean = running.get()

    private fun handleClient(socket: Socket) {
        socket.use {
            val reader = BufferedReader(InputStreamReader(it.getInputStream()))
            val requestLine = reader.readLine() ?: return
            val parts = requestLine.split(" ")
            if (parts.size < 2) return
            val headers = mutableMapOf<String, String>()
            var line: String?
            var contentLength = 0
            while (true) {
                line = reader.readLine()
                if (line.isNullOrEmpty()) break
                val key = line.substringBefore(':').trim().lowercase(Locale.US)
                val value = line.substringAfter(':', "").trim()
                headers[key] = value
                if (key == "content-length") contentLength = value.toIntOrNull() ?: 0
            }
            val bodyChars = CharArray(contentLength)
            if (contentLength > 0) reader.read(bodyChars, 0, contentLength)
            val response = runBlocking { routes.handle(parts[0], parts[1], headers, String(bodyChars), it.inetAddress.hostAddress) }
            writeResponse(it, response)
        }
    }

    private fun writeResponse(socket: Socket, response: HttpResponse) {
        val bytes = response.body.toByteArray(Charsets.UTF_8)
        val statusText = when (response.code) { 200 -> "OK"; 400 -> "Bad Request"; 401 -> "Unauthorized"; 404 -> "Not Found"; else -> "Internal Server Error" }
        socket.getOutputStream().write("HTTP/1.1 ${response.code} $statusText\r\nContent-Type: ${response.contentType}\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n".toByteArray(Charsets.UTF_8))
        socket.getOutputStream().write(bytes)
        socket.getOutputStream().flush()
    }
}
