//@formatter:off
/*
 * FormatUtils
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

import static de.calamanari.adl.CombinedExpressionType.createOperatorString;
import static de.calamanari.adl.FormatConstants.T_CURB;
import static de.calamanari.adl.FormatConstants.T_NOT;
import static de.calamanari.adl.FormatConstants.T_STRICT;
import static de.calamanari.adl.erl.CommentUtils.appendComments;
import static de.calamanari.adl.erl.CommentUtils.appendCommentsOrWhitespace;
import static de.calamanari.adl.erl.PlComment.Position.AFTER_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.C1;

import java.util.Collections;
import java.util.List;

import de.calamanari.adl.erl.CommentAware;
import de.calamanari.adl.erl.CommentUtils;
import de.calamanari.adl.erl.PlComment;

/**
 * This class contains a couple of helper methods to simplify and standardize formatting across the Audlang expressions
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class FormatUtils {

    /**
     * Removes all white characters from the end of the string builder
     * <p>
     * Ideally this should not be necessary when building output but it turned out to be complex as hell to consider all possible cases. This method eases the
     * pain! ;-)
     * 
     * @param sb
     */
    public static void stripTrailingWhitespace(StringBuilder sb) {
        int len = sb.length();
        for (int i = sb.length() - 1; i > -1; i--) {
            char ch = sb.charAt(i);
            if (Character.isWhitespace(ch)) {
                len--;
            }
            else {
                break;
            }
        }
        sb.setLength(len);
    }

    /**
     * Appends the given indent n times to the string builder, the default implementation appends {@link FormatConstants#DEFAULT_INDENT}
     * 
     * @param sb to be modified
     * @param style determines indentation
     * @param n number of times to append 4 spaces
     */
    public static void appendIndent(StringBuilder sb, FormatStyle style, int n) {
        for (int i = 0; i < n; i++) {
            sb.append(style.getIndent());
        }
    }

    /**
     * Depending on the style (multi-line or not), this method adds a single space or a line break, if and only if there is not already one.
     * 
     * @param sb
     * @param style
     * @param level
     */
    public static void appendIndentOrWhitespace(StringBuilder sb, FormatStyle style, int level) {
        if (endsWithNewLine(sb)) {
            appendIndent(sb, style, level);
        }
        else if (!sb.isEmpty()) {
            space(sb);
        }

    }

    /**
     * Depending on the configured style either appends an optional line break followed by indentation or just a space character.
     * <p>
     * For convenience reasons the method will not repeat the space or indentation after an existing space or indentation.
     * 
     * @param sb
     * @param style
     * @param level
     * @param prependLineBreak first adds the line break before the indentation
     */
    public static void appendIndentOrWhitespace(StringBuilder sb, FormatStyle style, int level, boolean prependLineBreak) {
        if (style.isMultiLine() && prependLineBreak) {
            stripTrailingWhitespace(sb);
            newLine(sb);
        }
        FormatUtils.appendIndentOrWhitespace(sb, style, level);
    }

    /**
     * Adds a left-brace (open) to the builder prepended by a space if and only if the character right before is an opening brace or the end of a comment to
     * improve readability
     * 
     * @param sb
     */
    public static void openBrace(StringBuilder sb) {
        if (!sb.isEmpty() && (sb.charAt(sb.length() - 1) == '(' || endsWith(sb, "*/"))) {
            sb.append(' ');
        }
        sb.append("(");
    }

    /**
     * Adds a right-brace (close) to the builder prepended by a space if and only if the character right before is a closing brace or the end of a comment to
     * improve readability
     * 
     * @param sb
     */
    public static void closeBrace(StringBuilder sb) {
        if (!sb.isEmpty() && (sb.charAt(sb.length() - 1) == ')' || endsWith(sb, "*/"))) {
            sb.append(' ');
        }
        sb.append(")");
    }

    /**
     * Adds a single space if the last character is not whitespace
     */
    public static void space(StringBuilder sb) {
        if (sb.isEmpty() || !Character.isWhitespace(sb.charAt(sb.length() - 1))) {
            sb.append(" ");
        }
    }

    /**
     * Adds a space (if not empty/linbreak right before), value, space, value, ..., value, space
     * 
     * @see #space(StringBuilder)
     * 
     * @param sb
     * @param value(s) to append surrounded by space
     */
    public static void appendSpaced(StringBuilder sb, String... values) {
        if (values.length > 0) {
            for (String value : values) {
                if (!sb.isEmpty()) {
                    space(sb);
                }
                sb.append(value);
            }
            space(sb);
        }
    }

    /**
     * Adds a comma. Removes a line break right before to avoid unnatural lines starting with a comma
     * 
     * @param sb
     */
    public static void comma(StringBuilder sb) {
        if (endsWithNewLine(sb)) {
            sb.setLength(sb.length() - 1);
        }
        sb.append(',');
    }

    /**
     * @param sb
     * @param pattern
     * @return true if the content of the string builder ends with the given pattern
     */
    public static boolean endsWith(StringBuilder sb, String pattern) {
        boolean res = false;
        if (sb.length() >= pattern.length()) {
            int offset = sb.length() - pattern.length();
            for (int i = 0; i < pattern.length(); i++) {
                if (sb.charAt(offset + i) != pattern.charAt(i)) {
                    return false;
                }
            }
            res = true;
        }
        return res;
    }

    /**
     * Appends a line break if there is not already one at the end of the string builder
     * 
     * @param sb
     */
    public static void newLine(StringBuilder sb) {

        if (!endsWithNewLine(sb)) {
            stripTrailingWhitespace(sb);
            sb.append(FormatConstants.LINE_BREAK);
        }
    }

    /**
     * @param sb
     * @return true if the string builder ends with a line break
     */
    public static boolean endsWithNewLine(StringBuilder sb) {
        return endsWith(sb, FormatConstants.LINE_BREAK);
    }

    /**
     * Appends a combined (AND/OR) expression inline
     * 
     * @param sb
     * @param combiType
     * @param members
     * @param formatInfo
     */
    public static void appendCombinedExpressionSingleLine(StringBuilder sb, CombinedExpressionType combiType, List<? extends AudlangFormattable> members,
            FormatInfo formatInfo) {
        appendCombinedExpressionSingleLine(sb, combiType, members, Collections.emptyList(), formatInfo);
    }

    /**
     * Appends a combined (AND/OR) expression inline
     * 
     * @param sb
     * @param combiType
     * @param members
     * @param comments
     * @param formatInfo
     */
    public static void appendCombinedExpressionSingleLine(StringBuilder sb, CombinedExpressionType combiType, List<? extends AudlangFormattable> members,
            List<PlComment> comments, FormatInfo formatInfo) {
        if (appendComments(sb, comments, BEFORE_EXPRESSION, formatInfo.style, formatInfo.level, true)) {
            space(sb);
        }

        if (formatInfo.level > 0) {
            openBrace(sb);
        }

        for (int i = 0; i < members.size(); i++) {
            if (i > 0) {
                space(sb);
                sb.append(createOperatorString(combiType, formatInfo.style, 0));
                space(sb);
            }

            AudlangFormattable member = members.get(i);
            if (formatInfo.style == FormatStyle.INLINE && !(member instanceof CommentAware)) {
                appendMemberAsStringCopy(sb, member, true);
            }
            else {
                member.appendSingleLine(sb, formatInfo.style, formatInfo.level + 1);
            }
        }
        if (formatInfo.level > 0) {
            closeBrace(sb);
        }
        appendComments(sb, comments, AFTER_EXPRESSION, formatInfo.style, 0, true);
    }

    /**
     * Appends a combined (AND/OR) expression multi-line
     * 
     * @param sb
     * @param combiType
     * @param members
     * @param formatInfo
     */
    public static void appendCombinedExpressionMultiLine(StringBuilder sb, CombinedExpressionType combiType, List<? extends AudlangFormattable> members,
            FormatInfo formatInfo) {
        appendCombinedExpressionMultiLine(sb, combiType, members, Collections.emptyList(), formatInfo);
    }

    /**
     * Appends a combined (AND/OR) expression multi-line
     * 
     * @param sb
     * @param combiType
     * @param members
     * @param comments
     * @param formatInfo
     */
    public static void appendCombinedExpressionMultiLine(StringBuilder sb, CombinedExpressionType combiType, List<? extends AudlangFormattable> members,
            List<PlComment> comments, FormatInfo formatInfo) {
        if (appendComments(sb, comments, BEFORE_EXPRESSION, formatInfo.style, formatInfo.level, false)) {
            newLine(sb);
            appendIndent(sb, formatInfo.style, formatInfo.level);
        }
        boolean mustUseBraces = formatInfo.level > 0;
        if (mustUseBraces) {
            openBrace(sb);
            newLine(sb);
        }
        for (int i = 0; i < members.size(); i++) {
            appendIndent(sb, formatInfo.style, formatInfo.level);
            if (i > 0) {
                sb.append(createOperatorString(combiType, formatInfo.style, formatInfo.level));
                space(sb);
            }
            else if (mustUseBraces) {
                sb.append(FormatConstants.DEFAULT_INDENT);
            }
            AudlangFormattable member = members.get(i);

            int subLevel = formatInfo.level;

            if (member.enforceCompositeFormat() || (member instanceof CommentAware caw && caw.allDirectComments().stream().anyMatch(PlComment::isComplex))) {
                subLevel++;
            }

            member.appendMultiLine(sb, formatInfo.style, subLevel);
            newLine(sb);
        }
        if (mustUseBraces) {
            appendIndent(sb, formatInfo.style, formatInfo.level);
            closeBrace(sb);
        }
        appendComments(sb, comments, AFTER_EXPRESSION, formatInfo.style, formatInfo.level, false);
    }

    /**
     * Appends a negation expression in single-line style
     * 
     * @param sb
     * @param delegate
     * @param isStrict
     * @param formatInfo
     */
    public static void appendNegationExpressionSingleLine(StringBuilder sb, AudlangFormattable delegate, boolean isStrict, FormatInfo formatInfo) {
        appendNegationExpressionSingleLine(sb, delegate, isStrict, Collections.emptyList(), formatInfo);
    }

    /**
     * Appends a negation expression in single-line style
     * 
     * @param sb
     * @param delegate
     * @param isStrict
     * @param comments
     * @param formatInfo
     */
    public static void appendNegationExpressionSingleLine(StringBuilder sb, AudlangFormattable delegate, boolean isStrict, List<PlComment> comments,
            FormatInfo formatInfo) {
        if (CommentUtils.appendComments(sb, comments, BEFORE_EXPRESSION, formatInfo.style, formatInfo.level, true)) {
            space(sb);
        }

        if (isStrict) {
            sb.append(T_STRICT);
            CommentUtils.appendCommentsOrWhitespace(sb, comments, C1, formatInfo.style, 0, true);
        }
        sb.append(T_NOT);
        space(sb);
        if (formatInfo.style == FormatStyle.INLINE) {
            appendMemberAsStringCopy(sb, delegate, false);
        }
        else {
            delegate.appendSingleLine(sb, formatInfo.style, formatInfo.level + 1);
        }
    }

    /**
     * Appends a negation expression in multi-line style
     * 
     * @param sb
     * @param delegate
     * @param isStrict
     * @param formatInfo
     */
    public static void appendNegationExpressionMultiLine(StringBuilder sb, AudlangFormattable delegate, boolean isStrict, FormatInfo formatInfo) {
        appendNegationExpressionMultiLine(sb, delegate, isStrict, Collections.emptyList(), formatInfo);
    }

    /**
     * Appends a negation expression in multi-line style
     * 
     * @param sb
     * @param delegate
     * @param isStrict
     * @param comments
     * @param formatInfo
     */
    public static void appendNegationExpressionMultiLine(StringBuilder sb, AudlangFormattable delegate, boolean isStrict, List<PlComment> comments,
            FormatInfo formatInfo) {

        if (appendComments(sb, comments, BEFORE_EXPRESSION, formatInfo.style, formatInfo.level, false)) {
            appendIndentOrWhitespace(sb, formatInfo.style, formatInfo.level);
        }

        if (isStrict) {
            sb.append(T_STRICT);
            appendCommentsOrWhitespace(sb, comments, C1, formatInfo.style, formatInfo.level, false);
            appendIndentOrWhitespace(sb, formatInfo.style, formatInfo.level);
        }
        sb.append(T_NOT);
        appendIndentOrWhitespace(sb, formatInfo.style, formatInfo.level + 1);
        delegate.appendMultiLine(sb, formatInfo.style, formatInfo.level + 1);
    }

    /**
     * Appends a curb expression
     * 
     * @param sb
     * @param delegate
     * @param operatorString
     * @param bound
     * @param formatInfo
     */
    public static void appendCurbExpression(StringBuilder sb, AudlangFormattable delegate, String operatorString, int bound, FormatInfo formatInfo) {
        appendCurbExpression(sb, delegate, operatorString, bound, Collections.emptyList(), formatInfo);
    }

    /**
     * Appends a curb expression
     * 
     * @param sb
     * @param delegate
     * @param operatorString
     * @param bound
     * @param comments
     * @param formatInfo
     */
    public static void appendCurbExpression(StringBuilder sb, AudlangFormattable delegate, String operatorString, int bound, List<PlComment> comments,
            FormatInfo formatInfo) {

        if (appendComments(sb, comments, BEFORE_EXPRESSION, formatInfo.style, formatInfo.level, formatInfo.forceSingleLine)) {
            appendIndentOrWhitespace(sb, formatInfo.style, formatInfo.level);
        }

        sb.append(T_CURB);
        space(sb);

        if (formatInfo.style == FormatStyle.INLINE && !(delegate instanceof CommentAware)) {
            appendMemberAsStringCopy(sb, delegate, true);
        }
        else if (formatInfo.forceSingleLine) {
            delegate.appendSingleLine(sb, formatInfo.style, formatInfo.level + 1);
        }
        else {
            delegate.appendMultiLine(sb, formatInfo.style, formatInfo.level + 1);
            if (delegate instanceof CommentAware caw && caw.allDirectComments().stream().noneMatch(cmn -> cmn.position() == AFTER_EXPRESSION)) {
                stripTrailingWhitespace(sb);
            }
        }

        appendIndentOrWhitespace(sb, formatInfo.style, formatInfo.level + 1);
        sb.append(operatorString);
        appendCommentsOrWhitespace(sb, comments, C1, formatInfo.style, formatInfo.level + 1, formatInfo.forceSingleLine);
        appendIndentOrWhitespace(sb, formatInfo.style, formatInfo.level + 1);
        sb.append(String.valueOf(bound));
        appendComments(sb, comments, AFTER_EXPRESSION, formatInfo.style, formatInfo.level, formatInfo.forceSingleLine);
    }

    /**
     * Considers extra braces when directly concatenating composite members in inline-style
     * 
     * @param sb
     * @param member
     */
    private static void appendMemberAsStringCopy(StringBuilder sb, AudlangFormattable member, boolean enforceComposite) {
        // @formatter:off
        boolean needsExtraBraces = (
                ((enforceComposite || member.enforceCompositeFormat()) && (member instanceof CommentAware caw && caw.allDirectComments().isEmpty()))
                || (!(member instanceof CommentAware) && member.enforceCompositeFormat())
                );
        // @formatter:on
        String memberString = member.toString();
        if (needsExtraBraces) {
            openBrace(sb);
        }
        if (!sb.isEmpty() && (FormatUtils.endsWith(sb, "(") && memberString.startsWith("(") || memberString.startsWith("/*"))) {
            space(sb);
        }
        sb.append(memberString);
        if (needsExtraBraces) {
            if (memberString.endsWith(")") || memberString.endsWith("*/")) {
                space(sb);
            }
            closeBrace(sb);
        }
    }

    /**
     * Appends one of the two special set expressions (NONE/ALL)
     * 
     * @param sb
     * @param specialSetTypeString name of the set ALL or NONE
     * @param formatInfo
     */
    public static void appendSpecialSetExpression(StringBuilder sb, String specialSetTypeString, FormatInfo formatInfo) {
        appendSpecialSetExpression(sb, specialSetTypeString, Collections.emptyList(), formatInfo);
    }

    /**
     * Appends one of the two special set expressions (NONE/ALL)
     * 
     * @param sb
     * @param specialSetTypeString name of the set ALL or NONE
     * @param comments
     * @param formatInfo
     */
    public static void appendSpecialSetExpression(StringBuilder sb, String specialSetTypeString, List<PlComment> comments, FormatInfo formatInfo) {
        if (appendComments(sb, comments, BEFORE_EXPRESSION, formatInfo.style, formatInfo.level, formatInfo.forceSingleLine)) {
            appendIndentOrWhitespace(sb, formatInfo.style, formatInfo.level);
        }
        sb.append("<");
        sb.append(specialSetTypeString);
        sb.append(">");
        appendComments(sb, comments, AFTER_EXPRESSION, formatInfo.style, formatInfo.level, formatInfo.forceSingleLine);
    }

    /**
     * Computes the maximum string length of a list of values
     * 
     * @param values
     * @return max string length
     */
    public static int maxLength(List<String> values) {
        int max = 0;
        for (String value : values) {
            int len = value == null ? 4 : value.length();
            if (len > max) {
                max = len;
            }
        }
        return max;
    }

    /**
     * Appends a character n times to the string builder
     * 
     * @param sb
     * @param ch
     * @param times
     */
    public static void appendRepeat(StringBuilder sb, char ch, int times) {
        for (int i = 0; i < times; i++) {
            sb.append(ch);
        }
    }

    /**
     * Appends a value of a width n &lt;= size to the string builder, so that the result is enclosed in spaces to let the value appear centered in its reserved
     * range
     * 
     * @param sb
     * @param value (null appears as the literal null)
     * @param size
     */
    public static void appendAlignCenter(StringBuilder sb, String value, int size) {
        value = String.valueOf(value);
        if (value.length() >= size) {
            sb.append(value.substring(0, size));
        }
        else {
            int extraSpace = size - value.length();
            int extraSpaceLeft = extraSpace / 2;
            int extraSpaceRight = extraSpace - extraSpaceLeft;
            appendRepeat(sb, ' ', extraSpaceLeft);
            sb.append(value);
            appendRepeat(sb, ' ', extraSpaceRight);
        }
    }

    /**
     * Appends a value of a width n &lt;= size to the string builder, so that the result followed by spaces to let the value appear left-aligned in its reserved
     * range
     * 
     * @param sb
     * @param value (null appears as the literal null)
     * @param size
     */
    public static void appendAlignLeft(StringBuilder sb, String value, int size) {
        value = String.valueOf(value);
        if (value.length() >= size) {
            sb.append(value.substring(0, size));
        }
        else {
            int extraSpace = size - value.length();
            sb.append(value);
            appendRepeat(sb, ' ', extraSpace);
        }
    }

    /**
     * Appends a value of a width n &lt;= size to the string builder, so that the result followed by spaces to let the value appear right-aligned in its
     * reserved range
     * 
     * @param sb
     * @param value (null appears as the literal null)
     * @param size
     */
    public static void appendAlignRight(StringBuilder sb, String value, int size) {
        value = String.valueOf(value);
        if (value.length() >= size) {
            sb.append(value.substring(0, size));
        }
        else {
            int extraSpace = size - value.length();
            appendRepeat(sb, ' ', extraSpace);
            sb.append(value);
        }
    }

    private FormatUtils() {
        // utilities
    }

    /**
     * Container to keep the parameter lists a bit shorter
     */
    public static record FormatInfo(FormatStyle style, int level, boolean forceSingleLine) {

        public FormatInfo(FormatStyle style, int level) {
            this(style, level, false);
        }
    }
}
