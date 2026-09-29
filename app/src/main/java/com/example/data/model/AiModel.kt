package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class AiModelInfo(
    val id: String,
    val displayName: String,
    val versionTag: String,
    val creator: String,
    val description: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val apiEndpoint: String
)

object AiModels {
    val Nova = AiModelInfo(
        id = "nova",
        displayName = "Nova AI",
        versionTag = "Nova Pro",
        creator = "Supun AI",
        description = "Fast reasoning, coding, and multi-turn intelligence",
        icon = Icons.Default.AutoAwesome,
        badgeColor = Color(0xFF38BDF8),
        apiEndpoint = "https://supunofc.site/api/ai/ai/nova"
    )

    val Thenux = AiModelInfo(
        id = "thenux",
        displayName = "Thenux AI",
        versionTag = "T-Nex 1.0",
        creator = "THENUX Edge",
        description = "Powerful T-Nex 1.0 Edge neural model for deep reasoning",
        icon = Icons.Default.Bolt,
        badgeColor = Color(0xFF10B981),
        apiEndpoint = "https://edge.thenuxofc.store/api/chat"
    )

    val Kizer = AiModelInfo(
        id = "kizer",
        displayName = "Kizer AI",
        versionTag = "Gemini Ultra-Code",
        creator = "Kizer Engine / Gemini",
        description = "Elite coding intelligence specialized in clean code, algorithms, and deep system architecture",
        icon = Icons.Default.Code,
        badgeColor = Color(0xFF8B5CF6),
        apiEndpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent"
    )

    val all = listOf(Nova, Thenux, Kizer)

    fun getById(id: String): AiModelInfo {
        return all.find { it.id == id } ?: Kizer
    }
}
