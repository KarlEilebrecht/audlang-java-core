//@formatter:off
/*
 * ResolvedCurbExpressionTest
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.cnv.PlToCoreExpressionConverter;
import de.calamanari.adl.cnv.StandardConversions;
import de.calamanari.adl.erl.AudlangParseResult;
import de.calamanari.adl.erl.PlExpressionBuilder;
import de.calamanari.adl.irl.biceps.ExpressionTreeSimulator;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class ResolvedCurbExpressionTest {

    static final Logger LOGGER = LoggerFactory.getLogger(ResolvedCurbExpressionTest.class);

    @Test
    void testCurbBound1() {

        String expr = "CURB ( a=1 OR b!=1 OR STRICT c!=1 ) < 1";

        assertEquals("b = 1 AND (STRICT NOT a = 1 OR a IS UNKNOWN) AND (c = 1 OR c IS UNKNOWN)", parse(expr).format(FormatStyle.INLINE));

        assertEquals("<ALL>", parse(exprOrNotExpr(expr)).toString());
        assertEquals("<NONE>", parse(exprAndNotExpr(expr)).toString());

        expr = "CURB ( a=1 OR b!=1 OR STRICT c!=1 ) <= 1";

        assertEquals("""
                (
                        (
                            STRICT NOT a = 1
                         OR a IS UNKNOWN
                        )
                    AND (
                            c = 1
                         OR c IS UNKNOWN
                        )
                    )
                OR (
                        b = 1
                    AND (
                            STRICT NOT a = 1
                         OR a IS UNKNOWN
                         OR c = 1
                         OR c IS UNKNOWN
                        )
                    )""", parse(expr).format(FormatStyle.PRETTY_PRINT));

        assertEquals("<ALL>", parse(exprOrNotExpr(expr)).toString());
        assertEquals("<NONE>", parse(exprAndNotExpr(expr)).toString());

        expr = "CURB ( a=1 OR b!=1 OR STRICT c!=1 ) = 1";

        assertEquals("""
                (
                        (
                            (
                                (
                                    STRICT NOT a = 1
                                 OR a IS UNKNOWN
                                )
                            AND (
                                    STRICT NOT b = 1
                                 OR b IS UNKNOWN
                                )
                            )
                         OR (
                                a = 1
                            AND b = 1
                            )
                        )
                    AND (
                            c = 1
                         OR c IS UNKNOWN
                        )
                    )
                OR (
                        b = 1
                    AND STRICT NOT c = 1
                    AND (
                            STRICT NOT a = 1
                         OR a IS UNKNOWN
                        )
                    )""", parse(expr).format(FormatStyle.PRETTY_PRINT));

        assertEquals("<ALL>", parse(exprOrNotExpr(expr)).toString());
        assertEquals("<NONE>", parse(exprAndNotExpr(expr)).toString());

        expr = "CURB ( a=1 OR b!=1 OR STRICT c!=1 ) >= 1";

        assertEquals("a = 1 OR STRICT NOT b = 1 OR b IS UNKNOWN OR STRICT NOT c = 1", parse(expr).format(FormatStyle.INLINE));
        assertEquals("<ALL>", parse(exprOrNotExpr(expr)).toString());
        assertEquals("<NONE>", parse(exprAndNotExpr(expr)).toString());

        expr = "CURB ( a=1 OR b!=1 OR STRICT c!=1 ) > 1";

        assertEquals("(STRICT NOT c = 1 AND (STRICT NOT b = 1 OR b IS UNKNOWN) ) OR (a = 1 AND (STRICT NOT b = 1 OR b IS UNKNOWN OR STRICT NOT c = 1) )",
                parse(expr).format(FormatStyle.INLINE));
        assertEquals("<ALL>", parse(exprOrNotExpr(expr)).toString());
        assertEquals("<NONE>", parse(exprAndNotExpr(expr)).toString());

    }

    @Test
    void testCurbBound2() {

        String expr = "CURB ( a=1 OR b=1 OR c=1 OR d=1) < 2";

        assertEquals("""
                (
                        (
                            (
                                (
                                    STRICT NOT a = 1
                                 OR a IS UNKNOWN
                                 OR STRICT NOT b = 1
                                 OR b IS UNKNOWN
                                )
                            AND (
                                    STRICT NOT c = 1
                                 OR c IS UNKNOWN
                                )
                            )
                         OR (
                                (
                                    STRICT NOT a = 1
                                 OR a IS UNKNOWN
                                )
                            AND (
                                    STRICT NOT b = 1
                                 OR b IS UNKNOWN
                                )
                            )
                        )
                    AND (
                            STRICT NOT d = 1
                         OR d IS UNKNOWN
                        )
                    )
                OR (
                        (
                            STRICT NOT a = 1
                         OR a IS UNKNOWN
                        )
                    AND (
                            STRICT NOT b = 1
                         OR b IS UNKNOWN
                        )
                    AND (
                            STRICT NOT c = 1
                         OR c IS UNKNOWN
                        )
                    )""", parse(expr).format(FormatStyle.PRETTY_PRINT));
        assertEquals("<ALL>", parse(exprOrNotExpr(expr)).toString());
        assertEquals("<NONE>", parse(exprAndNotExpr(expr)).toString());

        expr = "CURB ( a=1 OR b=1 OR c=1 OR d=1) <= 2";

        assertEquals("""
                (
                        (
                            STRICT NOT a = 1
                         OR a IS UNKNOWN
                         OR STRICT NOT b = 1
                         OR b IS UNKNOWN
                         OR STRICT NOT c = 1
                         OR c IS UNKNOWN
                        )
                    AND (
                            STRICT NOT d = 1
                         OR d IS UNKNOWN
                        )
                    )
                OR (
                        (
                            STRICT NOT a = 1
                         OR a IS UNKNOWN
                         OR STRICT NOT b = 1
                         OR b IS UNKNOWN
                        )
                    AND (
                            STRICT NOT c = 1
                         OR c IS UNKNOWN
                        )
                    )
                OR (
                        (
                            STRICT NOT a = 1
                         OR a IS UNKNOWN
                        )
                    AND (
                            STRICT NOT b = 1
                         OR b IS UNKNOWN
                        )
                    )""", parse(expr).format(FormatStyle.PRETTY_PRINT));
        assertEquals("<ALL>", parse(exprOrNotExpr(expr)).toString());
        assertEquals("<NONE>", parse(exprAndNotExpr(expr)).toString());

        expr = "CURB ( a=1 OR b=1 OR c=1 OR d=1) = 2";

        assertEquals("""
                (
                        (
                            (
                                a = 1
                            AND b = 1
                            AND (
                                    STRICT NOT c = 1
                                 OR c IS UNKNOWN
                                )
                            )
                         OR (
                                c = 1
                            AND (
                                    (
                                        a = 1
                                    AND (
                                            STRICT NOT b = 1
                                         OR b IS UNKNOWN
                                        )
                                    )
                                 OR (
                                        b = 1
                                    AND (
                                            STRICT NOT a = 1
                                         OR a IS UNKNOWN
                                        )
                                    )
                                )
                            )
                        )
                    AND (
                            STRICT NOT d = 1
                         OR d IS UNKNOWN
                        )
                    )
                OR (
                        d = 1
                    AND (
                            (
                                (
                                    (
                                        a = 1
                                    AND (
                                            STRICT NOT b = 1
                                         OR b IS UNKNOWN
                                        )
                                    )
                                 OR (
                                        b = 1
                                    AND (
                                            STRICT NOT a = 1
                                         OR a IS UNKNOWN
                                        )
                                    )
                                )
                            AND (
                                    STRICT NOT c = 1
                                 OR c IS UNKNOWN
                                )
                            )
                         OR (
                                c = 1
                            AND (
                                    STRICT NOT a = 1
                                 OR a IS UNKNOWN
                                )
                            AND (
                                    STRICT NOT b = 1
                                 OR b IS UNKNOWN
                                )
                            )
                        )
                    )""", parse(expr).format(FormatStyle.PRETTY_PRINT));

        assertEquals("<ALL>", parse(exprOrNotExpr(expr)).toString());
        assertEquals("<NONE>", parse(exprAndNotExpr(expr)).toString());

        expr = "CURB ( a=1 OR b=1 OR c=1 OR d=1) >= 2";

        assertEquals("""
                (
                        a = 1
                    AND b = 1
                    )
                OR (
                        c = 1
                    AND (
                            a = 1
                         OR b = 1
                        )
                    )
                OR (
                        d = 1
                    AND (
                            a = 1
                         OR b = 1
                         OR c = 1
                        )
                    )""", parse(expr).format(FormatStyle.PRETTY_PRINT));
        assertEquals("<ALL>", parse(exprOrNotExpr(expr)).toString());
        assertEquals("<NONE>", parse(exprAndNotExpr(expr)).toString());

        expr = "CURB ( a=1 OR b=1 OR c=1 OR d=1) > 2";

        assertEquals("""
                (
                        a = 1
                    AND b = 1
                    AND (
                            c = 1
                         OR d = 1
                        )
                    )
                OR (
                        c = 1
                    AND d = 1
                    AND (
                            a = 1
                         OR b = 1
                        )
                    )""", parse(expr).format(FormatStyle.PRETTY_PRINT));
        assertEquals("<ALL>", parse(exprOrNotExpr(expr)).toString());
        assertEquals("<NONE>", parse(exprAndNotExpr(expr)).toString());

    }

    @Test
    void testCurbOfCurb() {

        String expr = "CURB ( a=1 OR b=1 OR CURB ( e = 1 OR f = 1 OR g = 1) = 1) = 2";

        assertEquals("""
                (
                        a = 1
                    AND (
                            (
                                (
                                    (
                                        e = 1
                                    AND (
                                            STRICT NOT f = 1
                                         OR f IS UNKNOWN
                                        )
                                    )
                                 OR (
                                        f = 1
                                    AND (
                                            STRICT NOT e = 1
                                         OR e IS UNKNOWN
                                        )
                                    )
                                )
                            AND (
                                    STRICT NOT g = 1
                                 OR g IS UNKNOWN
                                )
                            )
                         OR (
                                g = 1
                            AND (
                                    STRICT NOT e = 1
                                 OR e IS UNKNOWN
                                )
                            AND (
                                    STRICT NOT f = 1
                                 OR f IS UNKNOWN
                                )
                            )
                        )
                    AND (
                            STRICT NOT b = 1
                         OR b IS UNKNOWN
                        )
                    )
                OR (
                        b = 1
                    AND (
                            (
                                (
                                    (
                                        (
                                            (
                                                a = 1
                                            AND (
                                                    STRICT NOT e = 1
                                                 OR e IS UNKNOWN
                                                )
                                            )
                                         OR (
                                                e = 1
                                            AND (
                                                    STRICT NOT a = 1
                                                 OR a IS UNKNOWN
                                                )
                                            )
                                        )
                                    AND (
                                            STRICT NOT f = 1
                                         OR f IS UNKNOWN
                                        )
                                    )
                                 OR (
                                        f = 1
                                    AND (
                                            STRICT NOT a = 1
                                         OR a IS UNKNOWN
                                        )
                                    AND (
                                            STRICT NOT e = 1
                                         OR e IS UNKNOWN
                                        )
                                    )
                                )
                            AND (
                                    STRICT NOT g = 1
                                 OR g IS UNKNOWN
                                )
                            )
                         OR (
                                a = 1
                            AND e = 1
                            AND f = 1
                            )
                         OR (
                                g = 1
                            AND (
                                    (
                                        (
                                            STRICT NOT a = 1
                                         OR a IS UNKNOWN
                                        )
                                    AND (
                                            STRICT NOT e = 1
                                         OR e IS UNKNOWN
                                        )
                                    AND (
                                            STRICT NOT f = 1
                                         OR f IS UNKNOWN
                                        )
                                    )
                                 OR (
                                        a = 1
                                    AND (
                                            e = 1
                                         OR f = 1
                                        )
                                    )
                                )
                            )
                        )
                    )""", parse(expr).format(FormatStyle.PRETTY_PRINT));
        assertEquals("<ALL>", parse(exprOrNotExpr(expr)).toString());
        assertEquals("<NONE>", parse(exprAndNotExpr(expr)).toString());

    }

    @Test
    void testSpecialCurb() {
        String expr = """
                (
                        CURB (
                            <ALL>
                         OR argName CONTAINS ANY OF (green, green, f)
                        ) = 2
                     OR argName STRICT NOT CONTAINS ANY OF (green, /* comment */ "a=b", /* comment */ xyz)
                     OR argName STRICT NOT ANY OF (a, @r)
                    )
                AND (
                        argName NOT CONTAINS ANY
                        /* some longer comment */
                        OF (Hugo, red, red)
                     OR (
                            argName STRICT NOT CONTAINS "search text"
                        AND CURB (
                                b > 8
                             OR argName <= @argName
                             OR CURB (
                                    argName STRICT NOT ANY OF (a, @r)
                                 OR b > 8
                                 OR argName > xyz
                                ) = 1
                             OR CURB (
                                    argName STRICT NOT ANY OF (a, @r)
                                 OR c < 8
                                 OR STRICT argName != @argName
                                ) = 1
                            ) = 17
                        )
                    )""";

        assertEquals(expr, parsePl(expr).getResultExpression().format(FormatStyle.PRETTY_PRINT));

        assertEquals("<ALL>", parse("(" + expr + ") OR NOT (" + expr + ")").format(FormatStyle.PRETTY_PRINT));

        expr = """
                CURB (
                         b > 8
                      OR argName <= @argName
                      OR CURB (
                                  argName STRICT NOT ANY OF (a, @r)
                               OR b > 8
                               OR argName > xyz
                         ) = 1
                      OR CURB (
                                argName STRICT NOT ANY OF (a, @r)
                             OR c < 8
                             OR STRICT argName != @argName
                         ) = 1
                ) = 1""";

        assertEquals("""
                (
                        (
                            STRICT NOT argName > xyz
                        AND (
                                STRICT NOT c < 8
                             OR c IS UNKNOWN
                            )
                        AND (
                                argName = @r
                             OR argName = a
                             OR r IS UNKNOWN
                            )
                        )
                     OR (
                            c < 8
                        AND (
                                argName IS UNKNOWN
                             OR (
                                    argName > xyz
                                AND STRICT NOT argName = @r
                                AND STRICT NOT argName = a
                                )
                            )
                        )
                    )
                AND (
                        STRICT NOT b > 8
                     OR b IS UNKNOWN
                    )""", parse(expr).format(FormatStyle.PRETTY_PRINT));

        expr = """
                CURB (
                         b > 8
                      OR argName <= @argName
                      OR CURB (
                                  argName STRICT NOT ANY OF (a, @r)
                               OR b > 8
                               OR argName > xyz
                         ) = 1
                      OR CURB (
                                  argName STRICT NOT ANY OF (a, @r)
                               OR c < 8
                               OR STRICT argName != @argName
                         ) = 1
                ) != 5""";

        assertEquals("<ALL>", parse("(" + expr + ") OR NOT (" + expr + ")").format(FormatStyle.PRETTY_PRINT));

    }

    @Test
    void testCurbFromSpec() {

        String exprSpec = """
                (q1=yes AND q2=yes)
                OR (q1=yes AND q3=yes)
                OR (q1=yes AND q4=yes)
                OR (q1=yes AND q5=yes)
                OR (q2=yes AND q3=yes)
                OR (q2=yes AND q4=yes)
                OR (q2=yes AND q5=yes)
                OR (q3=yes AND q4=yes)
                OR (q3=yes AND q5=yes)
                OR (q4=yes AND q5=yes)
                                """;

        assertEquals(parse(exprSpec), parse("CURB ( q1=yes OR q2=yes OR q3=yes OR q4=yes OR q5=yes ) >= 2"));

        exprSpec = """
                (color = red AND fabric = cotton AND look != fancy)
                OR (color = red AND fabric != cotton AND look = fancy)
                OR (color = red AND fabric != cotton AND look != fancy)
                OR (color != red AND fabric = cotton AND look = fancy)
                OR (color != red AND fabric != cotton AND look = fancy)
                OR (color != red AND fabric = cotton AND look != fancy)
                OR (color != red AND fabric != cotton AND look != fancy)""";

        assertEquals(parse(exprSpec), parse("CURB (color = red OR fabric = cotton OR look = fancy) < 3"));

        assertEquals(parse("(color != red OR fabric != cotton OR look != fancy)"), parse("CURB (color = red OR fabric = cotton OR look = fancy) < 3"));

    }

    private static String exprAndNotExpr(String expr) {
        return "( " + expr + " ) AND NOT ( " + expr + ")";
    }

    private static String exprOrNotExpr(String expr) {
        return "( " + expr + " ) OR NOT ( " + expr + ")";
    }

    static String simulateComparisonParsedVsOptimized(String expr, boolean log) {
        ExpressionTreeSimulator simulator = new ExpressionTreeSimulator();

        String res = simulator.simulateComparison(parseNoOptimize(expr), parse(expr));

        if (log) {
            LOGGER.info("\n\n{}", res);
        }
        return res;

    }

    private static CoreExpression parse(String source) {
        return StandardConversions.parseCoreExpression(source);
    }

    private static CoreExpression parseNoOptimize(String source) {
        return new PlToCoreExpressionConverter().convert(parsePl(source).getResultExpression());
    }

    private static AudlangParseResult parsePl(String source) {
        return PlExpressionBuilder.stringToExpression(source);
    }

}
