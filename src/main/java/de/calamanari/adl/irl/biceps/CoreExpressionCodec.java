//@formatter:off
/*
 * CoreExpressionCodec
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

package de.calamanari.adl.irl.biceps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.calamanari.adl.AudlangField;
import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.MatchExpression;
import de.calamanari.adl.irl.MatchExpression.MatchOperator;
import de.calamanari.adl.irl.NegationExpression;
import de.calamanari.adl.irl.Operand;
import de.calamanari.adl.irl.SimpleExpression;
import de.calamanari.adl.irl.SpecialSetExpression;

/**
 * The {@link CoreExpressionCodec} allows representing {@link CoreExpression}s in <i>binary form</i>.
 * <p/>
 * All leaf expressions (matches, negated matches as well as special sets) are represented as 32-bit integer values.<br/>
 * A special encoding (see below) allows quickly resolving the nature (what kind of leaf) but also the details like names and values.
 * <p/>
 * This format reduces memory consumption but also allows very fast comparison and negation of leaf expressions (single bit change), so that way more
 * expressions can be processed (for optimization) than it would be possible based on instances of {@link CoreExpression}.
 * <p/>
 * Combined expressions (AND/OR) can also be stored as integers but instead of storing the details of a combination (would exceed the number of available bits)
 * we instead encode an <b>expression id</b> (external reference to a {@link MemberArrayRegistry}). This allows the codec to represent all kinds of
 * {@link CoreExpression}s as simple integer values.
 * <p/>
 * The theoretical limitations for a {@link CoreExpressionCodec} are:
 * <ul>
 * <li>{@value #MAX_NUMBER_OF_VALUES} of different values or arguments (both use different pools, so theoretically you could have this number of arguments and
 * this number of values.</li>
 * <li>The number of combined expressions that can be encoded is limited to {@value #MAX_EXTERNAL_EXPRESSION_ID}.
 * </ul>
 * The limitations above don't have any practical meaning because other limits (execution time and memory) will be reached earlier if there are too many
 * arguments/values or the overall expression complexity is too high. Combinatoric explosion can make it impossible to handle certain expressions.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class CoreExpressionCodec {

    /**
     * 13 bits, maximum number of argument names or values that can be encoded
     */
    public static final int MAX_NUMBER_OF_VALUES = 8_192;

    /**
     * 27 bits, maximum external expression id
     */
    public static final int MAX_EXTERNAL_EXPRESSION_ID = 134_217_727;

    /**
     * <code>01111111111111111111111111111111</code>
     */
    public static final int ALL = Integer.MAX_VALUE;

    /**
     * <code>00000000000000000000000000000000</code>
     */
    public static final int NONE = 0;

    /**
     * This value is not a valid encoded expression.
     * <p/>
     * You can understand this value as the equivalent to <b>null</b>, we use it frequently as a placeholder. <br/>
     * In contrast to {@link #NONE} the {@link #INVALID} has no logical meaning and neither contradicts nor implies anything.
     * <p/>
     * <code>10000000000000000000000000000000</code>
     */
    public static final int INVALID = 1 << 31;

    /**
     * <code>10000000000000000000000000000000</code>
     */
    public static final int NEGATION_MASK = 1 << 31;

    /**
     * <code>01111111111111111111111111111111</code>
     */
    public static final int NEGATION_REMOVAL_MASK = ALL;

    /**
     * <code>0001</code>
     */
    public static final int OP_LESS_THAN = 1;

    /**
     * <code>0010</code>
     */
    public static final int OP_GREATER_THAN = 2;

    /**
     * <code>0011</code>
     */
    public static final int OP_EQUALS = 3;

    /**
     * <code>0100</code>
     */
    public static final int OP_CONTAINS = 4;

    /**
     * <code>0101</code>
     */
    public static final int OP_IS_UNKNOWN = 5;

    /**
     * <code>1101</code>
     */
    public static final int OP_AND_EXPRESSION_ID = 13;

    /**
     * <code>1110</code>
     */
    public static final int OP_OR_EXPRESSION_ID = 14;

    /**
     * Use <i>or</i> to set the operator
     * <p/>
     * <code>00000000000000000100000000000000</code>
     */
    public static final int OP_LESS_THAN_MASK = OP_LESS_THAN << 14;

    /**
     * Use <i>or</i> to set the operator
     * <p/>
     * <code>00000000000000001000000000000000</code>
     */
    public static final int OP_GREATER_THAN_MASK = OP_GREATER_THAN << 14;

    /**
     * Use <i>or</i> to set the operator
     * <p/>
     * <code>00000000000000001100000000000000</code>
     */
    public static final int OP_EQUALS_MASK = OP_EQUALS << 14;

    /**
     * Use <i>or</i> to set the operator
     * <p/>
     * <code>00000000000000010000000000000000</code>
     */
    public static final int OP_CONTAINS_MASK = OP_CONTAINS << 14;

    /**
     * Use <i>or</i> to set the operator
     * <p/>
     * <code>00000000000000010100000000000000</code>
     */
    public static final int OP_IS_UNKNOWN_MASK = OP_IS_UNKNOWN << 14;

    /**
     * Use <i>or</i> to indicate this sequence contains an external expression-ID of type AND
     * <p/>
     * <code>00000000000000110100000000000000</code>
     */
    public static final int OP_AND_EXPRESSION_ID_MASK = OP_AND_EXPRESSION_ID << 14;

    /**
     * Use <i>or</i> to indicate this sequence contains an external expression-ID of type OR
     * <p/>
     * <code>00000000000000111000000000000000</code>
     */
    public static final int OP_OR_EXPRESSION_ID_MASK = OP_OR_EXPRESSION_ID << 14;

    /**
     * Use <i>and</i> followed by a right-shift by 14 to obtain the operator
     * <p/>
     * <code>00000000000000111100000000000000</code>
     */
    public static final int OP_FILTER_MASK = 15 << 14;

    /**
     * Use <i>and</i> followed by a right-shift by 14 to obtain the operator
     * <p/>
     * <code>11111111111111000011111111111111</code>
     */
    public static final int OP_REMOVAL_MASK = ~(OP_FILTER_MASK);

    /**
     * Use <i>and</i> to obtain the index of the value or reference
     * <p/>
     * <code>00000000000000000001111111111111</code>
     */
    public static final int VALUE_FILTER_MASK = MAX_NUMBER_OF_VALUES - 1;

    /**
     * Use <i>and</i> to obtain the index of the value or reference
     * <p/>
     * <code>11111111111111111100000000000000</code>
     */
    public static final int VALUE_REMOVAL_MASK = -1 << 14;

    /**
     * Use <i>and</i> followed by a right-shift by 18 to obtain the index of the argument name
     * <p/>
     * <code>01111111111111000000000000000000</code>
     */
    public static final int ARG_NAME_FILTER_MASK = VALUE_FILTER_MASK << 18;

    /**
     * Use <i>and</i> to test if a given value is an argument reference
     * <p/>
     * <code>00000000000000000010000000000000</code>
     */
    public static final int ARG_REF_FILTER_MASK = 1 << 13;

    /**
     * Holds the detail data related to the expressions, means: argument names and values
     */
    private final Dictionary dictionary;

    /**
     * For fast resolution caches the ids for given expressions (we don't want multiple ids for the same leaf expression)
     */
    private final Map<CoreExpression, Integer> encodedExpressionCache = new HashMap<>();

    /**
     * For fast resolution caches the expressions for given IDs (we don't want multiple ids for the same leaf expression)
     */
    private final Map<Integer, CoreExpression> decodedExpressionCache = new HashMap<>();

    /**
     * Operands may be decoded frequently, thus we cache the operand per id
     */
    private final Map<Integer, Operand> decodedOperandCache = new HashMap<>();

    /**
     * Creates a codec instance with the given dictionary
     * 
     * @param dictionary
     */
    public CoreExpressionCodec(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * @param encodedExpression
     * @return true if the given encoded expression is a negation
     */
    public static boolean isNegation(int encodedExpression) {
        return encodedExpression < 0 && encodedExpression != INVALID;
    }

    /**
     * Creates an IS UNKNOWN expression for the argument name in the given expression
     * <p/>
     * Example: for <code>arg = 1</code> we return <code>arg IS UNKNOWN</code>
     * 
     * @param encodedExpression
     * @return IS UNKNOWN expression for the included argument name
     * @throws ExpressionCodecException if there is no argument name involved
     */
    public static int createIsUnknownForArgName(int encodedExpression) {
        if (isCombinedExpressionId(encodedExpression)) {
            throw new ExpressionCodecException("Cannot create IS UNKNOWN for external expression-ID");
        }
        if (isSpecialSet(encodedExpression)) {
            throw new ExpressionCodecException("Cannot create IS UNKNOWN for special set");
        }

        return (encodedExpression & OP_REMOVAL_MASK & VALUE_REMOVAL_MASK & NEGATION_REMOVAL_MASK) | OP_IS_UNKNOWN_MASK;

    }

    /**
     * Creates an IS UNKNOWN expression for the referenced argument name in the given expression
     * <p/>
     * Example: for <code>arg = &#64;other</code> we return <code>other IS UNKNOWN</code>
     * 
     * @param encodedExpression
     * @return IS UNKNOWN expression for the included referenced argument name
     * @throws ExpressionCodecException if there is no referenced argument name involved
     */
    public static int createIsUnknownForReferencedArgName(int encodedExpression) {

        if (isCombinedExpressionId(encodedExpression)) {
            throw new ExpressionCodecException("Cannot create IS UNKNOWN for external expression-ID");
        }

        if (isSpecialSet(encodedExpression)) {
            throw new ExpressionCodecException("Cannot create IS UNKNOWN for special set");
        }

        if (!isReferenceMatch(encodedExpression)) {
            throw new ExpressionCodecException("Cannot create IS UNKNOWN for referenced argName (no reference match): " + encodedExpression);
        }
        // simply moves the referenced argName to the argName position and changes the operation
        return ((encodedExpression << 18) & NEGATION_REMOVAL_MASK) | OP_IS_UNKNOWN_MASK;

    }

    /**
     * Negates the given leaf expression
     * <p/>
     * Example 1: <code>a = 1</code> turns into <code>NOT a = 1</code><br/>
     * Example 2: <code>NOT a = 1</code> turns into <code>a = 1</code><br/>
     * Example 3: <code>NONE</code> turns into <code>ALL</code> and vice-versa.
     * 
     * @param encodedExpression leaf
     * @return negated leaf
     * @throws ExpressionCodecException if the given expression is not a leaf
     */
    public static int negate(int encodedExpression) {

        if (encodedExpression > 0 && isCombinedExpressionId(encodedExpression)) {
            throw new ExpressionCodecException(String.format("Cannot negate %s because it is an external expression-ID of type %s: %s", encodedExpression,
                    getNodeType(encodedExpression), decodeCombinedExpressionId(encodedExpression)));
        }

        if (encodedExpression == INVALID) {
            throw new ExpressionCodecException("Cannot negate expression (INVALID node)");
        }

        switch (encodedExpression) {
        case ALL:
            return NONE;
        case NONE:
            return ALL;
        default: {
            if (encodedExpression < 0) {
                // this unsets the first bit
                return encodedExpression & Integer.MAX_VALUE;
            }
            else {
                // this sets the first bit
                return encodedExpression | Integer.MIN_VALUE;
            }
        }
        }
    }

    /**
     * This method only deals with leaves and allows a quick check if the left one is the negation of the right one.
     * <p/>
     * <b>Important:</b> There is a difference between <i>strict negation</i> a <i>complement</i>: <code>STRICT NOT a = 1</code> is the strict negation of
     * <code>a = 1</code> but not its complement! The complement of <code>a = 1</code> would be <code>STRICT NOT a = 1 OR a IS UNKNOWN</code>.
     * 
     * @param leftEncoded
     * @param rightEncoded
     * @return if the left is a leaf expression and the negation of the right leaf expression, otherwise false
     */
    public static boolean isLeftNegationOfRight(int leftEncoded, int rightEncoded) {

        if (leftEncoded == INVALID || rightEncoded == INVALID || isCombinedExpressionId(leftEncoded) || isCombinedExpressionId(rightEncoded)) {
            return false;
        }

        return rightEncoded == negate(leftEncoded);
    }

    /**
     * Determines if the left expression's argument name equals the right expression's argument name or the right expression's referenced argument name.
     * <p/>
     * Example 1: <code>a = 1</code> vs. <code>NOT a = 1</code> <br/>
     * Example 2: <code>a = 1</code> vs. <code>a = 3</code><br/>
     * Example 3: <code>a IS UNKNOWN</code> vs. <code>a = 1</code><br/>
     * Example 4: <code>a IS UNKNOWN</code> vs. <code>other = &#64;a</code>
     * 
     * @param leftEncoded
     * @param rightEncoded
     * @return true if the left argName equals the right argName or the right referenced argName
     */
    public static boolean isLeftArgNameSameAsRightArgNameOrReferencedArgName(int leftEncoded, int rightEncoded) {
        if (leftEncoded == INVALID || rightEncoded == INVALID || isCombinedExpressionId(leftEncoded) || isCombinedExpressionId(rightEncoded)
                || isSpecialSet(leftEncoded) || isSpecialSet(rightEncoded)) {
            return false;
        }
        else {
            int argNameIdxLeft = (leftEncoded & ARG_NAME_FILTER_MASK) >> 18;
            int argNameIdxRight = (rightEncoded & ARG_NAME_FILTER_MASK) >> 18;

            boolean res = (argNameIdxLeft == argNameIdxRight);
            if (!res && isReferenceMatch(rightEncoded)) {
                int refArgNameIdxRight = rightEncoded & VALUE_FILTER_MASK;
                res = (argNameIdxLeft == refArgNameIdxRight);
            }
            return res;
        }
    }

    /**
     * Tells whether both expressions have the same argName
     * <p/>
     * Example 1: <code>a = 1</code> vs. <code>NOT a = 1</code> <br/>
     * Example 2: <code>a = 1</code> vs. <code>a = 3</code><br/>
     * Example 3: <code>a IS UNKNOWN</code> vs. <code>a = 1</code>
     * 
     * @param leftEncoded
     * @param rightEncoded
     * @return true if the left argName equals the right argName
     */
    public static boolean haveSameArgName(int leftEncoded, int rightEncoded) {
        if (leftEncoded == INVALID || rightEncoded == INVALID || isCombinedExpressionId(leftEncoded) || isCombinedExpressionId(rightEncoded)
                || isSpecialSet(leftEncoded) || isSpecialSet(rightEncoded)) {
            return false;
        }
        else {
            int argNameIdxLeft = (leftEncoded & ARG_NAME_FILTER_MASK) >> 18;
            int argNameIdxRight = (rightEncoded & ARG_NAME_FILTER_MASK) >> 18;
            return argNameIdxLeft == argNameIdxRight;
        }

    }

    /**
     * @param encodedExpression
     * @return true if the given expression is an IS UNKNOWN expression
     */
    public static boolean isUnknown(int encodedExpression) {
        return encodedExpression > 0 && (encodedExpression & OP_FILTER_MASK) >> 14 == OP_IS_UNKNOWN;
    }

    /**
     * @param encodedExpression
     * @return true if the given expression is a NOT IS UNKNOWN expression
     */
    public static boolean isNegatedUnknown(int encodedExpression) {
        return encodedExpression < 0 && (encodedExpression & OP_FILTER_MASK) >> 14 == OP_IS_UNKNOWN;
    }

    /**
     * @param encodedExpression
     * @return true if the given expression is a combined expression (AND/OR) and not a leaf
     */
    public static boolean isCombinedExpressionId(int encodedExpression) {
        return encodedExpression > 0
                && ((encodedExpression & OP_FILTER_MASK) >> 14 == OP_AND_EXPRESSION_ID || (encodedExpression & OP_FILTER_MASK) >> 14 == OP_OR_EXPRESSION_ID);
    }

    /**
     * @param encodedExpression
     * @return node type of the expression, returns {@link NodeType#LEAF} by default (also for INVALID nodes)
     */
    public static NodeType getNodeType(int encodedExpression) {
        if (encodedExpression > 0 && (encodedExpression & OP_FILTER_MASK) >> 14 == OP_AND_EXPRESSION_ID) {
            return NodeType.AND;
        }
        else if (encodedExpression > 0 && (encodedExpression & OP_FILTER_MASK) >> 14 == OP_OR_EXPRESSION_ID) {
            return NodeType.OR;
        }
        else {
            return NodeType.LEAF;
        }
    }

    /**
     * @param encodedExpression
     * @return true if the given expression is {@link #ALL} or {@link #NONE}
     */
    public static boolean isSpecialSet(int encodedExpression) {
        return encodedExpression == ALL || encodedExpression == NONE;
    }

    /**
     * Determines the name of the argument in a match expression
     * <p/>
     * Example: for <code>color = blue</code> we would return <code>color</code>
     * 
     * @param encodedExpression
     * @return argument name of the given expression, or null for {@link #ALL} or {@link #NONE}
     * @throws ExpressionCodecException if this is a combined expression or INVALID
     */
    public String getArgName(int encodedExpression) {

        if (isCombinedExpressionId(encodedExpression)) {
            throw new ExpressionCodecException(String.format("Cannot decode argName of %s because it is an external expression-ID of type %s: %s",
                    encodedExpression, getNodeType(encodedExpression), decodeCombinedExpressionId(encodedExpression)));

        }
        if (encodedExpression == INVALID) {
            throw new ExpressionCodecException("Cannot decode argument name (INVALID node)");
        }

        if (isSpecialSet(encodedExpression)) {
            return null;
        }
        else {
            int argNameIdx = (encodedExpression & ARG_NAME_FILTER_MASK) >> 18;
            return dictionary.getArgumentName(argNameIdx);
        }
    }

    /**
     * @param encodedExpression
     * @return operator of the given expression, or null for {@link #ALL} or {@link #NONE}
     * @throws ExpressionCodecException if this is a combined expression or INVALID
     */
    public static MatchOperator getOperator(int encodedExpression) {

        if (isCombinedExpressionId(encodedExpression)) {
            throw new ExpressionCodecException(String.format("Cannot decode operator of %s because it is an external expression-ID of type %s: %s",
                    encodedExpression, getNodeType(encodedExpression), decodeCombinedExpressionId(encodedExpression)));

        }
        if (encodedExpression == INVALID) {
            throw new ExpressionCodecException("Cannot decode operator (INVALID node)");
        }

        if (isSpecialSet(encodedExpression)) {
            return null;
        }
        else {
            int opMask = (encodedExpression & OP_FILTER_MASK) >> 14;
            switch (opMask) {
            case OP_LESS_THAN:
                return MatchOperator.LESS_THAN;
            case OP_GREATER_THAN:
                return MatchOperator.GREATER_THAN;
            case OP_EQUALS:
                return MatchOperator.EQUALS;
            case OP_CONTAINS:
                return MatchOperator.CONTAINS;
            case OP_IS_UNKNOWN:
                return MatchOperator.IS_UNKNOWN;
            default:
                return null;
            }
        }

    }

    /**
     * Determines if this is a match expression with a referenced argument
     * <p/>
     * Example: <code>arg = &#64;other</code>
     * 
     * @param encodedExpression
     * @return true if the given expression is a reference match
     */
    public static boolean isReferenceMatch(int encodedExpression) {
        return !isSpecialSet(encodedExpression) && !isCombinedExpressionId(encodedExpression)
                && (encodedExpression & ARG_REF_FILTER_MASK) == ARG_REF_FILTER_MASK;
    }

    /**
     * Determines the name of the referenced argument in a reference match expression
     * <p/>
     * Example: for <code>arg = &#64;other</code> we would return <code>other</code>
     * 
     * @param encodedExpression
     * @return referenced argument name of the given expression, or null if this is not a reference match
     * @throws ExpressionCodecException if this is a combined expression or INVALID
     */
    public String getReferencedArgName(int encodedExpression) {

        if (isCombinedExpressionId(encodedExpression)) {
            throw new ExpressionCodecException(String.format("Cannot referenced argName of %s because it is an external expression-ID of type %s: %s",
                    encodedExpression, getNodeType(encodedExpression), decodeCombinedExpressionId(encodedExpression)));

        }

        if (encodedExpression == INVALID) {
            throw new ExpressionCodecException("Cannot decode referenced argument name (INVALID node)");
        }

        if (isReferenceMatch(encodedExpression)) {
            int argNameIdx = encodedExpression & VALUE_FILTER_MASK;
            return dictionary.getArgumentName(argNameIdx);
        }
        else {
            return null;
        }
    }

    /**
     * Determines the value in a match expression.
     * <p/>
     * Example: for <code>color = blue</code> we would return <code>blue</code>
     * 
     * @param encodedExpression
     * @return value or null if this is not a match expression or a reference match expression
     * @throws ExpressionCodecException if this is a combined expression or INVALID
     */
    public String getValue(int encodedExpression) {

        if (isCombinedExpressionId(encodedExpression)) {
            throw new ExpressionCodecException(String.format("Cannot value of %s because it is an external expression-ID of type %s: %s", encodedExpression,
                    getNodeType(encodedExpression), decodeCombinedExpressionId(encodedExpression)));

        }

        if (encodedExpression == INVALID) {
            throw new ExpressionCodecException("Cannot decode value (INVALID node)");
        }
        if (isSpecialSet(encodedExpression)) {
            return null;
        }

        MatchOperator operator = getOperator(encodedExpression);

        if (operator == null || operator == MatchOperator.IS_UNKNOWN || isReferenceMatch(encodedExpression)) {
            return null;
        }
        else {
            int valueIdx = encodedExpression & VALUE_FILTER_MASK;
            return dictionary.getValue(valueIdx);
        }

    }

    /**
     * Determines the operand in a match expression.
     * 
     * @param encodedExpression
     * @return operand or null if this is not a match expression
     * @throws ExpressionCodecException if this is a combined expression or INVALID
     */
    public Operand getOperand(int encodedExpression) {

        if (isCombinedExpressionId(encodedExpression)) {
            throw new ExpressionCodecException(String.format("Cannot decode operand of %s because it is an external expression-ID of type %s: %s",
                    encodedExpression, getNodeType(encodedExpression), decodeCombinedExpressionId(encodedExpression)));

        }

        if (encodedExpression == INVALID) {
            throw new ExpressionCodecException("Cannot decode operand (INVALID node)");
        }
        if (isSpecialSet(encodedExpression)) {
            return null;
        }

        return decodedOperandCache.computeIfAbsent(encodedExpression, key -> {
            MatchOperator operator = getOperator(key);
            if (operator == null || operator == MatchOperator.IS_UNKNOWN) {
                return null;
            }
            else {
                int idx = key & VALUE_FILTER_MASK;
                Operand res = null;
                if (isReferenceMatch(key)) {
                    res = Operand.of(dictionary.getArgumentName(idx), true);
                }
                else {
                    res = Operand.of(dictionary.getValue(idx), false);
                }
                return res;
            }

        });

    }

    /**
     * Encodes a match expression or a negated match expression
     * 
     * @param expression
     * @return encoded expression
     * @throws ExpressionCodecException if the given expression is incompatible to the underlying dictionary (unknown arguments or values)
     */
    public int encode(SimpleExpression expression) {

        return encodedExpressionCache.computeIfAbsent(expression, key -> {
            int negMask = expression instanceof NegationExpression ? NEGATION_MASK : 0;
            int argNameIdxMask = dictionary.indexOfArgName(expression.argName()) << 18;
            int opMask = 0;
            int refMask = 0;
            int valueIdxMask = 0;

            switch (expression.operator()) {
            case LESS_THAN:
                opMask = OP_LESS_THAN_MASK;
                break;
            case GREATER_THAN:
                opMask = OP_GREATER_THAN_MASK;
                break;
            case EQUALS:
                opMask = OP_EQUALS_MASK;
                break;
            case CONTAINS:
                opMask = OP_CONTAINS_MASK;
                break;
            case IS_UNKNOWN:
                opMask = OP_IS_UNKNOWN_MASK;
                break;
            }
            if (opMask != OP_IS_UNKNOWN_MASK) {
                if (expression.referencedArgName() != null) {
                    refMask = ARG_REF_FILTER_MASK;
                    valueIdxMask = dictionary.indexOfArgName(expression.referencedArgName());
                }
                else {
                    valueIdxMask = dictionary.indexOfValue(expression.operand().value());
                }
            }

            int res = negMask | argNameIdxMask | opMask | refMask | valueIdxMask;
            decodedExpressionCache.putIfAbsent(res, expression);
            return res;

        });

    }

    /**
     * @param expression
     * @return encoded special set expression, either {@link #ALL} or {@link #NONE}
     */
    public int encode(SpecialSetExpression expression) {
        if (expression.equals(SpecialSetExpression.all())) {
            return ALL;
        }
        else {
            return NONE;
        }
    }

    /**
     * Encodes any leaf expression (matches, negations, special sets)
     * 
     * @param expression
     * @return encoded expression
     * @throws ExpressionCodecException if the given expression is a combined expression (AND/OR)
     */
    public int encode(CoreExpression expression) {
        switch (expression) {
        case SimpleExpression simple:
            return encode(simple);
        case SpecialSetExpression spc:
            return encode(spc);
        default:
            throw new ExpressionCodecException("Cannot directly encode combined Expression, given: " + expression);
        }
    }

    /**
     * Decodes any leaf expression (matches, negations, special sets)
     * 
     * @param encodedExpression
     * @return decoded expression
     * @throws ExpressionCodecException if the given expression is a combined expression (AND/OR), or INVALID or otherwise not de-codable
     */
    public CoreExpression decode(int encodedExpression) {
        if (encodedExpression == INVALID) {
            throw new ExpressionCodecException("Cannot decode (INVALID node)");
        }
        return decodedExpressionCache.computeIfAbsent(encodedExpression, key -> {
            switch (key) {
            case ALL:
                return SpecialSetExpression.all();
            case NONE:
                return SpecialSetExpression.none();
            default: {
                String argName = getArgName(key);
                MatchOperator operator = getOperator(key);
                Operand operand = getOperand(key);
                CoreExpression res = MatchExpression.of(argName, operator, operand);
                if (isNegation(key)) {
                    res = res.negate(true);
                }
                encodedExpressionCache.putIfAbsent(res, key);
                return res;
            }
            }
        });

    }

    /**
     * Encodes the ID of a combined expression (AND/OR)
     * 
     * @param id of the member array, see {@link MemberArrayRegistry}
     * @param nodeType
     * @return encoded combined expression
     * @throws ExpressionCodecException if the node type is missing or the given id is out of range, or if the expression-ID is too high, see
     *             {@link #MAX_EXTERNAL_EXPRESSION_ID}
     */
    public static int encodeCombinedExpressionId(int id, NodeType nodeType) {
        if (nodeType == null) {
            throw new ExpressionCodecException(String.format("Cannot encode combined expression-id (%s, type=null) because nodeType is mandatory", id));
        }
        if (nodeType == NodeType.LEAF) {
            throw new ExpressionCodecException(
                    String.format("Cannot encode combined expression-id (%s, type=%s) because only AND and OR are eligible.", id, nodeType));
        }

        if (id < 0) {
            throw new ExpressionCodecException(String.format("Cannot encode negative combined expression-id (%s, type=%s), expected 0 <= id <= %s", id,
                    nodeType, MAX_EXTERNAL_EXPRESSION_ID));
        }

        if (id > MAX_EXTERNAL_EXPRESSION_ID) {
            throw new ExpressionCodecException(
                    String.format("Cannot encode combined expression-id (%s, type=%s), expected 0 <= id <= %s", id, nodeType, MAX_EXTERNAL_EXPRESSION_ID));
        }

        // Because the reserved operator space is in the middle of the bit sequence, we need to split the 27-bit ID-value

        // part 1: 13 bits (leading)
        int part1 = (id >>> 14) << 18;

        // part 4: 14 bits (trailing)
        int part2 = (id << 18) >>> 18;

        if (nodeType == NodeType.AND) {
            return part1 | OP_AND_EXPRESSION_ID_MASK | part2;
        }
        else {
            return part1 | OP_OR_EXPRESSION_ID_MASK | part2;
        }

    }

    /**
     * Decodes the ID of a combined expression (AND/OR)
     * 
     * @param encodedExpression
     * @return de-coded ID of the member array, see {@link MemberArrayRegistry}
     * @throws ExpressionCodecException if the node is not a combined expression
     */
    public static int decodeCombinedExpressionId(int encodedExpression) {
        if (!isCombinedExpressionId(encodedExpression)) {
            throw new ExpressionCodecException(String.format("Cannot decode %s because it is not an combined expression-id.", encodedExpression));
        }

        if (encodedExpression == INVALID) {
            throw new ExpressionCodecException("Cannot decode combined expression-id (INVALID node)");
        }

        int part1 = (encodedExpression >>> 18) << 14;
        int part2 = (encodedExpression << 18) >>> 18;

        return part1 | part2;
    }

    /**
     * For testing purposes
     */
    public void clearCaches() {
        this.decodedExpressionCache.clear();
        this.encodedExpressionCache.clear();
        this.decodedOperandCache.clear();
    }

    /**
     * Creates a new codec that covers both, arguments and values from <i>this</i> instance and from the other.
     * <p/>
     * The new instance is created in a way that it exists independently from both sources but it remains fully compatible to <i>this</i> instance, so that an
     * existing expression tree based on <i>this</i> instance would remain valid on the newly created <i>merged</i> codec.
     * <p/>
     * Any expression tree based on the <i>other</i> coded remains valid but it would have to be recoded to be compatible to the new <i>merged</i> codec.
     * 
     * @param other to be merged
     * @return merged codec or this codec if both were compatible resp. identical
     */
    public CoreExpressionCodec merge(CoreExpressionCodec other) {
        if (this == other) {
            return this;
        }
        CoreExpressionCodec res = new CoreExpressionCodec(dictionary.merge(other.dictionary));
        res.decodedExpressionCache.putAll(this.decodedExpressionCache);
        res.encodedExpressionCache.putAll(this.encodedExpressionCache);
        res.decodedOperandCache.putAll(this.decodedOperandCache);
        if (this.dictionary == other.dictionary) {
            res.decodedExpressionCache.putAll(other.decodedExpressionCache);
            res.encodedExpressionCache.putAll(other.encodedExpressionCache);
            res.decodedOperandCache.putAll(other.decodedOperandCache);
        }
        return res;
    }

    /**
     * The dictionary holds the clear text values (attribute names and values), so that the expressions can use tiny references in their bit sequences instead
     * of dealing with clumsy text values.
     * <p/>
     * Therefore, all expected values must be known <i>beforehand</i>. Thus a dictionary instance is an <i>immutable</i> throughout its lifetime.
     */
    public static class Dictionary {

        private final Map<String, Integer> argNameToIndexMap;

        private final List<String> argNames;

        private final Map<String, Integer> valueToIndexMap;

        private final List<String> values;

        /**
         * Creates a new dictionary by indexing the given names and values.
         * <p/>
         * The returned dictionary is only suitable for expressions using these argument names and values.
         * 
         * @param argNames <b>all</b> expected argument names ever to be encoded with the codec
         * @param values <b>all</b> expected argument values ever to be encoded with the codec
         * @param skipValidation if true, we bypass the validation process (only internal use)
         * @throws ExpressionCodecException if the maximum number of values/names is exceeded (see {@link CoreExpressionCodec#MAX_NUMBER_OF_VALUES})
         */
        private Dictionary(List<String> argNames, List<String> values, boolean skipValidation) {
            if (skipValidation) {
                this.argNames = Collections.unmodifiableList(argNames);
                this.values = Collections.unmodifiableList(values);
            }
            else {
                this.argNames = Collections.unmodifiableList(dedup(argNames));
                this.values = Collections.unmodifiableList(dedup(values));
            }
            this.argNameToIndexMap = createIndexMap(this.argNames);
            this.valueToIndexMap = createIndexMap(this.values);
        }

        /**
         * Creates a new dictionary by indexing the given names and values.
         * <p/>
         * The returned dictionary is only suitable for expressions using these argument names and values.
         * 
         * @param argNames <b>all</b> expected argument names ever to be encoded with the codec
         * @param values <b>all</b> expected argument values ever to be encoded with the codec
         * @throws ExpressionCodecException if the maximum number of values/names is exceeded (see {@link CoreExpressionCodec#MAX_NUMBER_OF_VALUES})
         */
        public Dictionary(List<String> argNames, List<String> values) {
            this(argNames, values, false);
        }

        /**
         * Convenience method that takes all values from the fields previously collected from an expression
         * <p/>
         * The returned dictionary is only suitable for expressions using these argument names and values.
         * 
         * @param allFields
         * @throws ExpressionCodecException if the maximum number of values/names is exceeded (see {@link CoreExpressionCodec#MAX_NUMBER_OF_VALUES})
         */
        public Dictionary(List<AudlangField> allFields) {
            this(collectArgNames(allFields), collectValues(allFields));
        }

        /**
         * grabs all the names from the given list
         * 
         * @param allFields
         * @return list with names
         */
        private static List<String> collectArgNames(List<AudlangField> allFields) {
            List<String> res = new ArrayList<>();
            for (AudlangField field : allFields) {
                res.add(field.argName());
                res.addAll(field.refArgNames());
            }
            return res;
        }

        /**
         * grabs all the argument values from the given list
         * 
         * @param allFields
         * @return list with values
         */
        private static List<String> collectValues(List<AudlangField> allFields) {
            List<String> res = new ArrayList<>();
            for (AudlangField field : allFields) {
                res.addAll(field.values());
            }
            return res;
        }

        /**
         * @param values
         * @return list of values, free of duplicates
         */
        private static List<String> dedup(List<String> values) {
            List<String> valuesSorted = new ArrayList<>(values == null ? Collections.emptyList() : values);
            Collections.sort(valuesSorted);

            String prevValue = null;
            for (int i = valuesSorted.size() - 1; i > -1; i--) {
                String currentValue = valuesSorted.get(i);
                if (currentValue == null || currentValue.equals(prevValue)) {
                    valuesSorted.remove(i);
                }
                prevValue = currentValue;
            }
            if (valuesSorted.size() > MAX_NUMBER_OF_VALUES) {
                throw new ExpressionCodecException(String.format("Unable to process expression, maximum number of arguments or values (%s) exceeded. Given: %s",
                        MAX_NUMBER_OF_VALUES, valuesSorted.size()));
            }
            return valuesSorted;
        }

        /**
         * Creates the forward-index (value-to-number) (backward is implicit because the values corresponds to the list position)
         * 
         * @param uniqueValues
         * @return forward index for encoding
         */
        private static Map<String, Integer> createIndexMap(List<String> uniqueValues) {
            Map<String, Integer> res = new HashMap<>();
            for (int i = 0; i < uniqueValues.size(); i++) {
                res.put(uniqueValues.get(i), i);
            }
            return res;
        }

        /**
         * @param argName
         * @return code of the name in the dictionary
         * @throws ExpressionCodecException if the argument is unknown
         */
        public int indexOfArgName(String argName) {
            Integer res = argNameToIndexMap.get(argName);
            if (res == null) {
                throw new ExpressionCodecException("No such argument name in dictionary: " + argName);
            }
            return res;
        }

        /**
         * @param idx
         * @return name corresponding to the index value
         * @throws ExpressionCodecException if the index value is unknown or out of range
         */
        public String getArgumentName(int idx) {
            if (idx < 0 || idx > argNames.size() - 1) {
                throw new ExpressionCodecException("No argument name in dictionary at index: " + idx);
            }
            return argNames.get(idx);
        }

        /**
         * @param value
         * @return code of the value in the dictionary
         * @throws ExpressionCodecException if the value is unknown
         */
        public int indexOfValue(String value) {
            Integer res = valueToIndexMap.get(value);
            if (res == null) {
                throw new ExpressionCodecException("No such value in dictionary: " + value);
            }
            return res;
        }

        /**
         * @param idx
         * @return argument value corresponding to the index value
         * @throws ExpressionCodecException if the index value is unknown or out of range
         */
        public String getValue(int idx) {
            if (idx < 0 || idx > values.size() - 1) {
                throw new ExpressionCodecException("No value in dictionary at index: " + idx);
            }
            return values.get(idx);
        }

        /**
         * This method creates a <i>new Dictionary</i> instance with the merged argument names and values.
         * <p/>
         * The new dictionary keeps all argNames and values with their existing codes from <b>this</b> instance and adds any new argName/value from the other.
         * Means: Any existing expression tree based on <b>this</b> instance will be compatible to the merged instance while any expression tree originally
         * based on the other instance would need recoding.
         * 
         * @param other
         * @return merged dictionary or this instance if it covered the other anyway
         */
        public Dictionary merge(Dictionary other) {

            if (this == other) {
                return this;
            }

            List<String> combinedArgNames = new ArrayList<>(other.argNames.size());
            combinedArgNames.addAll(this.argNames);
            List<String> combinedValues = new ArrayList<>(other.values.size());
            combinedValues.addAll(this.values);

            boolean extended = false;
            for (String argName : other.argNames) {
                if (!this.argNameToIndexMap.containsKey(argName)) {
                    combinedArgNames.add(argName);
                    extended = true;
                }
            }
            for (String value : other.values) {
                if (!this.valueToIndexMap.containsKey(value)) {
                    combinedValues.add(value);
                    extended = true;
                }
            }
            if (extended) {
                return new Dictionary(combinedArgNames, combinedValues, true);
            }
            else {
                return this;
            }
        }

    }

}
