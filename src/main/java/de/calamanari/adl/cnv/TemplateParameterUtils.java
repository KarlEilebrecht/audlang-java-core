//@formatter:off
/*
 * TemplateParameterUtils
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Utilities for simple textual parameter placeholder replacement (<code>${<i>varName</i>}</code>) with tolerant behavior.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class TemplateParameterUtils {

    /**
     * Replaces all variable references <code>${<i>varName</i>}</code> with the value returned by the given lookup.<br>
     * If the returned value is null (not found) the related reference will be left as is.
     * <p>
     * It is left to the lookup function to decide whether a lookup miss is an error or not.
     * <p>
     * This method is tolerant about broken or even nested references. The <i>inner most</i> variable reference with a valid pattern will be replaced, e.g., the
     * text <code>...${foo${bar}...</code> with a mapping <code><b>bar</b>=<b>3</b></code> would result in <code>...${foo<b>3</b>...</code>.
     * <p>
     * The sequence <code>"${"</code>, and the letter <code>'}'</code> can never be part of a variable's <i>name</i>.<br>
     * There is no escaping implemented.
     * 
     * @param source null gracefully handled as null
     * @param lookup to obtain variable values from null means no lookup, so no replacement, returns input as-is
     * @return string with replacements applied or source if unchanged
     */
    public static String replaceVariables(String source, Function<String, Object> lookup, ReplacementConstraint constraint) {
        if (source == null || lookup == null) {
            return source;
        }
        StringBuilder sb = new StringBuilder(source.length());

        int startPos = -1;
        int pendingPos = 0;

        boolean replaced = false;
        for (int idx = 0; idx < source.length(); idx++) {
            char ch = source.charAt(idx);
            if (ch == '}' && startPos > 0) {
                String varName = source.substring(startPos, idx).trim();
                Object replacement = lookupReplacement(lookup, varName, constraint);
                if (replacement != null) {
                    sb.append(replacement);
                    replaced = true;
                }
                else {
                    sb.append(source.substring(pendingPos, idx + 1));
                }
                startPos = -1;
                pendingPos = idx + 1;
            }
            else if (ch == '$' && idx < source.length() - 2 && source.charAt(idx + 1) == '{') {
                sb.append(source.substring(pendingPos, idx));
                pendingPos = idx;
                startPos = idx + 2;
            }
        }
        if (replaced) {
            if (pendingPos < source.length()) {
                sb.append(source.substring(pendingPos));
            }
            return sb.toString();
        }
        return source;
    }

    /**
     * Replaces all variable references <code>${<i>varName</i>}</code> with the value returned by the given lookup.<br>
     * If the returned value is null (not found) or blank the related reference will be left as is.
     * 
     * @see #replaceVariables(String, Function, ReplacementContraint)
     * @see ReplacementConstraint#NOT_BLANK
     * @param source
     * @param lookup to obtain variable values from
     * @return string with replacements applied or source if unchanged
     */
    public static String replaceVariables(String source, Function<String, Object> lookup) {
        return replaceVariables(source, lookup, ReplacementConstraint.NOT_BLANK);
    }

    /**
     * @param lookup
     * @param varName
     * @param constraint null means {@link ReplacementConstraint#NONE}
     * @return replacement value or null if replacement is not possible
     */
    private static String lookupReplacement(Function<String, Object> lookup, String varName, ReplacementConstraint constraint) {
        Object replacement = lookup.apply(varName);
        String res = replacement == null ? null : replacement.toString();
        if (constraint == null || constraint.isValid(res)) {
            res = String.valueOf(res);
        }
        else {
            res = null;
        }
        return res;
    }

    /**
     * Determines if there are <i>any</i> placeholders (<code>${<i>varName</i>}</code>) for variables in the given source
     * 
     * @see #replaceVariables(String, Function)
     * @param source
     * @return true if there are any placeholders, otherwise false
     */
    public static boolean containsAnyVariables(String source) {
        return !extractVariableNames(source).isEmpty();
    }

    /**
     * Extracts any existing variable names (<code>${<i>varName</i>}</code>) from the given source.
     * 
     * @param source (null gracefully ignored)
     * @return list of variables (distinct)
     */
    public static List<String> extractVariableNames(String source) {
        List<String> res = new ArrayList<>();
        replaceVariables(source, varName -> {
            res.add(varName);
            return null;
        });
        return res.stream().distinct().toList();
    }

    private TemplateParameterUtils() {
        // static utility
    }

    /**
     * Restricts the replacement not to replace the placeholder under certain conditions.
     * 
     * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
     */
    public enum ReplacementConstraint {

        /**
         * Do not replace if the mapped value is unknown or null
         */
        NOT_NULL,

        /**
         * Do not replace if the mapped value is null or its string representation is blank
         * 
         * @see String#isBlank()
         */
        NOT_BLANK,

        /**
         * No constraint, null will appear as "null". If the replacement is empty, the placeholder will just vanish.
         */
        NONE;

        /**
         * @param input
         * @return true if the given string meets the constraint
         */
        public boolean isValid(String input) {
            switch (this) {
            case NOT_NULL:
                return input != null;
            case NOT_BLANK:
                return input != null && !input.isBlank();
            // $CASES-OMITTED$
            default:
                return true;
            }
        }

    }
}
