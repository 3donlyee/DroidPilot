package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ActionButtonsGrid
import com.example.ui.components.DeviceGuideCard
import com.example.ui.components.InputTextDialog
import com.example.ui.components.LogConsoleView
import com.example.ui.components.NodeInspectorView
import com.example.ui.components.ScreenshotChoiceDialog
import com.example.ui.components.ScreenshotView
import com.example.ui.components.StatusHeaderCard
import com.example.ui.components.TapCoordinateDialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PilotPrimary
import com.example.ui.theme.PilotSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: DroidPilotViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val logs by viewModel.logs.collectAsState()
    val isAccessibilityConnected by viewModel.isAccessibilityConnected.collectAsState()
    val currentPackage by viewModel.currentPackage.collectAsState()
    val isTikTokDetected by viewModel.isTikTokDetected.collectAsState()
    val screenResult by viewModel.screenResult.collectAsState()
    val latestScreenshot by viewModel.latestScreenshot.collectAsState()
    val isOverlayActive by viewModel.isOverlayActive.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showTapDialog by remember { mutableStateOf(false) }
    var showInputDialog by remember { mutableStateOf(false) }
    var showScreenshotChoiceDialog by remember { mutableStateOf(false) }

    // Overlay Permission launcher
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) {
            viewModel.toggleFloatingOverlay(context) {}
        }
    }

    // MediaProjection screen capture launcher
    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val metrics = context.resources.displayMetrics
            viewModel.startMediaProjectionCapture(result.resultCode, result.data!!, metrics, context)
            scope.launch {
                snackbarHostState.showSnackbar("Capturing screen via MediaProjection...")
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Screen capture permission cancelled")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DroidPilot",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF312E81), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TikTok Pilot v1.0",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PilotSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Controls", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PilotPrimary,
                        selectedTextColor = PilotPrimary,
                        indicatorColor = Color(0xFF1E293B)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Code, contentDescription = "Nodes") },
                    label = {
                        Text(
                            text = if (screenResult != null) "Nodes (${screenResult?.totalNodesCount})" else "Nodes",
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PilotPrimary,
                        selectedTextColor = PilotPrimary,
                        indicatorColor = Color(0xFF1E293B)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Screenshot") },
                    label = { Text("Screenshot", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PilotPrimary,
                        selectedTextColor = PilotPrimary,
                        indicatorColor = Color(0xFF1E293B)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.HelpOutline, contentDescription = "Guide") },
                    label = { Text("Guide", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PilotPrimary,
                        selectedTextColor = PilotPrimary,
                        indicatorColor = Color(0xFF1E293B)
                    )
                )
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Dashboard: Status + Action Buttons + Logs
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        StatusHeaderCard(
                            isAccessibilityConnected = isAccessibilityConnected,
                            currentPackage = currentPackage,
                            isTikTokDetected = isTikTokDetected,
                            isOverlayActive = isOverlayActive,
                            onEnableAccessibility = { viewModel.openAccessibilitySettings(context) },
                            onToggleOverlay = {
                                viewModel.toggleFloatingOverlay(context) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        overlayPermissionLauncher.launch(intent)
                                    }
                                }
                            }
                        )

                        ActionButtonsGrid(
                            onEnableAccessibility = { viewModel.openAccessibilitySettings(context) },
                            onOpenTikTok = { viewModel.openTikTok(context) },
                            onCheckTikTok = { viewModel.checkTikTok() },
                            onReadScreen = {
                                viewModel.readScreen()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Screen nodes read. Check Nodes tab.")
                                }
                            },
                            onTestTap = { showTapDialog = true },
                            onTestSwipeUp = { viewModel.testSwipeUp() },
                            onTestSwipeDown = { viewModel.testSwipeDown() },
                            onTestBack = { viewModel.testBack() },
                            onTestInputText = { showInputDialog = true },
                            onTakeScreenshot = { showScreenshotChoiceDialog = true },
                            onRunAutoSequence = {
                                viewModel.runAutomatedSequence(context) { msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            }
                        )

                        LogConsoleView(
                            logs = logs,
                            onClearLogs = { viewModel.clearLogs() }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                1 -> {
                    // Screen Nodes Inspector Tab
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NodeInspectorView(
                            screenResult = screenResult,
                            onTapNode = { node ->
                                viewModel.clickFirstClickable(node.text ?: node.contentDescription)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Dispatched tap to: ${node.className}")
                                }
                            }
                        )
                    }
                }

                2 -> {
                    // Screenshot Viewer Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ScreenshotView(
                            screenshot = latestScreenshot,
                            onTakeScreenshot = { showScreenshotChoiceDialog = true }
                        )
                    }
                }

                3 -> {
                    // Device Guide Tab for OPPO Reno5
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DeviceGuideCard()
                    }
                }
            }
        }
    }

    // Dialogs
    if (showTapDialog) {
        TapCoordinateDialog(
            onDismiss = { showTapDialog = false },
            onTapCenter = {
                viewModel.testTapCenter()
                showTapDialog = false
            },
            onTapCoordinates = { x, y ->
                viewModel.testTapCoordinate(x, y)
                showTapDialog = false
            }
        )
    }

    if (showInputDialog) {
        InputTextDialog(
            onDismiss = { showInputDialog = false },
            onSubmitText = { text ->
                viewModel.testInputText(text)
                showInputDialog = false
            }
        )
    }

    if (showScreenshotChoiceDialog) {
        ScreenshotChoiceDialog(
            onDismiss = { showScreenshotChoiceDialog = false },
            onChooseNative = {
                showScreenshotChoiceDialog = false
                viewModel.takeNativeScreenshot()
                selectedTab = 2 // Switch to screenshot tab
            },
            onChooseMediaProjection = {
                showScreenshotChoiceDialog = false
                val mpManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                mediaProjectionLauncher.launch(mpManager.createScreenCaptureIntent())
                selectedTab = 2 // Switch to screenshot tab
            }
        )
    }
}
