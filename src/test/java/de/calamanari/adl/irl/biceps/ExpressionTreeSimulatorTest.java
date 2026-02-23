//@formatter:off
/*
 * ExpressionTreeSimulatorTest
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

package de.calamanari.adl.irl.biceps;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.cnv.PlToCoreExpressionConverter;
import de.calamanari.adl.erl.AudlangParseResult;
import de.calamanari.adl.erl.PlExpressionBuilder;
import de.calamanari.adl.irl.CoreExpression;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class ExpressionTreeSimulatorTest {

    static final Logger LOGGER = LoggerFactory.getLogger(ExpressionTreeSimulatorTest.class);

    @Test
    void testBasics() {

        String expr = "(a NOT ANY OF (1,2) AND b NOT ANY OF (1,2) AND c NOT ANY OF (1,2)) OR (a ANY OF (1,2) AND b ANY OF (1,2) AND c ANY OF (1,2))";

        String result = simulateComparisonParsedVsOptimized(expr);

        assertTrue(result.indexOf("No differences detected.") > 0);

        String expr1 = "a ANY OF (1, 2, 3) AND b = 2";
        String expr2 = "(a = 1 AND b = 2) OR (a = 2 AND b = 2) OR (a = 3 AND b = 2)";

        result = simulateComparison(expr1, expr2);

        assertTrue(result.indexOf("No differences detected.") > 0);

    }

    @Test
    @Disabled("Takes a couple of seconds")
    void testComplexAllTrue() {

        String expr = """
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

        String compResult = simulateComparisonParsedVsOptimized(expr);

        assertTrue(compResult.indexOf("No differences detected.") > 0);

        assertTrue(compResult.indexOf("Expression is always true") > 0);

    }

    @Test
    @Disabled("Takes many seconds")
    void testComplexAllTrue2() {
        String expr = """
                                (
                                        (
                                            (
                                                argName STRICT NOT ANY OF (a, @r)
                                             OR (
                                                    CURB (
                                                        argName =
                                                        value
                                                     OR argName >= @argName
                                                    ) = 2
                                                 OR argName ANY OF /* hugo */ /* */ (val14)
                                                )
                                            )
                                        AND (
                                                (
                                                    argName2 = 8
                                                AND argName >= @argName
                                                )
                                            )
                                        )
                                    AND (
                                            (
                                                (
                                                    argName = argValue
                                                 OR argName <= k
                                                )
                                            AND argName STRICT NOT ANY OF (a, @r)
                                            )
                                         OR (
                                                argName NOT BETWEEN (value, value)
                                            AND (
                                                    argName NOT CONTAINS ANY OF (
                                                        f,
                                                        "a=b"
                                                    )
                                                 OR argName = argValue
                                                )
                                            )
                                        )
                                    )
                """;

        String compResult = simulateComparisonParsedVsOptimized("(" + expr + ") OR NOT (" + expr + ")");

        assertTrue(compResult.indexOf("No differences detected.") > 0);

        assertTrue(compResult.indexOf("Expression is always true") > 0);

    }

    @Test
    void testDocumentationExample() {

        String expr = """
                color = blue
                AND (
                        engine = Diesel
                     OR engine = gas
                     OR (
                            engine = alternative
                        AND fuel = hydrogen
                        )
                    )""";

        ExpressionTreeSimulator simulator = new ExpressionTreeSimulator();

        String report = simulator.simulate(parse(expr));

        assertTrue(report.indexOf("TRUE") > -1);
        assertTrue(report.indexOf("FALSE") > -1);

        expr = "color = blue and engine any of (Diesel, Electric)";

        simulator = new ExpressionTreeSimulator();

        report = simulator.simulate(parse(expr));

        assertTrue(report.indexOf("TRUE") > -1);
        assertTrue(report.indexOf("FALSE") > -1);

    }

    @Test
    void testLevel() {

        GrowingIntArray gia = new GrowingIntArray(new int[] { 1, 2, 3, 4 }, false);

        ExpressionTreeLevel level = new ExpressionTreeLevel(gia);

        assertEquals(gia, level.members());

        assertArrayEquals(new int[] { 1, 2, 3, 4 }, level.members().toArray());

        level.clear();

        assertEquals(gia, level.members());

        assertArrayEquals(new int[] {}, level.members().toArray());

        assertThrows(IllegalArgumentException.class, () -> new ExpressionTreeLevel(null));
    }

    private static String simulateComparisonParsedVsOptimized(String expr) {
        ExpressionTreeSimulator simulator = new ExpressionTreeSimulator();

        return simulator.simulateComparison(parse(expr), parseAndOptimize(expr));

    }

    private static String simulateComparison(String expr1, String expr2) {
        ExpressionTreeSimulator simulator = new ExpressionTreeSimulator();

        return simulator.simulateComparison(parse(expr1), parse(expr2));

    }

    private static CoreExpression parseAndOptimize(String source) {
        return new PlToCoreExpressionConverter(new CoreExpressionOptimizer()).convert(parsePl(source).getResultExpression());
    }

    private static CoreExpression parse(String source) {
        return new PlToCoreExpressionConverter().convert(parsePl(source).getResultExpression());
    }

    private static AudlangParseResult parsePl(String source) {
        return PlExpressionBuilder.stringToExpression(source);
    }
}
