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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.calamanari.adl.AudlangField;
import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.FormatStyle;

/**
 * Presentation layer expression that matches a given argument against a plain value or an argument reference resp. a list of arguments.
 * <p>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#3-basic-expressions">§3</a> Audlang
 * Spec
 * 
 * @param argName attribute name
 * @param operator for matching the argument against the operand(s)
 * @param operands list can be single-value, multi-value or empty depending on the given operator
 * @param comments optional list with comments on expression level, can be null
 * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public record PlMatchExpression(String argName, PlMatchOperator operator, @JsonInclude(JsonInclude.Include.NON_EMPTY) List<PlOperand> operands,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<PlComment> comments, String inline) implements PlExpression<PlMatchExpression> {

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
                            operator, operands, comments, operator.getOperandConstraint().getMessage()));

        }

        List<PlComment> commentsTemp = (comments == null ? new ArrayList<>() : new ArrayList<>(comments));
        if (!operator.isCompatibleWithComments(commentsTemp)) {
            throw new AudlangValidationException(
                    String.format("The given comments are not applicable (invalid positions), given: argName=%s, operator=%s, operand=%s, comments=%s. %s",
                            argName, operator, operands, comments, operator.getCommentConstraint().getMessage()));

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

}
