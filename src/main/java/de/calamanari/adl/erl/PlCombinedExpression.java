//@formatter:off
/*
 * PlCombinedExpression
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

import static de.calamanari.adl.FormatUtils.appendCombinedExpressionMultiLine;
import static de.calamanari.adl.FormatUtils.appendCombinedExpressionSingleLine;
import static de.calamanari.adl.erl.CommentUtils.verifyCommentsApplicable;
import static de.calamanari.adl.erl.PlComment.Position.AFTER_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_EXPRESSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.calamanari.adl.AudlangField;
import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.FormatUtils.FormatInfo;
import de.calamanari.adl.Visit;

/**
 * A {@link PlCombinedExpression} connects at least two expressions to form a new one either with {@link CombinedExpressionType#OR} or
 * {@link CombinedExpressionType#AND}
 * <p>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#41-logical-and">§4.1</a>,
 * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#42-logical-or">§4.2</a> Audlang Spec
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public record PlCombinedExpression(CombinedExpressionType combiType, List<PlExpression<?>> members,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<PlComment> comments, String inline) implements PlExpression<PlCombinedExpression> {

    private static final Set<PlComment.Position> VALID_COMMENT_POSITIONS = Collections
            .unmodifiableSet(new LinkedHashSet<>(Arrays.asList(BEFORE_EXPRESSION, AFTER_EXPRESSION)));

    /**
     * @param combiType logical connector
     * @param members the elements inside, list must at least contain two elements
     * @param comments optional comments, may be null
     * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
     */
    public PlCombinedExpression(CombinedExpressionType combiType, List<PlExpression<?>> members, List<PlComment> comments,
            @SuppressWarnings("java:S1172") String inline) {
        if (members == null || members.size() < 2) {
            throw new AudlangValidationException(String.format(
                    "AND- resp. OR-expressions must at least have two members, given: members=%s, combiType=%s, comments=%s", members, combiType, comments));
        }
        this.members = Collections.unmodifiableList(new ArrayList<>(members));
        if (combiType == null) {
            throw new AudlangValidationException(
                    String.format("combiType must not be null, given: members=%s, combiType=%s, comments=%s", members, combiType, comments));
        }
        this.combiType = combiType;
        this.comments = (comments == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(comments)));
        if (!verifyCommentsApplicable(this.comments, VALID_COMMENT_POSITIONS)) {
            throw new AudlangValidationException(String.format("Invalid comment position (expected: %s), given: members=%s, combiType=%s, comments=%s",
                    VALID_COMMENT_POSITIONS, members, combiType, this.comments));
        }
        this.inline = format(FormatStyle.INLINE);
    }

    /**
     * @param type AND vs. OR
     * @param members the elements inside, list must at least contain two members
     * @param comments optional comments, may be null
     */
    public PlCombinedExpression(CombinedExpressionType type, List<PlExpression<?>> members, List<PlComment> comments) {
        this(type, members, comments, null);
    }

    @Override
    public void appendSingleLine(StringBuilder sb, FormatStyle style, int level) {
        appendCombinedExpressionSingleLine(sb, combiType, members, comments, new FormatInfo(style, level));
    }

    @Override
    public void appendMultiLine(StringBuilder sb, FormatStyle style, int level) {
        appendCombinedExpressionMultiLine(sb, combiType, members, comments, new FormatInfo(style, level));
    }

    @Override
    public void accept(PlExpressionVisitor visitor) {
        visitor.visit(this, Visit.ENTER);
        members.stream().forEach(m -> m.accept(visitor));
        visitor.visit(this, Visit.EXIT);
    }

    @Override
    public boolean enforceCompositeFormat() {
        return true;
    }

    @Override
    public List<PlComment> allDirectComments() {
        return comments;
    }

    @Override
    public List<PlExpression<?>> childExpressions() {
        return members;
    }

    @Override
    public void collectFieldsInternal(Map<String, AudlangField.Builder> fieldMap) {
        members.forEach(member -> member.collectFieldsInternal(fieldMap));
    }

    @Override
    public void collectAllComments(List<PlComment> result) {
        result.addAll(comments);
        members.forEach(member -> member.collectAllComments(result));
    }

    @Override
    public PlCombinedExpression stripComments() {
        List<PlExpression<?>> updatedMembers = members.stream().map(PlExpression::stripComments).collect(Collectors.toCollection(ArrayList::new));
        if (comments.isEmpty() && updatedMembers.equals(members)) {
            return this;
        }
        else {
            return new PlCombinedExpression(combiType, updatedMembers, null);
        }
    }

    @Override
    public PlCombinedExpression withComments(List<PlComment> comments) {
        if ((comments == null && this.comments.isEmpty()) || this.comments.equals(comments)) {
            return this;
        }
        else {
            return new PlCombinedExpression(combiType, members, comments);
        }
    }

    @Override
    public PlExpression<?> resolveHigherLanguageFeatures() {
        // @formatter:off
        List<PlExpression<?>> updatedMembers = members.stream()
                .map(PlExpression::resolveHigherLanguageFeatures)
                .collect(Collectors.toCollection(ArrayList<PlExpression<?>>::new));
        // @formatter:on
        PlExpression<?> res = new PlCombinedExpression(combiType, updatedMembers, null);
        if (res.equals(this)) {
            res = this;
        }
        return res;
    }

    @Override
    public String toString() {
        return inline;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlCombinedExpression plc && plc.inline.equals(this.inline);
    }

    @Override
    public int hashCode() {
        return inline.hashCode();
    }

}
