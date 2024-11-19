//@formatter:off
/*
 * ExpressionLogicHelper
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

import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.ALL;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.INVALID;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.NONE;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.createIsUnknownForArgName;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.createIsUnknownForReferencedArgName;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.getNodeType;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.haveSameArgName;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isCombinedExpressionId;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isLeftArgNameSameAsRightArgNameOrReferencedArgName;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isLeftNegationOfRight;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isNegatedUnknown;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isReferenceMatch;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isSpecialSet;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isUnknown;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.negate;

import java.io.Serializable;
import java.util.Arrays;

/**
 * The {@link ExpressionLogicHelper} contains a couple of common utility methods to make logic decisions when transforming an expression tree.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class ExpressionLogicHelper implements Serializable {

    private static final long serialVersionUID = -5923746787896322169L;
    private final MemberArrayRegistry memberArrayRegistry;

    public ExpressionLogicHelper(MemberArrayRegistry memberArrayRegistry) {
        this.memberArrayRegistry = memberArrayRegistry;
    }

    /**
     * Checks whether the leaf on the left <i>implies</i> the right leaf.
     * 
     * @param left
     * @param right
     * @return true if left implies right
     */
    private boolean leftLeafImpliesRightLeaf(int left, int right) {
        // @formatter:off
        return (left == ALL 
                    || (left != NONE && left == right)
                    || (isLeftArgNameSameAsRightArgNameOrReferencedArgName(right, left) && !isUnknown(left) && isNegatedUnknown(right)))
                ;
        // @formatter:on

    }

    /**
     * Checks whether the left AND resp. OR <i>implies</i> the right leaf.
     * 
     * @param left
     * @param right
     * @return true if left implies right
     */
    private boolean leftCombinedImpliesRightLeaf(int left, int right) {
        NodeType nodeType = getNodeType(left);
        switch (nodeType) {
        case AND:
            return leftCombinedAndImpliesRightLeaf(membersOf(left), right);
        case OR:
            return leftCombinedOrImpliesRightLeaf(membersOf(left), right);
        // $CASES-OMITTED$
        default:
            return false;
        }
    }

    /**
     * Checks whether any of the left members <i>implies</i> the right leaf.
     * 
     * @param left
     * @param right
     * @return true if left implies right
     */
    private boolean leftCombinedAndImpliesRightLeaf(int[] leftMembers, int right) {
        for (int member : leftMembers) {
            if (leftImpliesRight(member, right)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the members of an OR on the left all <i>imply</i> the right leaf.
     * 
     * @param leftMembers
     * @param right
     * @return true if all of the members on the right imply the right one
     */
    private boolean leftCombinedOrImpliesRightLeaf(int[] leftMembers, int right) {
        boolean res = false;
        for (int member : leftMembers) {
            if (!leftImpliesRight(member, right)) {
                return false;
            }
            else {
                res = true;
            }
        }
        return res;
    }

    /**
     * Checks whether the left leaf or combines expression <i>implies</i> the right combined one.
     * 
     * @param left
     * @param right
     * @return true if left implies right
     */
    private boolean leftImpliesRightCombined(int left, int right) {

        NodeType rightNodeType = getNodeType(right);
        int[] rightMembers = membersOf(right);

        boolean res = false;
        for (int rightMember : rightMembers) {
            if (!leftImpliesRight(left, rightMember)) {
                if (rightNodeType == NodeType.AND) {
                    res = false;
                    break;
                }
            }
            else {
                res = true;
            }
        }
        return res;
    }

    /**
     * Determines if the left expression <i>logically implies</i> that the right expression is true.
     * <ul>
     * <li><code>left == &lt;ALL&gt;</code></li>
     * <li><code>left == right</code> (<b>Note:</b> &lt;NONE&gt; <i>does <b>not</b> imply</i> &lt;NONE&gt;)</li>
     * <li><code>arg = 1</code> implies <code>arg IS NOT UNKNOWN</code></li>
     * <li><code>other = &#64;arg</code> implies <code>arg IS NOT UNKNOWN</code></li>
     * </ul>
     * This method works recursively:
     * <ul>
     * <li><code>(a=1 AND b=1)</code> implies <code>(b IS NOT UNKNOWN)</code></li>
     * <li><code>(a=1 AND b=1 AND (d=1 OR d=2))</code> implies <code>(d IS NOT UNKNOWN)</code></li>
     * </ul>
     * <p>
     * This method is restricted to <i>simple implications</i> (we consider one element from the left at a time). <br>
     * See also {@link #leftContradictsRight(int, int)}
     * 
     * @param left encoded expression
     * @param right encoded expression
     * @return true if the left expression (if true) implies the right is true as well
     */
    public boolean leftImpliesRight(int left, int right) {

        if (left == INVALID || right == INVALID) {
            return false;
        }
        else if (left == right || left == ALL) {
            return true;
        }

        boolean leftIsCombined = isCombinedExpressionId(left);
        boolean rightIsCombined = isCombinedExpressionId(right);

        int[] leftMembers = leftIsCombined ? membersOf(left) : MemberUtils.EMPTY_MEMBERS;
        int[] rightMembers = rightIsCombined ? membersOf(right) : MemberUtils.EMPTY_MEMBERS;

        return leftImpliesRight(left, leftIsCombined, leftMembers, right, rightIsCombined, rightMembers);
    }

    /**
     * @param left
     * @param leftIsCombined
     * @param leftNodeType
     * @param leftMembers
     * @param right
     * @param rightIsCombined
     * @param rightNodeType
     * @param rightMembers
     * @return true if the left expression (if true) implies the right is true as well
     */
    private boolean leftImpliesRight(int left, boolean leftIsCombined, int[] leftMembers, int right, boolean rightIsCombined, int[] rightMembers) {

        NodeType leftNodeType = getNodeType(left);
        NodeType rightNodeType = getNodeType(right);

        boolean res = false;
        // @formatter:off
        if (((leftIsCombined && leftNodeType == rightNodeType)
                     && ((leftNodeType == NodeType.AND && MemberUtils.sortedLeftMembersContainSortedRightMembers(leftMembers, rightMembers))
                             || (leftNodeType == NodeType.OR && MemberUtils.sortedLeftMembersContainSortedRightMembers(rightMembers, leftMembers))))
                || (!leftIsCombined && rightNodeType == NodeType.OR && Arrays.binarySearch(rightMembers, left) > -1)) {
            // implication
            res = true;
        }
        // @formatter:on
        else if (rightIsCombined) {
            res = leftImpliesRightCombined(left, right);
        }
        else if (leftIsCombined) {
            res = leftCombinedImpliesRightLeaf(left, right);
        }
        else {
            res = leftLeafImpliesRightLeaf(left, right);
        }
        return res;
    }

    /**
     * Tells if the two conditions cannot both be true at the same time (AND)
     * <ul>
     * <li><code>a = 1 AND &lt;NONE&gt;</code></li>
     * <li><code>a = 1 AND a != 1</code></li>
     * <li><code>a IS UNKNOWN AND a IS NOT UNKNOWN</code></li>
     * <li><code>a = &#064;b AND a IS UNKNOWN</code></li>
     * <li><code>a = &#064;b AND b IS UNKNOWN</code></li>
     * </ul>
     * 
     * @param left
     * @param right
     * @return if an AND combination of left and right cannot be fulfilled
     */
    private boolean leftLeafContradictsRightLeaf(int left, int right) {
        // @formatter:off
        return (left == CoreExpressionCodec.NONE 
                    || isLeftNegationOfRight(left, right)
                    || (isLeftArgNameSameAsRightArgNameOrReferencedArgName(left, right) && isUnknown(left) && !isUnknown(right)) 
                    || (isLeftArgNameSameAsRightArgNameOrReferencedArgName(right, left) && isUnknown(right) && !isUnknown(left)))
                ;
        // @formatter:on
    }

    /**
     * Checks whether the left combined expression contradicts the right leaf
     * 
     * @param left
     * @param right
     * @return true if right would be false if left is true
     */
    private boolean leftCombinedContradictsRightLeaf(int left, int right) {
        NodeType nodeType = getNodeType(left);
        switch (nodeType) {
        case AND:
            return leftCombinedAndContradictsRightLeaf(membersOf(left), right);
        case OR:
            return leftCombinedOrContradictsRightLeaf(membersOf(left), right);
        // $CASES-OMITTED$
        default:
            return false;
        }
    }

    /**
     * Checks whether the left leaf or combined expression contradicts the right combined
     * 
     * @param left
     * @param right
     * @return true if right would be false if left is true
     */
    private boolean leftContradictsRightCombined(int left, int right) {

        NodeType rightNodeType = getNodeType(right);
        int[] rightMembers = membersOf(right);

        boolean res = false;
        for (int rightMember : rightMembers) {
            if (!leftContradictsRight(left, rightMember)) {
                if (rightNodeType == NodeType.OR) {
                    res = true;
                    break;
                }
            }
            else {
                res = true;
            }
        }
        return res;
    }

    /**
     * Checks whether any of the left AND expression members contradicts the right leaf
     * 
     * @param leftMembers
     * @param right
     * @return true if any member contradicts the right one
     */
    private boolean leftCombinedAndContradictsRightLeaf(int[] leftMembers, int right) {
        for (int member : leftMembers) {
            if (leftContradictsRight(member, right)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether all of the left OR expression members contradict the right leaf
     * 
     * @param leftMembers
     * @param right
     * @return true if all members contradict the right one
     */
    private boolean leftCombinedOrContradictsRightLeaf(int[] leftMembers, int right) {
        boolean res = false;
        for (int member : leftMembers) {
            if (!leftContradictsRight(member, right)) {
                return false;
            }
            else {
                res = true;
            }
        }
        return res;
    }

    /**
     * Checks whether any of the left <i>AND expression members</i> contradicts the right leaf or combined
     * 
     * @param leftAndMembers
     * @param right
     * @param skipLeftContradictionCheck true indicates that the members of left have already been checked for simple contradictions among each other
     * @return true if any member contradicts the right one
     */
    public boolean leftCombinedAndContradictsRight(int[] leftAndMembers, int right, boolean skipLeftContradictionCheck) {

        if (!skipLeftContradictionCheck && haveAnySimpleContradictionInAndParent(leftAndMembers)) {
            return true;
        }

        switch (getNodeType(right)) {
        case AND:
            return leftCombinedAndContradictsRightAnd(leftAndMembers, membersOf(right), skipLeftContradictionCheck);
        case OR:
            return leftCombinedAndContradictsRightOr(leftAndMembers, membersOf(right), skipLeftContradictionCheck);
        // $CASES-OMITTED$
        default: {
            for (int leftMember : leftAndMembers) {
                if (leftContradictsRight(leftMember, right)) {
                    return true;
                }
            }
        }
        }
        return false;
    }

    /**
     * @param leftAndMembers
     * @param rightAndMembers
     * @return true if the left AND-members contradicts and of the right AND-members
     */
    private boolean leftCombinedAndContradictsRightAnd(int[] leftAndMembers, int[] rightAndMembers, boolean skipLeftContradictionCheck) {
        for (int rightMember : rightAndMembers) {
            if (leftCombinedAndContradictsRight(leftAndMembers, rightMember, skipLeftContradictionCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param leftAndMembers
     * @param rightAndMembers
     * @return true if the left AND-members contradicts all of the right OR-members
     */
    private boolean leftCombinedAndContradictsRightOr(int[] leftAndMembers, int[] rightOrMembers, boolean skipLeftContradictionCheck) {
        boolean res = false;
        for (int rightMember : rightOrMembers) {
            if (!leftCombinedAndContradictsRight(leftAndMembers, rightMember, skipLeftContradictionCheck)) {
                return false;
            }
            else {
                res = true;
            }
        }
        return res;
    }

    /**
     * Checks whether the left expression contradicts the right one.
     * <p>
     * This method can only detect what we call <i>simple</i> contradictions. <br>
     * The contract of this method is:
     * <ul>
     * <li>Simple contradictions will be detected, complex ones not. The term <i>simple</i> means <i>"taking only one member from the left at a time"</i> <br>
     * Example:
     * <ul>
     * <li>left: <code>(a = 1 OR STRICT NOT c = 1) <b>AND</b> (b = 1 OR STRICT NOT c = 1)</code></li>
     * <li>right: <code>c = 1 AND (STRICT NOT a = 1 OR STRICT NOT b = 1)</code></li>
     * </ul>
     * As you can see there is no simple contradiction between left and right, because one of the left members alone does not contradict anything on the right.
     * Only if you consider both conditions from the left <i>at the same time</i> suddenly right gets contradicted.<br>
     * These contradictions are harder to find and not covered by this method.
     * <li>For the same inputs this method and its counterpart {@link #leftImpliesRight(int, int)} will not return true at the same time (both may return
     * false).</li>
     * </ul>
     * 
     * @param left
     * @param right
     * @return true if the left expression contradicts the right one
     */
    public boolean leftContradictsRight(int left, int right) {

        if (left == INVALID || right == INVALID || left == right || left == ALL) {
            return false;
        }

        boolean leftIsCombined = isCombinedExpressionId(left);
        boolean rightIsCombined = isCombinedExpressionId(right);

        int[] leftMembers = leftIsCombined ? membersOf(left) : MemberUtils.EMPTY_MEMBERS;
        int[] rightMembers = rightIsCombined ? membersOf(right) : MemberUtils.EMPTY_MEMBERS;

        return leftContradictsRight(left, right, leftIsCombined, rightIsCombined, leftMembers, rightMembers);
    }

    /**
     * @param left
     * @param right
     * @param leftIsCombined
     * @param rightIsCombined
     * @param leftMembers
     * @param rightMembers
     * @param skipLeftContradictionCheck true indicates that the members of left have already been checked for simple contradictions among each other
     * @return true if the left expression contradicts the right one
     */
    private boolean leftContradictsRight(int left, int right, boolean leftIsCombined, boolean rightIsCombined, int[] leftMembers, int[] rightMembers) {
        NodeType leftNodeType = getNodeType(left);
        NodeType rightNodeType = getNodeType(right);

        boolean res = false;

        // @formatter:off
        if (((leftIsCombined && leftNodeType == rightNodeType) && ((MemberUtils.sortedLeftMembersContainSortedRightMembers(leftMembers, rightMembers))
                                                                      || MemberUtils.sortedLeftMembersContainSortedRightMembers(rightMembers, leftMembers)))
                || (leftIsCombined && rightNodeType == NodeType.LEAF && Arrays.binarySearch(leftMembers, right) > -1)
                || (rightIsCombined && leftNodeType == NodeType.LEAF && Arrays.binarySearch(rightMembers, left) > -1)) {
            // no contradiction
            res = false;
        }
        // @formatter:on
        else if (rightIsCombined) {
            res = leftContradictsRightCombined(left, right);
        }
        else if (leftIsCombined) {
            res = leftCombinedContradictsRightLeaf(left, right);
        }
        else {
            res = leftLeafContradictsRightLeaf(left, right);
        }
        return res;
    }

    /**
     * Checks two conditions for bi-directional contradiction
     * <p>
     * <code>arg IS UNKNOWN</code> vs. <code>arg IS NOT UNKNOWN</code> and vice-versa.
     * 
     * @param left
     * @param right
     * @return true if one side is an IS UNKNOWN and the other is an IS NOT UNKNOWN of the same argument
     */
    public boolean haveIsUnknownContradiction(int left, int right) {
        return left != INVALID && right != INVALID && haveSameArgName(left, right)
                && ((isUnknown(left) && isNegatedUnknown(right)) || (isNegatedUnknown(left) && isUnknown(right)));
    }

    /**
     * Checks whether there is any <i>simple</i> contradiction among the members of an AND.
     * 
     * @param members
     * @return true if there is any contradiction
     */
    public boolean haveAnySimpleContradictionInAndParent(int[] members) {

        int[] testMembers = Arrays.copyOf(members, members.length);

        for (int idx = 0; idx < members.length; idx++) {
            int member = members[idx];
            if (member != INVALID) {
                testMembers[idx] = INVALID;
                if (leftCombinedAndContradictsRight(testMembers, member, true)) {
                    return true;
                }
                testMembers[idx] = member;
            }
        }
        return false;
    }

    /**
     * Checks whether there is any IS-UNKNOWN contradiction (see {@link #haveIsUnknownContradiction(int, int)}) among the members of an OR.
     * 
     * @param members
     * @return true if there is any contradiction
     */
    public boolean haveAnyIsUnknownContradictionInOrParent(int[] members) {

        for (int leftIdx = 0; leftIdx < members.length - 1; leftIdx++) {
            int left = members[leftIdx];
            for (int rightIdx = leftIdx + 1; rightIdx < members.length; rightIdx++) {
                int right = members[rightIdx];
                if (haveIsUnknownContradiction(left, right)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param node
     * @return true if the node is an AND or OR that itself contains a nested AND or OR
     */
    public boolean containsAnyCombinedMember(int node) {
        return node != INVALID && isCombinedExpressionId(node) && MemberUtils.containsAnyCombinedMember(membersOf(node));
    }

    /**
     * @param combinedNode
     * @return members of the given node if it is a combined expression, otherwise throws exception
     */
    public int[] membersOf(int combinedNode) {
        return MemberUtils.membersOf(memberArrayRegistry, combinedNode);
    }

    /**
     * Performs a couple of checks and returns an {@link Advice} what to do next.
     * 
     * @param nodeType
     * @param leftMember
     * @param rightMember
     * @return advice
     */
    public Advice checkImplications(NodeType nodeType, int leftMember, int rightMember) {
        if (leftMember == INVALID || rightMember == INVALID) {
            // Advice covers logical implications, not cleanup intentions!
            return Advice.RETAIN_BOTH;
        }
        switch (nodeType) {
        case AND:
            return checkImplicationsInAndParent(leftMember, rightMember);
        case OR:
            return checkImplicationsInOrParent(leftMember, rightMember);
        // $CASES-OMITTED$
        default:
            throw new IllegalStateException("Illegal parent-nodeType: " + nodeType);
        }
    }

    /**
     * Computes the advice inside an AND
     * 
     * @param leftMember
     * @param rightMember
     * @return advice
     */
    private Advice checkImplicationsInAndParent(int leftMember, int rightMember) {
        if (leftMember == ALL && rightMember == ALL) {
            return Advice.REMOVE_BOTH;
        }
        else if (leftMember == NONE || rightMember == NONE || leftContradictsRight(leftMember, rightMember)) {
            return Advice.NEVER_TRUE;
        }
        else if (leftMember == ALL) {
            return Advice.REMOVE_LEFT;
        }
        else if (rightMember == ALL) {
            return Advice.REMOVE_RIGHT;
        }
        else {
            boolean leftRightImplication = leftImpliesRight(leftMember, rightMember);
            boolean rightLeftImplication = leftImpliesRight(rightMember, leftMember);
            if (leftRightImplication && rightLeftImplication) {
                return computeBidirectionalImplicationAdvice(leftMember, rightMember);
            }
            else if (leftRightImplication) {
                return Advice.REMOVE_RIGHT;
            }
            else if (rightLeftImplication) {
                return Advice.REMOVE_LEFT;
            }
            else {
                return computeDeepImplicationAdviceInAndParent(leftMember, rightMember);
            }
        }
    }

    /**
     * Computes the advice inside an OR
     * 
     * @param leftMember
     * @param rightMember
     * @return advice
     */
    private Advice checkImplicationsInOrParent(int leftMember, int rightMember) {
        if (leftMember == NONE && rightMember == NONE) {
            return Advice.REMOVE_BOTH;
        }
        else if (leftMember == ALL || rightMember == ALL || haveIsUnknownContradiction(leftMember, rightMember)) {
            // Important: We are not checking for further contradictions here, because
            // with just two members you cannot decide
            // Example: arg = 1 OR arg != 1 is not a true contradiction because arg could be UNKNOWN
            // Besides ALL, only one case is trivial: arg IS UNKNOWN OR arg IS NOT UNKNOWN => always true
            return Advice.ALWAYS_TRUE;
        }
        else if (leftMember == NONE) {
            return Advice.REMOVE_LEFT;
        }
        else if (rightMember == NONE) {
            return Advice.REMOVE_RIGHT;
        }
        else if (bothCanBeReplacedWithIsNotUnknownInOrParent(leftMember, rightMember)) {
            return Advice.REPLACE_BOTH_WITH_IS_NOT_UNKNOWN;
        }
        else {
            return computeDeepImplicationAdviceInOrParent(leftMember, rightMember);
        }
    }

    /**
     * Sometimes we see <code>arg = 1 OR arg != 1</code>, which can be replaced with <code>arg IS NOT UNKNOWN</code>
     * 
     * @param leftMember
     * @param rightMember
     * @return true if the two can be replaced
     */
    private boolean bothCanBeReplacedWithIsNotUnknownInOrParent(int leftMember, int rightMember) {
        return leftMember != INVALID && rightMember != INVALID && !isCombinedExpressionId(leftMember) && !isSpecialSet(leftMember) && !isUnknown(leftMember)
                && !isNegatedUnknown(leftMember) && isLeftNegationOfRight(leftMember, rightMember);
    }

    /**
     * Performs a detail comparison (analysis of the members and their members) to compute an implication advice for an OR
     * 
     * @param leftMember
     * @param rightMember
     * @return advice
     */
    private Advice computeDeepImplicationAdviceInOrParent(int leftMember, int rightMember) {
        boolean leftRightImplication = leftImpliesRight(leftMember, rightMember);
        boolean rightLeftImplication = leftImpliesRight(rightMember, leftMember);
        if (leftRightImplication && rightLeftImplication) {
            return computeBidirectionalImplicationAdvice(leftMember, rightMember);
        }
        else if (leftRightImplication) {
            return Advice.REMOVE_LEFT;
        }
        else if (rightLeftImplication) {
            return Advice.REMOVE_RIGHT;
        }
        return Advice.RETAIN_BOTH;
    }

    /**
     * Sometimes both members could be candidates for removal, this method tries to guess the better option.
     * 
     * @param leftMember
     * @param rightMember
     * @return advice
     */
    private Advice computeBidirectionalImplicationAdvice(int leftMember, int rightMember) {
        boolean leftIsNested = containsAnyCombinedMember(leftMember);
        boolean rightIsNested = containsAnyCombinedMember(rightMember);
        if (!rightIsNested) {
            return Advice.REMOVE_ANY_LEFT;
        }
        else if (!leftIsNested) {
            return Advice.REMOVE_ANY_RIGHT;
        }
        else {
            int leftNumberOfMembers = membersOf(leftMember).length;
            int rightNumberOfMembers = membersOf(rightMember).length;
            if (rightNumberOfMembers > leftNumberOfMembers) {
                return Advice.REMOVE_ANY_RIGHT;
            }
            else {
                return Advice.REMOVE_ANY_LEFT;
            }
        }
    }

    /**
     * Performs a detail comparison (analysis of the members and their members) to compute an implication advice for an AND
     * 
     * @param leftMember
     * @param rightMember
     * @return advice
     */
    private Advice computeDeepImplicationAdviceInAndParent(int leftMember, int rightMember) {
        boolean leftRightImplication = leftImpliesRight(leftMember, rightMember);
        boolean rightLeftImplication = leftImpliesRight(rightMember, leftMember);
        if (leftRightImplication && rightLeftImplication) {
            return computeBidirectionalImplicationAdvice(leftMember, rightMember);
        }
        else if (leftRightImplication) {
            return Advice.REMOVE_RIGHT;
        }
        else if (rightLeftImplication) {
            return Advice.REMOVE_LEFT;
        }
        return Advice.RETAIN_BOTH;
    }

    /**
     * An AND of AND resp. OR of OR is not only confusing, it also complicates the logic. Thus, this method resolves any child element that is of the same time
     * as the parent into the parent.
     * 
     * @param parentNodeType
     * @param members
     * @return the members array or a new array if there where any members resolved
     */
    public int[] expandCombinedNodesOfSameType(NodeType parentNodeType, int[] members) {

        if (MemberUtils.containsAnyCombinedMember(members)) {
            GrowingIntArray temp = new GrowingIntArray(members.length);
            for (int memberIdx = 0; memberIdx < members.length; memberIdx++) {
                expandCombinedNodesOfSameType(parentNodeType, members[memberIdx], temp, memberIdx, false);
            }
            return temp.toArray();
        }
        else {
            return members;
        }

    }

    /**
     * Expands any member in result into result if it is of the given parent node type
     * 
     * @param parentNodeType
     * @param result
     */
    public void expandCombinedNodesOfSameType(NodeType parentNodeType, GrowingIntArray result) {
        int max = result.size();
        for (int idx = 0; idx < max; idx++) {
            expandCombinedNodesOfSameType(parentNodeType, result.get(idx), result, idx, true);
        }
    }

    /**
     * Expands the member into result if it is of the given parent node type
     * 
     * @param parentNodeType
     * @param result
     */
    private void expandCombinedNodesOfSameType(NodeType nodeType, int member, GrowingIntArray result, int memberIdx, boolean update) {
        if (getNodeType(member) == nodeType) {
            int[] subMembers = membersOf(member);
            for (int subMemberIdx = 0; subMemberIdx < subMembers.length; subMemberIdx++) {
                expandCombinedNodesOfSameType(nodeType, subMembers[subMemberIdx], result, subMemberIdx, false);
            }
            if (update) {
                result.set(memberIdx, INVALID);
            }
        }
        else if (!update) {
            result.add(member);
        }
    }

    /**
     * Creates a raw complement of the given leaf node, "raw" means without knowledge about neighbors and implications
     * 
     * @param tree
     * @param node
     * @return complement
     */
    private int createComplementOfLeafNode(EncodedExpressionTree tree, int node) {
        if (node == INVALID) {
            return node;
        }
        int negatedNode = negate(node);
        if (isSpecialSet(node) || isUnknown(node) || isNegatedUnknown(node)) {
            return negatedNode;
        }

        int argIsUnknown = createIsUnknownForArgName(node);

        if (isReferenceMatch(node)) {
            int referencedArgIsUnknown = createIsUnknownForReferencedArgName(node);

            int[] orMembers = new int[] { negatedNode, argIsUnknown, referencedArgIsUnknown };
            return tree.createNode(NodeType.OR, orMembers);

        }
        else {
            return tree.createNode(NodeType.OR, new int[] { negatedNode, argIsUnknown });
        }

    }

    /**
     * Creates a raw complement of the given combined node (AND/OR), "raw" means without implication analysis
     * 
     * @param tree
     * @param node
     * @return complement
     */
    private int createComplementOfCombinedNode(EncodedExpressionTree tree, int node) {
        int[] members = membersOf(node);
        int[] targetMembers = new int[members.length];
        NodeType targetNodeType = getNodeType(node) == NodeType.AND ? NodeType.OR : NodeType.AND;
        for (int idx = 0; idx < members.length; idx++) {
            targetMembers[idx] = createComplementOf(tree, members[idx]);
        }
        return tree.createNode(targetNodeType, targetMembers);
    }

    /**
     * Creates a raw complement of the given node, "raw" means without any implication analysis or optimization
     * <p>
     * Especially in case of nested expressions raw complements may be full of anomalies and thus will need implication resolution.
     * 
     * @param tree
     * @param node
     * @return complement of the node
     */
    public int createComplementOf(EncodedExpressionTree tree, int node) {
        if (getNodeType(node) == NodeType.LEAF) {
            return createComplementOfLeafNode(tree, node);
        }
        else {
            return createComplementOfCombinedNode(tree, node);
        }
    }

    /**
     * This enum defines possible advices on two members if there was any implication or contradiction detected
     */
    public enum Advice {

        RETAIN_BOTH,

        REMOVE_LEFT,

        /**
         * Both candidates could be removed, the left one is recommended
         */
        REMOVE_ANY_LEFT,

        REMOVE_RIGHT,

        /**
         * Both candidates could be removed, the right one is recommended
         */
        REMOVE_ANY_RIGHT,

        REMOVE_BOTH,

        /**
         * Special case <code>arg = 1 OR arg != 1</code> can be replaced with IS NOT UNKNOWN
         */
        REPLACE_BOTH_WITH_IS_NOT_UNKNOWN,

        ALWAYS_TRUE,

        NEVER_TRUE;

    }
}
