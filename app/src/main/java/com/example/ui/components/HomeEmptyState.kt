package com.example.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeEmptyState(
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Hero Title
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nova Coding AI",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "High-performance AI agent for coding, debugging & Sinhala tech assistance.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp
                )
            }

            // Quick Action Rows (matching Screenshot 1 style: icon + text)
            SuggestionRow(
                icon = Icons.Default.Code,
                text = "Write a complete Jetpack Compose screen",
                onClick = { onPromptSelected("Write a complete, responsive Jetpack Compose screen with Material 3 styling and animations.") }
            )

            SuggestionRow(
                icon = Icons.Default.BugReport,
                text = "Debug & optimize an algorithm",
                onClick = { onPromptSelected("Help me optimize this algorithm for best time and space complexity with clean code and explanations.") }
            )

            SuggestionRow(
                icon = Icons.Default.Edit,
                text = "Write or edit code in any language",
                onClick = { onPromptSelected("Write a Python script that analyzes JSON data, extracts key metrics, and handles exceptions cleanly.") }
            )

            SuggestionRow(
                icon = Icons.Default.School,
                text = "Coding tutor in Sinhala (සිංහලෙන් කෝඩින්)",
                onClick = { onPromptSelected("මට සිංහලෙන් පැහැදිලි කරන්න Object Oriented Programming (OOP) සංකල්ප සහ Kotlin උදාහරණ.") }
            )

            SuggestionRow(
                icon = Icons.Default.Language,
                text = "REST API & Database integration",
                onClick = { onPromptSelected("Explain how to build a robust Android Room and Retrofit repository with offline caching.") }
            )
        }
    }
}

@Composable
private fun SuggestionRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 9.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = Color(0xFFD1D5DB),
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
