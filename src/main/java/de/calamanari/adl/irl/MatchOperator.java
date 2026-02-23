//@formatter:off
/*
 * MatchOperator
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

package de.calamanari.adl.irl;

import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.util.AdlTextUtils;

import static de.calamanari.adl.FormatUtils.endsWith;
import static de.calamanari.adl.FormatUtils.space;

/**
 * The {@link MatchOperator} enumeration covers the simple compare operations of {@link CoreExpression}s.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public enum MatchOperator {

    /**
     * <a href= "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#33-less-than-and-greater-than">§3.3</a>
     * Audlang Spec
     */
    LESS_THAN("<", OperandConstraint.ONE_VALUE_OR_ARG_REF),

    /**
     * <a href= "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#33-less-than-and-greater-than">§3.3</a>
     * Audlang Spec
     */
    GREATER_THAN(">", OperandConstraint.ONE_VALUE_OR_ARG_REF),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#31-equals">§3.1</a> Audlang Spec
     */
    EQUALS("=", OperandConstraint.ONE_VALUE_OR_ARG_REF),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#36-contains-text-snippet">§3.6</a>
     * Audlang Spec
     */
    CONTAINS("CONTAINS", OperandConstraint.ONE_VALUE),

    /**
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#38-is-not-unknown">§3.8</a> Audlang Spec
     */
    IS_UNKNOWN("IS UNKNOWN", OperandConstraint.NONE);

    private final OperandConstraint operandConstraint;

    private final String operatorString;

    private MatchOperator(String operatorString, OperandConstraint operandConstraint) {
        this.operatorString = operatorString;
        this.operandConstraint = operandConstraint;
    }

    /**
     * @return the constraint associated with this operator
     */
    public OperandConstraint getOperandConstraint() {
        return operandConstraint;
    }

    /**
     * @param operand
     * @return true if the given operand is compatible with this operator
     */
    public boolean isCompatibleWithOperand(Operand operand) {
        switch (this.operandConstraint) {
        case NONE:
            return (operand == null);
        case ONE_VALUE:
            return operand != null && !operand.isReference();
        case ONE_VALUE_OR_ARG_REF:
            return (operand != null);
        default:
            return false;
        }

    }

    /**
     * During expression formatting appends this operator with its operand.
     * 
     * @param sb
     * @param argName
     * @param operand
     * @param style
     * @param level
     */
    public void formatAndAppend(StringBuilder sb, String argName, Operand operand, FormatStyle style, int level) {
        sb.append(AdlTextUtils.addDoubleQuotesIfRequired(AdlTextUtils.escapeSpecialCharacters(argName)));
        space(sb);
        sb.append(operatorString);
        if (operand != null) {
            space(sb);
            operand.appendSingleLine(sb, style, level);
        }
        else if (endsWith(sb, " ") && !endsWith(sb, "/ ")) {
            sb.setLength(sb.length() - 1);
        }
    }

}
