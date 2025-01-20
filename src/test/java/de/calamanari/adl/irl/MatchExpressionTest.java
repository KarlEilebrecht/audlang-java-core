//@formatter:off
/*
 * MatchExpressionTest
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

import static de.calamanari.adl.FormatStyle.INLINE;
import static de.calamanari.adl.FormatStyle.PRETTY_PRINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.AudlangField;
import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.cnv.CoreToPlExpressionConverter;
import de.calamanari.adl.cnv.StandardConversions;
import de.calamanari.adl.erl.AudlangParseResult;
import de.calamanari.adl.erl.PlExpression;
import de.calamanari.adl.erl.PlExpressionBuilder;
import de.calamanari.adl.irl.biceps.EncodedExpressionTree;
import de.calamanari.adl.irl.biceps.ImplicationResolver;
import de.calamanari.adl.util.JsonUtils;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class MatchExpressionTest {

    static final Logger LOGGER = LoggerFactory.getLogger(MatchExpressionTest.class);

    private static final List<MatchOperator> SIMPLE_MATCH_OPERATORS = Arrays.asList(MatchOperator.EQUALS, MatchOperator.LESS_THAN, MatchOperator.GREATER_THAN);

    private static final Operand SOME_VALUE_OPERAND = Operand.of("fooBar", false);
    private static final Operand SOME_REF_OPERAND = Operand.of("fooBar", true);

    @Test
    void testWhatHappens() {

        assertEquals("b = 2", parse("(a = @k AND b=2) OR (a != @k AND b=2) OR (a IS UNKNOWN AND b=2) OR (k IS UNKNOWN AND b=2)").toString());

        assertEquals("a = 1", parse("STRICT NOT a IS UNKNOWN AND (a = 1 OR a IS UNKNOWN)").toString());

        assertEquals("<ALL>", parse("a=1 or a != 1").toString());

        assertEquals("STRICT NOT a IS UNKNOWN", parse("a=1 or STRICT a != 1").toString());

        assertEquals("a = 1 AND b = 2", parse("(a=1 and b=2 and c=3) OR (a=1 and b=2)").toString());

        assertEquals("STRICT NOT a IS UNKNOWN AND b = 2", parse("(a=1 and b=2) OR (STRICT a!=1 and b=2)").toString());

        assertEquals("a IS UNKNOWN AND b = 2", parse("a IS UNKNOWN AND (b = 2 OR (c=3 AND a=1))").toString());

        assertEquals("STRICT NOT a IS UNKNOWN AND b = 1 AND STRICT NOT c IS UNKNOWN", parse("(a = @c AND b = 1) OR (STRICT a != @c AND b = 1)").toString());

        assertEquals("b = 2 AND (a IS UNKNOWN OR k IS UNKNOWN)", parse("( a IS UNKNOWN AND b = 2 ) OR ( b = 2 AND k IS UNKNOWN )").toString());

    }

    @Test
    void testBasics() {

        assertEquals("a IS UNKNOWN", parse("a IS UNKNOWN").toString());
        assertEquals("a IS UNKNOWN", parse("a IS UNKNOWN").format(INLINE));
        assertEquals("a IS UNKNOWN", parse("a IS UNKNOWN").format(PRETTY_PRINT));

        assertEquals("a = 1", parse("a = 1").toString());
        assertEquals("a = 1", parse("a = 1").format(INLINE));
        assertEquals("a = 1", parse("a = 1").format(PRETTY_PRINT));

        assertEquals("a > 1", parse("a > 1").toString());
        assertEquals("a > 1", parse("a > 1").format(INLINE));
        assertEquals("a > 1", parse("a > 1").format(PRETTY_PRINT));

        assertEquals("a < 1", parse("a < 1").toString());
        assertEquals("a < 1", parse("a < 1").format(INLINE));
        assertEquals("a < 1", parse("a < 1").format(PRETTY_PRINT));

        assertEquals("a CONTAINS b", parse("a CONTAINS b").toString());
        assertEquals("a CONTAINS b", parse("a CONTAINS b").format(INLINE));
        assertEquals("a CONTAINS b", parse("a CONTAINS b").format(PRETTY_PRINT));

        assertEquals("a = @other", parse("a = @other").toString());
        assertEquals("a = @other", parse("a = @other").format(INLINE));
        assertEquals("a = @other", parse("a = @other").format(PRETTY_PRINT));

        assertEquals("a = @other", parse("other = @a").toString());
        assertEquals("a > @other", parse("other < @a").toString());
        assertEquals("a < @other", parse("other > @a").toString());

        assertEquals("STRICT NOT a = @other", parse("a = @other").negate(true).toString());
        assertEquals("STRICT NOT a = @other OR a IS UNKNOWN OR other IS UNKNOWN", parse("a = @other").negate(false).toString());

    }

    @Test
    void testSpecial() {

        assertEquals(1, parse("a = 1").depth());

        assertEquals(1, parse("a = 1").compareTo(SpecialSetExpression.all()));
        assertEquals(1, parse("a = 1").compareTo(SpecialSetExpression.none()));

        assertEquals(1, parse("STRICT a != 1").compareTo(parse("a = 1")));
        assertEquals(-1, parse("a = 1").compareTo(parse("STRICT a != 1")));

        assertEquals("""
                a IS UNKNOWN
                OR (
                        STRICT NOT a = 1
                    AND STRICT NOT a = 2
                    AND (
                            STRICT NOT a = @other
                         OR other IS UNKNOWN
                        )
                    )""", parse("a NOT ANY OF (1, 2, @other)").format(PRETTY_PRINT));

    }

    @Test
    void testHigherLanguageFeatures() {

        assertEquals("STRICT NOT a IS UNKNOWN", parse("a IS NOT UNKNOWN").toString());
        assertEquals("STRICT NOT a IS UNKNOWN", parse("a IS NOT UNKNOWN").format(INLINE));
        assertEquals("STRICT NOT a IS UNKNOWN", parse("a IS NOT UNKNOWN").format(PRETTY_PRINT));

        assertEquals("STRICT NOT a = 1 OR a IS UNKNOWN", parse("a != 1").toString());
        assertEquals("STRICT NOT a = 1 OR a IS UNKNOWN", parse("a != 1").format(INLINE));

        assertEquals("""
                STRICT NOT a = 1
                OR a IS UNKNOWN""", parse("a != 1").format(PRETTY_PRINT));

        assertEquals("STRICT NOT a = 1", parse("STRICT a != 1").toString());
        assertEquals("STRICT NOT a = 1", parse("STRICT a != 1").format(INLINE));
        assertEquals("STRICT NOT a = 1", parse("STRICT a != 1").format(PRETTY_PRINT));

        assertEquals("a > 1 OR a = 1", parse("a >= 1").toString());
        assertEquals("a > 1 OR a = 1", parse("a >= 1").format(INLINE));
        assertEquals("""
                a > 1
                OR a = 1""", parse("a >= 1").format(PRETTY_PRINT));

        assertEquals("a < 1 OR a = 1", parse("a <= 1").toString());
        assertEquals("a < 1 OR a = 1", parse("a <= 1").format(INLINE));
        assertEquals("""
                a < 1
                OR a = 1""", parse("a <= 1").format(PRETTY_PRINT));

        assertEquals("a = 1 OR a = 2 OR a = 3", parse("a ANY OF (1, 2, 3)").toString());

        assertEquals("""
                a IS UNKNOWN
                OR (
                        STRICT NOT a = 1
                    AND STRICT NOT a = 2
                    AND STRICT NOT a = 3
                    )""", parse("a NOT ANY OF (1, 2, 3)").format(PRETTY_PRINT));

        assertEquals("""
                a IS UNKNOWN
                OR (
                        STRICT NOT a = 1
                    AND STRICT NOT a = 2
                    AND (
                            STRICT NOT a = @other
                         OR other IS UNKNOWN
                        )
                    )""", parse("a NOT ANY OF (1, 2, @other)").format(PRETTY_PRINT));

        assertEquals("""
                STRICT NOT a = 1
                AND STRICT NOT a = 2
                AND STRICT NOT a = 3""", parse("a STRICT NOT ANY OF (1, 2, 3)").format(PRETTY_PRINT));

        assertEquals("""
                STRICT NOT a = 1
                AND STRICT NOT a = 2
                AND STRICT NOT a = @other""", parse("a STRICT NOT ANY OF (1, 2, @other)").format(PRETTY_PRINT));

        assertEquals("a CONTAINS 1 OR a CONTAINS 2 OR a CONTAINS 3", parse("a CONTAINS ANY OF (1, 2, 3)").toString());

        assertEquals("""
                a IS UNKNOWN
                OR (
                        STRICT NOT a CONTAINS 1
                    AND STRICT NOT a CONTAINS 2
                    AND STRICT NOT a CONTAINS 3
                    )""", parse("a NOT CONTAINS ANY OF (1, 2, 3)").format(PRETTY_PRINT));

        assertEquals("""
                STRICT NOT a CONTAINS 1
                AND STRICT NOT a CONTAINS 2
                AND STRICT NOT a CONTAINS 3""", parse("a STRICT NOT CONTAINS ANY OF (1, 2, 3)").format(PRETTY_PRINT));

        assertEquals("""
                (
                        STRICT NOT a < 5
                    AND STRICT NOT a = 5
                    )
                OR (
                        STRICT NOT a = 1
                    AND STRICT NOT a > 1
                    )""", parse("a STRICT NOT BETWEEN (1, 5)").format(PRETTY_PRINT));

        assertEquals("""
                a IS UNKNOWN
                OR (
                        STRICT NOT a < 5
                    AND STRICT NOT a = 5
                    )
                OR (
                        STRICT NOT a = 1
                    AND STRICT NOT a > 1
                    )""", parse("a NOT BETWEEN (1, 5)").format(PRETTY_PRINT));

    }

    @Test
    void testNegate() {
        assertEquals("STRICT NOT a IS UNKNOWN", parse("a IS UNKNOWN").negate(true).toString());
        assertEquals("STRICT NOT a IS UNKNOWN", parse("a IS UNKNOWN").negate(false).toString());

        assertEquals("STRICT NOT a = 1", parse("a = 1").negate(true).toString());
        assertEquals("a = 1", parse("a = 1").negate(true).negate(true).toString());
        assertEquals("STRICT NOT a = 1", parse("a = 1").negate(true).negate(true).negate(true).toString());

        assertEquals("STRICT NOT a = @b", parse("a = @b").negate(true).toString());
        assertEquals("a = @b", parse("a = @b").negate(true).negate(true).toString());
        assertEquals("STRICT NOT a = @b", parse("a = @b").negate(true).negate(true).negate(true).toString());

        assertEquals("STRICT NOT a = 1 OR a IS UNKNOWN", parse("a = 1").negate(false).toString());

        assertEquals("a = 1", parse(parse("a = 1").negate(false).negate(false).toString()).toString());
        assertEquals("STRICT NOT a = 1 OR a IS UNKNOWN", parse(parse("a = 1").negate(false).negate(false).negate(false).toString()).toString());

        assertEquals("STRICT NOT a = @b OR a IS UNKNOWN OR b IS UNKNOWN", parse("a = @b").negate(false).toString());
        assertEquals("a = @b", parse(parse("a = @b").negate(false).negate(false).toString()).toString());
        assertEquals("STRICT NOT a = @b OR a IS UNKNOWN OR b IS UNKNOWN",
                parse(parse("a = @b").negate(false).negate(false).negate(false).toString()).toString());

    }

    @Test
    void testNotUnknownOverlapWinnerCondition() {
        assertEquals("STRICT NOT a IS UNKNOWN AND b = 2", parse("(a IS NOT UNKNOWN AND b=2) OR (a=1 AND b=2)").toString());
        assertEquals("STRICT NOT a IS UNKNOWN AND b = 2", parse("(a IS NOT UNKNOWN AND b=2) OR (a=1 AND b=2) OR (a=2 AND b=2)").toString());
        assertEquals("(STRICT NOT a IS UNKNOWN AND b = 2) OR (a = 2 AND b = 3)",
                parse("(a IS NOT UNKNOWN AND b=2) OR (a=1 AND b=2) OR (a=2 AND b=3)").toString());

        assertEquals("b = 2 AND STRICT NOT c IS UNKNOWN", parse("(c IS NOT UNKNOWN AND b=2) OR (a=@c AND b=2)").toString());

    }

    @Test
    void testNotUnknownVsIsUnknownOverlap() {
        assertEquals("b = 2", parse("(a IS NOT UNKNOWN AND b=2) OR (a IS UNKNOWN AND b=2)").toString());
        assertEquals("b = 2 OR (a IS UNKNOWN AND c = 1)", parse("(a IS NOT UNKNOWN AND b=2) OR (a IS UNKNOWN AND (b=2 OR c=1))").toString());
        assertEquals("STRICT NOT b = 2", parse("(a IS NOT UNKNOWN AND STRICT NOT b=2) OR (a IS UNKNOWN AND STRICT NOT b=2)").toString());
        assertEquals("STRICT NOT b = 2 OR b IS UNKNOWN", parse("(a IS NOT UNKNOWN AND NOT b=2) OR (a IS UNKNOWN AND NOT b=2)").toString());
    }

    @Test
    void testIrrelevantMatchConditions() {
        assertEquals("b = 2 AND (a = 1 OR a IS UNKNOWN)", parse("(a = 1 AND b=2) OR (a IS UNKNOWN AND b=2)").toString());

        assertEquals("b = 2", parse("(a = 1 AND b=2) OR (a != 1 AND b=2) OR (a IS UNKNOWN AND b=2)").toString());
        assertEquals("b = 2", parse("(a = @k AND b=2) OR (a != @k AND b=2) OR (a IS UNKNOWN AND b=2) OR (k IS UNKNOWN AND b=2)").toString());
        assertEquals("b = 2", parse(
                "(a = @k AND b=2) OR (a != @k AND b=2) OR (a IS UNKNOWN AND b=2) OR (k IS UNKNOWN AND b=2) OR (s=1 AND b=2) OR (s!=1 AND b=2) OR (s IS UNKNOWN AND b=2)")
                        .toString());
    }

    @Test
    void testComplement() {

        assertEquals("a = 1", makeComplement(parse("a != 1")).toString());

        assertEquals("STRICT NOT a = 1 OR a IS UNKNOWN", makeComplement(parse("a = 1")).toString());

    }

    private static CoreExpression makeComplement(CoreExpression expression) {
        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(expression);
        ImplicationResolver implicationResolver = new ImplicationResolver(null);
        return tree
                .createCoreExpression(implicationResolver.cleanupImplications(tree, tree.getLogicHelper().createComplementOf(tree, tree.getRootNode()), false));
    }

    @Test
    void testSpecialCases() {

        assertEquals("STRICT NOT a IS UNKNOWN", parse("a IS NOT UNKNOWN").toString());
        assertEquals("STRICT NOT a IS UNKNOWN", parse("STRICT NOT a IS UNKNOWN").toString());

        assertEquals("<NONE>", parse("STRICT NOT a IS NOT UNKNOWN").toString());
        assertEquals("a IS UNKNOWN", parse("NOT STRICT NOT a IS UNKNOWN").toString());
        assertEquals("STRICT NOT a IS UNKNOWN", parse("NOT NOT STRICT NOT a IS UNKNOWN").toString());
        assertEquals("a IS UNKNOWN", parse("NOT NOT NOT STRICT NOT a IS UNKNOWN").toString());

        assertEquals("arg CONTAINS \"\"", new MatchExpression("arg", MatchOperator.CONTAINS, Operand.of("", false), null).toString());

        assertEquals("STRICT NOT arg IS UNKNOWN", MatchExpression.of("arg", MatchOperator.CONTAINS, Operand.of("", false)).toString());

        assertThrows(AudlangValidationException.class, () -> new MatchExpression(null, MatchOperator.IS_UNKNOWN, null, null));

        assertThrows(AudlangValidationException.class, () -> new MatchExpression("arg", MatchOperator.IS_UNKNOWN, SOME_VALUE_OPERAND, null));

        assertThrows(AudlangValidationException.class, () -> new MatchExpression("arg", null, SOME_VALUE_OPERAND, null));

        for (MatchOperator operator : SIMPLE_MATCH_OPERATORS) {
            assertThrows(AudlangValidationException.class, () -> new MatchExpression("arg", operator, null, null));
        }
        assertThrows(AudlangValidationException.class, () -> new MatchExpression("arg", MatchOperator.CONTAINS, null, null));

        assertThrows(AudlangValidationException.class, () -> new MatchExpression("arg", MatchOperator.CONTAINS, SOME_REF_OPERAND, null));

        assertThrows(AudlangValidationException.class, () -> new MatchExpression("fooBar", MatchOperator.EQUALS, SOME_REF_OPERAND, null));

        assertThrows(AudlangValidationException.class, () -> Operand.of(null, false));
        assertThrows(AudlangValidationException.class, () -> Operand.of(null, true));

    }

    @Test
    void testEqualsHashCode() {

        CoreExpression expr = MatchExpression.isUnknown("arg");
        CoreExpression expr2 = MatchExpression.isUnknown("arg");

        assertTrue(expr instanceof MatchExpression);

        assertNotSame(expr, expr2);

        assertEquals(expr, expr2);

        assertEquals(expr.hashCode(), expr2.hashCode());

    }

    @Test
    void testGetAllFields() {

        CoreExpression expr = parse("arg ANY OF (v1, v2, @ref1, v3, v4, v5)");

        // @formatter:on
        List<String> expectedFieldNames = new ArrayList<>(Arrays.asList("arg", "ref1"));
        Collections.sort(expectedFieldNames);
        assertEquals(expectedFieldNames, expr.allArgNames());

        List<CoreExpression> collectedExpressions = new ArrayList<>();
        expr.collectExpressions(e -> e instanceof MatchExpression m && m.argName().equals("arg"), collectedExpressions);
        assertEquals(6, collectedExpressions.size());

        AudlangField field = expr.allFields().stream().filter(f -> f.argName().equals("arg")).toList().get(0);
        assertEquals(Arrays.asList("v1", "v2", "v3", "v4", "v5"), field.values());
        assertEquals(Arrays.asList("ref1"), field.refArgNames());

        field = expr.allFields().stream().filter(f -> f.argName().equals("ref1")).toList().get(0);
        assertTrue(field.values().isEmpty());
        assertEquals(Arrays.asList("arg"), field.refArgNames());

    }

    @Test
    void testJson() {

        CoreExpression expr = parse("arg ANY OF (v1, v2, @ref1, v3, v4, v5)");

        String json = JsonUtils.writeAsJsonString(expr, true);

        CoreExpression res = JsonUtils.readFromJsonString(json, CoreExpression.class);

        assertEquals(expr, res);

        assertThrows(RuntimeException.class, () -> JsonUtils.readFromJsonString(json, PlExpression.class));

        PlExpression<?> exprPl = parsePl("arg ANY OF (v1, v2, @ref1, v3, v4, v5)").getResultExpression();
        String jsonPl = JsonUtils.writeAsJsonString(exprPl, true);

        assertThrows(RuntimeException.class, () -> JsonUtils.readFromJsonString(jsonPl, CoreExpression.class));

    }

    @Test
    void testPrepareContext() {
        CoreToPlExpressionConverter converter = new CoreToPlExpressionConverter();

        AtomicInteger counter = new AtomicInteger();

        CoreExpression expr = parse("a = 1");

        converter.setContextPreparator(ctx -> {
            counter.incrementAndGet();
            return ctx;
        });
        assertEquals("a = 1", converter.convert(expr).toString());

        // root context and one level context
        assertEquals(2, counter.get());

    }

    private static CoreExpression parse(String source) {
        return StandardConversions.parseCoreExpression(source);
    }

    private static AudlangParseResult parsePl(String source) {
        return PlExpressionBuilder.stringToExpression(source);
    }
}
