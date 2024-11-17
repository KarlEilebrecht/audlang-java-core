//@formatter:off
/*
 * AdlDateUtils
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.MatchExpression;
import de.calamanari.adl.irl.MatchExpression.MatchOperator;

/**
 * Utility methods related to the type convention for date values
 * <p/>
 * See <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#23-date-values">§2.3 Audlang Spec</a>
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class AdlDateUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdlDateUtils.class);

    /**
     * Minimum valid date: {@value}
     */
    public static final String MIN_DATE = "0001-01-01";

    /**
     * Maximum valid date: {@value}
     */
    public static final String MAX_DATE = "2199-12-31";

    /**
     * Maximum valid date (for comparison cases) : {@value}
     */
    public static final String TOLERATED_MAXIMUM_DATE = "2200-01-01";

    /**
     * Indicator for a date that is invalid or malformed
     */
    public static final long INVALID_DATE = Long.MIN_VALUE;

    /**
     * Format for any dates in expressions: {@value}
     */
    public static final String AUDLANG_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Checks whether the given value is a correct data string ({@link #AUDLANG_DATE_FORMAT}) and valid. If so, we return the UTC millis.
     * 
     * @param argValue to be converted
     * @return &gt; 0 if and only if the value is a valid UTC time for the given date, otherwise {@link #INVALID_DATE}
     */
    public static long tryParseUtcMillis(String argValue) {
        long res = INVALID_DATE;
        if (argValue != null && argValue.length() == 10 && argValue.charAt(4) == '-' && argValue.charAt(7) == '-' && argValue.compareTo(MIN_DATE) >= 0
                && argValue.compareTo(TOLERATED_MAXIMUM_DATE) <= 0) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(AUDLANG_DATE_FORMAT);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(argValue);
                if (sdf.format(date).equals(argValue)) {
                    res = date.getTime();
                }
            }
            catch (ParseException ex) {
                // In this case anything went wrong, so we return the default
                LOGGER.trace("Probing argValue={} for type date failed.", argValue, ex);
            }
        }
        return res;
    }

    /**
     * Compute the day <i>after</i> the given one.
     * 
     * @param argValue (<code>yyyy-MM-dd</code>)
     * @return next day (<code>yyyy-MM-dd +1d</code>) or {@link #MIN_DATE} if the given date was invalid
     */
    public static final String computeDayAfter(String argValue) {
        long utcMillis = tryParseUtcMillis(argValue);
        if (utcMillis == INVALID_DATE) {
            return MIN_DATE;
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(utcMillis);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date date = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(AUDLANG_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    /**
     * This method allows the caller to apply special rules in case of date comparisons. The problem is that the underyling store may contain a <i>time</i>
     * which must be ignored to ensure proper comparison results.
     * 
     * @param expression
     * @return true if the given match is match with a valid Audlang date <b>value</b> using the given operator.
     */
    public static boolean isDateComparison(CoreExpression expression, MatchOperator operator) {
        return expression instanceof MatchExpression match && match.operator() == operator && !match.operand().isReference()
                && tryParseUtcMillis(match.operand().value()) != INVALID_DATE;
    }

    /**
     * This method increases the date by one day to match to align the technical condition to the semantical expectations.
     * <p/>
     * Example: The expression <code>dateValue &gt; 2024-10-12</code> would technically match <code>2024-10-12T12:34:01Z</code> in the underlying database.<br/>
     * This is not what a user expects (Audlang does not know about time!). Thus we bump the date by one day and change the expression to
     * <code>dateValue &gt;= 2024-10-13</code>.
     * 
     * @param expression match expression
     * @param techDateValue date value (after mapping)
     * @return either the techDateValue or a date bumped by one day
     */
    public static String bumpDateBoundIfRequired(CoreExpression expression, String techDateValue) {
        if (isDateComparison(expression, MatchOperator.GREATER_THAN)) {
            // Problem: The query myDateTime > 2021-05-06 would match the value 2021-05-06T20:32:00Z, which looks wrong to the user
            // Solution: we change here the operator to >= (code outside) and increase the date value by one day
            return computeDayAfter(techDateValue);
        }
        return techDateValue;
    }

    private AdlDateUtils() {
        // utility
    }

}
