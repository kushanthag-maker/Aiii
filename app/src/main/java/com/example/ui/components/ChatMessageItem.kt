package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.persona.AgentPersona
import com.example.ui.theme.NovaTextPrimary

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    currentPersona: AgentPersona,
    onSpeakText: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUser = message.role == "user"

    if (isUser) {
        // User message bubble aligned to the right
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                    .background(Color(0xFF262629))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content,
                    color = NovaTextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
            }
        }
    } else {
        // Assistant message: full width with avatar and action bar
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Assistant Header (Nova AI Spark icon + Badge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Nova AI",
                        tint = if (message.isError) Color(0xFFEF4444) else Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Nova AI",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF38BDF8).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = currentPersona.badge,
                        color = Color(0xFF38BDF8),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Message Body
            if (message.isError) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2B1214))
                        .border(1.dp, Color(0xFF7F1D1D), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.content,
                                color = Color(0xFFFECACA),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                MarkdownText(
                    content = message.content,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Action Bar (Copy, Listen, Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy entire message
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Nova AI response", message.content)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy message",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(15.dp)
                    )
                }

                // TTS Read Aloud
                IconButton(
                    onClick = { onSpeakText(message.content) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Read aloud",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Share
                IconButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, message.content)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share with"))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(15.dp)
                    )
                }

                if (message.isError) {
                    IconButton(
                        onClick = onRetry,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
