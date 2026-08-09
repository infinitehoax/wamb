package com.eduprep.app.presentation.quiz

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

sealed class ContentBlock {
    data class Text(val content: String) : ContentBlock()
    data class Image(val alt: String, val filename: String) : ContentBlock()
}

object UmfMarkdownParser {

    /**
     * Splits a raw string into content blocks by scanning for markdown image tags `![alt](filename)`.
     */
    fun parseContent(content: String): List<ContentBlock> {
        val imageRegex = Regex("""!\[(.*?)\]\((.*?)\)""")
        val blocks = mutableListOf<ContentBlock>()
        var lastIndex = 0

        imageRegex.findAll(content).forEach { matchResult ->
            if (matchResult.range.first > lastIndex) {
                blocks.add(ContentBlock.Text(content.substring(lastIndex, matchResult.range.first)))
            }
            val alt = matchResult.groupValues[1]
            val filename = matchResult.groupValues[2]
            blocks.add(ContentBlock.Image(alt, filename))
            lastIndex = matchResult.range.last + 1
        }

        if (lastIndex < content.length) {
            blocks.add(ContentBlock.Text(content.substring(lastIndex)))
        }

        return blocks
    }

    /**
     * Converts simple markdown rich-text syntax (**_bold underline_**, **bold**, *italics*, _underline_)
     * into a Compose AnnotatedString.
     */
    fun parseMarkdownToAnnotatedString(text: String): AnnotatedString {
        val builder = AnnotatedString.Builder()
        // Regex pattern order is critical: match more specific bold-underline pattern first
        val tokenRegex = Regex("""(\*\*_(.*?)_\*\*)|(\*\*(.*?)\*\*)|(\*(.*?)\*)|(_(.*?)_)""")

        var lastMatchEnd = 0
        tokenRegex.findAll(text).forEach { matchResult ->
            if (matchResult.range.first > lastMatchEnd) {
                builder.append(text.substring(lastMatchEnd, matchResult.range.first))
            }

            val groupValues = matchResult.groupValues
            when {
                // Group 1 matches **_text_**
                groupValues[1].isNotEmpty() -> {
                    builder.withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(groupValues[2])
                    }
                }
                // Group 3 matches **text**
                groupValues[3].isNotEmpty() -> {
                    builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(groupValues[4])
                    }
                }
                // Group 5 matches *text*
                groupValues[5].isNotEmpty() -> {
                    builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(groupValues[6])
                    }
                }
                // Group 7 matches _text_
                groupValues[7].isNotEmpty() -> {
                    builder.withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(groupValues[8])
                    }
                }
            }
            lastMatchEnd = matchResult.range.last + 1
        }

        if (lastMatchEnd < text.length) {
            builder.append(text.substring(lastMatchEnd))
        }

        return builder.toAnnotatedString()
    }
}
