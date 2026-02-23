//@formatter:off
/*
 * PlMatchOperator
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

import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.erl.PlComment.Position;
import de.calamanari.adl.util.AdlTextUtils;

import static de.calamanari.adl.FormatConstants.EMPTY_PREFIX;
import static de.calamanari.adl.FormatConstants.STRICT_PREFIX;
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

/**
 * The {@link PlMatchOperator} enumeration covers the Audlang operators for {@link PlMatchExpression}s on the presentation layer.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public enum PlMatchOperator {

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#31-equals">§3.1</a> Audlang Spec
     */
    EQUALS(tokens(EMPTY_PREFIX, "="), PlOperandConstraint.ONE_VALUE_OR_ARG_REF, PlCommentConstraint.ONE_INTERNAL_COMMENT),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#32-not-equals">§3.2</a>,
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#51-default-negation">§5.1</a> Audlang
     * Spec
     */
    NOT_EQUALS(tokens(EMPTY_PREFIX, "!="), PlOperandConstraint.ONE_VALUE_OR_ARG_REF, PlCommentConstraint.ONE_INTERNAL_COMMENT),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#32-not-equals">§3.2</a>,
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#52-strict-negation">§5.2</a> Audlang
     * Spec
     */
    STRICT_NOT_EQUALS(tokens(STRICT_PREFIX, "!="), PlOperandConstraint.ONE_VALUE_OR_ARG_REF, PlCommentConstraint.TWO_INTERNAL_COMMENTS),

    /**
     * <a href= "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#33-less-than-and-greater-than">§3.3</a>
     * Audlang Spec
     */
    GREATER_THAN_OR_EQUALS(tokens(EMPTY_PREFIX, ">="), PlOperandConstraint.ONE_VALUE_OR_ARG_REF, PlCommentConstraint.ONE_INTERNAL_COMMENT),

    /**
     * <a href= "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#33-less-than-and-greater-than">§3.3</a>
     * Audlang Spec
     */
    GREATER_THAN(tokens(EMPTY_PREFIX, ">"), PlOperandConstraint.ONE_VALUE_OR_ARG_REF, PlCommentConstraint.ONE_INTERNAL_COMMENT),

    /**
     * <a href= "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#33-less-than-and-greater-than">§3.3</a>
     * Audlang Spec
     */
    LESS_THAN(tokens(EMPTY_PREFIX, "<"), PlOperandConstraint.ONE_VALUE_OR_ARG_REF, PlCommentConstraint.ONE_INTERNAL_COMMENT),

    /**
     * <a href= "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#33-less-than-and-greater-than">§3.3</a>
     * Audlang Spec
     */
    LESS_THAN_OR_EQUALS(tokens(EMPTY_PREFIX, "<="), PlOperandConstraint.ONE_VALUE_OR_ARG_REF, PlCommentConstraint.ONE_INTERNAL_COMMENT),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#34-between">§3.4</a> Audlang Spec
     */
    BETWEEN(tokens(EMPTY_PREFIX, T_BETWEEN), PlOperandConstraint.TWO_VALUES, PlCommentConstraint.TWO_INTERNAL_COMMENTS),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#34-between">§3.4</a>,
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#51-default-negation">§5.1</a> Audlang
     * Spec
     */
    NOT_BETWEEN(tokens(EMPTY_PREFIX, T_NOT, T_BETWEEN), PlOperandConstraint.TWO_VALUES, PlCommentConstraint.THREE_INTERNAL_COMMENTS),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#34-between">§3.4</a>,
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#52-strict-negation">§5.2</a> Audlang
     * Spec
     */
    STRICT_NOT_BETWEEN(tokens(EMPTY_PREFIX, T_STRICT, T_NOT, T_BETWEEN), PlOperandConstraint.TWO_VALUES, PlCommentConstraint.FOUR_INTERNAL_COMMENTS),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#35-any-of">§3.5</a> Audlang Spec
     */
    ANY_OF(tokens(EMPTY_PREFIX, T_ANY, T_OF), PlOperandConstraint.AT_LEAST_ONE_VALUE_OR_ARG_REF, PlCommentConstraint.THREE_INTERNAL_COMMENTS),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#35-any-of">§3.5</a>,
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#51-default-negation">§5.1</a> Audlang
     * Spec
     */
    NOT_ANY_OF(tokens(EMPTY_PREFIX, T_NOT, T_ANY, T_OF), PlOperandConstraint.AT_LEAST_ONE_VALUE_OR_ARG_REF, PlCommentConstraint.FOUR_INTERNAL_COMMENTS),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#35-any-of">§3.5</a>,
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#52-strict-negation">§5.2</a> Audlang
     * Spec
     */
    STRICT_NOT_ANY_OF(
            tokens(EMPTY_PREFIX, T_STRICT, T_NOT, T_ANY, T_OF),
            PlOperandConstraint.AT_LEAST_ONE_VALUE_OR_ARG_REF,
            PlCommentConstraint.FIVE_INTERNAL_COMMENTS),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#36-contains-text-snippet">§3.6</a>
     * Audlang Spec
     */
    CONTAINS(tokens(EMPTY_PREFIX, T_CONTAINS), PlOperandConstraint.ONE_VALUE, PlCommentConstraint.ONE_INTERNAL_COMMENT),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#36-contains-text-snippet">§3.6</a>,
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#51-default-negation">§5.1</a> Audlang
     * Spec
     */
    NOT_CONTAINS(tokens(EMPTY_PREFIX, T_NOT, T_CONTAINS), PlOperandConstraint.ONE_VALUE, PlCommentConstraint.TWO_INTERNAL_COMMENTS),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#36-contains-text-snippet">§3.6</a>,
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#52-strict-negation">§5.2</a> Audlang
     * Spec
     */
    STRICT_NOT_CONTAINS(tokens(EMPTY_PREFIX, T_STRICT, T_NOT, T_CONTAINS), PlOperandConstraint.ONE_VALUE, PlCommentConstraint.THREE_INTERNAL_COMMENTS),

    /**
     * <a href=
     * "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#37-contains-any-of-text-snippet-list">§3.7</a>
     * Audlang Spec
     */
    CONTAINS_ANY_OF(tokens(EMPTY_PREFIX, T_CONTAINS, T_ANY, T_OF), PlOperandConstraint.AT_LEAST_ONE_VALUE, PlCommentConstraint.FOUR_INTERNAL_COMMENTS),

    /**
     * <a href=
     * "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#37-contains-any-of-text-snippet-list">§3.7</a>,
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#51-default-negation">§5.1</a> Audlang
     * Spec
     */
    NOT_CONTAINS_ANY_OF(
            tokens(EMPTY_PREFIX, T_NOT, T_CONTAINS, T_ANY, T_OF),
            PlOperandConstraint.AT_LEAST_ONE_VALUE,
            PlCommentConstraint.FIVE_INTERNAL_COMMENTS),

    /**
     * <a href=
     * "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#37-contains-any-of-text-snippet-list">§3.7</a>,
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#52-strict-negation">§5.2</a> Audlang
     * Spec
     */
    STRICT_NOT_CONTAINS_ANY_OF(
            tokens(EMPTY_PREFIX, T_STRICT, T_NOT, T_CONTAINS, T_ANY, T_OF),
            PlOperandConstraint.AT_LEAST_ONE_VALUE,
            PlCommentConstraint.SIX_INTERNAL_COMMENTS),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#38-is-not-unknown">§3.8</a> Audlang Spec
     */
    IS_UNKNOWN(tokens(EMPTY_PREFIX, T_IS, T_UNKNOWN), PlOperandConstraint.NONE, PlCommentConstraint.TWO_INTERNAL_COMMENTS),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#38-is-not-unknown">§3.8</a> Audlang Spec
     */
    IS_NOT_UNKNOWN(tokens(EMPTY_PREFIX, T_IS, T_NOT, T_UNKNOWN), PlOperandConstraint.NONE, PlCommentConstraint.THREE_INTERNAL_COMMENTS);

    /**
     * This is for validation to avoid creating invalid expressions
     */
    private final PlOperandConstraint operandConstraint;

    /**
     * This is for validation to avoid creating invalid expressions
     */
    private final PlCommentConstraint commentConstraint;

    /**
     * The operator between the argument name and the operand as a list of tokens with optional comments in between, the first one is the prefix-token, usually
     * "", only relevant for "STRICT" not-equal
     */
    private final String[] operatorTokens;

    private PlMatchOperator(String[] operatorTokens, PlOperandConstraint constraint, PlCommentConstraint commentConstraint) {
        this.operatorTokens = operatorTokens;
        this.operandConstraint = constraint;
        this.commentConstraint = commentConstraint;
    }

    /**
     * @return the constraint related to possible operand(s) for this operator
     */
    public PlOperandConstraint getOperandConstraint() {
        return operandConstraint;
    }

    /**
     * @return the constraint related to placing comments along with this operator
     */
    public PlCommentConstraint getCommentConstraint() {
        return commentConstraint;
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
    public void formatAndAppend(StringBuilder sb, String argName, List<PlOperand> operands, List<PlComment> comments, FormatStyle style, int level,
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

        if (operandConstraint.isListOperandRequired()) {
            openBrace(sb);
            newLine(sb);
        }
        for (int i = 0; i < operands.size(); i++) {

            if (i > 0) {
                comma(sb);
                newLine(sb);
            }
            if (operandConstraint.isListOperandRequired()) {
                appendIndent(sb, style, subLevel);
            }
            operands.get(i).appendMultiLine(sb, style, subLevel);
        }
        newLine(sb);
        if (operandConstraint.isListOperandRequired()) {
            appendIndent(sb, style, level + 1);
            closeBrace(sb);
        }
    }

    private void appendOperandsSingleLine(StringBuilder sb, List<PlOperand> operands, FormatStyle style, int level) {

        if (operandConstraint.isListOperandRequired()) {
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
        if (operandConstraint.isListOperandRequired()) {
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
     * If a match expression has a single value or reference operand, we cannot distinguish trailing operand comments from trailing expression comments. <br>
     * This method moves the comments from after the operand after the expression to standardize behavior.
     * 
     * @param parentComments
     * @param operands
     */
    public void moveIndistinguishableTrailingOperandCommentsAfterExpression(List<PlComment> parentComments, List<PlOperand> operands) {
        if (operands.size() == 1 && this.operandConstraint == PlOperandConstraint.ONE_VALUE
                || this.operandConstraint == PlOperandConstraint.ONE_VALUE_OR_ARG_REF) {
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
    public boolean isCompatibleWithComments(List<PlComment> comments) {
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
     * @param operands
     * @return true if the given operands are compatible with this operator
     */
    public boolean isCompatibleWithOperands(List<PlOperand> operands) {
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
