package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.persona.AgentPersona

@Composable
fun TopBar(
    currentPersona: AgentPersona,
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onPersonaBadgeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Menu Button (matching circular dark button in Screenshot 1)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF1C1C1E))
                .clickable { onMenuClick() }
                .testTag("menu_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open navigation menu",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Center "Nova Plus" / Persona pill (matching "✦ Get Plus" in Screenshot 1)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .clickable { onPersonaBadgeClick() }
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("persona_badge"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentPersona.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // New Chat Button (matching circular dark button in Screenshot 1)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF1C1C1E))
                .clickable { onNewChatClick() }
                .testTag("new_chat_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "New Chat",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
