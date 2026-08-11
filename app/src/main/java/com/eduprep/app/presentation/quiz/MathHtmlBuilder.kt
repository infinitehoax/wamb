package com.eduprep.app.presentation.quiz

object MathHtmlBuilder {
    fun buildHtml(content: String, isDark: Boolean, isLoading: Boolean = false): String {
        val textColor = if (isDark) "#E0E0E0" else "#121212"
        val bgColor = "transparent"
        val surfaceColor = if (isDark) "#2C2C2C" else "#F4F4F4"
        val borderColor = if (isDark) "#444444" else "#DDDDDD"
        val linkColor = if (isDark) "#90CAF9" else "#1976D2"

        var finalContent = UmfParser.parseToHtml(content)
        if (isLoading) {
            finalContent += "<span class='blinking-cursor'>█</span>"
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">

                <!-- KaTeX -->
                <link rel="stylesheet" href="file:///android_asset/math/katex.css">
                <script defer src="file:///android_asset/math/katex.js"></script>
                <script defer src="file:///android_asset/math/contrib/auto-render.js" onload="initKaTeX()"></script>
                <script defer src="file:///android_asset/math/contrib/mhchem.js"></script>

                <!-- PrismJS (Syntax Highlighting) & Mermaid (Diagrams) -->
                <link rel="stylesheet" href="file:///android_asset/prism/prism.min.css">
                <script defer src="file:///android_asset/prism/prism.min.js"></script>
                <script defer src="file:///android_asset/mermaid/mermaid.min.js" onload="mermaid.initialize({startOnLoad:true, theme: '${if (isDark) "dark" else "default"}'})"></script>

                <style>
                    body {
                        color: $textColor; background-color: $bgColor;
                        margin: 0; padding: 0; font-family: -apple-system, sans-serif;
                        font-size: 16px; line-height: 1.6; word-wrap: break-word;
                    }

                    /* THE FIX: Force KaTeX to inherit Android Theme Colors */
                    .katex, .katex * { color: inherit !important; border-color: currentColor !important; }

                    /* Headers & Dividers */
                    h1, h2, h3, h4, h5, h6 { margin: 1.2em 0 0.6em 0; font-weight: 700; line-height: 1.2; }
                    h1 { font-size: 1.8em; border-bottom: 1px solid $borderColor; padding-bottom: 0.3em; }
                    h2 { font-size: 1.5em; border-bottom: 1px solid $borderColor; padding-bottom: 0.3em; }
                    h3 { font-size: 1.25em; }
                    hr { border: 0; height: 1px; background: $borderColor; margin: 1.5em 0; }

                    /* Links, Marks & Spoilers */
                    a { color: $linkColor; text-decoration: none; }
                    mark { background-color: #FFF59D; color: #000; padding: 0.1em 0.3em; border-radius: 4px; }
                    .spoiler { background-color: $textColor; color: transparent; border-radius: 4px; padding: 0 4px; cursor: pointer; transition: 0.3s; }
                    .spoiler.revealed { background-color: $surfaceColor; color: inherit; }

                    /* Lists & Tables */
                    ul, ol { padding-left: 1.5em; margin: 0.5em 0; }
                    li { margin-bottom: 0.25em; }
                    .task-list-item { list-style-type: none; margin-left: -1.5em; }
                    .task-list-item input { margin-right: 0.5em; }
                    table { border-collapse: collapse; width: 100%; margin: 1em 0; display: block; overflow-x: auto; }
                    th, td { border: 1px solid $borderColor; padding: 0.5em 0.8em; }
                    th { background-color: $surfaceColor; font-weight: bold; }

                    /* Blockquotes */
                    blockquote { border-left: 4px solid #2E7D32; margin: 1em 0; padding: 0.5em 1em; background: $surfaceColor; border-radius: 0 8px 8px 0; color: inherit; opacity: 0.85; }

                    /* Code Blocks */
                    code { background: $surfaceColor; padding: 0.2em 0.4em; border-radius: 4px; font-family: monospace; font-size: 0.9em; color: ${if (isDark) "#FF8A65" else "#D84315"}; }
                    pre { background: $surfaceColor !important; border: 1px solid $borderColor; border-radius: 8px; padding: 1em; overflow-x: auto; font-family: monospace; }
                    pre code { background: transparent; padding: 0; color: inherit; }

                    /* Interactive Cursor */
                    .blinking-cursor { animation: blink 1s step-end infinite; color: #2E7D32; }
                    @keyframes blink { 50% { opacity: 0; } }
                    img { max-width: 100%; height: auto; border-radius: 8px; margin: 1em auto; display: block; }
                </style>
                <script>
                    function initKaTeX() {
                        renderMathInElement(document.body, {
                            delimiters: [
                                {left: "$$", right: "$$", display: true},
                                {left: "\\[", right: "\\]", display: true},
                                {left: "\\(", right: "\\)", display: false},
                                {left: "$", right: "$", display: false}
                            ],
                            throwOnError: false
                        });
                    }

                    // Native Clipboard Bridge for Code Blocks
                    document.addEventListener('DOMContentLoaded', () => {
                        const preTags = document.querySelectorAll('pre');
                        preTags.forEach(pre => {
                            if(pre.classList.contains('mermaid')) return;
                            const btn = document.createElement('button');
                            btn.innerText = 'Copy';
                            btn.style.cssText = 'position:absolute; right:8px; top:8px; padding:4px 8px; font-size:12px; border-radius:4px; border:none; background:#2E7D32; color:#fff; cursor:pointer;';
                            pre.style.position = 'relative';
                            pre.appendChild(btn);
                            btn.onclick = () => {
                                const code = pre.querySelector('code').innerText;
                                AndroidInterface.copyToClipboard(code);
                                btn.innerText = 'Copied!';
                                setTimeout(() => btn.innerText = 'Copy', 2000);
                            };
                        });
                    });
                </script>
            </head>
            <body>
                $finalContent
            </body>
            </html>
        """.trimIndent()
    }
}
