package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaBlueLight
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@Composable
fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onPlusClick: () -> Unit,
    onMicClick: () -> Unit,
    onVoiceWaveClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Main pill container (matching ChatGPT Screenshot 1)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF1E1E1E))
                .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(28.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // '+' Action Button
            IconButton(
                onClick = onPlusClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("plus_actions_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick Actions & Templates",
                    tint = Color(0xFFE5E7EB),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Text Input Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "Ask Nova AI or write code...",
                        color = Color(0xFF8E8E93),
                        fontSize = 15.sp
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = TextStyle(
                        color = NovaTextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(NovaBlueLight),
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_input_field")
                )
            }

            // Right Actions: Send button OR (Mic + Waveform button)
            if (text.isNotBlank() && !isLoading) {
                // Send button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NovaBlueLight)
                        .clickable { onSend() }
                        .testTag("send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else if (isLoading) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.5.dp,
                        color = NovaBlueLight,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                // Mic Icon Button
                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("mic_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = Color(0xFFE5E7EB),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Blue Waveform Call Button (matching Screenshot 1 blue circle with audio wave!)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3875F6))
                        .clickable { onVoiceWaveClick() }
                        .testTag("voice_assistant_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Voice Agent Mode",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
