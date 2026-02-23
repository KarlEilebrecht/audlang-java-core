//@formatter:off
/*
 * MappingCoreExpressionConverterTest
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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.ConversionException;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.erl.AudlangParseResult;
import de.calamanari.adl.erl.PlExpressionBuilder;
import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.CoreExpressionVisitor;
import de.calamanari.adl.irl.biceps.CoreExpressionOptimizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class MappingCoreExpressionConverterTest {

    static final Logger LOGGER = LoggerFactory.getLogger(MappingCoreExpressionConverterTest.class);

    @Test
    void testBasics() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("color", "blue", "cl", "light blue")
            .withMapping("look", "nice", "l", "good")
            .withMapping("color", "red", "cl", "dark red")
            .withMapping("shape", "square", "sh", "square")
            .withMapping("shape", "circle", "sh", "circle")
            .withMapping("taste", "lemon", "ts", "citrus")
            .withMapping("taste", "blueberry", "ts", "berry")
            .withMapping("favcolor", "orange", "fc", "orange")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping);

        MappingCoreExpressionConverter converter = new MappingCoreExpressionConverter(mapper);

        String expr = "color = @favcolor OR color = blue OR taste = blueberry OR ( (STRICT NOT look = nice OR look IS UNKNOWN) AND (shape = circle OR taste = lemon) ) OR (look = nice AND shape = square)";

        assertEquals("""
                cl = "light blue"
                OR cl = @fc
                OR ts = berry
                OR (
                        (
                            STRICT NOT l = good
                         OR l IS UNKNOWN
                        )
                    AND (
                            sh = circle
                         OR ts = citrus
                        )
                    )
                OR (
                        l = good
                    AND sh = square
                    )""", convert(converter, parse(expr)).format(FormatStyle.PRETTY_PRINT));

        MappingCoreExpressionConverter reverseConverter = new MappingCoreExpressionConverter(mapper.reverse());

        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

        assertEquals("<NONE>", convert(reverseConverter, convert(converter, parse("<NONE>"))).format(FormatStyle.INLINE));
        assertEquals("<ALL>", convert(reverseConverter, convert(converter, parse("<ALL>"))).format(FormatStyle.INLINE));

    }

    @Test
    void testContains() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("color", "blue", "cl", "light blue")
            .withMapping("look", "nice", "l", "good")
            .withMapping("color", "red", "cl", "dark red")
            .withMapping("shape", "square", "sh", "square")
            .withMapping("shape", "circle", "sh", "circle")
            .withMapping("taste", "lemon", "ts", "citrus")
            .withMapping("taste", "blueberry", "ts", "berry")
            .withMapping("favcolor", "orange", "fc", "orange")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping);

        MappingCoreExpressionConverter converter = new MappingCoreExpressionConverter(mapper);

        String expr = "color CONTAINS \"N/A\" OR taste = blueberry OR ( (STRICT NOT look = nice OR look IS UNKNOWN) AND (shape = circle OR taste = lemon) ) OR (look = nice AND shape = square)";

        assertEquals("""
                cl CONTAINS "N/A"
                OR ts = berry
                OR (
                        (
                            STRICT NOT l = good
                         OR l IS UNKNOWN
                        )
                    AND (
                            sh = circle
                         OR ts = citrus
                        )
                    )
                OR (
                        l = good
                    AND sh = square
                    )""", convert(converter, parse(expr)).format(FormatStyle.PRETTY_PRINT));

        MappingCoreExpressionConverter reverseConverter = new MappingCoreExpressionConverter(mapper.reverse());

        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

        expr = "STRICT NOT color CONTAINS \"N/A\" OR color IS UNKNOWN OR taste = blueberry OR ( (STRICT NOT look = nice OR look IS UNKNOWN) AND (shape = circle OR taste = lemon) ) OR (look = nice AND shape = square)";
        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

    }

    @Test
    void testRestructuredContains() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("color", "blue", "blue color", "1")
            .withMapping("look", "nice", "nice look", "1")
            .withMapping("color", "red", "red color", "1")
            .withMapping("shape", "square", "squared", "1")
            .withMapping("shape", "circle", "round", "1")
            .withMapping("taste", "lemon", "lemon taste", "1")
            .withMapping("taste", "blueberry", "berry taste", "1")
            .withMapping("favcolor", "orange", "fav orange", "1")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping);

        MappingCoreExpressionConverter converter = new MappingCoreExpressionConverter(mapper);

        String expr = "taste = blueberry OR ( (STRICT NOT look = nice OR look IS UNKNOWN) AND (shape = circle OR taste = lemon) ) OR (look = nice AND shape = square)";

        assertEquals("""
                "berry taste" = 1
                OR (
                        "nice look" = 1
                    AND squared = 1
                    )
                OR (
                        (
                            "lemon taste" = 1
                         OR round = 1
                        )
                    AND (
                            STRICT NOT "nice look" = 1
                         OR "nice look" IS UNKNOWN
                        )
                    )""", convert(converter, parse(expr)).format(FormatStyle.PRETTY_PRINT));

        MappingCoreExpressionConverter reverseConverter = new MappingCoreExpressionConverter(mapper.reverse());

        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

        expr = "STRICT NOT color CONTAINS \"N/A\" OR color IS UNKNOWN OR taste = blueberry OR ( (STRICT NOT look = nice OR look IS UNKNOWN) AND (shape = circle OR taste = lemon) ) OR (look = nice AND shape = square)";
        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

    }

    @Test
    void testSpecialCase() {
        CoreExpression badGuy = new CoreExpression() {

            private static final long serialVersionUID = 7852320023666619710L;

            @Override
            public void appendSingleLine(StringBuilder sb, FormatStyle style, int level) {
                // no-op
            }

            @Override
            public void appendMultiLine(StringBuilder sb, FormatStyle style, int level) {
                // no-op

            }

            @Override
            public void accept(CoreExpressionVisitor visitor) {
                // no-op

            }

            @Override
            public CoreExpression negate(boolean strict) {
                return null;
            }
        };

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("color", "blue", "cl", "light blue")
            .withMapping("look", "nice", "l", "good")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping);

        MappingCoreExpressionConverter converter = new MappingCoreExpressionConverter(mapper);

        assertThrows(ConversionException.class, () -> converter.convert(badGuy));

    }

    private static CoreExpression convert(ExpressionConverter<CoreExpression, CoreExpression> converter, CoreExpression expression) {
        return converter.convert(expression);
    }

    private static CoreExpression parse(String source) {
        return new PlToCoreExpressionConverter(new CoreExpressionOptimizer()).convert(parsePl(source).getResultExpression());
    }

    private static AudlangParseResult parsePl(String source) {
        return PlExpressionBuilder.stringToExpression(source);
    }
}
