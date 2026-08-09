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
                        margin: 0;
                        padding: 0;
                        font-family: sans-serif;
                        font-size: 16px;
                        line-height: 24px;
                        word-wrap: break-word;
                    }
                    /* Noteless-style tables and blockquotes */
                    table { border-collapse: collapse; width: 100%; margin: 10px 0; }
                    table, th, td { border: 1px solid ${if (isDark) "#555" else "#CCC"}; padding: 8px; }
                    blockquote { border-left: 4px solid #4CAF50; padding-left: 12px; margin-left: 0; }
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
        var result = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        result = result.replace(Regex("""\*\*_(.*?)_\*\*"""), "<strong><u>$1</u></strong>")
        result = result.replace(Regex("""\*\*(.*?)\*\*"""), "<strong>$1</strong>")
        result = result.replace(Regex("""\*(.*?)\*"""), "<em>$1</em>")
        result = result.replace(Regex("""_(.*?)_"""), "<u>$1</u>")
        return result.replace("\n", "<br>")
    }
}
