//@formatter:off
/*
 * AudlangFormattable
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

package de.calamanari.adl;

/**
 * Interface to be implemented by objects that can be printed as a String inline more sophisticated than toString().
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface AudlangFormattable {

    /**
     * Appends the string representation <b>without any linebreaks</b> to the string builder.
     * 
     * @param sb destination
     * @param style format settings
     * @param level current depth in the output
     */
    void appendSingleLine(StringBuilder sb, FormatStyle style, int level);

    /**
     * Append this object's formatted string representation to the given string builder.
     * 
     * @param sb string builder
     * @param style output style
     * @param level current depth in the output tree for indentation
     */
    void appendMultiLine(StringBuilder sb, FormatStyle style, int level);

    /**
     * @param style
     * @return true if based on the style and the object data we should print multi-line, otherwise false
     */
    boolean shouldUseMultiLineFormatting(FormatStyle style);

    /**
     * @return true if this item needs a composite format
     */
    default boolean enforceCompositeFormat() {
        return false;
    }

    /**
     * Formats this object as a string according to the given style information
     * 
     * @param style how to format the output
     * @return object formatted according to the style
     */
    default String format(FormatStyle style) {
        StringBuilder sb = new StringBuilder();
        if (shouldUseMultiLineFormatting(style)) {
            appendMultiLine(sb, style, 0);
        }
        else {
            appendSingleLine(sb, style, 0);
        }
        FormatUtils.stripTrailingWhitespace(sb);
        return sb.toString();
    }

}
