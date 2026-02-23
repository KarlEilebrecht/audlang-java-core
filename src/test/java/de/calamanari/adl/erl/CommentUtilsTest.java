//@formatter:off
/*
 * CommentUtilsTest
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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class CommentUtilsTest {

    static final Logger LOGGER = LoggerFactory.getLogger(CommentUtilsTest.class);

    @Test
    void testCommentTokenizer() {

        assertEquals("[/*, */]", CommentUtils.tokenizeComment("/**/").toString());

        assertEquals("[/*, /*, */, */]", CommentUtils.tokenizeComment("/* /* */ */").toString());

        assertEquals("[/*, The, quick, brown, fox, jumped, over, the, lazy, dog., */]",
                CommentUtils.tokenizeComment("/* The quick brown fox jumped over the lazy dog. */").toString());

        assertEquals("[/*, The, quick, brown, fox, jumped, over, the, lazy, dog., */]",
                CommentUtils.tokenizeComment("/* The quick         brown fox jumped over       the lazy   dog. */").toString());

        assertEquals("[/*, The, \"quick brown\", fox, jumped, over, the, lazy, dog., */]",
                CommentUtils.tokenizeComment("/* The \"quick brown\" fox jumped over the lazy dog. */").toString());

        assertEquals("[/*, The, \"quick \"\"brown\"\"\", fox, jumped, over, the, lazy, dog., */]",
                CommentUtils.tokenizeComment("/* The \"quick \"\"brown\"\"\" fox jumped over the lazy dog. */").toString());

        assertEquals("[/*, \"The quick brown fox jumped over the lazy dog., */]",
                CommentUtils.tokenizeComment("/* \"The quick brown fox jumped over the lazy dog. */").toString());

        assertEquals("[/*, \"This is a very long sentence that cannot be split because it is quoted.\", */]",
                CommentUtils.tokenizeComment("/* \"This is a very long sentence that cannot be split because it is quoted.\" */").toString());

        assertEquals("[/*, \"This is a very long sentence that cannot be split - quoting open!, */]",
                CommentUtils.tokenizeComment("/* \"This is a very long sentence that cannot be split - quoting open!                    */").toString());

        assertEquals("[/*, \"                                                                       \", */]",
                CommentUtils.tokenizeComment("/* \"                                                                       \" */").toString());

        assertEquals("[/*, ThisIsAVeryLongSentenceThatCannotBeSplitBecauseThereAreNoGaps, */]",
                CommentUtils.tokenizeComment("/* ThisIsAVeryLongSentenceThatCannotBeSplitBecauseThereAreNoGaps */").toString());

        assertEquals("[/*, This,Is,A,Very,, Long,Sentence,That,, Cannot,Be,Split,By,, Whitespace,But,By,, Comma, */]",
                CommentUtils.tokenizeComment("/* This,Is,A,Very,Long,Sentence,That,Cannot,Be,Split,By,Whitespace,But,By,Comma */").toString());

        assertEquals("[/*, ThisIsAVeryLongSentenceThatCannotBeSplitExceptForAtTheClosing), Brace, */]",
                CommentUtils.tokenizeComment("/* ThisIsAVeryLongSentenceThatCannotBeSplitExceptForAtTheClosing)Brace */").toString());

        assertEquals("[/*, aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, */]",
                CommentUtils.tokenizeComment("/* aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb */").toString());
        assertEquals("[/*, aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa(, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, */]",
                CommentUtils.tokenizeComment("/* aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa(bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb */").toString());
        assertEquals("[/*, aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa(, (, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, */]",
                CommentUtils.tokenizeComment("/* aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa((bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb */").toString());

    }

    @Test
    void testNormalizeComment() {

        assertEquals("/* */", CommentUtils.normalizeComment("/**/"));
        assertEquals("/* */", CommentUtils.normalizeComment("/*    */"));
        assertEquals("/* */", CommentUtils.normalizeComment("/* \n   */"));
        assertEquals("/* a=b */", CommentUtils.normalizeComment("/* a=b */"));
        assertEquals("/* a = b */", CommentUtils.normalizeComment("/* a = b */"));
        assertEquals("/* a= b */", CommentUtils.normalizeComment("/* a= b */"));
        assertEquals("/* a =b */", CommentUtils.normalizeComment("/* a =b */"));
        assertEquals("/* a> =b */", CommentUtils.normalizeComment("/* a> =b */"));
        assertEquals("/* a > = b */", CommentUtils.normalizeComment("/* a > = b */"));
        assertEquals("/* a < = b */", CommentUtils.normalizeComment("/* a < = b */"));
        assertEquals("/* a ! = b */", CommentUtils.normalizeComment("/* a ! = b */"));
        assertEquals("/* a != b */", CommentUtils.normalizeComment("/* a != b */"));
        assertEquals("/* a = ! b */", CommentUtils.normalizeComment("/* a = ! b  */"));
        assertEquals("/* ( (a != b) OR (b!=c) ) */", CommentUtils.normalizeComment("/* ( (a != b) OR (b!=c) ) */"));
        assertEquals("/* \"a\" ! = \"b\" */", CommentUtils.normalizeComment("/* \"a\"! =\"b\" */"));
        assertEquals("/* ( \"a\" ! = \"b\" ) */", CommentUtils.normalizeComment("/* ( \"a\"! =\"b\" ) */"));
        assertEquals("/* (a=b) OR (b=c) */", CommentUtils.normalizeComment("/* (a=b) OR (b=c) */"));
        assertEquals("/* The quick brown fox jumped over the lazy dog. */",
                CommentUtils.normalizeComment("/* The quick brown fox jumped over the lazy dog. */"));
        assertEquals("/* The quick brown fox jumped over the lazy dog. */",
                CommentUtils.normalizeComment("/*   The quick brown  fox \n\njumped over \tthe lazy dog.*/"));
        assertEquals("/* The \"quick  \"\"brown\"\"  fox\" jumped over the lazy dog. */",
                CommentUtils.normalizeComment("/*   The \n\"quick  \"\"brown\"\"  fox\" \n\njumped over \tthe lazy dog.*/"));

    }

    @Test
    void testNormalizeComplex() {
        assertEquals("/* aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa= bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb */",
                CommentUtils.normalizeComment("/* aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb */"));

        // the "gaps" below are expected artifacts from tokenization and recombination
        assertEquals("/* aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa( (bbbbbbbbbbbbb)) bbbbbbbbbbbbbbbbbbbbbbbbbbbb */",
                CommentUtils.normalizeComment("/* aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa((bbbbbbbbbbbbb))bbbbbbbbbbbbbbbbbbbbbbbbbbbb */"));

        assertEquals("/* aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb */",
                CommentUtils.normalizeComment("/* aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb */"));

    }

}
