//@formatter:off
/*
 * PlCommentExpression
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

package de.calamanari.adl.erl;

import java.io.Serializable;

import de.calamanari.adl.AudlangFormattable;
import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.FormatConstants;
import de.calamanari.adl.FormatStyle;

import static de.calamanari.adl.FormatUtils.space;

/**
 * A {@link PlComment} represents a comment, see
 * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#15-comments">§1.5 AudLang Spec</a>
 * <p>
 * This implementation handles comments explicitly as first-class-citizens.
 * <p>
 * Comment text will be implicitly normalized (see {@link CommentUtils#normalizeComment(String)} to make output reproducible.
 * <p>
 * <b>This will ...</b>
 * <ul>
 * <li>... affect line breaks, new lines are fully controlled by the formatter and they are independent from the given text.</li>
 * <li>... eliminate any control characters such as tabulator.</li>
 * <li>... condense any whitespace <i>outside</i> double-quoted areas into a single space each. The latter compromise shall preserve quoted Audlang
 * expressions.</li>
 * </ul>
 * 
 * @param comment not null, text <b>must</b> start with <code>'/*'</code> and end with <code>'*&#47;'</code>, so the shortest possible comment is
 *            <code>'/**&#47;'</code>
 * @param position not null
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public record PlComment(String comment, Position position) implements AudlangFormattable, Serializable {

    /**
     * @param comment not null, text <b>must</b> start with <code>'/*'</code> and end with <code>'*&#47;'</code>, so the shortest possible comment is
     *            <code>'/**&#47;'</code>
     * @param position not null
     */
    public PlComment(String comment, Position position) {

        if (comment == null) {
            throw new AudlangValidationException("comment must not be null");
        }
        if (!comment.startsWith("/*") || !comment.endsWith("*/") || comment.length() < 4) {
            throw new AudlangValidationException(String.format(
                    "comment must start with '/*' and end with '*/', so the shortest possible comment is '/**/', given: comment='%s', position=%s", comment,
                    position));
        }
        if (position == null) {
            throw new AudlangValidationException(String.format("Comment position must be specified, given: comment='%s', position=%s", comment, position));
        }
        this.comment = CommentUtils.normalizeComment(comment);
        this.position = position;
    }

    @Override
    public void appendSingleLine(StringBuilder sb, FormatStyle style, int level) {

        if (!sb.isEmpty()) {
            space(sb);
        }
        sb.append(comment);
    }

    @Override
    public boolean shouldUseMultiLineFormatting(FormatStyle style) {
        return style.isMultiLine() && isComplex();
    }

    @Override
    public void appendMultiLine(StringBuilder sb, FormatStyle style, int level) {
        CommentUtils.appendCommentMultiLine(comment, sb, style, level);
    }

    /**
     * @return true if this comment contains line-breaks or should be written on its own dedicated line
     */
    public boolean isComplex() {
        return (comment.length() > FormatConstants.COMPLEX_COMMENT_THRESHOLD);
    }

    /**
     * Specification where we allow printing comments, there are two positions which are valid for every expression, two which are valid for all operands and a
     * couple of optional positions ({@link #C1}, {@link #C2}, ...) which can only be used if the expression allows that. In other words: each element
     * represents a textual gap where Audlang allows to place a comment.
     */
    public enum Position {
        BEFORE_EXPRESSION, C1, C2, C3, C4, C5, C6, BEFORE_OPERAND, AFTER_OPERAND, AFTER_EXPRESSION;

        public Position next() {
            return Position.values()[this.ordinal() + 1];
        }

    }

}
