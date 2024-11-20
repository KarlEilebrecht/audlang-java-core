//@formatter:off
/*
 * PlMatchExpressionTest
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
import static de.calamanari.adl.erl.PlComment.Position.AFTER_OPERAND;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_OPERAND;
import static de.calamanari.adl.erl.PlComment.Position.C1;
import static de.calamanari.adl.erl.PlComment.Position.C2;
import static de.calamanari.adl.erl.PlComment.Position.C3;
import static de.calamanari.adl.erl.PlMatchOperator.ANY_OF;
import static de.calamanari.adl.erl.PlMatchOperator.BETWEEN;
import static de.calamanari.adl.erl.PlMatchOperator.CONTAINS;
import static de.calamanari.adl.erl.PlMatchOperator.CONTAINS_ANY_OF;
import static de.calamanari.adl.erl.PlMatchOperator.EQUALS;
import static de.calamanari.adl.erl.PlMatchOperator.GREATER_THAN;
import static de.calamanari.adl.erl.PlMatchOperator.GREATER_THAN_OR_EQUALS;
import static de.calamanari.adl.erl.PlMatchOperator.IS_NOT_UNKNOWN;
import static de.calamanari.adl.erl.PlMatchOperator.IS_UNKNOWN;
import static de.calamanari.adl.erl.PlMatchOperator.LESS_THAN;
import static de.calamanari.adl.erl.PlMatchOperator.LESS_THAN_OR_EQUALS;
import static de.calamanari.adl.erl.PlMatchOperator.NOT_ANY_OF;
import static de.calamanari.adl.erl.PlMatchOperator.NOT_BETWEEN;
import static de.calamanari.adl.erl.PlMatchOperator.NOT_CONTAINS;
import static de.calamanari.adl.erl.PlMatchOperator.NOT_CONTAINS_ANY_OF;
import static de.calamanari.adl.erl.PlMatchOperator.NOT_EQUALS;
import static de.calamanari.adl.erl.PlMatchOperator.STRICT_NOT_ANY_OF;
import static de.calamanari.adl.erl.PlMatchOperator.STRICT_NOT_BETWEEN;
import static de.calamanari.adl.erl.PlMatchOperator.STRICT_NOT_CONTAINS;
import static de.calamanari.adl.erl.PlMatchOperator.STRICT_NOT_CONTAINS_ANY_OF;
import static de.calamanari.adl.erl.PlMatchOperator.STRICT_NOT_EQUALS;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_BEFORE_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.SHORT_COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.SHORT_COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C1;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C2;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C3;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C4;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C5;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C6;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_BEFORE_AND_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION;
import static de.calamanari.adl.erl.SamplePlExpressions.comments;
import static de.calamanari.adl.erl.SamplePlExpressions.lop;
import static de.calamanari.adl.erl.SamplePlExpressions.match;
import static de.calamanari.adl.erl.SamplePlExpressions.rop;
import static de.calamanari.adl.erl.SamplePlExpressions.vop;
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

import de.calamanari.adl.AudlangField;
import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.erl.PlComment.Position;
import de.calamanari.adl.util.JsonUtils;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class PlMatchExpressionTest {

    private static final List<PlMatchOperator> SIMPLE_MATCH_OPERATORS = Arrays.asList(EQUALS, NOT_EQUALS, LESS_THAN, GREATER_THAN, LESS_THAN_OR_EQUALS,
            GREATER_THAN_OR_EQUALS);

    private static final PlOperand SOME_VALUE_OPERAND = new PlOperand("fooBar", false, null);

    @Test
    void testIsUnknown() {

        PlMatchExpression expr = new PlMatchExpression("arg", IS_UNKNOWN, null);

        assertEquals("arg IS UNKNOWN", expr.format(INLINE));
        assertEquals("arg IS UNKNOWN", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg IS UNKNOWN", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg IS UNKNOWN /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals("arg IS UNKNOWN /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ IS UNKNOWN", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 new line */ IS /* comment C2 */ UNKNOWN",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE));

        assertEquals("/* comment BE */ arg IS UNKNOWN", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg IS UNKNOWN /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals("arg IS UNKNOWN\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nIS /* comment C2 */ UNKNOWN",
                expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2)).format(PRETTY_PRINT));

    }

    @Test
    void testIsNotUnknown() {

        PlMatchExpression expr = new PlMatchExpression("arg", IS_NOT_UNKNOWN, null);

        assertEquals("arg IS NOT UNKNOWN", expr.format(INLINE));
        assertEquals("arg IS NOT UNKNOWN", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg IS NOT UNKNOWN", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg IS NOT UNKNOWN /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals("arg IS NOT UNKNOWN /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ IS NOT UNKNOWN", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */ IS /* comment C2 */ NOT UNKNOWN",
                expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2)).format(INLINE));

        assertEquals("arg /* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */ IS /* comment C2 */ NOT /* comment C3 */ UNKNOWN",
                expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3))
                        .format(INLINE));

        assertEquals("/* comment BE */ arg IS NOT UNKNOWN", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg IS NOT UNKNOWN /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals(
                "arg IS NOT UNKNOWN\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nIS /* comment C2 */ NOT /* comment C3 */ UNKNOWN",
                expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3))
                        .format(PRETTY_PRINT));

    }

    @Test
    void testSingleOperandMatching() {

        for (PlMatchOperator operator : SIMPLE_MATCH_OPERATORS) {
            assertSingleOperandMatching(operator, new PlOperand("value", false, null));
            assertSingleOperandMatching(operator, new PlOperand("ref", true, null));
        }

    }

    private static void assertSingleOperandMatching(PlMatchOperator operator, PlOperand operand) {

        PlMatchExpression expr = new PlMatchExpression("argName", operator, operand, null);

        String simpleExpectation = simpex("", "argName", " ", operator, " ", operand, "");
        assertEquals(simpleExpectation, expr.format(INLINE));
        assertEquals(simpleExpectation, expr.format(PRETTY_PRINT));

        simpleExpectation = simpex("/* comment BE */ ", "argName", " ", operator, " ", operand, "");
        assertEquals(simpleExpectation, expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals(simpleExpectation, expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));

        simpleExpectation = simpex("", "argName", " ", operator, " ", operand, " /* comment AE */");
        assertEquals(simpleExpectation, expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals(simpleExpectation, expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));

        simpleExpectation = simpex("", "argName", " /* comment C1 */ ", operator, " ", operand, "");
        assertEquals("" + simpleExpectation, expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("" + simpleExpectation, expr.withComments(SINGLE_COMMENT_C1).format(PRETTY_PRINT));

        simpleExpectation = simpex("", "argName", " ", operator,
                " /* LONG VERY LONG VERY LONG VERY LONG VERY LONG LONG VERY LONG VERY LONG VERY LONG VERY LONG */ ", operand, "");
        assertEquals("" + simpleExpectation,
                new PlMatchExpression("argName", operator,
                        operand.withComments(
                                comments(BEFORE_OPERAND, "/* LONG VERY LONG VERY LONG VERY LONG VERY LONG LONG VERY LONG VERY LONG VERY LONG VERY LONG */")),
                        null).format(INLINE));

        simpleExpectation = simpex("", "argName", " ", operator,
                "\n    /* LONG VERY LONG VERY LONG VERY LONG VERY LONG\n       LONG VERY LONG VERY LONG VERY LONG VERY LONG */\n    ", operand, "");
        assertEquals("" + simpleExpectation,
                new PlMatchExpression("argName", operator,
                        operand.withComments(
                                comments(BEFORE_OPERAND, "/* LONG VERY LONG VERY LONG VERY LONG VERY LONG LONG VERY LONG VERY LONG VERY LONG VERY LONG */")),
                        null).format(PRETTY_PRINT));

        simpleExpectation = simpex("", "argName", " ", operator, " ", operand, " /* new line */");
        assertEquals("" + simpleExpectation,
                new PlMatchExpression("argName", operator, operand.withComments(comments(AFTER_OPERAND, "/*\nnew line\n*/")), null).format(INLINE));

        simpleExpectation = simpex("", "argName", " ", operator, " ", operand,
                "\n/* LONG VERY LONG VERY LONG VERY LONG VERY LONG\n   LONG VERY LONG VERY LONG VERY LONG VERY LONG */");
        assertEquals("" + simpleExpectation,
                new PlMatchExpression("argName", operator,
                        operand.withComments(
                                comments(AFTER_OPERAND, "/* LONG VERY LONG VERY LONG VERY LONG VERY LONG LONG VERY LONG VERY LONG VERY LONG VERY LONG */")),
                        null).format(PRETTY_PRINT));

    }

    private static String simpex(String before, String argName, String c1, PlMatchOperator operator, String beforeOperand, PlOperand operand, String after) {
        return String.format("%s%s%s%s%s%s%s%s", before, argName, c1, operator.getOperatorTokens().get(1), beforeOperand, (operand.isReference() ? "@" : ""),
                operand.value(), after);
    }

    @Test
    void testStrictNotEquals() {

        PlMatchExpression expr = new PlMatchExpression("arg", STRICT_NOT_EQUALS, new PlOperand("value", false, null), null);

        assertEquals("STRICT arg != value", expr.format(INLINE));
        assertEquals("STRICT arg != value", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ STRICT arg != value", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("STRICT arg != value /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals("STRICT arg != value /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("STRICT /* comment C1 */ arg != value", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("STRICT /* comment C1 new line */ arg /* comment C2 */ != value",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE));

        assertEquals("/* comment BE */ STRICT arg != value", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("STRICT arg != value /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals(
                "STRICT arg != value\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("STRICT /* comment C1 */ arg\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\n!= value",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"))).format(PRETTY_PRINT));

    }

    @Test
    void testBetween() {

        List<PlOperand> btwOperand = Arrays.asList(new PlOperand("low", false, null), new PlOperand("high", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", BETWEEN, btwOperand, null);

        assertEquals("arg BETWEEN (low, high)", expr.format(INLINE));
        assertEquals("arg BETWEEN (low, high)", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg BETWEEN (low, high)", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg BETWEEN (low, high) /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals(
                "arg BETWEEN (low, high) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ BETWEEN (low, high)", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 new line */ BETWEEN /* comment C2 */ (low, high)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE));

        assertEquals("/* comment BE */ arg BETWEEN (low, high)", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg BETWEEN (low, high) /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals(
                "arg BETWEEN (low, high)\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ BETWEEN\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\n(low, high)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"))).format(PRETTY_PRINT));

        List<PlOperand> btwOperandWithComments = Arrays.asList(
                btwOperand.get(0).withComments(comments(BEFORE_OPERAND, "/* comment: VERY LONG VERY LONG VERY LONG VERY LONG before low */")), btwOperand.get(1)
                        .withComments(comments(comments(BEFORE_OPERAND, "/* comment before high */"), comments(AFTER_OPERAND, "/* comment after high */"))));

        expr = new PlMatchExpression("arg", BETWEEN, btwOperandWithComments, null);

        assertEquals(
                "arg BETWEEN ( /* comment: VERY LONG VERY LONG VERY LONG VERY LONG before low */ low, /* comment before high */ high /* comment after high */ )",
                expr.format(INLINE));
        assertEquals("""
                arg BETWEEN (
                        /* comment: VERY LONG VERY LONG VERY LONG VERY
                           LONG before low */
                        low,
                        /* comment before high */
                        high
                        /* comment after high */
                    )""", expr.format(PRETTY_PRINT));

    }

    @Test
    void testNotBetween() {

        List<PlOperand> btwOperand = Arrays.asList(new PlOperand("low", false, null), new PlOperand("high", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", NOT_BETWEEN, btwOperand, null);

        assertEquals("arg NOT BETWEEN (low, high)", expr.format(INLINE));
        assertEquals("arg NOT BETWEEN (low, high)", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg NOT BETWEEN (low, high)", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg NOT BETWEEN (low, high) /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals(
                "arg NOT BETWEEN (low, high) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ NOT BETWEEN (low, high)", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */ NOT /* comment C2 */ BETWEEN (low, high)",
                expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2)).format(INLINE));
        assertEquals("arg /* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */ NOT /* comment C2 */ BETWEEN /* comment C3 new line */ (low, high)",
                expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2,
                        comments(C3, "/* comment C3\nnew line */"))).format(INLINE));

        assertEquals("/* comment BE */ arg NOT BETWEEN (low, high)", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg NOT BETWEEN (low, high) /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals(
                "arg NOT BETWEEN (low, high)\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ NOT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nBETWEEN (low, high)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"))).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ NOT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nBETWEEN /* comment C3 */ (low, high)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3))
                        .format(PRETTY_PRINT));

    }

    @Test
    void testStrictNotBetween() {

        List<PlOperand> btwOperand = Arrays.asList(new PlOperand("low", false, null), new PlOperand("high", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", STRICT_NOT_BETWEEN, btwOperand, null);

        assertEquals("arg STRICT NOT BETWEEN (low, high)", expr.format(INLINE));
        assertEquals("arg STRICT NOT BETWEEN (low, high)", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg STRICT NOT BETWEEN (low, high)", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg STRICT NOT BETWEEN (low, high) /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals(
                "arg STRICT NOT BETWEEN (low, high) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ STRICT NOT BETWEEN (low, high)", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT BETWEEN (low, high)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 new line */ BETWEEN (low, high)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, comments(C3, "/* comment C3\nnew line */")))
                        .format(INLINE));
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 new line */ BETWEEN /* comment C4 */ (low, high)",
                expr.withComments(
                        comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, comments(C3, "/* comment C3\nnew line */"), SINGLE_COMMENT_C4))
                        .format(INLINE));

        assertEquals("/* comment BE */ arg STRICT NOT BETWEEN (low, high)", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg STRICT NOT BETWEEN (low, high) /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals(
                "arg STRICT NOT BETWEEN (low, high)\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG */\nNOT BETWEEN (low, high)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")))
                        .format(PRETTY_PRINT));
        assertEquals("""
                arg /* comment C1 */ STRICT
                /* comment C2 VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG */
                NOT /* comment C3 */ BETWEEN /* comment C4 */ (low, high)""",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"),
                        SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(PRETTY_PRINT));

    }

    @Test
    void testContains() {

        PlMatchExpression expr = new PlMatchExpression("arg", CONTAINS, new PlOperand("value", false, null), null);

        assertEquals("arg CONTAINS value", expr.format(INLINE));
        assertEquals("arg CONTAINS value", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg CONTAINS value", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg CONTAINS value /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals("arg CONTAINS value /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ CONTAINS value", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */ CONTAINS value",
                expr.withComments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */")).format(INLINE));

        assertEquals("/* comment BE */ arg CONTAINS value", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg CONTAINS value /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals(
                "arg CONTAINS value\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nCONTAINS value",
                expr.withComments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */")).format(PRETTY_PRINT));

        PlOperand operand = new PlOperand("value", false,
                comments(comments(BEFORE_OPERAND, "/* comment before the value VERY LONG VERY LONG VERY LONG VERY LONG */"),
                        comments(AFTER_OPERAND, "/* comment after the value VERY LONG VERY LONG VERY LONG VERY LONG */")));

        expr = new PlMatchExpression("arg", CONTAINS, operand, null);
        assertEquals(
                "arg CONTAINS /* comment before the value VERY LONG VERY LONG VERY LONG VERY LONG */ value /* comment after the value VERY LONG VERY LONG VERY LONG VERY LONG */",
                expr.format(INLINE));

        // It is no mistake that the comment after the value is less indented because
        // comments after the operand are indistinguishable from comments after the expression!
        assertEquals("""
                arg CONTAINS
                    /* comment before the value VERY LONG VERY LONG
                       VERY LONG VERY LONG */
                    value
                /* comment after the value VERY LONG VERY LONG
                   VERY LONG VERY LONG */""", expr.format(PRETTY_PRINT));

    }

    @Test
    void testNotContains() {

        PlMatchExpression expr = new PlMatchExpression("arg", NOT_CONTAINS, new PlOperand("value", false, null), null);

        assertEquals("arg NOT CONTAINS value", expr.format(INLINE));
        assertEquals("arg NOT CONTAINS value", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg NOT CONTAINS value", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg NOT CONTAINS value /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals(
                "arg NOT CONTAINS value /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ NOT CONTAINS value", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 new line */ NOT CONTAINS value", expr.withComments(comments(C1, "/* comment C1\nnew line */")).format(INLINE));

        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ CONTAINS value",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE));

        assertEquals("/* comment BE */ arg NOT CONTAINS value", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg NOT CONTAINS value /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals(
                "arg NOT CONTAINS value\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT CONTAINS value",
                expr.withComments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */")).format(PRETTY_PRINT));

        assertEquals("arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT /* comment C2 */ CONTAINS value",
                expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2)).format(PRETTY_PRINT));

    }

    @Test
    void testStrictNotContains() {

        PlMatchExpression expr = new PlMatchExpression("arg", STRICT_NOT_CONTAINS, new PlOperand("value", false, null), null);

        assertEquals("arg STRICT NOT CONTAINS value", expr.format(INLINE));
        assertEquals("arg STRICT NOT CONTAINS value", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg STRICT NOT CONTAINS value", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg STRICT NOT CONTAINS value /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals(
                "arg STRICT NOT CONTAINS value /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ STRICT NOT CONTAINS value", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 new line */ STRICT NOT CONTAINS value", expr.withComments(comments(C1, "/* comment C1\nnew line */")).format(INLINE));

        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT CONTAINS value",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE));

        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ CONTAINS value",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE));

        assertEquals("/* comment BE */ arg STRICT NOT CONTAINS value", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg STRICT NOT CONTAINS value /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals(
                "arg STRICT NOT CONTAINS value\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nSTRICT NOT CONTAINS value",
                expr.withComments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")).format(PRETTY_PRINT));

        assertEquals(
                "arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nSTRICT /* comment C2 */ NOT /* comment C3 */ CONTAINS value",
                expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2,
                        SINGLE_COMMENT_C3)).format(PRETTY_PRINT));

    }

    @Test
    void testAnyOf() {

        List<PlOperand> operandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("ref1", true, null));

        PlMatchExpression expr = new PlMatchExpression("arg", ANY_OF, operandList, null);

        assertEquals("arg ANY OF (v1, v2, @ref1)", expr.format(INLINE));
        assertEquals("arg ANY OF (v1, v2, @ref1)", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg ANY OF (v1, v2, @ref1)", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg ANY OF (v1, v2, @ref1) /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals(
                "arg ANY OF (v1, v2, @ref1) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ ANY OF (v1, v2, @ref1)", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 new line */ ANY /* comment C2 */ OF (v1, v2, @ref1)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ ANY /* comment C2 */ OF /* comment C3 */ (v1, v2, @ref1)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE));

        assertEquals("/* comment BE */ arg ANY OF (v1, v2, @ref1)", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg ANY OF (v1, v2, @ref1) /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals("""
                arg ANY OF (v1, v2, @ref1)
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""", expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ ANY\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nOF (v1, v2, @ref1)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")))
                        .format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ ANY\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nOF /* comment C3 */ (v1, v2, @ref1)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"),
                        SINGLE_COMMENT_C3)).format(PRETTY_PRINT));

        List<PlOperand> operandListWithComments = Arrays.asList(
                operandList.get(0).withComments(comments(BEFORE_OPERAND, "/* comment: VERY LONG VERY LONG VERY LONG VERY LONG before v1 */")),
                operandList.get(2)
                        .withComments(comments(comments(BEFORE_OPERAND, "/* comment before ref1 */"), comments(AFTER_OPERAND, "/* comment after ref1 */"))));

        expr = new PlMatchExpression("arg", ANY_OF, operandListWithComments, null);

        assertEquals(
                "arg ANY OF ( /* comment: VERY LONG VERY LONG VERY LONG VERY LONG before v1 */ v1, /* comment before ref1 */ @ref1 /* comment after ref1 */ )",
                expr.format(INLINE));
        assertEquals("""
                arg ANY OF (
                        /* comment: VERY LONG VERY LONG VERY LONG VERY
                           LONG before v1 */
                        v1,
                        /* comment before ref1 */
                        @ref1
                        /* comment after ref1 */
                    )""", expr.format(PRETTY_PRINT));

        List<PlOperand> longOperandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("ref1", true, null),
                new PlOperand("v3", false, null), new PlOperand("v4", false, null), new PlOperand("v5", false, null));

        expr = new PlMatchExpression("arg", ANY_OF, longOperandList, null);

        assertEquals("arg ANY OF (v1, v2, @ref1, v3, v4, v5)", expr.format(INLINE));
        assertEquals("""
                arg ANY OF (
                        v1,
                        v2,
                        @ref1,
                        v3,
                        v4,
                        v5
                    )""", expr.format(PRETTY_PRINT));

    }

    @Test
    void testNotAnyOf() {
        List<PlOperand> operandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("ref1", true, null));

        PlMatchExpression expr = new PlMatchExpression("arg", NOT_ANY_OF, operandList, null);

        assertEquals("arg NOT ANY OF (v1, v2, @ref1)", expr.format(INLINE));
        assertEquals("arg NOT ANY OF (v1, v2, @ref1)", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg NOT ANY OF (v1, v2, @ref1)", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg NOT ANY OF (v1, v2, @ref1) /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals(
                """
                        arg NOT ANY OF (v1, v2, @ref1) /* comment 1 "    " VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */""",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ NOT ANY OF (v1, v2, @ref1)", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ ANY OF (v1, v2, @ref1)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ ANY /* comment C3 */ OF (v1, v2, @ref1)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ ANY /* comment C3 */ OF /* comment C4 */ (v1, v2, @ref1)", expr
                .withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(INLINE));

        assertEquals("/* comment before expression */\narg NOT ANY OF (v1, v2, @ref1)", expr.withComments(COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg NOT ANY OF (v1, v2, @ref1)\n/* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals("""
                arg NOT ANY OF (v1, v2, @ref1)
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""", expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("""
                arg /* comment C1 */ NOT
                /* comment C2 VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG VERY LONG */
                ANY OF (v1, v2, @ref1)""",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")))
                        .format(PRETTY_PRINT));
        assertEquals("""
                arg /* comment C1 */ NOT
                /* comment C2 VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG VERY LONG VERY LONG */
                ANY /* comment C3 */ OF /* comment C4 */ (v1, v2, @ref1)""", expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2,
                "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"),
                SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(PRETTY_PRINT));

    }

    @Test
    void testStrictNotAnyOf() {
        List<PlOperand> operandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("ref1", true, null));

        PlMatchExpression expr = new PlMatchExpression("arg", STRICT_NOT_ANY_OF, operandList, null);

        assertEquals("arg STRICT NOT ANY OF (v1, v2, @ref1)", expr.format(INLINE));
        assertEquals("arg STRICT NOT ANY OF (v1, v2, @ref1)", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg STRICT NOT ANY OF (v1, v2, @ref1)", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg STRICT NOT ANY OF (v1, v2, @ref1) /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals(
                "arg STRICT NOT ANY OF (v1, v2, @ref1) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ STRICT NOT ANY OF (v1, v2, @ref1)", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT ANY OF (v1, v2, @ref1)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ ANY OF (v1, v2, @ref1)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ ANY /* comment C4 */ OF (v1, v2, @ref1)", expr
                .withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ ANY /* comment C4 */ OF /* comment C5 */ (v1, v2, @ref1)",
                expr.withComments(
                        comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4, SINGLE_COMMENT_C5))
                        .format(INLINE));

        assertEquals("/* comment BE */ arg STRICT NOT ANY OF (v1, v2, @ref1)", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg STRICT NOT ANY OF (v1, v2, @ref1) /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals("""
                arg STRICT NOT ANY OF (v1, v2, @ref1)
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""", expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT ANY OF (v1, v2, @ref1)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"))).format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT /* comment C3 */ ANY /* comment C4 */ OF /* comment C5 */ (v1, v2, @ref1)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3,
                        SINGLE_COMMENT_C4, SINGLE_COMMENT_C5)).format(PRETTY_PRINT));

    }

    @Test
    void testContainsAnyOf() {

        List<PlOperand> operandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("v3", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", CONTAINS_ANY_OF, operandList, null);

        assertEquals("arg CONTAINS ANY OF (v1, v2, v3)", expr.format(INLINE));
        assertEquals("arg CONTAINS ANY OF (v1, v2, v3)", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg CONTAINS ANY OF (v1, v2, v3)", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg CONTAINS ANY OF (v1, v2, v3) /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals(
                "arg CONTAINS ANY OF (v1, v2, v3) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ CONTAINS ANY OF (v1, v2, v3)", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 new line */ CONTAINS /* comment C2 */ ANY OF (v1, v2, v3)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ CONTAINS /* comment C2 */ ANY /* comment C3 */ OF (v1, v2, v3)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ CONTAINS /* comment C2 */ ANY /* comment C3 */ OF /* comment C4 */ (v1, v2, v3)", expr
                .withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(INLINE));

        assertEquals("/* comment BE */ arg CONTAINS ANY OF (v1, v2, v3)", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg CONTAINS ANY OF (v1, v2, v3) /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals(
                "arg CONTAINS ANY OF (v1, v2, v3)\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ CONTAINS\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nANY OF (v1, v2, v3)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"))).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ CONTAINS\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nANY /* comment C3 */ OF (v1, v2, v3)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3))
                        .format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ CONTAINS\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nANY /* comment C3 */ OF /* comment C4 */ (v1, v2, v3)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3,
                        SINGLE_COMMENT_C4)).format(PRETTY_PRINT));

        List<PlOperand> operandListWithComments = Arrays.asList(
                operandList.get(0).withComments(comments(BEFORE_OPERAND, "/* comment: VERY LONG VERY LONG VERY LONG VERY LONG before v1 */")),
                operandList.get(2).withComments(comments(comments(BEFORE_OPERAND, "/* before v3 */"), comments(AFTER_OPERAND, "/* after v3 */"))));

        expr = new PlMatchExpression("arg", CONTAINS_ANY_OF, operandListWithComments, null);

        assertEquals("arg CONTAINS ANY OF ( /* comment: VERY LONG VERY LONG VERY LONG VERY LONG before v1 */ v1, /* before v3 */ v3 /* after v3 */ )",
                expr.format(INLINE));
        assertEquals("""
                arg CONTAINS ANY OF (
                        /* comment: VERY LONG VERY LONG VERY LONG VERY
                           LONG before v1 */
                        v1,
                        /* before v3 */ v3 /* after v3 */
                    )""", expr.format(PRETTY_PRINT));

        List<PlOperand> longOperandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("v3", false, null),
                new PlOperand("v4", false, null), new PlOperand("v5", false, null), new PlOperand("v6", false, null));

        expr = new PlMatchExpression("arg", CONTAINS_ANY_OF, longOperandList, null);

        assertEquals("arg CONTAINS ANY OF (v1, v2, v3, v4, v5, v6)", expr.format(INLINE));
        assertEquals("""
                arg CONTAINS ANY OF (
                        v1,
                        v2,
                        v3,
                        v4,
                        v5,
                        v6
                    )""", expr.format(PRETTY_PRINT));

    }

    @Test
    void testNotContainsAnyOf() {

        List<PlOperand> operandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("v3", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", NOT_CONTAINS_ANY_OF, operandList, null);

        assertEquals("arg NOT CONTAINS ANY OF (v1, v2, v3)", expr.format(INLINE));
        assertEquals("arg NOT CONTAINS ANY OF (v1, v2, v3)", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg NOT CONTAINS ANY OF (v1, v2, v3)", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg NOT CONTAINS ANY OF (v1, v2, v3) /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals(
                "arg NOT CONTAINS ANY OF (v1, v2, v3) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ NOT CONTAINS ANY OF (v1, v2, v3)", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ CONTAINS ANY OF (v1, v2, v3)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ CONTAINS /* comment C3 */ ANY OF (v1, v2, v3)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ CONTAINS /* comment C3 */ ANY /* comment C4 */ OF (v1, v2, v3)", expr
                .withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ CONTAINS /* comment C3 */ ANY /* comment C4 */ OF /* comment C5 */ (v1, v2, v3)",
                expr.withComments(
                        comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4, SINGLE_COMMENT_C5))
                        .format(INLINE));

        assertEquals("/* comment BE */ arg NOT CONTAINS ANY OF (v1, v2, v3)", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg NOT CONTAINS ANY OF (v1, v2, v3) /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals("""
                arg NOT CONTAINS ANY OF (v1, v2, v3)
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""", expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ NOT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nCONTAINS ANY OF (v1, v2, v3)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")))
                        .format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ NOT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nCONTAINS /* comment C3 */ ANY OF (v1, v2, v3)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"),
                        SINGLE_COMMENT_C3)).format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ NOT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nCONTAINS /* comment C3 */ ANY /* comment C4 */ OF (v1, v2, v3)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"),
                        SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ NOT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nCONTAINS /* comment C3 */ ANY /* comment C4 */ OF /* comment C5 */ (v1, v2, v3)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"),
                        SINGLE_COMMENT_C3, SINGLE_COMMENT_C4, SINGLE_COMMENT_C5)).format(PRETTY_PRINT));

    }

    @Test
    void testStrictNotContainsAnyOf() {

        List<PlOperand> operandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("v3", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", STRICT_NOT_CONTAINS_ANY_OF, operandList, null);

        assertEquals("arg STRICT NOT CONTAINS ANY OF (v1, v2, v3)", expr.format(INLINE));
        assertEquals("arg STRICT NOT CONTAINS ANY OF (v1, v2, v3)", expr.format(PRETTY_PRINT));

        assertEquals("/* comment before expression */ arg STRICT NOT CONTAINS ANY OF (v1, v2, v3)", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("arg STRICT NOT CONTAINS ANY OF (v1, v2, v3) /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals(
                "arg STRICT NOT CONTAINS ANY OF (v1, v2, v3) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));
        assertEquals("arg /* comment C1 */ STRICT NOT CONTAINS ANY OF (v1, v2, v3)", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT CONTAINS ANY OF (v1, v2, v3)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ CONTAINS ANY OF (v1, v2, v3)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ CONTAINS /* comment C4 */ ANY OF (v1, v2, v3)", expr
                .withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(INLINE));
        assertEquals(
                "arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ CONTAINS /* comment C4 */ ANY /* comment C5 */ OF (v1, v2, v3)",
                expr.withComments(
                        comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4, SINGLE_COMMENT_C5))
                        .format(INLINE));
        assertEquals(
                "arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ CONTAINS /* comment C4 */ ANY /* comment C5 */ OF /* comment C6 */ (v1, v2, v3)",
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4,
                        SINGLE_COMMENT_C5, SINGLE_COMMENT_C6)).format(INLINE));

        assertEquals("/* comment BE */ arg STRICT NOT CONTAINS ANY OF (v1, v2, v3)", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("arg STRICT NOT CONTAINS ANY OF (v1, v2, v3) /* comment AE */", expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals("""
                arg STRICT NOT CONTAINS ANY OF (v1, v2, v3)
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""", expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("""
                arg /* comment C1 */ STRICT
                /* comment C2 VERY LONG VERY LONG VERY LONG VERY
                   LONG */
                NOT CONTAINS ANY OF (v1, v2, v3)""",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"))).format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT /* comment C3 */ CONTAINS ANY OF (v1, v2, v3)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3))
                        .format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT /* comment C3 */ CONTAINS /* comment C4 */ ANY OF (v1, v2, v3)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3,
                        SINGLE_COMMENT_C4)).format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT /* comment C3 */ CONTAINS /* comment C4 */ ANY /* comment C5 */ OF (v1, v2, v3)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3,
                        SINGLE_COMMENT_C4, SINGLE_COMMENT_C5)).format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT /* comment C3 */ CONTAINS /* comment C4 */ ANY /* comment C5 */ OF /* comment C6 */ (v1, v2, v3)",
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3,
                        SINGLE_COMMENT_C4, SINGLE_COMMENT_C5, SINGLE_COMMENT_C6)).format(PRETTY_PRINT));
    }

    @Test
    void testSpecialCases() {

        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression(null, IS_UNKNOWN, null));

        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", IS_UNKNOWN, SOME_VALUE_OPERAND, null));

        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", null, SOME_VALUE_OPERAND, null));

        for (PlMatchOperator operator : SIMPLE_MATCH_OPERATORS) {
            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", operator, null));
            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", operator, SOME_VALUE_OPERAND, SINGLE_COMMENT_C2));

        }

        PlMatchExpression expr = new PlMatchExpression("arg", IS_UNKNOWN, null);
        assertSame(expr, expr.stripComments());
        assertSame(expr, expr.withComments(Collections.emptyList()));

        expr = new PlMatchExpression("arg", IS_UNKNOWN, COMMENT_BEFORE_EXPR);

        assertSame(expr, expr.withComments(COMMENT_BEFORE_EXPR));

        final List<PlComment> badComments = comments(SINGLE_COMMENT_C1, SINGLE_COMMENT_C2, SINGLE_COMMENT_C3);
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", IS_UNKNOWN, badComments));

        final List<PlComment> badComments2 = comments(Position.BEFORE_OPERAND, "/* foo */");
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", IS_UNKNOWN, badComments2));

        final List<PlComment> badComments3 = comments(SINGLE_COMMENT_C1, SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4);
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", IS_NOT_UNKNOWN, badComments3));

        assertThrows(AudlangValidationException.class, () -> PlMatchOperator.mapSlotToCommentPosition(0));
        assertThrows(AudlangValidationException.class, () -> PlMatchOperator.mapSlotToCommentPosition(7));

    }

    @Test
    void testSpecialCasesBetween() {
        final PlOperand badBetweenBounds = new PlOperand("value", false, null);

        final List<PlOperand> badBetweenBounds2 = Arrays.asList(new PlOperand("value", false, null));

        final List<PlOperand> badBetweenBounds3 = Arrays.asList(new PlOperand("value", false, null), new PlOperand("value2", false, null),
                new PlOperand("value3", false, null));

        final List<PlOperand> badBetweenBounds4 = Arrays.asList(new PlOperand("value", false, null), new PlOperand("ref", true, null));

        for (PlMatchOperator op : Arrays.asList(BETWEEN, NOT_BETWEEN, STRICT_NOT_BETWEEN)) {

            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", op, badBetweenBounds4, null));
            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", op, badBetweenBounds, null));
            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", op, badBetweenBounds2, null));
            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", op, badBetweenBounds3, null));
            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", op, badBetweenBounds4, null));

        }

        final List<PlOperand> validBetweenBounds = Arrays.asList(new PlOperand("low", false, null), new PlOperand("high", false, null));
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", BETWEEN, validBetweenBounds, SINGLE_COMMENT_C3));
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", NOT_BETWEEN, validBetweenBounds, SINGLE_COMMENT_C4));
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", STRICT_NOT_BETWEEN, validBetweenBounds, SINGLE_COMMENT_C5));

    }

    @Test
    void testSpecialCasesContains() {
        final List<PlOperand> unexpectedListOperand = Arrays.asList(new PlOperand("value", false, null), new PlOperand("value2", false, null));
        final PlOperand unexpectedReferenceOperand = new PlOperand("ref", true, null);

        for (PlMatchOperator op : Arrays.asList(CONTAINS, NOT_CONTAINS, STRICT_NOT_CONTAINS)) {

            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", op, null));
            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", op, unexpectedListOperand, null));
            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", op, unexpectedReferenceOperand, null));

        }

        final PlOperand validValueOperand = new PlOperand("value", false, null);
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", CONTAINS, validValueOperand, SINGLE_COMMENT_C2));
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", NOT_CONTAINS, validValueOperand, SINGLE_COMMENT_C3));
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", STRICT_NOT_CONTAINS, validValueOperand, SINGLE_COMMENT_C4));

    }

    @Test
    void testSpecialCasesAnyOf() {

        List<PlOperand> emptyList = Collections.emptyList();

        for (PlMatchOperator op : Arrays.asList(ANY_OF, NOT_ANY_OF, STRICT_NOT_ANY_OF)) {
            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", op, null));
            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", op, emptyList, null));
        }

        final List<PlOperand> validListOperand = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null));
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", ANY_OF, validListOperand, SINGLE_COMMENT_C4));
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", NOT_ANY_OF, validListOperand, SINGLE_COMMENT_C5));
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", STRICT_NOT_ANY_OF, validListOperand, SINGLE_COMMENT_C6));

    }

    @Test
    void testSpecialCasesContainsAnyOf() {
        final PlOperand unexpectedReferenceOperand = new PlOperand("ref", true, null);
        final List<PlOperand> unexpectedRefInListOperand = Arrays.asList(new PlOperand("ref", true, null));

        for (PlMatchOperator op : Arrays.asList(CONTAINS_ANY_OF, NOT_CONTAINS_ANY_OF, STRICT_NOT_CONTAINS_ANY_OF)) {
            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", op, null));
            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", op, unexpectedReferenceOperand, null));
            assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", op, unexpectedRefInListOperand, null));
        }

        final List<PlOperand> validListOperand = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null));
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", CONTAINS_ANY_OF, validListOperand, SINGLE_COMMENT_C5));
        assertThrows(AudlangValidationException.class, () -> new PlMatchExpression("arg", NOT_CONTAINS_ANY_OF, validListOperand, SINGLE_COMMENT_C6));

    }

    @Test
    void testResolveHigherLanguageFeaturesPositive() {
        PlMatchExpression expr = new PlMatchExpression("arg", IS_UNKNOWN, null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("arg = 1", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", ANY_OF, lop(vop("1"), vop("2"), vop("3")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("arg = 1 OR arg = 2 OR arg = 3", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", GREATER_THAN, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("arg > 1", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", LESS_THAN, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("arg < 1", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", GREATER_THAN_OR_EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("arg > 1 OR arg = 1", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", LESS_THAN_OR_EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("arg < 1 OR arg = 1", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", BETWEEN, lop(vop("1"), vop("5")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("(arg > 1 OR arg = 1) AND (arg < 5 OR arg = 5)", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", CONTAINS, lop(vop("x")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("arg CONTAINS x", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", CONTAINS_ANY_OF, lop(vop("x"), vop("y"), vop("z")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("arg CONTAINS x OR arg CONTAINS y OR arg CONTAINS z", expr.resolveHigherLanguageFeatures().toString());

    }

    @Test
    void testResolveHigherLanguageFeaturesNegative() {
        PlMatchExpression expr = new PlMatchExpression("arg", IS_NOT_UNKNOWN, null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("STRICT NOT arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", NOT_EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("STRICT NOT arg = 1 OR arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", STRICT_NOT_EQUALS, lop(vop("1")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("STRICT NOT arg = 1", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", NOT_ANY_OF, lop(vop("1"), vop("2"), vop("3")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("(STRICT NOT arg = 1 OR arg IS UNKNOWN) AND (STRICT NOT arg = 2 OR arg IS UNKNOWN) AND (STRICT NOT arg = 3 OR arg IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", STRICT_NOT_ANY_OF, lop(vop("1"), vop("2"), vop("3")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("STRICT NOT arg = 1 AND STRICT NOT arg = 2 AND STRICT NOT arg = 3", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", NOT_BETWEEN, lop(vop("1"), vop("5")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("(STRICT NOT arg > 1 AND STRICT NOT arg = 1) OR (STRICT NOT arg < 5 AND STRICT NOT arg = 5) OR arg IS UNKNOWN",
                expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", STRICT_NOT_BETWEEN, lop(vop("1"), vop("5")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("(STRICT NOT arg > 1 AND STRICT NOT arg = 1) OR (STRICT NOT arg < 5 AND STRICT NOT arg = 5)",
                expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", NOT_CONTAINS, lop(vop("x")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("STRICT NOT arg CONTAINS x OR arg IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", STRICT_NOT_CONTAINS, lop(vop("x")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("STRICT NOT arg CONTAINS x", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", NOT_CONTAINS_ANY_OF, lop(vop("x"), vop("y"), vop("z")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals(
                "(STRICT NOT arg CONTAINS x OR arg IS UNKNOWN) AND (STRICT NOT arg CONTAINS y OR arg IS UNKNOWN) AND (STRICT NOT arg CONTAINS z OR arg IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", STRICT_NOT_CONTAINS_ANY_OF, lop(vop("x"), vop("y"), vop("z")), null)
                .withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("STRICT NOT arg CONTAINS x AND STRICT NOT arg CONTAINS y AND STRICT NOT arg CONTAINS z", expr.resolveHigherLanguageFeatures().toString());

    }

    @Test
    void testResolveHigherLanguageFeaturesNegativeRef() {
        PlMatchExpression expr = new PlMatchExpression("arg", NOT_EQUALS, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("STRICT NOT arg = @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", STRICT_NOT_EQUALS, lop(rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("STRICT NOT arg = @arg2", expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", NOT_ANY_OF, lop(vop("1"), vop("2"), rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals(
                "(STRICT NOT arg = 1 OR arg IS UNKNOWN) AND (STRICT NOT arg = 2 OR arg IS UNKNOWN) AND (STRICT NOT arg = @arg2 OR arg IS UNKNOWN OR arg2 IS UNKNOWN)",
                expr.resolveHigherLanguageFeatures().toString());

        expr = new PlMatchExpression("arg", STRICT_NOT_ANY_OF, lop(vop("1"), vop("2"), rop("arg2")), null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertEquals("STRICT NOT arg = 1 AND STRICT NOT arg = 2 AND STRICT NOT arg = @arg2", expr.resolveHigherLanguageFeatures().toString());

    }

    @Test
    void testEqualsHashCode() {

        PlMatchExpression expr = new PlMatchExpression("arg", IS_UNKNOWN, null);
        PlMatchExpression expr2 = new PlMatchExpression("arg", IS_UNKNOWN, null);

        assertNotSame(expr, expr2);

        assertEquals(expr, expr2);

        assertEquals(expr.hashCode(), expr2.hashCode());

    }

    @Test
    void testStripComments() {
        PlMatchExpression expr = match("argName", EQUALS, vop("value"));
        PlMatchExpression expr2 = match("argName", EQUALS, vop("value").withComments(TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND))
                .withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertNotEquals(expr, expr2);

        assertEquals(expr, expr.stripComments());
    }

    @Test
    void testCommentHandling() {
        PlMatchExpression expr = match("argName", EQUALS, vop("value"));

        PlMatchExpression expr2 = match("argName", EQUALS, vop("value"));

        assertEquals(expr, expr2);
        assertEquals(expr, expr.stripComments());

        expr2 = expr2.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);

        assertNotEquals(expr, expr2);

        assertEquals(expr, expr2.stripComments());

        assertSame(expr, expr.withComments(null));
        assertSame(expr, expr.withComments(Collections.emptyList()));

        PlMatchExpression expr3 = expr.withComments(SHORT_COMMENT_BEFORE_EXPR);

        assertSame(expr3, expr3.withComments(SHORT_COMMENT_BEFORE_EXPR));

        PlMatchExpression expr4 = expr.withComments(SHORT_COMMENT_AFTER_EXPR);

        assertSame(expr4, expr4.withComments(SHORT_COMMENT_AFTER_EXPR));

    }

    @Test
    void testGetAllFieldsAndComments() {

        // @formatter:off
        List<PlOperand> longOperandList = Arrays.asList(
                new PlOperand("v1", false, null).withComments(COMMENT_BEFORE_OPERAND), 
                new PlOperand("v2", false, null), 
                new PlOperand("ref1", true, null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND),
                new PlOperand("v3", false, null), 
                new PlOperand("v4", false, null), 
                new PlOperand("v5", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", ANY_OF, longOperandList, null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);
        
        
        // @formatter:on
        List<String> expectedFieldNames = new ArrayList<>(Arrays.asList("arg", "ref1"));
        Collections.sort(expectedFieldNames);
        assertEquals(expectedFieldNames, expr.allArgNames());

        List<PlExpression<?>> collectedExpressions = new ArrayList<>();
        expr.collectExpressions(e -> e instanceof PlMatchExpression m && m.argName().equals("arg"), collectedExpressions);
        assertEquals(1, collectedExpressions.size());

        AudlangField field = expr.allFields().stream().filter(f -> f.argName().equals("arg")).toList().get(0);
        assertEquals(Arrays.asList("v1", "v2", "v3", "v4", "v5"), field.values());
        assertEquals(Arrays.asList("ref1"), field.refArgNames());

        field = expr.allFields().stream().filter(f -> f.argName().equals("ref1")).toList().get(0);
        assertTrue(field.values().isEmpty());
        assertEquals(Arrays.asList("arg"), field.refArgNames());

        // @formatter:off
        List<String> expectedComments = new ArrayList<>(Arrays.asList(
                TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND.get(0).comment(), 
                TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND.get(1).comment(),
                TWO_COMMENTS_BEFORE_AND_AFTER_EXPR.get(0).comment(), 
                TWO_COMMENTS_BEFORE_AND_AFTER_EXPR.get(1).comment(),
                COMMENT_BEFORE_OPERAND.get(0).comment() 
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
        List<PlOperand> longOperandList = Arrays.asList(
                new PlOperand("v1", false, null).withComments(COMMENT_BEFORE_OPERAND), 
                new PlOperand("v2", false, null), 
                new PlOperand("ref1", true, null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND),
                new PlOperand("v3", false, null), 
                new PlOperand("v4", false, null), 
                new PlOperand("v5", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", ANY_OF, longOperandList, null).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR);
        
        
        // @formatter:on

        String json = JsonUtils.writeAsJsonString(expr, true);

        PlExpression<?> res = JsonUtils.readFromJsonString(json, PlExpression.class);

        assertEquals(expr, res);
    }

}
