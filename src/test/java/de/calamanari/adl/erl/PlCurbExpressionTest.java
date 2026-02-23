//@formatter:off
/*
 * PlCurbExpressionTest
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.AudlangField;
import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.erl.PlComment.Position;
import de.calamanari.adl.erl.PlCurbExpression.PlCurbOperator;
import de.calamanari.adl.util.JsonUtils;

import static de.calamanari.adl.CombinedExpressionType.AND;
import static de.calamanari.adl.CombinedExpressionType.OR;
import static de.calamanari.adl.FormatStyle.INLINE;
import static de.calamanari.adl.FormatStyle.PRETTY_PRINT;
import static de.calamanari.adl.erl.PlComment.Position.AFTER_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.C1;
import static de.calamanari.adl.erl.PlMatchOperator.ANY_OF;
import static de.calamanari.adl.erl.PlMatchOperator.CONTAINS_ANY_OF;
import static de.calamanari.adl.erl.PlMatchOperator.EQUALS;
import static de.calamanari.adl.erl.PlMatchOperator.GREATER_THAN_OR_EQUALS;
import static de.calamanari.adl.erl.PlMatchOperator.LESS_THAN;
import static de.calamanari.adl.erl.PlMatchOperator.NOT_EQUALS;
import static de.calamanari.adl.erl.PlMatchOperator.STRICT_NOT_EQUALS;
import static de.calamanari.adl.erl.SamplePlExpressions.ALL;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_AFTER_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_BEFORE_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.NONE;
import static de.calamanari.adl.erl.SamplePlExpressions.SHORT_COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.SHORT_COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C1;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_BEFORE_AND_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION;
import static de.calamanari.adl.erl.SamplePlExpressions.and;
import static de.calamanari.adl.erl.SamplePlExpressions.comments;
import static de.calamanari.adl.erl.SamplePlExpressions.lop;
import static de.calamanari.adl.erl.SamplePlExpressions.match;
import static de.calamanari.adl.erl.SamplePlExpressions.or;
import static de.calamanari.adl.erl.SamplePlExpressions.rop;
import static de.calamanari.adl.erl.SamplePlExpressions.vop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class PlCurbExpressionTest {

    @Test
    void testBasicCombinationsInline() {

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

        assertEquals("/* comment before expression */ CURB (a = 1 OR b = 2) = 2", expr.withComments(COMMENT_BEFORE_EXPR).toString());
        assertEquals("/* comment before expression */ CURB (a = 1 OR b = 2) = 2", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));

        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).toString());
        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));

        assertEquals("/* comment 1 before expression */ CURB (a = 1 OR b = 2) = 2 /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).toString());
        assertEquals("/* comment 1 before expression */ CURB (a = 1 OR b = 2) = 2 /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).format(INLINE));

        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment 1 after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_AFTER_EXPR).toString());
        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment 1 after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_AFTER_EXPR).format(INLINE));

        assertEquals(
                "CURB (a = 1 OR b = 2) = 2 /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).toString());
        assertEquals(
                "CURB (a = 1 OR b = 2) = 2 /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));

    }

    @Test
    void testWithCommentsInline() {

        PlCombinedExpression innerOr = or(match("a", EQUALS, "1"), match("b", EQUALS, "2"));
        PlCurbExpression expr = new PlCurbExpression(innerOr, PlCurbOperator.EQUALS, 2, null);

        assertEquals("/* comment before expression */ CURB (a = 1 OR b = 2) = 2", expr.withComments(COMMENT_BEFORE_EXPR).toString());
        assertEquals("/* comment before expression */ CURB (a = 1 OR b = 2) = 2", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));

        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).toString());
        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));

        assertEquals("/* comment 1 before expression */ CURB (a = 1 OR b = 2) = 2 /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).toString());
        assertEquals("/* comment 1 before expression */ CURB (a = 1 OR b = 2) = 2 /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).format(INLINE));

        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment 1 after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_AFTER_EXPR).toString());
        assertEquals("CURB (a = 1 OR b = 2) = 2 /* comment 1 after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_AFTER_EXPR).format(INLINE));

        assertEquals(
                "CURB (a = 1 OR b = 2) = 2 /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).toString());
        assertEquals(
                "CURB (a = 1 OR b = 2) = 2 /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));

        PlCurbExpression expr2 = new PlCurbExpression(innerOr.withComments(SHORT_COMMENT_BEFORE_EXPR), PlCurbOperator.EQUALS, 2, null);

        assertEquals("CURB /* comment BE */ (a = 1 OR b = 2) = 2", expr2.format(INLINE));

        PlCurbExpression expr3 = new PlCurbExpression(innerOr.withComments(SHORT_COMMENT_AFTER_EXPR), PlCurbOperator.EQUALS, 2, null);

        assertEquals("CURB (a = 1 OR b = 2) /* comment AE */ = 2", expr3.format(INLINE));

        assertEquals("CURB (a = 1 OR b = 2) = /* comment C1 */ 2", expr.withComments(SINGLE_COMMENT_C1).format(INLINE));

    }

    @Test
    void testBasicCombinationsPrettyPrint() {

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
    void testWithCommentsPrettyPrint() {
        PlCombinedExpression innerOr = or(match("a", EQUALS, "1"), match("b", EQUALS, "2"));

        PlCurbExpression expr = new PlCurbExpression(innerOr, PlCurbOperator.EQUALS, 2, null);

        assertEquals("""
                /* comment before expression */
                CURB (
                        a = 1
                     OR b = 2
                    ) = 2""", expr.withComments(COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    ) = 2
                /* comment after expression */""", expr.withComments(COMMENT_AFTER_EXPR).format(PRETTY_PRINT));

        assertEquals("""
                /* comment 1 before expression */
                CURB (
                        a = 1
                     OR b = 2
                    ) = 2
                /* comment 2 after expression */""", expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).format(PRETTY_PRINT));

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    ) = 2
                /* comment 1 after expression */
                /* comment 2 after expression */""", expr.withComments(TWO_COMMENTS_AFTER_EXPR).format(PRETTY_PRINT));

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    ) = 2
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""", expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));

        expr = new PlCurbExpression(innerOr.withComments(SHORT_COMMENT_BEFORE_EXPR), PlCurbOperator.EQUALS, 2, null);

        assertEquals("""
                CURB /* comment BE */
                    (
                        a = 1
                     OR b = 2
                    ) = 2""", expr.format(PRETTY_PRINT));

        expr = new PlCurbExpression(innerOr.withComments(comments(BEFORE_EXPRESSION, "/* longer comment before members */")), PlCurbOperator.EQUALS, 2, null);

        assertEquals("""
                CURB
                    /* longer comment before members */
                    (
                        a = 1
                     OR b = 2
                    ) = 2""", expr.format(PRETTY_PRINT));

        expr = new PlCurbExpression(innerOr.withComments(SHORT_COMMENT_AFTER_EXPR), PlCurbOperator.EQUALS, 2, null);

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    ) /* comment AE */ = 2""", expr.format(PRETTY_PRINT));

        expr = new PlCurbExpression(innerOr.withComments(comments(AFTER_EXPRESSION, "/* longer comment before operator */")), PlCurbOperator.EQUALS, 2, null);

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    )
                    /* longer comment before operator */
                    = 2""", expr.format(PRETTY_PRINT));

        expr = new PlCurbExpression(innerOr, PlCurbOperator.EQUALS, 2, null);

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    ) = /* comment C1 */ 2""", expr.withComments(SINGLE_COMMENT_C1).format(PRETTY_PRINT));

        assertEquals("""
                CURB (
                        a = 1
                     OR b = 2
                    ) =
                    /* longer comment before bound */
                    2""", expr.withComments(comments(C1, "/* longer comment before bound */")).format(PRETTY_PRINT));

    }

    @Test
    void testNestedCurbInline() {
        PlCurbExpression inner = new PlCurbExpression(or(match("c", EQUALS, "3"), match("d", EQUALS, "4")), PlCurbOperator.GREATER_THAN_OR_EQUALS, 1, null);

        assertCurbOutput("CURB (a = 1 OR b = 2 OR CURB (c = 3 OR d = 4) >= 1) ${operator} ${bound}",
                or(match("a", EQUALS, "1"), match("b", EQUALS, "2"), inner), INLINE);

        assertCurbOutput(
                "CURB ( /* comment before expression */ a = 1 OR b = 2 /* comment after expression */ OR /* comment before expression */ CURB (c = 3 OR d = 4) >= 1) ${operator} ${bound}",
                or(match("a", EQUALS, "1").withComments(COMMENT_BEFORE_EXPR), match("b", EQUALS, "2").withComments(COMMENT_AFTER_EXPR),
                        inner.withComments(COMMENT_BEFORE_EXPR)),
                INLINE);

        assertCurbOutput("CURB ( /* comment BE */ a = 1 OR b = 2 /* comment AE */ OR /* comment BE */ CURB (c = 3 OR d = 4) >= 1) ${operator} ${bound}",
                or(match("a", EQUALS, "1").withComments(SHORT_COMMENT_BEFORE_EXPR), match("b", EQUALS, "2").withComments(SHORT_COMMENT_AFTER_EXPR),
                        inner.withComments(SHORT_COMMENT_BEFORE_EXPR)),
                INLINE);

    }

    @Test
    void testNestedCurbPrettyPrint() {
        PlCurbExpression inner = new PlCurbExpression(or(match("c", EQUALS, "3"), match("d", EQUALS, "4")), PlCurbOperator.GREATER_THAN_OR_EQUALS, 1, null);

        assertCurbOutput("""
                CURB (
                        a = 1
                     OR b = 2
                     OR CURB (
                            c = 3
                         OR d = 4
                        ) >= 1
                    ) ${operator} ${bound}""", or(match("a", EQUALS, "1"), match("b", EQUALS, "2"), inner), PRETTY_PRINT);

        assertCurbOutput("""
                CURB (
                        /* comment before expression */
                        a = 1
                     OR b = 2
                        /* comment after expression */
                     OR
                        /* comment before expression */
                        CURB (
                                c = 3
                             OR d = 4
                            ) >= 1
                    ) ${operator} ${bound}""", or(match("a", EQUALS, "1").withComments(COMMENT_BEFORE_EXPR),
                match("b", EQUALS, "2").withComments(COMMENT_AFTER_EXPR), inner.withComments(COMMENT_BEFORE_EXPR)), PRETTY_PRINT);

        assertCurbOutput("""
                CURB (
                        /* comment BE */ a = 1
                     OR b = 2 /* comment AE */
                     OR /* comment BE */ CURB (
                            c = 3
                         OR d = 4
                        ) >= 1
                    ) ${operator} ${bound}""", or(match("a", EQUALS, "1").withComments(SHORT_COMMENT_BEFORE_EXPR),
                match("b", EQUALS, "2").withComments(SHORT_COMMENT_AFTER_EXPR), inner.withComments(SHORT_COMMENT_BEFORE_EXPR)), PRETTY_PRINT);

    }

    @Test
    void testSpecialCases() {

        assertThrows(AudlangValidationException.class, () -> new PlCurbExpression(null, PlCurbOperator.EQUALS, 2, null));

        final List<PlExpression<?>> validMembers = Arrays.asList(ALL, NONE);

        final PlCombinedExpression invalidAndMembers = new PlCombinedExpression(AND, validMembers, null);

        assertThrows(AudlangValidationException.class, () -> new PlCurbExpression(invalidAndMembers, PlCurbOperator.EQUALS, 2, null));

        final PlCombinedExpression validOrMembers = new PlCombinedExpression(OR, validMembers, null);

        assertThrows(AudlangValidationException.class, () -> new PlCurbExpression(validOrMembers, null, 2, null));

        assertThrows(AudlangValidationException.class, () -> new PlCurbExpression(validOrMembers, PlCurbOperator.EQUALS, -1, null));

        for (Position commentPosition : Position.values()) {
            if (!Arrays.asList(BEFORE_EXPRESSION, AFTER_EXPRESSION, C1).contains(commentPosition)) {
                final List<PlComment> cml = comments(commentPosition, "/* comment */");
                assertThrows(AudlangValidationException.class, () -> new PlCurbExpression(validOrMembers, PlCurbOperator.EQUALS, 2, cml));
            }
        }

    }

    @Test
    void testResolveHigherLanguageFeatures() {

        PlCurbExpression inner = new PlCurbExpression(
                or(match("c", EQUALS, "3"), match("d", EQUALS, "4"),
                        new PlCurbExpression(or(match("r", EQUALS, "6"), match("s", EQUALS, "7")), PlCurbOperator.EQUALS, 1, null)),
                PlCurbOperator.LESS_THAN, 3, null);

        PlExpression<?> curb = or(match("a", EQUALS, "1").withComments(COMMENT_BEFORE_EXPR), match("b", EQUALS, "2").withComments(COMMENT_AFTER_EXPR),
                inner.withComments(COMMENT_BEFORE_EXPR));

        assertEquals("""
                a = 1
                OR b = 2
                OR (
                        (
                            STRICT NOT c = 3
                         OR c IS UNKNOWN
                        )
                     OR (
                            STRICT NOT d = 4
                         OR d IS UNKNOWN
                        )
                     OR (
                            (
                                (
                                    STRICT NOT r = 6
                                 OR r IS UNKNOWN
                                )
                            AND (
                                    STRICT NOT s = 7
                                 OR s IS UNKNOWN
                                )
                            )
                         OR (
                                r = 6
                            AND s = 7
                            )
                        )
                    )""", curb.resolveHigherLanguageFeatures().format(PRETTY_PRINT));

    }

    @Test
    void testCommentHandling() {
        PlCurbExpression expr = new PlCurbExpression(or(match("c", EQUALS, "3"), match("d", EQUALS, "4")), PlCurbOperator.GREATER_THAN_OR_EQUALS, 1, null);

        PlCurbExpression expr2 = new PlCurbExpression(
                or(match("c", EQUALS, "3"), match("d", EQUALS, vop("4").withComments(COMMENT_AFTER_OPERAND)).withComments(COMMENT_BEFORE_EXPR)),
                PlCurbOperator.GREATER_THAN_OR_EQUALS, 1, null).withComments(COMMENT_AFTER_EXPR);

        assertNotEquals(expr, expr2);
        assertEquals(expr, expr.stripComments());

        assertEquals(expr, expr2.stripComments());

        assertSame(expr, expr.withComments(null));
        assertSame(expr, expr.withComments(Collections.emptyList()));

        PlCurbExpression expr3 = expr.withComments(SHORT_COMMENT_AFTER_EXPR);

        assertSame(expr3, expr3.withComments(SHORT_COMMENT_AFTER_EXPR));

    }

    @Test
    void testEqualsHashCode() {

        PlCurbExpression expr = new PlCurbExpression(or(match("a", EQUALS, "1").withComments(COMMENT_BEFORE_EXPR), match("b", EQUALS, "2")),
                PlCurbOperator.EQUALS, 2, null);
        PlCurbExpression expr2 = new PlCurbExpression(or(match("a", EQUALS, "1").withComments(COMMENT_BEFORE_EXPR), match("b", EQUALS, "2")),
                PlCurbOperator.EQUALS, 2, null);

        assertNotSame(expr, expr2);

        assertEquals(expr, expr2);

        assertEquals(expr.hashCode(), expr2.hashCode());

    }

    @Test
    void testGetAllFieldsAndComments() {

        // @formatter:off
        PlCurbExpression inner = new PlCurbExpression(
                or(
                        match("a", EQUALS, rop("x").withComments(TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND)), 
                        match("d", EQUALS, "4")), 
                PlCurbOperator.GREATER_THAN_OR_EQUALS, 1, null);
        
        PlCurbExpression expr = new PlCurbExpression(
                or(
                        match("a", EQUALS, vop("1").withComments(COMMENT_BEFORE_OPERAND)).withComments(COMMENT_BEFORE_EXPR),
                        match("b", EQUALS, "2").withComments(COMMENT_AFTER_EXPR), 
                        inner.withComments(COMMENT_BEFORE_EXPR)
                    ), PlCurbOperator.GREATER_THAN, 1, null);
        
        
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
        
        PlCurbExpression expr = new PlCurbExpression(
                or(
                        match("a", EQUALS, vop("1").withComments(COMMENT_BEFORE_OPERAND)).withComments(COMMENT_BEFORE_EXPR),
                        match("b", EQUALS, "2").withComments(COMMENT_AFTER_EXPR), 
                        inner.withComments(COMMENT_BEFORE_EXPR)
                    ), PlCurbOperator.GREATER_THAN, 1, null);
        
        
        // @formatter:on

        String json = JsonUtils.writeAsJsonString(expr, true);

        PlExpression<?> res = JsonUtils.readFromJsonString(json, PlExpression.class);

        assertEquals(expr, res);
    }

    private static void assertCurbOutput(String exprTemplate, PlCombinedExpression delegate, FormatStyle style) {

        int bound = 0;
        for (PlCurbOperator operator : PlCurbOperator.values()) {
            PlCurbExpression curb = new PlCurbExpression(delegate, operator, bound, null);
            String expected = exprTemplate.replace("${operator}", operator.operatorString).replace("${bound}", String.valueOf(bound));

            if (style == INLINE) {
                assertEquals(expected, curb.toString());
            }
            assertEquals(expected, curb.format(style));
            bound++;
        }

    }

}
