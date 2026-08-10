package com.eduprep.app.presentation.quiz

object MathHtmlBuilder {
    fun buildHtml(content: String, isDark: Boolean): String {
        val textColor = if (isDark) "#FAFAFA" else "#121212"
        val parsedBody = UmfParser.parseToHtml(content)

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <link rel="stylesheet" href="file:///android_asset/math/katex.css">
                <script defer src="file:///android_asset/math/katex.js"></script>
                <script defer src="file:///android_asset/math/contrib/auto-render.js" onload="initKaTeX()"></script>
                <style>
                    body {
                        color: $textColor;
                        background-color: transparent;
                        margin: 0; padding: 0;
                        font-family: sans-serif; font-size: 16px; line-height: 24px; word-wrap: break-word;
                    }
                    h1, h2, h3 { margin: 16px 0 8px 0; font-weight: bold; }
                    h1 { font-size: 1.5em; } h2 { font-size: 1.3em; } h3 { font-size: 1.1em; }
                    hr { border: 0; border-bottom: 1px solid ${if (isDark) "#555" else "#ccc"}; margin: 16px 0; }
                    pre { background: ${if (isDark) "#2d2d2d" else "#f4f4f4"}; padding: 12px; border-radius: 8px; overflow-x: auto; font-family: monospace; font-size: 14px; margin: 8px 0; }
                    code { background: ${if (isDark) "#2d2d2d" else "#f4f4f4"}; color: ${if (isDark) "#ff7b72" else "#d63384"}; padding: 2px 4px; border-radius: 4px; font-family: monospace; font-size: 0.9em; }
                    pre code { background: transparent; color: inherit; padding: 0; }
                    blockquote { border-left: 4px solid ${if (isDark) "#555" else "#ccc"}; padding-left: 12px; color: ${if (isDark) "#aaa" else "#666"}; margin: 8px 0; }
                    li.ul-item { list-style-type: disc; margin-left: 24px; }
                    li.ol-item { list-style-type: decimal; margin-left: 24px; }
                    table { border-collapse: collapse; width: 100%; margin: 10px 0; }
                    table, th, td { border: 1px solid ${if (isDark) "#555" else "#CCC"}; padding: 8px; }
                    img { max-width: 100%; height: auto; display: block; margin: 8px auto; }
                    .katex-display { overflow-x: auto; overflow-y: hidden; padding: 4px 0; }
                </style>
                <script>
                    function initKaTeX() {
                        if (typeof renderMathInElement !== 'undefined') {
                            renderMathInElement(document.body, {
                                delimiters: [
                                    {left: "$$", right: "$$", display: true},
                                    {left: "\\[", right: "\\]", display: true},
                                    {left: "\\(", right: "\\)", display: false},
                                    {left: "$", right: "$", display: false}, // Added single dollar inline support
                                    {left: "<math>", right: "</math>", display: false}
                                ],
                                throwOnError: false
                            });
                        }
                    }
                </script>
            </head>
            <body>
                $parsedBody
            </body>
            </html>
        """.trimIndent()
    }
}

object UmfParser {
    fun parseToHtml(content: String): String {
        // Regex to match math blocks cleanly, supporting single dollar sign inline math
        val pattern = """(\$\$.*?\$\$)|(\\\(.*?\\\))|(\\\[.*?\\\])|(\$.*?\$)|(<math>.*?</math>)""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val matches = pattern.findAll(content).toList()

        if (matches.isEmpty()) return parseMarkdownToHtml(content)

        val htmlBuilder = StringBuilder()
        var lastIdx = 0

        for (match in matches) {
            if (match.range.first > lastIdx) {
                htmlBuilder.append(parseMarkdownToHtml(content.substring(lastIdx, match.range.first)))
            }
            htmlBuilder.append(match.value) // Keep math blocks raw for KaTeX
            lastIdx = match.range.last + 1
        }

        if (lastIdx < content.length) {
            htmlBuilder.append(parseMarkdownToHtml(content.substring(lastIdx)))
        }

        return htmlBuilder.toString()
    }

    private fun parseMarkdownToHtml(text: String): String {
        var result = text.replace("\r\n", "\n").replace("\r", "\n")
            .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

        // 1. Extract and Protect Code Blocks (Multi-line)
        val codeBlocks = mutableListOf<String>()
        result = result.replace(Regex("""(?s)```(.*?)```""")) { match ->
            codeBlocks.add("<pre><code>${match.groupValues[1].trim()}</code></pre>")
            "###CODEBLOCK${codeBlocks.size - 1}###"
        }

        // 2. Inline Code
        result = result.replace(Regex("""`([^`]+)`""")) { match ->
            "<code>${match.groupValues[1]}</code>"
        }

        // 3. Headers (H1, H2, H3)
        result = result.replace(Regex("""(?m)^###\s+(.*)$"""), "<h3>$1</h3>")
        result = result.replace(Regex("""(?m)^##\s+(.*)$"""), "<h2>$1</h2>")
        result = result.replace(Regex("""(?m)^#\s+(.*)$"""), "<h1>$1</h1>")

        // 4. Horizontal Rules
        result = result.replace(Regex("""(?m)^---+$"""), "<hr>")
        result = result.replace(Regex("""(?m)^\*\*\*+$"""), "<hr>")

        // 5. Bold, Italics, Underline
        result = result.replace(Regex("""\*\*_(.*?)_\*\*"""), "<strong><u>$1</u></strong>")
        result = result.replace(Regex("""\*\*(.*?)\*\*"""), "<strong>$1</strong>")
        result = result.replace(Regex("""\*(.*?)\*"""), "<em>$1</em>")
        result = result.replace(Regex("""_(.*?)_"""), "<u>$1</u>")

        // 6. Blockquotes (supports escaped and unescaped greater-than sign)
        result = result.replace(Regex("""(?m)^(?:>|&gt;)\s+(.*)$"""), "<blockquote>$1</blockquote>")

        // 7. Lists (Bullets and Numbered)
        result = result.replace(Regex("""(?m)^[\-\*]\s+(.*)$"""), "<li class='ul-item'>$1</li>")
        result = result.replace(Regex("""(?m)^\d+\.\s+(.*)$"""), "<li class='ol-item'>$1</li>")

        // 8. Line Breaks (Convert \n to <br>, but prevent double spacing after block elements)
        result = result.replace("\n", "<br>")
        result = result.replace(Regex("""(</h[1-3]>|<hr>|</blockquote>|</li>)<br>"""), "$1")
        result = result.replace(Regex("""<br>(<li)"""), "$1")

        // 9. Restore Code Blocks
        codeBlocks.forEachIndexed { index, codeHtml ->
            result = result.replace("###CODEBLOCK$index###", codeHtml)
        }

        // Remove trailing <br> from restored codeblocks if any leaked through
        result = result.replace(Regex("""</pre><br>"""), "</pre>")

        return result
    }
}
