//@formatter:off
/*
 * NegationExpressionTest
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
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.cnv.StandardConversions;
import de.calamanari.adl.erl.AudlangParseResult;
import de.calamanari.adl.erl.PlExpression;
import de.calamanari.adl.erl.PlExpressionBuilder;
import de.calamanari.adl.util.JsonUtils;

import static de.calamanari.adl.FormatStyle.INLINE;
import static de.calamanari.adl.FormatStyle.PRETTY_PRINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class NegationExpressionTest {

    static final Logger LOGGER = LoggerFactory.getLogger(NegationExpressionTest.class);

    @Test
    void testBasics() {

        assertEquals("<NONE>", parse("NOT <ALL>").toString());
        assertEquals("<NONE>", parse("NOT <ALL>").format(FormatStyle.INLINE));
        assertEquals("<NONE>", parse("NOT <ALL>").format(FormatStyle.PRETTY_PRINT));

        assertEquals("<NONE>", parse("STRICT NOT <ALL>").toString());
        assertEquals("<NONE>", parse("STRICT NOT <ALL>").format(FormatStyle.INLINE));
        assertEquals("<NONE>", parse("STRICT NOT <ALL>").format(FormatStyle.PRETTY_PRINT));

        assertEquals("STRICT NOT a = 1 OR a IS UNKNOWN OR b IS UNKNOWN OR (STRICT NOT b = 2 AND STRICT NOT b = 3 AND STRICT NOT b = 4)",
                parse("NOT (a = 1 AND b ANY OF (2, 3, 4) )").toString());
        assertEquals("STRICT NOT a = 1 OR a IS UNKNOWN OR b IS UNKNOWN OR (STRICT NOT b = 2 AND STRICT NOT b = 3 AND STRICT NOT b = 4)",
                parse("NOT (a = 1 AND b ANY OF (2, 3, 4) )").format(INLINE));

        assertEquals("""
                STRICT NOT a = 1
                OR a IS UNKNOWN
                OR b IS UNKNOWN
                OR (
                        STRICT NOT b = 2
                    AND STRICT NOT b = 3
                    AND STRICT NOT b = 4
                    )""", parse("NOT (a = 1 AND b ANY OF (2, 3, 4) )").format(PRETTY_PRINT));

        assertEquals("a = 1 AND (b = 2 OR b = 3 OR b = 4)", parse("NOT NOT (a = 1 AND b ANY OF (2, 3, 4) )").toString());
        assertEquals("a = 1 AND (b = 2 OR b = 3 OR b = 4)", parse("NOT NOT (a = 1 AND b ANY OF (2, 3, 4) )").format(INLINE));

        assertEquals("""
                a = 1
                AND (
                        b = 2
                     OR b = 3
                     OR b = 4
                    )""", parse("NOT NOT (a = 1 AND b ANY OF (2, 3, 4) )").format(PRETTY_PRINT));

        assertEquals("STRICT NOT a = 1 OR a IS UNKNOWN", parse("a = 1").negate(false).toString());
        assertEquals("STRICT NOT a = 1", parse("a = 1").negate(true).toString());

        assertEquals("STRICT NOT a = @b OR a IS UNKNOWN OR b IS UNKNOWN", parse("a = @b").negate(false).toString());
        assertEquals("STRICT NOT a = @b", parse("a = @b").negate(true).toString());

        assertEquals("STRICT NOT a = 1", NegationExpression.of(parse("a = 1"), true).toString());
        assertEquals("STRICT NOT a = 1 OR a IS UNKNOWN", NegationExpression.of(parse("a = 1"), false).toString());

    }

    @Test
    void testSpecialCases() {

        assertEquals("<NONE>", parse("STRICT NOT argName = @argName").toString());
        assertEquals("STRICT NOT argName IS UNKNOWN", parse("argName = @argName").toString());

        assertThrows(AudlangValidationException.class, () -> new NegationExpression(null, null));

        assertEquals(1, parse("STRICT NOT a = 1").compareTo(parse("a = 1")));
        assertEquals(-1, parse("a = 1").compareTo(parse("STRICT NOT a = 1")));

    }

    @Test
    void testEqualsHashCode() {

        CoreExpression expr = parse("STRICT NOT a = 1");
        CoreExpression expr2 = parse("STRICT a != 1");

        assertTrue(expr instanceof SimpleExpression);

        assertNotSame(expr, expr2);

        assertEquals(expr, expr2);

        assertEquals(expr.hashCode(), expr2.hashCode());

    }

    @Test
    void testGetAllFields() {

        CoreExpression expr = parse("arg STRICT NOT ANY OF (v1, v2, @ref1, v3, v4, v5)");

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

        CoreExpression expr = parse("arg NOT ANY OF (v1, v2, @ref1, v3, v4, v5)");

        String json = JsonUtils.writeAsJsonString(expr, true);

        CoreExpression res = JsonUtils.readFromJsonString(json, CoreExpression.class);

        assertEquals(expr, res);

        assertThrows(RuntimeException.class, () -> JsonUtils.readFromJsonString(json, PlExpression.class));

        PlExpression<?> exprPl = parsePl("arg NOT ANY OF (v1, v2, @ref1, v3, v4, v5)").getResultExpression();
        String jsonPl = JsonUtils.writeAsJsonString(exprPl, true);

        assertThrows(RuntimeException.class, () -> JsonUtils.readFromJsonString(jsonPl, CoreExpression.class));

    }

    private static CoreExpression parse(String source) {
        return StandardConversions.parseCoreExpression(source);
    }

    private static AudlangParseResult parsePl(String source) {
        return PlExpressionBuilder.stringToExpression(source);
    }

}
