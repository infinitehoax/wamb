package com.eduprep.app.presentation.quiz

import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun RichMathText(
    content: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default
) {
    val blocks = remember(content) { UmfMarkdownParser.parseContent(content) }
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is ContentBlock.Image -> {
                    ClickableAssetImage(
                        filename = block.filename,
                        altText = block.alt
                    )
                }
                is ContentBlock.Text -> {
                    val hasMath = remember(block.content) {
                        block.content.contains("$$") ||
                                block.content.contains("\\(") ||
                                block.content.contains("\\\\(") ||
                                block.content.contains("<math>") ||
                                // Single $ inline math support
                                block.content.contains("$")
                    }

                    if (!hasMath) {
                        // Fast Path: Pure native Compose Markdown Text
                        val annotated = remember(block.content) {
                            UmfMarkdownParser.parseMarkdownToAnnotatedString(block.content)
                        }
                        val finalFontSize = if (style.fontSize.isUnspecified) 16.sp else style.fontSize
                        val finalLineHeight = if (style.lineHeight.isUnspecified) 24.sp else style.lineHeight
                        val finalColor = style.color.takeOrElse { LocalContentColor.current }

                        Text(
                            text = annotated,
                            style = style.copy(
                                fontSize = finalFontSize,
                                lineHeight = finalLineHeight,
                                color = finalColor
                            )
                        )
                    } else {
                        // WebView Path for LaTeX rendering
                        val html = remember(block.content, isDark) {
                            MathHtmlBuilder.buildHtml(block.content, isDark)
                        }

                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    // THE FIX: Explicitly tell WebView to wrap its content height
                                    layoutParams = android.view.ViewGroup.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                    isVerticalScrollBarEnabled = false
                                    isHorizontalScrollBarEnabled = false
                                    overScrollMode = View.OVER_SCROLL_NEVER

                                    settings.apply {
                                        javaScriptEnabled = true
                                        allowFileAccess = true
                                        domStorageEnabled = true
                                        useWideViewPort = false
                                        loadWithOverviewMode = true
                                    }
                                }
                            },
                            update = { webView ->
                                webView.loadDataWithBaseURL(
                                    "file:///android_asset/",
                                    html,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                    }
                }
            }
        }
    }
}
