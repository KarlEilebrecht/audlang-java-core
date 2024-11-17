//@formatter:off
/*
 * CoreToPlExpressionConverterTest
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

package de.calamanari.adl.cnv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.erl.AudlangParseResult;
import de.calamanari.adl.erl.PlExpression;
import de.calamanari.adl.erl.PlExpressionBuilder;
import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.biceps.CoreExpressionOptimizer;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class CoreToPlExpressionConverterTest {

    static final Logger LOGGER = LoggerFactory.getLogger(CoreToPlExpressionConverterTest.class);

    @Test
    void testBasics() {

        assertEquals("<NONE>", convertCoreToPl(parseAndOptimize("<NONE>")).toString());
        assertEquals("<ALL>", convertCoreToPl(parseAndOptimize("<ALL>")).toString());

        assertEquals("STRICT a != 1", convertCoreToPl(parseAndOptimize("STRICT NOT a = 1")).toString());
        assertEquals("STRICT a != @other", convertCoreToPl(parseAndOptimize("STRICT NOT a = @other")).toString());
        assertEquals("a IS NOT UNKNOWN", convertCoreToPl(parseAndOptimize("STRICT NOT a IS UNKNOWN")).toString());
        assertEquals("a STRICT NOT CONTAINS foobar", convertCoreToPl(parseAndOptimize("STRICT NOT a CONTAINS foobar")).toString());

        assertNoChangeAfterConversion("a IS UNKNOWN");
        assertNoChangeAfterConversion("a = 1 OR a IS UNKNOWN");
        assertNoChangeAfterConversion("a = 1");
        assertNoChangeAfterConversion("a > 1");
        assertNoChangeAfterConversion("a < 1");
        assertNoChangeAfterConversion("a CONTAINS b");
        assertNoChangeAfterConversion("a = @other");
        assertNoChangeAfterConversion("STRICT NOT a = @other");
        assertNoChangeAfterConversion("STRICT NOT a = @other OR a IS UNKNOWN OR other IS UNKNOWN");

        assertNoChangeAfterConversion("""
                a IS UNKNOWN
                OR (
                        STRICT NOT a = 1
                    AND STRICT NOT a = 2
                    AND (
                            STRICT NOT a = @other
                         OR other IS UNKNOWN
                        )
                    )""");

    }

    @Test
    void testRemoveStrict() {

        String expr = """
                a IS UNKNOWN
                OR (
                        STRICT NOT a = 1
                    AND STRICT NOT a = 2
                    AND (
                            STRICT NOT a = @other
                         OR other IS UNKNOWN
                        )
                    )""";

        CoreExpression expression = parseAndOptimize(expr);

        PlExpression<?> plExpression = convertCoreToPl(expression);

        assertEquals("a NOT ANY OF (1, 2, @other)", plExpression.format(FormatStyle.PRETTY_PRINT));

    }

    @Test
    void testLessThanGreaterThan() {

        String expr = "a <= 1";

        CoreExpression expression = parseAndOptimize(expr);

        PlExpression<?> plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a <= 1 AND b >= 0";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "NOT a <= 1";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "NOT a <= 1 AND NOT b >= 0";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "STRICT NOT a <= 1 AND NOT b >= 0";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a = 5 OR (r = 1 AND (q = 2 OR (STRICT NOT a <= 1 AND NOT b >= 0) ) )";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

    }

    @Test
    void testLessThanGreaterThanRef() {

        String expr = "a <= @b";

        CoreExpression expression = parseAndOptimize(expr);

        PlExpression<?> plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a <= @c AND b >= @c";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "NOT a <= @d";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "NOT a <= @b AND NOT b >= 0";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "STRICT NOT a <= @d AND NOT b >= 0";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a = 5 OR (r = 1 AND (q = 2 OR (STRICT NOT a <= @k AND NOT b >= 0) ) )";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

    }

    @Test
    void testBetween() {

        String expr = "a BETWEEN (1, 2)";

        CoreExpression expression = parseAndOptimize(expr);

        PlExpression<?> plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        String expr2 = "(a > 1 OR a = 1) AND (a < 2 OR a = 2)";

        expression = parseAndOptimize(expr2);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a >= @b AND a <= 2";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a NOT BETWEEN (1, 2)";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

    }

    @Test
    void testAnyOf() {

        String expr = "a ANY OF (1, 2)";

        CoreExpression expression = parseAndOptimize(expr);

        PlExpression<?> plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a ANY OF (1, 2, 3, @b)";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a NOT ANY OF (1, 2, 3, @b)";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a STRICT NOT ANY OF (1, 2, 3, @b)";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a >= 1 OR a ANY OF (2, 3, @b)";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

    }

    @Test
    void testContainsAnyOf() {

        String expr = "a CONTAINS ANY OF (bar, foo)";

        CoreExpression expression = parseAndOptimize(expr);

        PlExpression<?> plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a CONTAINS ANY OF (\"x y z\", bar, foo)";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "NOT a >= 1 AND a NOT CONTAINS ANY OF (\"2 words\", 1, 3)";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "STRICT a != 1 AND a NOT CONTAINS ANY OF (\"2 words\", 1, 3)";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a CONTAINS America AND a NOT CONTAINS ANY OF (\"Two words\", 1, 3)";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a CONTAINS foobar AND (b = 3 OR (a NOT CONTAINS ANY OF (\"2 words\", 1, 3) AND c = 5) )";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals(expr, plExpression.format(FormatStyle.INLINE));

        expr = "a STRICT NOT CONTAINS America AND a NOT CONTAINS ANY OF (\"2 words\", 1, 3)";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals("a STRICT NOT CONTAINS ANY OF (\"2 words\", 1, 3, America)", plExpression.format(FormatStyle.INLINE));

    }

    @Test
    void testComplex() {

        String expr = """
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
                             OR l = 93
                             OR l = 94
                             OR l = 95
                             OR (m >= 5 AND m <= 9)
                            )
                        )
                    )""";

        CoreExpression expression = parseAndOptimize(expr);

        PlExpression<?> plExpression = convertCoreToPl(expression);

        assertEquals("""
                a = 1
                AND (
                        (
                            b = 2
                        AND e = 9
                        AND foo >= low
                        AND foo < high
                        AND q = 10
                        )
                     OR (
                            c = 3
                        AND d = 5
                        AND (
                                k = 4
                             OR l ANY OF (92, 93, 94, 95)
                             OR m BETWEEN (5, 9)
                            )
                        )
                    )""", plExpression.format(FormatStyle.PRETTY_PRINT));

        expr = """
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
                             OR (l != 92 AND l != 93 AND l != 94 AND l != 95)
                             OR (m >= 5 AND m <= 9)
                            )
                        )
                    )""";

        expression = parseAndOptimize(expr);

        plExpression = convertCoreToPl(expression);

        assertEquals("""
                a = 1
                AND (
                        (
                            b = 2
                        AND e = 9
                        AND foo >= low
                        AND foo < high
                        AND q = 10
                        )
                     OR (
                            c = 3
                        AND d = 5
                        AND (
                                k = 4
                             OR l NOT ANY OF (92, 93, 94, 95)
                             OR m BETWEEN (5, 9)
                            )
                        )
                    )""", plExpression.format(FormatStyle.PRETTY_PRINT));

    }

    private static void assertNoChangeAfterConversion(String expressionString) {
        CoreExpression expr = parseAndOptimize(expressionString);
        assertEquals(expr, convertPlToCore(convertCoreToPl(expr)));
    }

    private static CoreExpression parseAndOptimize(String source) {
        return new PlToCoreExpressionConverter(new CoreExpressionOptimizer()).convert(parsePl(source).getResultExpression());
    }

    private static AudlangParseResult parsePl(String source) {
        return PlExpressionBuilder.stringToExpression(source);
    }

    private static CoreExpression convertPlToCore(PlExpression<?> expression) {
        return new PlToCoreExpressionConverter(new CoreExpressionOptimizer()).convert(expression);
    }

    private static PlExpression<?> convertCoreToPl(CoreExpression expression) {
        return new CoreToPlExpressionConverter().convert(expression);
    }

}
