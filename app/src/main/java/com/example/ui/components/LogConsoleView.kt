package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogEntry
import com.example.model.LogLevel
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokRed

@Composable
fun LogConsoleView(
    logs: List<LogEntry>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredLogs = remember(logs, selectedFilter) {
        when (selectedFilter) {
            "TIKTOK" -> logs.filter { it.level == LogLevel.TIKTOK || it.message.contains("TikTok", ignoreCase = true) }
            "ACTIONS" -> logs.filter { it.level == LogLevel.ACTION || it.level == LogLevel.SUCCESS }
            "SCANNER" -> logs.filter { it.tag == "SCANNER" || it.message.contains("node", ignoreCase = true) }
            "ERRORS" -> logs.filter { it.level == LogLevel.ERROR || it.level == LogLevel.WARNING }
            else -> logs
        }
    }

    // Auto-scroll to bottom on new log
    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TerminalBg)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("log_console_view")
    ) {
        // Terminal Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // macOS/Terminal style dots
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Console",
                    tint = TerminalGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE CONSOLE LOGS (${filteredLogs.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TerminalGreen
                )
            }

            Row {
                IconButton(
                    onClick = {
                        val text = logs.joinToString("\n") { "[${it.timestamp}] [${it.tag}] ${it.message}" }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("DroidPilot Logs", text))
                        Toast.makeText(context, "Logs copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp).testTag("copy_logs_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Logs",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onClearLogs,
                    modifier = Modifier.size(32.dp).testTag("clear_logs_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Logs",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filters = listOf("ALL", "ACTIONS", "TIKTOK", "SCANNER", "ERRORS")
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1E293B),
                        selectedLabelColor = TerminalGreen,
                        containerColor = Color.Transparent,
                        labelColor = Color(0xFF64748B)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == filter,
                        borderColor = Color(0xFF334155),
                        selectedBorderColor = TerminalGreen
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Log Output List
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No logs yet. Perform an action to view events...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF475569),
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    LogItemRow(log)
                }
            }
        }
    }
}

@Composable
private fun LogItemRow(log: LogEntry) {
    val (tagColor, textColor) = when (log.level) {
        LogLevel.TIKTOK -> Pair(TikTokCyan, Color.White)
        LogLevel.ACTION -> Pair(Color(0xFFA78BFA), Color(0xFFEDE9FE))
        LogLevel.SUCCESS -> Pair(Color(0xFF34D399), Color(0xFFA7F3D0))
        LogLevel.WARNING -> Pair(Color(0xFFFBBF24), Color(0xFFFEF3C7))
        LogLevel.ERROR -> Pair(Color(0xFFF87171), Color(0xFFFEE2E2))
        LogLevel.INFO -> Pair(Color(0xFF38BDF8), Color(0xFFE2E8F0))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    ) {
        Text(
            text = log.timestamp,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF64748B),
            modifier = Modifier.width(82.dp)
        )

        Text(
            text = "[${log.tag}] ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = tagColor
        )

        Text(
            text = log.message,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}
