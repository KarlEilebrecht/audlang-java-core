//@formatter:off
/*
 * AdlDateUtilsTest
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.cnv.StandardConversions;
import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.MatchExpression.MatchOperator;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class AdlDateUtilsTest {

    @Test
    void testBasics() {
        assertEquals(AdlDateUtils.INVALID_DATE, AdlDateUtils.tryParseUtcMillis("xxx"));
        assertEquals(AdlDateUtils.INVALID_DATE, AdlDateUtils.tryParseUtcMillis("0000-01-01"));
        assertEquals(AdlDateUtils.INVALID_DATE, AdlDateUtils.tryParseUtcMillis("999-01-01"));
        assertEquals(AdlDateUtils.INVALID_DATE, AdlDateUtils.tryParseUtcMillis("2200-01-02"));

        SimpleDateFormat sdf = new SimpleDateFormat(AdlDateUtils.AUDLANG_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String parsed = sdf.format(new Date(AdlDateUtils.tryParseUtcMillis("2200-01-01")));
        assertEquals("2200-01-01", parsed);

        assertEquals("2200-01-02", AdlDateUtils.computeDayAfter("2200-01-01"));
        assertEquals("2025-03-01", AdlDateUtils.computeDayAfter("2025-02-28"));

    }

    @Test
    void testIsDateComparison() {

        assertTrue(AdlDateUtils.isDateComparison(parse("date > 2025-09-12"), MatchOperator.GREATER_THAN));
        assertFalse(AdlDateUtils.isDateComparison(parse("date > 2325-09-12"), MatchOperator.GREATER_THAN));
        assertFalse(AdlDateUtils.isDateComparison(parse("date > hugo"), MatchOperator.GREATER_THAN));
        assertFalse(AdlDateUtils.isDateComparison(parse("date > 2025-09-12"), MatchOperator.LESS_THAN));
        assertFalse(AdlDateUtils.isDateComparison(parse("date > 2025-09-12"), MatchOperator.EQUALS));

        assertTrue(AdlDateUtils.isDateComparison(parse("date < 2025-09-12"), MatchOperator.LESS_THAN));
        assertFalse(AdlDateUtils.isDateComparison(parse("date < 2325-09-12"), MatchOperator.LESS_THAN));
        assertFalse(AdlDateUtils.isDateComparison(parse("date < hugo"), MatchOperator.LESS_THAN));
        assertFalse(AdlDateUtils.isDateComparison(parse("date < 2025-09-12"), MatchOperator.GREATER_THAN));
        assertFalse(AdlDateUtils.isDateComparison(parse("date < 2025-09-12"), MatchOperator.EQUALS));

        assertTrue(AdlDateUtils.isDateComparison(parse("date = 2025-09-12"), MatchOperator.EQUALS));
        assertFalse(AdlDateUtils.isDateComparison(parse("date = 2325-09-12"), MatchOperator.EQUALS));
        assertFalse(AdlDateUtils.isDateComparison(parse("date = hugo"), MatchOperator.EQUALS));
        assertFalse(AdlDateUtils.isDateComparison(parse("date = 2025-09-12"), MatchOperator.GREATER_THAN));
        assertFalse(AdlDateUtils.isDateComparison(parse("date = 2025-09-12"), MatchOperator.LESS_THAN));

    }

    @Test
    void testBump() {
        assertEquals("2025-03-01", AdlDateUtils.bumpDateBoundIfRequired(parse("date > 2025-02-28"), "2025-02-28"));
        assertEquals("2025-02-28", AdlDateUtils.bumpDateBoundIfRequired(parse("date < 2025-02-28"), "2025-02-28"));
        assertEquals("2025-02-28", AdlDateUtils.bumpDateBoundIfRequired(parse("date = 2025-02-28"), "2025-02-28"));
    }

    private static CoreExpression parse(String source) {
        return StandardConversions.parseCoreExpression(source);
    }
}
