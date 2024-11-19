//@formatter:off
/*
 * PlMatchExpression
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

import static de.calamanari.adl.FormatConstants.T_ANY;
import static de.calamanari.adl.FormatConstants.T_BETWEEN;
import static de.calamanari.adl.FormatConstants.T_CONTAINS;
import static de.calamanari.adl.FormatConstants.T_IS;
import static de.calamanari.adl.FormatConstants.T_NOT;
import static de.calamanari.adl.FormatConstants.T_OF;
import static de.calamanari.adl.FormatConstants.T_STRICT;
import static de.calamanari.adl.FormatConstants.T_UNKNOWN;
import static de.calamanari.adl.FormatUtils.appendIndent;
import static de.calamanari.adl.FormatUtils.appendIndentOrWhitespace;
import static de.calamanari.adl.FormatUtils.closeBrace;
import static de.calamanari.adl.FormatUtils.comma;
import static de.calamanari.adl.FormatUtils.endsWith;
import static de.calamanari.adl.FormatUtils.newLine;
import static de.calamanari.adl.FormatUtils.openBrace;
import static de.calamanari.adl.FormatUtils.space;
import static de.calamanari.adl.FormatUtils.stripTrailingWhitespace;
import static de.calamanari.adl.erl.CommentUtils.appendComments;
import static de.calamanari.adl.erl.CommentUtils.appendCommentsOrWhitespace;
import static de.calamanari.adl.erl.PlComment.Position.AFTER_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.C1;
import static de.calamanari.adl.erl.PlComment.Position.C2;
import static de.calamanari.adl.erl.PlComment.Position.C3;
import static de.calamanari.adl.erl.PlComment.Position.C4;
import static de.calamanari.adl.erl.PlComment.Position.C5;
import static de.calamanari.adl.erl.PlComment.Position.C6;

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
import de.calamanari.adl.erl.PlComment.Position;
import de.calamanari.adl.util.AdlTextUtils;

/**
 * Presentation layer expression that matches a given argument against a plain value or an argument reference resp. a list of arguments.
 * <p>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#3-basic-expressions">§3</a> Audlang
 * Spec
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public record PlMatchExpression(String argName, PlMatchOperator operator, @JsonInclude(JsonInclude.Include.NON_EMPTY) List<PlOperand> operands,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<PlComment> comments, String inline) implements PlExpression<PlMatchExpression> {

    private static final String EMPTY_PREFIX = "";
    private static final String STRICT_PREFIX = T_STRICT;

    /**
     * @param argName attribute name
     * @param operator for matching the argument against the operand(s)
     * @param operands list can be single-value, multi-value or empty depending on the given operator
     * @param comments optional list with comments on expression level, can be null
     * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
     */
    public PlMatchExpression(String argName, PlMatchOperator operator, List<PlOperand> operands, List<PlComment> comments,
            @SuppressWarnings("java:S1172") String inline) {
        if (argName == null || argName.isEmpty()) {
            throw new AudlangValidationException(String.format("argName must not be null or empty, given: argName=%s, operator=%s, operand=%s, comments=%s",
                    argName, operator, operands, comments));
        }

        List<PlOperand> operandsTemp = (operands == null ? new ArrayList<>() : new ArrayList<>(operands));

        if (operator == null) {
            throw new AudlangValidationException(
                    String.format("Operator must not be null, given: argName=%s, operator=%s, operand=%s, comments=%s", argName, operator, operands, comments));
        }
        if (!operator.isCompatibleWithOperands(operandsTemp)) {
            throw new AudlangValidationException(
                    String.format("The operator is not applicable to the operand(s), given: argName=%s, operator=%s, operand=%s, comments=%s. %s", argName,
                            operator, operands, comments, operator.operandConstraint.message));

        }

        List<PlComment> commentsTemp = (comments == null ? new ArrayList<>() : new ArrayList<>(comments));
        if (!operator.isCompatibleWithComments(commentsTemp)) {
            throw new AudlangValidationException(
                    String.format("The given comments are not applicable (invalid positions), given: argName=%s, operator=%s, operand=%s, comments=%s. %s",
                            argName, operator, operands, comments, operator.commentConstraint.message));

        }

        operator.moveIndistinguishableTrailingOperandCommentsAfterExpression(commentsTemp, operandsTemp);

        this.argName = argName;
        this.operator = operator;
        this.operands = Collections.unmodifiableList(operandsTemp);
        this.comments = Collections.unmodifiableList(commentsTemp);
        this.inline = format(FormatStyle.INLINE);
    }

    /**
     * @param argName attribute name
     * @param operator for matching the argument against the operand(s)
     * @param operands list can be single-value, multi-value or empty depending on the given operator
     * @param comments optional list with comments on expression level, can be null
     */
    public PlMatchExpression(String argName, PlMatchOperator operator, List<PlOperand> operands, List<PlComment> comments) {
        this(argName, operator, operands, comments, null);
    }

    /**
     * convenience constructor
     * 
     * @param argName attribute name
     * @param operator for matching the argument against the operand(s)
     * @param operand the single operand will be wrapped in a list
     * @param comments optional list with comments on expression level, can be null
     */
    public PlMatchExpression(String argName, PlMatchOperator operator, PlOperand operand, List<PlComment> comments) {
        this(argName, operator, Arrays.asList(operand), comments, null);
    }

    /**
     * convenience constructor (no operand)
     * 
     * @param argName attribute name
     * @param operator for matching the argument against the operand(s)
     * @param comments optional list with comments on expression level, can be null
     */
    public PlMatchExpression(String argName, PlMatchOperator operator, List<PlComment> comments) {
        this(argName, operator, Collections.emptyList(), comments, null);
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
    public void collectFieldsInternal(Map<String, AudlangField.Builder> fieldMap) {

        // ensure the field is listed (even for is [not] unknown
        AudlangField.Builder field = fieldMap.computeIfAbsent(argName, AudlangField::forField);

        operands.forEach(operand -> collectFieldOperand(field, operand, fieldMap));
    }

    /**
     * Add the value(s) of the operand to the argName's field and (for referenced attributes) vice-versa
     * 
     * @param field of the argName in the fieldMap
     * @param operand to be collected
     * @param fieldMap maps all the fields to argNames
     */
    private void collectFieldOperand(AudlangField.Builder field, PlOperand operand, Map<String, AudlangField.Builder> fieldMap) {
        if (operand.isReference()) {
            field.addRefArgName(operand.value());
            AudlangField.Builder viceVersaField = fieldMap.computeIfAbsent(operand.value(), AudlangField::forField);
            viceVersaField.addRefArgName(argName);
        }
        else {
            field.addValue(operand.value());
        }
    }

    @Override
    public String toString() {
        return inline;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PlMatchExpression plm && plm.inline.equals(this.inline);
    }

    @Override
    public int hashCode() {
        return inline.hashCode();
    }

    @Override
    public void collectAllComments(List<PlComment> result) {
        result.addAll(comments);
        operands.forEach(operand -> operand.collectAllComments(result));
    }

    @Override
    public void appendSingleLine(StringBuilder sb, FormatStyle style, int level) {
        operator.formatAndAppend(sb, argName, operands, comments, style, level, true);
    }

    @Override
    public void appendMultiLine(StringBuilder sb, FormatStyle style, int level) {
        operator.formatAndAppend(sb, argName, operands, comments, style, level, false);
    }

    @Override
    public PlMatchExpression stripComments() {
        List<PlOperand> updatedOperands = operands.stream().map(PlOperand::stripComments).toList();

        if (updatedOperands.equals(operands) && comments.isEmpty()) {
            return this;
        }
        else {
            return new PlMatchExpression(argName, operator, updatedOperands, null);
        }
    }

    @Override
    public PlMatchExpression withComments(List<PlComment> comments) {
        if ((comments == null && this.comments.isEmpty()) || this.comments.equals(comments)) {
            return this;
        }
        else {
            return new PlMatchExpression(argName, operator, operands, comments);
        }
    }

    @Override
    public boolean shouldUseMultiLineFormatting(FormatStyle style) {
        return ((style.isMultiLine() && operands.size() >= 5) || operands.stream().anyMatch(operand -> operand.shouldUseMultiLineFormatting(style)))
                || (style.isMultiLine() && comments.stream().anyMatch(PlComment::isComplex));
    }

    /**
     * This method returns the lazy not resolution, also considering references
     * 
     * @return resolved non-strict not on an equals
     */
    private PlCombinedExpression resolveNonStrictNotEquals() {
        PlOperand operand = operands.get(0);
        List<PlExpression<?>> orConditions = new ArrayList<>();
        orConditions.add(new PlNegationExpression(new PlMatchExpression(argName, PlMatchOperator.EQUALS, operands, null).stripComments(), true, null));
        orConditions.add(new PlMatchExpression(argName, PlMatchOperator.IS_UNKNOWN, null));
        if (operand.isReference()) {
            orConditions.add(new PlMatchExpression(operand.value(), PlMatchOperator.IS_UNKNOWN, null));
        }
        return new PlCombinedExpression(CombinedExpressionType.OR, orConditions, null);
    }

    @Override
    public PlExpression<?> resolveHigherLanguageFeatures() {
        PlExpression<?> res = null;
        switch (this.operator) {
        case NOT_EQUALS:
            res = resolveNonStrictNotEquals();
            break;
        case NOT_CONTAINS:
            res = new PlCombinedExpression(CombinedExpressionType.OR,
                    Arrays.asList(
                            new PlNegationExpression(new PlMatchExpression(argName, PlMatchOperator.CONTAINS, operands, null).stripComments(), true, null),
                            new PlMatchExpression(argName, PlMatchOperator.IS_UNKNOWN, null)),
                    null);
            break;
        case LESS_THAN_OR_EQUALS:
            res = new PlCombinedExpression(CombinedExpressionType.OR,
                    Arrays.asList(new PlMatchExpression(argName, PlMatchOperator.LESS_THAN, operands, null).stripComments(),
                            new PlMatchExpression(argName, PlMatchOperator.EQUALS, operands, null).stripComments()),
                    null);
            break;
        case GREATER_THAN_OR_EQUALS:
            res = new PlCombinedExpression(CombinedExpressionType.OR,
                    Arrays.asList(new PlMatchExpression(argName, PlMatchOperator.GREATER_THAN, operands, null).stripComments(),
                            new PlMatchExpression(argName, PlMatchOperator.EQUALS, operands, null).stripComments()),
                    null);
            break;
        case BETWEEN:
            res = new PlCombinedExpression(CombinedExpressionType.AND, createResolvedMemberList(), null);
            break;
        case NOT_BETWEEN:
            res = new PlCombinedExpression(CombinedExpressionType.OR, createResolvedMemberList(), null);
            break;
        case STRICT_NOT_BETWEEN:
            res = new PlCombinedExpression(CombinedExpressionType.OR, createResolvedMemberList(), null);
            break;
        case ANY_OF, CONTAINS_ANY_OF:
            res = createCombinedExpressionIfRequired(CombinedExpressionType.OR, createResolvedMemberList());
            break;
        case NOT_ANY_OF, NOT_CONTAINS_ANY_OF:
            res = createCombinedExpressionIfRequired(CombinedExpressionType.AND, createResolvedMemberList());
            break;
        case STRICT_NOT_ANY_OF, STRICT_NOT_CONTAINS_ANY_OF:
            res = createCombinedExpressionIfRequired(CombinedExpressionType.AND, createResolvedMemberList());
            break;
        case STRICT_NOT_EQUALS:
            res = new PlNegationExpression(new PlMatchExpression(argName, PlMatchOperator.EQUALS, operands, null).stripComments(), true, null);
            break;
        case STRICT_NOT_CONTAINS:
            res = new PlNegationExpression(new PlMatchExpression(argName, PlMatchOperator.CONTAINS, operands, null).stripComments(), true, null);
            break;
        case IS_NOT_UNKNOWN:
            res = new PlNegationExpression(new PlMatchExpression(argName, PlMatchOperator.IS_UNKNOWN, null), true, null);
            break;
        // $CASES-OMITTED$
        default:
            res = this.stripComments();
        }
        if (res.equals(this)) {
            res = this;
        }
        return res;
    }

    /**
     * @param combiType
     * @param members
     * @return combined expression of the requested type or single member if the list only contained one element
     */
    private static PlExpression<?> createCombinedExpressionIfRequired(CombinedExpressionType combiType, List<PlExpression<?>> members) {
        if (members.size() > 1) {
            return new PlCombinedExpression(combiType, members, null);
        }
        else {
            return members.get(0);
        }
    }

    /**
     * This method enlists operands as separate expressions (e.g. arg ANY OF (a, b, c) => [arg=a , arg=b , arg=c]
     * 
     * @return list of expressions for list operands
     */
    private List<PlExpression<?>> createResolvedMemberList() {

        List<PlExpression<?>> res = new ArrayList<>();

        switch (this.operator) {
        case BETWEEN:
            res.addAll(
                    Arrays.asList(new PlMatchExpression(argName, PlMatchOperator.GREATER_THAN_OR_EQUALS, operands.get(0), null).resolveHigherLanguageFeatures(),
                            new PlMatchExpression(argName, PlMatchOperator.LESS_THAN_OR_EQUALS, operands.get(1), null).resolveHigherLanguageFeatures()));
            break;
        case NOT_BETWEEN, STRICT_NOT_BETWEEN:
            res.addAll(Arrays.asList(
                    new PlNegationExpression(new PlMatchExpression(argName, PlMatchOperator.GREATER_THAN_OR_EQUALS, operands.get(0), null), true, null)
                            .resolveHigherLanguageFeatures(),
                    new PlNegationExpression(new PlMatchExpression(argName, PlMatchOperator.LESS_THAN_OR_EQUALS, operands.get(1), null), true, null)
                            .resolveHigherLanguageFeatures()));
            if (this.operator == PlMatchOperator.NOT_BETWEEN) {
                res.add(new PlMatchExpression(argName, PlMatchOperator.IS_UNKNOWN, null));
            }
            break;
        case ANY_OF:
            res.addAll(operands.stream().map(op -> new PlMatchExpression(argName, PlMatchOperator.EQUALS, op, null).resolveHigherLanguageFeatures()).toList());
            break;
        case NOT_ANY_OF:
            res.addAll(
                    operands.stream().map(op -> new PlMatchExpression(argName, PlMatchOperator.NOT_EQUALS, op, null).resolveHigherLanguageFeatures()).toList());
            break;
        case STRICT_NOT_ANY_OF:
            res.addAll(operands.stream().map(op -> new PlMatchExpression(argName, PlMatchOperator.STRICT_NOT_EQUALS, op, null).resolveHigherLanguageFeatures())
                    .toList());
            break;
        case CONTAINS_ANY_OF:
            res.addAll(
                    operands.stream().map(op -> new PlMatchExpression(argName, PlMatchOperator.CONTAINS, op, null).resolveHigherLanguageFeatures()).toList());
            break;
        case NOT_CONTAINS_ANY_OF:
            res.addAll(operands.stream().map(op -> new PlMatchExpression(argName, PlMatchOperator.NOT_CONTAINS, op, null).resolveHigherLanguageFeatures())
                    .toList());
            break;
        case STRICT_NOT_CONTAINS_ANY_OF:
            res.addAll(operands.stream()
                    .map(op -> new PlMatchExpression(argName, PlMatchOperator.STRICT_NOT_CONTAINS, op, null).resolveHigherLanguageFeatures()).toList());
            break;
        // $CASES-OMITTED$
        default:
            throw new IllegalStateException("This method is only applicable to certain operators, given: " + this.operator);
        }
        return res;
    }

    /**
     * Available basic audience
     */
    public enum PlMatchOperator {

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#31-equals">§3.1</a> Audlang Spec
         */
        EQUALS(tokens(EMPTY_PREFIX, "="), OperandConstraint.ONE_VALUE_OR_ARG_REF, CommentConstraint.ONE_INTERNAL_COMMENT),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#32-not-equals">§3.2</a>,
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#51-default-negation">§5.1</a>
         * Audlang Spec
         */
        NOT_EQUALS(tokens(EMPTY_PREFIX, "!="), OperandConstraint.ONE_VALUE_OR_ARG_REF, CommentConstraint.ONE_INTERNAL_COMMENT),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#32-not-equals">§3.2</a>,
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#52-strict-negation">§5.2</a> Audlang
         * Spec
         */
        STRICT_NOT_EQUALS(tokens(STRICT_PREFIX, "!="), OperandConstraint.ONE_VALUE_OR_ARG_REF, CommentConstraint.TWO_INTERNAL_COMMENTS),

        /**
         * <a href=
         * "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#33-less-than-and-greater-than">§3.3</a>
         * Audlang Spec
         */
        GREATER_THAN_OR_EQUALS(tokens(EMPTY_PREFIX, ">="), OperandConstraint.ONE_VALUE_OR_ARG_REF, CommentConstraint.ONE_INTERNAL_COMMENT),

        /**
         * <a href=
         * "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#33-less-than-and-greater-than">§3.3</a>
         * Audlang Spec
         */
        GREATER_THAN(tokens(EMPTY_PREFIX, ">"), OperandConstraint.ONE_VALUE_OR_ARG_REF, CommentConstraint.ONE_INTERNAL_COMMENT),

        /**
         * <a href=
         * "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#33-less-than-and-greater-than">§3.3</a>
         * Audlang Spec
         */
        LESS_THAN(tokens(EMPTY_PREFIX, "<"), OperandConstraint.ONE_VALUE_OR_ARG_REF, CommentConstraint.ONE_INTERNAL_COMMENT),

        /**
         * <a href=
         * "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#33-less-than-and-greater-than">§3.3</a>
         * Audlang Spec
         */
        LESS_THAN_OR_EQUALS(tokens(EMPTY_PREFIX, "<="), OperandConstraint.ONE_VALUE_OR_ARG_REF, CommentConstraint.ONE_INTERNAL_COMMENT),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#34-between">§3.4</a> Audlang Spec
         */
        BETWEEN(tokens(EMPTY_PREFIX, T_BETWEEN), OperandConstraint.TWO_VALUES, CommentConstraint.TWO_INTERNAL_COMMENTS),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#34-between">§3.4</a>,
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#51-default-negation">§5.1</a>
         * Audlang Spec
         */
        NOT_BETWEEN(tokens(EMPTY_PREFIX, T_NOT, T_BETWEEN), OperandConstraint.TWO_VALUES, CommentConstraint.THREE_INTERNAL_COMMENTS),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#34-between">§3.4</a>,
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#52-strict-negation">§5.2</a> Audlang
         * Spec
         */
        STRICT_NOT_BETWEEN(tokens(EMPTY_PREFIX, T_STRICT, T_NOT, T_BETWEEN), OperandConstraint.TWO_VALUES, CommentConstraint.FOUR_INTERNAL_COMMENTS),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#35-any-of">§3.5</a> Audlang Spec
         */
        ANY_OF(tokens(EMPTY_PREFIX, T_ANY, T_OF), OperandConstraint.AT_LEAST_ONE_VALUE_OR_ARG_REF, CommentConstraint.THREE_INTERNAL_COMMENTS),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#35-any-of">§3.5</a>,
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#51-default-negation">§5.1</a>
         * Audlang Spec
         */
        NOT_ANY_OF(tokens(EMPTY_PREFIX, T_NOT, T_ANY, T_OF), OperandConstraint.AT_LEAST_ONE_VALUE_OR_ARG_REF, CommentConstraint.FOUR_INTERNAL_COMMENTS),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#35-any-of">§3.5</a>,
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#52-strict-negation">§5.2</a> Audlang
         * Spec
         */
        STRICT_NOT_ANY_OF(
                tokens(EMPTY_PREFIX, T_STRICT, T_NOT, T_ANY, T_OF),
                OperandConstraint.AT_LEAST_ONE_VALUE_OR_ARG_REF,
                CommentConstraint.FIVE_INTERNAL_COMMENTS),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#36-contains-text-snippet">§3.6</a>
         * Audlang Spec
         */
        CONTAINS(tokens(EMPTY_PREFIX, T_CONTAINS), OperandConstraint.ONE_VALUE, CommentConstraint.ONE_INTERNAL_COMMENT),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#36-contains-text-snippet">§3.6</a>,
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#51-default-negation">§5.1</a>
         * Audlang Spec
         */
        NOT_CONTAINS(tokens(EMPTY_PREFIX, T_NOT, T_CONTAINS), OperandConstraint.ONE_VALUE, CommentConstraint.TWO_INTERNAL_COMMENTS),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#36-contains-text-snippet">§3.6</a>,
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#52-strict-negation">§5.2</a> Audlang
         * Spec
         */
        STRICT_NOT_CONTAINS(tokens(EMPTY_PREFIX, T_STRICT, T_NOT, T_CONTAINS), OperandConstraint.ONE_VALUE, CommentConstraint.THREE_INTERNAL_COMMENTS),

        /**
         * <a href=
         * "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#37-contains-any-of-text-snippet-list">§3.7</a>
         * Audlang Spec
         */
        CONTAINS_ANY_OF(tokens(EMPTY_PREFIX, T_CONTAINS, T_ANY, T_OF), OperandConstraint.AT_LEAST_ONE_VALUE, CommentConstraint.FOUR_INTERNAL_COMMENTS),

        /**
         * <a href=
         * "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#37-contains-any-of-text-snippet-list">§3.7</a>,
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#51-default-negation">§5.1</a>
         * Audlang Spec
         */
        NOT_CONTAINS_ANY_OF(
                tokens(EMPTY_PREFIX, T_NOT, T_CONTAINS, T_ANY, T_OF),
                OperandConstraint.AT_LEAST_ONE_VALUE,
                CommentConstraint.FIVE_INTERNAL_COMMENTS),

        /**
         * <a href=
         * "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#37-contains-any-of-text-snippet-list">§3.7</a>,
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#52-strict-negation">§5.2</a> Audlang
         * Spec
         */
        STRICT_NOT_CONTAINS_ANY_OF(
                tokens(EMPTY_PREFIX, T_STRICT, T_NOT, T_CONTAINS, T_ANY, T_OF),
                OperandConstraint.AT_LEAST_ONE_VALUE,
                CommentConstraint.SIX_INTERNAL_COMMENTS),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#38-is-not-unknown">§3.8</a> Audlang
         * Spec
         */
        IS_UNKNOWN(tokens(EMPTY_PREFIX, T_IS, T_UNKNOWN), OperandConstraint.NONE, CommentConstraint.TWO_INTERNAL_COMMENTS),

        /**
         * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#38-is-not-unknown">§3.8</a> Audlang
         * Spec
         */
        IS_NOT_UNKNOWN(tokens(EMPTY_PREFIX, T_IS, T_NOT, T_UNKNOWN), OperandConstraint.NONE, CommentConstraint.THREE_INTERNAL_COMMENTS);

        /**
         * This is for validation to avoid creating invalid expressions
         */
        private final OperandConstraint operandConstraint;

        /**
         * This is for validation to avoid creating invalid expressions
         */
        private final CommentConstraint commentConstraint;

        /**
         * The operator between the argument name and the operand as a list of tokens with optional comments in between, the first one is the prefix-token,
         * usually "", only relevant for "STRICT" not-equal
         */
        private final String[] operatorTokens;

        PlMatchOperator(String[] operatorTokens, OperandConstraint constraint, CommentConstraint commentConstraint) {
            this.operatorTokens = operatorTokens;
            this.operandConstraint = constraint;
            this.commentConstraint = commentConstraint;
        }

        /**
         * Appends the full expression character sequence including argument name, operator, operands and optional comments
         * 
         * @param sb destination
         * @param argName
         * @param operands
         * @param comments
         * @param style
         * @param level
         * @param forceSingleLine enforces output on a single line
         */
        void formatAndAppend(StringBuilder sb, String argName, List<PlOperand> operands, List<PlComment> comments, FormatStyle style, int level,
                boolean forceSingleLine) {

            if (appendComments(sb, comments, BEFORE_EXPRESSION, style, level, forceSingleLine)) {
                appendIndentOrWhitespace(sb, style, level);
            }

            int slot = 1;
            if (!operatorTokens[0].isEmpty()) {
                // handle prefix (strict not equals case)
                sb.append(operatorTokens[0]);
                appendCommentsOrWhitespace(sb, comments, mapSlotToCommentPosition(slot), style, level, forceSingleLine);
                appendIndentOrWhitespace(sb, style, level);
                slot++;
            }
            sb.append(AdlTextUtils.addDoubleQuotesIfRequired(AdlTextUtils.escapeSpecialCharacters(argName)));

            appendCommentsOrWhitespace(sb, comments, mapSlotToCommentPosition(slot), style, level, forceSingleLine);
            slot++;

            appendIndentOrWhitespace(sb, style, level);
            appendOperatorString(sb, comments, slot, style, level, forceSingleLine);

            if (!operands.isEmpty()) {
                if (forceSingleLine) {
                    appendOperandsSingleLine(sb, operands, style, level);
                }
                else {
                    appendOperandsMultiLine(sb, operands, style, level);
                }
            }
            else if (endsWith(sb, " ") && !endsWith(sb, "/ ")) {
                sb.setLength(sb.length() - 1);
            }
            appendComments(sb, comments, AFTER_EXPRESSION, style, level, forceSingleLine);
        }

        private boolean shouldUseMultiLineFormattingForOperandList(List<PlOperand> operands, FormatStyle style) {
            return style.isMultiLine() && (operands.size() >= 5 || operands.stream().anyMatch(operand -> operand.shouldUseMultiLineFormatting(style)));
        }

        private void appendOperandsMultiLine(StringBuilder sb, List<PlOperand> operands, FormatStyle style, int level) {
            if (!shouldUseMultiLineFormattingForOperandList(operands, style)) {
                appendOperandsSingleLine(sb, operands, style, level);
            }
            else {
                appendOperandsMultiLineInternal(sb, operands, style, level);
            }
            stripTrailingWhitespace(sb);
        }

        private void appendOperandsMultiLineInternal(StringBuilder sb, List<PlOperand> operands, FormatStyle style, int level) {
            int subLevel = level + 1;
            if (operands.size() > 1) {
                subLevel++;
            }

            if (operandConstraint.requiresListOperand) {
                openBrace(sb);
                newLine(sb);
            }
            for (int i = 0; i < operands.size(); i++) {

                if (i > 0) {
                    comma(sb);
                    newLine(sb);
                }
                if (operandConstraint.requiresListOperand) {
                    appendIndent(sb, style, subLevel);
                }
                operands.get(i).appendMultiLine(sb, style, subLevel);
            }
            newLine(sb);
            if (operandConstraint.requiresListOperand) {
                appendIndent(sb, style, level + 1);
                closeBrace(sb);
            }
        }

        private void appendOperandsSingleLine(StringBuilder sb, List<PlOperand> operands, FormatStyle style, int level) {

            if (operandConstraint.requiresListOperand) {
                openBrace(sb);
            }
            for (int i = 0; i < operands.size(); i++) {
                if (i > 0) {
                    comma(sb);
                    space(sb);
                }
                operands.get(i).appendSingleLine(sb, style, level);
            }
            if (endsWith(sb, "*/")) {
                space(sb);
            }
            if (operandConstraint.requiresListOperand) {
                closeBrace(sb);
            }
        }

        /**
         * @return the list of the operator tokens
         */
        public List<String> getOperatorTokens() {
            return Collections.unmodifiableList(Arrays.asList(operatorTokens));
        }

        /**
         * Constructs the full operator string from the tokens with optional comments in between
         * 
         * @param sb
         * @param comments
         * @param slot
         * @param style
         * @param level
         * @param forceSingleLine
         */
        private void appendOperatorString(StringBuilder sb, List<PlComment> comments, int slot, FormatStyle style, int level, boolean forceSingleLine) {
            for (int i = 1; i < operatorTokens.length; i++) {
                sb.append(operatorTokens[i]);
                appendCommentsOrWhitespace(sb, comments, mapSlotToCommentPosition(slot), style, level, forceSingleLine);
                slot++;
                appendIndentOrWhitespace(sb, style, level);
            }
        }

        /**
         * If a match expression has a single value or reference operand, we cannot distinguish trailing operand comments from trailing expression comments.
         * <br>
         * This method moves the comments from after the operand after the expression to standardize behavior.
         * 
         * @param parentComments
         * @param operands
         */
        void moveIndistinguishableTrailingOperandCommentsAfterExpression(List<PlComment> parentComments, List<PlOperand> operands) {
            if (operands.size() == 1 && this.operandConstraint == OperandConstraint.ONE_VALUE
                    || this.operandConstraint == OperandConstraint.ONE_VALUE_OR_ARG_REF) {
                PlOperand operand = operands.get(0);
                List<PlComment> operandComments = new ArrayList<>(operand.comments());
                if (moveCommentsInternal(parentComments, operandComments)) {
                    operands.set(0, operand.withComments(operandComments));
                }
            }
        }

        private boolean moveCommentsInternal(List<PlComment> parentComments, List<PlComment> operandComments) {
            boolean modified = false;
            for (int i = operandComments.size() - 1; i > -1; i--) {
                PlComment operandComment = operandComments.get(i);
                if (operandComment.position() == Position.AFTER_OPERAND) {
                    operandComments.remove(i);
                    parentComments.add(0, new PlComment(operandComment.comment(), AFTER_EXPRESSION));
                    modified = true;
                }
            }
            return modified;
        }

        /**
         * @param comments
         * @return true if the given comments are compatible with the operator
         */
        boolean isCompatibleWithComments(List<PlComment> comments) {
            if (comments.isEmpty()) {
                return true;
            }
            return comments.stream().allMatch(commentConstraint::verify);
        }

        /**
         * Maps the given slot (internal index) to any of the comment positions
         * 
         * @param slot where we are in the output
         * @return comment position for filtering
         */
        static PlComment.Position mapSlotToCommentPosition(int slot) {

            if (slot < 1 || slot > 6) {
                throw new AudlangValidationException(String.format("%s is not a valid internal token slot", slot));
            }

            return PlComment.Position.values()[slot];
        }

        /**
         * @param operand
         * @return true if the given operand is compatible with this operator
         */
        boolean isCompatibleWithOperands(List<PlOperand> operands) {
            switch (this.operandConstraint) {
            case NONE:
                return operands.isEmpty();
            case ONE_VALUE:
                return operands.size() == 1 && !operands.get(0).isReference();
            case ONE_VALUE_OR_ARG_REF:
                return operands.size() == 1;
            case AT_LEAST_ONE_VALUE:
                return !operands.isEmpty() && operands.stream().noneMatch(PlOperand::isReference);
            case AT_LEAST_ONE_VALUE_OR_ARG_REF:
                return !operands.isEmpty();
            case TWO_VALUES:
                return operands.size() == 2 && operands.stream().noneMatch(PlOperand::isReference);
            default:
                return false;
            }
        }

        /**
         * @return true if this operator implies a NOT
         */
        public boolean supportsIntraNegation() {
            return (this != LESS_THAN && this != LESS_THAN_OR_EQUALS && this != GREATER_THAN_OR_EQUALS && this != GREATER_THAN);
        }

        /**
         * Tells whether this is a strict negation operator
         * 
         * @return true if this operator is strict
         */
        public boolean isStrictMatchNegation() {
            return (this == STRICT_NOT_ANY_OF || this == STRICT_NOT_BETWEEN || this == STRICT_NOT_CONTAINS || this == STRICT_NOT_CONTAINS_ANY_OF
                    || this == STRICT_NOT_EQUALS);
        }

        /**
         * Tells whether this is a negation operator (does not cover IS NOT UNKNOWN)
         * 
         * @return true if this operator is a negation
         */
        public boolean isMatchNegation() {
            return (this == STRICT_NOT_ANY_OF || this == STRICT_NOT_BETWEEN || this == STRICT_NOT_CONTAINS || this == STRICT_NOT_CONTAINS_ANY_OF
                    || this == STRICT_NOT_EQUALS || this == NOT_ANY_OF || this == NOT_BETWEEN || this == NOT_CONTAINS || this == NOT_CONTAINS_ANY_OF
                    || this == NOT_EQUALS);
        }

        private static String[] tokens(String... tokens) {
            return tokens;
        }

    }

    /**
     * Operator constraints for validation
     */
    private enum OperandConstraint {

        NONE(false, "Operator does not take any operand (unary operation)."),
        ONE_VALUE(false, "Operator expects exactly one value argument, no argument references allowed."),
        ONE_VALUE_OR_ARG_REF(false, "Operator expects exactly one value or one argument reference."),
        TWO_VALUES(true, "Operator expects exactly two values, no argument references allowed."),
        AT_LEAST_ONE_VALUE(true, "Operator expects one or more values, no argument references allowed."),
        AT_LEAST_ONE_VALUE_OR_ARG_REF(true, "Operator expects one or multiple value(s) or argument reference(s).");

        private final String message;

        private final boolean requiresListOperand;

        private OperandConstraint(boolean requiresListOperand, String message) {
            this.message = message;
            this.requiresListOperand = requiresListOperand;
        }

    }

    /**
     * Comments can only appear between certain tokens of the Audlang, and not every operator has the same number of tokens so that the number of available
     * slots to place a comment depends on the operator. This is covered by the constraint.
     */
    private enum CommentConstraint {

        ONE_INTERNAL_COMMENT(C1),
        TWO_INTERNAL_COMMENTS(C1, C2),
        THREE_INTERNAL_COMMENTS(C1, C2, C3),
        FOUR_INTERNAL_COMMENTS(C1, C2, C3, C4),
        FIVE_INTERNAL_COMMENTS(C1, C2, C3, C4, C5),
        SIX_INTERNAL_COMMENTS(C1, C2, C3, C4, C5, C6);

        private final Set<PlComment.Position> validPositions;

        private final String message;

        private CommentConstraint(PlComment.Position... validPositions) {
            LinkedHashSet<PlComment.Position> set = new LinkedHashSet<>();
            set.add(BEFORE_EXPRESSION);
            set.addAll(Arrays.asList(validPositions));
            set.add(AFTER_EXPRESSION);
            this.validPositions = Collections.unmodifiableSet(set);
            this.message = String.format("Only the following comment positions are valid: %s", this.validPositions);
        }

        private boolean verify(PlComment comment) {
            return validPositions.contains(comment.position());
        }
    }

}
