//@formatter:off
/*
 * PlOperand
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

import static de.calamanari.adl.FormatUtils.appendIndentOrWhitespace;
import static de.calamanari.adl.erl.CommentUtils.appendComments;
import static de.calamanari.adl.erl.CommentUtils.verifyCommentsApplicable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import de.calamanari.adl.AudlangFormattable;
import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.erl.PlComment.Position;
import de.calamanari.adl.util.AdlTextUtils;

/**
 * A {@link PlOperand} can either be a plain string value or an argument reference in Audlang comparison to an argument
 * <p/>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#122-argument-values">§1.2.2</a>,
 * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#123-argument-reference">§1.2.3</a> Audlang
 * Spec
 * 
 * @param value the value or argRef (without the '@'), not null
 * @paran isReference true if this operand is an argument name reference
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public record PlOperand(String value, @JsonInclude(JsonInclude.Include.NON_DEFAULT) boolean isReference,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<PlComment> comments) implements AudlangFormattable, CommentAware, Serializable {

    private static final Set<PlComment.Position> VALID_COMMENT_POSITIONS = Collections
            .unmodifiableSet(new LinkedHashSet<>(Arrays.asList(PlComment.Position.BEFORE_OPERAND, PlComment.Position.AFTER_OPERAND)));

    /**
     * @param value either a plain value of the plain name of another argument
     * @param isReference true to indicate that the given value is the name of another argument
     * @param comments optional list of comments, may be null
     */
    public PlOperand(String value, boolean isReference, List<PlComment> comments) {
        if (value == null) {
            throw new AudlangValidationException("value must not be null");
        }
        this.value = value;
        this.isReference = isReference;
        this.comments = (comments == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(comments)));
        if (!verifyCommentsApplicable(this.comments, VALID_COMMENT_POSITIONS)) {
            throw new AudlangValidationException(String.format("Invalid comment position (expected: %s), given: value=%s, isReference=%s, comments=%s",
                    VALID_COMMENT_POSITIONS, value, isReference, this.comments));
        }

    }

    public void appendInternal(StringBuilder sb, FormatStyle style, int level, boolean forceSingleLine) {
        if (appendComments(sb, comments, Position.BEFORE_OPERAND, style, level, forceSingleLine)) {
            appendIndentOrWhitespace(sb, style, level);
        }

        String outputValue = AdlTextUtils.addDoubleQuotesIfRequired(AdlTextUtils.escapeSpecialCharacters(value));
        if (isReference()) {
            sb.append('@');
        }
        sb.append(outputValue);
        appendComments(sb, comments, Position.AFTER_OPERAND, style, level, forceSingleLine);
    }

    @Override
    public void appendSingleLine(StringBuilder sb, FormatStyle style, int level) {
        appendInternal(sb, style, level, true);
    }

    @Override
    public void appendMultiLine(StringBuilder sb, FormatStyle style, int level) {
        appendInternal(sb, style, level, false);
    }

    @Override
    public void collectAllComments(List<PlComment> result) {
        result.addAll(comments);
    }

    private boolean operandHasComplexComments() {
        // @formatter:off
        return comments.size() > 2 
                || comments.stream().filter(comment -> comment.position() == Position.BEFORE_OPERAND).count() > 1
                || comments.stream().filter(comment -> comment.position() == Position.AFTER_OPERAND).count() > 1
                || comments.stream().anyMatch(PlComment::isComplex);
        // @formatter:on
    }

    @Override
    public List<PlComment> allDirectComments() {
        return comments;
    }

    @Override
    public boolean shouldUseMultiLineFormatting(FormatStyle style) {
        return style.isMultiLine() && operandHasComplexComments();
    }

    /**
     * @return a new operand without comments or this instance if there were no comments
     */
    public PlOperand stripComments() {
        if (comments.isEmpty()) {
            return this;
        }
        else {
            return new PlOperand(value, isReference, null);
        }
    }

    /**
     * @param comments
     * @return a new operand with the given comments or this instance if there was no change
     */
    public PlOperand withComments(List<PlComment> comments) {
        if ((comments == null && this.comments.isEmpty()) || this.comments.equals(comments)) {
            return this;
        }
        else {
            return new PlOperand(value, isReference, comments);
        }
    }

}
