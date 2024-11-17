//@formatter:off
/*
 * PlNegationExpressionTest
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
import static de.calamanari.adl.erl.PlComment.Position.AFTER_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.C1;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.ANY_OF;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.BETWEEN;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.CONTAINS;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.CONTAINS_ANY_OF;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.EQUALS;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.GREATER_THAN;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.GREATER_THAN_OR_EQUALS;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.IS_NOT_UNKNOWN;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.IS_UNKNOWN;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.LESS_THAN;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.LESS_THAN_OR_EQUALS;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.NOT_ANY_OF;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.NOT_BETWEEN;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.NOT_CONTAINS;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.NOT_CONTAINS_ANY_OF;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.NOT_EQUALS;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.STRICT_NOT_ANY_OF;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.STRICT_NOT_BETWEEN;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.STRICT_NOT_CONTAINS;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.STRICT_NOT_CONTAINS_ANY_OF;
import static de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator.STRICT_NOT_EQUALS;
import static de.calamanari.adl.erl.SamplePlExpressions.ALL;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_AFTER_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_BEFORE_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.EMPTY_COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.MULTI_LINE_COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.MULTI_LINE_COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.SHORT_COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.SHORT_COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C1;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C2;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_BEFORE_AND_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_MULTI_LINE_COMMENTS_BEFORE_AND_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_MULTI_LINE_COMMENTS_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.and;
import static de.calamanari.adl.erl.SamplePlExpressions.comments;
import static de.calamanari.adl.erl.SamplePlExpressions.lop;
import static de.calamanari.adl.erl.SamplePlExpressions.match;
import static de.calamanari.adl.erl.SamplePlExpressions.not;
import static de.calamanari.adl.erl.SamplePlExpressions.or;
import static de.calamanari.adl.erl.SamplePlExpressions.rop;
import static de.calamanari.adl.erl.SamplePlExpressions.strictNot;
import static de.calamanari.adl.erl.SamplePlExpressions.vop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.AudlangField;
import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.erl.PlCurbExpression.PlCurbOperator;
import de.calamanari.adl.util.JsonUtils;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class PlNegationExpressionTest {

    static final Logger LOGGER = LoggerFactory.getLogger(PlNegationExpressionTest.class);

    @Test
    void testBasics() {

        assertEquals("NOT <ALL>", not(ALL).toString());
        assertEquals("NOT <ALL>", not(ALL).format(FormatStyle.INLINE));
        assertEquals("NOT <ALL>", not(ALL).format(FormatStyle.PRETTY_PRINT));

        assertEquals("STRICT NOT <ALL>", strictNot(ALL).toString());
        assertEquals("STRICT NOT <ALL>", strictNot(ALL).format(FormatStyle.INLINE));
        assertEquals("STRICT NOT <ALL>", strictNot(ALL).format(FormatStyle.PRETTY_PRINT));

        assertEquals("NOT (a = 1 AND b ANY OF (2, 3, 4) )",
                not(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))))).toString());
        assertEquals("NOT (a = 1 AND b ANY OF (2, 3, 4) )",
                not(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))))).format(INLINE));

        assertEquals("""
                NOT (
                        a = 1
                    AND b ANY OF (2, 3, 4)
                    )""", not(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))))).format(PRETTY_PRINT));

        assertEquals("NOT NOT (a = 1 AND b ANY OF (2, 3, 4) )",
                not(not(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4")))))).toString());
        assertEquals("NOT NOT (a = 1 AND b ANY OF (2, 3, 4) )",
                not(not(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4")))))).format(INLINE));

        assertEquals("""
                NOT NOT (
                            a = 1
                        AND b ANY OF (2, 3, 4)
                        )""", not(not(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4")))))).format(PRETTY_PRINT));

    }

    @Test
    void testInlineFormatComments() {
        assertEquals("/* comment before expression */ NOT <ALL>", not(ALL).withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("NOT /* comment before expression */ <ALL>", not(ALL.withComments(COMMENT_BEFORE_EXPR)).format(INLINE));

        assertEquals("/* */ NOT <ALL>", not(ALL).withComments(EMPTY_COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("/* comment 1 before expression */ /* comment 2 before expression */ NOT <ALL>",
                not(ALL).withComments(TWO_COMMENTS_BEFORE_EXPR).format(INLINE));
        assertEquals("/* comment VERY LONG VERY LONG VERY LONG VERY LONG before expression */ NOT <ALL>",
                not(ALL).withComments(MULTI_LINE_COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY LONG before expression */ /* comment 2 VERY LONG VERY LONG VERY LONG VERY LONG before expression */ NOT <ALL>",
                not(ALL).withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_EXPR).format(INLINE));
        assertEquals("NOT <ALL> /* comment after expression */", not(ALL).withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals("NOT <ALL> /* comment 1 after expression */ /* comment 2 after expression */",
                not(ALL).withComments(TWO_COMMENTS_AFTER_EXPR).format(INLINE));
        assertEquals("NOT <ALL> /* comment VERY LONG VERY LONG VERY LONG VERY LONG after expression */",
                not(ALL).withComments(MULTI_LINE_COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals("NOT <ALL> /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                not(ALL).withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY LONG before expression */ NOT <ALL> /* comment 2 VERY LONG VERY LONG VERY LONG VERY LONG after expression */",
                not(ALL).withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_AND_AFTER_EXPR).format(INLINE));

        assertEquals("/* */ /* Hugo */ /* Fluffy */ NOT <ALL>",
                not(ALL).withComments(comments(BEFORE_EXPRESSION, "/**/", "/* Hugo */", "/* Fluffy */")).format(INLINE));

        assertEquals("NOT /* */ <ALL>", not(ALL.withComments(comments(BEFORE_EXPRESSION, "/**/"))).format(INLINE));
        assertEquals("NOT <ALL> /* */", not(ALL).withComments(comments(AFTER_EXPRESSION, "/**/")).format(INLINE));

        assertEquals("NOT /* */ <ALL>", not(ALL.withComments(comments(BEFORE_EXPRESSION, "/*\n\n\n  */"))).format(INLINE));

        assertEquals("NOT NOT /* */ <ALL>", not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/**/")))).format(INLINE));

        assertEquals("NOT NOT /* */ <ALL> /* after all */", not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/*\n\n\n  */"))))
                .withComments(comments(AFTER_EXPRESSION, "/* after all */")).format(INLINE));

        assertEquals("NOT /* C1 of first NOT */ NOT /* */ <ALL>",
                not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/*\n\n\n  */"))).withComments(comments(BEFORE_EXPRESSION, "/* C1 of first NOT */")))
                        .format(INLINE));

        assertEquals("STRICT /* */ NOT <ALL>", strictNot(ALL).withComments(comments(C1, "/**/")).format(INLINE));

        assertEquals("STRICT /* C1 VERY LONG VERY LONG VERY LONG VERY LONG comment */ NOT /* BE comment */ <ALL>",
                strictNot(ALL.withComments(comments(new PlComment("/* BE comment */", BEFORE_EXPRESSION))))
                        .withComments(comments(new PlComment("/* C1 VERY LONG VERY LONG VERY LONG VERY LONG comment */", C1))).format(INLINE));

        assertEquals(
                "/* before all VERY LONG VERY LONG VERY LONG VERY LONG */ STRICT /* C1 VERY LONG VERY LONG VERY LONG VERY LONG comment */ NOT /* BE comment */ <ALL> /* after all VERY LONG VERY LONG VERY LONG VERY LONG */",
                strictNot(ALL.withComments(comments(new PlComment("/* BE comment */", BEFORE_EXPRESSION))))
                        .withComments(comments(new PlComment("/* C1 VERY LONG VERY LONG VERY LONG VERY LONG comment */", C1),
                                new PlComment("/* before all VERY LONG VERY LONG VERY LONG VERY LONG */", BEFORE_EXPRESSION),
                                new PlComment("/* after all VERY LONG VERY LONG VERY LONG VERY LONG */", AFTER_EXPRESSION)))
                        .format(INLINE));

    }

    @Test
    void testPrettyFormatComments() {
        assertEquals("/* comment before expression */\nNOT <ALL>", not(ALL).withComments(COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("NOT /* comment before expression */ <ALL>", not(ALL.withComments(COMMENT_BEFORE_EXPR)).format(INLINE));

        assertEquals("/* */ NOT <ALL>", not(ALL).withComments(EMPTY_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("/* comment 1 before expression */\n/* comment 2 before expression */\nNOT <ALL>",
                not(ALL).withComments(TWO_COMMENTS_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("/* comment VERY LONG VERY LONG VERY LONG VERY LONG\n   before expression */\nNOT <ALL>",
                not(ALL).withComments(MULTI_LINE_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY\n   LONG before expression */\n/* comment 2 VERY LONG VERY LONG VERY LONG VERY\n   LONG before expression */\nNOT <ALL>",
                not(ALL).withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("NOT <ALL>\n    /* comment after expression */", not(ALL).withComments(COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals("NOT <ALL>\n    /* comment 1 after expression */\n    /* comment 2 after expression */",
                not(ALL).withComments(TWO_COMMENTS_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals("NOT <ALL>\n    /* comment VERY LONG VERY LONG VERY LONG VERY LONG\n       after expression */",
                not(ALL).withComments(MULTI_LINE_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals(
                "NOT <ALL>\n    /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n       VERY LONG after expression */\n    /* comment 2 after expression */",
                not(ALL).withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));

        // Note: The indentation of the comment after the not is no mistake. Because it is indistinguishable from the wrapped expression's comment
        // it gets sucked by the delegate of the negation
        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY\n   LONG before expression */\nNOT <ALL>\n    /* comment 2 VERY LONG VERY LONG VERY LONG VERY\n       LONG after expression */",
                not(ALL).withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_AND_AFTER_EXPR).format(PRETTY_PRINT));

        assertEquals("NOT /* */ <ALL>", not(ALL.withComments(comments(BEFORE_EXPRESSION, "/* */"))).format(PRETTY_PRINT));
        assertEquals("NOT /* */ <ALL>", not(ALL.withComments(comments(BEFORE_EXPRESSION, "/*\n\n\n  */"))).format(PRETTY_PRINT));

        assertEquals("NOT NOT /* */ <ALL>", not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/**/")))).format(PRETTY_PRINT));
        assertEquals("NOT NOT /* */ <ALL>", not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/*\n\n\n  */")))).format(PRETTY_PRINT));

        assertEquals("NOT NOT /* */ <ALL> /* after all */", not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/*\n\n\n  */"))))
                .withComments(comments(AFTER_EXPRESSION, "/* after all */")).format(PRETTY_PRINT));

        assertEquals("""
                NOT /* comment BE */ NOT
                        /* VERY LONG VERY LONG VERY LONG VERY LONG VERY
                           LONG VERY LONG VERY LONG VERY LONG */
                        <ALL>""",
                not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/* VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")))
                        .withComments(SHORT_COMMENT_BEFORE_EXPR)).format(PRETTY_PRINT));

        assertEquals("""
                NOT
                    /* comment before expression */
                    NOT
                        /* VERY LONG VERY LONG VERY LONG VERY LONG VERY
                           LONG VERY LONG VERY LONG VERY LONG */
                        <ALL>""",
                not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/* VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")))
                        .withComments(COMMENT_BEFORE_EXPR)).format(PRETTY_PRINT));

        assertEquals("STRICT /* */ NOT <ALL>", strictNot(ALL).withComments(comments(C1, "/**/")).format(PRETTY_PRINT));

        assertEquals("""
                STRICT
                /* C1 VERY LONG VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG comment */
                NOT /* BE comment */ <ALL>""",
                strictNot(ALL.withComments(comments(new PlComment("/* BE comment */", BEFORE_EXPRESSION))))
                        .withComments(comments(new PlComment("/* C1 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG comment */", C1)))
                        .format(PRETTY_PRINT));

        assertEquals("""
                /* before all VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG VERY LONG */
                STRICT
                /* C1 VERY LONG VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG comment */
                NOT /* BE comment */ <ALL>
                    /* after all VERY LONG VERY LONG VERY LONG VERY
                       LONG VERY LONG VERY LONG */""",
                strictNot(ALL.withComments(comments(new PlComment("/* BE comment */", BEFORE_EXPRESSION))))
                        .withComments(comments(new PlComment("/* C1 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG comment */", C1),
                                new PlComment("/* before all VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */", BEFORE_EXPRESSION),
                                new PlComment("/* after all VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */", AFTER_EXPRESSION)))
                        .format(PRETTY_PRINT));

    }

    @Test
    void testSpecialCases() {

        assertThrows(AudlangValidationException.class, () -> new PlNegationExpression(null, false, null));
        assertThrows(AudlangValidationException.class, () -> new PlNegationExpression(ALL, false, SINGLE_COMMENT_C1));
        assertThrows(AudlangValidationException.class, () -> new PlNegationExpression(ALL, true, SINGLE_COMMENT_C2));

    }

    @Test
    void testResolveHigherLanguageFeaturesNegatedPositiveMatch() {

        PlNegationExpression expr = not(new PlMatchExpression("arg", IS_UNKNOWN, null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg = 1 OR arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", ANY_OF, lop(vop("1"), vop("2"), vop("3")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(STRICT NOT arg = 1 OR arg IS UNKNOWN) AND (STRICT NOT arg = 2 OR arg IS UNKNOWN) AND (STRICT NOT arg = 3 OR arg IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", GREATER_THAN, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg > 1 OR arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", LESS_THAN, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg < 1 OR arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", GREATER_THAN_OR_EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(STRICT NOT arg > 1 OR arg IS UNKNOWN) AND (STRICT NOT arg = 1 OR arg IS UNKNOWN)", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", LESS_THAN_OR_EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(STRICT NOT arg < 1 OR arg IS UNKNOWN) AND (STRICT NOT arg = 1 OR arg IS UNKNOWN)", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", BETWEEN, lop(vop("1"), vop("5")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals(
                "( (STRICT NOT arg > 1 OR arg IS UNKNOWN) AND (STRICT NOT arg = 1 OR arg IS UNKNOWN) ) OR ( (STRICT NOT arg < 5 OR arg IS UNKNOWN) AND (STRICT NOT arg = 5 OR arg IS UNKNOWN) )",
                expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", CONTAINS, lop(vop("x")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg CONTAINS x OR arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", CONTAINS_ANY_OF, lop(vop("x"), vop("y"), vop("z")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals(
                "(STRICT NOT arg CONTAINS x OR arg IS UNKNOWN) AND (STRICT NOT arg CONTAINS y OR arg IS UNKNOWN) AND (STRICT NOT arg CONTAINS z OR arg IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

    }

    @Test
    void testResolveHigherLanguageFeaturesNegatedPositiveMatchRef() {

        PlNegationExpression expr = not(new PlMatchExpression("arg", EQUALS, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg = @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", ANY_OF, lop(vop("1"), vop("2"), rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals(
                "(STRICT NOT arg = 1 OR arg IS UNKNOWN) AND (STRICT NOT arg = 2 OR arg IS UNKNOWN) AND (STRICT NOT arg = @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", GREATER_THAN, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg > @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", LESS_THAN, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg < @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", GREATER_THAN_OR_EQUALS, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(STRICT NOT arg > @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN) AND (STRICT NOT arg = @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", LESS_THAN_OR_EQUALS, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(STRICT NOT arg < @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN) AND (STRICT NOT arg = @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

    }

    @Test
    void testResolveHigherLanguageFeaturesStrictNegatedPositiveMatch() {

        PlNegationExpression expr = strictNot(new PlMatchExpression("arg", IS_UNKNOWN, null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg = 1", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", ANY_OF, lop(vop("1"), vop("2"), vop("3")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg = 1 AND STRICT NOT arg = 2 AND STRICT NOT arg = 3", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", GREATER_THAN, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg > 1", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", LESS_THAN, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg < 1", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", GREATER_THAN_OR_EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg > 1 AND STRICT NOT arg = 1", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", LESS_THAN_OR_EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg < 1 AND STRICT NOT arg = 1", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", BETWEEN, lop(vop("1"), vop("5")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(STRICT NOT arg > 1 AND STRICT NOT arg = 1) OR (STRICT NOT arg < 5 AND STRICT NOT arg = 5)",
                expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", CONTAINS, lop(vop("x")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg CONTAINS x", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(
                new PlMatchExpression("arg", CONTAINS_ANY_OF, lop(vop("x"), vop("y"), vop("z")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg CONTAINS x AND STRICT NOT arg CONTAINS y AND STRICT NOT arg CONTAINS z", expr.resolveHigherLanguageFeatures().toString());

    }

    @Test
    void testResolveHigherLanguageFeaturesStrictNegatedPositiveMatchRef() {

        PlNegationExpression expr = strictNot(new PlMatchExpression("arg", EQUALS, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg = @arg2", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", ANY_OF, lop(vop("1"), vop("2"), rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg = 1 AND STRICT NOT arg = 2 AND STRICT NOT arg = @arg2", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", GREATER_THAN, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg > @arg2", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", LESS_THAN, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg < @arg2", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", GREATER_THAN_OR_EQUALS, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg > @arg2 AND STRICT NOT arg = @arg2", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", LESS_THAN_OR_EQUALS, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("STRICT NOT arg < @arg2 AND STRICT NOT arg = @arg2", expr.resolveHigherLanguageFeatures().toString());

    }

    @Test
    void testResolveHigherLanguageFeaturesNegatedIntraNegativeMatch() {

        PlNegationExpression expr = not(new PlMatchExpression("arg", IS_NOT_UNKNOWN, null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", NOT_EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(arg = 1 OR arg IS UNKNOWN) AND STRICT NOT arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", NOT_ANY_OF, lop(vop("1"), vop("2"), vop("3")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("( (arg = 1 OR arg IS UNKNOWN) AND STRICT NOT arg IS UNKNOWN) OR ( (arg = 2 OR arg IS UNKNOWN) AND STRICT NOT arg IS UNKNOWN) "
                + "OR ( (arg = 3 OR arg IS UNKNOWN) AND STRICT NOT arg IS UNKNOWN)", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", NOT_BETWEEN, lop(vop("1"), vop("5")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals(
                "( (arg > 1 OR arg IS UNKNOWN) OR (arg = 1 OR arg IS UNKNOWN) ) AND ( (arg < 5 OR arg IS UNKNOWN) OR (arg = 5 OR arg IS UNKNOWN) ) AND STRICT NOT arg IS UNKNOWN",
                expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", NOT_CONTAINS, lop(vop("x")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(arg CONTAINS x OR arg IS UNKNOWN) AND STRICT NOT arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", NOT_CONTAINS_ANY_OF, lop(vop("x"), vop("y"), vop("z")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals(
                "( (arg CONTAINS x OR arg IS UNKNOWN) AND STRICT NOT arg IS UNKNOWN) OR ( (arg CONTAINS y OR arg IS UNKNOWN) AND STRICT NOT arg IS UNKNOWN) "
                        + "OR ( (arg CONTAINS z OR arg IS UNKNOWN) AND STRICT NOT arg IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", STRICT_NOT_EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        // special case §5 Audlang

        assertEquals("arg = 1 OR arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", STRICT_NOT_ANY_OF, lop(vop("1"), vop("2"), vop("3")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(arg = 1 OR arg IS UNKNOWN) OR (arg = 2 OR arg IS UNKNOWN) OR (arg = 3 OR arg IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", STRICT_NOT_BETWEEN, lop(vop("1"), vop("5")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("( (arg > 1 OR arg IS UNKNOWN) OR (arg = 1 OR arg IS UNKNOWN) ) AND ( (arg < 5 OR arg IS UNKNOWN) OR (arg = 5 OR arg IS UNKNOWN) )",
                expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", STRICT_NOT_CONTAINS, lop(vop("x")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("arg CONTAINS x OR arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", STRICT_NOT_CONTAINS_ANY_OF, lop(vop("x"), vop("y"), vop("z")), null)
                .withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(arg CONTAINS x OR arg IS UNKNOWN) OR (arg CONTAINS y OR arg IS UNKNOWN) OR (arg CONTAINS z OR arg IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

    }

    @Test
    void testResolveHigherLanguageFeaturesNegatedIntraNegativeMatchRef() {

        PlNegationExpression expr = not(new PlMatchExpression("arg", NOT_EQUALS, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(arg = @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN) AND STRICT NOT arg IS UNKNOWN AND STRICT NOT arg2 IS UNKNOWN",
                expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", NOT_ANY_OF, lop(vop("1"), vop("2"), rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals(
                "( (arg = 1 OR arg IS UNKNOWN) AND STRICT NOT arg IS UNKNOWN) OR ( (arg = 2 OR arg IS UNKNOWN) AND STRICT NOT arg IS UNKNOWN) "
                        + "OR ( (arg = @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN) AND STRICT NOT arg IS UNKNOWN AND STRICT NOT arg2 IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

        expr = not(new PlMatchExpression("arg", STRICT_NOT_EQUALS, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        // special case §5 Audlang

        assertEquals("arg = @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = not(
                new PlMatchExpression("arg", STRICT_NOT_ANY_OF, lop(vop("1"), vop("2"), rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(arg = 1 OR arg IS UNKNOWN) OR (arg = 2 OR arg IS UNKNOWN) OR (arg = @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

    }

    @Test
    void testResolveHigherLanguageFeaturesStrictNegatedIntraNegativeMatch() {

        PlNegationExpression expr = strictNot(new PlMatchExpression("arg", IS_NOT_UNKNOWN, null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        // special case §5.2 Audlang
        assertEquals("<NONE>", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", NOT_EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("arg = 1 AND STRICT NOT arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", NOT_ANY_OF, lop(vop("1"), vop("2"), vop("3")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(arg = 1 AND STRICT NOT arg IS UNKNOWN) OR (arg = 2 AND STRICT NOT arg IS UNKNOWN) OR (arg = 3 AND STRICT NOT arg IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", NOT_BETWEEN, lop(vop("1"), vop("5")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(arg > 1 OR arg = 1) AND (arg < 5 OR arg = 5) AND STRICT NOT arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", NOT_CONTAINS, lop(vop("x")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("arg CONTAINS x AND STRICT NOT arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(
                new PlMatchExpression("arg", NOT_CONTAINS_ANY_OF, lop(vop("x"), vop("y"), vop("z")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals(
                "(arg CONTAINS x AND STRICT NOT arg IS UNKNOWN) OR (arg CONTAINS y AND STRICT NOT arg IS UNKNOWN) OR (arg CONTAINS z AND STRICT NOT arg IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", STRICT_NOT_EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("arg = 1", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(
                new PlMatchExpression("arg", STRICT_NOT_ANY_OF, lop(vop("1"), vop("2"), vop("3")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("arg = 1 OR arg = 2 OR arg = 3", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", STRICT_NOT_BETWEEN, lop(vop("1"), vop("5")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("(arg > 1 OR arg = 1) AND (arg < 5 OR arg = 5)", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", STRICT_NOT_CONTAINS, lop(vop("x")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("arg CONTAINS x", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", STRICT_NOT_CONTAINS_ANY_OF, lop(vop("x"), vop("y"), vop("z")), null)
                .withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("arg CONTAINS x OR arg CONTAINS y OR arg CONTAINS z", expr.resolveHigherLanguageFeatures().toString());

    }

    @Test
    void testResolveHigherLanguageFeaturesStrictNegatedIntraNegativeMatchRef() {

        PlNegationExpression expr = strictNot(
                new PlMatchExpression("arg", NOT_EQUALS, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals("arg = @arg2 AND STRICT NOT arg IS UNKNOWN AND STRICT NOT arg2 IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = strictNot(new PlMatchExpression("arg", NOT_ANY_OF, lop(vop("1"), vop("2"), rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR));

        assertEquals(
                "(arg = 1 AND STRICT NOT arg IS UNKNOWN) OR (arg = 2 AND STRICT NOT arg IS UNKNOWN) OR (arg = @arg2 AND STRICT NOT arg IS UNKNOWN AND STRICT NOT arg2 IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

    }

    @Test
    void testEqualsHashCode() {

        PlNegationExpression expr = not(ALL);
        PlNegationExpression expr2 = not(ALL);

        assertNotSame(expr, expr2);

        assertEquals(expr, expr2);

        assertEquals(expr.hashCode(), expr2.hashCode());

    }

    @Test
    void testCommentHandling() {
        PlNegationExpression expr = new PlNegationExpression(or(match("c", EQUALS, "3"), match("d", EQUALS, "4")), false, null);

        PlNegationExpression expr2 = new PlNegationExpression(
                or(match("c", EQUALS, "3"), match("d", EQUALS, vop("4").withComments(COMMENT_AFTER_OPERAND)).withComments(COMMENT_BEFORE_EXPR)), false, null)
                        .withComments(COMMENT_AFTER_EXPR);

        assertNotEquals(expr, expr2);
        assertEquals(expr, expr.stripComments());

        assertEquals(expr, expr2.stripComments());

        assertSame(expr, expr.withComments(null));
        assertSame(expr, expr.withComments(Collections.emptyList()));

        PlNegationExpression expr3 = expr.withComments(SHORT_COMMENT_BEFORE_EXPR);

        assertSame(expr3, expr3.withComments(SHORT_COMMENT_BEFORE_EXPR));

        PlNegationExpression expr4 = expr.withComments(SHORT_COMMENT_AFTER_EXPR);

        assertSame(expr4, expr4.withComments(SHORT_COMMENT_AFTER_EXPR));

        PlNegationExpression expr5 = new PlNegationExpression(or(match("c", EQUALS, "3"), match("d", EQUALS, "4")).withComments(comments(COMMENT_BEFORE_EXPR)),
                false, comments(COMMENT_BEFORE_EXPR, COMMENT_AFTER_EXPR));

        assertEquals(comments(COMMENT_BEFORE_EXPR), expr5.comments());
        assertEquals(comments(COMMENT_BEFORE_EXPR, COMMENT_AFTER_EXPR), ((PlCombinedExpression) expr5.delegate()).comments());

        assertEquals(expr, expr3.withComments(null));

    }

    @Test
    void testGetAllFieldsAndComments() {

        // @formatter:off
        PlCurbExpression inner = new PlCurbExpression(
                or(
                        match("a", EQUALS, rop("x").withComments(TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND)), 
                        match("d", EQUALS, "4")), 
                PlCurbOperator.GREATER_THAN_OR_EQUALS, 1, null);
        
        PlNegationExpression expr = not(new PlCurbExpression(
                or(
                        match("a", EQUALS, vop("1").withComments(COMMENT_BEFORE_OPERAND)).withComments(COMMENT_BEFORE_EXPR),
                        match("b", EQUALS, "2").withComments(COMMENT_AFTER_EXPR), 
                        inner.withComments(COMMENT_BEFORE_EXPR)
                    ), PlCurbOperator.GREATER_THAN, 1, null)).withComments(TWO_COMMENTS_AFTER_EXPR);
        
        
        // @formatter:on
        List<String> expectedFieldNames = new ArrayList<>(Arrays.asList("a", "x", "d", "b"));
        Collections.sort(expectedFieldNames);
        assertEquals(expectedFieldNames, expr.allArgNames());

        List<PlExpression<?>> collectedExpressions = new ArrayList<>();
        expr.collectExpressions(e -> e instanceof PlMatchExpression m && m.argName().equals("a"), collectedExpressions);
        assertEquals(2, collectedExpressions.size());

        AudlangField field = expr.allFields().stream().filter(f -> f.argName().equals("a")).toList().get(0);
        assertEquals(Arrays.asList("1"), field.values());
        assertEquals(Arrays.asList("x"), field.refArgNames());

        // @formatter:off
        List<String> expectedComments = new ArrayList<>(Arrays.asList(
                TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND.get(0).comment(), 
                TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND.get(1).comment(),
                TWO_COMMENTS_AFTER_EXPR.get(0).comment(), 
                TWO_COMMENTS_AFTER_EXPR.get(1).comment(),
                COMMENT_BEFORE_OPERAND.get(0).comment(), 
                COMMENT_BEFORE_EXPR.get(0).comment(), 
                COMMENT_BEFORE_EXPR.get(0).comment(), 
                COMMENT_AFTER_EXPR.get(0).comment()
            ));
        // @formatter:on

        Collections.sort(expectedComments);

        List<String> comments = new ArrayList<>(expr.allComments().stream().map(PlComment::comment).toList());
        Collections.sort(comments);

        assertEquals(expectedComments, comments);

    }

    @Test
    void testJson() {

        // @formatter:off
        PlCurbExpression inner = new PlCurbExpression(
                or(
                        match("a", EQUALS, rop("x").withComments(TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND)), 
                        match("d", EQUALS, "4")), 
                PlCurbOperator.GREATER_THAN_OR_EQUALS, 1, null);
        
        PlNegationExpression expr = not(new PlCurbExpression(
                or(
                        match("a", EQUALS, vop("1").withComments(COMMENT_BEFORE_OPERAND)).withComments(COMMENT_BEFORE_EXPR),
                        match("b", EQUALS, "2").withComments(COMMENT_AFTER_EXPR), 
                        inner.withComments(COMMENT_BEFORE_EXPR)
                    ), PlCurbOperator.GREATER_THAN, 1, null)).withComments(TWO_COMMENTS_AFTER_EXPR);
        
        
        // @formatter:on

        String json = JsonUtils.writeAsJsonString(expr, true);

        PlExpression<?> res = JsonUtils.readFromJsonString(json, PlExpression.class);

        assertEquals(expr, res);
    }

}
