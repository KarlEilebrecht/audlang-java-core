//@formatter:off
/*
 * MatchExpression
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.calamanari.adl.AudlangField;
import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.FormatStyle;

/**
 * Internal representation layer expression that matches a given argument against a plain value or an argument reference. Because all <i>syntactic sugar</i> has
 * been removed, you don't see here any list arguments but only single value/reference matches. Also any negation becomes a surrounding NOT (implicitly strict).
 * <p>
 * <b>Note:</b> To avoid ambiguity, {@link MatchException}s perform a few auto-adjustments on reference matches:
 * <ul>
 * <li><code>arg2 = &#64;arg1</code> =&gt; <code>arg1 = &#64;arg2</code></li>
 * <li><code>arg2 &lt; &#64;arg1</code> =&gt; <code>arg1 &gt; &#64;arg2</code></li>
 * <li><code>arg2 &gt; &#64;arg1</code> =&gt; <code>arg1 &lt; &#64;arg2</code></li>
 * </ul>
 * <p>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#3-basic-expressions">§3</a> Audlang
 * Spec
 * 
 * @param argName attribute name
 * @param operator for matching the argument against the operand
 * @param operand single operand or null (IS UNKNOWN case)
 * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public record MatchExpression(String argName, MatchOperator operator, @JsonInclude(JsonInclude.Include.NON_EMPTY) Operand operand, String inline)
        implements SimpleExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatchExpression.class);

    /**
     * @param argName attribute name
     * @param operator for matching the argument against the operand
     * @param operand single operand or null (IS UNKNOWN case)
     * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
     */
    public MatchExpression(String argName, MatchOperator operator, Operand operand, @SuppressWarnings("java:S1172") String inline) {
        if (argName == null || argName.isEmpty()) {
            throw new AudlangValidationException(
                    String.format("argName must not be null or empty, given: argName=%s, operator=%s, operand=%s", argName, operator, operand));
        }

        if (operator == null) {
            throw new AudlangValidationException(
                    String.format("Operator must not be null, given: argName=%s, operator=%s, operand=%s", argName, operator, operand));
        }
        if (!operator.isCompatibleWithOperand(operand)) {
            throw new AudlangValidationException(String.format("The operator is not applicable to the operand, given: argName=%s, operator=%s, operand=%s. %s",
                    argName, operator, operand, operator.getOperandConstraint().getMessage()));

        }
        if (operand != null && operand.isReference() && operand.value().equals(argName)) {
            throw new AudlangValidationException(
                    String.format("An argument cannot be matched against itself, given: argName=%s, operator=%s, operand=%s", argName, operator, operand));
        }
        if (operand != null && operator == MatchOperator.CONTAINS && operand.value().isEmpty()) {
            LOGGER.warn(
                    "Operator CONTAINS should not be used with empty operand (always true), given: argName={}, operator={}, operand={}. Better replace this match with {} IS NOT UNKNOWN",
                    argName, operator, operand, argName);
        }

        if (operand != null && operand.isReference()) {
            // Fix ambiguities:
            // arg2 = @arg1 => arg1 = @arg2
            // arg2 > @arg1 => arg1 < @arg2
            // arg2 < @arg1 => arg1 > @arg2

            String referencedArgName = operand.value();
            if (argName.compareTo(referencedArgName) > 0) {
                operand = Operand.of(argName, true);
                argName = referencedArgName;
                switch (operator) {
                case LESS_THAN:
                    operator = MatchOperator.GREATER_THAN;
                    break;
                case GREATER_THAN:
                    operator = MatchOperator.LESS_THAN;
                    break;
                // $CASES-OMITTED$
                default:
                }
            }
        }

        this.argName = argName;
        this.operator = operator;
        this.operand = operand;
        this.inline = format(FormatStyle.INLINE);
    }

    /**
     * Creates a match expression for the given argName
     * <p>
     * This is the preferred way to create a {@link MatchExpression}
     * 
     * @param argName
     * @param operator
     * @param operand null in case of IS UNKNOWN, see also {@link #isUnknown(String)} resp. {@link #isNotUnknown(String)}
     * @return expression
     */
    public static CoreExpression of(String argName, MatchOperator operator, Operand operand) {
        if (operand != null && operand.isReference() && operand.value().equals(argName)) {
            // handle illegal self-reference
            if (operator == MatchOperator.EQUALS) {
                // arg = @arg <=> arg IS NOT UNKNOWN
                return isNotUnknown(argName);
            }
            else {
                // arg < @arg can never be true under any circumstances
                return SpecialSetExpression.none();
            }
        }
        else if (operator == MatchOperator.CONTAINS && operand != null && operand.value().isEmpty()) {
            // arg CONTAINS "" <=> arg IS NOT UNKNOWN (empty string is part of every string)
            return isNotUnknown(argName);
        }
        else {
            return new MatchExpression(argName, operator, operand, null);
        }
    }

    /**
     * Shorthand for creating an IS UNKNOWN expression
     * 
     * @param argName
     * @return argName IS UNKNOWN
     */
    public static CoreExpression isUnknown(String argName) {
        return new MatchExpression(argName, MatchOperator.IS_UNKNOWN, null, null);
    }

    /**
     * Shorthand for creating an IS NOT UNKNOWN expression
     * 
     * @param argName
     * @return NOT argName IS UNKNOWN
     */
    public static CoreExpression isNotUnknown(String argName) {
        return isUnknown(argName).negate(true);
    }

    @Override
    public void collectFieldsInternal(Map<String, AudlangField.Builder> fieldMap) {

        // ensure the field is listed (even for is [not] unknown
        AudlangField.Builder field = fieldMap.computeIfAbsent(argName, AudlangField::forField);

        if (operand != null) {
            if (operand.isReference()) {
                field.addRefArgName(operand.value());
                AudlangField.Builder viceVersaField = fieldMap.computeIfAbsent(operand.value(), AudlangField::forField);
                viceVersaField.addRefArgName(argName);
            }
            else {
                field.addValue(operand.value());
            }
        }
    }

    @Override
    public String toString() {
        return inline;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MatchExpression match && match.inline.equals(this.inline);
    }

    @Override
    public int hashCode() {
        return inline.hashCode();
    }

    @Override
    public int compareTo(CoreExpression other) {
        if (other instanceof SpecialSetExpression) {
            // special set expressions come first (usually indicating a mistake)
            return 1;
        }
        else if (other instanceof CombinedExpression) {
            // move the combined expressions after the simple ones
            return -1;
        }
        else if (other instanceof MatchExpression otherMatch) {
            int res = this.argName.compareTo(otherMatch.argName);
            if (res != 0) {
                return res;
            }
            res = Integer.compare(this.operator.ordinal(), otherMatch.operator.ordinal());
            if (res != 0) {
                return res;
            }
        }
        else if (other instanceof NegationExpression neg) {
            int res = this.compareTo(neg.delegate());
            if (res == 0) {
                // negation of a match AFTER the same match
                res = -1;
            }
            return res;
        }
        return SimpleExpression.super.compareTo(other);
    }

    @Override
    public void appendSingleLine(StringBuilder sb, FormatStyle style, int level) {
        operator.formatAndAppend(sb, argName, operand, style, level);
    }

    @Override
    public void appendMultiLine(StringBuilder sb, FormatStyle style, int level) {
        operator.formatAndAppend(sb, argName, operand, style, level);
    }

    @Override
    public boolean shouldUseMultiLineFormatting(FormatStyle style) {
        return false;
    }

    @Override
    public void accept(CoreExpressionVisitor visitor) {
        visitor.visit(this);

    }

    @Override
    public CoreExpression negate(boolean strict) {
        if (strict || operator() == MatchOperator.IS_UNKNOWN) {
            return new NegationExpression(this, null);
        }
        else {
            List<CoreExpression> orMembers = new ArrayList<>();
            orMembers.add(new NegationExpression(this, null));
            orMembers.add(MatchExpression.of(argName, MatchOperator.IS_UNKNOWN, null));
            if (operand.isReference()) {
                orMembers.add(MatchExpression.of(operand.value(), MatchOperator.IS_UNKNOWN, null));
            }
            return CombinedExpression.orOf(orMembers);
        }
    }

}
