//@formatter:off
/*
 * PlCommentTest
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

package de.calamanari.adl.erl;

import java.util.Random;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.FormatStyle;

import static de.calamanari.adl.erl.PlComment.Position.BEFORE_OPERAND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class PlCommentTest {

    @Test
    void testLongCommentsMultiLine() {

        String veryLongNotSplittable = "/* 123456789022345678903234567890423456789052345678906234567890 */";
        String veryLongSplittable = "/* The quick brown fox jumped over the lazy dog on a sunny afternoon. */";
        String veryLongSplittableProtected = "/* name=\"The quick brown fox jumped over the lazy dog on a \"\"sunny afternoon\"\".\" */";

        PlComment comment = new PlComment(veryLongNotSplittable, BEFORE_OPERAND);
        assertEquals(veryLongNotSplittable, comment.format(FormatStyle.INLINE));
        assertEquals("""
                /*
                   123456789022345678903234567890423456789052345678906234567890
                */""", comment.format(FormatStyle.PRETTY_PRINT));

        comment = new PlComment(veryLongSplittable, BEFORE_OPERAND);
        assertEquals(veryLongSplittable, comment.format(FormatStyle.INLINE));
        assertEquals("/* The quick brown fox jumped over the lazy dog on\n   a sunny afternoon. */", comment.format(FormatStyle.PRETTY_PRINT));

        comment = new PlComment(veryLongSplittableProtected, BEFORE_OPERAND);
        assertEquals("/* name= \"The quick brown fox jumped over the lazy dog on a \"\"sunny afternoon\"\".\" */", comment.format(FormatStyle.INLINE));
        assertEquals("""
                /* name=
                   "The quick brown fox jumped over the lazy dog on a ""sunny afternoon""."
                */""", comment.format(FormatStyle.PRETTY_PRINT));

    }

    @Test
    void testCommentWithQuotedWhitespaceSingleLine() {

        String commentText = "/* \"                                                                   \" */";
        PlComment comment = new PlComment(commentText, BEFORE_OPERAND);
        assertEquals(commentText, comment.format(FormatStyle.INLINE));
        assertEquals("""
                /*
                   "                                                                   "
                */""", comment.format(FormatStyle.PRETTY_PRINT));

    }

    @Test
    void testSpecialCases() {

        assertThrows(AudlangValidationException.class, () -> new PlComment(null, BEFORE_OPERAND));
        assertThrows(AudlangValidationException.class, () -> new PlComment("", BEFORE_OPERAND));
        assertThrows(AudlangValidationException.class, () -> new PlComment("foo", BEFORE_OPERAND));
        assertThrows(AudlangValidationException.class, () -> new PlComment("/* foo", BEFORE_OPERAND));
        assertThrows(AudlangValidationException.class, () -> new PlComment(" /* foo bar */", BEFORE_OPERAND));
        assertThrows(AudlangValidationException.class, () -> new PlComment("/* foo bar */ ", BEFORE_OPERAND));

        assertThrows(AudlangValidationException.class, () -> new PlComment("/* comment */", null));

    }

    private void appendNextToken(StringBuilder sb, String[] fragments, Random rand) {
        sb.append(fragments[rand.nextInt(fragments.length)]);
    }

    private String createComment(Random rand) {

        String[] fragments = new String[] { " ", " ", " ", " ", " ", "\n", "hugo", "/*", "*/", "\"   bla    \"", "(", ")", ",", "=", "a", "b", "c", "1",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" };

        StringBuilder sb = new StringBuilder();
        sb.append("/*");

        int fCount = rand.nextInt(50);

        for (int i = 0; i < fCount; i++) {
            appendNextToken(sb, fragments, rand);
        }

        sb.append("*/");

        return sb.toString();
    }

    @Test
    void testReproducibleComments() {

        Random rand = new Random(8274463123255L);

        for (int i = 0; i < 100_000; i++) {

            PlComment comment = new PlComment(createComment(rand), BEFORE_OPERAND);

            String inline = comment.format(FormatStyle.INLINE);

            String pretty = comment.format(FormatStyle.PRETTY_PRINT);

            assertEquals(inline, new PlComment(pretty, BEFORE_OPERAND).format(FormatStyle.INLINE));
            assertEquals(pretty, new PlComment(inline, BEFORE_OPERAND).format(FormatStyle.PRETTY_PRINT));

        }

    }

}
