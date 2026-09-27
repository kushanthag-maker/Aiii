package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CodeSnippetPrompt(
    val title: String,
    val language: String,
    val prompt: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeStudioSheet(
    sheetState: SheetState,
    onSelectSnippet: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val snippets = listOf(
        CodeSnippetPrompt(
            title = "Android Jetpack Compose Architecture",
            language = "KOTLIN",
            prompt = "Write a complete Android clean architecture example in Kotlin with a ViewModel, StateFlow, Room Database, and a modern Jetpack Compose UI screen.",
            description = "Production MVI/MVVM pattern with Room & Coroutines"
        ),
        CodeSnippetPrompt(
            title = "Python Data Analysis & Scraping",
            language = "PYTHON",
            prompt = "Write a complete Python script to fetch data from an API, parse JSON, handle errors, clean the data using pandas, and export summary statistics to CSV.",
            description = "Data pipeline, error handling, CSV export"
        ),
        CodeSnippetPrompt(
            title = "Full Stack REST API in Node.js / Express",
            language = "JAVASCRIPT",
            prompt = "Write an Express.js TypeScript REST API backend with CRUD endpoints, middleware error handling, JWT auth check, and clean validation.",
            description = "TypeScript backend with JWT authentication"
        ),
        CodeSnippetPrompt(
            title = "Binary Tree & Graph Algorithms",
            language = "ALGORITHMS",
            prompt = "Implement Depth First Search (DFS), Breadth First Search (BFS), and Dijkstra's algorithm in Kotlin with step-by-step Big-O complexity analysis.",
            description = "Graph traversal & shortest path algorithms"
        ),
        CodeSnippetPrompt(
            title = "Sinhala Coding Tutorial (සිංහල පාඩම)",
            language = "SINHALA",
            prompt = "කරුණාකර සිංහලෙන් පැහැදිලි කරන්න: REST API එකක් සහ Mobile App එකක් අතර සන්නිවේදනය සිදුවන්නේ කෙසේද? Retrofit භාවිතා කරන ආකාරය කෝඩ් සමග පෙන්වන්න.",
            description = "Mobile & REST API architecture explained in Sinhala"
        ),
        CodeSnippetPrompt(
            title = "SQL Database Schema & Optimized Queries",
            language = "SQL",
            prompt = "Design a normalized relational SQL schema for an E-commerce system with indexed tables, transactions, foreign keys, and complex aggregation queries.",
            description = "Normalized schema, indexes, joins, aggregations"
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Code Studio & Prompts",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pre-configured elite coding tasks for Nova AI",
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp
                    )
                }

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

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(snippets.size) { index ->
                    val item = snippets[index]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1A1D24))
                            .border(1.dp, Color(0xFF2B303D), RoundedCornerShape(14.dp))
                            .clickable {
                                onSelectSnippet(item.prompt)
                                onDismiss()
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = item.language,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.description,
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.5.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Run with Nova AI",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
