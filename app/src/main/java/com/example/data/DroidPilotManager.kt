package com.example.data

import android.graphics.Bitmap
import com.example.model.LogEntry
import com.example.model.LogLevel
import com.example.model.ScreenReadResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DroidPilotManager {

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private val _isAccessibilityConnected = MutableStateFlow(false)
    val isAccessibilityConnected: StateFlow<Boolean> = _isAccessibilityConnected.asStateFlow()

    private val _currentPackage = MutableStateFlow("None")
    val currentPackage: StateFlow<String> = _currentPackage.asStateFlow()

    private val _isTikTokDetected = MutableStateFlow(false)
    val isTikTokDetected: StateFlow<Boolean> = _isTikTokDetected.asStateFlow()

    private val _screenResult = MutableStateFlow<ScreenReadResult?>(null)
    val screenResult: StateFlow<ScreenReadResult?> = _screenResult.asStateFlow()

    private val _latestScreenshot = MutableStateFlow<Bitmap?>(null)
    val latestScreenshot: StateFlow<Bitmap?> = _latestScreenshot.asStateFlow()

    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    fun log(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        val entry = LogEntry(
            timestamp = timeFormat.format(Date()),
            tag = tag,
            message = message,
            level = level
        )
        val currentList = _logs.value
        val updated = if (currentList.size >= 500) {
            currentList.drop(1) + entry
        } else {
            currentList + entry
        }
        _logs.value = updated
    }

    fun clearLogs() {
        _logs.value = emptyList()
        log("SYSTEM", "Logs cleared", LogLevel.INFO)
    }

    fun setAccessibilityConnected(connected: Boolean) {
        _isAccessibilityConnected.value = connected
        if (connected) {
            log("ACCESSIBILITY", "[ACCESSIBILITY CONNECTED] Service is active and ready", LogLevel.SUCCESS)
        } else {
            log("ACCESSIBILITY", "[ACCESSIBILITY DISCONNECTED] Service was disconnected", LogLevel.WARNING)
        }
    }

    fun updateCurrentPackage(packageName: String) {
        if (_currentPackage.value == packageName) return
        _currentPackage.value = packageName

        val isTikTok = isTikTokPackage(packageName)
        _isTikTokDetected.value = isTikTok

        if (isTikTok) {
            log("DETECTOR", "[TIKTOK DETECTED] Foreground app is TikTok ($packageName)", LogLevel.TIKTOK)
        } else {
            log("DETECTOR", "[PACKAGE DETECTED] Current package: $packageName", LogLevel.INFO)
        }
    }

    fun isTikTokPackage(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return false
        val pkg = packageName.lowercase()
        return pkg == "com.zhiliaoapp.musically" ||
                pkg == "com.ss.android.ugc.trill" ||
                pkg == "com.zhiliaoapp.musically.go" ||
                pkg == "com.ss.android.ugc.aweme" ||
                pkg.contains("musically") ||
                pkg.contains("tiktok")
    }

    fun setScreenResult(result: ScreenReadResult) {
        _screenResult.value = result
        log("SCANNER", "[NODES FOUND: ${result.totalNodesCount}] Clickable: ${result.clickableCount} on package: ${result.packageName}", LogLevel.SUCCESS)
    }

    fun setScreenshot(bitmap: Bitmap?) {
        _latestScreenshot.value = bitmap
        if (bitmap != null) {
            log("SCREENSHOT", "[SCREENSHOT CAPTURED] ${bitmap.width}x${bitmap.height} px", LogLevel.SUCCESS)
        }
    }

    fun setOverlayActive(active: Boolean) {
        _isOverlayActive.value = active
    }
}
