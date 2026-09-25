package com.example.model

import android.graphics.Rect

enum class LogLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    ACTION,
    TIKTOK
}

data class LogEntry(
    val id: Long = System.nanoTime(),
    val timestamp: String,
    val tag: String,
    val message: String,
    val level: LogLevel = LogLevel.INFO
)

data class NodeInfoItem(
    val id: String,
    val className: String,
    val text: String?,
    val contentDescription: String?,
    val isClickable: Boolean,
    val isEnabled: Boolean,
    val isEditable: Boolean,
    val viewId: String?,
    val bounds: Rect,
    val depth: Int
)

data class ScreenReadResult(
    val packageName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalNodesCount: Int,
    val clickableCount: Int,
    val nodes: List<NodeInfoItem>
)
