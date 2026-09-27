package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun VoiceModal(
    isListening: Boolean,
    recognizedText: String,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSubmitVoice: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141416)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nova Voice Agent",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Pulsing Audio Waveform Visualizer
                WaveformVisualizer(isAnimating = isListening)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isListening) "Listening to your voice..." else "Tap microphone to speak",
                    color = if (isListening) Color(0xFF38BDF8) else Color(0xFF9CA3AF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (recognizedText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF20222A))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "\"$recognizedText\"",
                            color = Color(0xFFF3F4F6),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Controls
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Mic toggle
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (isListening) Color(0xFFEF4444) else Color(0xFF3875F6))
                            .clickable {
                                if (isListening) onStopListening() else onStartListening()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Toggle Mic",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                if (recognizedText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF38BDF8))
                            .clickable { onSubmitVoice() }
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Ask Nova AI",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WaveformVisualizer(isAnimating: Boolean) {
    val transition = rememberInfiniteTransition(label = "waveform")

    val h1 by transition.animateFloat(
        initialValue = 12f,
        targetValue = if (isAnimating) 48f else 16f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 24f,
        targetValue = if (isAnimating) 64f else 24f,
        animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 18f,
        targetValue = if (isAnimating) 54f else 20f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val h4 by transition.animateFloat(
        initialValue = 30f,
        targetValue = if (isAnimating) 70f else 28f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "h4"
    )
    val h5 by transition.animateFloat(
        initialValue = 14f,
        targetValue = if (isAnimating) 42f else 18f,
        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
        label = "h5"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(72.dp)
    ) {
        WaveBar(height = h1, color = Color(0xFF60A5FA))
        WaveBar(height = h2, color = Color(0xFF38BDF8))
        WaveBar(height = h4, color = Color(0xFF3875F6))
        WaveBar(height = h3, color = Color(0xFF818CF8))
        WaveBar(height = h5, color = Color(0xFFA78BFA))
    }
}

@Composable
private fun WaveBar(height: Float, color: Color) {
    Box(
        modifier = Modifier
            .width(6.dp)
            .height(height.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color)
    )
}
