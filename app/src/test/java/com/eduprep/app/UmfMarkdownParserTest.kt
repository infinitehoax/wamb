package com.eduprep.app

import com.eduprep.app.presentation.quiz.ContentBlock
import com.eduprep.app.presentation.quiz.UmfMarkdownParser
import com.eduprep.app.presentation.quiz.UmfParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UmfMarkdownParserTest {

    @Test
    fun testParseContentWithImages() {
        val rawText = "This is text before ![Biology Circulatory System](circulatory_system.png) and this is text after."
        val blocks = UmfMarkdownParser.parseContent(rawText)

        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is ContentBlock.Text)
        assertEquals("This is text before ", (blocks[0] as ContentBlock.Text).content)

        assertTrue(blocks[1] is ContentBlock.Image)
        val imgBlock = blocks[1] as ContentBlock.Image
        assertEquals("Biology Circulatory System", imgBlock.alt)
        assertEquals("circulatory_system.png", imgBlock.filename)

        assertTrue(blocks[2] is ContentBlock.Text)
        assertEquals(" and this is text after.", (blocks[2] as ContentBlock.Text).content)
    }

    @Test
    fun testParseToHtmlLeavesLaTexIntact() {
        val rawText = "Solve this: **_bold underline_** $$ x = \\frac{-b \\pm \\sqrt{b^2-4ac}}{2a} $$ where \$a\$ and \$b\$ are constants."
        val htmlOutput = UmfParser.parseToHtml(rawText)

        // Bold-underline should be formatted
        assertTrue(htmlOutput.contains("<strong><u>bold underline</u></strong>"))
        // LaTeX block should be preserved exactly as-is
        assertTrue(htmlOutput.contains("$$ x = \\frac{-b \\pm \\sqrt{b^2-4ac}}{2a} $$"))
    }

    @Test
    fun testParseMarkdownToHtmlOnly() {
        val rawText = "Normal text, **bold text**, *italic text*, _underline_ and **_bold underline_**"
        val htmlOutput = UmfParser.parseToHtml(rawText)

        assertTrue(htmlOutput.contains("<strong>bold text</strong>"))
        assertTrue(htmlOutput.contains("<em>italic text</em>"))
        assertTrue(htmlOutput.contains("<u>underline</u>"))
        assertTrue(htmlOutput.contains("<strong><u>bold underline</u></strong>"))
    }
}
