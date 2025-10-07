//@formatter:off
/*
 * DefaultEscaper
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

import java.util.Arrays;
import java.util.function.Function;

/**
 * The {@link DefaultEscaper} escapes a configurable set of characters using given escape sequences.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class DefaultEscaper implements NativeEscaper {

    /**
     * Default set of characters which must be prepended by a backslash to escape them: {@value}
     */
    private static final String DEFAULT_CHARS_TO_BE_ESCAPED_DQ = "\b\n\r\t\\\"";

    /**
     * Sequences to represent escaped characters (same order as {@link #DEFAULT_CHARS_TO_BE_ESCAPED_DQ}).
     */
    private static final String[] DEFAULT_ESCAPE_SEQUENCES_DQ = new String[] { "\\b", "\\n", "\\r", "\\t", "\\\\", "\\\"" };

    /**
     * Default set of characters which must be prepended by a backslash to escape them: {@value}
     */
    private static final String DEFAULT_CHARS_TO_BE_ESCAPED_SQ = "\b\n\r\t\\'";

    /**
     * Sequences to represent escaped characters (same order as {@link #DEFAULT_CHARS_TO_BE_ESCAPED_SQ}).
     */
    private static final String[] DEFAULT_ESCAPE_SEQUENCES_SQ = new String[] { "\\b", "\\n", "\\r", "\\t", "\\\\", "\\'" };

    private static final DefaultEscaper DEFAULT_DQ_INSTANCE = new DefaultEscaper(DEFAULT_CHARS_TO_BE_ESCAPED_DQ, DEFAULT_ESCAPE_SEQUENCES_DQ,
            ch -> (ch >= 32 || DEFAULT_CHARS_TO_BE_ESCAPED_DQ.indexOf(ch) > -1));

    private static final DefaultEscaper DEFAULT_SQ_INSTANCE = new DefaultEscaper(DEFAULT_CHARS_TO_BE_ESCAPED_SQ, DEFAULT_ESCAPE_SEQUENCES_SQ,
            ch -> (ch >= 32 || DEFAULT_CHARS_TO_BE_ESCAPED_SQ.indexOf(ch) > -1));

    /**
     * all characters to be escaped
     */
    private final String charsToBeEscaped;

    /**
     * the replacements in the same order as {@link #charsToBeEscaped}
     */
    private final String[] escapeSequences;

    /**
     * Function to determine if a character is allowed at all, see {@link #isAllowed(char)}
     */
    private final Function<Character, Boolean> supportChecker;

    /**
     * Returns an escaper that escapes {@value #DEFAULT_CHARS_TO_BE_ESCAPED_DQ} prepending the backslash and otherwise supports all characters &gt;=32, see
     * {@link #isAllowed(char)}. This instance and {@link #singleQuoteInstance()} cover the two common scenarios to either use the double quote (") or the
     * single quote (') to enclose strings in target platform expressions.
     * 
     * @return default instance
     */
    public static DefaultEscaper doubleQuoteInstance() {
        return DEFAULT_DQ_INSTANCE;
    }

    /**
     * Returns an escaper that escapes {@value #DEFAULT_CHARS_TO_BE_ESCAPED_SQ} prepending the backslash and otherwise supports all characters &gt;=32, see
     * {@link #isAllowed(char)}. This instance and {@link #doubleQuoteInstance()} cover the two common scenarios to either use the double quote (") or the
     * single quote (') to enclose strings in target platform expressions.
     * 
     * @return default instance
     */
    public static DefaultEscaper singleQuoteInstance() {
        return DEFAULT_SQ_INSTANCE;
    }

    /**
     * Creates a new default escaper that replaces certain characters with escape sequences (backslash followed by a single letter
     * 
     * @param charsToBeEscaped characters that must be escaped
     * @param escapeSequences the sequences to represent the given characters, <b>same order</b> as charsToBeEscaped
     * @param supportChecker function to decide whether a character is allowed at all or not, see {@link #isAllowed(char)}
     */
    public DefaultEscaper(String charsToBeEscaped, String[] escapeSequences, Function<Character, Boolean> supportChecker) {
        if (charsToBeEscaped == null || escapeSequences == null || charsToBeEscaped.length() != escapeSequences.length) {
            throw new IllegalArgumentException(String.format(
                    "The escapeSequences must contain the same number of elements as charsToBeEscaped, given: charsToBeEscaped=%s, escapeSequences=%s",
                    charsToBeEscaped, Arrays.toString(escapeSequences)));
        }
        if (supportChecker == null) {
            supportChecker = _ -> true;
        }

        this.charsToBeEscaped = charsToBeEscaped;
        this.escapeSequences = Arrays.copyOf(escapeSequences, escapeSequences.length);
        this.supportChecker = supportChecker;
    }

    /**
     * Creates a new default escaper that supports any character and replaces certain characters with escape sequences (backslash followed by a single letter
     * 
     * @param charsToBeEscaped characters that must be escaped
     * @param escapeSequences the sequences to represent the given characters, <b>same order</b> as charsToBeEscaped
     */
    public DefaultEscaper(String charsToBeEscaped, String[] escapeSequences) {
        this(charsToBeEscaped, escapeSequences, null);
    }

    @Override
    public void append(StringBuilder sb, char ch) {
        if (!isAllowed(ch)) {
            throw new AdlFormattingException(String.format("Illegal attempt to append unsupported character (char)%s.", (int) ch));
        }
        int idx = charsToBeEscaped.indexOf(ch);
        if (idx > -1) {
            sb.append(escapeSequences[idx]);
        }
        else {
            sb.append(ch);
        }

    }

    @Override
    public boolean needsEscaping(char ch) {
        return charsToBeEscaped.indexOf(ch) > -1;
    }

    @Override
    public boolean isAllowed(char ch) {
        return supportChecker.apply(ch) || needsEscaping(ch);
    }

}
