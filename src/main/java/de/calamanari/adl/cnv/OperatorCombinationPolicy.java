//@formatter:off
/*
 * OperatorCombinationPolicy
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

package de.calamanari.adl.cnv;

import java.util.Arrays;
import java.util.List;

import de.calamanari.adl.erl.PlExpression;
import de.calamanari.adl.erl.PlMatchExpression;
import de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator;
import de.calamanari.adl.erl.PlNegationExpression;
import de.calamanari.adl.erl.PlOperand;

/**
 * Supplementary enumeration that helps re-creating the higher language features of Audlang by combining operators within the same OR resp.AND.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public enum OperatorCombinationPolicy {

    /**
     * Re-creates less than or equals resp. the negation form
     */
    LESS_THAN_OR_EQUALS(PlMatchOperator.LESS_THAN, PlMatchOperator.LESS_THAN_OR_EQUALS),

    /**
     * Re-creates greater than or equals resp. the negation form
     */
    GREATER_THAN_OR_EQUALS(PlMatchOperator.GREATER_THAN, PlMatchOperator.GREATER_THAN_OR_EQUALS),

    /**
     * Re-creates BETWEEN resp. the negation form
     */
    BETWEEN(PlMatchOperator.GREATER_THAN_OR_EQUALS, PlMatchOperator.BETWEEN);

    /**
     * The operator to look for first (this will also be the expression that will be replaced in the parent)
     */
    private final PlMatchOperator srcOperator;

    /**
     * The replacement operator if the policy is applicable
     */
    private final PlMatchOperator destOperator;

    private OperatorCombinationPolicy(PlMatchOperator srcOperator, PlMatchOperator destOperator) {
        this.srcOperator = srcOperator;
        this.destOperator = destOperator;
    }

    /**
     * Runs the policy on the members of an OR
     * 
     * @param members to be updated
     * @param candidateIdx
     * @param consumedMembers when combining two members, one of them becomes obsolete (here we only collect for later removal)
     * @return true if the members were modified
     */
    public boolean applyInOrParent(List<PlExpression<?>> members, int candidateIdx, List<PlExpression<?>> consumedMembers) {
        boolean modified = false;
        PlExpression<?> candidate = members.get(candidateIdx);
        if (this == BETWEEN) {
            modified = applyBetweenCombinationInOrParent(members, candidateIdx, consumedMembers);
        }
        else if (candidate instanceof PlMatchExpression match && match.operator() == srcOperator) {
            modified = applyCompareCombinationInOrParent(members, candidateIdx, match, consumedMembers);
        }
        return modified;
    }

    /**
     * Applies the rules for less-than-or-equals resp. greater-than-or-equals (look for equals and ensure both operands are the same)
     * 
     * @param members
     * @param candidateIdx
     * @param match identified left candidate
     * @param consumedMembers when combining two members, one of them becomes obsolete (here we only collect for later removal)
     * @return true if members were updated
     */
    private boolean applyCompareCombinationInOrParent(List<PlExpression<?>> members, int candidateIdx, PlMatchExpression match,
            List<PlExpression<?>> consumedMembers) {
        boolean modified = false;
        int indexOfEquals = indexOfExpression(members, match.argName(), PlMatchOperator.EQUALS, match.operands().get(0));
        if (indexOfEquals > -1) {
            consumedMembers.add(members.get(indexOfEquals));
            members.set(candidateIdx, new PlMatchExpression(match.argName(), destOperator, match.operands(), match.comments()));
            modified = true;
        }
        return modified;
    }

    /**
     * Applies the rules for BETWEEN, here we must check that the operands are not references.
     * <p/>
     * Note: BETWEEN is different than less-than/greater than, here we look for the negative conditions (NOT) inside the OR rather than the AND because
     * <code>a NOT BETWEEN (1, 2)</code> is equivalent to <code>a NOT &gt;= 1 OR a NOT &lt;= 2)</code>
     * 
     * @param members
     * @param candidateIdx
     * @param consumedMembers when combining two members, one of them becomes obsolete (here we only collect for later removal)
     * @return true if the members were updated
     */
    private boolean applyBetweenCombinationInOrParent(List<PlExpression<?>> members, int candidateIdx, List<PlExpression<?>> consumedMembers) {
        boolean modified = applyBetweenCombinationInOrParent(members, candidateIdx, consumedMembers, true);
        modified = applyBetweenCombinationInOrParent(members, candidateIdx, consumedMembers, false) || modified;
        return modified;
    }

    /**
     * Detail implementation considering the strictness of the negation
     * 
     * @param members
     * @param candidateIdx
     * @param consumedMembers when combining two members, one of them becomes obsolete (here we only collect for later removal)
     * @param strict
     * @return true if the members were updated
     */
    private boolean applyBetweenCombinationInOrParent(List<PlExpression<?>> members, int candidateIdx, List<PlExpression<?>> consumedMembers, boolean strict) {
        boolean modified = false;
        PlExpression<?> candidate = members.get(candidateIdx);

        if (candidate instanceof PlNegationExpression neg && neg.isStrict() == strict && neg.delegate() instanceof PlMatchExpression match
                && match.operator() == PlMatchOperator.GREATER_THAN_OR_EQUALS) {
            PlOperand operand = match.operands().get(0);
            if (!operand.isReference()) {
                modified = applyBetweenCombinationInOrParent(members, candidateIdx, match, operand, consumedMembers, strict);
            }

        }
        return modified;
    }

    /**
     * Detail implementation after a promising left candidate was identified.
     * 
     * @param members
     * @param candidateIdx
     * @param match
     * @param operand
     * @param consumedMembers when combining two members, one of them becomes obsolete (here we only collect for later removal)
     * @param strict
     * @return true if modified
     */
    private boolean applyBetweenCombinationInOrParent(List<PlExpression<?>> members, int candidateIdx, PlMatchExpression match, PlOperand operand,
            List<PlExpression<?>> consumedMembers, boolean strict) {
        boolean modified = false;
        for (int idx = 0; !modified && idx < members.size(); idx++) {
            PlExpression<?> candidate2 = members.get(idx);
            if (candidate2 instanceof PlNegationExpression neg2 && neg2.isStrict() == strict && neg2.delegate() instanceof PlMatchExpression match2
                    && match2.argName().equals(match.argName()) && match2.operator() == PlMatchOperator.LESS_THAN_OR_EQUALS) {
                PlOperand operand2 = match2.operands().get(0);
                if (operand2.isReference()) {
                    break;
                }
                members.set(candidateIdx, new PlMatchExpression(match.argName(), strict ? PlMatchOperator.STRICT_NOT_BETWEEN : PlMatchOperator.NOT_BETWEEN,
                        Arrays.asList(operand, operand2), null));
                consumedMembers.add(candidate2);
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Runs the policy on the members of an AND
     * 
     * @param members to be modified
     * @param candidateIdx
     * @param consumedMembers when combining two members, one of them becomes obsolete (here we only collect for later removal)
     * @return true if the members were modified
     */
    public boolean applyInAndParent(List<PlExpression<?>> members, int candidateIdx, List<PlExpression<?>> consumedMembers) {
        boolean modified = false;
        PlExpression<?> candidate = members.get(candidateIdx);
        if (this == BETWEEN) {
            modified = applyBetweenCombinationInAndParent(members, candidateIdx, consumedMembers);
        }
        else if (candidate instanceof PlNegationExpression neg && neg.isStrict() && neg.delegate() instanceof PlMatchExpression match
                && match.operator() == srcOperator) {
            modified = applyCompareCombinationInAndParent(members, candidateIdx, match, consumedMembers, true);
        }
        else if (candidate instanceof PlNegationExpression neg && !neg.isStrict() && neg.delegate() instanceof PlMatchExpression match
                && match.operator() == srcOperator) {
            modified = applyCompareCombinationInAndParent(members, candidateIdx, match, consumedMembers, false);
        }
        return modified;
    }

    /**
     * Checks for less-than-or-equals resp. greater-then-or-equals inside an AND (strict vs. non-strict)
     * 
     * @param members
     * @param candidateIdx
     * @param match
     * @param consumedMembers when combining two members, one of them becomes obsolete (here we only collect for later removal)
     * @param strict flag to avoid mixing strict and non-strict members
     * @return true if the members were updated
     */
    private boolean applyCompareCombinationInAndParent(List<PlExpression<?>> members, int candidateIdx, PlMatchExpression match,
            List<PlExpression<?>> consumedMembers, boolean strict) {
        boolean modified = false;
        int indexOfNotEquals = indexOfExpression(members, match.argName(), (strict ? PlMatchOperator.STRICT_NOT_EQUALS : PlMatchOperator.NOT_EQUALS),
                match.operands().get(0));
        if (indexOfNotEquals > -1) {
            consumedMembers.add(members.get(indexOfNotEquals));
            members.set(candidateIdx,
                    new PlNegationExpression(new PlMatchExpression(match.argName(), destOperator, match.operands(), match.comments()), strict, null));
            modified = true;
        }
        return modified;
    }

    /**
     * Checks for the BETWEEN (positive form) inside an AND, e.g. <code>a &gt;= 3 AND a &lt;= 4</code> makes <code>a BETWEEN (3, 4)</code>
     * 
     * @param members
     * @param candidateIdx
     * @param consumedMembers when combining two members, one of them becomes obsolete (here we only collect for later removal)
     * @return true if members were updated
     */
    private boolean applyBetweenCombinationInAndParent(List<PlExpression<?>> members, int candidateIdx, List<PlExpression<?>> consumedMembers) {

        boolean modified = false;
        PlExpression<?> candidate = members.get(candidateIdx);

        if (candidate instanceof PlMatchExpression match && match.operator() == PlMatchOperator.GREATER_THAN_OR_EQUALS) {
            PlOperand operand = match.operands().get(0);
            if (!operand.isReference()) {
                modified = applyBetweenCombinationInAndParent(members, candidateIdx, match, operand, consumedMembers);
            }

        }
        return modified;
    }

    /**
     * Detail implementation after identifying the left candidate
     * 
     * @param members
     * @param candidateIdx
     * @param match
     * @param operand
     * @param consumedMembers when combining two members, one of them becomes obsolete (here we only collect for later removal)
     * @return true if members were modified
     */
    private boolean applyBetweenCombinationInAndParent(List<PlExpression<?>> members, int candidateIdx, PlMatchExpression match, PlOperand operand,
            List<PlExpression<?>> consumedMembers) {
        boolean modified = false;
        for (int idx = 0; !modified && idx < members.size(); idx++) {
            PlExpression<?> candidate2 = members.get(idx);
            if (candidate2 instanceof PlMatchExpression match2 && match2.argName().equals(match.argName())
                    && match2.operator() == PlMatchOperator.LESS_THAN_OR_EQUALS) {
                PlOperand operand2 = match2.operands().get(0);
                if (operand2.isReference()) {
                    break;
                }
                members.set(candidateIdx, new PlMatchExpression(match.argName(), PlMatchOperator.BETWEEN, Arrays.asList(operand, operand2), null));
                consumedMembers.add(candidate2);
                modified = true;
            }
        }
        return modified;
    }

    /**
     * compares the operands, e.g., required for combining less-than-or-equals
     * 
     * @param left
     * @param right
     * @return true if both are equal
     */
    private boolean haveEqualOperands(PlOperand left, PlOperand right) {
        return left.isReference() == right.isReference() && left.value().equals(right.value());
    }

    /**
     * Searches for the requested expression and returns the index
     * 
     * @param members
     * @param argName
     * @param operator
     * @param operand
     * @return index or -1 if not found
     */
    private int indexOfExpression(List<PlExpression<?>> members, String argName, PlMatchOperator operator, PlOperand operand) {
        for (int idx = 0; idx < members.size(); idx++) {
            PlExpression<?> candidate = members.get(idx);
            if (candidate instanceof PlMatchExpression match && match.argName().equals(argName) && match.operator() == operator && match.operands().size() == 1
                    && haveEqualOperands(match.operands().get(0), operand)) {
                return idx;
            }
        }
        return -1;
    }
}