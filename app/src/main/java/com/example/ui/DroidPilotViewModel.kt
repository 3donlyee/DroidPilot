package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DroidPilotManager
import com.example.model.LogLevel
import com.example.model.NodeInfoItem
import com.example.service.DroidPilotAccessibilityService
import com.example.service.FloatingOverlayService
import com.example.service.MediaProjectionService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DroidPilotViewModel(application: Application) : AndroidViewModel(application) {

    val logs = DroidPilotManager.logs
    val isAccessibilityConnected = DroidPilotManager.isAccessibilityConnected
    val currentPackage = DroidPilotManager.currentPackage
    val isTikTokDetected = DroidPilotManager.isTikTokDetected
    val screenResult = DroidPilotManager.screenResult
    val latestScreenshot = DroidPilotManager.latestScreenshot
    val isOverlayActive = DroidPilotManager.isOverlayActive

    private val knownTikTokPackages = listOf(
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically.go",
        "com.ss.android.ugc.aweme"
    )

    fun openAccessibilitySettings(context: Context) {
        try {
            DroidPilotManager.log("SETTINGS", "Opening Accessibility Settings...", LogLevel.INFO)
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            DroidPilotManager.log("SETTINGS", "Failed to open accessibility settings: ${e.message}", LogLevel.ERROR)
        }
    }

    fun openTikTok(context: Context) {
        DroidPilotManager.log("ACTION", "[ACTION] Launching TikTok app...", LogLevel.ACTION)
        val pm = context.packageManager
        var launched = false

        for (pkg in knownTikTokPackages) {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                DroidPilotManager.log("ACTION", "[RESULT] Successfully launched TikTok package: $pkg", LogLevel.SUCCESS)
                launched = true
                break
            }
        }

        if (!launched) {
            DroidPilotManager.log("ACTION", "[WARNING] TikTok is not installed on this device with known package names. Trying store intent.", LogLevel.WARNING)
            try {
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.zhiliaoapp.musically")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(storeIntent)
            } catch (e: Exception) {
                DroidPilotManager.log("ACTION", "[ERROR] Could not open store: ${e.message}", LogLevel.ERROR)
            }
        }
    }

    fun checkTikTok() {
        val current = DroidPilotManager.currentPackage.value
        val isTT = DroidPilotManager.isTikTokDetected.value
        DroidPilotManager.log(
            "CHECK",
            if (isTT) "[TIKTOK DETECTED] TikTok is currently ACTIVE in foreground ($current)"
            else "[CHECK RESULT] TikTok NOT in foreground. Active package: $current",
            if (isTT) LogLevel.TIKTOK else LogLevel.INFO
        )
    }

    fun readScreen() {
        val service = DroidPilotAccessibilityService.instance
        if (service != null) {
            service.readCurrentScreenNodes()
        } else {
            DroidPilotManager.log("SCANNER", "[ERROR] Accessibility Service is not connected. Please enable it in Settings.", LogLevel.ERROR)
        }
    }

    fun testSwipeUp(durationMs: Long = 280L) {
        val service = DroidPilotAccessibilityService.instance
        if (service != null) {
            service.swipeUp(durationMs)
        } else {
            DroidPilotManager.log("ACTION", "[ERROR] Accessibility Service is not connected.", LogLevel.ERROR)
        }
    }

    fun testSwipeDown(durationMs: Long = 280L) {
        val service = DroidPilotAccessibilityService.instance
        if (service != null) {
            service.swipeDown(durationMs)
        } else {
            DroidPilotManager.log("ACTION", "[ERROR] Accessibility Service is not connected.", LogLevel.ERROR)
        }
    }

    fun testTapCenter() {
        val service = DroidPilotAccessibilityService.instance
        if (service != null) {
            service.tapCenterScreen()
        } else {
            DroidPilotManager.log("ACTION", "[ERROR] Accessibility Service is not connected.", LogLevel.ERROR)
        }
    }

    fun testTapCoordinate(x: Float, y: Float) {
        val service = DroidPilotAccessibilityService.instance
        if (service != null) {
            service.tapCoordinate(x, y)
        } else {
            DroidPilotManager.log("ACTION", "[ERROR] Accessibility Service is not connected.", LogLevel.ERROR)
        }
    }

    fun testBack() {
        val service = DroidPilotAccessibilityService.instance
        if (service != null) {
            service.triggerBack()
        } else {
            DroidPilotManager.log("ACTION", "[ERROR] Accessibility Service is not connected.", LogLevel.ERROR)
        }
    }

    fun testInputText(text: String) {
        val service = DroidPilotAccessibilityService.instance
        if (service != null) {
            service.inputText(text)
        } else {
            DroidPilotManager.log("ACTION", "[ERROR] Accessibility Service is not connected.", LogLevel.ERROR)
        }
    }

    fun clickFirstClickable(filterText: String?) {
        val service = DroidPilotAccessibilityService.instance
        if (service != null) {
            service.clickFirstClickableNode(filterText)
        } else {
            DroidPilotManager.log("ACTION", "[ERROR] Accessibility Service is not connected.", LogLevel.ERROR)
        }
    }

    fun takeNativeScreenshot() {
        val service = DroidPilotAccessibilityService.instance
        if (service != null) {
            service.takeAccessibilityScreenshot { }
        } else {
            DroidPilotManager.log("SCREENSHOT", "[ERROR] Accessibility Service is not connected.", LogLevel.ERROR)
        }
    }

    fun startMediaProjectionCapture(resultCode: Int, data: Intent, metrics: DisplayMetrics, context: Context) {
        val serviceIntent = Intent(context, MediaProjectionService::class.java).apply {
            action = MediaProjectionService.ACTION_START_CAPTURE
            putExtra(MediaProjectionService.EXTRA_RESULT_CODE, resultCode)
            putExtra(MediaProjectionService.EXTRA_RESULT_DATA, data)
            putExtra(MediaProjectionService.EXTRA_WIDTH, metrics.widthPixels)
            putExtra(MediaProjectionService.EXTRA_HEIGHT, metrics.heightPixels)
            putExtra(MediaProjectionService.EXTRA_DENSITY, metrics.densityDpi)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    fun toggleFloatingOverlay(context: Context, onRequestPermission: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                DroidPilotManager.log("OVERLAY", "Overlay permission required to display floating controller", LogLevel.WARNING)
                onRequestPermission()
                return
            }
        }

        if (FloatingOverlayService.isRunning) {
            FloatingOverlayService.stop(context)
        } else {
            FloatingOverlayService.start(context)
        }
    }

    fun runAutomatedSequence(context: Context, onTick: (String) -> Unit) {
        viewModelScope.launch {
            DroidPilotManager.log("SEQUENCE", "[SEQUENCE STARTED] Beginning 4-step TikTok automation test...", LogLevel.ACTION)
            onTick("Launching TikTok...")
            openTikTok(context)
            delay(3500)

            onTick("Reading screen nodes...")
            val service = DroidPilotAccessibilityService.instance
            if (service != null) {
                service.readCurrentScreenNodes()
                delay(1500)

                onTick("Executing Swipe Up (Next Video)...")
                service.swipeUp()
                delay(2500)

                onTick("Taking Verification Screenshot...")
                service.takeAccessibilityScreenshot { }
                delay(1000)

                onTick("Sequence Finished!")
                DroidPilotManager.log("SEQUENCE", "[SEQUENCE COMPLETED] All steps executed successfully!", LogLevel.SUCCESS)
            } else {
                DroidPilotManager.log("SEQUENCE", "[SEQUENCE FAILED] Accessibility service was not running.", LogLevel.ERROR)
                onTick("Failed: Service not running")
            }
        }
    }

    fun clearLogs() {
        DroidPilotManager.clearLogs()
    }
}
