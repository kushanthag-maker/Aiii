package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeComment
import com.example.ui.theme.CodeEditorBg
import com.example.ui.theme.CodeFunction
import com.example.ui.theme.CodeHeaderBg
import com.example.ui.theme.CodeKeyword
import com.example.ui.theme.CodeNumber
import com.example.ui.theme.CodeString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CodeBlockView(
    code: String,
    language: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    val cleanLanguage = language.ifBlank { "code" }.uppercase()
    val lines = remember(code) { code.lines() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CodeEditorBg)
            .border(1.dp, Color(0xFF262A36), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CodeHeaderBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Code",
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cleanLanguage,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("code", code)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                            Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                delay(2000)
                                isCopied = false
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        AnimatedVisibility(
                            visible = isCopied,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Copied",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (!isCopied) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy code",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Code Body with line numbers and horizontal scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                // Line Numbers
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    lines.indices.forEach { index ->
                        Text(
                            text = "${index + 1}",
                            color = Color(0xFF4B5563),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Code Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                        .padding(end = 12.dp)
                ) {
                    val highlightedCode = remember(code, cleanLanguage) {
                        highlightSyntax(code)
                    }
                    Text(
                        text = highlightedCode,
                        fontSize = 12.5.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

/**
 * Lightweight syntax highlighter for Kotlin, Python, JS, Java, and common languages
 */
private fun highlightSyntax(code: String): AnnotatedString {
    return buildAnnotatedString {
        val keywords = setOf(
            "fun", "val", "var", "class", "interface", "object", "return", "if", "else",
            "while", "for", "in", "when", "is", "import", "package", "public", "private",
            "protected", "override", "suspend", "data", "sealed", "def", "from", "as",
            "const", "let", "function", "async", "await", "try", "catch", "throw", "finally",
            "new", "this", "super", "null", "true", "false", "type", "struct", "void", "int", "boolean"
        )

        val tokens = Regex("""(//.*|#.*|"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\b\w+\b|[^\w\s])""")
        var lastIndex = 0

        for (match in tokens.findAll(code)) {
            val range = match.range
            if (range.first > lastIndex) {
                append(code.substring(lastIndex, range.first))
            }

            val token = match.value
            when {
                // Comments
                token.startsWith("//") || token.startsWith("#") -> {
                    pushStyle(SpanStyle(color = CodeComment))
                    append(token)
                    pop()
                }
                // Strings
                (token.startsWith("\"") && token.endsWith("\"")) ||
                (token.startsWith("'") && token.endsWith("'")) -> {
                    pushStyle(SpanStyle(color = CodeString))
                    append(token)
                    pop()
                }
                // Numbers
                token.all { it.isDigit() } -> {
                    pushStyle(SpanStyle(color = CodeNumber))
                    append(token)
                    pop()
                }
                // Keywords
                keywords.contains(token) -> {
                    pushStyle(SpanStyle(color = CodeKeyword, fontWeight = FontWeight.SemiBold))
                    append(token)
                    pop()
                }
                // Function calls or types
                token.firstOrNull()?.isUpperCase() == true -> {
                    pushStyle(SpanStyle(color = CodeFunction))
                    append(token)
                    pop()
                }
                else -> {
                    pushStyle(SpanStyle(color = Color(0xFFECEFF4)))
                    append(token)
                    pop()
                }
            }
            lastIndex = range.last + 1
        }

        if (lastIndex < code.length) {
            append(code.substring(lastIndex))
        }
    }
}
