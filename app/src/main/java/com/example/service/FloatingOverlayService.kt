package com.example.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.R
import com.example.data.DroidPilotManager
import com.example.model.LogLevel
import kotlin.math.abs

class FloatingOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isExpanded = false

    companion object {
        var isRunning = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, FloatingOverlayService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingOverlayService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createFloatingView()
        isRunning = true
        DroidPilotManager.setOverlayActive(true)
        DroidPilotManager.log("OVERLAY", "[OVERLAY] Floating test controller started", LogLevel.INFO)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingView() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 350
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // Header / Pill button (Draggable)
        val pillBg = GradientDrawable().apply {
            setColor(Color.parseColor("#1E1B4B"))
            cornerRadius = 48f
            setStroke(3, Color.parseColor("#6366F1"))
        }

        val pillLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(28, 16, 28, 16)
            background = pillBg
            elevation = 12f
        }

        val icon = ImageView(this).apply {
            setImageResource(R.mipmap.ic_launcher)
            layoutParams = LinearLayout.LayoutParams(48, 48)
        }

        val titleText = TextView(this).apply {
            text = " DroidPilot"
            setTextColor(Color.WHITE)
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        pillLayout.addView(icon)
        pillLayout.addView(titleText)
        rootLayout.addView(pillLayout)

        // Expanded Action Panel
        val panelBg = GradientDrawable().apply {
            setColor(Color.parseColor("#0F172A"))
            cornerRadius = 24f
            setStroke(2, Color.parseColor("#38BDF8"))
        }

        val actionsPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 20)
            background = panelBg
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 12
            }
        }

        fun createActionButton(label: String, colorHex: String, onClick: () -> Unit): Button {
            return Button(this).apply {
                text = label
                textSize = 11f
                isAllCaps = false
                setTextColor(Color.WHITE)
                background = GradientDrawable().apply {
                    setColor(Color.parseColor(colorHex))
                    cornerRadius = 16f
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    74
                ).apply {
                    bottomMargin = 8
                }
                setOnClickListener {
                    onClick()
                }
            }
        }

        actionsPanel.addView(createActionButton("⬆ Swipe Up (Next)", "#4F46E5") {
            val service = DroidPilotAccessibilityService.instance
            if (service != null) {
                service.swipeUp { success ->
                    Toast.makeText(this@FloatingOverlayService, if (success) "Swipe Up Done" else "Swipe Cancelled", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@FloatingOverlayService, "Accessibility not enabled!", Toast.LENGTH_SHORT).show()
            }
        })

        actionsPanel.addView(createActionButton("⬇ Swipe Down", "#3B82F6") {
            val service = DroidPilotAccessibilityService.instance
            if (service != null) {
                service.swipeDown { success ->
                    Toast.makeText(this@FloatingOverlayService, if (success) "Swipe Down Done" else "Swipe Cancelled", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@FloatingOverlayService, "Accessibility not enabled!", Toast.LENGTH_SHORT).show()
            }
        })

        actionsPanel.addView(createActionButton("🔍 Read Screen Nodes", "#059669") {
            val service = DroidPilotAccessibilityService.instance
            if (service != null) {
                val result = service.readCurrentScreenNodes()
                Toast.makeText(this@FloatingOverlayService, "Read ${result.totalNodesCount} nodes (${result.clickableCount} clickable)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@FloatingOverlayService, "Accessibility not enabled!", Toast.LENGTH_SHORT).show()
            }
        })

        actionsPanel.addView(createActionButton("🎯 Tap Screen Center", "#D97706") {
            val service = DroidPilotAccessibilityService.instance
            if (service != null) {
                service.tapCenterScreen { success ->
                    Toast.makeText(this@FloatingOverlayService, if (success) "Tap Done" else "Tap Failed", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@FloatingOverlayService, "Accessibility not enabled!", Toast.LENGTH_SHORT).show()
            }
        })

        actionsPanel.addView(createActionButton("📸 Screenshot (Native)", "#8B5CF6") {
            val service = DroidPilotAccessibilityService.instance
            if (service != null) {
                service.takeAccessibilityScreenshot { bitmap ->
                    Toast.makeText(this@FloatingOverlayService, if (bitmap != null) "Screenshot Saved!" else "Screenshot Failed", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@FloatingOverlayService, "Accessibility not enabled!", Toast.LENGTH_SHORT).show()
            }
        })

        actionsPanel.addView(createActionButton("◀ Back", "#475569") {
            val service = DroidPilotAccessibilityService.instance
            if (service != null) {
                service.triggerBack()
            } else {
                Toast.makeText(this@FloatingOverlayService, "Accessibility not enabled!", Toast.LENGTH_SHORT).show()
            }
        })

        actionsPanel.addView(createActionButton("✖ Close Controller", "#DC2626") {
            stopSelf()
        })

        rootLayout.addView(actionsPanel)

        // Drag and touch handler for the pill
        pillLayout.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDragging = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (abs(dx) > 10 || abs(dy) > 10) {
                            isDragging = true
                            params.x = initialX + dx
                            params.y = initialY + dy
                            windowManager?.updateViewLayout(rootLayout, params)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            // Toggle expand/collapse
                            isExpanded = !isExpanded
                            actionsPanel.visibility = if (isExpanded) View.VISIBLE else View.GONE
                            windowManager?.updateViewLayout(rootLayout, params)
                        }
                        return true
                    }
                }
                return false
            }
        })

        overlayView = rootLayout
        windowManager?.addView(rootLayout, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
            overlayView = null
        }
        isRunning = false
        DroidPilotManager.setOverlayActive(false)
        DroidPilotManager.log("OVERLAY", "[OVERLAY] Floating test controller closed", LogLevel.INFO)
    }
}
