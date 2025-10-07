//@formatter:off
/*
 * DefaultArgValueFormatter
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

import java.text.NumberFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.AudlangMessage;
import de.calamanari.adl.CommonErrors;
import de.calamanari.adl.irl.MatchOperator;
import de.calamanari.adl.util.TriFunction;

/**
 * The default formatters in this enumeration provide a common way to format values suitable for most target systems (DBs).
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public enum DefaultArgValueFormatter implements ArgValueFormatter {

    /**
     * Passes the string value as-is, replaces <b>null</b> with the string <code>"null"</code>
     */
    NONE((_, argValue, _) -> String.valueOf(argValue)),

    /**
     * Encloses the argValue in double-quotes and escapes in standard manner, see {@link DefaultEscaper}, unsupported characters (control characters &lt;32)
     * will be suppressed
     */
    STRING_IN_DOUBLE_QUOTES((argName, argValue, operator) -> formatEnclosedInQuotes(argName, argValue, operator, false)),

    /**
     * Encloses the argValue in single-quotes and escapes in standard manner, see {@link DefaultEscaper}, unsupported characters (control characters &lt;32)
     * will be suppressed
     */
    STRING_IN_SINGLE_QUOTES((argName, argValue, operator) -> formatEnclosedInQuotes(argName, argValue, operator, true)),

    /**
     * Formats the argValue as a decimal with <b>dot</b> and 7 decimal digits max
     * <p>
     * See also: <a href=
     * "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#212-decimal-values-floating-point">§2.1.2
     * Audlang Spec</a>
     */
    DECIMAL(DefaultArgValueFormatter::formatDecimal),

    /**
     * Formats the argValue as a 64-bits signed integer with.
     * <p>
     * See also: <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#211-integer-values">§2.1.1
     * Audlang Spec</a>
     */
    INTEGER(DefaultArgValueFormatter::formatInteger),

    /**
     * Formats the argValue as BOOL, converting the Audlang values 1/0 into the literals <code>TRUE</code> and <code>FALSE</code>.
     * <p>
     * See also: <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#22-logical-values">§2.2
     * Audlang Spec</a>
     */
    BOOL(DefaultArgValueFormatter::formatBool),

    /**
     * Formats the argValue as date <code>yyyy-MM-dd</code> (pass-through) if the value is a valid Audlang date
     * <p>
     * See also: <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#23-date-values">§2.3 Audlang
     * Spec</a>
     */
    DATE(DefaultArgValueFormatter::formatDate);

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultArgValueFormatter.class);

    /**
     * False, represented by <b>{@value}</b>
     */
    public static final String BOOL_FALSE = "0";

    /**
     * True, represented by <b>{@value}</b>
     */
    public static final String BOOL_TRUE = "1";

    /**
     * The formatting function
     */
    private final TriFunction<String, String, MatchOperator, String> formatFunction;

    private DefaultArgValueFormatter(TriFunction<String, String, MatchOperator, String> formatFunction) {
        this.formatFunction = formatFunction;
    }

    @Override
    public String format(String argName, String argValue, MatchOperator operator) {
        return formatFunction.apply(argName, argValue, operator);
    }

    /**
     * This method formats a string enclosed in quotes.
     * <p>
     * Unsupported characters will be dropped with a warning.
     * 
     * @param argName name of the current argument
     * @param argValue value to be formatted
     * @param operator current operator
     * @param useSingleQuotes if true we will enclose the string in single quotes rather than double-quotes (default behavior)
     * @return string enclosed in quotes, illegal characters escaped, see {@link DefaultEscaper#doubleQuoteInstance()} resp.
     *         {@link DefaultEscaper#singleQuoteInstance()}
     * @throws AdlFormattingException in case of error
     */
    public static String formatEnclosedInQuotes(String argName, String argValue, MatchOperator operator, boolean useSingleQuotes) {
        StringBuilder sb = new StringBuilder();
        char quoteCharacter = useSingleQuotes ? '\'' : '"';
        NativeEscaper escaper = useSingleQuotes ? DefaultEscaper.singleQuoteInstance() : DefaultEscaper.doubleQuoteInstance();
        sb.append(quoteCharacter);
        argValue = String.valueOf(argValue);
        for (int i = 0; i < argValue.length(); i++) {
            char ch = argValue.charAt(i);
            if (!escaper.isAllowed(ch)) {
                LOGGER.warn("Dropping special character: (char){} in value {}, argName={}, operator={}", (int) ch, argValue, argName, operator);
            }
            else {
                escaper.append(sb, ch);
            }
        }
        sb.append(quoteCharacter);
        return sb.toString();

    }

    /**
     * Formats the given value as a DECIMAL if possible
     * <p>
     * This method implicitly handles Audlang DATE values as milliseconds UTC
     * 
     * @param argName
     * @param argValue
     * @param operator
     * @return decimal formatted according to <a href=
     *         "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#212-decimal-values-floating-point">§2.1.2
     *         Audlang Spec</a>
     * @throws AdlFormattingException in case of error
     */
    public static String formatDecimal(String argName, String argValue, MatchOperator operator) {
        long probedDateMillis = AdlDateUtils.tryParseUtcMillis(argValue);
        if (probedDateMillis != AdlDateUtils.INVALID_DATE) {
            LOGGER.trace("Successfully probed argValue={} as date (UTC: {}.0), argName={}, operator={}.", argValue, probedDateMillis, argName, operator);
            return "" + probedDateMillis + ".0";
        }
        try {
            if (argValue.length() > 1 && argValue.charAt(0) == '0' && argValue.charAt(1) != '.') {
                // §2.1.2 Audlang Spec, no leading zeros
                throw new NumberFormatException("Audlang integers shall not have leading zeros, given: " + argValue);
            }
            double value = Double.parseDouble(argValue);
            if (!Double.isFinite(value)) {
                throw new NumberFormatException("Parsed value is not a number or infinite, given: " + argValue);
            }
            NumberFormat nf = NumberFormat.getInstance(Locale.US);
            // §2.1.2 Audlang Spec: 7 decimal digits max, no grouping
            nf.setMaximumFractionDigits(7);
            nf.setMinimumFractionDigits(1);
            nf.setGroupingUsed(false);
            return nf.format(value);
        }
        catch (RuntimeException ex) {
            throw new AdlFormattingException(String.format("Unable to format '%s' as decimal (argName=%s, operator=%s)", argValue, argName, operator), ex);
        }
    }

    /**
     * Formats the given value as a INTEGER if possible
     * <p>
     * This method implicitly handles Audlang DATE values as milliseconds UTC
     * 
     * @param argName
     * @param argValue
     * @param operator
     * @return integer formatted according to
     *         <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#211-integer-values">§2.1.1
     *         Audlang Spec</a>
     * @throws AdlFormattingException in case of error
     */
    public static String formatInteger(String argName, String argValue, MatchOperator operator) {
        long probedDateMillis = AdlDateUtils.tryParseUtcMillis(argValue);
        if (probedDateMillis != AdlDateUtils.INVALID_DATE) {
            LOGGER.trace("Successfully probed argValue={} as date (UTC: {}), argName={}, operator={}.", argValue, probedDateMillis, argName, operator);
            return String.valueOf(probedDateMillis);
        }
        try {
            if (argValue.length() > 1 && argValue.charAt(0) == '0') {
                // §2.1.1 Audlang Spec, no leading zeros
                throw new NumberFormatException("Audlang integers shall not have leading zeros, given: " + argValue);
            }
            int dotPos = argValue.indexOf('.');
            if (dotPos > 0) {
                long integerPart = Long.parseLong(argValue.substring(0, dotPos));
                long decimalPart = Long.parseLong(argValue.substring(dotPos + 1));
                if (decimalPart > 0 && operator == MatchOperator.LESS_THAN) {
                    // Problem: 3.5 -> 3, so query "arg < 3" would not include 3 (which is wrong because the user entered 3.5!)
                    // Solution: we increase the bound
                    LOGGER.trace("Special case: bumping integer value: {} -> {} -> {}, argName={}, operator={}.", argValue, integerPart, (integerPart + 1),
                            argName, operator);
                    integerPart = integerPart + 1;
                }
                return String.valueOf(integerPart);
            }
            else {
                return String.valueOf(Long.parseLong(argValue));
            }
        }
        catch (RuntimeException ex) {
            throw new AdlFormattingException(String.format("Unable to format '%s' as integer (argName=%s, operator=%s)", argValue, argName, operator), ex);
        }
    }

    /**
     * Formats the Audlang values 1/0 as literals <code>TRUE</code> resp. <code>FALSE</code>
     * 
     * @param argName
     * @param argValue either '0' or '1', any other will generate an error, compliant to
     *            <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#22-logical-values">§2.2
     *            Audlang Spec</a>
     * @param operator
     * @return formatted value
     * @throws AdlFormattingException in case of error
     */
    public static String formatBool(String argName, String argValue, MatchOperator operator) {
        String res = null;
        if (BOOL_FALSE.equals(argValue)) {
            res = "FALSE";
        }
        else if (BOOL_TRUE.equals(argValue)) {
            res = "TRUE";
        }
        else {
            AudlangMessage userMessage = AudlangMessage.argValueMsg(CommonErrors.ERR_2005_VALUE_FORMAT_BOOL, argName, argValue);
            throw new AdlFormattingException(String.format("Unable to format '%s' as boolean (argName=%s, operator=%s), expected: '%s' or '%s'.", argValue,
                    argName, operator, BOOL_FALSE, BOOL_TRUE), userMessage);
        }
        return res;
    }

    /**
     * Formats the Audlang date values (validation, pass-through: <code>yyyy-MM-dd</code>)
     * 
     * @param argName
     * @param argValue valid date according to
     *            <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#23-date-values">§2.3 Audlang
     *            Spec</a>
     * @param operator
     * @return formatted value
     * @throws AdlFormattingException if the given date was not valid resp. not in range
     */
    public static String formatDate(String argName, String argValue, MatchOperator operator) {
        long probedDateMillis = AdlDateUtils.tryParseUtcMillis(argValue);
        if (probedDateMillis != AdlDateUtils.INVALID_DATE) {
            return argValue;
        }
        else {
            AudlangMessage errorInfo = AudlangMessage.argValueMsg(CommonErrors.ERR_2006_VALUE_FORMAT_DATE, argName, argValue);
            throw new AdlFormattingException(
                    String.format("Unable to format '%s' as date (argName=%s, operator=%s), expected: 'yyyy-MM-dd'.", argValue, argName, operator), errorInfo);
        }
    }

}
