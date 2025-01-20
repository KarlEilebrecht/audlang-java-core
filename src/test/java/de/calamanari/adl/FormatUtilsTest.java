//@formatter:off
/*
 * FormatUtilsTest
 * Copyright 2024 Karl Eilebrecht
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"):
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//@formatter:on

package de.calamanari.adl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class FormatUtilsTest {

    @Test
    void testStripTrailingWhitespace() {
        StringBuilder sb = new StringBuilder();

        FormatUtils.stripTrailingWhitespace(sb);
        assertEquals(0, sb.length());

        sb.append("w");
        FormatUtils.stripTrailingWhitespace(sb);
        assertEquals("w", sb.toString());

        sb.append("ord");
        FormatUtils.stripTrailingWhitespace(sb);
        assertEquals("word", sb.toString());

        sb.append("     word2");
        FormatUtils.stripTrailingWhitespace(sb);
        assertEquals("word     word2", sb.toString());

        sb.append(" ");
        FormatUtils.stripTrailingWhitespace(sb);
        assertEquals("word     word2", sb.toString());

        sb.append("\t");
        assertEquals("word     word2\t", sb.toString());
        FormatUtils.stripTrailingWhitespace(sb);
        assertEquals("word     word2", sb.toString());

        sb.append("\n");
        assertEquals("word     word2\n", sb.toString());
        FormatUtils.stripTrailingWhitespace(sb);
        assertEquals("word     word2", sb.toString());

    }

    @Test
    void testAppendIndent() {
        StringBuilder sb = new StringBuilder();

        FormatUtils.appendIndent(sb, FormatStyle.INLINE, 0);
        assertEquals(0, sb.length());

        sb.append("word");
        FormatUtils.appendIndent(sb, FormatStyle.INLINE, 0);
        assertEquals("word", sb.toString());

        sb.setLength(0);
        FormatUtils.appendIndent(sb, FormatStyle.PRETTY_PRINT, 0);
        assertEquals(0, sb.length());

        sb.append("word");
        FormatUtils.appendIndent(sb, FormatStyle.PRETTY_PRINT, 0);
        assertEquals("word", sb.toString());

        sb.setLength(0);

        FormatUtils.appendIndent(sb, FormatStyle.INLINE, 1);
        assertEquals(0, sb.length());

        FormatUtils.appendIndent(sb, FormatStyle.INLINE, 10);
        assertEquals(0, sb.length());

        sb.append("word");
        FormatUtils.appendIndent(sb, FormatStyle.INLINE, 1);
        assertEquals("word", sb.toString());
        FormatUtils.appendIndent(sb, FormatStyle.INLINE, 10);
        assertEquals("word", sb.toString());

        sb.setLength(0);
        FormatUtils.appendIndent(sb, FormatStyle.PRETTY_PRINT, 1);
        assertEquals(FormatConstants.DEFAULT_INDENT, sb.toString());

        sb.append("word");
        FormatUtils.appendIndent(sb, FormatStyle.PRETTY_PRINT, 1);
        assertEquals(FormatConstants.DEFAULT_INDENT + "word" + FormatConstants.DEFAULT_INDENT, sb.toString());
        FormatUtils.appendIndent(sb, FormatStyle.PRETTY_PRINT, 2);
        assertEquals(FormatConstants.DEFAULT_INDENT + "word" + FormatConstants.DEFAULT_INDENT + FormatConstants.DEFAULT_INDENT + FormatConstants.DEFAULT_INDENT,
                sb.toString());

    }

    @Test
    void testAppendIndentOrWhitespace() {
        StringBuilder sb = new StringBuilder();

        FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.INLINE, 0);
        assertEquals("", sb.toString());

        sb.append("word");
        FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.INLINE, 0);
        assertEquals("word ", sb.toString());

        FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.INLINE, 1);
        assertEquals("word ", sb.toString());

        FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.INLINE, 3);
        assertEquals("word ", sb.toString());

        sb.setLength(0);
        FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.PRETTY_PRINT, 0);
        assertEquals("", sb.toString());

        sb.append("word\n");
        FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.PRETTY_PRINT, 0);
        assertEquals("word\n", sb.toString());
        FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.PRETTY_PRINT, 1);
        assertEquals("word\n" + FormatConstants.DEFAULT_INDENT, sb.toString());
        FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.PRETTY_PRINT, 2);
        assertEquals("word\n" + FormatConstants.DEFAULT_INDENT, sb.toString());
        sb.append("word2\n");
        FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.PRETTY_PRINT, 2);
        assertEquals("word\n" + FormatConstants.DEFAULT_INDENT + "word2\n" + FormatConstants.DEFAULT_INDENT + FormatConstants.DEFAULT_INDENT, sb.toString());

        sb.setLength(0);

        assertSbEquals("", sb, () -> FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.INLINE, 0, false));
        assertSbEquals("", sb, () -> FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.INLINE, 2, false));
        assertSbEquals("", sb, () -> FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.INLINE, 0, true));
        assertSbEquals("", sb, () -> FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.INLINE, 2, true));

        assertSbEquals("\n", sb, () -> FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.PRETTY_PRINT, 0, true));
        assertSbEquals("\n        ", sb, () -> FormatUtils.appendIndentOrWhitespace(sb, FormatStyle.PRETTY_PRINT, 2, true));

    }

    @Test
    void testOpenBrace() {
        StringBuilder sb = new StringBuilder();

        FormatUtils.openBrace(sb);
        assertEquals("(", sb.toString());

        FormatUtils.openBrace(sb);
        assertEquals("( (", sb.toString());

        sb.append("name");
        FormatUtils.openBrace(sb);
        assertEquals("( (name(", sb.toString());

        sb.append("*/");
        FormatUtils.openBrace(sb);
        assertEquals("( (name(*/ (", sb.toString());

    }

    @Test
    void testCloseBrace() {
        StringBuilder sb = new StringBuilder();

        FormatUtils.closeBrace(sb);
        assertEquals(")", sb.toString());

        FormatUtils.closeBrace(sb);
        assertEquals(") )", sb.toString());

        sb.append("name");
        FormatUtils.closeBrace(sb);
        assertEquals(") )name)", sb.toString());

        sb.append("*/");
        FormatUtils.closeBrace(sb);
        assertEquals(") )name)*/ )", sb.toString());

    }

    @Test
    void testSpace() {
        StringBuilder sb = new StringBuilder();

        FormatUtils.space(sb);
        assertEquals(" ", sb.toString());

        FormatUtils.space(sb);
        assertEquals(" ", sb.toString());

        sb.append("\n");
        FormatUtils.space(sb);
        assertEquals(" \n", sb.toString());

        sb.append("\t");
        FormatUtils.space(sb);
        assertEquals(" \n\t", sb.toString());

        sb.append("word");
        FormatUtils.space(sb);
        assertEquals(" \n\tword ", sb.toString());

    }

    @Test
    void testAppendSpaced() {

        StringBuilder sb = new StringBuilder();
        assertSbEquals("a ", sb, () -> FormatUtils.appendSpaced(sb, "a"));
        assertSbEquals("a a ", sb, () -> FormatUtils.appendSpaced(sb, "a"));
        sb.append("\n");
        assertSbEquals("a a \na ", sb, () -> FormatUtils.appendSpaced(sb, "a"));

    }

    @Test
    void testComma() {
        StringBuilder sb = new StringBuilder();

        FormatUtils.comma(sb);
        assertEquals(",", sb.toString());

        FormatUtils.comma(sb);
        assertEquals(",,", sb.toString());

        sb.append("\n");
        FormatUtils.comma(sb);
        assertEquals(",,,", sb.toString());

        sb.append(" \n");
        FormatUtils.comma(sb);
        assertEquals(",,, ,", sb.toString());

        sb.append("\n ");
        FormatUtils.comma(sb);
        assertEquals(",,, ,\n ,", sb.toString());

    }

    @Test
    void testEndsWith() {
        StringBuilder sb = new StringBuilder();

        assertTrue(FormatUtils.endsWith(sb, ""));
        assertFalse(FormatUtils.endsWith(sb, "."));

        sb.append(".");
        assertTrue(FormatUtils.endsWith(sb, "."));

        sb.append("    ");
        assertTrue(FormatUtils.endsWith(sb, ".    "));
        assertTrue(FormatUtils.endsWith(sb, "    "));
        assertTrue(FormatUtils.endsWith(sb, " "));

        sb.append("\n");
        assertTrue(FormatUtils.endsWithNewLine(sb));

    }

    @Test
    void testNewLine() {
        StringBuilder sb = new StringBuilder();

        FormatUtils.newLine(sb);
        assertEquals("\n", sb.toString());
        FormatUtils.newLine(sb);
        assertEquals("\n", sb.toString());

        sb.append("word");
        FormatUtils.newLine(sb);
        assertEquals("\nword\n", sb.toString());

        sb.append("word     ");
        FormatUtils.newLine(sb);
        assertEquals("\nword\nword\n", sb.toString());

    }

    @Test
    void testAppendRepeat() {

        StringBuilder sb = new StringBuilder();

        FormatUtils.appendRepeat(sb, '.', 0);
        assertEquals("", sb.toString());

        FormatUtils.appendRepeat(sb, '.', 1);
        assertEquals(".", sb.toString());

        FormatUtils.appendRepeat(sb, '.', 5);
        assertEquals("......", sb.toString());

    }

    @Test
    void testAlignments() {

        StringBuilder sb = new StringBuilder();

        FormatUtils.appendAlignLeft(sb, "Hello", 0);

        assertEquals(0, sb.length());

        FormatUtils.appendAlignLeft(sb, "Hello", 1);

        assertEquals("H", sb.toString());

        sb.setLength(0);
        FormatUtils.appendAlignLeft(sb, "Hello", 10);

        assertEquals("Hello     ", sb.toString());

        sb.setLength(0);
        FormatUtils.appendAlignLeft(sb, null, 10);

        assertEquals("null      ", sb.toString());

        sb.setLength(0);

        FormatUtils.appendAlignRight(sb, "Hello", 0);

        assertEquals(0, sb.length());

        FormatUtils.appendAlignRight(sb, "Hello", 1);

        assertEquals("H", sb.toString());

        sb.setLength(0);

        FormatUtils.appendAlignRight(sb, "Hello", 10);

        assertEquals("     Hello", sb.toString());

        sb.setLength(0);

        FormatUtils.appendAlignRight(sb, null, 10);

        assertEquals("      null", sb.toString());

        sb.setLength(0);

        FormatUtils.appendAlignCenter(sb, "Hello", 0);

        assertEquals(0, sb.length());

        FormatUtils.appendAlignCenter(sb, "Hello", 1);

        assertEquals("H", sb.toString());

        sb.setLength(0);
        FormatUtils.appendAlignCenter(sb, "Hello", 10);

        assertEquals("  Hello   ", sb.toString());

        sb.setLength(0);
        FormatUtils.appendAlignCenter(sb, null, 10);

        assertEquals("   null   ", sb.toString());

    }

    @Test
    void testMaxLength() {

        assertEquals(0, FormatUtils.maxLength(Collections.emptyList()));

        assertEquals(4, FormatUtils.maxLength(Arrays.asList((String) null)));

        assertEquals(5, FormatUtils.maxLength(Arrays.asList("12345", "1", "1234", "12", "123")));

    }

    private static void assertSbEquals(String expected, StringBuilder sb, Runnable runnable) {
        runnable.run();
        assertEquals(expected, sb.toString());
    }
}
