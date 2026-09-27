package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
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

data class QuickActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val promptTemplate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptPresetsSheet(
    sheetState: SheetState,
    onSelectPreset: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val items = listOf(
        QuickActionItem(
            title = "Jetpack Compose Screen",
            subtitle = "Generate responsive UI with Material 3 & animations",
            icon = Icons.Default.Code,
            promptTemplate = "Write a complete Jetpack Compose screen with Material 3 styling, StateFlow, ViewModel, and clean separation of concerns."
        ),
        QuickActionItem(
            title = "Debug & Fix Errors",
            subtitle = "Analyze stack trace or find logic bugs",
            icon = Icons.Default.BugReport,
            promptTemplate = "Here is my code and error log. Please analyze the root cause, fix the issue, and explain why it happened:\n\n```\n// Paste code here\n```"
        ),
        QuickActionItem(
            title = "Sinhala Coding Assistant",
            subtitle = "කේතනය සහ තාක්ෂණය සිංහලෙන් පැහැදිලි කරගන්න",
            icon = Icons.Default.School,
            promptTemplate = "මට සිංහල භාෂාවෙන් පැහැදිලි කරන්න: "
        ),
        QuickActionItem(
            title = "Code Optimization (Big-O)",
            subtitle = "Refactor for speed, memory, and low complexity",
            icon = Icons.Default.ElectricBolt,
            promptTemplate = "Analyze this algorithm for time and space complexity. Optimize it for better performance:\n\n```\n// Paste code here\n```"
        ),
        QuickActionItem(
            title = "REST API & Networking",
            subtitle = "Retrofit / Ktor / Fetch integration with error handling",
            icon = Icons.Default.Language,
            promptTemplate = "Write a production-grade API client with Retrofit, Moshi JSON deserialization, Coroutines, and network error handling."
        ),
        QuickActionItem(
            title = "Python Automation Script",
            subtitle = "Data processing, scraping, or automation script",
            icon = Icons.Default.Terminal,
            promptTemplate = "Write a clean Python script with comments and type annotations that performs: "
        ),
        QuickActionItem(
            title = "Documentation & Unit Tests",
            subtitle = "Generate Javadoc, comments, and JUnit/Robolectric tests",
            icon = Icons.Default.Description,
            promptTemplate = "Add comprehensive documentation comments and write unit tests covering edge cases for this code:\n\n```\n// Paste code here\n```"
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141416),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Coding Presets & Quick Tools",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select a quick template for Nova Coding Agent",
                color = Color(0xFF9CA3AF),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(items.size) { index ->
                    val item = items[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSelectPreset(item.promptTemplate)
                                onDismiss()
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = item.subtitle,
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
