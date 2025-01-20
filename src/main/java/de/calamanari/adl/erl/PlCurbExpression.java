//@formatter:off
/*
 * PlCurbExpression
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

import static de.calamanari.adl.FormatUtils.appendCurbExpression;
import static de.calamanari.adl.erl.CommentUtils.verifyCommentsApplicable;
import static de.calamanari.adl.erl.PlComment.Position.AFTER_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.C1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * {@link PlCurbExpression} represents a an Audlang curb-expression (syntactic sugar on the presentation layer) to limit an OR (e.g., <i>two of five</i>).
 * <p>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#43-curbed-or">§4.3</a> Audlang Spec
 * 
 * @param curbDelegate {@link PlCombinedExpression} of type {@link CombinedExpressionType#OR} to be curbed
 * @param operator the comparison operator against the bound value
 * @param bound count to compare
 * @param comments optional comments, may be null
 * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public record PlCurbExpression(PlCombinedExpression curbDelegate, PlCurbOperator operator, int bound,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<PlComment> comments, String inline) implements PlExpression<PlCurbExpression> {

    private static final Set<PlComment.Position> VALID_COMMENT_POSITIONS = Collections
            .unmodifiableSet(new LinkedHashSet<>(Arrays.asList(BEFORE_EXPRESSION, C1, AFTER_EXPRESSION)));

    /**
     * @param curbDelegate {@link PlCombinedExpression} of type {@link CombinedExpressionType#OR} to be curbed
     * @param operator the comparison operator against the bound value
     * @param bound count to compare
     * @param comments optional comments, may be null
     * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
     */
    public PlCurbExpression(PlCombinedExpression curbDelegate, PlCurbOperator operator, int bound, List<PlComment> comments,
            @SuppressWarnings("java:S1172") String inline) {

        if (curbDelegate == null || curbDelegate.combiType() != CombinedExpressionType.OR) {
            throw new AudlangValidationException(
                    String.format("CURB-expressions must surround an OR-expression, given: delegate=%s, operator=%s, bound=%s, comments=%s", curbDelegate,
                            operator, bound, comments));
        }

        if (operator == null) {
            throw new AudlangValidationException(
                    String.format("CURB-operator missing, given: delegate=%s, operator=%s, bound=%s, comments=%s", curbDelegate, operator, bound, comments));
        }
        if (bound < 0) {
            throw new AudlangValidationException(String.format("CURB bound must not be negative, given: delegate=%s, operator=%s, bound=%s, comments=%s",
                    curbDelegate, operator, bound, comments));
        }
        this.curbDelegate = curbDelegate;
        this.operator = operator;
        this.bound = bound;
        this.comments = (comments == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(comments)));
        if (!verifyCommentsApplicable(this.comments, VALID_COMMENT_POSITIONS)) {
            throw new AudlangValidationException(
                    String.format("Invalid comment position (expected: %s), given: delegate=%s, operator=%s, bound=%s, comments=%s", VALID_COMMENT_POSITIONS,
                            curbDelegate, operator, bound, this.comments));
        }
        this.inline = format(FormatStyle.INLINE);

    }

    /**
     * @param delegate {@link PlCombinedExpression} of type {@link CombinedExpressionType#OR} to be curbed
     * @param operator the comparison operator against the bound value
     * @param bound count to compare
     * @param comments optional comments, may be null
     */
    public PlCurbExpression(PlCombinedExpression delegate, PlCurbOperator operator, int bound, List<PlComment> comments) {
        this(delegate, operator, bound, comments, null);
    }

    @Override
    public void appendSingleLine(StringBuilder sb, FormatStyle style, int level) {
        operator.formatAndAppend(sb, curbDelegate, bound, comments, style, level, true);
    }

    @Override
    public void appendMultiLine(StringBuilder sb, FormatStyle style, int level) {
        operator.formatAndAppend(sb, curbDelegate, bound, comments, style, level, false);
    }

    @Override
    public List<PlExpression<?>> childExpressions() {
        return Arrays.asList(curbDelegate);
    }

    @Override
    public void collectFieldsInternal(Map<String, AudlangField.Builder> fieldMap) {
        curbDelegate.collectFieldsInternal(fieldMap);
    }

    @Override
    public void collectAllComments(List<PlComment> result) {
        result.addAll(comments);
        curbDelegate.collectAllComments(result);
    }

    @Override
    public void accept(PlExpressionVisitor visitor) {
        visitor.visit(this, Visit.ENTER);
        // skip the OR-level
        curbDelegate.members().stream().forEach(m -> m.accept(visitor));
        visitor.visit(this, Visit.EXIT);
    }

    @Override
    public List<PlComment> allDirectComments() {
        return comments;
    }

    @Override
    public PlCurbExpression stripComments() {
        PlCombinedExpression updatedDelegate = curbDelegate.stripComments();
        if (updatedDelegate == curbDelegate && comments.isEmpty()) {
            return this;
        }
        else {
            return new PlCurbExpression(updatedDelegate, operator, bound, null);
        }
    }

    @Override
    public PlCurbExpression withComments(List<PlComment> comments) {
        if ((comments == null && this.comments.isEmpty()) || this.comments.equals(comments)) {
            return this;
        }
        else {
            return new PlCurbExpression(curbDelegate, operator, bound, comments);
        }
    }

    @Override
    public PlExpression<?> resolveHigherLanguageFeatures() {
        return CurbResolver.resolve(this);
    }

    @Override
    public String toString() {
        return inline;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlCurbExpression plc && plc.inline.equals(this.inline);
    }

    @Override
    public int hashCode() {
        return inline.hashCode();
    }

    /**
     * Operator for comparing the count of matching members against the bound of the curb
     */
    public enum PlCurbOperator {

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#43-curbed-or">§4.3</a> Audlang Spec
         */
        EQUALS("="),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#43-curbed-or">§4.3</a> Audlang Spec
         */
        NOT_EQUALS("!="),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#43-curbed-or">§4.3</a> Audlang Spec
         */
        LESS_THAN("<"),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#43-curbed-or">§4.3</a> Audlang Spec
         */
        LESS_THAN_OR_EQUALS("<="),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#43-curbed-or">§4.3</a> Audlang Spec
         */
        GREATER_THAN(">"),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#43-curbed-or">§4.3</a> Audlang Spec
         */
        GREATER_THAN_OR_EQUALS(">=");

        /**
         * The operator string to be printed between the CURB and the bound
         */
        public final String operatorString;

        private PlCurbOperator(String operatorString) {
            this.operatorString = operatorString;
        }

        /**
         * Appends the formatted curb instructions according to the given style
         * 
         * @param sb destination
         * @param delegate OR's members
         * @param bound
         * @param comments
         * @param style
         * @param level
         * @param forceSingleLine to enforce output on a single line
         */
        void formatAndAppend(StringBuilder sb, PlCombinedExpression delegate, int bound, List<PlComment> comments, FormatStyle style, int level,
                boolean forceSingleLine) {
            appendCurbExpression(sb, delegate, operatorString, bound, comments, new FormatInfo(style, level, forceSingleLine));
        }

        /**
         * @return the opposite operator of this one
         */
        public PlCurbOperator negate() {
            switch (this) {
            case EQUALS:
                return NOT_EQUALS;
            case NOT_EQUALS:
                return EQUALS;
            case LESS_THAN:
                return GREATER_THAN_OR_EQUALS;
            case LESS_THAN_OR_EQUALS:
                return GREATER_THAN;
            case GREATER_THAN:
                return LESS_THAN_OR_EQUALS;
            case GREATER_THAN_OR_EQUALS:
                return LESS_THAN;
            default:
                throw new IllegalStateException("Unknown operator: " + this);
            }

        }
    }

}
