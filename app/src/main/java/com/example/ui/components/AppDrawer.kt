package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModelInfo
import com.example.data.model.Conversation

@Composable
fun AppDrawer(
    pinnedConversations: List<Conversation>,
    recentConversations: List<Conversation>,
    currentModel: AiModelInfo,
    selectedConversationId: String?,
    onSelectConversation: (String) -> Unit,
    onNewChatClick: () -> Unit,
    onTogglePin: (String, Boolean) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onOpenModelSelector: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPersonas: () -> Unit,
    onOpenCodeLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Drawer Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSearchActive) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1C1C1E))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        cursorBrush = SolidColor(Color.White),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            isSearchActive = false
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Search",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Nova AI",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C1C1E))
                        .clickable { isSearchActive = true }
                        .testTag("drawer_search_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Conversations",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Drawer Items List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            item {
                // AI Model Selection Item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF181B24))
                        .clickable { onOpenModelSelector() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Model",
                        tint = currentModel.badgeColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Model",
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.5.sp
                        )
                        Text(
                            text = currentModel.displayName,
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(currentModel.badgeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = currentModel.versionTag,
                            color = currentModel.badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                DrawerNavigationRow(
                    icon = Icons.Default.Code,
                    label = "Code Studio & Prompts",
                    onClick = onOpenCodeLibrary
                )
                DrawerNavigationRow(
                    icon = Icons.Default.Extension,
                    label = "Agent Personas",
                    onClick = onOpenPersonas
                )
                DrawerNavigationRow(
                    icon = Icons.Default.Folder,
                    label = "Saved Projects",
                    onClick = onOpenCodeLibrary
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Pinned Section
            val filteredPinned = pinnedConversations.filter {
                searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true)
            }
            if (filteredPinned.isNotEmpty()) {
                item {
                    Text(
                        text = "Pinned",
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                items(filteredPinned, key = { "pinned_" + it.id }) { conv ->
                    ConversationItemRow(
                        conversation = conv,
                        isSelected = conv.id == selectedConversationId,
                        onClick = { onSelectConversation(conv.id) },
                        onTogglePin = { onTogglePin(conv.id, !conv.isPinned) },
                        onDelete = { onDeleteConversation(conv.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Recents Section
            val filteredRecent = recentConversations.filter {
                searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true)
            }
            if (filteredRecent.isNotEmpty()) {
                item {
                    Text(
                        text = "Recents",
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                items(filteredRecent, key = { "recent_" + it.id }) { conv ->
                    ConversationItemRow(
                        conversation = conv,
                        isSelected = conv.id == selectedConversationId,
                        onClick = { onSelectConversation(conv.id) },
                        onTogglePin = { onTogglePin(conv.id, !conv.isPinned) },
                        onDelete = { onDeleteConversation(conv.id) }
                    )
                }
            } else if (filteredPinned.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No chats found matching \"$searchQuery\"" else "No recent conversations yet",
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Bottom Bar (matching Screenshot 2: Blue pill "+ Chat" and Settings gear button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF3875F6))
                    .clickable { onNewChatClick() }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("drawer_new_chat_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Chat",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C1E))
                    .clickable { onOpenSettings() }
                    .testTag("drawer_settings_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun DrawerNavigationRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFFE5E7EB),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            color = Color(0xFFE5E7EB),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ConversationItemRow(
    conversation: Conversation,
    isSelected: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF1E2433) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (conversation.isPinned) Icons.Default.PushPin else Icons.AutoMirrored.Filled.Chat,
            contentDescription = null,
            tint = if (conversation.isPinned) Color(0xFF38BDF8) else Color(0xFF9CA3AF),
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = conversation.title,
            color = if (isSelected) Color.White else Color(0xFFD1D5DB),
            fontSize = 14.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onTogglePin,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = if (conversation.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                contentDescription = "Pin conversation",
                tint = if (conversation.isPinned) Color(0xFF38BDF8) else Color(0xFF6B7280),
                modifier = Modifier.size(16.dp)
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete conversation",
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
