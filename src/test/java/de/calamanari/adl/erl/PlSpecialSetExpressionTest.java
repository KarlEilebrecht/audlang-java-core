//@formatter:off
/*
 * PlSpecialSetExpressionTest
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

import static de.calamanari.adl.FormatStyle.INLINE;
import static de.calamanari.adl.FormatStyle.PRETTY_PRINT;
import static de.calamanari.adl.erl.SamplePlExpressions.ALL;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.EMPTY_COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.MULTI_LINE_COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.MULTI_LINE_COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.NONE;
import static de.calamanari.adl.erl.SamplePlExpressions.SHORT_COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.SHORT_COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C1;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_BEFORE_AND_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_MULTI_LINE_COMMENTS_BEFORE_AND_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_MULTI_LINE_COMMENTS_BEFORE_EXPR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.SpecialSetType;
import de.calamanari.adl.util.JsonUtils;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class PlSpecialSetExpressionTest {

    static final Logger LOGGER = LoggerFactory.getLogger(PlSpecialSetExpressionTest.class);

    @Test
    void testBasics() {

        assertEquals("<ALL>", ALL.toString());
        assertEquals("<ALL>", ALL.format(FormatStyle.INLINE));
        assertEquals("<ALL>", ALL.format(FormatStyle.PRETTY_PRINT));

        assertEquals("<NONE>", NONE.toString());
        assertEquals("<NONE>", NONE.format(FormatStyle.INLINE));
        assertEquals("<NONE>", NONE.format(FormatStyle.PRETTY_PRINT));

    }

    @Test
    void testInlineFormatComments() {
        assertEquals("/* comment BE */ <ALL>", ALL.withComments(SHORT_COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("/* */ <ALL>", ALL.withComments(EMPTY_COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("/* comment 1 before expression */ /* comment 2 before expression */ <ALL>", ALL.withComments(TWO_COMMENTS_BEFORE_EXPR).format(INLINE));
        assertEquals("/* comment VERY LONG VERY LONG VERY LONG VERY LONG before expression */ <ALL>",
                ALL.withComments(MULTI_LINE_COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY LONG before expression */ /* comment 2 VERY LONG VERY LONG VERY LONG VERY LONG before expression */ <ALL>",
                ALL.withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_EXPR).format(INLINE));

        assertEquals("<ALL> /* comment after expression */", ALL.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals("<ALL> /* comment 1 after expression */ /* comment 2 after expression */", ALL.withComments(TWO_COMMENTS_AFTER_EXPR).format(INLINE));
        assertEquals("<ALL> /* comment VERY LONG VERY LONG VERY LONG VERY LONG after expression */",
                ALL.withComments(MULTI_LINE_COMMENT_AFTER_EXPR).format(INLINE));

        assertEquals("<ALL> /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                ALL.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));

        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY LONG before expression */ <ALL> /* comment 2 VERY LONG VERY LONG VERY LONG VERY LONG after expression */",
                ALL.withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_AND_AFTER_EXPR).format(INLINE));

    }

    @Test
    void testPrettyFormatComments() {
        assertEquals("/* comment BE */ <ALL>", ALL.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("/* */ <ALL>", ALL.withComments(EMPTY_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("/* comment 1 before expression */\n/* comment 2 before expression */\n<ALL>",
                ALL.withComments(TWO_COMMENTS_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("/* comment VERY LONG VERY LONG VERY LONG VERY LONG\n   before expression */\n<ALL>",
                ALL.withComments(MULTI_LINE_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY\n   LONG before expression */\n/* comment 2 VERY LONG VERY LONG VERY LONG VERY\n   LONG before expression */\n<ALL>",
                ALL.withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_EXPR).format(PRETTY_PRINT));

        assertEquals("<ALL>\n/* comment after expression */", ALL.withComments(COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals("<ALL>\n/* comment 1 after expression */\n/* comment 2 after expression */",
                ALL.withComments(TWO_COMMENTS_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals("<ALL>\n/* comment VERY LONG VERY LONG VERY LONG VERY LONG\n   after expression */",
                ALL.withComments(MULTI_LINE_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));

        assertEquals("<ALL>\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                ALL.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));

        assertEquals("""
                /* comment 1 VERY LONG VERY LONG VERY LONG VERY
                   LONG before expression */
                <ALL>
                /* comment 2 VERY LONG VERY LONG VERY LONG VERY
                   LONG after expression */""", ALL.withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_AND_AFTER_EXPR).format(PRETTY_PRINT));

    }

    @Test
    void testSpecialCases() {
        assertThrows(AudlangValidationException.class, () -> new PlSpecialSetExpression(null, null));
        assertThrows(AudlangValidationException.class, () -> ALL.withComments(SINGLE_COMMENT_C1));
    }

    @Test
    void testResolveHigherLanguageFeatures() {
        assertEquals("<ALL>", ALL.withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_AND_AFTER_EXPR).resolveHigherLanguageFeatures().toString());
    }

    @Test
    void testEqualsHashCode() {

        PlSpecialSetExpression expr = new PlSpecialSetExpression(SpecialSetType.NONE, null);
        PlSpecialSetExpression expr2 = new PlSpecialSetExpression(SpecialSetType.NONE, null);

        assertNotSame(expr, expr2);

        assertEquals(expr, expr2);

        assertEquals(expr.hashCode(), expr2.hashCode());

    }

    @Test
    void testCommentHandling() {
        PlSpecialSetExpression expr = new PlSpecialSetExpression(SpecialSetType.NONE, null);

        PlSpecialSetExpression expr2 = new PlSpecialSetExpression(SpecialSetType.NONE, null);

        assertEquals(expr, expr2);
        assertEquals(expr, expr.stripComments());

        expr2 = expr2.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertNotEquals(expr, expr2);

        assertEquals(expr, expr2.stripComments());

        assertSame(expr, expr.withComments(null));
        assertSame(expr, expr.withComments(Collections.emptyList()));

        PlSpecialSetExpression expr3 = expr.withComments(SHORT_COMMENT_BEFORE_EXPR);

        assertSame(expr3, expr3.withComments(SHORT_COMMENT_BEFORE_EXPR));

        PlSpecialSetExpression expr4 = expr.withComments(SHORT_COMMENT_AFTER_EXPR);

        assertSame(expr4, expr4.withComments(SHORT_COMMENT_AFTER_EXPR));

    }

    @Test
    void testGetAllFieldsAndComments() {

        PlSpecialSetExpression expr = NONE.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertTrue(expr.allArgNames().isEmpty());

        List<PlExpression<?>> collectedExpressions = new ArrayList<>();
        expr.collectExpressions(e -> e instanceof PlMatchExpression m && m.argName().equals("argName"), collectedExpressions);
        assertTrue(collectedExpressions.isEmpty());

        // @formatter:off
        List<String> expectedComments = new ArrayList<>(Arrays.asList(
                TWO_COMMENTS_BEFORE_AND_AFTER_EXPR.get(0).comment(), 
                TWO_COMMENTS_BEFORE_AND_AFTER_EXPR.get(1).comment()
            ));
        // @formatter:on

        Collections.sort(expectedComments);

        List<String> comments = new ArrayList<>(expr.allComments().stream().map(PlComment::comment).toList());
        Collections.sort(comments);

        assertEquals(expectedComments, comments);

    }

    @Test
    void testJson() {

        PlSpecialSetExpression expr = NONE.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        String json = JsonUtils.writeAsJsonString(expr, true);

        PlExpression<?> res = JsonUtils.readFromJsonString(json, PlExpression.class);

        assertEquals(expr, res);
    }

}
