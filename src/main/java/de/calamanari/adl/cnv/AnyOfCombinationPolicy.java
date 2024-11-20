//@formatter:off
/*
 * AnyOfCombinationPolicy
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

import java.util.ArrayList;
import java.util.List;

import de.calamanari.adl.erl.PlExpression;
import de.calamanari.adl.erl.PlMatchExpression;
import de.calamanari.adl.erl.PlMatchOperator;
import de.calamanari.adl.erl.PlOperand;

/**
 * Supplementary enumeration that helps re-creating the higher language features of Audlang by replacing multiple comparison with a single ANY-OF condition or
 * the corresponding negative form.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public enum AnyOfCombinationPolicy {

    ANY_OF(
            new OperatorPair(PlMatchOperator.EQUALS, PlMatchOperator.ANY_OF),
            new OperatorPair(PlMatchOperator.NOT_EQUALS, PlMatchOperator.NOT_ANY_OF),
            new OperatorPair(PlMatchOperator.STRICT_NOT_EQUALS, PlMatchOperator.STRICT_NOT_ANY_OF),
            true),
    CONTAINS_ANY_OF(
            new OperatorPair(PlMatchOperator.CONTAINS, PlMatchOperator.CONTAINS_ANY_OF),
            new OperatorPair(PlMatchOperator.NOT_CONTAINS, PlMatchOperator.NOT_CONTAINS_ANY_OF),
            new OperatorPair(PlMatchOperator.STRICT_NOT_CONTAINS, PlMatchOperator.STRICT_NOT_CONTAINS_ANY_OF),
            false);

    private final OperatorPair positive;
    private final OperatorPair negative;
    private final OperatorPair strictNegative;
    private final boolean supportsReferenceMatch;

    private AnyOfCombinationPolicy(OperatorPair positive, OperatorPair negative, OperatorPair strictNegative, boolean supportsReferenceMatch) {
        this.positive = positive;
        this.negative = negative;
        this.strictNegative = strictNegative;
        this.supportsReferenceMatch = supportsReferenceMatch;
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
        if (candidate instanceof PlMatchExpression match && match.operator() == positive.single
                && (!match.operands().get(0).isReference() || supportsReferenceMatch) && !consumedMembers.contains(candidate)) {
            List<PlMatchExpression> furtherCandidates = collectFurtherCandidates(members, candidateIdx, match.argName(), positive.single);
            if (!furtherCandidates.isEmpty()) {
                consolidateMembers(members, candidateIdx, match, furtherCandidates, positive.multi, consumedMembers);
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Runs the policy on the members of an AND
     * 
     * @param members to be updated
     * @param candidateIdx
     * @param consumedMembers when combining two members, one of them becomes obsolete (here we only collect for later removal)
     * @return true if the members were modified
     */
    public boolean applyInAndParent(List<PlExpression<?>> members, int candidateIdx, List<PlExpression<?>> consumedMembers) {
        boolean modified = applyInAndParent(members, candidateIdx, consumedMembers, true);
        modified = applyInAndParent(members, candidateIdx, consumedMembers, false) || modified;
        return modified;
    }

    /**
     * Runs the policy on the members of an AND (either strict or non-strict, never mix)
     * 
     * @param members to be updated
     * @param candidateIdx
     * @param consumedMembers when combining two members, one of them becomes obsolete (here we only collect for later removal)
     * @param strict
     * @return true if the members were modified
     */
    private boolean applyInAndParent(List<PlExpression<?>> members, int candidateIdx, List<PlExpression<?>> consumedMembers, boolean strict) {
        boolean modified = false;
        PlMatchOperator srcOperator = strict ? strictNegative.single : negative.single;
        PlExpression<?> candidate = members.get(candidateIdx);
        if (candidate instanceof PlMatchExpression match && match.operator() == srcOperator
                && (!match.operands().get(0).isReference() || supportsReferenceMatch) && !consumedMembers.contains(candidate)) {
            List<PlMatchExpression> furtherCandidates = collectFurtherCandidates(members, candidateIdx, match.argName(), srcOperator);
            if (!furtherCandidates.isEmpty()) {
                consolidateMembers(members, candidateIdx, match, furtherCandidates, strict ? strictNegative.multi : negative.multi, consumedMembers);
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Looks for further candidates (left to right), for example: <code>a = 1 OR a = 2 OR a = 3</code>, then <code>a = 1</code> would be the first candidate and
     * <code>a = 2</code> plus <code>a = 3</code> would be <i>further</i> candidates.
     * 
     * @param members
     * @param candidateIdx
     * @param argName
     * @param srcOperator the operator to search for
     * @return list with additional candidates, may be empty
     */
    private List<PlMatchExpression> collectFurtherCandidates(List<PlExpression<?>> members, int candidateIdx, String argName, PlMatchOperator srcOperator) {
        List<PlMatchExpression> furtherCandidates = new ArrayList<>();
        for (int idx = candidateIdx + 1; idx < members.size(); idx++) {
            PlExpression<?> candidate2 = members.get(idx);
            if (candidate2 instanceof PlMatchExpression match2 && match2.argName().equals(argName) && match2.operator() == srcOperator
                    && (!match2.operands().get(0).isReference() || supportsReferenceMatch)) {
                furtherCandidates.add(match2);
            }
        }
        return furtherCandidates;
    }

    /**
     * Here we replace the main candidate with the new combination and mark all of the remaining candidates as consumed.
     * 
     * @param members
     * @param candidateIdx
     * @param match
     * @param furtherCandidates
     * @param destOperator
     * @param consumedMembers
     */
    private void consolidateMembers(List<PlExpression<?>> members, int candidateIdx, PlMatchExpression match, List<PlMatchExpression> furtherCandidates,
            PlMatchOperator destOperator, List<PlExpression<?>> consumedMembers) {
        List<PlOperand> operands = new ArrayList<>(furtherCandidates.size() + 1);
        operands.add(match.operands().get(0));
        for (PlMatchExpression furtherCandidate : furtherCandidates) {
            operands.add(furtherCandidate.operands().get(0));
            consumedMembers.add(furtherCandidate);
        }
        members.set(candidateIdx, new PlMatchExpression(match.argName(), destOperator, operands, null));
    }

    /**
     * Simple tuple to keep the single-operand operator together with the matching multi-operand operator (ANY OF)
     */
    private static record OperatorPair(PlMatchOperator single, PlMatchOperator multi) {

    }

}
