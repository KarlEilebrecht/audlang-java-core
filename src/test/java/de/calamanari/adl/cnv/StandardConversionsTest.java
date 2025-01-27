//@formatter:off
/*
 * StandardConversionsTest
 * Copyright 2025 Karl Eilebrecht
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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.AudlangResult;
import de.calamanari.adl.ConversionException;
import de.calamanari.adl.erl.AudlangParseResult;
import de.calamanari.adl.erl.PlExpression;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class StandardConversionsTest {

    @Test
    void testBasics() {

        PlExpression<?> expr = StandardConversions.parsePlExpression("color = blue");

        Optional<String> o1 = Optional.of("color = blue");

        AudlangResult res = o1.map(StandardConversions.parse()).orElseThrow(NullPointerException::new);

        assertEquals(expr, ((AudlangParseResult) res).getResultExpression());

        PlExpression<?> expr2 = o1.map(StandardConversions.parse()).map(StandardConversions.toCoreExpression()).map(StandardConversions.coreToPlExpression())
                .orElseThrow(NullPointerException::new);

        assertEquals(expr, expr2);

        PlExpression<?> expr3 = o1.map(StandardConversions.parse()).map(StandardConversions.toPlExpression()).orElseThrow(NullPointerException::new);

        assertEquals(expr, expr3);

        assertEquals("color = blue", o1.map(StandardConversions.parse()).map(StandardConversions.toCoreExpression()).map(StandardConversions.asString())
                .orElseThrow(NullPointerException::new));

        assertEquals("color = blue", o1.map(StandardConversions.parse()).map(StandardConversions.toCoreExpression()).map(StandardConversions.prettyPrint())
                .orElseThrow(NullPointerException::new));

        Optional<AudlangParseResult> o4 = Optional.of("x").map(StandardConversions.parse());

        UnaryOperator<AudlangParseResult> opValid = StandardConversions.assertValidParseResult();
        assertThrows(ConversionException.class, () -> o4.map(opValid));

        assertThrows(ConversionException.class, () -> StandardConversions.parsePlExpression(null));
        assertThrows(ConversionException.class, () -> StandardConversions.parseCoreExpression(null));

    }

    @Test
    void testMapping() {

        Optional<String> o1 = Optional.of("color = blue");

        ArgNameValueMapping mapping = ArgNameValueMapping.create().withMapping("color", "blue", "clr", "blue-2").get();

        assertEquals("clr = blue-2", o1.map(StandardConversions.parse()).map(StandardConversions.toPlExpression())
                .map(StandardConversions.mapPlArguments(mapping, false)).orElseThrow(NullPointerException::new).toString());

        Optional<String> o2 = Optional.of("color = red");

        assertEquals("clr = red", o2.map(StandardConversions.parse()).map(StandardConversions.toPlExpression())
                .map(StandardConversions.mapPlArguments(mapping, true)).orElseThrow(NullPointerException::new).toString());

        Optional<PlExpression<?>> o3 = o2.map(StandardConversions.parse()).map(StandardConversions.toPlExpression());

        UnaryOperator<PlExpression<?>> op = StandardConversions.mapPlArguments(mapping, false);

        assertThrows(MappingNotFoundException.class, () -> o3.map(op));

        assertEquals("clr = blue-2", o1.map(StandardConversions.parse()).map(StandardConversions.toCoreExpression())
                .map(StandardConversions.mapCoreArguments(mapping, false)).orElseThrow(NullPointerException::new).toString());

        assertEquals("clr = red", o2.map(StandardConversions.parse()).map(StandardConversions.toCoreExpression())
                .map(StandardConversions.mapCoreArguments(mapping, true)).orElseThrow(NullPointerException::new).toString());

    }

}
