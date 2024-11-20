//@formatter:off
/*
 * DefaultArgValueFormatterTest
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.irl.MatchOperator;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class DefaultArgValueFormatterTest {

    @Test
    void testNone() {

        assertEquals("null", DefaultArgValueFormatter.NONE.format("argName", null, null));
        assertEquals("null", DefaultArgValueFormatter.NONE.format("argName", "null", null));
        assertEquals("", DefaultArgValueFormatter.NONE.format("argName", "", MatchOperator.EQUALS));
        assertEquals("bla", DefaultArgValueFormatter.NONE.format("argName", "bla", MatchOperator.EQUALS));

    }

    @Test
    void testStringInDoubleQuotes() {

        assertEquals("\"null\"", DefaultArgValueFormatter.STRING_IN_DOUBLE_QUOTES.format("argName", null, null));
        assertEquals("\"null\"", DefaultArgValueFormatter.STRING_IN_DOUBLE_QUOTES.format("argName", "null", null));
        assertEquals("\"\"", DefaultArgValueFormatter.STRING_IN_DOUBLE_QUOTES.format("argName", "", MatchOperator.EQUALS));
        assertEquals("\"bla\"", DefaultArgValueFormatter.STRING_IN_DOUBLE_QUOTES.format("argName", "bla", MatchOperator.EQUALS));
        assertEquals("\"bla bla\"", DefaultArgValueFormatter.STRING_IN_DOUBLE_QUOTES.format("argName", "bla bla", MatchOperator.EQUALS));
        assertEquals("\"\\b\\n\\r\\t\\\\\\\"\"", DefaultArgValueFormatter.STRING_IN_DOUBLE_QUOTES.format("argName", "\b\n\r\t\\\"", MatchOperator.EQUALS));
        assertEquals("\"\"", DefaultArgValueFormatter.STRING_IN_DOUBLE_QUOTES.format("argName", "\u0004", MatchOperator.EQUALS));

    }

    @Test
    void testStringInSingleQuotes() {

        assertEquals("'null'", DefaultArgValueFormatter.STRING_IN_SINGLE_QUOTES.format("argName", null, null));
        assertEquals("'null'", DefaultArgValueFormatter.STRING_IN_SINGLE_QUOTES.format("argName", "null", null));
        assertEquals("''", DefaultArgValueFormatter.STRING_IN_SINGLE_QUOTES.format("argName", "", MatchOperator.EQUALS));
        assertEquals("'bla'", DefaultArgValueFormatter.STRING_IN_SINGLE_QUOTES.format("argName", "bla", MatchOperator.EQUALS));
        assertEquals("'bla bla'", DefaultArgValueFormatter.STRING_IN_SINGLE_QUOTES.format("argName", "bla bla", MatchOperator.EQUALS));
        assertEquals("'\\b\\n\\r\\t\\\\\\''", DefaultArgValueFormatter.STRING_IN_SINGLE_QUOTES.format("argName", "\b\n\r\t\\'", MatchOperator.EQUALS));
        assertEquals("''", DefaultArgValueFormatter.STRING_IN_SINGLE_QUOTES.format("argName", "\u0004", MatchOperator.EQUALS));

    }

    @Test
    void testInteger() {
        assertEquals("917364283", DefaultArgValueFormatter.INTEGER.format("argName", "917364283", null));
        assertEquals("4", DefaultArgValueFormatter.INTEGER.format("argName", "4", null));
        assertEquals("4", DefaultArgValueFormatter.INTEGER.format("argName", "4.8", null));
        assertEquals("41232", DefaultArgValueFormatter.INTEGER.format("argName", "41232.84353", null));
        assertEquals("41233", DefaultArgValueFormatter.INTEGER.format("argName", "41232.84353", MatchOperator.LESS_THAN));

        assertBadValue(DefaultArgValueFormatter.INTEGER, null);
        assertBadValue(DefaultArgValueFormatter.INTEGER, "" + Long.MAX_VALUE + "0");
        assertBadValue(DefaultArgValueFormatter.INTEGER, "" + Long.MIN_VALUE + "0");
        assertBadValue(DefaultArgValueFormatter.INTEGER, "");
        assertBadValue(DefaultArgValueFormatter.INTEGER, "04");
        assertBadValue(DefaultArgValueFormatter.INTEGER, " 5");
        assertBadValue(DefaultArgValueFormatter.INTEGER, "xy");

        String msUtc = DefaultArgValueFormatter.INTEGER.format("argName", "2024-01-31", MatchOperator.EQUALS);
        SimpleDateFormat sdf = new SimpleDateFormat(AdlDateUtils.AUDLANG_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String parsed = sdf.format(new Date(Long.parseLong(msUtc)));
        assertEquals("2024-01-31", parsed);

    }

    @Test
    void testDecimal() {
        assertEquals("4.0", DefaultArgValueFormatter.DECIMAL.format("argName", "4", MatchOperator.EQUALS));
        assertEquals("2.0000001", DefaultArgValueFormatter.DECIMAL.format("argName", "2.0000001", MatchOperator.EQUALS));
        assertEquals("2.0", DefaultArgValueFormatter.DECIMAL.format("argName", "2.00000001", MatchOperator.EQUALS));
        assertEquals("192376423743.1273", DefaultArgValueFormatter.DECIMAL.format("argName", "192376423743.1273", MatchOperator.EQUALS));
        assertEquals("289237.1234568", DefaultArgValueFormatter.DECIMAL.format("argName", "289237.12345678", MatchOperator.EQUALS));
        assertEquals("97.1234567", DefaultArgValueFormatter.DECIMAL.format("argName", "97.12345673", MatchOperator.EQUALS));

        assertBadValue(DefaultArgValueFormatter.DECIMAL, null);
        assertBadValue(DefaultArgValueFormatter.DECIMAL, "1,400,000.999");
        assertBadValue(DefaultArgValueFormatter.DECIMAL, "04");
        assertBadValue(DefaultArgValueFormatter.DECIMAL, "04.1");
        assertBadValue(DefaultArgValueFormatter.DECIMAL, "");
        assertBadValue(DefaultArgValueFormatter.DECIMAL, "xy");

        String msUtc = DefaultArgValueFormatter.DECIMAL.format("argName", "2200-01-01", MatchOperator.EQUALS);
        SimpleDateFormat sdf = new SimpleDateFormat(AdlDateUtils.AUDLANG_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String parsed = sdf.format(new Date(Long.parseLong(msUtc.substring(0, msUtc.indexOf('.')))));
        assertEquals("2200-01-01", parsed);

    }

    @Test
    void testBool() {
        assertEquals("TRUE", DefaultArgValueFormatter.BOOL.format("argName", "1", MatchOperator.EQUALS));
        assertEquals("FALSE", DefaultArgValueFormatter.BOOL.format("argName", "0", MatchOperator.EQUALS));

        assertBadValue(DefaultArgValueFormatter.BOOL, null);
        assertBadValue(DefaultArgValueFormatter.BOOL, "666");
        assertBadValue(DefaultArgValueFormatter.BOOL, "");
        assertBadValue(DefaultArgValueFormatter.BOOL, "TRUE");

    }

    @Test
    void testDate() {
        assertEquals("2025-10-13", DefaultArgValueFormatter.DATE.format("argName", "2025-10-13", MatchOperator.EQUALS));
        assertEquals(AdlDateUtils.MIN_DATE, DefaultArgValueFormatter.DATE.format("argName", AdlDateUtils.MIN_DATE, MatchOperator.EQUALS));
        assertEquals(AdlDateUtils.MAX_DATE, DefaultArgValueFormatter.DATE.format("argName", AdlDateUtils.MAX_DATE, MatchOperator.EQUALS));
        assertEquals(AdlDateUtils.TOLERATED_MAXIMUM_DATE,
                DefaultArgValueFormatter.DATE.format("argName", AdlDateUtils.TOLERATED_MAXIMUM_DATE, MatchOperator.EQUALS));

        assertBadValue(DefaultArgValueFormatter.DATE, null);
        assertBadValue(DefaultArgValueFormatter.DATE, "999-01-01");
        assertBadValue(DefaultArgValueFormatter.DATE, "2210-01-01");
        assertBadValue(DefaultArgValueFormatter.DATE, "9.999");
        assertBadValue(DefaultArgValueFormatter.DATE, "");
        assertBadValue(DefaultArgValueFormatter.DATE, "1");

    }

    private static void assertBadValue(ArgValueFormatter formatter, String value) {
        assertThrows(AdlFormattingException.class, () -> formatter.format("argName", value, MatchOperator.EQUALS));

    }

}
