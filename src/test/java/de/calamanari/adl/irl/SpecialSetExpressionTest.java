//@formatter:off
/*
 * SpecialSetExpressionTest
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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.SpecialSetType;
import de.calamanari.adl.cnv.StandardConversions;
import de.calamanari.adl.util.JsonUtils;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class SpecialSetExpressionTest {

    static final Logger LOGGER = LoggerFactory.getLogger(SpecialSetExpressionTest.class);

    @Test
    void testBasics() {

        assertEquals("<ALL>", parse("<ALL>").toString());
        assertEquals("<ALL>", parse("<ALL>").format(FormatStyle.INLINE));
        assertEquals("<ALL>", parse("<ALL>").format(FormatStyle.PRETTY_PRINT));

        assertEquals("<NONE>", parse("<NONE>").toString());
        assertEquals("<NONE>", parse("<NONE>").format(FormatStyle.INLINE));
        assertEquals("<NONE>", parse("<NONE>").format(FormatStyle.PRETTY_PRINT));

        assertEquals("<NONE>", parse("<ALL>").negate(false).toString());
        assertEquals("<NONE>", parse("<ALL>").negate(true).toString());

        assertEquals("<ALL>", parse("<NONE>").negate(false).toString());
        assertEquals("<ALL>", parse("<NONE>").negate(true).toString());

        assertEquals(1, parse("<NONE>").compareTo(parse("<ALL>")));
        assertEquals(-1, parse("<ALL>").compareTo(parse("<NONE>")));
        assertEquals(-1, parse("<ALL>").compareTo(parse("a > 1")));
        assertEquals(-1, parse("<NONE>").compareTo(parse("a > 1")));

    }

    @Test
    void testSpecialCases() {
        assertThrows(AudlangValidationException.class, () -> new SpecialSetExpression(null, null));
    }

    @Test
    void testEqualsHashCode() {

        SpecialSetExpression expr = new SpecialSetExpression(SpecialSetType.NONE, null);
        SpecialSetExpression expr2 = new SpecialSetExpression(SpecialSetType.NONE, null);

        assertNotSame(expr, expr2);

        assertEquals(expr, expr2);

        assertEquals(expr.hashCode(), expr2.hashCode());

    }

    @Test
    void testGetAllFields() {
        assertEquals(Collections.emptyList(), SpecialSetExpression.all().allFields());
    }

    @Test
    void testJson() {

        CoreExpression expr = parse("<ALL>");

        String json = JsonUtils.writeAsJsonString(expr, true);

        CoreExpression res = JsonUtils.readFromJsonString(json, CoreExpression.class);

        assertEquals(expr, res);

    }

    private static CoreExpression parse(String source) {
        return StandardConversions.parseCoreExpression(source);
    }

}
