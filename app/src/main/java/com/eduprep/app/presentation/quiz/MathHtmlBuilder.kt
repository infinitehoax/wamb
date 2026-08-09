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
                <!-- Fetching Exact Local Files -->
                <link rel="stylesheet" href="math/katex.css">
                <script defer src="math/katex.js"></script>
                <script defer src="math/contrib/auto-render.js" onload="initKaTeX()"></script>
                <style>
                    body {
                        color: $textColor;
                        background-color: transparent;
                        margin: 0;
                        padding: 0;
                        font-family: sans-serif;
                        font-size: 16px;
                        line-height: 24px;
                        word-wrap: break-word;
                    }
                    img { max-width: 100%; height: auto; display: block; margin: 8px auto; }
                    .katex-display {
                        overflow-x: auto;
                        overflow-y: hidden;
                        padding: 4px 0;
                    }
                </style>
                <script>
                    function initKaTeX() {
                        if (typeof renderMathInElement !== 'undefined') {
                            renderMathInElement(document.body, {
                                delimiters: [
                                    {left: "$$", right: "$$", display: true},
                                    {left: "\\[", right: "\\]", display: true},
                                    {left: "\\(", right: "\\)", display: false},
                                    {left: "<math>", right: "</math>", display: false}
                                ],
                                throwOnError: false,
                                strict: false
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
        // More robust Regex to match math blocks without breaking standard LaTeX escaping
        val pattern = """(\$\$.*?\$\$)|(\\\(.*?\\\))|(\\\[.*?\\\])|(<math>.*?</math>)""".toRegex(RegexOption.DOT_MATCHES_ALL)

        val matches = pattern.findAll(content).toList()
        if (matches.isEmpty()) {
            return parseMarkdownToHtml(content)
        }

        val htmlBuilder = StringBuilder()
        var lastIdx = 0

        for (match in matches) {
            if (match.range.first > lastIdx) {
                val nonMathText = content.substring(lastIdx, match.range.first)
                htmlBuilder.append(parseMarkdownToHtml(nonMathText))
            }
            // Append math block exactly as-is to preserve backslashes for KaTeX
            htmlBuilder.append(match.value)
            lastIdx = match.range.last + 1
        }

        if (lastIdx < content.length) {
            val nonMathText = content.substring(lastIdx)
            htmlBuilder.append(parseMarkdownToHtml(nonMathText))
        }

        return htmlBuilder.toString()
    }

    private fun parseMarkdownToHtml(text: String): String {
        var result = text
        result = result.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

        result = result.replace(Regex("""\*\*_(.*?)_\*\*"""), "<strong><u>$1</u></strong>")
        result = result.replace(Regex("""\*\*(.*?)\*\*"""), "<strong>$1</strong>")
        result = result.replace(Regex("""\*(.*?)\*"""), "<em>$1</em>")
        result = result.replace(Regex("""_(.*?)_"""), "<u>$1</u>")

        // Map newline to HTML breaks
        result = result.replace("\n", "<br>")
        return result
    }
}
