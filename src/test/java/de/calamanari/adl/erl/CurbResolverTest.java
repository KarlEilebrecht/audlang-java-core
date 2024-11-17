//@formatter:off
/*
 * CurbResolverTest
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

import static de.calamanari.adl.erl.CurbResolver.resolve;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.FormatStyle;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class CurbResolverTest {

    @Test
    void testBasicResolution() {

        assertEquals("a = 1 OR b = 1 OR c > 3", resolve(parse("CURB(a=1 OR b=1 OR c>3) > 0")).toString());
        assertEquals("a = 1 AND b = 1 AND c > 3", resolve(parse("CURB(a=1 OR b=1 OR c>3) > 2")).toString());
        assertEquals("<NONE>", resolve(parse("CURB(a=1 OR b=1 OR c>3) > 3")).toString());
        assertEquals("<NONE>", resolve(parse("CURB(a=1 OR b=1 OR c>3) < 0")).toString());
        assertEquals("<ALL>", resolve(parse("CURB(a=1 OR b=1 OR c>3) < 4")).toString());
        assertEquals("(STRICT NOT a = 1 OR a IS UNKNOWN) AND (STRICT NOT b = 1 OR b IS UNKNOWN) AND (STRICT NOT c > 3 OR c IS UNKNOWN)",
                resolve(parse("CURB(a=1 OR b=1 OR c>3) < 1")).toString());
        assertEquals(
                "( (STRICT NOT a = 1 OR a IS UNKNOWN) AND (STRICT NOT b = 1 OR b IS UNKNOWN) ) "
                        + "OR ( (STRICT NOT a = 1 OR a IS UNKNOWN) AND (STRICT NOT c > 3 OR c IS UNKNOWN) ) "
                        + "OR ( (STRICT NOT b = 1 OR b IS UNKNOWN) AND (STRICT NOT c > 3 OR c IS UNKNOWN) )",
                resolve(parse("CURB(a=1 OR b=1 OR c>3) < 2")).toString());
        assertEquals("(STRICT NOT a = 1 OR a IS UNKNOWN) OR (STRICT NOT b = 1 OR b IS UNKNOWN) OR (STRICT NOT c > 3 OR c IS UNKNOWN)",
                resolve(parse("CURB(a=1 OR b=1 OR c>3) < 3")).toString());

        assertEquals("(a = 1 AND b = 1) OR (a = 1 AND c > 3) OR (b = 1 AND c > 3)", resolve(parse("CURB(a=1 OR b=1 OR c>3) > 1")).toString());

        assertEquals("(a = 1 AND b = 1) OR (a = 1 AND c > 3) OR (a = 1 AND d = 5) OR (b = 1 AND c > 3) OR (b = 1 AND d = 5) OR (c > 3 AND d = 5)",
                resolve(parse("CURB(a=1 OR b=1 OR c>3 OR d=5) > 1")).toString());

        assertEquals(
                "( (STRICT NOT a = 1 OR a IS UNKNOWN) AND (STRICT NOT b = 1 OR b IS UNKNOWN) ) "
                        + "OR ( (STRICT NOT a = 1 OR a IS UNKNOWN) AND (STRICT NOT c > 3 OR c IS UNKNOWN) ) "
                        + "OR ( (STRICT NOT a = 1 OR a IS UNKNOWN) AND (STRICT NOT d = 5 OR d IS UNKNOWN) ) "
                        + "OR ( (STRICT NOT b = 1 OR b IS UNKNOWN) AND (STRICT NOT c > 3 OR c IS UNKNOWN) ) "
                        + "OR ( (STRICT NOT b = 1 OR b IS UNKNOWN) AND (STRICT NOT d = 5 OR d IS UNKNOWN) ) "
                        + "OR ( (STRICT NOT c > 3 OR c IS UNKNOWN) AND (STRICT NOT d = 5 OR d IS UNKNOWN) )",
                resolve(parse("CURB(a=1 OR b=1 OR c>3 OR d=5) < 3")).toString());

        assertEquals("(STRICT NOT a = 1 OR a IS UNKNOWN) OR (STRICT NOT b = 1 OR b IS UNKNOWN) OR (STRICT NOT c > 3 OR c IS UNKNOWN) "
                + "OR (STRICT NOT d = 5 OR d IS UNKNOWN)", resolve(parse("CURB(a=1 OR b=1 OR c>3 OR d=5) < 4")).toString());

        assertEquals(
                "( (STRICT NOT a = 1 OR a IS UNKNOWN) AND (STRICT NOT b = 1 OR b IS UNKNOWN) AND (STRICT NOT c > 3 OR c IS UNKNOWN) ) "
                        + "OR ( (STRICT NOT a = 1 OR a IS UNKNOWN) AND (STRICT NOT b = 1 OR b IS UNKNOWN) AND (STRICT NOT d = 5 OR d IS UNKNOWN) ) "
                        + "OR ( (STRICT NOT a = 1 OR a IS UNKNOWN) AND (STRICT NOT c > 3 OR c IS UNKNOWN) AND (STRICT NOT d = 5 OR d IS UNKNOWN) ) "
                        + "OR ( (STRICT NOT b = 1 OR b IS UNKNOWN) AND (STRICT NOT c > 3 OR c IS UNKNOWN) AND (STRICT NOT d = 5 OR d IS UNKNOWN) )",
                resolve(parse("CURB(a=1 OR b=1 OR c>3 OR d=5) < 2")).toString());

    }

    @Test
    void testNotEqualsResolution() {
        assertEquals("a = 1 OR b = 1 OR c > 3", resolve(parse("CURB(a=1 OR b=1 OR c>3) != 0")).toString());
        assertEquals("<ALL>", resolve(parse("CURB(a=1 OR b=1 OR c>3) != 4")).toString());
        assertEquals("(STRICT NOT a = 1 OR a IS UNKNOWN) OR (STRICT NOT b = 1 OR b IS UNKNOWN) OR (STRICT NOT c > 3 OR c IS UNKNOWN)",
                resolve(parse("CURB(a=1 OR b=1 OR c>3) != 3")).toString());

        assertEquals("""
                (
                        (
                            STRICT NOT a = 1
                         OR a IS UNKNOWN
                        )
                    AND (
                            STRICT NOT b = 1
                         OR b IS UNKNOWN
                        )
                    AND (
                            STRICT NOT c > 3
                         OR c IS UNKNOWN
                        )
                    )
                OR (
                        (
                            a = 1
                        AND b = 1
                        )
                     OR (
                            a = 1
                        AND c > 3
                        )
                     OR (
                            b = 1
                        AND c > 3
                        )
                    )""", resolve(parse("CURB(a=1 OR b=1 OR c>3) != 1")).format(FormatStyle.PRETTY_PRINT));

    }

    @Test
    void testOverload() {

        assertNotNull(
                resolve(parse("CURB(a=1 OR b=1 OR c=1 OR d=1 OR e=1 OR f=1 OR g=1 OR h=1 OR i=1 OR j=1 OR k=1 OR l=1) != 6")).format(FormatStyle.PRETTY_PRINT));

        PlCurbExpression expr = parse("CURB(a=1 OR b=1 OR c=1 OR d=1 OR e=1 OR f=1 OR g=1 OR h=1 OR i=1 OR j=1 OR k=1 OR l=1 OR m=1 OR n=1) != 7");
        assertThrows(CurbComplexityException.class, () -> resolve(expr));

    }

    private static PlCurbExpression parse(String source) {
        return (PlCurbExpression) PlExpressionBuilder.stringToExpression(source).getResultExpression();
    }

}
