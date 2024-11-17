//@formatter:off
/*
 * MappingPlExpressionConverterTest
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.ConversionException;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.erl.AudlangParseResult;
import de.calamanari.adl.erl.PlExpression;
import de.calamanari.adl.erl.PlExpressionBuilder;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class MappingPlExpressionConverterTest {

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

        MappingPlExpressionConverter converter = new MappingPlExpressionConverter(mapper);

        String expr = "color = blue OR (look = nice AND shape = square) OR (look != nice AND (shape = circle OR taste = lemon) ) OR taste = blueberry OR color = @favcolor";

        assertEquals("""
                cl = "light blue"
                OR (
                        l = good
                    AND sh = square
                    )
                OR (
                        l != good
                    AND (
                            sh = circle
                         OR ts = citrus
                        )
                    )
                OR ts = berry
                OR cl = @fc""", convert(converter, parse(expr)).format(FormatStyle.PRETTY_PRINT));

        MappingPlExpressionConverter reverseConverter = new MappingPlExpressionConverter(mapper.reverse());

        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

        assertEquals("<NONE>", convert(reverseConverter, convert(converter, parse("<NONE>"))).format(FormatStyle.INLINE));
        assertEquals("<ALL>", convert(reverseConverter, convert(converter, parse("<ALL>"))).format(FormatStyle.INLINE));

    }

    @Test
    void testCurb() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("color", "blue", "cl", "light blue")
            .withMapping("look", "nice", "l", "good")
            .withMapping("color", "red", "cl", "dark red")
            .withMapping("shape", "square", "sh", "square")
            .withMapping("shape", "circle", "sh", "circle")
            .withMapping("taste", "lemon", "ts", "citrus")
            .withMapping("taste", "blueberry", "ts", "berry")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping);

        MappingPlExpressionConverter converter = new MappingPlExpressionConverter(mapper);

        String expr = "CURB (color = blue OR (look = nice AND shape = square) OR (look != nice AND (shape = circle OR taste = lemon) ) OR taste = blueberry) > 2";

        assertEquals("""
                CURB (
                        cl = "light blue"
                     OR (
                            l = good
                        AND sh = square
                        )
                     OR (
                            l != good
                        AND (
                                sh = circle
                             OR ts = citrus
                            )
                        )
                     OR ts = berry
                    ) > 2""", convert(converter, parse(expr)).format(FormatStyle.PRETTY_PRINT));

        MappingPlExpressionConverter reverseConverter = new MappingPlExpressionConverter(mapper.reverse());

        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

        expr = "CURB (color = blue OR (look = nice AND shape = square) OR (look != nice AND NOT (shape = circle OR taste = lemon) ) OR taste = blueberry) > 2";
        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

        expr = "CURB (color = blue OR (look = nice AND shape = square) OR (look != nice AND STRICT NOT (shape = circle OR taste = lemon) ) OR taste = blueberry) > 2";
        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

    }

    @Test
    void testAnyOf() {

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

        MappingPlExpressionConverter converter = new MappingPlExpressionConverter(mapper);

        String expr = "color ANY OF (blue, red, @favcolor)";

        assertEquals("""
                cl ANY OF ("light blue", "dark red", @fc)""", convert(converter, parse(expr)).format(FormatStyle.PRETTY_PRINT));

        MappingPlExpressionConverter reverseConverter = new MappingPlExpressionConverter(mapper.reverse());

        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

        expr = "color NOT ANY OF (blue, red, @favcolor)";
        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));
        expr = "color STRICT NOT ANY OF (blue, red, @favcolor)";
        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

    }

    @Test
    void testBetween() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("color", "blue", "cl", "light blue")
            .withMapping("look", "nice", "l", "good")
            .withMapping("color", "red", "cl", "dark red")
            .withMapping("shape", "square", "sh", "square")
            .withMapping("shape", "circle", "sh", "circle")
            .withMapping("taste", "lemon", "ts", "citrus")
            .withMapping("taste", "blueberry", "ts", "berry")
            .withMapping("income", "small", "ic", "small")
            .withMapping("income", "large", "ic", "high")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping);

        MappingPlExpressionConverter converter = new MappingPlExpressionConverter(mapper);

        String expr = "income BETWEEN (small, large)";

        assertEquals("""
                ic BETWEEN (small, high)""", convert(converter, parse(expr)).format(FormatStyle.PRETTY_PRINT));

        MappingPlExpressionConverter reverseConverter = new MappingPlExpressionConverter(mapper.reverse());

        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

        expr = "income NOT BETWEEN (small, large)";
        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));
        expr = "income STRICT NOT BETWEEN (small, large)";
        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

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

        MappingPlExpressionConverter converter = new MappingPlExpressionConverter(mapper);

        String expr = "color CONTAINS \"N/A\" OR (look = nice AND shape = square) OR (look != nice AND (shape = circle OR taste = lemon) ) OR taste = blueberry";

        assertEquals("""
                cl CONTAINS "N/A"
                OR (
                        l = good
                    AND sh = square
                    )
                OR (
                        l != good
                    AND (
                            sh = circle
                         OR ts = citrus
                        )
                    )
                OR ts = berry""", convert(converter, parse(expr)).format(FormatStyle.PRETTY_PRINT));

        MappingPlExpressionConverter reverseConverter = new MappingPlExpressionConverter(mapper.reverse());

        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

        expr = "color NOT CONTAINS \"N/A\" OR (look = nice AND shape = square) OR (look != nice AND (shape = circle OR taste = lemon) ) OR taste = blueberry";
        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

        expr = "color STRICT NOT CONTAINS \"N/A\" OR (look = nice AND shape = square) OR (look != nice AND (shape = circle OR taste = lemon) ) OR taste = blueberry";
        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

    }

    @Test
    void testContainsAnyOf() {

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

        MappingPlExpressionConverter converter = new MappingPlExpressionConverter(mapper);

        String expr = "color CONTAINS ANY OF (blue, black, purple)";

        assertEquals("cl CONTAINS ANY OF (blue, black, purple)", convert(converter, parse(expr)).format(FormatStyle.PRETTY_PRINT));

        MappingPlExpressionConverter reverseConverter = new MappingPlExpressionConverter(mapper.reverse());

        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

        expr = "color NOT CONTAINS ANY OF (blue, black, purple)";
        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

        expr = "color STRICT NOT CONTAINS ANY OF (blue, black, purple)";
        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

    }

    @Test
    void testFallback() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("srcArg1", "blue", "destArg1", "light blue")
            .withMapping("srcArg2", "nice", "destArg2", "good")
            .withMapping("srcArg1", "red", "destArg1", "dark red")
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .get();
        // @formatter:on

        String expr = "srcArg1 = blue OR color = blue OR (look = nice AND shape = square) OR (look != nice AND (shape = circle OR taste = lemon) ) OR taste = blueberry";

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping, DummyArgNameValueMapper.getInstance());

        MappingPlExpressionConverter converter = new MappingPlExpressionConverter(mapper);

        assertEquals("""
                destArg1 = "light blue"
                OR color = blue
                OR (
                        look = nice
                    AND shape = square
                    )
                OR (
                        look != nice
                    AND (
                            shape = circle
                         OR taste = lemon
                        )
                    )
                OR taste = blueberry""", convert(converter, parse(expr)).format(FormatStyle.PRETTY_PRINT));

        MappingPlExpressionConverter reverseConverter = new MappingPlExpressionConverter(mapper.reverse());

        assertEquals(expr, convert(reverseConverter, convert(converter, parse(expr))).format(FormatStyle.INLINE));

    }

    @Test
    void testNotBijective() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("srcArg1", "blue", "destArg1", "light blue")
            .withMapping("srcArg2", "nice", "destArg2", "good")
            .withMapping("srcArg1", "red", "destArg1", "dark red")
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .withMapping("srcArg3", "slippery", "destArg3", "snowy")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping);
        assertTrue(mapper.isArgumentStructurePreserving());
        assertFalse(mapper.isBijective());

        assertThrows(IncompatibleMappingException.class, () -> new MappingPlExpressionConverter(mapper));

    }

    @Test
    void testNotStructurePreserving() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("srcArg1", "blue", "destArg1", "light blue")
            .withMapping("srcArg2", "nice", "destArg2", "good")
            .withMapping("srcArg1", "red", "destArg4", "dark red")
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping);
        assertFalse(mapper.isArgumentStructurePreserving());
        assertTrue(mapper.isBijective());

        assertThrows(IncompatibleMappingException.class, () -> new MappingPlExpressionConverter(mapper));

    }

    @Test
    void testNoMapping() {
        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("color", "blue", "cl", "light blue")
            .withMapping("look", "nice", "l", "good")
            .withMapping("color", "red", "cl", "dark red")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping);

        MappingPlExpressionConverter converter = new MappingPlExpressionConverter(mapper);

        String expr = "color = blue OR (look = nice AND shape = square) OR (look != nice AND (shape = circle OR taste = lemon) ) OR taste = blueberry OR color = @favcolor";

        PlExpression<?> parsed = parse(expr);
        assertThrows(ConversionException.class, () -> convert(converter, parsed));

    }

    private static PlExpression<?> convert(ExpressionConverter<PlExpression<?>, PlExpression<?>> converter, PlExpression<?> expression) {
        return converter.convert(expression);
    }

    private static PlExpression<?> parse(String source) {
        return parsePl(source).getResultExpression();
    }

    private static AudlangParseResult parsePl(String source) {
        return PlExpressionBuilder.stringToExpression(source);
    }

}
