package com.eduprep.app.presentation.quiz

object MathHtmlBuilder {

    fun buildHtml(content: String, isDark: Boolean): String {
        val textColor = if (isDark) "#FAFAFA" else "#121212"
        val parsedBody = UmfParser.parseToHtml(content)

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                <link rel="stylesheet" href="file:///android_asset/math/katex.min.css">
                <script defer src="file:///android_asset/math/katex.min.js"></script>
                <script defer src="file:///android_asset/math/contrib/auto-render.min.js" onload="initKaTeX()"></script>
                <style>
                    body {
                        color: $textColor;
                        background-color: transparent;
                        margin: 0;
                        padding: 0;
                        font-family: sans-serif;
                        font-size: 16px;
                        line-height: 24px;
                    }
                    /* Custom scrollbar and overflow styles */
                    .katex-display {
                        overflow-x: auto;
                        overflow-y: hidden;
                        padding: 4px 0;
                    }
                </style>
                <script>
                    function initKaTeX() {
                        renderMathInElement(document.body, {
                            delimiters: [
                                {left: "$$", right: "$$", display: true},
                                {left: "\\\\(", right: "\\\\)", display: false},
                                {left: "\\(", right: "\\)", display: false},
                                {left: "<math>", right: "</math>", display: false}
                            ],
                            throwOnError: false
                        });
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
        // Regex to match math blocks: block $$, double-backslashed inline \\( ... \\), single-backslashed inline \( ... \), and <math>
        val pattern = """(\$\$.*?\$\$)|(\\\\\\\(.*?\\\\\\\))|(\\\(.*?\\\))|(<math>.*?</math>)""".toRegex(RegexOption.DOT_MATCHES_ALL)

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
            // Append math block exactly as-is to preserve backslashes for KaTeX parsing
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
        // Safe escaping of HTML control chars
        result = result.replace("&", "&amp;")
        result = result.replace("<", "&lt;")
        result = result.replace(">", "&gt;")

        // Bold-underline: **_text_** -> <strong><u>text</u></strong>
        result = result.replace(Regex("""\*\*_(.*?)_\*\*"""), "<strong><u>$1</u></strong>")
        // Bold: **text** -> <strong>text</strong>
        result = result.replace(Regex("""\*\*(.*?)\*\*"""), "<strong>$1</strong>")
        // Italics: *text* -> <em>text</em>
        result = result.replace(Regex("""\*(.*?)\*"""), "<em>$1</em>")
        // Underline: _text_ -> <u>text</u>
        result = result.replace(Regex("""_(.*?)_"""), "<u>$1</u>")

        // Map newline to HTML breaks
        result = result.replace("\n", "<br>")
        return result
    }
}
