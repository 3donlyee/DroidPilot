package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PilotPrimary
import com.example.ui.theme.PilotSecondary
import com.example.ui.theme.TikTokRed

@Composable
fun ActionButtonsGrid(
    onEnableAccessibility: () -> Unit,
    onOpenTikTok: () -> Unit,
    onCheckTikTok: () -> Unit,
    onReadScreen: () -> Unit,
    onTestTap: () -> Unit,
    onTestSwipeUp: () -> Unit,
    onTestSwipeDown: () -> Unit,
    onTestBack: () -> Unit,
    onTestInputText: () -> Unit,
    onTakeScreenshot: () -> Unit,
    onRunAutoSequence: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("action_buttons_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONTROL ACTIONS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Accessibility & MediaProjection",
                    style = MaterialTheme.typography.labelSmall,
                    color = PilotSecondary
                )
            }

            // Row 1: Core setup [Enable Accessibility] & [Open TikTok]
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionTile(
                    title = "Enable Accessibility",
                    subtitle = "System Settings",
                    icon = Icons.Default.AccessibilityNew,
                    color = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_enable_accessibility",
                    onClick = onEnableAccessibility
                )

                ActionTile(
                    title = "Open TikTok",
                    subtitle = "Launch App",
                    icon = Icons.Default.VideoLibrary,
                    color = TikTokRed,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_open_tiktok",
                    onClick = onOpenTikTok
                )
            }

            // Row 2: [Check TikTok] & [Read Screen]
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionTile(
                    title = "Check TikTok",
                    subtitle = "Verify foreground",
                    icon = Icons.Default.Refresh,
                    color = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_check_tiktok",
                    onClick = onCheckTikTok
                )

                ActionTile(
                    title = "Read Screen",
                    subtitle = "Parse UI Nodes",
                    icon = Icons.Default.Search,
                    color = Color(0xFF059669),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_read_screen",
                    onClick = onReadScreen
                )
            }

            // Row 3: [Test Tap] & [Test Swipe Up]
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionTile(
                    title = "Test Tap",
                    subtitle = "Center / Coords",
                    icon = Icons.Default.TouchApp,
                    color = Color(0xFFD97706),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_test_tap",
                    onClick = onTestTap
                )

                ActionTile(
                    title = "Test Swipe Up",
                    subtitle = "Next Video gesture",
                    icon = Icons.Default.KeyboardArrowUp,
                    color = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_test_swipe_up",
                    onClick = onTestSwipeUp
                )
            }

            // Row 4: [Test Swipe Down] & [Test Back]
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionTile(
                    title = "Test Swipe Down",
                    subtitle = "Previous Video",
                    icon = Icons.Default.KeyboardArrowDown,
                    color = Color(0xFF4338CA),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_test_swipe_down",
                    onClick = onTestSwipeDown
                )

                ActionTile(
                    title = "Test Back",
                    subtitle = "Global Back action",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    color = Color(0xFF475569),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_test_back",
                    onClick = onTestBack
                )
            }

            // Row 5: [Test Input Text] & [Take Screenshot]
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionTile(
                    title = "Test Input Text",
                    subtitle = "Type into focus",
                    icon = Icons.Default.Edit,
                    color = Color(0xFF0D9488),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_test_input_text",
                    onClick = onTestInputText
                )

                ActionTile(
                    title = "Take Screenshot",
                    subtitle = "Capture frame",
                    icon = Icons.Default.CameraAlt,
                    color = Color(0xFFEC4899),
                    modifier = Modifier.weight(1f),
                    testTag = "btn_take_screenshot",
                    onClick = onTakeScreenshot
                )
            }

            // Full width automated sequence test button
            Button(
                onClick = onRunAutoSequence,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_run_auto_sequence"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PilotPrimary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Auto Sequence", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Run Automated TikTok Test Sequence (3s delay)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.88f)),
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}
