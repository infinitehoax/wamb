package com.eduprep.app.presentation.quiz

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import java.util.UUID

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

object UmfParser {
    fun parseToHtml(content: String): String {
        var text = content.replace("\r\n", "\n").replace("\r", "\n")
        val protectionMap = mutableMapOf<String, String>()

        // 1. Protect Mermaid Diagrams
        text = Regex("""(?s)```mermaid\n(.*?)\n```""").replace(text) { match ->
            val id = "MERMAIDTOKEN" + UUID.randomUUID().toString().replace("-", "")
            protectionMap[id] = "<div class='mermaid'>${match.groupValues[1]}</div>"
            id
        }

        // 2. Protect Code Blocks
        text = Regex("""(?s)```(\w*)\n(.*?)\n```""").replace(text) { match ->
            val id = "CODETOKEN" + UUID.randomUUID().toString().replace("-", "")
            val lang = match.groupValues[1]
            val classAttr = if (lang.isEmpty()) "" else " class='language-$lang'"
            protectionMap[id] = "<pre><code$classAttr>${match.groupValues[2].replace("<", "&lt;").replace(">", "&gt;")}</code></pre>"
            id
        }

        // 3. Protect Math Blocks (Block & Inline)
        val realMathPattern = """(\$\$.*?\$\$)|(\\\(.*?\\\))|(\\\[.*?\\\])|(?<!\$)\$([^$]+)\$(?!\$)|(<math>.*?</math>)""".toRegex(RegexOption.DOT_MATCHES_ALL)
        text = realMathPattern.replace(text) { match ->
            val id = "MATHTOKEN" + UUID.randomUUID().toString().replace("-", "")
            protectionMap[id] = match.value
            id
        }

        // 4. Escape remaining HTML
        text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

        // 5. Parse Structural Block Elements
        // Headers
        text = Regex("""(?m)^######\s+(.*)$""").replace(text, "<h6>$1</h6>")
        text = Regex("""(?m)^#####\s+(.*)$""").replace(text, "<h5>$1</h5>")
        text = Regex("""(?m)^####\s+(.*)$""").replace(text, "<h4>$1</h4>")
        text = Regex("""(?m)^###\s+(.*)$""").replace(text, "<h3>$1</h3>")
        text = Regex("""(?m)^##\s+(.*)$""").replace(text, "<h2>$1</h2>")
        text = Regex("""(?m)^#\s+(.*)$""").replace(text, "<h1>$1</h1>")

        // Horizontal Rules
        text = Regex("""(?m)^---+$""").replace(text, "<hr>")
        text = Regex("""(?m)^\*\*\*+$""").replace(text, "<hr>")

        // Blockquotes
        text = Regex("""(?m)^(?:>|&gt;)\s+(.*)$""").replace(text, "<blockquote>$1</blockquote>")

        // 6. Parse Lists (Task, Bullet, Ordered)
        text = Regex("""(?m)^[\-\*]\s+\[ \]\s+(.*)$""").replace(text, "<li class='task-list-item'><input type='checkbox' disabled> $1</li>")
        text = Regex("""(?m)^[\-\*]\s+\[x\]\s+(.*)$""", RegexOption.IGNORE_CASE).replace(text, "<li class='task-list-item'><input type='checkbox' checked disabled> $1</li>")
        text = Regex("""(?m)^[\-\*]\s+(.*)$""").replace(text, "<li class='ul-item'>$1</li>")
        text = Regex("""(?m)^\d+\.\s+(.*)$""").replace(text, "<li class='ol-item'>$1</li>")

        // 7. Parse Tables (Basic)
        if (text.contains("|")) {
            text = text.split("\n").joinToString("\n") { line ->
                if (line.trim().startsWith("|") && line.trim().endsWith("|")) {
                    val cells = line.trim().removeSurrounding("|").split("|").map { it.trim() }
                    if (cells.all { it.matches(Regex("""^[:\-]+$""")) }) {
                        "" // Ignore standard markdown table separator lines
                    } else {
                        "<tr>" + cells.joinToString("") { "<td>$it</td>" } + "</tr>"
                    }
                } else {
                    line
                }
            }
            text = text.replace(Regex("""(?s)(<tr>.*?</tr>)""")) { "<table>${it.value}</table>" }
            text = text.replace("</table>\n<table>", "\n")
            text = text.replace("<table><tr><td>", "<table><tr><th>").replace("</td></tr>\n<tr><td>", "</th></tr>\n<tr><td>") // Crude header hack
        }

        // 8. Parse Inline Elements
        text = Regex("""\*\*_(.*?)_\*\*""").replace(text, "<strong><u>$1</u></strong>") // Bold Underline
        text = Regex("""\*\*(.*?)\*\*""").replace(text, "<strong>$1</strong>") // Bold
        text = Regex("""\*(.*?)\*""").replace(text, "<em>$1</em>") // Italic
        text = Regex("""_(.*?)_""").replace(text, "<u>$1</u>") // Underline
        text = Regex("""~~(.*?)~~""").replace(text, "<del>$1</del>") // Strikethrough
        text = Regex("""==(.*?)==""").replace(text, "<mark>$1</mark>") // Highlight
        text = Regex("""\|\|(.*?)\|\|""").replace(text, "<span class='spoiler' onclick='this.classList.toggle(\"revealed\")'>$1</span>") // Spoiler
        text = Regex("""\^([^\s\^]+)\^""").replace(text, "<sup>$1</sup>") // Superscript
        text = Regex("""~([^\s~]+)~""").replace(text, "<sub>$1</sub>") // Subscript
        text = Regex("""\[(.*?)\]\((.*?)\)""").replace(text, "<a href='$2'>$1</a>") // Links
        text = Regex("""`([^`]+)`""").replace(text, "<code>$1</code>") // Inline Code

        // 9. Line Breaks
        text = text.replace("\n", "<br>")
        text = Regex("""(</h[1-6]>|<hr>|</blockquote>|</li>|</table>|</pre>|<div class='mermaid'>.*</div>)<br>""").replace(text, "$1")
        text = Regex("""<br>(<li)""").replace(text, "$1")

        // 10. Restore Protections
        protectionMap.forEach { (id, original) ->
            text = text.replace(id, original)
        }

        // Remove trailing <br> from restored codeblocks if any leaked through
        text = text.replace(Regex("""</pre><br>"""), "</pre>")

        return text
    }
}
