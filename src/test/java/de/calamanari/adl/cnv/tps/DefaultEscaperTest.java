//@formatter:off
/*
 * DefaultEscaperTest
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

package de.calamanari.adl.cnv.tps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class DefaultEscaperTest {

    @Test
    void testDoubleQuoteStringInstance() {

        DefaultEscaper escaper = DefaultEscaper.doubleQuoteInstance();

        assertEquals("", escape(escaper, ""));
        assertEquals("b", escape(escaper, "b"));
        assertEquals("\\b", escape(escaper, "\b"));
        assertEquals("\\\"", escape(escaper, "\""));
        assertEquals("'", escape(escaper, "'"));
        assertEquals("bla \\\" bla", escape(escaper, "bla \" bla"));
        assertEquals("bla \\\\ bla", escape(escaper, "bla \\ bla"));
        assertEquals("bla \\\\", escape(escaper, "bla \\"));

        assertThrows(AdlFormattingException.class, () -> escape(escaper, "\u0003"));
        assertThrows(AdlFormattingException.class, () -> escape(escaper, "bla \u0003 bla"));

    }

    @Test
    void testSingleQuoteStringInstance() {

        DefaultEscaper escaper = DefaultEscaper.singleQuoteInstance();

        assertEquals("", escape(escaper, ""));
        assertEquals("b", escape(escaper, "b"));
        assertEquals("\\b", escape(escaper, "\b"));
        assertEquals("\\'", escape(escaper, "'"));
        assertEquals("\"", escape(escaper, "\""));
        assertEquals("bla \\' bla", escape(escaper, "bla ' bla"));
        assertEquals("bla \\\\ bla", escape(escaper, "bla \\ bla"));
        assertEquals("bla \\\\", escape(escaper, "bla \\"));

        assertThrows(AdlFormattingException.class, () -> escape(escaper, "\u0003"));
        assertThrows(AdlFormattingException.class, () -> escape(escaper, "bla \u0003 bla"));

    }

    @Test
    void testCustomInstance() {

        DefaultEscaper escaper = new DefaultEscaper("abc+", new String[] { "+a", "+b", "+c", "++" });

        assertEquals("", escape(escaper, ""));
        assertEquals("+b", escape(escaper, "b"));
        assertEquals("d+b+a+c", escape(escaper, "dbac"));
        assertEquals("d+++++c", escape(escaper, "d++c"));
        assertEquals("\\", escape(escaper, "\\"));
        assertEquals("\u0003", escape(escaper, "\u0003"));

        assertTrue(escaper.needsEscaping('a'));
        assertTrue(escaper.needsEscaping('b'));
        assertTrue(escaper.needsEscaping('c'));
        assertTrue(escaper.needsEscaping('+'));
        assertFalse(escaper.needsEscaping('\\'));

        assertTrue(escaper.isAllowed('a'));
        assertTrue(escaper.isAllowed('b'));
        assertTrue(escaper.isAllowed('c'));
        assertTrue(escaper.isAllowed('+'));
        assertTrue(escaper.isAllowed('\\'));

    }

    @Test
    void testCustomInstanceWithForbiddenCharacters() {

        DefaultEscaper escaper2 = new DefaultEscaper("a+", new String[] { "+a", "++" }, ch -> ch > 96);

        assertTrue(escaper2.needsEscaping('a'));
        assertFalse(escaper2.needsEscaping('b'));
        assertFalse(escaper2.needsEscaping('c'));
        assertTrue(escaper2.needsEscaping('+'));
        assertFalse(escaper2.needsEscaping('\\'));

        assertTrue(escaper2.isAllowed('a'));
        assertTrue(escaper2.isAllowed('b'));
        assertTrue(escaper2.isAllowed('c'));
        assertTrue(escaper2.isAllowed('+'));
        assertFalse(escaper2.isAllowed('\\'));

        assertThrows(AdlFormattingException.class, () -> escape(escaper2, "\u0003"));
        assertThrows(AdlFormattingException.class, () -> escape(escaper2, "bla \u0003 bla"));
        assertThrows(AdlFormattingException.class, () -> escape(escaper2, "BAD"));

        assertThrows(IllegalArgumentException.class, () -> new DefaultEscaper("a+", new String[] { "+a" }));

    }

    private static String escape(NativeEscaper escaper, String value) {
        StringBuilder sb = new StringBuilder();

        for (char ch : value.toCharArray()) {
            escaper.append(sb, ch);
        }

        return sb.toString();
    }
}
