//@formatter:off
/*
 * IsUnknownRemovalConverterTest
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.cnv.tps.DefaultAdlType;
import de.calamanari.adl.cnv.tps.DefaultArgMetaInfoLookup;
import de.calamanari.adl.erl.AudlangParseResult;
import de.calamanari.adl.erl.PlExpressionBuilder;
import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.biceps.CoreExpressionOptimizer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class IsUnknownRemovalConverterTest {

    @Test
    void testBasics() {

        IsUnknownRemovalConverter converter = new IsUnknownRemovalConverter(Arrays.asList("favcolor", "taste", "shape"));

        String expr = "color != @favcolor OR color = blue OR taste != blueberry OR ( (STRICT NOT look = nice OR look IS UNKNOWN) AND (shape != circle OR taste = lemon) ) OR (look = nice AND shape = square)";

        assertEquals("""
                STRICT NOT color = @favcolor
                OR color = blue
                OR color IS UNKNOWN
                OR favcolor IS UNKNOWN
                OR STRICT NOT taste = blueberry
                OR taste IS UNKNOWN
                OR (
                        (
                            STRICT NOT look = nice
                         OR look IS UNKNOWN
                        )
                    AND (
                            STRICT NOT shape = circle
                         OR shape IS UNKNOWN
                         OR taste = lemon
                        )
                    )
                OR (
                        look = nice
                    AND shape = square
                    )""", parse(expr).format(FormatStyle.PRETTY_PRINT));

        assertEquals("""
                STRICT NOT color = @favcolor
                OR color = blue
                OR favcolor IS UNKNOWN
                OR STRICT NOT taste = blueberry
                OR taste IS UNKNOWN
                OR (
                        STRICT NOT look = nice
                    AND (
                            STRICT NOT shape = circle
                         OR shape IS UNKNOWN
                         OR taste = lemon
                        )
                    )
                OR (
                        look = nice
                    AND shape = square
                    )""", converter.convert(parse(expr)).format(FormatStyle.PRETTY_PRINT));

        new IsUnknownRemovalConverter((List<String>) null);

        converter = new IsUnknownRemovalConverter(Collections.emptyList());

        assertEquals("""
                STRICT NOT color = @favcolor
                OR color = blue
                OR STRICT NOT taste = blueberry
                OR (
                        STRICT NOT look = nice
                    AND (
                            STRICT NOT shape = circle
                         OR taste = lemon
                        )
                    )
                OR (
                        look = nice
                    AND shape = square
                    )""", converter.convert(parse(expr)).format(FormatStyle.PRETTY_PRINT));

        assertEquals("<NONE>", converter.convert(parse("<NONE>")).format(FormatStyle.PRETTY_PRINT));
        assertEquals("<ALL>", converter.convert(parse("<ALL>")).format(FormatStyle.PRETTY_PRINT));

    }

    @Test
    void testSpecialCase() {
        IsUnknownRemovalConverter converter = new IsUnknownRemovalConverter(Collections.emptyList());

        String expr = "color = @favcolor OR STRICT color != @favcolor OR taste = lemon OR STRICT taste != lemon";
        assertEquals("""
                STRICT NOT taste IS UNKNOWN
                OR (
                        STRICT NOT color IS UNKNOWN
                    AND STRICT NOT favcolor IS UNKNOWN
                    )""", parse(expr).format(FormatStyle.PRETTY_PRINT));

        assertEquals("<ALL>", converter.convert(parse(expr)).format(FormatStyle.PRETTY_PRINT));

        expr = "color IS UNKNOWN OR favcolor IS UNKNOWN OR taste IS UNKNOWN";
        assertEquals("<NONE>", converter.convert(parse(expr)).format(FormatStyle.PRETTY_PRINT));

    }

    @Test
    void testWithLookup() {

        // @formatter:off
        DefaultArgMetaInfoLookup lookup = DefaultArgMetaInfoLookup
                                        .withArg("favcolor").ofType(DefaultAdlType.STRING)
                                        .withArg("taste").ofType(DefaultAdlType.STRING)
                                        .withArg("shape").ofType(DefaultAdlType.STRING)
                                        .withArg("color").ofType(DefaultAdlType.STRING).thatIsAlwaysKnown()
                                        .withArg("look").ofType(DefaultAdlType.STRING).thatIsAlwaysKnown()
                                        .get();


                
        // @formatter:on

        IsUnknownRemovalConverter converter = new IsUnknownRemovalConverter(lookup);

        String expr = "color != @favcolor OR color = blue OR taste != blueberry OR ( (STRICT NOT look = nice OR look IS UNKNOWN) AND (shape != circle OR taste = lemon) ) OR (look = nice AND shape = square)";

        assertEquals("""
                STRICT NOT color = @favcolor
                OR color = blue
                OR favcolor IS UNKNOWN
                OR STRICT NOT taste = blueberry
                OR taste IS UNKNOWN
                OR (
                        STRICT NOT look = nice
                    AND (
                            STRICT NOT shape = circle
                         OR shape IS UNKNOWN
                         OR taste = lemon
                        )
                    )
                OR (
                        look = nice
                    AND shape = square
                    )""", converter.convert(parse(expr)).format(FormatStyle.PRETTY_PRINT));

    }

    private static CoreExpression parse(String source) {
        return new PlToCoreExpressionConverter(new CoreExpressionOptimizer()).convert(parsePl(source).getResultExpression());
    }

    private static AudlangParseResult parsePl(String source) {
        return PlExpressionBuilder.stringToExpression(source);
    }

}
