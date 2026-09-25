package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.DroidPilotManager
import com.example.model.LogLevel

class MediaProjectionService : Service() {

    companion object {
        const val CHANNEL_ID = "droidpilot_media_projection"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_CAPTURE = "com.example.action.START_CAPTURE"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
        const val EXTRA_WIDTH = "extra_width"
        const val EXTRA_HEIGHT = "extra_height"
        const val EXTRA_DENSITY = "extra_density"

        var isRunning = false
            private set
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            }
            startForeground(NOTIFICATION_ID, notification, fgsType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        isRunning = true

        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
        val width = intent.getIntExtra(EXTRA_WIDTH, 1080)
        val height = intent.getIntExtra(EXTRA_HEIGHT, 1920)
        val density = intent.getIntExtra(EXTRA_DENSITY, 400)

        if (resultCode != 0 && resultData != null) {
            processCapture(resultCode, resultData, width, height, density)
        } else {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun processCapture(resultCode: Int, resultData: Intent, width: Int, height: Int, density: Int) {
        DroidPilotManager.log("MEDIA_PROJECTION", "[ACTION] Initializing MediaProjection capture...", LogLevel.ACTION)

        val mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mpManager.getMediaProjection(resultCode, resultData)

        if (mediaProjection == null) {
            DroidPilotManager.log("MEDIA_PROJECTION", "[ERROR] Failed to obtain MediaProjection session", LogLevel.ERROR)
            stopSelf()
            return
        }

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "DroidPilotCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            handler
        )

        var captured = false

        imageReader?.setOnImageAvailableListener({ reader ->
            if (captured) return@setOnImageAvailableListener

            var image: Image? = null
            try {
                image = reader.acquireLatestImage()
                if (image != null) {
                    captured = true
                    val planes = image.planes
                    val buffer = planes[0].buffer
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val rowPadding = rowStride - pixelStride * width

                    val bitmap = Bitmap.createBitmap(
                        width + rowPadding / pixelStride,
                        height,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)

                    // Crop to exact width if padding exists
                    val finalBitmap = if (rowPadding > 0) {
                        Bitmap.createBitmap(bitmap, 0, 0, width, height)
                    } else {
                        bitmap
                    }

                    handler.post {
                        DroidPilotManager.setScreenshot(finalBitmap)
                        DroidPilotManager.log("MEDIA_PROJECTION", "[RESULT] MediaProjection Screenshot captured successfully (${width}x${height})", LogLevel.SUCCESS)
                        cleanup()
                        stopSelf()
                    }
                }
            } catch (e: Exception) {
                DroidPilotManager.log("MEDIA_PROJECTION", "[ERROR] Capture failed: ${e.message}", LogLevel.ERROR)
                cleanup()
                stopSelf()
            } finally {
                image?.close()
            }
        }, handler)

        // Timeout fallback after 4 seconds
        handler.postDelayed({
            if (!captured) {
                DroidPilotManager.log("MEDIA_PROJECTION", "[WARNING] Screen capture timed out waiting for image frame", LogLevel.WARNING)
                cleanup()
                stopSelf()
            }
        }, 4000)
    }

    private fun cleanup() {
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader?.close()
            imageReader = null
            mediaProjection?.stop()
            mediaProjection = null
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
        isRunning = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DroidPilot Screen Capture")
            .setContentText("Screen capture in progress")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
