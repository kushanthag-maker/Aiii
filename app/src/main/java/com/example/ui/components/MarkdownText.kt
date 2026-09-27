package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

private sealed class MarkdownSegment {
    data class Text(val content: String, val isHeading: Boolean = false, val isBullet: Boolean = false) : MarkdownSegment()
    data class CodeBlock(val code: String, val language: String) : MarkdownSegment()
}

@Composable
fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier
) {
    val segments = remember(content) { parseMarkdown(content) }

    Column(modifier = modifier.fillMaxWidth()) {
        segments.forEach { segment ->
            when (segment) {
                is MarkdownSegment.CodeBlock -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    CodeBlockView(
                        code = segment.code,
                        language = segment.language
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                is MarkdownSegment.Text -> {
                    if (segment.isBullet) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF60A5FA))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatInlineMarkdown(segment.content),
                                color = NovaTextPrimary,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }
                    } else if (segment.isHeading) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatInlineMarkdown(segment.content),
                            color = Color(0xFFF9FAFB),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Text(
                            text = formatInlineMarkdown(segment.content),
                            color = NovaTextPrimary,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun parseMarkdown(raw: String): List<MarkdownSegment> {
    val segments = mutableListOf<MarkdownSegment>()
    val lines = raw.lines()
    val textBuffer = StringBuilder()
    var inCodeBlock = false
    var codeLang = ""
    val codeBuffer = StringBuilder()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                // End of code block
                segments.add(MarkdownSegment.CodeBlock(codeBuffer.toString().trimEnd(), codeLang))
                codeBuffer.clear()
                codeLang = ""
                inCodeBlock = false
            } else {
                // Flush text buffer if any
                if (textBuffer.isNotBlank()) {
                    flushTextLines(textBuffer.toString(), segments)
                    textBuffer.clear()
                }
                // Start of code block
                codeLang = trimmed.removePrefix("```").trim()
                inCodeBlock = true
            }
        } else if (inCodeBlock) {
            codeBuffer.append(line).append("\n")
        } else {
            textBuffer.append(line).append("\n")
        }
    }

    if (inCodeBlock && codeBuffer.isNotBlank()) {
        segments.add(MarkdownSegment.CodeBlock(codeBuffer.toString().trimEnd(), codeLang))
    } else if (textBuffer.isNotBlank()) {
        flushTextLines(textBuffer.toString(), segments)
    }

    return segments
}

private fun flushTextLines(text: String, target: MutableList<MarkdownSegment>) {
    val lines = text.trimEnd().lines()
    val paragraph = StringBuilder()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("# ") || trimmed.startsWith("## ") || trimmed.startsWith("### ")) {
            if (paragraph.isNotBlank()) {
                target.add(MarkdownSegment.Text(paragraph.toString().trim()))
                paragraph.clear()
            }
            val headingText = trimmed.replace(Regex("^#+\\s*"), "")
            target.add(MarkdownSegment.Text(headingText, isHeading = true))
        } else if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("• ")) {
            if (paragraph.isNotBlank()) {
                target.add(MarkdownSegment.Text(paragraph.toString().trim()))
                paragraph.clear()
            }
            val bulletText = trimmed.substring(2)
            target.add(MarkdownSegment.Text(bulletText, isBullet = true))
        } else if (trimmed.isEmpty()) {
            if (paragraph.isNotBlank()) {
                target.add(MarkdownSegment.Text(paragraph.toString().trim()))
                paragraph.clear()
            }
        } else {
            paragraph.append(line).append("\n")
        }
    }

    if (paragraph.isNotBlank()) {
        target.add(MarkdownSegment.Text(paragraph.toString().trim()))
    }
}

/**
 * Handles inline **bold**, *italic*, and `code`
 */
private fun formatInlineMarkdown(text: String) = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        // Inline code `...`
        if (text[i] == '`') {
            val endIdx = text.indexOf('`', i + 1)
            if (endIdx != -1) {
                val inlineCode = text.substring(i + 1, endIdx)
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFF1E2230),
                        color = Color(0xFF93C5FD),
                        fontSize = 13.5.sp
                    )
                ) {
                    append(" $inlineCode ")
                }
                i = endIdx + 1
                continue
            }
        }

        // Bold **...**
        if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
            val endIdx = text.indexOf("**", i + 2)
            if (endIdx != -1) {
                val boldText = text.substring(i + 2, endIdx)
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                    append(boldText)
                }
                i = endIdx + 2
                continue
            }
        }

        // Italic *...*
        if (text[i] == '*') {
            val endIdx = text.indexOf('*', i + 1)
            if (endIdx != -1) {
                val italicText = text.substring(i + 1, endIdx)
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(italicText)
                }
                i = endIdx + 1
                continue
            }
        }

        append(text[i])
        i++
    }
}

@Composable
private fun Box(
    modifier: Modifier
) {
    androidx.compose.foundation.layout.Box(modifier = modifier)
}
