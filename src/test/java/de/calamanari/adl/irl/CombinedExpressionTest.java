//@formatter:off
/*
 * CombinedExpressionTest
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

package de.calamanari.adl.irl;

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
import de.calamanari.adl.erl.AudlangParseResult;
import de.calamanari.adl.erl.PlExpression;
import de.calamanari.adl.erl.PlExpressionBuilder;
import de.calamanari.adl.irl.biceps.CoreExpressionOptimizer;
import de.calamanari.adl.irl.biceps.EncodedExpressionTree;
import de.calamanari.adl.util.JsonUtils;

import static de.calamanari.adl.CombinedExpressionType.AND;
import static de.calamanari.adl.CombinedExpressionType.OR;
import static de.calamanari.adl.FormatStyle.INLINE;
import static de.calamanari.adl.FormatStyle.PRETTY_PRINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class CombinedExpressionTest {

    static final Logger LOGGER = LoggerFactory.getLogger(CombinedExpressionTest.class);

    @Test
    void testBasicCombinationsInline() {

        assertEquals("a = 1 AND b = 2", parse("a = 1 AND b = 2").toString());
        assertEquals("a = 1 AND b = 2", parse("a = 1 AND b = 2").format(INLINE));

        assertEquals("a = 1 OR b = 2", parse("a = 1 OR b = 2").toString());
        assertEquals("a = 1 OR b = 2", parse("a = 1 OR b = 2").format(INLINE));

        assertEquals("a = 1 AND (STRICT NOT b = 2 OR b IS UNKNOWN)", parse("a = 1 AND b != 2").toString());
        assertEquals("a = 1 AND (STRICT NOT b = 2 OR b IS UNKNOWN)", parse("a = 1 AND b != 2").format(INLINE));

        assertEquals("a = 1 AND STRICT NOT b = 2", parse("a = 1 AND STRICT b != 2").toString());
        assertEquals("a = 1 AND STRICT NOT b = 2", parse("a = 1 AND STRICT b != 2").format(INLINE));

        assertEquals("a = 1 AND (b = 2 OR b = 3 OR b = 4)", parse("a = 1 AND b ANY OF (2, 3, 4)").toString());
        assertEquals("a = 1 AND (b = 2 OR b = 3 OR b = 4)", parse("a = 1 AND b ANY OF (2, 3, 4)").format(INLINE));

        assertEquals("STRICT NOT argName IS UNKNOWN", parse("STRICT NOT argName IS UNKNOWN OR argName = @REF").toString());

        assertEquals("STRICT NOT argName IS UNKNOWN", parse("STRICT NOT argName IS UNKNOWN OR (argName < value AND argName > value)").toString());

        assertEquals("argName < value AND argName > value", parse("STRICT NOT argName IS UNKNOWN AND (argName < value AND argName > value)").toString());

        assertEquals("REF = @argName", parse("STRICT NOT argName IS UNKNOWN AND argName = @REF").toString());

        assertEquals("REF = @argName", parse("<NONE> OR argName = @REF").toString());

        assertEquals("(a = 1 OR b = 1 OR c = 1) AND (d = 1 OR e = 1 OR f = 1)", parse("(a=1 OR b=1 OR c=1) AND (d=1 OR e=1 OR f=1)").toString());

        assertEquals("STRICT NOT a = 1 OR a IS UNKNOWN OR b = 2",
                parse("( STRICT NOT a = 1 OR a IS UNKNOWN OR ( STRICT NOT b IS UNKNOWN AND b = 2 ) )").toString());

        assertEquals("a = 1 AND b = 1", CombinedExpression.andOf(parse("a = 1"), parse("b = 1")).toString());
        assertEquals("a = 1 OR b = 1", CombinedExpression.orOf(parse("a = 1"), parse("b = 1")).toString());

        assertEquals(4, parse("a = 1 AND b ANY OF (2, 3, 4)")
                .collectExpressions(e -> (e instanceof MatchExpression match && match.operator() == MatchOperator.EQUALS)).size());

        assertEquals(4, parse("a = 1 AND b ANY OF (2, 3, 4)")
                .collectExpressionsUnique(e -> (e instanceof MatchExpression match && match.operator() == MatchOperator.EQUALS)).size());

        assertEquals(3, parse("a = 1 AND b ANY OF (2, 3, 4)").depth());
    }

    @Test
    void testWhatHappens() {

        assertEquals("<NONE>", parse(
                "STRICT NOT a = 2 AND STRICT NOT a = 3 AND STRICT NOT a = 4 AND ( a = 2 OR a = 3 OR a = 4 OR a = @c ) AND ( a = a OR a = @r OR r IS UNKNOWN ) AND ( STRICT NOT a = @c OR c IS UNKNOWN )")
                        .format(PRETTY_PRINT));

        assertEquals("q IS UNKNOWN", parse("( q = 1 OR q IS UNKNOWN ) AND ( STRICT NOT q = 1 OR q IS UNKNOWN )").format(PRETTY_PRINT));

        String expr = "(a = 1 AND b != 2) OR (c = 4 AND NOT (d =6 OR (e != 7 AND STRICT f != 9)))";

        assertEquals("<ALL>", parse("(" + expr + ") OR NOT (" + expr + ")").format(PRETTY_PRINT));

        assertEquals("<NONE>", parse("(" + expr + ") AND NOT (" + expr + ")").format(PRETTY_PRINT));

        expr = """
                STRICT NOT a = 1
                OR b = 1
                OR (
                       STRICT NOT a = a
                   AND STRICT NOT a = @r
                   )
                OR (
                       (
                           c = @a
                        OR a = 2
                        OR a = 3
                        OR a = 4
                       )
                   AND (
                           a = a
                        OR a = @r
                        OR r IS UNKNOWN
                       )
                   AND (
                           STRICT NOT q = 1
                        OR q IS UNKNOWN
                       )
                   )
                OR (
                       STRICT NOT a = 2
                   AND STRICT NOT a = 3
                   AND STRICT NOT a = 4
                   AND (
                           STRICT NOT c = @a
                        OR c IS UNKNOWN
                       )
                   )
                OR REF = @a
                OR q = 1
                OR a IS UNKNOWN
                OR e = 24
                """;

        assertEquals("<ALL>", parse(expr).format(PRETTY_PRINT));

    }

    @Test
    void testBasicCombinationsPrettyPrint() {

        assertEquals("a = 1\nAND b = 2", parse("a = 1 AND b = 2").format(PRETTY_PRINT));

        assertEquals("a = 1\nOR b = 2", parse("a = 1 OR b = 2").format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND (
                        STRICT NOT b = 2
                     OR b IS UNKNOWN
                    )""", parse("a = 1 AND b != 2").format(PRETTY_PRINT));

        assertEquals("a = 1\nAND STRICT NOT b = 2", parse("a = 1 AND STRICT b != 2").format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND (
                        b = 2
                     OR b = 3
                     OR b = 4
                    )""", parse("a = 1 AND b ANY OF (2, 3, 4)").format(PRETTY_PRINT));
    }

    @Test
    void testNestingInline() {

        assertEquals("a = 1 AND (b = 2 OR c = 3)", parse("a = 1 AND (b = 2 OR c = 3)").toString());
        assertEquals("a = 1 AND (b = 2 OR c = 3)", parse("a = 1 AND (b = 2 OR c = 3)").format(INLINE));

        assertEquals("a = 1 AND b = 2 AND c = 3", parse("a = 1 AND (b = 2 AND c = 3)").toString());
        assertEquals("a = 1 AND b = 2 AND c = 3", parse("a = 1 AND (b = 2 AND c = 3)").format(INLINE));

        assertEquals("a = 1 OR (b = 2 AND c = 3)", parse("a = 1 OR (b = 2 AND c = 3)").toString());
        assertEquals("a = 1 OR (b = 2 AND c = 3)", parse("a = 1 OR (b = 2 AND c = 3)").format(INLINE));

        assertEquals("a = 1 OR b = 2 OR c = 3", parse("a = 1 OR (b = 2 OR c = 3)").toString());
        assertEquals("a = 1 OR b = 2 OR c = 3", parse("a = 1 OR (b = 2 OR c = 3)").format(INLINE));

        assertEquals("a = 1 AND (b = 2 OR c = 3)", parse("(b = 2 OR c = 3) AND a = 1").toString());
        assertEquals("a = 1 AND (b = 2 OR c = 3)", parse("(b = 2 OR c = 3) AND a = 1").format(INLINE));

        assertEquals("(a = 1 OR z = 4) AND (b = 2 OR c = 3)", parse("(b = 2 OR c = 3) AND (a = 1 OR z = 4)").toString());
        assertEquals("(a = 1 OR z = 4) AND (b = 2 OR c = 3)", parse("(b = 2 OR c = 3) AND (a = 1 OR z = 4)").format(INLINE));

        assertEquals("""
                a = 1 AND (b = 2 OR b = 3 OR b = 4 OR c = 2 OR c = 3 OR c = 4)""", parse("a = 1 AND (b ANY OF (2, 3, 4) OR c ANY OF (2, 3, 4) )").toString());
        assertEquals("""
                a = 1 AND (b = 2 OR b = 3 OR b = 4 OR c = 2 OR c = 3 OR c = 4)""",
                parse("a = 1 AND (b ANY OF (2, 3, 4) OR c ANY OF (2, 3, 4) )").format(INLINE));

        assertEquals(
                """
                        a = 1 AND (b CONTAINS s2 OR b CONTAINS s3 OR b CONTAINS s4 OR b CONTAINS s5 OR b CONTAINS s6 OR c CONTAINS s2 OR c CONTAINS s3 OR c CONTAINS s4)""",
                parse("a = 1 AND (b CONTAINS ANY OF (s2, s3, s4, s5, s6) OR c CONTAINS ANY OF (s2, s3, s4) )").toString());
        assertEquals(
                """
                        a = 1 AND (b CONTAINS s2 OR b CONTAINS s3 OR b CONTAINS s4 OR b CONTAINS s5 OR b CONTAINS s6 OR c CONTAINS s2 OR c CONTAINS s3 OR c CONTAINS s4)""",
                parse("a = 1 AND (b CONTAINS ANY OF (s2, s3, s4, s5, s6) OR c CONTAINS ANY OF (s2, s3, s4) )").format(INLINE));

        assertEquals("a = 1 AND ( (b = 2 AND e = 9 AND foo < high AND q = 10 AND (foo > low OR foo = low) ) OR (c = 3 AND d = 5 AND (k = 4 OR l = 92) ) )",
                parse("""
                        a = 1
                        AND (
                                (
                                    (
                                        foo < high
                                    AND foo >= low
                                    )
                                AND b = 2
                                AND e = 9
                                AND q = 10
                                )
                             OR (
                                    c = 3
                                AND d = 5
                                AND (
                                        k = 4
                                     OR l = 92
                                    )
                                )
                            )""").toString());

        assertEquals("a = 1 AND ( (b = 2 AND e = 9 AND foo < high AND q = 10 AND (foo > low OR foo = low) ) OR (c = 3 AND d = 5 AND (k = 4 OR l = 92) ) )",
                parse("""
                        a = 1
                        AND (
                                (
                                    (
                                        foo < high
                                    AND foo >= low
                                    )
                                AND b = 2
                                AND e = 9
                                AND q = 10
                                )
                             OR (
                                    c = 3
                                AND d = 5
                                AND (
                                        k = 4
                                     OR l = 92
                                    )
                                )
                            )""").format(INLINE));
    }

    @Test
    void testNestingPrettyPrint() {

        assertEquals("""
                a = 1
                AND (
                        b = 2
                     OR c = 3
                    )""", parse("a = 1 AND (b = 2 OR c = 3)").format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND b = 2
                AND c = 3""", parse("a = 1 AND (b = 2 AND c = 3)").format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                OR (
                        b = 2
                    AND c = 3
                    )""", parse("a = 1 OR (b = 2 AND c = 3)").format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                OR b = 2
                OR c = 3""", parse("a = 1 OR (b = 2 OR c = 3)").format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND (
                        b = 2
                     OR c = 3
                    )""", parse("(b = 2 OR c = 3) AND a = 1").format(PRETTY_PRINT));

        assertEquals("""
                (
                        a = 1
                     OR z = 4
                    )
                AND (
                        b = 2
                     OR c = 3
                    )""", parse("(b = 2 OR c = 3) AND (a = 1 OR z = 4)").format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND (
                        b = 2
                     OR b = 3
                     OR b = 4
                     OR c = 2
                     OR c = 3
                     OR c = 4
                    )""", parse("a = 1 AND (b ANY OF (2, 3, 4) OR c ANY OF (2, 3, 4) )").format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND (
                        b CONTAINS s2
                     OR b CONTAINS s3
                     OR b CONTAINS s4
                     OR b CONTAINS s5
                     OR b CONTAINS s6
                     OR c CONTAINS s2
                     OR c CONTAINS s3
                     OR c CONTAINS s4
                    )""", parse("a = 1 AND (b CONTAINS ANY OF (s2, s3, s4, s5, s6) OR c CONTAINS ANY OF (s2, s3, s4) )").format(PRETTY_PRINT));

        assertEquals("""
                a = 1
                AND (
                        (
                            b = 2
                        AND e = 9
                        AND foo < high
                        AND q = 10
                        AND (
                                foo > low
                             OR foo = low
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
                    )""", parse("""
                a = 1
                AND (
                        (
                            (
                                foo < high
                            AND foo >= low
                            )
                        AND b = 2
                        AND e = 9
                        AND q = 10
                        )
                     OR (
                            c = 3
                        AND d = 5
                        AND (
                                k = 4
                             OR l = 92
                            )
                        )
                    )""").format(PRETTY_PRINT));
    }

    @Test
    void testSpecialCases() {

        assertThrows(AudlangValidationException.class, () -> new CombinedExpression(AND, null, null));

        assertThrows(AudlangValidationException.class, () -> CombinedExpression.of(null, null));

        assertThrows(AudlangValidationException.class, () -> CombinedExpression.of(null, AND));

        final List<CoreExpression> emptyMembers = Collections.emptyList();
        assertThrows(AudlangValidationException.class, () -> CombinedExpression.of(emptyMembers, OR));
        assertThrows(AudlangValidationException.class, () -> new CombinedExpression(OR, emptyMembers, null));

        final List<CoreExpression> singleMembers = Arrays.asList(SpecialSetExpression.all());

        assertThrows(AudlangValidationException.class, () -> new CombinedExpression(OR, singleMembers, null));

        final List<CoreExpression> validMembers = Arrays.asList(parse("a = 1"), parse("b = 1"));

        assertThrows(AudlangValidationException.class, () -> new CombinedExpression(null, validMembers, null));

        final List<CoreExpression> invalidMembers = Arrays.asList(SpecialSetExpression.all(), SpecialSetExpression.none());

        assertThrows(AudlangValidationException.class, () -> new CombinedExpression(OR, invalidMembers, null));

        CoreExpression expr = parse("a = 1 AND (b = 2 OR c = 3)");
        assertSame(expr, CombinedExpression.of(Arrays.asList(expr), OR));
        assertEquals(expr, CombinedExpression.of(Arrays.asList(expr, expr), AND));

        assertEquals(SpecialSetExpression.none(), CombinedExpression.of(Arrays.asList(expr, SpecialSetExpression.none()), AND));

        assertEquals(SpecialSetExpression.all(), CombinedExpression.of(Arrays.asList(expr, SpecialSetExpression.all()), OR));

        assertEquals(SpecialSetExpression.none(), CombinedExpression.of(Arrays.asList(SpecialSetExpression.none(), SpecialSetExpression.none()), OR));

        assertEquals(SpecialSetExpression.all(), CombinedExpression.of(Arrays.asList(SpecialSetExpression.all(), SpecialSetExpression.all()), AND));

    }

    @Test
    void testVeryComplex() {

        assertEquals("""
                (
                        (
                            a = 1
                         OR a = 2
                        )
                    AND (
                            b = 1
                         OR b = 2
                        )
                    AND (
                            c = 1
                         OR c = 2
                        )
                    )
                OR (
                        (
                            a IS UNKNOWN
                         OR (
                                STRICT NOT a = 1
                            AND STRICT NOT a = 2
                            )
                        )
                    AND (
                            b IS UNKNOWN
                         OR (
                                STRICT NOT b = 1
                            AND STRICT NOT b = 2
                            )
                        )
                    AND (
                            c IS UNKNOWN
                         OR (
                                STRICT NOT c = 1
                            AND STRICT NOT c = 2
                            )
                        )
                    )""", parse("(a NOT ANY OF (1,2) AND b NOT ANY OF (1,2) AND c NOT ANY OF (1,2)) OR (a ANY OF (1,2) AND b ANY OF (1,2) AND c ANY OF (1,2))")
                .format(PRETTY_PRINT));

        assertEquals("<ALL>", parse(
                "a NOT ANY OF (1,2,3,4,5,6,7,8,9,10) OR b NOT ANY OF (1,2,3,4,5,6,7,8,9,10) OR c NOT ANY OF (1,2,3,4,5,6,7,8,9,10) OR (a ANY OF (1,2,3,4,5,6,7,8,9,10) AND b ANY OF (1,2,3,4,5,6,7,8,9,10) AND c ANY OF (1,2,3,4,5,6,7,8,9,10))")
                        .format(PRETTY_PRINT));

        assertEquals("<ALL>", parse(
                "a NOT ANY OF (1,2,3,4,5,6,7,8,9,10) OR a ANY OF (1,2,3,4,5,6,7,8,9,10) OR b NOT ANY OF (1,2,3,4,5,6,7,8,9,10) OR b ANY OF (1,2,3,4,5,6,7,8,9,10)")
                        .format(PRETTY_PRINT));

        assertEquals("""
                (
                        (
                            a = 1
                         OR a = 10
                         OR a = 2
                         OR a = 3
                         OR a = 4
                         OR a = 5
                         OR a = 6
                         OR a = 7
                         OR a = 8
                         OR a = 9
                        )
                    AND (
                            b = 1
                         OR b = 10
                         OR b = 2
                         OR b = 3
                         OR b = 4
                         OR b = 5
                         OR b = 6
                         OR b = 7
                         OR b = 8
                         OR b = 9
                        )
                    AND (
                            c = 1
                         OR c = 10
                         OR c = 2
                         OR c = 3
                         OR c = 4
                         OR c = 5
                         OR c = 6
                         OR c = 7
                         OR c = 8
                         OR c = 9
                        )
                    )
                OR (
                        (
                            a IS UNKNOWN
                         OR (
                                STRICT NOT a = 1
                            AND STRICT NOT a = 10
                            AND STRICT NOT a = 2
                            AND STRICT NOT a = 3
                            AND STRICT NOT a = 4
                            AND STRICT NOT a = 5
                            AND STRICT NOT a = 6
                            AND STRICT NOT a = 7
                            AND STRICT NOT a = 8
                            AND STRICT NOT a = 9
                            )
                        )
                    AND (
                            b IS UNKNOWN
                         OR (
                                STRICT NOT b = 1
                            AND STRICT NOT b = 10
                            AND STRICT NOT b = 2
                            AND STRICT NOT b = 3
                            AND STRICT NOT b = 4
                            AND STRICT NOT b = 5
                            AND STRICT NOT b = 6
                            AND STRICT NOT b = 7
                            AND STRICT NOT b = 8
                            AND STRICT NOT b = 9
                            )
                        )
                    AND (
                            c IS UNKNOWN
                         OR (
                                STRICT NOT c = 1
                            AND STRICT NOT c = 10
                            AND STRICT NOT c = 2
                            AND STRICT NOT c = 3
                            AND STRICT NOT c = 4
                            AND STRICT NOT c = 5
                            AND STRICT NOT c = 6
                            AND STRICT NOT c = 7
                            AND STRICT NOT c = 8
                            AND STRICT NOT c = 9
                            )
                        )
                    )""", parse(
                "(a NOT ANY OF (1,2,3,4,5,6,7,8,9,10) AND b NOT ANY OF (1,2,3,4,5,6,7,8,9,10) AND c NOT ANY OF (1,2,3,4,5,6,7,8,9,10)) OR (a ANY OF (1,2,3,4,5,6,7,8,9,10) AND b ANY OF (1,2,3,4,5,6,7,8,9,10) AND c ANY OF (1,2,3,4,5,6,7,8,9,10))")
                        .format(PRETTY_PRINT));

        assertEquals("<ALL>", parse(
                "a NOT ANY OF (1,2,3,4,5,6,7,8,9,10) OR b NOT ANY OF (1,2,3,4,5,6,7,8,9,10) OR c NOT ANY OF (1,2,3,4,5,6,7,8,9,10) OR (a ANY OF (1,2,3,4,5,6,7,8,9,10) AND b ANY OF (1,2,3,4,5,6,7,8,9,10) AND c ANY OF (1,2,3,4,5,6,7,8,9,10))")
                        .format(PRETTY_PRINT));

        assertEquals("<ALL>", parse("""
                STRICT NOT a = 1
                OR b = 1
                OR (
                       STRICT NOT a = a
                   AND STRICT NOT a = @r
                   )
                OR (
                       (
                           c = @a
                        OR a = 2
                        OR a = 3
                        OR a = 4
                       )
                   AND (
                           a = a
                        OR a = @r
                        OR r IS UNKNOWN
                       )
                   AND (
                           STRICT NOT q = 1
                        OR q IS UNKNOWN
                       )
                   )
                OR (
                       STRICT NOT a = 2
                   AND STRICT NOT a = 3
                   AND STRICT NOT a = 4
                   AND (
                           STRICT NOT c = @a
                        OR c IS UNKNOWN
                       )
                   )
                OR REF = @a
                OR q = 1
                OR a IS UNKNOWN
                OR e = 24
                """).format(PRETTY_PRINT));

    }

    @Test
    void testEqualsHashCode() {

        CoreExpression expr = parse("""
                a = 1
                AND (
                        (
                            (
                                foo < high
                            AND foo >= low
                            )
                        AND b = 2
                        AND e = 9
                        AND q = 10
                        )
                     OR (
                            c = 3
                        AND d = 5
                        AND (
                                k = 4
                             OR l = 92
                            )
                        )
                    )""");
        CoreExpression expr2 = parse("""
                a = 1
                AND (
                        (
                            (
                                foo < high
                            AND foo >= low
                            )
                        AND b = 2
                        AND e = 9
                        AND q = 10
                        )
                     OR (
                            c = 3
                        AND d = 5
                        AND (
                                k = 4
                             OR l = 92
                            )
                        )
                    )""");

        assertTrue(expr instanceof CombinedExpression);

        assertNotSame(expr, expr2);

        assertEquals(expr, expr2);

        assertEquals(expr.hashCode(), expr2.hashCode());

    }

    @Test
    void testGetAllFields() {

        CoreExpression expr = parse("""
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
                        AND a = 5
                        AND (
                                k = 4
                             OR l = 92
                             OR a = @b
                            )
                        )
                    )
                """);

        List<String> expectedFieldNames = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "k", "l", "q", "foo"));
        Collections.sort(expectedFieldNames);
        assertEquals(expectedFieldNames, expr.allArgNames());

        List<CoreExpression> collectedExpressions = new ArrayList<>();
        expr.collectExpressions(e -> e instanceof MatchExpression m && m.argName().equals("a"), collectedExpressions);
        assertEquals(3, collectedExpressions.size());

        AudlangField field = expr.allFields().stream().filter(f -> f.argName().equals("a")).toList().get(0);
        assertEquals(Arrays.asList("1", "5"), field.values());
        assertEquals(Arrays.asList("b"), field.refArgNames());

    }

    @Test
    void testJson() {

        CoreExpression expr = parse("""
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
                        AND a = 5
                        AND (
                                k = 4
                             OR l = 92
                             OR a = @b
                            )
                        )
                    )
                """);

        String json = JsonUtils.writeAsJsonString(expr, true);

        CoreExpression res = JsonUtils.readFromJsonString(json, CoreExpression.class);

        assertEquals(expr, res);

        assertThrows(RuntimeException.class, () -> JsonUtils.readFromJsonString(json, PlExpression.class));

        PlExpression<?> exprPl = parsePl("""
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
                        AND a = 5
                        AND (
                                k = 4
                             OR l = 92
                             OR a = @b
                            )
                        )
                    )
                """).getResultExpression();
        String jsonPl = JsonUtils.writeAsJsonString(exprPl, true);

        assertThrows(RuntimeException.class, () -> JsonUtils.readFromJsonString(jsonPl, CoreExpression.class));

    }

    @Test
    void testBadJson() {
        String json = """
                {
                    "combi_type" : "OR",
                    "members" : [
                        {
                            "unexpected" : "a",
                            "operator" : "EQUALS",
                            "operand" : {
                                "value" : "1"
                            },
                            "inline" : "a = 1"
                        },
                        {
                            "arg_name" : "b",
                            "operator" : "EQUALS",
                            "operand" : {
                                "value" : "1"
                            },
                            "inline" : "b = 1"
                        }
                    ],
                    "inline" : "a = 1 OR b = 1"
                }""";

        assertThrows(RuntimeException.class, () -> JsonUtils.readFromJsonString(json, CoreExpression.class));

    }

    @Test
    void testLateOptimize() {
        CoreExpression expr = CombinedExpression.andOf(parse("a = 1"), parse("a = 1 OR a = 2"), parse("a = 1 OR a = 3"), parse("a = 1 OR a != 4"));

        CoreExpressionOptimizer optimizer = new CoreExpressionOptimizer();
        expr = optimizer.process(expr);
        assertEquals("a = 1", expr.toString());

    }

    @Test
    void testTreeCreateDebugString() {
        CoreExpression expr = parse("""
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
                        AND a = 5
                        AND (
                                k = 4
                             OR l = 92
                             OR a = @b
                            )
                        )
                    )
                """);

        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(expr);

        assertEquals(
                "( a = 1 AND ( ( a = 5 AND ( a = @b OR k = 4 OR l = 92 ) AND c = 3 AND d = 5 ) OR ( ( e = v1 OR e = v2 OR e = v3 ) AND ( foo > low OR foo = low ) AND ( ( STRICT NOT q = v1 AND STRICT NOT q = v2 AND STRICT NOT q = v3 AND STRICT NOT q = v4 AND STRICT NOT q = v5 ) OR q IS UNKNOWN ) AND b = 2 AND foo < high ) ) )",
                tree.createDebugString(tree.getRootNode()));

        assertNotNull(tree.getCodec());

    }

    @Test
    void testDocumentationExamples() {

        assertEquals("""
                brand = Toyota
                AND (
                        color = blue
                     OR engine = Diesel
                    )""", StandardConversions.parseCoreExpression("(color=blue or engine=Diesel) and brand=Toyota").format(PRETTY_PRINT));

        assertEquals("""
                brand = Toyota
                AND color = blue""", StandardConversions.parseCoreExpression("(color=blue /*or engine=Diesel*/) and brand=Toyota").format(PRETTY_PRINT));

        assertEquals("""
                brand = Toyota
                AND color = red
                AND (
                        engine = Diesel
                     OR tech.type CONTAINS p2
                     OR tech.type CONTAINS p9
                    )""", StandardConversions.parseCoreExpression(
                "brand=Toyota and ((color any of (red, green, /* violet-metallic, */ yellow,black,grey) and (tech.type contains any of (p9, \"p2\"))) or engine=Diesel) and color=red")
                .format(PRETTY_PRINT));

        assertEquals("""
                color = blue
                AND (
                        engine = Diesel
                     OR engine = gas
                     OR (
                            engine = alternative
                        AND fuel = hydrogen
                        )
                    )""", StandardConversions
                .parseCoreExpression("color = blue and (engine=Diesel or engine = gas or (engine=alternative and fuel = hydrogen))").format(PRETTY_PRINT));

    }

    private static CoreExpression parse(String source) {
        return StandardConversions.parseCoreExpression(source);
    }

    private static AudlangParseResult parsePl(String source) {
        return PlExpressionBuilder.stringToExpression(source);
    }

}
