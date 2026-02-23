//@formatter:off
/*
 * PlExpressionBuilderTest
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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.erl.PlCurbExpression.PlCurbOperator;

import static de.calamanari.adl.FormatStyle.INLINE;
import static de.calamanari.adl.FormatStyle.PRETTY_PRINT;
import static de.calamanari.adl.erl.PlComment.Position.AFTER_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.AFTER_OPERAND;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_EXPRESSION;
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
import static de.calamanari.adl.erl.SamplePlExpressions.ALL;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_AFTER_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_BEFORE_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.EMPTY_COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.MULTI_LINE_COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.MULTI_LINE_COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.NONE;
import static de.calamanari.adl.erl.SamplePlExpressions.SHORT_COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.SHORT_COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C1;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C2;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C3;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C4;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C5;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C6;
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
import static de.calamanari.adl.erl.SamplePlExpressions.strictNot;
import static de.calamanari.adl.erl.SamplePlExpressions.vop;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class PlExpressionBuilderTest {

    static final Logger LOGGER = LoggerFactory.getLogger(PlExpressionBuilderTest.class);

    private static final List<PlMatchOperator> SIMPLE_MATCH_OPERATORS = Arrays.asList(EQUALS, NOT_EQUALS, LESS_THAN, GREATER_THAN, LESS_THAN_OR_EQUALS,
            GREATER_THAN_OR_EQUALS);

    @Test
    void testIsUnknown() {

        PlMatchExpression expr = new PlMatchExpression("arg", IS_UNKNOWN, null);

        assertEquals("arg IS UNKNOWN", parse(expr.format(INLINE)).toString());
        assertEquals("arg IS UNKNOWN", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg IS UNKNOWN", parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg IS UNKNOWN /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals("arg IS UNKNOWN /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ IS UNKNOWN", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ IS /* comment C2 */ UNKNOWN",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE)).toString());

        assertEquals("/* comment BE */ arg IS UNKNOWN", parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("arg IS UNKNOWN /* comment AE */", parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("arg IS UNKNOWN\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nIS /* comment C2 */ UNKNOWN", parse(
                expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2)).format(PRETTY_PRINT))
                        .format(PRETTY_PRINT));

    }

    @Test
    void testIsNotUnknown() {

        PlMatchExpression expr = new PlMatchExpression("arg", IS_NOT_UNKNOWN, null);

        assertEquals("arg IS NOT UNKNOWN", parse(expr.format(INLINE)).toString());
        assertEquals("arg IS NOT UNKNOWN", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg IS NOT UNKNOWN", parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg IS NOT UNKNOWN /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals("arg IS NOT UNKNOWN /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ IS NOT UNKNOWN", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */ IS /* comment C2 */ NOT UNKNOWN",
                parse(expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2)).format(INLINE))
                        .toString());

        assertEquals("arg /* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */ IS /* comment C2 */ NOT /* comment C3 */ UNKNOWN",
                parse(expr
                        .withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3))
                        .format(INLINE)).toString());

        assertEquals("/* comment BE */ arg IS NOT UNKNOWN", parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("arg IS NOT UNKNOWN /* comment AE */", parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals(
                "arg IS NOT UNKNOWN\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nIS /* comment C2 */ NOT /* comment C3 */ UNKNOWN",
                parse(expr
                        .withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3))
                        .format(PRETTY_PRINT)).format(PRETTY_PRINT));

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
        assertEquals(simpleExpectation, parse(expr.format(INLINE)).toString());
        assertEquals(simpleExpectation, parse(expr.format(PRETTY_PRINT)).toString());

        simpleExpectation = simpex("/* comment BE */ ", "argName", " ", operator, " ", operand, "");
        assertEquals(simpleExpectation, parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals(simpleExpectation, parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).toString());

        simpleExpectation = simpex("", "argName", " ", operator, " ", operand, " /* comment AE */");
        assertEquals(simpleExpectation, parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals(simpleExpectation, parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).toString());

        simpleExpectation = simpex("", "argName", " /* comment C1 */ ", operator, " ", operand, "");
        assertEquals("" + simpleExpectation, parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("" + simpleExpectation, parse(expr.withComments(SINGLE_COMMENT_C1).format(PRETTY_PRINT)).toString());

        simpleExpectation = simpex("", "argName", " ", operator,
                " /* LONG VERY LONG VERY LONG VERY LONG VERY LONG LONG VERY LONG VERY LONG VERY LONG VERY LONG */ ", operand, "");
        assertEquals("" + simpleExpectation,
                parse(new PlMatchExpression("argName", operator,
                        operand.withComments(
                                comments(BEFORE_OPERAND, "/* LONG VERY LONG VERY LONG VERY LONG VERY LONG LONG VERY LONG VERY LONG VERY LONG VERY LONG */")),
                        null).format(INLINE)).toString());

        simpleExpectation = simpex("", "argName", " ", operator,
                "\n    /* LONG VERY LONG VERY LONG VERY LONG VERY LONG\n       LONG VERY LONG VERY LONG VERY LONG VERY LONG */\n    ", operand, "");
        assertEquals("" + simpleExpectation,
                parse(new PlMatchExpression("argName", operator,
                        operand.withComments(
                                comments(BEFORE_OPERAND, "/* LONG VERY LONG VERY LONG VERY LONG VERY LONG LONG VERY LONG VERY LONG VERY LONG VERY LONG */")),
                        null).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        simpleExpectation = simpex("", "argName", " ", operator, " ", operand, " /* new line */");
        assertEquals("" + simpleExpectation,
                parse(new PlMatchExpression("argName", operator, operand.withComments(comments(AFTER_OPERAND, "/*\nnew line\n*/")), null).format(INLINE))
                        .toString());

        simpleExpectation = simpex("", "argName", " ", operator, " ", operand,
                "\n/* LONG VERY LONG VERY LONG VERY LONG VERY LONG\n   LONG VERY LONG VERY LONG VERY LONG VERY LONG */");
        assertEquals("" + simpleExpectation,
                parse(new PlMatchExpression("argName", operator,
                        operand.withComments(
                                comments(AFTER_OPERAND, "/* LONG VERY LONG VERY LONG VERY LONG VERY LONG LONG VERY LONG VERY LONG VERY LONG VERY LONG */")),
                        null).format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    private static String simpex(String before, String argName, String c1, PlMatchOperator operator, String beforeOperand, PlOperand operand, String after) {
        return String.format("%s%s%s%s%s%s%s%s", before, argName, c1, operator.getOperatorTokens().get(1), beforeOperand, (operand.isReference() ? "@" : ""),
                operand.value(), after);
    }

    @Test
    void testStrictNotEquals() {

        PlMatchExpression expr = new PlMatchExpression("arg", STRICT_NOT_EQUALS, new PlOperand("value", false, null), null);

        assertEquals("STRICT arg != value", parse(expr.format(INLINE)).toString());
        assertEquals("STRICT arg != value", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ STRICT arg != value", parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("STRICT arg != value /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals("STRICT arg != value /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("STRICT /* comment C1 */ arg != value", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("STRICT /* comment C1 new line */ arg /* comment C2 */ != value",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE)).toString());

        assertEquals("/* comment BE */ STRICT arg != value", parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("STRICT arg != value /* comment AE */", parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals(
                "STRICT arg != value\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("STRICT /* comment C1 */ arg\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\n!= value", parse(
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"))).format(PRETTY_PRINT))
                        .format(PRETTY_PRINT));

    }

    @Test
    void testBetween() {

        List<PlOperand> btwOperand = Arrays.asList(new PlOperand("low", false, null), new PlOperand("high", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", BETWEEN, btwOperand, null);

        assertEquals("arg BETWEEN (low, high)", parse(expr.format(INLINE)).toString());
        assertEquals("arg BETWEEN (low, high)", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg BETWEEN (low, high)", parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg BETWEEN (low, high) /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals(
                "arg BETWEEN (low, high) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ BETWEEN (low, high)", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ BETWEEN /* comment C2 */ (low, high)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE)).toString());

        assertEquals("/* comment BE */ arg BETWEEN (low, high)", parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("arg BETWEEN (low, high) /* comment AE */", parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals(
                "arg BETWEEN (low, high)\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ BETWEEN\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\n(low, high)", parse(
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"))).format(PRETTY_PRINT))
                        .format(PRETTY_PRINT));

        List<PlOperand> btwOperandWithComments = Arrays.asList(
                btwOperand.get(0).withComments(comments(BEFORE_OPERAND, "/* comment: VERY LONG VERY LONG VERY LONG VERY LONG before low */")), btwOperand.get(1)
                        .withComments(comments(comments(BEFORE_OPERAND, "/* comment before high */"), comments(AFTER_OPERAND, "/* comment after high */"))));

        expr = new PlMatchExpression("arg", BETWEEN, btwOperandWithComments, null);

        assertEquals(
                "arg BETWEEN ( /* comment: VERY LONG VERY LONG VERY LONG VERY LONG before low */ low, /* comment before high */ high /* comment after high */ )",
                parse(expr.format(INLINE)).toString());
        assertEquals("""
                arg BETWEEN (
                        /* comment: VERY LONG VERY LONG VERY LONG VERY
                           LONG before low */
                        low,
                        /* comment before high */
                        high
                        /* comment after high */
                    )""", parse(expr.format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testNotBetween() {

        List<PlOperand> btwOperand = Arrays.asList(new PlOperand("low", false, null), new PlOperand("high", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", NOT_BETWEEN, btwOperand, null);

        assertEquals("arg NOT BETWEEN (low, high)", parse(expr.format(INLINE)).toString());
        assertEquals("arg NOT BETWEEN (low, high)", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg NOT BETWEEN (low, high)", parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg NOT BETWEEN (low, high) /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals(
                "arg NOT BETWEEN (low, high) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ NOT BETWEEN (low, high)", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */ NOT /* comment C2 */ BETWEEN (low, high)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2)).format(INLINE))
                        .toString());
        assertEquals("arg /* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */ NOT /* comment C2 */ BETWEEN /* comment C3 new line */ (low, high)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2,
                        comments(C3, "/* comment C3\nnew line */"))).format(INLINE)).toString());

        assertEquals("/* comment BE */ arg NOT BETWEEN (low, high)", parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("arg NOT BETWEEN (low, high) /* comment AE */", parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals(
                "arg NOT BETWEEN (low, high)\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ NOT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nBETWEEN (low, high)", parse(
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"))).format(PRETTY_PRINT))
                        .format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ NOT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nBETWEEN /* comment C3 */ (low, high)",
                parse(expr
                        .withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3))
                        .format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testStrictNotBetween() {

        List<PlOperand> btwOperand = Arrays.asList(new PlOperand("low", false, null), new PlOperand("high", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", STRICT_NOT_BETWEEN, btwOperand, null);

        assertEquals("arg STRICT NOT BETWEEN (low, high)", parse(expr.format(INLINE)).toString());
        assertEquals("arg STRICT NOT BETWEEN (low, high)", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg STRICT NOT BETWEEN (low, high)",
                parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg STRICT NOT BETWEEN (low, high) /* comment after expression */",
                parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals(
                "arg STRICT NOT BETWEEN (low, high) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ STRICT NOT BETWEEN (low, high)", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT BETWEEN (low, high)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 new line */ BETWEEN (low, high)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, comments(C3, "/* comment C3\nnew line */")))
                        .format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 new line */ BETWEEN /* comment C4 */ (low, high)",
                parse(expr.withComments(
                        comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, comments(C3, "/* comment C3\nnew line */"), SINGLE_COMMENT_C4))
                        .format(INLINE)).toString());

        assertEquals("/* comment BE */ arg STRICT NOT BETWEEN (low, high)",
                parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("arg STRICT NOT BETWEEN (low, high) /* comment AE */", parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals(
                "arg STRICT NOT BETWEEN (low, high)\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG */\nNOT BETWEEN (low, high)",
                parse(expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")))
                        .format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("""
                arg /* comment C1 */ STRICT
                /* comment C2 VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG */
                NOT /* comment C3 */ BETWEEN /* comment C4 */ (low, high)""",
                parse(expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"),
                        SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testContains() {

        PlMatchExpression expr = new PlMatchExpression("arg", CONTAINS, new PlOperand("value", false, null), null);

        assertEquals("arg CONTAINS value", parse(expr.format(INLINE)).toString());
        assertEquals("arg CONTAINS value", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg CONTAINS value", parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg CONTAINS value /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals("arg CONTAINS value /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ CONTAINS value", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */ CONTAINS value",
                parse(expr.withComments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */")).format(INLINE)).toString());

        assertEquals("/* comment BE */ arg CONTAINS value", parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("arg CONTAINS value /* comment AE */", parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals(
                "arg CONTAINS value\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nCONTAINS value",
                parse(expr.withComments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */")).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        PlOperand operand = new PlOperand("value", false,
                comments(comments(BEFORE_OPERAND, "/* comment before the value VERY LONG VERY LONG VERY LONG VERY LONG */"),
                        comments(AFTER_OPERAND, "/* comment after the value VERY LONG VERY LONG VERY LONG VERY LONG */")));

        expr = new PlMatchExpression("arg", CONTAINS, operand, null);
        assertEquals(
                "arg CONTAINS /* comment before the value VERY LONG VERY LONG VERY LONG VERY LONG */ value /* comment after the value VERY LONG VERY LONG VERY LONG VERY LONG */",
                parse(expr.format(INLINE)).toString());

        assertEquals("""
                arg CONTAINS
                    /* comment before the value VERY LONG VERY LONG
                       VERY LONG VERY LONG */
                    value
                /* comment after the value VERY LONG VERY LONG
                   VERY LONG VERY LONG */""", parse(expr.format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testNotContains() {

        PlMatchExpression expr = new PlMatchExpression("arg", NOT_CONTAINS, new PlOperand("value", false, null), null);

        assertEquals("arg NOT CONTAINS value", parse(expr.format(INLINE)).toString());
        assertEquals("arg NOT CONTAINS value", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg NOT CONTAINS value", parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg NOT CONTAINS value /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals(
                "arg NOT CONTAINS value /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ NOT CONTAINS value", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ NOT CONTAINS value",
                parse(expr.withComments(comments(C1, "/* comment C1\nnew line */")).format(INLINE)).toString());

        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ CONTAINS value",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE)).toString());

        assertEquals("/* comment BE */ arg NOT CONTAINS value", parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("arg NOT CONTAINS value /* comment AE */", parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals(
                "arg NOT CONTAINS value\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT CONTAINS value",
                parse(expr.withComments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */")).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT /* comment C2 */ CONTAINS value", parse(
                expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C2)).format(PRETTY_PRINT))
                        .format(PRETTY_PRINT));

    }

    @Test
    void testStrictNotContains() {

        PlMatchExpression expr = new PlMatchExpression("arg", STRICT_NOT_CONTAINS, new PlOperand("value", false, null), null);

        assertEquals("arg STRICT NOT CONTAINS value", parse(expr.format(INLINE)).toString());
        assertEquals("arg STRICT NOT CONTAINS value", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg STRICT NOT CONTAINS value", parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg STRICT NOT CONTAINS value /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals(
                "arg STRICT NOT CONTAINS value /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ STRICT NOT CONTAINS value", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ STRICT NOT CONTAINS value",
                parse(expr.withComments(comments(C1, "/* comment C1\nnew line */")).format(INLINE)).toString());

        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT CONTAINS value",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE)).toString());

        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ CONTAINS value",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE)).toString());

        assertEquals("/* comment BE */ arg STRICT NOT CONTAINS value", parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("arg STRICT NOT CONTAINS value /* comment AE */", parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals(
                "arg STRICT NOT CONTAINS value\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nSTRICT NOT CONTAINS value",
                parse(expr.withComments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")).format(PRETTY_PRINT))
                        .format(PRETTY_PRINT));

        assertEquals(
                "arg\n/* comment C1 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nSTRICT /* comment C2 */ NOT /* comment C3 */ CONTAINS value",
                parse(expr.withComments(comments(comments(C1, "/* comment C1 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"),
                        SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testAnyOf() {

        List<PlOperand> operandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("ref1", true, null));

        PlMatchExpression expr = new PlMatchExpression("arg", ANY_OF, operandList, null);

        assertEquals("arg ANY OF (v1, v2, @ref1)", parse(expr.format(INLINE)).toString());
        assertEquals("arg ANY OF (v1, v2, @ref1)", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg ANY OF (v1, v2, @ref1)", parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg ANY OF (v1, v2, @ref1) /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals(
                "arg ANY OF (v1, v2, @ref1) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ ANY OF (v1, v2, @ref1)", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ ANY /* comment C2 */ OF (v1, v2, @ref1)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ ANY /* comment C2 */ OF /* comment C3 */ (v1, v2, @ref1)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE)).toString());

        assertEquals("/* comment BE */ arg ANY OF (v1, v2, @ref1)", parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("arg ANY OF (v1, v2, @ref1) /* comment AE */", parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("""
                arg ANY OF (v1, v2, @ref1)
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""", expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ ANY\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nOF (v1, v2, @ref1)",
                parse(expr
                        .withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")))
                        .format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ ANY\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nOF /* comment C3 */ (v1, v2, @ref1)",
                parse(expr.withComments(comments(SINGLE_COMMENT_C1,
                        comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3)).format(PRETTY_PRINT))
                                .format(PRETTY_PRINT));

        List<PlOperand> operandListWithComments = Arrays.asList(
                operandList.get(0).withComments(comments(BEFORE_OPERAND, "/* comment: VERY LONG VERY LONG VERY LONG VERY LONG before v1 */")),
                operandList.get(2)
                        .withComments(comments(comments(BEFORE_OPERAND, "/* comment before ref1 */"), comments(AFTER_OPERAND, "/* comment after ref1 */"))));

        expr = new PlMatchExpression("arg", ANY_OF, operandListWithComments, null);

        assertEquals(
                "arg ANY OF ( /* comment: VERY LONG VERY LONG VERY LONG VERY LONG before v1 */ v1, /* comment before ref1 */ @ref1 /* comment after ref1 */ )",
                parse(expr.format(INLINE)).toString());
        assertEquals("""
                arg ANY OF (
                        /* comment: VERY LONG VERY LONG VERY LONG VERY
                           LONG before v1 */
                        v1,
                        /* comment before ref1 */
                        @ref1
                        /* comment after ref1 */
                    )""", parse(expr.format(PRETTY_PRINT)).format(PRETTY_PRINT));

        List<PlOperand> longOperandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("ref1", true, null),
                new PlOperand("v3", false, null), new PlOperand("v4", false, null), new PlOperand("v5", false, null));

        expr = new PlMatchExpression("arg", ANY_OF, longOperandList, null);

        assertEquals("arg ANY OF (v1, v2, @ref1, v3, v4, v5)", parse(expr.format(INLINE)).toString());
        assertEquals("""
                arg ANY OF (
                        v1,
                        v2,
                        @ref1,
                        v3,
                        v4,
                        v5
                    )""", parse(expr.format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testNotAnyOf() {
        List<PlOperand> operandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("ref1", true, null));

        PlMatchExpression expr = new PlMatchExpression("arg", NOT_ANY_OF, operandList, null);

        assertEquals("arg NOT ANY OF (v1, v2, @ref1)", parse(expr.format(INLINE)).toString());
        assertEquals("arg NOT ANY OF (v1, v2, @ref1)", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg NOT ANY OF (v1, v2, @ref1)", parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg NOT ANY OF (v1, v2, @ref1) /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals(
                """
                        arg NOT ANY OF (v1, v2, @ref1) /* comment 1 "    " VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */""",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ NOT ANY OF (v1, v2, @ref1)", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ ANY OF (v1, v2, @ref1)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ ANY /* comment C3 */ OF (v1, v2, @ref1)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ ANY /* comment C3 */ OF /* comment C4 */ (v1, v2, @ref1)", parse(
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(INLINE))
                        .toString());

        assertEquals("/* comment before expression */\narg NOT ANY OF (v1, v2, @ref1)",
                parse(expr.withComments(COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg NOT ANY OF (v1, v2, @ref1)\n/* comment after expression */",
                parse(expr.withComments(COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("""
                arg NOT ANY OF (v1, v2, @ref1)
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("""
                arg /* comment C1 */ NOT
                /* comment C2 VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG VERY LONG */
                ANY OF (v1, v2, @ref1)""",
                parse(expr
                        .withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")))
                        .format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("""
                arg /* comment C1 */ NOT
                /* comment C2 VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG VERY LONG VERY LONG */
                ANY /* comment C3 */ OF /* comment C4 */ (v1, v2, @ref1)""", parse(expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2,
                "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"),
                SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testStrictNotAnyOf() {
        List<PlOperand> operandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("ref1", true, null));

        PlMatchExpression expr = new PlMatchExpression("arg", STRICT_NOT_ANY_OF, operandList, null);

        assertEquals("arg STRICT NOT ANY OF (v1, v2, @ref1)", parse(expr.format(INLINE)).toString());
        assertEquals("arg STRICT NOT ANY OF (v1, v2, @ref1)", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg STRICT NOT ANY OF (v1, v2, @ref1)",
                parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg STRICT NOT ANY OF (v1, v2, @ref1) /* comment after expression */",
                parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals(
                "arg STRICT NOT ANY OF (v1, v2, @ref1) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ STRICT NOT ANY OF (v1, v2, @ref1)", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT ANY OF (v1, v2, @ref1)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ ANY OF (v1, v2, @ref1)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ ANY /* comment C4 */ OF (v1, v2, @ref1)", parse(
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(INLINE))
                        .toString());
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ ANY /* comment C4 */ OF /* comment C5 */ (v1, v2, @ref1)",
                parse(expr.withComments(
                        comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4, SINGLE_COMMENT_C5))
                        .format(INLINE)).toString());

        assertEquals("/* comment BE */ arg STRICT NOT ANY OF (v1, v2, @ref1)",
                parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg STRICT NOT ANY OF (v1, v2, @ref1) /* comment AE */",
                parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("""
                arg STRICT NOT ANY OF (v1, v2, @ref1)
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT ANY OF (v1, v2, @ref1)", parse(
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"))).format(PRETTY_PRINT))
                        .format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT /* comment C3 */ ANY /* comment C4 */ OF /* comment C5 */ (v1, v2, @ref1)",
                parse(expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3,
                        SINGLE_COMMENT_C4, SINGLE_COMMENT_C5)).format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testContainsAnyOf() {

        List<PlOperand> operandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("v3", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", CONTAINS_ANY_OF, operandList, null);

        assertEquals("arg CONTAINS ANY OF (v1, v2, v3)", parse(expr.format(INLINE)).toString());
        assertEquals("arg CONTAINS ANY OF (v1, v2, v3)", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg CONTAINS ANY OF (v1, v2, v3)",
                parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg CONTAINS ANY OF (v1, v2, v3) /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals(
                "arg CONTAINS ANY OF (v1, v2, v3) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ CONTAINS ANY OF (v1, v2, v3)", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ CONTAINS /* comment C2 */ ANY OF (v1, v2, v3)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ CONTAINS /* comment C2 */ ANY /* comment C3 */ OF (v1, v2, v3)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ CONTAINS /* comment C2 */ ANY /* comment C3 */ OF /* comment C4 */ (v1, v2, v3)", parse(
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(INLINE))
                        .toString());

        assertEquals("/* comment BE */ arg CONTAINS ANY OF (v1, v2, v3)", parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("arg CONTAINS ANY OF (v1, v2, v3) /* comment AE */", parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals(
                "arg CONTAINS ANY OF (v1, v2, v3)\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ CONTAINS\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nANY OF (v1, v2, v3)", parse(
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"))).format(PRETTY_PRINT))
                        .format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ CONTAINS\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nANY /* comment C3 */ OF (v1, v2, v3)",
                parse(expr
                        .withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3))
                        .format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ CONTAINS\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nANY /* comment C3 */ OF /* comment C4 */ (v1, v2, v3)",
                parse(expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3,
                        SINGLE_COMMENT_C4)).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        List<PlOperand> operandListWithComments = Arrays.asList(
                operandList.get(0).withComments(comments(BEFORE_OPERAND, "/* comment: VERY LONG VERY LONG VERY LONG VERY LONG before v1 */")),
                operandList.get(2).withComments(comments(comments(BEFORE_OPERAND, "/* before v3 */"), comments(AFTER_OPERAND, "/* after v3 */"))));

        expr = new PlMatchExpression("arg", CONTAINS_ANY_OF, operandListWithComments, null);

        assertEquals("arg CONTAINS ANY OF ( /* comment: VERY LONG VERY LONG VERY LONG VERY LONG before v1 */ v1, /* before v3 */ v3 /* after v3 */ )",
                parse(expr.format(INLINE)).toString());
        assertEquals("""
                arg CONTAINS ANY OF (
                        /* comment: VERY LONG VERY LONG VERY LONG VERY
                           LONG before v1 */
                        v1,
                        /* before v3 */ v3 /* after v3 */
                    )""", parse(expr.format(PRETTY_PRINT)).format(PRETTY_PRINT));

        List<PlOperand> longOperandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("v3", false, null),
                new PlOperand("v4", false, null), new PlOperand("v5", false, null), new PlOperand("v6", false, null));

        expr = new PlMatchExpression("arg", CONTAINS_ANY_OF, longOperandList, null);

        assertEquals("arg CONTAINS ANY OF (v1, v2, v3, v4, v5, v6)", parse(expr.format(INLINE)).toString());
        assertEquals("""
                arg CONTAINS ANY OF (
                        v1,
                        v2,
                        v3,
                        v4,
                        v5,
                        v6
                    )""", parse(expr.format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testNotContainsAnyOf() {

        List<PlOperand> operandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("v3", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", NOT_CONTAINS_ANY_OF, operandList, null);

        assertEquals("arg NOT CONTAINS ANY OF (v1, v2, v3)", parse(expr.format(INLINE)).toString());
        assertEquals("arg NOT CONTAINS ANY OF (v1, v2, v3)", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg NOT CONTAINS ANY OF (v1, v2, v3)",
                parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg NOT CONTAINS ANY OF (v1, v2, v3) /* comment after expression */",
                parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals(
                "arg NOT CONTAINS ANY OF (v1, v2, v3) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ NOT CONTAINS ANY OF (v1, v2, v3)", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ CONTAINS ANY OF (v1, v2, v3)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ CONTAINS /* comment C3 */ ANY OF (v1, v2, v3)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ CONTAINS /* comment C3 */ ANY /* comment C4 */ OF (v1, v2, v3)", expr
                .withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(INLINE));
        assertEquals("arg /* comment C1 new line */ NOT /* comment C2 */ CONTAINS /* comment C3 */ ANY /* comment C4 */ OF /* comment C5 */ (v1, v2, v3)",
                parse(expr.withComments(
                        comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4, SINGLE_COMMENT_C5))
                        .format(INLINE)).toString());

        assertEquals("/* comment BE */ arg NOT CONTAINS ANY OF (v1, v2, v3)",
                parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg NOT CONTAINS ANY OF (v1, v2, v3) /* comment AE */",
                parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("""
                arg NOT CONTAINS ANY OF (v1, v2, v3)
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("arg /* comment C1 */ NOT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nCONTAINS ANY OF (v1, v2, v3)",
                parse(expr
                        .withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")))
                        .format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ NOT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nCONTAINS /* comment C3 */ ANY OF (v1, v2, v3)",
                parse(expr.withComments(comments(SINGLE_COMMENT_C1,
                        comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3)).format(PRETTY_PRINT))
                                .format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ NOT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nCONTAINS /* comment C3 */ ANY /* comment C4 */ OF (v1, v2, v3)",
                parse(expr.withComments(comments(SINGLE_COMMENT_C1,
                        comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3, SINGLE_COMMENT_C4))
                        .format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ NOT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG VERY LONG VERY LONG */\nCONTAINS /* comment C3 */ ANY /* comment C4 */ OF /* comment C5 */ (v1, v2, v3)",
                parse(expr
                        .withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */"),
                                SINGLE_COMMENT_C3, SINGLE_COMMENT_C4, SINGLE_COMMENT_C5))
                        .format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testOperatorCommentsPrettyPrint() {
        String expr2 = """
                a = b
                AND (
                        argName
                        /* some comment very long comment */
                        NOT
                        /* some comment very long comment */
                        CONTAINS
                        /* some comment very long comment */
                        ANY
                        /* some comment very long comment */
                        OF (Hugo, red, red)
                     OR argName STRICT NOT CONTAINS "search text"
                    )""";

        assertEquals(expr2, parse(expr2).format(PRETTY_PRINT));

        expr2 = """
                a = b
                AND (
                        argName /* some comment */ NOT /* some comment */ CONTAINS /* some comment */ ANY /* some comment */ OF (Hugo, red, red)
                     OR argName STRICT NOT CONTAINS "search text"
                    )""";

        assertEquals(expr2, parse(expr2).format(PRETTY_PRINT));

        expr2 = """
                a = b
                AND (
                        argName /* short comment */ NOT
                        /* some comment very long comment */
                        CONTAINS /* short comment */ ANY
                        /* some comment very long comment */
                        OF (Hugo, red, red)
                     OR argName STRICT NOT CONTAINS "search text"
                    )""";

        assertEquals(expr2, parse(expr2).format(PRETTY_PRINT));

    }

    @Test
    void testStrictNotContainsAnyOf() {

        List<PlOperand> operandList = Arrays.asList(new PlOperand("v1", false, null), new PlOperand("v2", false, null), new PlOperand("v3", false, null));

        PlMatchExpression expr = new PlMatchExpression("arg", STRICT_NOT_CONTAINS_ANY_OF, operandList, null);

        assertEquals("arg STRICT NOT CONTAINS ANY OF (v1, v2, v3)", parse(expr.format(INLINE)).toString());
        assertEquals("arg STRICT NOT CONTAINS ANY OF (v1, v2, v3)", parse(expr.format(PRETTY_PRINT)).toString());

        assertEquals("/* comment before expression */ arg STRICT NOT CONTAINS ANY OF (v1, v2, v3)",
                parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("arg STRICT NOT CONTAINS ANY OF (v1, v2, v3) /* comment after expression */",
                parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals(
                "arg STRICT NOT CONTAINS ANY OF (v1, v2, v3) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals("arg /* comment C1 */ STRICT NOT CONTAINS ANY OF (v1, v2, v3)", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT CONTAINS ANY OF (v1, v2, v3)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2)).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ CONTAINS ANY OF (v1, v2, v3)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3)).format(INLINE)).toString());
        assertEquals("arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ CONTAINS /* comment C4 */ ANY OF (v1, v2, v3)", parse(
                expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4)).format(INLINE))
                        .toString());
        assertEquals(
                "arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ CONTAINS /* comment C4 */ ANY /* comment C5 */ OF (v1, v2, v3)",
                parse(expr.withComments(
                        comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4, SINGLE_COMMENT_C5))
                        .format(INLINE)).toString());
        assertEquals(
                "arg /* comment C1 new line */ STRICT /* comment C2 */ NOT /* comment C3 */ CONTAINS /* comment C4 */ ANY /* comment C5 */ OF /* comment C6 */ (v1, v2, v3)",
                parse(expr.withComments(comments(comments(C1, "/* comment C1\nnew line */"), SINGLE_COMMENT_C2, SINGLE_COMMENT_C3, SINGLE_COMMENT_C4,
                        SINGLE_COMMENT_C5, SINGLE_COMMENT_C6)).format(INLINE)).toString());

        assertEquals("/* comment BE */ arg STRICT NOT CONTAINS ANY OF (v1, v2, v3)",
                parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("arg STRICT NOT CONTAINS ANY OF (v1, v2, v3) /* comment AE */",
                parse(expr.withComments(SHORT_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).toString());
        assertEquals("""
                arg STRICT NOT CONTAINS ANY OF (v1, v2, v3)
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("""
                arg /* comment C1 */ STRICT
                /* comment C2 VERY LONG VERY LONG VERY LONG VERY
                   LONG */
                NOT CONTAINS ANY OF (v1, v2, v3)""", parse(
                expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"))).format(PRETTY_PRINT))
                        .format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT /* comment C3 */ CONTAINS ANY OF (v1, v2, v3)",
                parse(expr
                        .withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3))
                        .format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT /* comment C3 */ CONTAINS /* comment C4 */ ANY OF (v1, v2, v3)",
                parse(expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3,
                        SINGLE_COMMENT_C4)).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT /* comment C3 */ CONTAINS /* comment C4 */ ANY /* comment C5 */ OF (v1, v2, v3)",
                parse(expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3,
                        SINGLE_COMMENT_C4, SINGLE_COMMENT_C5)).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals(
                "arg /* comment C1 */ STRICT\n/* comment C2 VERY LONG VERY LONG VERY LONG VERY\n   LONG */\nNOT /* comment C3 */ CONTAINS /* comment C4 */ ANY /* comment C5 */ OF /* comment C6 */ (v1, v2, v3)",
                parse(expr.withComments(comments(SINGLE_COMMENT_C1, comments(C2, "/* comment C2 VERY LONG VERY LONG VERY LONG VERY LONG */"), SINGLE_COMMENT_C3,
                        SINGLE_COMMENT_C4, SINGLE_COMMENT_C5, SINGLE_COMMENT_C6)).format(PRETTY_PRINT)).format(PRETTY_PRINT));
    }

    @Test
    void testBasicCombinationsInline() {

        assertEquals("a = 1 AND b = 2", parse(and(match("a", EQUALS, "1"), match("b", EQUALS, "2")).toString()).toString());
        assertEquals("a = 1 OR b = 2", parse(or(match("a", EQUALS, "1"), match("b", EQUALS, "2")).toString()).toString());
        assertEquals("a = 1 AND b != 2", parse(and(match("a", EQUALS, "1"), match("b", NOT_EQUALS, "2")).toString()).toString());
        assertEquals("a = 1 AND STRICT b != 2", parse(and(match("a", EQUALS, "1"), match("b", STRICT_NOT_EQUALS, "2")).toString()).toString());
        assertEquals("a = 1 AND NOT b = 2", parse(and(match("a", EQUALS, "1"), not(match("b", EQUALS, "2"))).toString()).toString());
        assertEquals("a = 1 AND b ANY OF (2, 3, 4)",
                parse(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4")))).toString()).toString());

        PlCombinedExpression expr = and(match("a", EQUALS, "1"), match("b", EQUALS, "2"));

        assertEquals("/* comment before expression */ a = 1 AND b = 2", parse(expr.withComments(COMMENT_BEFORE_EXPR).toString()).toString());

        assertEquals("a = 1 AND b = 2 /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).toString()).toString());
        assertEquals("/* comment 1 before expression */ a = 1 AND b = 2 /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).toString()).toString());

        assertEquals("a = 1 AND b = 2 /* comment 1 after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_AFTER_EXPR).toString()).toString());

        assertEquals("a = 1 AND b = 2 /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).toString()).toString());

    }

    @Test
    void testBasicCombinationsPrettyPrint() {

        assertEquals("a = 1\nAND b = 2", parse(and(match("a", EQUALS, "1"), match("b", EQUALS, "2")).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("a = 1\nOR b = 2", parse(or(match("a", EQUALS, "1"), match("b", EQUALS, "2")).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("a = 1\nAND b != 2", parse(and(match("a", EQUALS, "1"), match("b", NOT_EQUALS, "2")).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("a = 1\nAND STRICT b != 2",
                parse(and(match("a", EQUALS, "1"), match("b", STRICT_NOT_EQUALS, "2")).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("a = 1\nAND NOT b = 2", parse(and(match("a", EQUALS, "1"), not(match("b", EQUALS, "2"))).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("a = 1\nAND b ANY OF (2, 3, 4)",
                parse(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4")))).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("""
                a = 1
                AND b ANY OF (
                        2,
                        3,
                        4,
                        5,
                        6
                    )""", parse(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"), vop("5"), vop("6")))).format(PRETTY_PRINT))
                .format(PRETTY_PRINT));

        PlCombinedExpression expr = and(match("a", EQUALS, "1"), match("b", EQUALS, "2"));

        assertEquals("""
                /* comment before expression */
                a = 1
                AND b = 2""", parse(expr.withComments(COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("""
                /* comment BE */
                a = 1
                AND b = 2""", parse(expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("""
                a = 1
                AND b = 2
                    /* comment after expression */""", parse(expr.withComments(COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("""
                /* comment 1 before expression */
                a = 1
                AND b = 2
                    /* comment 2 after expression */""",
                parse(expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND b = 2
                    /* comment 1 after expression */
                    /* comment 2 after expression */""", parse(expr.withComments(TWO_COMMENTS_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND b = 2
                    /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                       VERY LONG after expression */
                    /* comment 2 after expression */""",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testCombinedNestingInline() {

        assertEquals("a = 1 AND (b = 2 OR c = 3)",
                parse(and(match("a", EQUALS, "1"), or(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).toString()).toString());
        assertEquals("a = 1 AND (b = 2 OR c = 3)",
                parse(and(match("a", EQUALS, "1"), or(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(INLINE)).toString());

        assertEquals("a = 1 AND (b = 2 AND c = 3)",
                parse(and(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).toString()).toString());
        assertEquals("a = 1 AND (b = 2 AND c = 3)",
                parse(and(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(INLINE)).toString());

        assertEquals("a = 1 OR (b = 2 AND c = 3)",
                parse(or(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).toString()).toString());
        assertEquals("a = 1 OR (b = 2 AND c = 3)",
                parse(or(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(INLINE)).toString());

        assertEquals("a = 1 OR (b = 2 OR c = 3)",
                parse(or(match("a", EQUALS, "1"), or(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).toString()).toString());
        assertEquals("a = 1 OR (b = 2 OR c = 3)",
                parse(or(match("a", EQUALS, "1"), or(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(INLINE)).toString());

        assertEquals("(b = 2 OR c = 3) AND a = 1",
                parse(and(or(match("b", EQUALS, "2"), match("c", EQUALS, "3")), match("a", EQUALS, "1")).toString()).toString());
        assertEquals("(b = 2 OR c = 3) AND a = 1",
                parse(and(or(match("b", EQUALS, "2"), match("c", EQUALS, "3")), match("a", EQUALS, "1")).format(INLINE)).toString());

        assertEquals("(b = 2 OR c = 3) AND (a = 1 OR z = 4)",
                parse(and(or(match("b", EQUALS, "2"), match("c", EQUALS, "3")), or(match("a", EQUALS, "1"), match("z", EQUALS, "4"))).toString()).toString());
        assertEquals("(b = 2 OR c = 3) AND (a = 1 OR z = 4)",
                parse(and(or(match("b", EQUALS, "2"), match("c", EQUALS, "3")), or(match("a", EQUALS, "1"), match("z", EQUALS, "4"))).format(INLINE))
                        .toString());

        assertEquals("""
                a = 1 AND (b ANY OF (2, 3, 4) OR c ANY OF (2, 3, 4) )""",
                parse(and(match("a", EQUALS, "1"),
                        or(match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))), match("c", ANY_OF, lop(vop("2"), vop("3"), vop("4"))))).toString())
                                .toString());
        assertEquals("""
                a = 1 AND (b ANY OF (2, 3, 4) OR c ANY OF (2, 3, 4) )""",
                parse(and(match("a", EQUALS, "1"),
                        or(match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))), match("c", ANY_OF, lop(vop("2"), vop("3"), vop("4"))))).format(INLINE))
                                .toString());

        assertEquals("""
                a = 1 AND (b CONTAINS ANY OF (s2, s3, s4, s5, s6) OR c CONTAINS ANY OF (s2, s3, s4) )""",
                parse(and(match("a", EQUALS, "1"), or(match("b", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"), vop("s5"), vop("s6"))),
                        match("c", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"))))).toString()).toString());
        assertEquals("""
                a = 1 AND (b CONTAINS ANY OF (s2, s3, s4, s5, s6) OR c CONTAINS ANY OF (s2, s3, s4) )""",
                parse(and(match("a", EQUALS, "1"), or(match("b", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"), vop("s5"), vop("s6"))),
                        match("c", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"))))).format(INLINE)).toString());

        // @formatter:off
        PlExpression<?> expr =
                and(match("a", EQUALS, "1"), 
                    or(
                       and(and (match("foo", LESS_THAN, "high"),
                               match("foo", GREATER_THAN_OR_EQUALS, "low")
                           ),
                           match("b", EQUALS, "2"), 
                           match("e", EQUALS, "9"),
                           match("q", EQUALS, "10")
                       ), 
                       and(match("c", EQUALS, "3"), 
                           match("d", EQUALS, "5"),
                           or (match("k", EQUALS, "4"),
                               match("l", EQUALS, "92")
                           )
                       )
                    )
                );
        // @formatter:on

        assertEquals("a = 1 AND ( ( (foo < high AND foo >= low) AND b = 2 AND e = 9 AND q = 10) OR (c = 3 AND d = 5 AND (k = 4 OR l = 92) ) )",
                parse(expr.toString()).toString());

        assertEquals("a = 1 AND ( ( (foo < high AND foo >= low) AND b = 2 AND e = 9 AND q = 10) OR (c = 3 AND d = 5 AND (k = 4 OR l = 92) ) )",
                parse(expr.format(INLINE)).toString());

    }

    @Test
    void testCombinedNestingPrettyPrint() {

        assertEquals("a = 1\nAND (\n        b = 2\n     OR c = 3\n    )",
                parse(and(match("a", EQUALS, "1"), or(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("a = 1\nAND (\n        b = 2\n    AND c = 3\n    )",
                parse(and(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("a = 1\nOR (\n        b = 2\n    AND c = 3\n    )",
                parse(or(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("a = 1\nOR (\n        b = 2\n     OR c = 3\n    )",
                parse(or(match("a", EQUALS, "1"), or(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("(\n        b = 2\n     OR c = 3\n    )\nAND a = 1",
                parse(and(or(match("b", EQUALS, "2"), match("c", EQUALS, "3")), match("a", EQUALS, "1")).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("(\n        b = 2\n     OR c = 3\n    )\nAND (\n        a = 1\n     OR z = 4\n    )",
                parse(and(or(match("b", EQUALS, "2"), match("c", EQUALS, "3")), or(match("a", EQUALS, "1"), match("z", EQUALS, "4"))).format(PRETTY_PRINT))
                        .format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND (
                        b ANY OF (2, 3, 4)
                     OR c ANY OF (2, 3, 4)
                    )""",
                parse(and(match("a", EQUALS, "1"),
                        or(match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))), match("c", ANY_OF, lop(vop("2"), vop("3"), vop("4"))))).format(PRETTY_PRINT))
                                .format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND (
                        b CONTAINS ANY OF (
                            s2,
                            s3,
                            s4,
                            s5,
                            s6
                        )
                     OR c CONTAINS ANY OF (s2, s3, s4)
                    )""", parse(and(match("a", EQUALS, "1"), or(match("b", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"), vop("s5"), vop("s6"))),
                match("c", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"))))).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        // @formatter:off
        PlExpression<?> expr =
                and(match("a", EQUALS, "1"), 
                    or(
                       and(and (match("foo", LESS_THAN, "high"),
                               match("foo", GREATER_THAN_OR_EQUALS, "low")
                           ),
                           match("b", EQUALS, "2"), 
                           match("e", ANY_OF, lop(vop("v1"), vop("v2"), vop("v3"))),
                           match("q", NOT_ANY_OF, lop(vop("v1"), vop("v2"), vop("v3"), vop("v4"), vop("v5")))
                       ), 
                       and(match("c", EQUALS, "3"), 
                           match("d", EQUALS, "5"),
                           or (match("k", EQUALS, "4"),
                               match("l", EQUALS, "92")
                           )
                       )
                    )
                );
        // @formatter:on

        assertEquals("""
                a = 1
                AND (
                        (
                            (
                                foo < high
                            AND foo >= low
                            )
                        AND b = 2
                        AND e ANY OF (v1, v2, v3)
                        AND q NOT ANY OF (
                                v1,
                                v2,
                                v3,
                                v4,
                                v5
                            )
                        )
                     OR (
                            c = 3
                        AND d = 5
                        AND (
                                k = 4
                             OR l = 92
                            )
                        )
                    )""", parse(expr.format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testCombinedNestingWithCommentsInline() {

        assertEquals("a = /* comment before operand */ 1 AND (b = 2 /* comment after operand */ OR c = 3)",
                parse(and(match("a", EQUALS, vop("1").withComments(COMMENT_BEFORE_OPERAND)),
                        or(match("b", EQUALS, vop("2").withComments(COMMENT_AFTER_OPERAND)), match("c", EQUALS, "3"))).toString()).toString());
        assertEquals("a = /* comment before operand */ 1 AND (b = 2 /* comment after operand */ OR c = 3)",
                parse(and(match("a", EQUALS, vop("1").withComments(COMMENT_BEFORE_OPERAND)),
                        or(match("b", EQUALS, vop("2").withComments(COMMENT_AFTER_OPERAND)), match("c", EQUALS, "3"))).format(INLINE)).toString());

        assertEquals("a = 1 OR /* comment 1 before expression */ (b = 2 AND c = 3) /* comment 2 after expression */", parse(
                or(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3")).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR)).toString())
                        .toString());

        assertEquals("a = 1 OR /* comment 1 before expression */ (b = 2 AND c = 3) /* comment 2 after expression */",
                parse("(a = 1 OR /* comment 1 before expression */ (b = 2 AND c = 3) ) /* comment 2 after expression */").toString());

        assertEquals(
                "a = 1 OR (b = 2 AND c = 3) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(or(match("a", EQUALS, "1"),
                        and(match("b", EQUALS, "2"), match("c", EQUALS, "3")).withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION)).toString())
                                .toString());

        assertEquals(
                "a = 1 OR (b = 2 AND c = 3) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse("(a = 1 OR (b = 2 AND c = 3) ) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */")
                        .toString());

        // @formatter:off
        PlExpression<?> expr =
                and(match("a", EQUALS, vop("1").withComments(COMMENT_AFTER_OPERAND)), 
                    or(
                       and(and (match("foo", LESS_THAN, "high"),
                                match("foo", GREATER_THAN_OR_EQUALS, vop("low").withComments(COMMENT_BEFORE_OPERAND))
                           ).withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION),
                           match("b", EQUALS, "2"), 
                           match("e", ANY_OF, lop(vop("v1"), vop("v2"), vop("v3"))),
                           match("q", NOT_ANY_OF, lop(vop("v1"), vop("v2"), vop("v3"), vop("v4").withComments(TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND), vop("v5")))
                       ), 
                       and(match("c", EQUALS, "3").withComments(SHORT_COMMENT_BEFORE_EXPR), 
                           match("d", EQUALS, "5"),
                           or (match("k", EQUALS, "4"),
                               match("l", EQUALS, "92").withComments(COMMENT_AFTER_EXPR)
                           )
                       )
                    )
                );
        // @formatter:on

        assertEquals(
                """
                        a = 1 /* comment after operand */ AND ( ( (foo < high AND foo >= /* comment before operand */ low) /* comment 1 "    " VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */ AND b = 2 AND e ANY OF (v1, v2, v3) AND q NOT ANY OF (v1, v2, v3, /* comment 1 before operand */ v4 /* comment 2 after operand */, v5) ) OR ( /* comment BE */ c = 3 AND d = 5 AND (k = 4 OR l = 92 /* comment after expression */ ) ) )""",
                parse(expr.toString()).toString());
        assertEquals(
                """
                        a = 1 /* comment after operand */ AND ( ( (foo < high AND foo >= /* comment before operand */ low) /* comment 1 "    " VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */ AND b = 2 AND e ANY OF (v1, v2, v3) AND q NOT ANY OF (v1, v2, v3, /* comment 1 before operand */ v4 /* comment 2 after operand */, v5) ) OR ( /* comment BE */ c = 3 AND d = 5 AND (k = 4 OR l = 92 /* comment after expression */ ) ) )""",
                parse(expr.format(INLINE)).toString());

        assertEquals(
                "CURB (argName STRICT NOT ANY OF (a, @r) OR argName /* A */ STRICT /* B */ NOT /* C */ CONTAINS /* D */ ANY /* E */ OF /* F */ (red, green, yeti) OR NOT argName IS UNKNOWN) = 10",
                parse("CURB (argName STRICT NOT ANY OF (a, @r) OR argName /* A */ STRICT /* B */ NOT /* C */ CONTAINS /* D */ ANY /* E */ OF /* F */ (red, green, yeti) OR NOT argName IS UNKNOWN) = 10")
                        .format(INLINE));

    }

    @Test
    void testNestingWithCommentsPrettyPrint() {

        assertEquals("""
                a =
                    /* comment before operand */
                    1
                AND (
                        b = 2
                        /* comment after operand */
                     OR c = 3
                    )""",
                parse(and(match("a", EQUALS, vop("1").withComments(COMMENT_BEFORE_OPERAND)),
                        or(match("b", EQUALS, vop("2").withComments(COMMENT_AFTER_OPERAND)), match("c", EQUALS, "3"))).format(PRETTY_PRINT))
                                .format(PRETTY_PRINT));

        assertEquals("""
                a = /* before */ 1 /* after */
                AND (
                        b = 2
                        /* comment after operand */
                     OR c = 3
                    )""",
                parse(and(match("a", EQUALS, vop("1").withComments(comments(comments(BEFORE_OPERAND, "/* before */"), comments(AFTER_OPERAND, "/* after */")))),
                        or(match("b", EQUALS, vop("2").withComments(COMMENT_AFTER_OPERAND)), match("c", EQUALS, "3"))).format(PRETTY_PRINT))
                                .format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                OR
                    /* comment 1 before expression */
                    (
                        b = 2
                    AND c = 3
                    )
                /* comment 2 after expression */""",
                parse(or(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3")).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR))
                        .format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                OR (
                        b = 2
                    AND c = 3
                    )
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""",
                parse(or(match("a", EQUALS, "1"),
                        and(match("b", EQUALS, "2"), match("c", EQUALS, "3")).withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION)).format(PRETTY_PRINT))
                                .format(PRETTY_PRINT));

        // @formatter:off
        PlExpression<?> expr =
                and(match("a", EQUALS, vop("1").withComments(comments(AFTER_OPERAND, "/* after (1) */"))), 
                    or(
                       and(and (match("foo", LESS_THAN, "high"),
                                match("foo", GREATER_THAN_OR_EQUALS, vop("low").withComments(comments(BEFORE_OPERAND, "/* before low */")))
                           ).withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION),
                           match("b", EQUALS, "2"), 
                           match("e", ANY_OF, lop(vop("v1"), vop("v2"), vop("v3"))),
                           match("q", NOT_ANY_OF, lop(vop("v1"), vop("v2"), vop("v3"), vop("v4").withComments(TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND), vop("v5")))
                       ), 
                       and(match("c", EQUALS, "3").withComments(comments(SHORT_COMMENT_BEFORE_EXPR, SHORT_COMMENT_AFTER_EXPR)), 
                           match("d", EQUALS, "5"),
                           or (match("k", EQUALS, "4"),
                               match("l", EQUALS, "92").withComments(COMMENT_AFTER_EXPR)
                           )
                       )
                    )
                );
        // @formatter:on

        assertEquals("""
                a = 1 /* after (1) */
                AND (
                        (
                            (
                                foo < high
                            AND foo >= /* before low */ low
                            )
                            /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                               VERY LONG after expression */
                            /* comment 2 after expression */
                        AND b = 2
                        AND e ANY OF (v1, v2, v3)
                        AND q NOT ANY OF (
                                v1,
                                v2,
                                v3,
                                /* comment 1 before operand */
                                v4
                                /* comment 2 after operand */,
                                v5
                            )
                        )
                     OR (
                            /* comment BE */ c = 3 /* comment AE */
                        AND d = 5
                        AND (
                                k = 4
                             OR l = 92
                                /* comment after expression */
                            )
                        )
                    )""", parse(expr.format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                CURB (
                        argName STRICT NOT ANY OF (a, @r)
                     OR argName /* A */ STRICT /* B */ NOT /* C */ CONTAINS /* D */ ANY /* E */ OF /* F */ (red, green, yeti)
                     OR /* G */ NOT /* H */ argName IS UNKNOWN
                    ) = 10""", parse(
                "CURB (argName STRICT NOT ANY OF (a, @r) OR argName /* A */ STRICT /* B */ NOT /* C */ CONTAINS /* D */ ANY /* E */ OF /* F */ (red, green, yeti) OR /* G */ NOT /* H */ argName IS UNKNOWN) = 10")
                        .format(PRETTY_PRINT));

    }

    @Test
    void testBasicCurbCombinationsInline() {

        assertCurbOutput("CURB (a = 1 OR b = 2) ${operator} ${bound}", or(match("a", EQUALS, "1"), match("b", EQUALS, "2")), INLINE);
        assertCurbOutput("CURB (a < 1 OR b != 2 OR c >= 3) ${operator} ${bound}",
                or(match("a", LESS_THAN, "1"), match("b", NOT_EQUALS, "2"), match("c", GREATER_THAN_OR_EQUALS, "3")), INLINE);

        assertCurbOutput("CURB (a = 1 OR STRICT b != 2) ${operator} ${bound}", or(match("a", EQUALS, "1"), match("b", STRICT_NOT_EQUALS, "2")), INLINE);

        assertCurbOutput("CURB (a = 1 OR b ANY OF (2, 3, 4) ) ${operator} ${bound}",
                or(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4")))), INLINE);

        assertCurbOutput("CURB (a = 1 OR (b ANY OF (2, 3, 4) OR c CONTAINS ANY OF (s2, s3, s4, s5, s6) ) ) ${operator} ${bound}",
                or(match("a", EQUALS, "1"), or(match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))),
                        match("c", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"), vop("s5"), vop("s6"))))),
                INLINE);

        assertCurbOutput("CURB (a = 1 OR (b ANY OF (2, 3, 4) AND c CONTAINS ANY OF (s2, s3, s4, s5, s6) ) ) ${operator} ${bound}",
                or(match("a", EQUALS, "1"), and(match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))),
                        match("c", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"), vop("s5"), vop("s6"))))),
                INLINE);

        PlCurbExpression expr = new PlCurbExpression(or(match("a", EQUALS, "1"), match("b", EQUALS, "2")), PlCurbOperator.EQUALS, 2, null);

        assertEquals("/* comment before expression */ CURB (a = 1 OR b = 2) = 2", parse(expr.withComments(COMMENT_BEFORE_EXPR).toString()).toString());
        assertEquals("/* comment before expression */ CURB (a = 1 OR b = 2) = 2", parse(expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());

        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).toString()).toString());
        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());

        assertEquals("/* comment 1 before expression */ CURB (a = 1 OR b = 2) = 2 /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).toString()).toString());
        assertEquals("/* comment 1 before expression */ CURB (a = 1 OR b = 2) = 2 /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).format(INLINE)).toString());

        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment 1 after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_AFTER_EXPR).toString()).toString());
        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment 1 after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_AFTER_EXPR).format(INLINE)).toString());

        assertEquals(
                "CURB (a = 1 OR b = 2) = 2 /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).toString()).toString());
        assertEquals(
                "CURB (a = 1 OR b = 2) = 2 /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());

    }

    @Test
    void testCurbWithCommentsInline() {

        PlCombinedExpression innerOr = or(match("a", EQUALS, "1"), match("b", EQUALS, "2"));
        PlCurbExpression expr = new PlCurbExpression(innerOr, PlCurbOperator.EQUALS, 2, null);

        assertEquals("/* comment before expression */ CURB (a = 1 OR b = 2) = 2", parse(expr.withComments(COMMENT_BEFORE_EXPR).toString()).toString());

        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment after expression */", parse(expr.withComments(COMMENT_AFTER_EXPR).toString()).toString());

        assertEquals("/* comment 1 before expression */ CURB (a = 1 OR b = 2) = 2 /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).toString()).toString());

        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment 1 after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_AFTER_EXPR).toString()).toString());

        assertEquals(
                "CURB (a = 1 OR b = 2) = 2 /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).toString()).toString());

        PlCurbExpression expr2 = new PlCurbExpression(innerOr.withComments(SHORT_COMMENT_BEFORE_EXPR), PlCurbOperator.EQUALS, 2, null);

        assertEquals("CURB /* comment BE */ (a = 1 OR b = 2) = 2", parse(expr2.format(INLINE)).toString());

        PlCurbExpression expr3 = new PlCurbExpression(innerOr.withComments(SHORT_COMMENT_AFTER_EXPR), PlCurbOperator.EQUALS, 2, null);

        assertEquals("CURB (a = 1 OR b = 2) /* comment AE */ = 2", parse(expr3.format(INLINE)).toString());

        assertEquals("CURB (a = 1 OR b = 2) = /* comment C1 */ 2", parse(expr.withComments(SINGLE_COMMENT_C1).format(INLINE)).toString());

    }

    @Test
    void testCurbBasicCombinationsPrettyPrint() {

        assertCurbOutput("""
                CURB (
                        a = 1
                     OR b = 2
                    ) ${operator} ${bound}""", or(match("a", EQUALS, "1"), match("b", EQUALS, "2")), PRETTY_PRINT);

        assertCurbOutput("""
                CURB (
                        a < 1
                     OR b != 2
                     OR c >= 3
                    ) ${operator} ${bound}""", or(match("a", LESS_THAN, "1"), match("b", NOT_EQUALS, "2"), match("c", GREATER_THAN_OR_EQUALS, "3")),
                PRETTY_PRINT);

        assertCurbOutput("""
                CURB (
                        a = 1
                     OR STRICT b != 2
                    ) ${operator} ${bound}""", or(match("a", EQUALS, "1"), match("b", STRICT_NOT_EQUALS, "2")), PRETTY_PRINT);

        assertCurbOutput("""
                CURB (
                        a = 1
                     OR b ANY OF (2, 3, 4)
                    ) ${operator} ${bound}""", or(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4")))), PRETTY_PRINT);

        assertCurbOutput("""
                CURB (
                        a = 1
                     OR (
                            b ANY OF (2, 3, 4)
                         OR c CONTAINS ANY OF (
                                s2,
                                s3,
                                s4,
                                s5,
                                s6
                            )
                        )
                    ) ${operator} ${bound}""", or(match("a", EQUALS, "1"), or(match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))),
                match("c", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"), vop("s5"), vop("s6"))))), PRETTY_PRINT);

        assertCurbOutput("""
                CURB (
                        a = 1
                     OR (
                            b ANY OF (2, 3, 4)
                        AND c CONTAINS ANY OF (
                                s2,
                                s3,
                                s4,
                                s5,
                                s6
                            )
                        )
                    ) ${operator} ${bound}""", or(match("a", EQUALS, "1"), and(match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))),
                match("c", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"), vop("s5"), vop("s6"))))), PRETTY_PRINT);

    }

    @Test
    void testCurbWithCommentsPrettyPrint() {
        PlCombinedExpression innerOr = or(match("a", EQUALS, "1"), match("b", EQUALS, "2"));

        PlCurbExpression expr = new PlCurbExpression(innerOr, PlCurbOperator.EQUALS, 2, null);

        assertEquals("""
                /* comment before expression */
                CURB (
                        a = 1
                     OR b = 2
                    ) = 2""", parse(expr.withComments(COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    ) = 2
                /* comment after expression */""", parse(expr.withComments(COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                /* comment 1 before expression */
                CURB (
                        a = 1
                     OR b = 2
                    ) = 2
                /* comment 2 after expression */""", parse(expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    ) = 2
                /* comment 1 after expression */
                /* comment 2 after expression */""", parse(expr.withComments(TWO_COMMENTS_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    ) = 2
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""",
                parse(expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        expr = new PlCurbExpression(innerOr.withComments(SHORT_COMMENT_BEFORE_EXPR), PlCurbOperator.EQUALS, 2, null);

        assertEquals("""
                CURB /* comment BE */
                    (
                        a = 1
                     OR b = 2
                    ) = 2""", parse(expr.format(PRETTY_PRINT)).format(PRETTY_PRINT));

        expr = new PlCurbExpression(innerOr.withComments(comments(BEFORE_EXPRESSION, "/* longer comment before members */")), PlCurbOperator.EQUALS, 2, null);

        assertEquals("""
                CURB
                    /* longer comment before members */
                    (
                        a = 1
                     OR b = 2
                    ) = 2""", parse(expr.format(PRETTY_PRINT)).format(PRETTY_PRINT));

        expr = new PlCurbExpression(innerOr.withComments(SHORT_COMMENT_AFTER_EXPR), PlCurbOperator.EQUALS, 2, null);

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    ) /* comment AE */ = 2""", parse(expr.format(PRETTY_PRINT)).format(PRETTY_PRINT));

        expr = new PlCurbExpression(innerOr.withComments(comments(AFTER_EXPRESSION, "/* longer comment before operator */")), PlCurbOperator.EQUALS, 2, null);

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    )
                    /* longer comment before operator */
                    = 2""", parse(expr.format(PRETTY_PRINT)).format(PRETTY_PRINT));

        expr = new PlCurbExpression(innerOr, PlCurbOperator.EQUALS, 2, null);

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    ) = /* comment C1 */ 2""", parse(expr.withComments(SINGLE_COMMENT_C1).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    ) =
                    /* longer comment before bound */
                    2""", parse(expr.withComments(comments(C1, "/* longer comment before bound */")).format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testNegBasics() {

        assertEquals("NOT <ALL>", parse(not(ALL).toString()).toString());
        assertEquals("NOT <ALL>", parse(not(ALL).format(FormatStyle.PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("STRICT NOT <ALL>", parse(strictNot(ALL).toString()).toString());
        assertEquals("STRICT NOT <ALL>", parse(strictNot(ALL).format(FormatStyle.PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("NOT (a = 1 AND b ANY OF (2, 3, 4) )",
                parse(not(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))))).toString()).toString());
        assertEquals("NOT (a = 1 AND b ANY OF (2, 3, 4) )",
                parse(not(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))))).format(INLINE)).toString());

        assertEquals("""
                NOT (
                        a = 1
                    AND b ANY OF (2, 3, 4)
                    )""",
                parse(not(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))))).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("NOT NOT (a = 1 AND b ANY OF (2, 3, 4) )",
                parse(not(not(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4")))))).toString()).toString());
        assertEquals("NOT NOT (a = 1 AND b ANY OF (2, 3, 4) )",
                parse(not(not(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4")))))).format(INLINE)).toString());

        assertEquals("""
                NOT NOT (
                            a = 1
                        AND b ANY OF (2, 3, 4)
                        )""",
                parse(not(not(and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4")))))).format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testNegInlineFormatComments() {
        assertEquals("/* comment before expression */ NOT <ALL>", parse(not(ALL).withComments(COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("NOT /* comment before expression */ <ALL>", parse(not(ALL.withComments(COMMENT_BEFORE_EXPR)).format(INLINE)).toString());

        assertEquals("/* */ NOT <ALL>", parse(not(ALL).withComments(EMPTY_COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("/* comment 1 before expression */ /* comment 2 before expression */ NOT <ALL>",
                parse(not(ALL).withComments(TWO_COMMENTS_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("/* comment VERY LONG VERY LONG VERY LONG VERY LONG before expression */ NOT <ALL>",
                parse(not(ALL).withComments(MULTI_LINE_COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY LONG before expression */ /* comment 2 VERY LONG VERY LONG VERY LONG VERY LONG before expression */ NOT <ALL>",
                parse(not(ALL).withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("NOT <ALL> /* comment after expression */", parse(not(ALL).withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals("NOT <ALL> /* comment 1 after expression */ /* comment 2 after expression */",
                parse(not(ALL).withComments(TWO_COMMENTS_AFTER_EXPR).format(INLINE)).toString());
        assertEquals("NOT <ALL> /* comment VERY LONG VERY LONG VERY LONG VERY LONG after expression */",
                parse(not(ALL).withComments(MULTI_LINE_COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals("NOT <ALL> /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(not(ALL).withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());
        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY LONG before expression */ NOT <ALL> /* comment 2 VERY LONG VERY LONG VERY LONG VERY LONG after expression */",
                parse(not(ALL).withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_AND_AFTER_EXPR).format(INLINE)).toString());

        assertEquals("/* */ /* Hugo */ /* Fluffy */ NOT <ALL>",
                parse(not(ALL).withComments(comments(BEFORE_EXPRESSION, "/**/", "/* Hugo */", "/* Fluffy */")).format(INLINE)).toString());

        assertEquals("NOT /* */ <ALL>", parse(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/**/"))).format(INLINE)).toString());
        assertEquals("NOT <ALL> /* */", parse(not(ALL).withComments(comments(AFTER_EXPRESSION, "/**/")).format(INLINE)).toString());

        assertEquals("NOT /* */ <ALL>", parse(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/*\n\n\n  */"))).format(INLINE)).toString());

        assertEquals("NOT NOT /* */ <ALL>", parse(not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/**/")))).format(INLINE)).toString());

        assertEquals("NOT NOT /* */ <ALL> /* after all */", parse(not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/*\n\n\n  */"))))
                .withComments(comments(AFTER_EXPRESSION, "/* after all */")).format(INLINE)).toString());

        assertEquals("NOT /* C1 of first NOT */ NOT /* */ <ALL>",
                parse(not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/*\n\n\n  */"))).withComments(comments(BEFORE_EXPRESSION, "/* C1 of first NOT */")))
                        .format(INLINE)).toString());

        assertEquals("STRICT /* */ NOT <ALL>", parse(strictNot(ALL).withComments(comments(C1, "/**/")).format(INLINE)).toString());

        assertEquals("STRICT /* C1 VERY LONG VERY LONG VERY LONG VERY LONG comment */ NOT /* BE comment */ <ALL>",
                parse(strictNot(ALL.withComments(comments(new PlComment("/* BE comment */", BEFORE_EXPRESSION))))
                        .withComments(comments(new PlComment("/* C1 VERY LONG VERY LONG VERY LONG VERY LONG comment */", C1))).format(INLINE)).toString());

        assertEquals(
                "/* before all VERY LONG VERY LONG VERY LONG VERY LONG */ STRICT /* C1 VERY LONG VERY LONG VERY LONG VERY LONG comment */ NOT /* BE comment */ <ALL> /* after all VERY LONG VERY LONG VERY LONG VERY LONG */",
                parse(strictNot(ALL.withComments(comments(new PlComment("/* BE comment */", BEFORE_EXPRESSION))))
                        .withComments(comments(new PlComment("/* C1 VERY LONG VERY LONG VERY LONG VERY LONG comment */", C1),
                                new PlComment("/* before all VERY LONG VERY LONG VERY LONG VERY LONG */", BEFORE_EXPRESSION),
                                new PlComment("/* after all VERY LONG VERY LONG VERY LONG VERY LONG */", AFTER_EXPRESSION)))
                        .format(INLINE)).toString());

    }

    @Test
    void testNegPrettyFormatComments() {
        assertEquals("/* comment before expression */\nNOT <ALL>", parse(not(ALL).withComments(COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("NOT\n    /* comment before expression */\n    <ALL>",
                parse(not(ALL.withComments(COMMENT_BEFORE_EXPR)).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("/* */ NOT <ALL>", parse(not(ALL).withComments(EMPTY_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("/* comment 1 before expression */\n/* comment 2 before expression */\nNOT <ALL>",
                parse(not(ALL).withComments(TWO_COMMENTS_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("/* comment VERY LONG VERY LONG VERY LONG VERY LONG\n   before expression */\nNOT <ALL>",
                parse(not(ALL).withComments(MULTI_LINE_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY\n   LONG before expression */\n/* comment 2 VERY LONG VERY LONG VERY LONG VERY\n   LONG before expression */\nNOT <ALL>",
                parse(not(ALL).withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("NOT <ALL>\n    /* comment after expression */",
                parse(not(ALL).withComments(COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("NOT <ALL>\n    /* comment 1 after expression */\n    /* comment 2 after expression */",
                parse(not(ALL).withComments(TWO_COMMENTS_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("NOT <ALL>\n    /* comment VERY LONG VERY LONG VERY LONG VERY LONG\n       after expression */",
                parse(not(ALL).withComments(MULTI_LINE_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals(
                "NOT <ALL>\n    /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n       VERY LONG after expression */\n    /* comment 2 after expression */",
                parse(not(ALL).withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY\n   LONG before expression */\nNOT <ALL>\n    /* comment 2 VERY LONG VERY LONG VERY LONG VERY\n       LONG after expression */",
                parse(not(ALL).withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_AND_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("NOT /* */ <ALL>", parse(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/* */"))).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("NOT /* */ <ALL>", parse(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/*\n\n\n  */"))).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("NOT NOT /* */ <ALL>", parse(not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/**/")))).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("NOT NOT /* */ <ALL>",
                parse(not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/*\n\n\n  */")))).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("NOT NOT /* */ <ALL> /* after all */", parse(not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/*\n\n\n  */"))))
                .withComments(comments(AFTER_EXPRESSION, "/* after all */")).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                NOT /* comment BE */ NOT
                        /* VERY LONG VERY LONG VERY LONG VERY LONG VERY
                           LONG VERY LONG VERY LONG VERY LONG */
                        <ALL>""", parse(
                not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/* VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")))
                        .withComments(SHORT_COMMENT_BEFORE_EXPR)).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                NOT
                    /* comment before expression */
                    NOT
                        /* VERY LONG VERY LONG VERY LONG VERY LONG VERY
                           LONG VERY LONG VERY LONG VERY LONG */
                        <ALL>""", parse(
                not(not(ALL.withComments(comments(BEFORE_EXPRESSION, "/* VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */")))
                        .withComments(COMMENT_BEFORE_EXPR)).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("STRICT /* */ NOT <ALL>", parse(strictNot(ALL).withComments(comments(C1, "/**/")).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                STRICT
                /* C1 VERY LONG VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG comment */
                NOT /* BE comment */ <ALL>""",
                parse(strictNot(ALL.withComments(comments(new PlComment("/* BE comment */", BEFORE_EXPRESSION))))
                        .withComments(comments(new PlComment("/* C1 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG comment */", C1)))
                        .format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                /* before all VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG VERY LONG */
                STRICT
                /* C1 VERY LONG VERY LONG VERY LONG VERY LONG VERY
                   LONG VERY LONG comment */
                NOT /* BE comment */ <ALL>
                    /* after all VERY LONG VERY LONG VERY LONG VERY
                       LONG VERY LONG VERY LONG */""",
                parse(strictNot(ALL.withComments(comments(new PlComment("/* BE comment */", BEFORE_EXPRESSION))))
                        .withComments(comments(new PlComment("/* C1 VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG comment */", C1),
                                new PlComment("/* before all VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */", BEFORE_EXPRESSION),
                                new PlComment("/* after all VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG VERY LONG */", AFTER_EXPRESSION)))
                        .format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testSpecialSetBasics() {

        assertEquals("<ALL>", parse(ALL.toString()).toString());
        assertEquals("<ALL>", parse(ALL.format(FormatStyle.PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("<NONE>", parse(NONE.toString()).toString());
        assertEquals("<NONE>", parse(NONE.format(FormatStyle.PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testSpecialSetInlineFormatComments() {
        assertEquals("/* comment BE */ <ALL>", parse(ALL.withComments(SHORT_COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("/* */ <ALL>", parse(ALL.withComments(EMPTY_COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("/* comment 1 before expression */ /* comment 2 before expression */ <ALL>",
                parse(ALL.withComments(TWO_COMMENTS_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals("/* comment VERY LONG VERY LONG VERY LONG VERY LONG before expression */ <ALL>",
                parse(ALL.withComments(MULTI_LINE_COMMENT_BEFORE_EXPR).format(INLINE)).toString());
        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY LONG before expression */ /* comment 2 VERY LONG VERY LONG VERY LONG VERY LONG before expression */ <ALL>",
                parse(ALL.withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_EXPR).format(INLINE)).toString());

        assertEquals("<ALL> /* comment after expression */", parse(ALL.withComments(COMMENT_AFTER_EXPR).format(INLINE)).toString());
        assertEquals("<ALL> /* comment 1 after expression */ /* comment 2 after expression */",
                parse(ALL.withComments(TWO_COMMENTS_AFTER_EXPR).format(INLINE)).toString());
        assertEquals("<ALL> /* comment VERY LONG VERY LONG VERY LONG VERY LONG after expression */",
                parse(ALL.withComments(MULTI_LINE_COMMENT_AFTER_EXPR).format(INLINE)).toString());

        assertEquals("<ALL> /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                parse(ALL.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE)).toString());

        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY LONG before expression */ <ALL> /* comment 2 VERY LONG VERY LONG VERY LONG VERY LONG after expression */",
                parse(ALL.withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_AND_AFTER_EXPR).format(INLINE)).toString());

    }

    @Test
    void testSpecialSetPrettyFormatComments() {
        assertEquals("/* comment BE */ <ALL>", parse(ALL.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("/* */ <ALL>", parse(ALL.withComments(EMPTY_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("/* comment 1 before expression */\n/* comment 2 before expression */\n<ALL>",
                parse(ALL.withComments(TWO_COMMENTS_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("/* comment VERY LONG VERY LONG VERY LONG VERY LONG\n   before expression */\n<ALL>",
                parse(ALL.withComments(MULTI_LINE_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals(
                "/* comment 1 VERY LONG VERY LONG VERY LONG VERY\n   LONG before expression */\n/* comment 2 VERY LONG VERY LONG VERY LONG VERY\n   LONG before expression */\n<ALL>",
                parse(ALL.withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("<ALL>\n/* comment after expression */", parse(ALL.withComments(COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("<ALL>\n/* comment 1 after expression */\n/* comment 2 after expression */",
                parse(ALL.withComments(TWO_COMMENTS_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));
        assertEquals("<ALL>\n/* comment VERY LONG VERY LONG VERY LONG VERY LONG\n   after expression */",
                parse(ALL.withComments(MULTI_LINE_COMMENT_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("<ALL>\n/* comment 1 \"    \" VERY LONG VERY LONG VERY LONG\n   VERY LONG after expression */\n/* comment 2 after expression */",
                parse(ALL.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT)).format(PRETTY_PRINT));

        assertEquals("""
                /* comment 1 VERY LONG VERY LONG VERY LONG VERY
                   LONG before expression */
                <ALL>
                /* comment 2 VERY LONG VERY LONG VERY LONG VERY
                   LONG after expression */""",
                parse(ALL.withComments(TWO_MULTI_LINE_COMMENTS_BEFORE_AND_AFTER_EXPR).format(PRETTY_PRINT)).format(PRETTY_PRINT));

    }

    @Test
    void testCommentsOnExtraBraces() {
        String expression = "q=1 AND ( /* da */ (u=c OR ( ( a=b OR c=d OR e=f ) AND ( (g=h OR i=j ) OR (k=l AND m=n ) ) AND (o=p AND (q=r OR s=t ) /* comment */ ) )))";

        PlExpression<?> expr = parse(expression);

        assertEquals(expr.toString(), parse(expr.format(FormatStyle.INLINE)).toString());

    }

    private static PlExpression<?> parse(String source) {
        return PlExpressionBuilder.stringToExpression(source).getResultExpression();
    }

    private static void assertCurbOutput(String exprTemplate, PlCombinedExpression delegate, FormatStyle style) {

        int bound = 0;
        for (PlCurbOperator operator : PlCurbOperator.values()) {
            PlCurbExpression curb = new PlCurbExpression(delegate, operator, bound, null);
            String expected = exprTemplate.replace("${operator}", operator.operatorString).replace("${bound}", String.valueOf(bound));

            if (style == INLINE) {
                assertEquals(expected, parse(curb.toString()).toString());
            }
            assertEquals(expected, parse(curb.format(style)).format(style));
            bound++;
        }

    }

}
