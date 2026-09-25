package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NodeInfoItem
import com.example.model.ScreenReadResult
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PilotPrimary
import com.example.ui.theme.PilotSecondary

@Composable
fun NodeInspectorView(
    screenResult: ScreenReadResult?,
    onTapNode: (NodeInfoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("node_inspector_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ACCESSIBILITY NODE INSPECTOR",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (screenResult != null) {
                        Text(
                            text = "Found ${screenResult.totalNodesCount} nodes (${screenResult.clickableCount} clickable) on ${screenResult.packageName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PilotSecondary
                        )
                    } else {
                        Text(
                            text = "Press [Read Screen] to inspect TikTok UI tree",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            if (screenResult == null || screenResult.nodes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No screen nodes scanned yet.\nClick 'Read Screen' to parse active window elements.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter by text, id, or className...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("node_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PilotPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    singleLine = true
                )

                val filteredNodes = remember(screenResult, searchQuery) {
                    if (searchQuery.isBlank()) {
                        screenResult.nodes
                    } else {
                        screenResult.nodes.filter { item ->
                            (item.text?.contains(searchQuery, ignoreCase = true) == true) ||
                                    (item.contentDescription?.contains(searchQuery, ignoreCase = true) == true) ||
                                    item.className.contains(searchQuery, ignoreCase = true) ||
                                    (item.viewId?.contains(searchQuery, ignoreCase = true) == true)
                        }
                    }
                }

                Text(
                    text = "Showing ${filteredNodes.size} matching elements",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredNodes, key = { it.id }) { node ->
                        NodeItemCard(node = node, onTap = { onTapNode(node) })
                    }
                }
            }
        }
    }
}

@Composable
private fun NodeItemCard(
    node: NodeInfoItem,
    onTap: () -> Unit
) {
    val displayLabel = node.text ?: node.contentDescription
    val hasContent = !displayLabel.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(
                1.dp,
                if (node.isClickable) Color(0xFF3B82F6).copy(alpha = 0.6f) else Color(0xFF1E293B),
                RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Class and depth
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Lvl ${node.depth}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF38BDF8)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = node.className,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Badges
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (node.isClickable) {
                    Badge(text = "CLICKABLE", bgColor = Color(0xFF065F46), textColor = Color(0xFF34D399))
                }
                if (node.isEditable) {
                    Badge(text = "EDITABLE", bgColor = Color(0xFF701A75), textColor = Color(0xFFF472B6))
                }
                if (!node.isEnabled) {
                    Badge(text = "DISABLED", bgColor = Color(0xFF7F1D1D), textColor = Color(0xFFF87171))
                }
            }
        }

        // Visible Text / Content Description
        if (hasContent) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (node.text != null) "Text: " else "Desc: ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "\"$displayLabel\"",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE2E8F0)
                )
            }
        }

        // ViewId & Bounds
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                if (!node.viewId.isNullOrBlank()) {
                    Text(
                        text = "id: ${node.viewId.substringAfterLast('/')}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF64748B)
                    )
                }
                Text(
                    text = "bounds: [${node.bounds.left}, ${node.bounds.top} - ${node.bounds.right}, ${node.bounds.bottom}] (${node.bounds.width()}x${node.bounds.height()})",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF64748B)
                )
            }

            if (node.isClickable) {
                Button(
                    onClick = onTap,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.TouchApp, contentDescription = "Tap", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tap Node", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun Badge(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(text = text, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}
