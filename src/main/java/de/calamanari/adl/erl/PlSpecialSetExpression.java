//@formatter:off
/*
 * PlSpecialExpression
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

import static de.calamanari.adl.FormatUtils.appendSpecialSetExpression;
import static de.calamanari.adl.erl.CommentUtils.verifyCommentsApplicable;
import static de.calamanari.adl.erl.PlComment.Position.AFTER_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_EXPRESSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.FormatUtils.FormatInfo;
import de.calamanari.adl.SpecialSetType;

/**
 * {@link PlSpecialSetExpression} stands for the Audlang expressions <code>&lt;ALL&gt;</code> (resp. no restriction, always true, {@link SpecialSetType#ALL})
 * and <code>&lt;NONE&gt;</code> (never true, {@link SpecialSetType#NONE})
 * 
 * <p>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#39-all-and-none">§3.9</a> Audlang
 * Spec
 * 
 * @param setType distinguishes between ALL and NONE
 * @param comments optional comments, maybe null
 * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public record PlSpecialSetExpression(SpecialSetType setType, @JsonInclude(JsonInclude.Include.NON_EMPTY) List<PlComment> comments, String inline)
        implements PlExpression<PlSpecialSetExpression> {

    private static final Set<PlComment.Position> VALID_COMMENT_POSITIONS = Collections
            .unmodifiableSet(new LinkedHashSet<>(Arrays.asList(BEFORE_EXPRESSION, AFTER_EXPRESSION)));

    /**
     * @param setType distinguishes between ALL and NONE
     * @param comments optional comments, maybe null
     * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
     */
    public PlSpecialSetExpression(SpecialSetType setType, List<PlComment> comments, @SuppressWarnings("java:S1172") String inline) {
        if (setType == null) {
            throw new AudlangValidationException("setType must not be null");
        }
        this.setType = setType;
        this.comments = (comments == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(comments)));
        if (!verifyCommentsApplicable(this.comments, VALID_COMMENT_POSITIONS)) {
            throw new AudlangValidationException(String.format("Invalid comment position (expected: %s), given: setType=<%s>, comments=%s",
                    VALID_COMMENT_POSITIONS, setType, this.comments));
        }
        this.inline = format(FormatStyle.INLINE);
    }

    /**
     * @param type distinguishes between ALL and NONE
     * @param comments optional comments, maybe null
     */
    public PlSpecialSetExpression(SpecialSetType type, List<PlComment> comments) {
        this(type, comments, null);
    }

    @Override
    public void appendSingleLine(StringBuilder sb, FormatStyle style, int level) {
        appendSpecialSetExpression(sb, setType.name(), comments, new FormatInfo(style, level, true));
    }

    @Override
    public void appendMultiLine(StringBuilder sb, FormatStyle style, int level) {
        appendSpecialSetExpression(sb, setType.name(), comments, new FormatInfo(style, level));
    }

    @Override
    public void accept(PlExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<PlComment> allDirectComments() {
        return comments;
    }

    @Override
    public String toString() {
        return inline;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlSpecialSetExpression pls && pls.inline.equals(this.inline);
    }

    @Override
    public int hashCode() {
        return inline.hashCode();
    }

    @Override
    public void collectAllComments(List<PlComment> result) {
        result.addAll(comments);
    }

    @Override
    public PlSpecialSetExpression stripComments() {
        if (comments.isEmpty()) {
            return this;
        }
        else {
            return new PlSpecialSetExpression(setType, null);
        }
    }

    @Override
    public PlExpression<?> resolveHigherLanguageFeatures() {
        return stripComments();
    }

    @Override
    public PlSpecialSetExpression withComments(List<PlComment> comments) {
        if ((comments == null && this.comments.isEmpty()) || this.comments.equals(comments)) {
            return this;
        }
        else {
            return new PlSpecialSetExpression(setType, comments);
        }
    }

}
