package com.halilozel.codeblockview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinSyntaxTest {
    @Test
    fun highlightingDoesNotTreatCommentsOrStringsAsKeywords() {
        val source = "/* outer /* val */ fun */ val msg = \"fun // 123\" // class"
        val tokens = KotlinSyntax.tokens(source)
        assertEquals(listOf(KotlinSyntax.Kind.COMMENT, KotlinSyntax.Kind.KEYWORD,
            KotlinSyntax.Kind.STRING, KotlinSyntax.Kind.COMMENT), tokens.map { it.kind })
        assertEquals("val", source.substring(tokens[1].start, tokens[1].end))
        assertTrue(tokens.zipWithNext().all { (a, b) -> a.end <= b.start })
    }

    @Test
    fun rawStringsEscapesAndUnfinishedCodeHaveSafeRanges() {
        val source = "val raw = \"\"\"fun \"hi\" \"\"\"\nval s = \"escaped \\\" quote\"\n/* unfinished"
        val tokens = KotlinSyntax.tokens(source)
        assertEquals(2, tokens.count { it.kind == KotlinSyntax.Kind.STRING })
        assertEquals(KotlinSyntax.Kind.COMMENT, tokens.last().kind)
        assertEquals(source.length, tokens.last().end)
        assertTrue(tokens.all { it.start >= 0 && it.end <= source.length && it.start < it.end })
    }
}
