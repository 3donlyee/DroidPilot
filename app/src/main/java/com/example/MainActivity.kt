package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DroidPilotManager
import com.example.model.LogLevel
import com.example.ui.DroidPilotViewModel
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            DroidPilotManager.log("PERMISSION", "Notification permission granted", LogLevel.SUCCESS)
        } else {
            DroidPilotManager.log("PERMISSION", "Notification permission denied (background services may not display notifications)", LogLevel.WARNING)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission on Android 13+ (OPPO Reno5 target)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        DroidPilotManager.log("SYSTEM", "[START] DroidPilot initialized on ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})", LogLevel.INFO)

        setContent {
            MyApplicationTheme {
                val viewModel: DroidPilotViewModel = viewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
