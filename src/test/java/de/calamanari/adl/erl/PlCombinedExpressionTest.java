//@formatter:off
/*
 * PlCombinedExpressionTest
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

import static de.calamanari.adl.CombinedExpressionType.AND;
import static de.calamanari.adl.CombinedExpressionType.OR;
import static de.calamanari.adl.FormatStyle.INLINE;
import static de.calamanari.adl.FormatStyle.PRETTY_PRINT;
import static de.calamanari.adl.erl.PlComment.Position.AFTER_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.AFTER_OPERAND;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_OPERAND;
import static de.calamanari.adl.erl.PlMatchOperator.ANY_OF;
import static de.calamanari.adl.erl.PlMatchOperator.CONTAINS_ANY_OF;
import static de.calamanari.adl.erl.PlMatchOperator.EQUALS;
import static de.calamanari.adl.erl.PlMatchOperator.GREATER_THAN_OR_EQUALS;
import static de.calamanari.adl.erl.PlMatchOperator.LESS_THAN;
import static de.calamanari.adl.erl.PlMatchOperator.NOT_ANY_OF;
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
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_BEFORE_AND_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION;
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
import de.calamanari.adl.cnv.StandardConversions;
import de.calamanari.adl.erl.PlComment.Position;
import de.calamanari.adl.util.JsonUtils;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class PlCombinedExpressionTest {

    static final Logger LOGGER = LoggerFactory.getLogger(PlCombinedExpressionTest.class);

    @Test
    void testBasicCombinationsInline() {

        assertEquals("a = 1 AND b = 2", and(match("a", EQUALS, "1"), match("b", EQUALS, "2")).toString());
        assertEquals("a = 1 AND b = 2", and(match("a", EQUALS, "1"), match("b", EQUALS, "2")).format(INLINE));

        assertEquals("a = 1 OR b = 2", or(match("a", EQUALS, "1"), match("b", EQUALS, "2")).toString());
        assertEquals("a = 1 OR b = 2", or(match("a", EQUALS, "1"), match("b", EQUALS, "2")).format(INLINE));

        assertEquals("a = 1 AND b != 2", and(match("a", EQUALS, "1"), match("b", NOT_EQUALS, "2")).toString());
        assertEquals("a = 1 AND b != 2", and(match("a", EQUALS, "1"), match("b", NOT_EQUALS, "2")).format(INLINE));

        assertEquals("a = 1 AND STRICT b != 2", and(match("a", EQUALS, "1"), match("b", STRICT_NOT_EQUALS, "2")).toString());
        assertEquals("a = 1 AND STRICT b != 2", and(match("a", EQUALS, "1"), match("b", STRICT_NOT_EQUALS, "2")).format(INLINE));

        assertEquals("a = 1 AND NOT b = 2", and(match("a", EQUALS, "1"), not(match("b", EQUALS, "2"))).toString());
        assertEquals("a = 1 AND NOT b = 2", and(match("a", EQUALS, "1"), not(match("b", EQUALS, "2"))).format(INLINE));

        assertEquals("a = 1 AND b ANY OF (2, 3, 4)", and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4")))).toString());
        assertEquals("a = 1 AND b ANY OF (2, 3, 4)", and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4")))).format(INLINE));

        PlCombinedExpression expr = and(match("a", EQUALS, "1"), match("b", EQUALS, "2"));

        assertEquals("/* comment before expression */ a = 1 AND b = 2", expr.withComments(COMMENT_BEFORE_EXPR).toString());
        assertEquals("/* comment before expression */ a = 1 AND b = 2", expr.withComments(COMMENT_BEFORE_EXPR).format(INLINE));
        assertEquals("a = 1 AND b = 2 /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).toString());
        assertEquals("a = 1 AND b = 2 /* comment after expression */", expr.withComments(COMMENT_AFTER_EXPR).format(INLINE));
        assertEquals("/* comment 1 before expression */ a = 1 AND b = 2 /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).toString());
        assertEquals("/* comment 1 before expression */ a = 1 AND b = 2 /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).format(INLINE));

        assertEquals("a = 1 AND b = 2 /* comment 1 after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_AFTER_EXPR).toString());
        assertEquals("a = 1 AND b = 2 /* comment 1 after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_AFTER_EXPR).format(INLINE));

        assertEquals("a = 1 AND b = 2 /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).toString());
        assertEquals("a = 1 AND b = 2 /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(INLINE));

    }

    @Test
    void testBasicCombinationsPrettyPrint() {

        assertEquals("a = 1\nAND b = 2", and(match("a", EQUALS, "1"), match("b", EQUALS, "2")).format(PRETTY_PRINT));

        assertEquals("a = 1\nOR b = 2", or(match("a", EQUALS, "1"), match("b", EQUALS, "2")).format(PRETTY_PRINT));

        assertEquals("a = 1\nAND b != 2", and(match("a", EQUALS, "1"), match("b", NOT_EQUALS, "2")).format(PRETTY_PRINT));

        assertEquals("a = 1\nAND STRICT b != 2", and(match("a", EQUALS, "1"), match("b", STRICT_NOT_EQUALS, "2")).format(PRETTY_PRINT));

        assertEquals("a = 1\nAND NOT b = 2", and(match("a", EQUALS, "1"), not(match("b", EQUALS, "2"))).format(PRETTY_PRINT));

        assertEquals("a = 1\nAND b ANY OF (2, 3, 4)", and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4")))).format(PRETTY_PRINT));
        assertEquals("""
                a = 1
                AND b ANY OF (
                        2,
                        3,
                        4,
                        5,
                        6
                    )""", and(match("a", EQUALS, "1"), match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"), vop("5"), vop("6")))).format(PRETTY_PRINT));

        PlCombinedExpression expr = and(match("a", EQUALS, "1"), match("b", EQUALS, "2"));

        assertEquals("""
                /* comment before expression */
                a = 1
                AND b = 2""", expr.withComments(COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("""
                /* comment BE */
                a = 1
                AND b = 2""", expr.withComments(SHORT_COMMENT_BEFORE_EXPR).format(PRETTY_PRINT));
        assertEquals("""
                a = 1
                AND b = 2
                /* comment after expression */""", expr.withComments(COMMENT_AFTER_EXPR).format(PRETTY_PRINT));
        assertEquals("""
                /* comment 1 before expression */
                a = 1
                AND b = 2
                /* comment 2 after expression */""", expr.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR).format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND b = 2
                /* comment 1 after expression */
                /* comment 2 after expression */""", expr.withComments(TWO_COMMENTS_AFTER_EXPR).format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND b = 2
                /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                   VERY LONG after expression */
                /* comment 2 after expression */""", expr.withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION).format(PRETTY_PRINT));

    }

    @Test
    void testNestingInline() {

        assertEquals("a = 1 AND (b = 2 OR c = 3)", and(match("a", EQUALS, "1"), or(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).toString());
        assertEquals("a = 1 AND (b = 2 OR c = 3)", and(match("a", EQUALS, "1"), or(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(INLINE));

        assertEquals("a = 1 AND (b = 2 AND c = 3)", and(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).toString());
        assertEquals("a = 1 AND (b = 2 AND c = 3)", and(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(INLINE));

        assertEquals("a = 1 OR (b = 2 AND c = 3)", or(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).toString());
        assertEquals("a = 1 OR (b = 2 AND c = 3)", or(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(INLINE));

        assertEquals("a = 1 OR (b = 2 OR c = 3)", or(match("a", EQUALS, "1"), or(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).toString());
        assertEquals("a = 1 OR (b = 2 OR c = 3)", or(match("a", EQUALS, "1"), or(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(INLINE));

        assertEquals("(b = 2 OR c = 3) AND a = 1", and(or(match("b", EQUALS, "2"), match("c", EQUALS, "3")), match("a", EQUALS, "1")).toString());
        assertEquals("(b = 2 OR c = 3) AND a = 1", and(or(match("b", EQUALS, "2"), match("c", EQUALS, "3")), match("a", EQUALS, "1")).format(INLINE));

        assertEquals("(b = 2 OR c = 3) AND (a = 1 OR z = 4)",
                and(or(match("b", EQUALS, "2"), match("c", EQUALS, "3")), or(match("a", EQUALS, "1"), match("z", EQUALS, "4"))).toString());
        assertEquals("(b = 2 OR c = 3) AND (a = 1 OR z = 4)",
                and(or(match("b", EQUALS, "2"), match("c", EQUALS, "3")), or(match("a", EQUALS, "1"), match("z", EQUALS, "4"))).format(INLINE));

        assertEquals("""
                a = 1 AND (b ANY OF (2, 3, 4) OR c ANY OF (2, 3, 4) )""",
                and(match("a", EQUALS, "1"), or(match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))), match("c", ANY_OF, lop(vop("2"), vop("3"), vop("4")))))
                        .toString());
        assertEquals("""
                a = 1 AND (b ANY OF (2, 3, 4) OR c ANY OF (2, 3, 4) )""",
                and(match("a", EQUALS, "1"), or(match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))), match("c", ANY_OF, lop(vop("2"), vop("3"), vop("4")))))
                        .format(INLINE));

        assertEquals("""
                a = 1 AND (b CONTAINS ANY OF (s2, s3, s4, s5, s6) OR c CONTAINS ANY OF (s2, s3, s4) )""",
                and(match("a", EQUALS, "1"), or(match("b", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"), vop("s5"), vop("s6"))),
                        match("c", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"))))).toString());
        assertEquals("""
                a = 1 AND (b CONTAINS ANY OF (s2, s3, s4, s5, s6) OR c CONTAINS ANY OF (s2, s3, s4) )""",
                and(match("a", EQUALS, "1"), or(match("b", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"), vop("s5"), vop("s6"))),
                        match("c", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"))))).format(INLINE));

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
                expr.toString());

        assertEquals("a = 1 AND ( ( (foo < high AND foo >= low) AND b = 2 AND e = 9 AND q = 10) OR (c = 3 AND d = 5 AND (k = 4 OR l = 92) ) )",
                expr.format(INLINE));

    }

    @Test
    void testNestingPrettyPrint() {

        assertEquals("a = 1\nAND (\n        b = 2\n     OR c = 3\n    )",
                and(match("a", EQUALS, "1"), or(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(PRETTY_PRINT));

        assertEquals("a = 1\nAND (\n        b = 2\n    AND c = 3\n    )",
                and(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(PRETTY_PRINT));

        assertEquals("a = 1\nOR (\n        b = 2\n    AND c = 3\n    )",
                or(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(PRETTY_PRINT));

        assertEquals("a = 1\nOR (\n        b = 2\n     OR c = 3\n    )",
                or(match("a", EQUALS, "1"), or(match("b", EQUALS, "2"), match("c", EQUALS, "3"))).format(PRETTY_PRINT));

        assertEquals("(\n        b = 2\n     OR c = 3\n    )\nAND a = 1",
                and(or(match("b", EQUALS, "2"), match("c", EQUALS, "3")), match("a", EQUALS, "1")).format(PRETTY_PRINT));

        assertEquals("(\n        b = 2\n     OR c = 3\n    )\nAND (\n        a = 1\n     OR z = 4\n    )",
                and(or(match("b", EQUALS, "2"), match("c", EQUALS, "3")), or(match("a", EQUALS, "1"), match("z", EQUALS, "4"))).format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND (
                        b ANY OF (2, 3, 4)
                     OR c ANY OF (2, 3, 4)
                    )""",
                and(match("a", EQUALS, "1"), or(match("b", ANY_OF, lop(vop("2"), vop("3"), vop("4"))), match("c", ANY_OF, lop(vop("2"), vop("3"), vop("4")))))
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
                    )""", and(match("a", EQUALS, "1"), or(match("b", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"), vop("s5"), vop("s6"))),
                match("c", CONTAINS_ANY_OF, lop(vop("s2"), vop("s3"), vop("s4"))))).format(PRETTY_PRINT));

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
                    )""", expr.format(PRETTY_PRINT));

    }

    @Test
    void testNestingWithCommentsInline() {

        assertEquals("a = /* comment before operand */ 1 AND (b = 2 /* comment after operand */ OR c = 3)",
                and(match("a", EQUALS, vop("1").withComments(COMMENT_BEFORE_OPERAND)),
                        or(match("b", EQUALS, vop("2").withComments(COMMENT_AFTER_OPERAND)), match("c", EQUALS, "3"))).toString());
        assertEquals("a = /* comment before operand */ 1 AND (b = 2 /* comment after operand */ OR c = 3)",
                and(match("a", EQUALS, vop("1").withComments(COMMENT_BEFORE_OPERAND)),
                        or(match("b", EQUALS, vop("2").withComments(COMMENT_AFTER_OPERAND)), match("c", EQUALS, "3"))).format(INLINE));

        assertEquals("a = 1 OR /* comment 1 before expression */ (b = 2 AND c = 3) /* comment 2 after expression */",
                or(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3")).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR)).toString());

        assertEquals(
                "a = 1 OR (b = 2 AND c = 3) /* comment 1 \"    \" VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */",
                or(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3")).withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION))
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
                expr.toString());
        assertEquals(
                """
                        a = 1 /* comment after operand */ AND ( ( (foo < high AND foo >= /* comment before operand */ low) /* comment 1 "    " VERY LONG VERY LONG VERY LONG VERY LONG after expression */ /* comment 2 after expression */ AND b = 2 AND e ANY OF (v1, v2, v3) AND q NOT ANY OF (v1, v2, v3, /* comment 1 before operand */ v4 /* comment 2 after operand */, v5) ) OR ( /* comment BE */ c = 3 AND d = 5 AND (k = 4 OR l = 92 /* comment after expression */ ) ) )""",
                expr.format(INLINE));

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
                    )""", and(match("a", EQUALS, vop("1").withComments(COMMENT_BEFORE_OPERAND)),
                or(match("b", EQUALS, vop("2").withComments(COMMENT_AFTER_OPERAND)), match("c", EQUALS, "3"))).format(PRETTY_PRINT));

        assertEquals("""
                a = /* before */ 1 /* after */
                AND (
                        b = 2
                        /* comment after operand */
                     OR c = 3
                    )""",
                and(match("a", EQUALS, vop("1").withComments(comments(comments(BEFORE_OPERAND, "/* before */"), comments(AFTER_OPERAND, "/* after */")))),
                        or(match("b", EQUALS, vop("2").withComments(COMMENT_AFTER_OPERAND)), match("c", EQUALS, "3"))).format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                OR
                    /* comment 1 before expression */
                    (
                        b = 2
                    AND c = 3
                    )
                    /* comment 2 after expression */""",
                or(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3")).withComments(TWO_COMMENTS_BEFORE_AND_AFTER_EXPR))
                        .format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                OR (
                        b = 2
                    AND c = 3
                    )
                    /* comment 1 "    " VERY LONG VERY LONG VERY LONG
                       VERY LONG after expression */
                    /* comment 2 after expression */""",
                or(match("a", EQUALS, "1"), and(match("b", EQUALS, "2"), match("c", EQUALS, "3")).withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION))
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
                    )""", expr.format(PRETTY_PRINT));

    }

    @Test
    void testSpecialCases() {

        assertThrows(AudlangValidationException.class, () -> new PlCombinedExpression(AND, null, null));

        final List<PlExpression<?>> emptyMembers = Collections.emptyList();

        assertThrows(AudlangValidationException.class, () -> new PlCombinedExpression(OR, emptyMembers, null));

        final List<PlExpression<?>> singleMembers = Arrays.asList(ALL);

        assertThrows(AudlangValidationException.class, () -> new PlCombinedExpression(OR, singleMembers, null));

        final List<PlExpression<?>> validMembers = Arrays.asList(ALL, NONE);

        assertThrows(AudlangValidationException.class, () -> new PlCombinedExpression(null, validMembers, null));

        for (Position commentPosition : Position.values()) {
            if (commentPosition != BEFORE_EXPRESSION && commentPosition != AFTER_EXPRESSION) {
                final List<PlComment> cml = comments(commentPosition, "/* comment */");
                assertThrows(AudlangValidationException.class, () -> new PlCombinedExpression(AND, validMembers, cml));
            }
        }

    }

    @Test
    void testResolveHigherLanguageFeatures() {
        PlExpression<?> expr = and(match("a", EQUALS, "1"),
                match("q", NOT_ANY_OF, lop(vop("v1"), vop("v2"), vop("v3"), vop("v4").withComments(TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND), vop("v5"))));

        assertEquals(
                "a = 1 AND ( (STRICT NOT q = v1 OR q IS UNKNOWN) AND (STRICT NOT q = v2 OR q IS UNKNOWN) AND (STRICT NOT q = v3 OR q IS UNKNOWN) AND (STRICT NOT q = v4 OR q IS UNKNOWN) AND (STRICT NOT q = v5 OR q IS UNKNOWN) )",
                expr.resolveHigherLanguageFeatures().toString());

        expr = and(match("a", EQUALS, "1"),
                match("q", ANY_OF, lop(vop("v1"), vop("v2"), vop("v3"), vop("v4").withComments(TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND), vop("v5"))));

        assertEquals("a = 1 AND (q = v1 OR q = v2 OR q = v3 OR q = v4 OR q = v5)", expr.resolveHigherLanguageFeatures().toString());

        // @formatter:off
        expr = and(match("a", EQUALS, vop("1").withComments(comments(AFTER_OPERAND, "/* after (1) */"))), 
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
                a = 1
                AND (
                        (
                            (
                                foo < high
                            AND (
                                    foo > low
                                 OR foo = low
                                )
                            )
                        AND b = 2
                        AND (
                                e = v1
                             OR e = v2
                             OR e = v3
                            )
                        AND (
                                (
                                    STRICT NOT q = v1
                                 OR q IS UNKNOWN
                                )
                            AND (
                                    STRICT NOT q = v2
                                 OR q IS UNKNOWN
                                )
                            AND (
                                    STRICT NOT q = v3
                                 OR q IS UNKNOWN
                                )
                            AND (
                                    STRICT NOT q = v4
                                 OR q IS UNKNOWN
                                )
                            AND (
                                    STRICT NOT q = v5
                                 OR q IS UNKNOWN
                                )
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
                    )""", expr.resolveHigherLanguageFeatures().format(PRETTY_PRINT));

        // @formatter:off
        expr = and(match("a", EQUALS, vop("1").withComments(comments(AFTER_OPERAND, "/* after (1) */"))), 
                    or(
                       strictNot(and(and (match("foo", LESS_THAN, "high"),
                                match("foo", GREATER_THAN_OR_EQUALS, vop("low").withComments(comments(BEFORE_OPERAND, "/* before low */")))
                           ).withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION),
                           match("b", EQUALS, "2"), 
                           match("e", ANY_OF, lop(vop("v1"), vop("v2"), vop("v3"))),
                           match("q", NOT_ANY_OF, lop(vop("v1"), vop("v2"), vop("v3"), vop("v4").withComments(TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND), vop("v5")))
                       )), 
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
                a = 1
                AND (
                        (
                            (
                                STRICT NOT foo < high
                             OR (
                                    STRICT NOT foo > low
                                AND STRICT NOT foo = low
                                )
                            )
                         OR STRICT NOT b = 2
                         OR (
                                STRICT NOT e = v1
                            AND STRICT NOT e = v2
                            AND STRICT NOT e = v3
                            )
                         OR (
                                (
                                    q = v1
                                AND STRICT NOT q IS UNKNOWN
                                )
                             OR (
                                    q = v2
                                AND STRICT NOT q IS UNKNOWN
                                )
                             OR (
                                    q = v3
                                AND STRICT NOT q IS UNKNOWN
                                )
                             OR (
                                    q = v4
                                AND STRICT NOT q IS UNKNOWN
                                )
                             OR (
                                    q = v5
                                AND STRICT NOT q IS UNKNOWN
                                )
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
                    )""", expr.resolveHigherLanguageFeatures().format(PRETTY_PRINT));

        // @formatter:off
        expr = and(match("a", EQUALS, vop("1").withComments(comments(AFTER_OPERAND, "/* after (1) */"))), 
                    or(
                       not(and(and (match("foo", LESS_THAN, "high"),
                                match("foo", GREATER_THAN_OR_EQUALS, vop("low").withComments(comments(BEFORE_OPERAND, "/* before low */")))
                           ).withComments(TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION),
                           match("b", EQUALS, "2"), 
                           match("e", ANY_OF, lop(vop("v1"), vop("v2"), vop("v3"))),
                           match("q", NOT_ANY_OF, lop(vop("v1"), vop("v2"), vop("v3"), vop("v4").withComments(TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND), vop("v5")))
                       )), 
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
                a = 1
                AND (
                        (
                            (
                                (
                                    STRICT NOT foo < high
                                 OR foo IS UNKNOWN
                                )
                             OR (
                                    (
                                        STRICT NOT foo > low
                                     OR foo IS UNKNOWN
                                    )
                                AND (
                                        STRICT NOT foo = low
                                     OR foo IS UNKNOWN
                                    )
                                )
                            )
                         OR (
                                STRICT NOT b = 2
                             OR b IS UNKNOWN
                            )
                         OR (
                                (
                                    STRICT NOT e = v1
                                 OR e IS UNKNOWN
                                )
                            AND (
                                    STRICT NOT e = v2
                                 OR e IS UNKNOWN
                                )
                            AND (
                                    STRICT NOT e = v3
                                 OR e IS UNKNOWN
                                )
                            )
                         OR (
                                (
                                    (
                                        q = v1
                                     OR q IS UNKNOWN
                                    )
                                AND STRICT NOT q IS UNKNOWN
                                )
                             OR (
                                    (
                                        q = v2
                                     OR q IS UNKNOWN
                                    )
                                AND STRICT NOT q IS UNKNOWN
                                )
                             OR (
                                    (
                                        q = v3
                                     OR q IS UNKNOWN
                                    )
                                AND STRICT NOT q IS UNKNOWN
                                )
                             OR (
                                    (
                                        q = v4
                                     OR q IS UNKNOWN
                                    )
                                AND STRICT NOT q IS UNKNOWN
                                )
                             OR (
                                    (
                                        q = v5
                                     OR q IS UNKNOWN
                                    )
                                AND STRICT NOT q IS UNKNOWN
                                )
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
                    )""", expr.resolveHigherLanguageFeatures().format(PRETTY_PRINT));

    }

    @Test
    void testEqualsHashCode() {

        PlCombinedExpression expr = and(match("a", EQUALS, "1"), match("b", EQUALS, "2"));
        PlCombinedExpression expr2 = and(match("a", EQUALS, "1"), match("b", EQUALS, "2"));

        assertNotSame(expr, expr2);

        assertEquals(expr, expr2);

        assertEquals(expr.hashCode(), expr2.hashCode());

    }

    @Test
    void testCommentHandling() {
        PlCombinedExpression expr = new PlCombinedExpression(AND, Arrays.asList(match("c", EQUALS, "3"), match("d", EQUALS, "4")), null);

        PlCombinedExpression expr2 = new PlCombinedExpression(AND,
                Arrays.asList(match("c", EQUALS, "3"), match("d", EQUALS, vop("4").withComments(COMMENT_AFTER_OPERAND)).withComments(COMMENT_BEFORE_EXPR)),
                null).withComments(COMMENT_AFTER_EXPR);

        assertNotEquals(expr, expr2);
        assertEquals(expr, expr.stripComments());

        assertEquals(expr, expr2.stripComments());

        assertSame(expr, expr.withComments(null));
        assertSame(expr, expr.withComments(Collections.emptyList()));

        PlCombinedExpression expr3 = expr.withComments(SHORT_COMMENT_AFTER_EXPR);

        assertSame(expr3, expr3.withComments(SHORT_COMMENT_AFTER_EXPR));

    }

    @Test
    void testGetAllFieldsAndComments() {

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
                           match("a", EQUALS, "5"),
                           or (match("k", EQUALS, "4"),
                               match("l", EQUALS, "92").withComments(COMMENT_AFTER_EXPR),
                               match("a", EQUALS, rop("b"))
                           )
                       )
                    )
                );
        // @formatter:on
        List<String> expectedFieldNames = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "k", "l", "q", "foo"));
        Collections.sort(expectedFieldNames);
        assertEquals(expectedFieldNames, expr.allArgNames());

        List<PlExpression<?>> collectedExpressions = new ArrayList<>();
        expr.collectExpressions(e -> e instanceof PlMatchExpression m && m.argName().equals("a"), collectedExpressions);
        assertEquals(3, collectedExpressions.size());

        AudlangField field = expr.allFields().stream().filter(f -> f.argName().equals("a")).toList().get(0);
        assertEquals(Arrays.asList("1", "5"), field.values());
        assertEquals(Arrays.asList("b"), field.refArgNames());

        // @formatter:off
        List<String> expectedComments = new ArrayList<>(Arrays.asList(
                "/* after (1) */", "/* before low */",
                TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION.get(0).comment(), 
                TWO_COMMENTS_ONE_MULTI_LINE_AFTER_EXPRESSION.get(1).comment(),
                TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND.get(0).comment(), 
                TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND.get(1).comment(),
                SHORT_COMMENT_BEFORE_EXPR.get(0).comment(), 
                SHORT_COMMENT_AFTER_EXPR.get(0).comment(), 
                COMMENT_AFTER_EXPR.get(0).comment())
            );
        // @formatter:on

        Collections.sort(expectedComments);

        List<String> comments = new ArrayList<>(expr.allComments().stream().map(PlComment::comment).toList());
        Collections.sort(comments);

        assertEquals(expectedComments, comments);

    }

    @Test
    void testJson() {

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
                           match("a", EQUALS, "5"),
                           or (match("k", EQUALS, "4"),
                               match("l", EQUALS, "92").withComments(COMMENT_AFTER_EXPR),
                               match("a", EQUALS, rop("b"))
                           )
                       )
                    )
                );
        // @formatter:on

        String json = JsonUtils.writeAsJsonString(expr, true);

        PlExpression<?> res = JsonUtils.readFromJsonString(json, PlExpression.class);

        assertEquals(expr, res);
    }

    @Test
    void testDocumentationExamples() {

        assertEquals("""
                (
                        color = blue
                     OR engine = Diesel
                    )
                AND brand = Toyota""", StandardConversions.parsePlExpression("(color=blue or engine=Diesel) and brand=Toyota").format(PRETTY_PRINT));

        assertEquals("""
                color = blue
                    /* or engine=Diesel */
                AND brand = Toyota""", StandardConversions.parsePlExpression("(color=blue /*or engine=Diesel*/) and brand=Toyota").format(PRETTY_PRINT));

        assertEquals("""
                brand = Toyota
                AND (
                        (
                            color ANY OF (
                                red,
                                green,
                                /* violet-metallic, */
                                yellow,
                                black,
                                grey
                            )
                        AND tech.type CONTAINS ANY OF (p9, p2)
                        )
                     OR engine = Diesel
                    )
                AND color = red""", StandardConversions.parsePlExpression(
                "brand=Toyota and ((color any of (red, green, /* violet-metallic, */ yellow,black,grey) and (tech.type contains any of (p9, \"p2\"))) or engine=Diesel) and color=red")
                .format(PRETTY_PRINT));

    }

}
