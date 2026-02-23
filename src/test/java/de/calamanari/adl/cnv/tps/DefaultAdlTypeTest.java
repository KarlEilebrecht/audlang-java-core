//@formatter:off
/*
 * AdlTypeTest
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

package de.calamanari.adl.cnv.tps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.DeepCopyUtils;
import de.calamanari.adl.irl.MatchOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class DefaultAdlTypeTest {

    static final Logger LOGGER = LoggerFactory.getLogger(DefaultAdlTypeTest.class);

    @Test
    void testBasics() {

        AdlType type1 = DefaultAdlType.BOOL;

        AdlType type2 = DefaultAdlType.BOOL.withFormatter(DefaultArgValueFormatter::formatBool);

        assertNotEquals(type1, type2);
        assertNotEquals(type1.name(), type2.name());

        LOGGER.info("{}", type2.name());

        assertEquals(type1, type2.getBaseType());

        type2 = DefaultAdlType.BOOL.withFormatter("BOOL", DefaultArgValueFormatter::formatBool);

        assertNotEquals(type1, type2);
        assertEquals(type1.name(), type2.name());

        type1 = DefaultAdlType.INTEGER;

        type2 = DefaultAdlType.INTEGER.withNativeTypeCaster(PassThroughTypeCaster.getInstance());

        assertNotEquals(type1, type2);
        assertNotEquals(type1.name(), type2.name());

        LOGGER.info("{}", type2.name());

        assertEquals(type1, type2.getBaseType());

        type2 = DefaultAdlType.INTEGER.withNativeTypeCaster("INTEGER", PassThroughTypeCaster.getInstance());

        assertNotEquals(type1, type2);
        assertEquals(type1.name(), type2.name());

    }

    @Test
    void testString() {

        AdlType type = DefaultAdlType.STRING;
        assertTrue(type.supportsContains());
        assertTrue(type.supportsLessThanGreaterThan());

        assertEquals("''", type.getFormatter().format("argName", "", MatchOperator.EQUALS));
        assertEquals("'val'", type.getFormatter().format("argName", "val", MatchOperator.EQUALS));
        assertEquals("'argValue is nice'", type.getFormatter().format("argName", "argValue is nice", MatchOperator.GREATER_THAN));

        assertEquals("'argValue \\tis nice'", type.getFormatter().format("argName", "argValue \tis nice", MatchOperator.GREATER_THAN));
        assertEquals("'\\b\\n\\r\\t\\\\\\''", type.getFormatter().format("argName", "\b\n\r\t\\'", MatchOperator.GREATER_THAN));

        assertEquals("''", type.getFormatter().format("argName", "\u0007", MatchOperator.LESS_THAN));

        assertEquals("'null'", type.getFormatter().format("argName", null, MatchOperator.EQUALS));

        AdlType type2 = type.withFormatter(new ArgValueFormatter() {

            private static final long serialVersionUID = -880154188494678018L;

            @Override
            public String format(String argName, String argValue, MatchOperator operator) {
                argValue = type.getFormatter().format(argName, argValue, operator);
                return "?" + argValue + "?";
            }
        });

        assertEquals("?'val'?", type2.getFormatter().format("argName", "val", MatchOperator.EQUALS));

    }

    @Test
    void testInteger() {

        AdlType type = DefaultAdlType.INTEGER;
        assertFalse(type.supportsContains());
        assertTrue(type.supportsLessThanGreaterThan());

        assertEquals("3", type.getFormatter().format("argName", "3", MatchOperator.EQUALS));
        assertEquals("192376423743", type.getFormatter().format("argName", "192376423743", MatchOperator.EQUALS));

        assertBadValue(type, null);
        assertBadValue(type, "" + Long.MAX_VALUE + "0");
        assertBadValue(type, "" + Long.MIN_VALUE + "0");
        assertBadValue(type, "");
        assertBadValue(type, "03");
        assertBadValue(type, " 3");
        assertBadValue(type, "x");

        String msUtc = type.getFormatter().format("argName", "2200-01-01", MatchOperator.EQUALS);
        SimpleDateFormat sdf = new SimpleDateFormat(AdlDateUtils.AUDLANG_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String parsed = sdf.format(new Date(Long.parseLong(msUtc)));
        assertEquals("2200-01-01", parsed);

    }

    @Test
    void testDecimal() {

        AdlType type = DefaultAdlType.DECIMAL;
        assertFalse(type.supportsContains());
        assertTrue(type.supportsLessThanGreaterThan());

        assertEquals("3.0", type.getFormatter().format("argName", "3", MatchOperator.EQUALS));
        assertEquals("1.0000001", type.getFormatter().format("argName", "1.0000001", MatchOperator.EQUALS));
        assertEquals("1.0", type.getFormatter().format("argName", "1.00000001", MatchOperator.EQUALS));
        assertEquals("192376423743.8273", type.getFormatter().format("argName", "192376423743.8273", MatchOperator.EQUALS));
        assertEquals("19237.1234568", type.getFormatter().format("argName", "19237.12345678", MatchOperator.EQUALS));
        assertEquals("19237.1234567", type.getFormatter().format("argName", "19237.12345673", MatchOperator.EQUALS));

        assertBadValue(type, null);
        assertBadValue(type, "9,400,000.999");
        assertBadValue(type, "03");
        assertBadValue(type, "03.3");
        assertBadValue(type, "");
        assertBadValue(type, "x");

        String msUtc = type.getFormatter().format("argName", "2200-01-01", MatchOperator.EQUALS);
        SimpleDateFormat sdf = new SimpleDateFormat(AdlDateUtils.AUDLANG_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String parsed = sdf.format(new Date(Long.parseLong(msUtc.substring(0, msUtc.indexOf('.')))));
        assertEquals("2200-01-01", parsed);

    }

    @Test
    void testBool() {
        AdlType type = DefaultAdlType.BOOL;
        assertFalse(type.supportsContains());
        assertFalse(type.supportsLessThanGreaterThan());

        assertEquals("TRUE", type.getFormatter().format("argName", "1", MatchOperator.EQUALS));
        assertEquals("FALSE", type.getFormatter().format("argName", "0", MatchOperator.EQUALS));

        assertBadValue(type, null);
        assertBadValue(type, "9,400,000.999");
        assertBadValue(type, "");
        assertBadValue(type, "TRUE");

        NativeTypeCaster ntc = new NativeTypeCaster() {

            private static final long serialVersionUID = -4716335076800837380L;

            @Override
            public String formatNativeTypeCast(String argName, String nativeFieldName, AdlType argType, AdlType requestedArgType) {
                return "bla";
            }

        };

        AdlType type2 = type.withNativeTypeCaster(ntc);

        assertNotEquals(type.name(), type2.name());

        assertSame(ntc, type2.getNativeTypeCaster());

    }

    @Test
    void testDate() {
        AdlType type = DefaultAdlType.DATE;
        assertFalse(type.supportsContains());
        assertTrue(type.supportsLessThanGreaterThan());

        assertEquals("2024-09-13", type.getFormatter().format("argName", "2024-09-13", MatchOperator.EQUALS));
        assertEquals(AdlDateUtils.MIN_DATE, type.getFormatter().format("argName", AdlDateUtils.MIN_DATE, MatchOperator.EQUALS));
        assertEquals(AdlDateUtils.MAX_DATE, type.getFormatter().format("argName", AdlDateUtils.MAX_DATE, MatchOperator.EQUALS));
        assertEquals(AdlDateUtils.TOLERATED_MAXIMUM_DATE, type.getFormatter().format("argName", AdlDateUtils.TOLERATED_MAXIMUM_DATE, MatchOperator.EQUALS));

        assertBadValue(type, null);
        assertBadValue(type, "0000-01-01");
        assertBadValue(type, "2300-01-01");
        assertBadValue(type, "9,400,000.999");
        assertBadValue(type, "");
        assertBadValue(type, "1");
    }

    @Test
    void testSerialization() {

        for (AdlType type : DefaultAdlType.values()) {

            assertSame(type, DeepCopyUtils.deepCopy(type));

        }

        AdlType type = DefaultAdlType.DATE.withFormatter((argName, argValue, operator) -> {
            return operator.toString() + "(" + argName + ", " + argValue + ")";
        });

        assertEquals("LESS_THAN(argName, 2024-09-13)", type.getFormatter().format("argName", "2024-09-13", MatchOperator.LESS_THAN));

        type = DeepCopyUtils.deepCopy(type);

        assertEquals("LESS_THAN(argName, 2024-09-13)", type.getFormatter().format("argName", "2024-09-13", MatchOperator.LESS_THAN));

        type = type.withNativeTypeCaster((argName, nativeFieldName, argType, reqArgType) -> {
            return "(" + argType + " -> " + reqArgType + ") for " + argName + "." + nativeFieldName + ")";
        });

        assertEquals("(STRING -> INTEGER) for color.cl33)",
                type.getNativeTypeCaster().formatNativeTypeCast("color", "cl33", DefaultAdlType.STRING, DefaultAdlType.INTEGER));

        type = DeepCopyUtils.deepCopy(type);

        assertEquals("(STRING -> INTEGER) for color.cl33)",
                type.getNativeTypeCaster().formatNativeTypeCast("color", "cl33", DefaultAdlType.STRING, DefaultAdlType.INTEGER));

    }

    private static void assertBadValue(AdlType type, String value) {
        ArgValueFormatter formatter = type.getFormatter();

        assertThrows(AdlFormattingException.class, () -> formatter.format("argName", value, MatchOperator.EQUALS));

    }

}
