package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.DroidPilotManager
import com.example.model.LogLevel
import com.example.model.NodeInfoItem
import com.example.model.ScreenReadResult

class DroidPilotAccessibilityService : AccessibilityService() {

    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        var instance: DroidPilotAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        DroidPilotManager.setAccessibilityConnected(true)
        DroidPilotManager.log("ACCESSIBILITY", "[INIT] Accessibility service configuration attached", LogLevel.INFO)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkg = event.packageName?.toString() ?: return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            DroidPilotManager.updateCurrentPackage(pkg)
        }
    }

    override fun onInterrupt() {
        DroidPilotManager.log("ACCESSIBILITY", "[INTERRUPT] Accessibility service interrupted", LogLevel.WARNING)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        DroidPilotManager.setAccessibilityConnected(false)
    }

    // -------------------------------------------------------------
    // GESTURES
    // -------------------------------------------------------------

    fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300L, onComplete: ((Boolean) -> Unit)? = null) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs.coerceAtLeast(100L))
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        DroidPilotManager.log(
            "GESTURE",
            "[ACTION] SWIPE from (${startX.toInt()}, ${startY.toInt()}) to (${endX.toInt()}, ${endY.toInt()}) in ${durationMs}ms",
            LogLevel.ACTION
        )

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                DroidPilotManager.log("GESTURE", "[RESULT] SWIPE SUCCESS", LogLevel.SUCCESS)
                mainHandler.post { onComplete?.invoke(true) }
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                DroidPilotManager.log("GESTURE", "[RESULT] SWIPE CANCELLED", LogLevel.WARNING)
                mainHandler.post { onComplete?.invoke(false) }
            }
        }, null)
    }

    fun swipeUp(durationMs: Long = 280L, onComplete: ((Boolean) -> Unit)? = null) {
        val displayMetrics = Resources.getSystem().displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()

        val startX = width / 2f
        val startY = height * 0.78f
        val endX = width / 2f
        val endY = height * 0.22f

        DroidPilotManager.log("ACTION", "[ACTION] SWIPE UP (Next Video gesture)", LogLevel.ACTION)
        swipe(startX, startY, endX, endY, durationMs, onComplete)
    }

    fun swipeDown(durationMs: Long = 280L, onComplete: ((Boolean) -> Unit)? = null) {
        val displayMetrics = Resources.getSystem().displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()

        val startX = width / 2f
        val startY = height * 0.25f
        val endX = width / 2f
        val endY = height * 0.75f

        DroidPilotManager.log("ACTION", "[ACTION] SWIPE DOWN (Previous Video gesture)", LogLevel.ACTION)
        swipe(startX, startY, endX, endY, durationMs, onComplete)
    }

    fun tapCoordinate(x: Float, y: Float, onComplete: ((Boolean) -> Unit)? = null) {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 80L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        DroidPilotManager.log("GESTURE", "[ACTION] TAP at (${x.toInt()}, ${y.toInt()})", LogLevel.ACTION)

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                DroidPilotManager.log("GESTURE", "[RESULT] TAP SUCCESS at (${x.toInt()}, ${y.toInt()})", LogLevel.SUCCESS)
                mainHandler.post { onComplete?.invoke(true) }
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                DroidPilotManager.log("GESTURE", "[RESULT] TAP CANCELLED", LogLevel.WARNING)
                mainHandler.post { onComplete?.invoke(false) }
            }
        }, null)
    }

    fun tapCenterScreen(onComplete: ((Boolean) -> Unit)? = null) {
        val metrics = Resources.getSystem().displayMetrics
        val centerX = metrics.widthPixels / 2f
        val centerY = metrics.heightPixels / 2f
        tapCoordinate(centerX, centerY, onComplete)
    }

    fun triggerBack(): Boolean {
        DroidPilotManager.log("ACTION", "[ACTION] BACK BUTTON", LogLevel.ACTION)
        val success = performGlobalAction(GLOBAL_ACTION_BACK)
        if (success) {
            DroidPilotManager.log("ACTION", "[RESULT] BACK ACTION EXECUTED", LogLevel.SUCCESS)
        } else {
            DroidPilotManager.log("ACTION", "[RESULT] BACK ACTION FAILED", LogLevel.ERROR)
        }
        return success
    }

    fun triggerHome(): Boolean {
        DroidPilotManager.log("ACTION", "[ACTION] HOME BUTTON", LogLevel.ACTION)
        val success = performGlobalAction(GLOBAL_ACTION_HOME)
        return success
    }

    // -------------------------------------------------------------
    // SCREEN INSPECTION / TREE TRAVERSAL
    // -------------------------------------------------------------

    fun readCurrentScreenNodes(): ScreenReadResult {
        DroidPilotManager.log("SCANNER", "[ACTION] Reading active window accessibility tree...", LogLevel.ACTION)
        val root = rootInActiveWindow
        if (root == null) {
            DroidPilotManager.log("SCANNER", "[WARNING] rootInActiveWindow is null. App might be in transit or secure screen.", LogLevel.WARNING)
            val emptyResult = ScreenReadResult(
                packageName = DroidPilotManager.currentPackage.value,
                totalNodesCount = 0,
                clickableCount = 0,
                nodes = emptyList()
            )
            DroidPilotManager.setScreenResult(emptyResult)
            return emptyResult
        }

        val pkgName = root.packageName?.toString() ?: DroidPilotManager.currentPackage.value
        DroidPilotManager.updateCurrentPackage(pkgName)

        val nodeList = mutableListOf<NodeInfoItem>()
        var clickableCount = 0

        fun traverse(node: AccessibilityNodeInfo?, depth: Int) {
            if (node == null || depth > 40) return

            val bounds = Rect()
            node.getBoundsInScreen(bounds)

            val text = node.text?.toString()
            val desc = node.contentDescription?.toString()
            val isClickable = node.isClickable
            val isEditable = node.isEditable
            val isEnabled = node.isEnabled
            val viewId = node.viewIdResourceName
            val className = node.className?.toString() ?: "Unknown"

            if (isClickable) clickableCount++

            val item = NodeInfoItem(
                id = "${node.hashCode()}_${depth}_${bounds.left}_${bounds.top}",
                className = className.substringAfterLast('.'),
                text = text,
                contentDescription = desc,
                isClickable = isClickable,
                isEnabled = isEnabled,
                isEditable = isEditable,
                viewId = viewId,
                bounds = bounds,
                depth = depth
            )
            nodeList.add(item)

            for (i in 0 until node.childCount) {
                val child = try {
                    node.getChild(i)
                } catch (e: Exception) {
                    null
                }
                if (child != null) {
                    traverse(child, depth + 1)
                }
            }
        }

        traverse(root, 0)

        val result = ScreenReadResult(
            packageName = pkgName,
            totalNodesCount = nodeList.size,
            clickableCount = clickableCount,
            nodes = nodeList
        )
        DroidPilotManager.setScreenResult(result)

        // Log brief summary
        val sampleElements = nodeList
            .filter { !it.text.isNullOrBlank() || !it.contentDescription.isNullOrBlank() }
            .take(5)
            .joinToString(separator = "\n") { item ->
                val label = item.text ?: item.contentDescription
                " - [${item.className}] \"$label\" (clickable=${item.isClickable}, bounds=${item.bounds.flattenToString()})"
            }

        if (sampleElements.isNotEmpty()) {
            DroidPilotManager.log("SCANNER", "Sample detected elements:\n$sampleElements", LogLevel.INFO)
        }

        return result
    }

    // -------------------------------------------------------------
    // NODE INTERACTIONS
    // -------------------------------------------------------------

    fun clickFirstClickableNode(matchingText: String? = null): Boolean {
        val root = rootInActiveWindow ?: return false
        var targetNode: AccessibilityNodeInfo? = null

        fun find(node: AccessibilityNodeInfo?) {
            if (node == null || targetNode != null) return

            val matches = if (matchingText.isNullOrBlank()) {
                node.isClickable
            } else {
                val text = node.text?.toString().orEmpty()
                val desc = node.contentDescription?.toString().orEmpty()
                node.isClickable && (text.contains(matchingText, ignoreCase = true) || desc.contains(matchingText, ignoreCase = true))
            }

            if (matches) {
                targetNode = node
                return
            }

            for (i in 0 until node.childCount) {
                find(node.getChild(i))
            }
        }

        find(root)

        val found = targetNode
        return if (found != null) {
            val label = found.text ?: found.contentDescription ?: found.className
            DroidPilotManager.log("ACTION", "[ACTION] Clicking node: $label", LogLevel.ACTION)
            val success = found.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            DroidPilotManager.log("ACTION", "[RESULT] Click action: ${if (success) "SUCCESS" else "FAILED"}", if (success) LogLevel.SUCCESS else LogLevel.ERROR)
            success
        } else {
            DroidPilotManager.log("ACTION", "[RESULT] No matching clickable node found for: $matchingText", LogLevel.WARNING)
            false
        }
    }

    fun inputText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        var targetNode: AccessibilityNodeInfo? = null

        fun findEditable(node: AccessibilityNodeInfo?) {
            if (node == null || targetNode != null) return
            if (node.isEditable || node.isFocused) {
                targetNode = node
                return
            }
            for (i in 0 until node.childCount) {
                findEditable(node.getChild(i))
            }
        }

        findEditable(root)

        val target = targetNode
        return if (target != null) {
            DroidPilotManager.log("ACTION", "[ACTION] Typing text: \"$text\"", LogLevel.ACTION)
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            val success = target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            DroidPilotManager.log("ACTION", "[RESULT] Set text: ${if (success) "SUCCESS" else "FAILED"}", if (success) LogLevel.SUCCESS else LogLevel.ERROR)
            success
        } else {
            DroidPilotManager.log("ACTION", "[RESULT] No editable or focused field found", LogLevel.WARNING)
            false
        }
    }

    // -------------------------------------------------------------
    // ACCESSIBILITY SCREENSHOT (Android 11+ / API 30+)
    // -------------------------------------------------------------

    fun takeAccessibilityScreenshot(onBitmapCaptured: (Bitmap?) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            DroidPilotManager.log("SCREENSHOT", "[ACTION] Requesting native Accessibility screenshot (API 30+)...", LogLevel.ACTION)
            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                mainHandler::post,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshotResult: ScreenshotResult) {
                        try {
                            val hardwareBuffer = screenshotResult.hardwareBuffer
                            val colorSpace = screenshotResult.colorSpace
                            val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)?.copy(Bitmap.Config.ARGB_8888, false)
                            hardwareBuffer.close()

                            if (bitmap != null) {
                                DroidPilotManager.setScreenshot(bitmap)
                                onBitmapCaptured(bitmap)
                            } else {
                                DroidPilotManager.log("SCREENSHOT", "[RESULT] Failed to convert HardwareBuffer to Bitmap", LogLevel.ERROR)
                                onBitmapCaptured(null)
                            }
                        } catch (e: Exception) {
                            DroidPilotManager.log("SCREENSHOT", "[ERROR] Screenshot processing error: ${e.message}", LogLevel.ERROR)
                            onBitmapCaptured(null)
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        DroidPilotManager.log("SCREENSHOT", "[RESULT] Accessibility screenshot failed with code: $errorCode", LogLevel.ERROR)
                        onBitmapCaptured(null)
                    }
                }
            )
        } else {
            DroidPilotManager.log("SCREENSHOT", "[WARNING] Native Accessibility screenshot requires Android 11+ (API 30)", LogLevel.WARNING)
            onBitmapCaptured(null)
        }
    }
}
