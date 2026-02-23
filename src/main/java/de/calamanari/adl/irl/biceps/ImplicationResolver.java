//@formatter:off
/*
 * ImplicationResolver
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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.TimeOut;
import de.calamanari.adl.irl.biceps.ExpressionLogicHelper.Advice;

import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.ALL;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.INVALID;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.NONE;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.createIsUnknownForArgName;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.createIsUnknownForReferencedArgName;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.getNodeType;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isCombinedExpressionId;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isNegatedUnknown;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isReferenceMatch;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isSpecialSet;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isUnknown;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.negate;

/**
 * The {@link ImplicationResolver} cleans-up a couple of implications in a given expression tree.
 * <p>
 * This is a rule-based implication resolver which can run on any tree. Additionally,
 * {@link ImplicationResolver#cleanupImplicationsRecursively(EncodedExpressionTree, CombinedNodeRegistry, int, int[], boolean)} allows to cleanup a single node
 * without touching the tree state. This way, you can leverage caching and advanced analytics.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class ImplicationResolver implements ExpressionTreeProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImplicationResolver.class);

    private static final int[] ALL_ARRAY = new int[] { CoreExpressionCodec.ALL };
    private static final int[] NONE_ARRAY = new int[] { CoreExpressionCodec.NONE };

    private final TimeOut timeout;

    /**
     * @param timeout if null we will use the default: {@link TimeOut#createDefaultTimeOut(String)}
     */
    public ImplicationResolver(TimeOut timeout) {
        this.timeout = timeout == null ? TimeOut.createDefaultTimeOut(ImplicationResolver.class.getSimpleName()) : timeout;
    }

    /**
     * Performs a couple of tests and potentially rebuilds the expression node tree, so its root node may change.
     * <p>
     * <b>Note:</b> For performance reasons this default run skips the expensive advanced analysis. If you need caching or combined complement analysis, please
     * check the documentation of {@link #cleanupImplications(EncodedExpressionTree, int, boolean)}.
     * 
     * @param tree
     */
    @Override
    public void process(EncodedExpressionTree tree) {

        int rootNode = tree.getRootNode();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("process BEFORE: {}", tree.createDebugString(rootNode));
        }
        timeout.assertHaveTime();

        int rootNodeUpd = cleanupImplications(tree, rootNode);

        boolean modified = (rootNodeUpd != rootNode);

        rootNode = rootNodeUpd;

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("process AFTER: {}{}", (modified ? "*" : " "), tree.createDebugString(rootNode));
        }

        tree.setRootNode(rootNode);

        tree.getMemberArrayRegistry().triggerHousekeeping(rootNode);

    }

    /**
     * This method runs the default cleanup (no caching, no advanced analytics) on a particular node to produce a simpler node if possible.
     * 
     * @param tree
     * @param node to be analyzed
     * @return node or optimized node
     */
    public int cleanupImplications(EncodedExpressionTree tree, int node) {
        return cleanupImplications(tree, node, false);
    }

    /**
     * This method runs the cleanup on a particular node to produce a simpler node if possible.
     * <p>
     * There are ways to configure the analysis process to either run a very strict analysis (time-consuming) or sacrifice precision for the purpose of speed.
     * 
     * @param tree
     * @param node to be analyzed
     * @param enforceCombinedComplementAnalysis run <b>expensive</b> combined complement analysis on OR-nodes
     * @return node or optimized node
     */
    public int cleanupImplications(EncodedExpressionTree tree, int node, boolean enforceCombinedComplementAnalysis) {
        return cleanupImplicationsRecursively(tree, node, enforceCombinedComplementAnalysis);
    }

    /**
     * Decomposes the tree top-down to detect and resolve implications bottom-up. The operations gets applied until there is no change anymore.
     * 
     * @param tree
     * @param node
     * @param enforceCombinedComplementAnalysis
     * @return the given node if not changed, otherwise replacement node
     */
    private int cleanupImplicationsRecursively(EncodedExpressionTree tree, int node, boolean enforceCombinedComplementAnalysis) {

        boolean modified = false;

        do {

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("cleanupImplicationsRecursively BEFORE: {}", tree.createDebugString(node));
            }

            timeout.assertHaveTime();

            int nodeUpd = cleanupImplicationsRecursivelySingleIteration(tree, node, MemberUtils.EMPTY_MEMBERS, enforceCombinedComplementAnalysis);
            modified = (nodeUpd != node);
            node = nodeUpd;

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("cleanupImplicationsRecursively AFTER: {}{}", (modified ? "*" : " "), tree.createDebugString(node));
            }

        } while (modified);
        return node;
    }

    /**
     * Decomposes the tree top-down to detect and resolve implications bottom-up (single run).
     * 
     * @param tree
     * @param node
     * @param assumptions list of encoded expressions we assume to be true for reduction of inner conditions
     * @param enforceCombinedComplementAnalysis
     * @return the given node if not changed, otherwise new entry node
     */
    private int cleanupImplicationsRecursivelySingleIteration(EncodedExpressionTree tree, int node, int[] assumptions,
            boolean enforceCombinedComplementAnalysis) {

        switch (getNodeType(node)) {
        case AND:
            return cleanupImplicationsInAndParent(tree, node, assumptions, enforceCombinedComplementAnalysis);
        case OR:
            return cleanupImplicationsInOrParent(tree, node, assumptions, enforceCombinedComplementAnalysis);
        // $CASES-OMITTED$
        default:
            return cleanupImplicationsOnLeaf(tree, NodeType.AND, node, assumptions);
        }
    }

    /**
     * This method applies assumptions top-down to every node. An assumption is any <i>true condition</i> we know about because of an enclosing AND. These facts
     * we collect recursively.
     * <p>
     * Example: <code>a = 1 AND (b = 3 OR (c = 4 AND a != 1))</code>, here we know that <code>a = 1</code>, so the term <code>(c = 4 AND a != 1)</code> can
     * never become true.
     * 
     * @param tree
     * @param parentNodeType
     * @param node
     * @param assumptions
     * @return replaced node or the node if it was not updated
     */
    private int cleanupImplicationsRecursively(EncodedExpressionTree tree, NodeType parentNodeType, int node, int[] assumptions,
            boolean enforceCombinedComplementAnalysis) {

        switch (getNodeType(node)) {
        case AND:
            return cleanupImplicationsInAndParent(tree, node, assumptions, enforceCombinedComplementAnalysis);
        case OR:
            return cleanupImplicationsInOrParent(tree, node, assumptions, enforceCombinedComplementAnalysis);
        // $CASES-OMITTED$
        default:
            return cleanupImplicationsOnLeaf(tree, parentNodeType, node, assumptions);
        }
    }

    /**
     * Applies the assumptions on the given leave inside the parent of the given type
     * 
     * @param tree
     * @param parentNodeType
     * @param leaf
     * @param assumptions
     * @return replaced node or the node if it was not updated
     */
    private int cleanupImplicationsOnLeaf(EncodedExpressionTree tree, NodeType parentNodeType, int leaf, int[] assumptions) {
        switch (parentNodeType) {
        case AND:
            return cleanupImplicationsOnLeafInsideAnd(tree, leaf, assumptions);
        case OR:
            return cleanupImplicationsOnLeafInsideOr(tree, leaf, assumptions);
        // $CASES-OMITTED$
        default:
        }
        return leaf;
    }

    /**
     * Deals with a leaf inside an enclosing AND to decide about the consequences of the assumptions on that leaf
     * 
     * @param tree
     * @param leaf
     * @param assumptions
     * @return replaced node or the node if it was not updated
     */
    private int cleanupImplicationsOnLeafInsideAnd(EncodedExpressionTree tree, int leaf, int[] assumptions) {
        for (int assumption : assumptions) {
            Advice advice = tree.getLogicHelper().checkImplications(NodeType.AND, assumption, leaf);

            // Note: REMOVE_LEFT is missing below because it says "the leaf we investigate implies the assumption"
            // This information does not help optimize the leaf.

            switch (advice) {
            case ALWAYS_TRUE, REMOVE_RIGHT, REMOVE_ANY_RIGHT, REMOVE_ANY_LEFT:
                return ALL;
            case NEVER_TRUE:
                return NONE;
            // $CASES-OMITTED$
            default:
            }
        }
        return leaf;
    }

    /**
     * Deals with a leaf inside an enclosing OR to decide about the consequences of the assumptions on that leaf
     * 
     * @param tree
     * @param leaf
     * @param assumptions
     * @return replaced node or the node if it was not updated
     */
    private int cleanupImplicationsOnLeafInsideOr(EncodedExpressionTree tree, int leaf, int[] assumptions) {
        for (int assumption : assumptions) {
            timeout.assertHaveTime();

            Advice advice = tree.getLogicHelper().checkImplications(NodeType.AND, assumption, leaf);

            // Note: REMOVE_LEFT is missing below because it says "the leaf we investigate implies the assumption"
            // This information does not help optimize the leaf.

            switch (advice) {
            case ALWAYS_TRUE, REMOVE_RIGHT, REMOVE_ANY_RIGHT, REMOVE_ANY_LEFT:
                return ALL;
            case NEVER_TRUE:
                return NONE;
            case REPLACE_BOTH_WITH_IS_NOT_UNKNOWN: {

                // here we can simplify the leaf (arg IS NOT UNKNOWN is a less strict condition than arg = value)

                int argNameIsNotUnknown = negate(createIsUnknownForArgName(leaf));
                if (isReferenceMatch(leaf)) {
                    int referencedArgNameIsNotUnknown = negate(createIsUnknownForReferencedArgName(leaf));
                    return tree.createNode(NodeType.AND, new int[] { referencedArgNameIsNotUnknown, argNameIsNotUnknown });
                }
                else {
                    return argNameIsNotUnknown;
                }
            }
            // $CASES-OMITTED$
            default:
            }
        }
        return leaf;
    }

    /**
     * This method applies the assumptions on any nested member inside an AND to potentially replace the whole node with a different one.
     * 
     * @param tree
     * @param node
     * @param parentAssumptions
     * @return replaced node or the node if it was not updated
     */
    private int cleanupImplicationsInAndParent(EncodedExpressionTree tree, int node, int[] parentAssumptions, boolean enforceCombinedComplementAnalysis) {
        int[] members = tree.membersOf(node);
        int[] memberAssumptions = createMemberAssumptionsInsideAnd(members, parentAssumptions);
        int[] updatedMembers = MemberUtils.EMPTY_MEMBERS;
        for (int memberIdx = 0; memberIdx < members.length; memberIdx++) {
            timeout.assertHaveTime();

            int member = members[memberIdx];
            if (tree.getLogicHelper().leftCombinedAndContradictsRight(parentAssumptions, member, true)) {
                return NONE;
            }
            else {
                // Due to the way of construction above, the slot with the member assumption for a particular member has the same index as the member
                // To avoid applying a member assumption on itself, we hide it temporarily
                memberAssumptions[memberIdx] = INVALID;
                int updatedMember = cleanupImplicationsRecursively(tree, NodeType.AND, member, prepareAssumptionsInAndParent(tree, memberAssumptions),
                        enforceCombinedComplementAnalysis);
                if (updatedMember == NONE) {
                    return NONE;
                }
                else if (updatedMember == ALL) {
                    updatedMembers = initUpdatedMembersIfRequired(members, updatedMembers);
                    updatedMembers[memberIdx] = INVALID;
                    // in this case we must not restore the assumption because otherwise
                    // conditions could get lost when they imply each other
                }
                else if (updatedMember != member) {
                    updatedMembers = initUpdatedMembersIfRequired(members, updatedMembers);
                    updatedMembers[memberIdx] = updatedMember;
                    // because we changed the member we also must adjust the corresponding assumption
                    // to ensure that follow-up implications still correspond to the current state of the expression
                    memberAssumptions[memberIdx] = updatedMember;
                }
                else {
                    // here we restore the hidden assumption (member did not change)
                    memberAssumptions[memberIdx] = member;
                }
            }
        }
        if (updatedMembers != MemberUtils.EMPTY_MEMBERS) {
            node = tree.createNode(NodeType.AND, updatedMembers);
        }
        return node;
    }

    /**
     * Here we dedup and sort the assumptions followed by a quick contradiction check (if the assumptions already contradict each other, the result can be
     * shorted to NONE)
     * 
     * @param tree
     * @param assumptions
     * @return prepared sorted array
     */
    private int[] prepareAssumptionsInAndParent(EncodedExpressionTree tree, int[] assumptions) {
        assumptions = MemberUtils.sortDistinctMembers(assumptions, true);
        if (tree.getLogicHelper().haveAnySimpleContradictionInAndParent(assumptions)) {
            return NONE_ARRAY;
        }
        return assumptions;
    }

    /**
     * This method creates a new combined array where the new members come first, followed by the parent assumptions
     * 
     * @param members
     * @param parentAssumptions
     * @return combined array
     */
    private int[] createMemberAssumptionsInsideAnd(int[] members, int[] parentAssumptions) {
        int[] combinedAssumptions = Arrays.copyOf(members, members.length + parentAssumptions.length);
        System.arraycopy(parentAssumptions, 0, combinedAssumptions, members.length, parentAssumptions.length);
        return combinedAssumptions;
    }

    /**
     * Here we check the members of an OR side-by-side to detect implications that may render conditions obsolete.
     * 
     * @param tree
     * @param members
     * @return updated members or members (same array instance) if not changed
     */
    private int[] processInterMemberImplicationsInOrParent(EncodedExpressionTree tree, int[] members) {
        int[] updatedMembers = MemberUtils.EMPTY_MEMBERS;
        for (int leftIdx = 0; leftIdx < members.length - 1; leftIdx++) {
            int leftMember = members[leftIdx];
            for (int rightIdx = leftIdx + 1; rightIdx < members.length; rightIdx++) {
                timeout.assertHaveTime();

                int rightMember = members[rightIdx];
                Advice advice = tree.getLogicHelper().checkImplications(NodeType.OR, leftMember, rightMember);
                switch (advice) {
                case ALWAYS_TRUE: {
                    return ALL_ARRAY;
                }
                case NEVER_TRUE: {
                    return NONE_ARRAY;
                }
                case REMOVE_BOTH: {
                    updatedMembers = initUpdatedMembersIfRequired(members, updatedMembers);
                    updatedMembers[leftIdx] = INVALID;
                    updatedMembers[rightIdx] = INVALID;
                    break;
                }
                case REMOVE_LEFT, REMOVE_ANY_LEFT: {
                    updatedMembers = initUpdatedMembersIfRequired(members, updatedMembers);
                    updatedMembers[leftIdx] = INVALID;
                    break;
                }
                case REMOVE_RIGHT, REMOVE_ANY_RIGHT: {
                    updatedMembers = initUpdatedMembersIfRequired(members, updatedMembers);
                    updatedMembers[rightIdx] = INVALID;
                    break;
                }
                case REPLACE_BOTH_WITH_IS_NOT_UNKNOWN: {
                    updatedMembers = initUpdatedMembersIfRequired(members, updatedMembers);
                    updateMembersReplaceBothWithIsNotUnknown(tree, members, updatedMembers, leftIdx, rightIdx);
                    break;
                }
                // $CASES-OMITTED$
                default:
                }
            }
        }
        if (updatedMembers != MemberUtils.EMPTY_MEMBERS) {
            updatedMembers = tree.consolidateMembers(NodeType.OR, updatedMembers);
        }
        else {
            updatedMembers = members;
        }
        return updatedMembers;
    }

    /**
     * This method performs two types of analysis on an OR-combination until there is no further change.
     * <p>
     * First we look at the simpler implications within pairs of members.
     * <p>
     * The second test tries to identify more complex problems to eliminate irrelevant sub-conditions.<br>
     * For example: <code>(a=1 AND b=2) OR (a!=1 AND b=2)</code> means: <code>(a IS NOT UNKNOWN AND b=2)</code>
     * <p>
     * The effort is quite high, but every branch we can eliminate early helps to reduce effort in later transformations.
     * 
     * @param tree
     * @param members
     * @return updated member set or members (unmodified instance)
     */
    private int[] processImplicationsInOrParent(EncodedExpressionTree tree, int[] members) {

        int[] updatedMembers = members;
        boolean modifiedInRun = false;
        do {
            timeout.assertHaveTime();

            modifiedInRun = false;
            int[] updatedMembersInRun = processInterMemberImplicationsInOrParent(tree, updatedMembers);
            if (updatedMembersInRun != updatedMembers) {
                modifiedInRun = true;
                updatedMembers = updatedMembersInRun;
            }

            updatedMembersInRun = processIrrelevantSubConditionsInOrOfAnds(tree, updatedMembers);
            if (updatedMembersInRun != updatedMembers) {
                modifiedInRun = true;
                updatedMembers = updatedMembersInRun;
            }
        } while (modifiedInRun);

        return updatedMembers;
    }

    /**
     * This method handles a special case in an OR:
     * <ul>
     * <li><code>a = 1 OR a != 1</code> can be written as <code>a IS NOT UNKNOWN</code></li>
     * <li><code>a = &#64;other OR a != &#64;other</code> can be written as <code>a IS NOT UNKNOWN AND other IS NOT UNKNOWN</code></li>
     * </ul>
     * Subsequently, this can crack more complex scenarios like <code>a = 1 OR a != 1 OR a IS UNKNOWN</code> which make the state of <code>a</code> pointless.
     * 
     * @param tree
     * @param members
     * @param updatedMembers to be updated
     * @param leftIdx
     * @param rightIdx
     */
    private void updateMembersReplaceBothWithIsNotUnknown(EncodedExpressionTree tree, int[] members, int[] updatedMembers, int leftIdx, int rightIdx) {
        int leftMember = members[leftIdx];
        int argNameIsNotUnknown = negate(createIsUnknownForArgName(leftMember));
        if (isReferenceMatch(leftMember)) {
            int referencedArgNameIsNotUnknown = negate(createIsUnknownForReferencedArgName(leftMember));
            updatedMembers[leftIdx] = tree.createNode(NodeType.AND, new int[] { referencedArgNameIsNotUnknown, argNameIsNotUnknown });
        }
        else {
            updatedMembers[leftIdx] = argNameIsNotUnknown;
        }
        updatedMembers[rightIdx] = INVALID;

    }

    /**
     * This method applies the assumptions on any nested member inside an AND to potentially replace the whole node with a different one.
     * 
     * @param tree
     * @param node
     * @param parentAssumptions
     * @param enforceCombinedComplementAnalysis run the expensive complement analysis
     * @return replaced node or the node if it was not updated
     */
    private int cleanupImplicationsInOrParent(EncodedExpressionTree tree, int node, int[] parentAssumptions, boolean enforceCombinedComplementAnalysis) {

        final int[] membersBefore = tree.membersOf(node);
        int[] members = membersBefore;
        int[] updatedMembers = processImplicationsInOrParent(tree, members);
        if (updatedMembers.length == 1 && updatedMembers[0] == ALL) {
            return ALL;
        }
        else if (updatedMembers.length == 1 && updatedMembers[0] == NONE) {
            return NONE;
        }
        else if (updatedMembers != members) {
            members = Arrays.copyOf(updatedMembers, updatedMembers.length);
        }
        else {
            updatedMembers = MemberUtils.EMPTY_MEMBERS;
        }
        if (isOrImpliedByParentAssumptions(tree, node, members, parentAssumptions)) {
            return ALL;
        }

        updatedMembers = cleanupImplicationsInOrParent(tree, members, updatedMembers, parentAssumptions, enforceCombinedComplementAnalysis);
        if (updatedMembers != MemberUtils.EMPTY_MEMBERS) {
            node = tree.createNode(NodeType.OR, updatedMembers);
        }
        return node;
    }

    /**
     * Core implementation of the OR-cleanup
     * 
     * @param tree
     * @param members
     * @param updatedMembers
     * @param parentAssumptions
     * @param enforceCombinedComplementAnalysis run the expensive complement analysis
     * @return updated members
     */
    private int[] cleanupImplicationsInOrParent(EncodedExpressionTree tree, int[] members, int[] updatedMembers, int[] parentAssumptions,
            boolean enforceCombinedComplementAnalysis) {

        // The reason we have two sets of assumptions, one for the combined members and the other for the leafs
        // is that sometimes we can derive an assumption applicable to combined members from two leaves
        // Example: a = 1 OR a IS NULL OR (b = 2 AND STRICT a != 1)
        // Here, the complement of (a = 1 OR a IS NULL) is STRICT a != 1
        // When (a = 1 OR a IS NULL) is true, we don't need (b = 2 AND STRICT a != 1), because the expression is anyway true
        // However, when (a = 1 OR a IS NULL) is false then STRICT a != 1 must be true
        // Conclusion: a = 1 OR a IS NULL OR (b = 2 AND STRICT a != 1) collapses to a = 1 OR a IS NULL OR b = 2
        // The problem with these complements derived from more than one leaf is that we cannot hide them per leaf anymore.
        // Thus, we separate assumptions applied to combined members (AND/OR) strictly from the ones applicable to leaves.
        // Assumptions for leaves always stem from a single leaf, so it can never happen that we apply an assumption
        // that was somehow derived from a member to that same member.

        int[] combinedMemberAssumptions = createAssumptionsForCombinedMembersInsideOr(tree, members, parentAssumptions, enforceCombinedComplementAnalysis);
        int[] leafMemberAssumptions = createAssumptionsForLeafMembersInsideOr(tree, members, parentAssumptions, enforceCombinedComplementAnalysis);

        for (int memberIdx = 0; memberIdx < members.length; memberIdx++) {
            timeout.assertHaveTime();

            int member = members[memberIdx];

            int[] memberAssumptions = selectApplicableAssumptionsInOrParent(member, combinedMemberAssumptions, leafMemberAssumptions);

            // Due to the way of construction above, the slot with the member assumption for a particular member has the same index as the member
            // To avoid applying a member assumption on itself, we temporarily hide it:
            int hiddenAssumption = memberAssumptions[memberIdx];
            memberAssumptions[memberIdx] = INVALID;

            int[] preparedAssumptions = prepareAssumptionsInOrParent(tree, memberAssumptions, enforceCombinedComplementAnalysis);

            if (preparedAssumptions == NONE_ARRAY || tree.getLogicHelper().leftCombinedAndContradictsRight(preparedAssumptions, member, true)) {
                updatedMembers = initUpdatedMembersIfRequired(members, updatedMembers);
                updatedMembers[memberIdx] = INVALID;
                // drop the corresponding assumption (member is gone)
                combinedMemberAssumptions[memberIdx] = INVALID;
                leafMemberAssumptions[memberIdx] = INVALID;
            }
            else {
                int updatedMember = cleanupImplicationsRecursively(tree, NodeType.OR, member, preparedAssumptions, enforceCombinedComplementAnalysis);
                if (updatedMember == NONE) {
                    updatedMembers = initUpdatedMembersIfRequired(members, updatedMembers);
                    updatedMembers[memberIdx] = INVALID;
                    // drop the corresponding assumption (member is gone)
                    combinedMemberAssumptions[memberIdx] = INVALID;
                    leafMemberAssumptions[memberIdx] = INVALID;
                }
                else if (updatedMember == ALL) {
                    updatedMembers = new int[] { ALL };
                    break;
                }
                else if (updatedMember != member) {
                    updatedMembers = initUpdatedMembersIfRequired(members, updatedMembers);
                    updatedMembers[memberIdx] = updatedMember;
                    // the member has changed, thus we must update the assumption as well
                    combinedMemberAssumptions[memberIdx] = createAssumptionForMemberInsideOr(tree, updatedMembers, updatedMember,
                            enforceCombinedComplementAnalysis);
                }
                else {
                    // restore the hidden assumption (member unchanged)
                    memberAssumptions[memberIdx] = hiddenAssumption;
                }
            }
        }
        return updatedMembers;
    }

    /**
     * We use different assumption arrays for leaves vs. combined members.
     * 
     * @param member
     * @param combinedMemberAssumptions
     * @param leafMemberAssumptions
     * @return parentAssumptions for leaves and the combinedMemberAssumptions for any AND/OR member
     */
    private int[] selectApplicableAssumptionsInOrParent(int member, int[] combinedMemberAssumptions, int[] leafMemberAssumptions) {
        return isCombinedExpressionId(member) ? combinedMemberAssumptions : leafMemberAssumptions;
    }

    /**
     * The preparation method can be either quick (detect contradictions) or complex with consolidation. Consolidation means running the full process of
     * creating an AND-node with optimization to ensure the assumptions array is optimal (lean).
     * <p>
     * Because the consolidation is quite expensive we control it by parameter.
     * <p>
     * In either case the returned array of assumptions will be sorted and free of duplicates.
     * 
     * @param tree
     * @param assumptions (won't be modified)
     * @param consolidate run expensive assumption consolidation
     * @return assumptions, consolidated assumptions or {@link MemberUtils#EMPTY_MEMBERS} to indicate there are no applicable assumptions
     */
    private int[] prepareAssumptionsInOrParent(EncodedExpressionTree tree, int[] assumptions, boolean consolidate) {
        if (consolidate) {
            int andNode = tree.createNode(NodeType.AND, Arrays.copyOf(assumptions, assumptions.length));
            int node = cleanupImplicationsRecursively(tree, andNode, false);
            if (getNodeType(node) == NodeType.AND) {
                assumptions = tree.membersOf(node);
            }
            else if (node == NONE) {
                return NONE_ARRAY;
            }
            else if (node == ALL) {
                // <ALL> AND "some condition" does not help simplify "some condition"
                return MemberUtils.EMPTY_MEMBERS;
            }
            else {
                assumptions = new int[] { node };
            }
        }
        else {
            assumptions = MemberUtils.sortDistinctMembers(assumptions, true);
            if (tree.getLogicHelper().haveAnySimpleContradictionInAndParent(assumptions)) {
                return NONE_ARRAY;
            }
        }
        return assumptions;
    }

    /**
     * This method fills an analysis gap: sometimes one of the parent assumptions <i>equals</i> or <i>implies</i> the complete OR, then the detail analysis
     * would fail. Thus, we check if this is the case before going into the details.
     * 
     * @param tree
     * @param orNode
     * @param orMembers
     * @param parentAssumptions
     * @return true if the given OR is anyway implied by the given assumptions
     */
    private boolean isOrImpliedByParentAssumptions(EncodedExpressionTree tree, int orNode, int[] orMembers, int[] parentAssumptions) {
        for (int assumption : parentAssumptions) {
            if (assumption == orNode || (getNodeType(assumption) == NodeType.OR
                    && MemberUtils.sortedLeftMembersContainSortedRightMembers(orMembers, tree.membersOf(assumption)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns the assumptions which can safely be applied to leaf members inside an OR, because they stem from a single other OR-member.
     * <p>
     * The created array has always the same length, the combined length of both given arrays. New member assumptions precede the parent assumptions. This way,
     * we can later easily hide a member-related assumption by using its member index.<br>
     * Any member we cannot create an assumptions for (or we want to hide) gets an INVALID assumption.
     * 
     * @param tree
     * @param members
     * @param parentAssumptions
     * @param enforceCombinedComplementAnalysis if true, we create expensive complements for commbined members
     * @return array with assumptions or INVALIDs at the corresponding positions
     */
    private int[] createAssumptionsForLeafMembersInsideOr(EncodedExpressionTree tree, int[] members, int[] parentAssumptions,
            boolean enforceCombinedComplementAnalysis) {

        int[] combinedAssumptions = new int[members.length + parentAssumptions.length];
        System.arraycopy(parentAssumptions, 0, combinedAssumptions, members.length, parentAssumptions.length);

        for (int idx = 0; idx < members.length; idx++) {
            timeout.assertHaveTime();
            int member = members[idx];
            if (isUnknown(member) || isNegatedUnknown(member)) {
                combinedAssumptions[idx] = negate(member);
            }
            else if (enforceCombinedComplementAnalysis && getNodeType(member) != NodeType.LEAF) {
                combinedAssumptions[idx] = this.cleanupImplicationsRecursively(tree, tree.getLogicHelper().createComplementOf(tree, member), false);
            }
            else {
                combinedAssumptions[idx] = INVALID;
            }
        }
        return combinedAssumptions;
    }

    /**
     * This method derives special complement assumptions from members within an OR.
     * <p>
     * In general if you have a number of conditions combined with OR, and you evaluate left-to-right, you can do a short-circuit-evaluation. The first
     * condition that matches is fine, forget about the trailing. This is interesting, because it means for any two given conditions <i>left</i> and
     * <i>right</i> that <i>right</i> would only be relevant (subject to testing) if the the <i>complement of left</i> is <b>true</b>. <br>
     * This method follows this idea and creates <i>complement assumptions</i> that can be applied to the other conditions to potentially simplify or even
     * contradict them early.
     * <p>
     * <b>Example 1:</b> <code>arg IS NOT UNKNOWN OR (arg IS UNKNOWN AND b = 2)</code> is <b>true</b> if <code>arg IS NOT UNKNOWN</code> but also <b>true</b> if
     * <code>arg IS UNKNOWN</code> and at the same time <code>b = 2</code>, so the whole condition collapses into <code>arg IS NOT UNKNOWN OR b = 2</code>.
     * <p>
     * <b>Example 2:</b> <code>arg != 3 OR b IS UNKNOWN OR b = 2 OR (arg = 3 AND b != 2)</code> can be simplified because <code>b IS UNKNOWN OR b = 2</code> is
     * the <i>complement</i> of <code>b != 2</code>, so we can take <code>b != 2</code> as a valid assumption when analyzing the other condition in the same OR
     * <code>(arg = 3 AND b != 2)</code>.<br>
     * Reason: if the complement of this assumption is true, the OR anyway becomes true. With this assumption the sub-condition collapses to
     * <code>arg = 3</code> and consequently the OR collapses to <code>arg != 3 OR b IS UNKNOWN OR b = 2 OR arg = 3</code>, finally:
     * <code>arg IS NOT UNKNOWN OR b IS UNKNOWN OR b = 2</code>
     * <p>
     * <b>Example 3:</b> <code>arg = 1 OR (b = 2 AND (arg != 1 OR arg IS UNKNOWN))</code> can be simplified because <code>(arg != 1 OR arg IS UNKNOWN)</code> is
     * the <i>complement</i> of <code>arg = 1</code>. When we take this complement as an assumption, the second condition of the OR
     * (<code>(b = 2 AND (arg != 1 OR arg IS UNKNOWN))</code>) collapses to <code>b = 2</code>, the expression gets shortened to <code>arg = 1 OR b = 2</code>.
     * <p>
     * By default we do this only for leaves to limit the effort. However, the control flag enforces the <b>expensive</b> <i>combined complement analysis</i>.
     * <p>
     * <b>Important:</b>All the parent assumptions come <i>after</i> the member assumptions in the returned array, so that an original member index is always
     * equal to the index of the assumption.
     * 
     * @param tree
     * @param members
     * @param currentMember to be skipped
     * @param parentAssumptions base assumptions from higher ANDs
     * @param enforceCombinedComplementAnalysis also create complements for combined members (expensive)
     * @return assumptions for the check of the current member
     */
    private int[] createAssumptionsForCombinedMembersInsideOr(EncodedExpressionTree tree, int[] members, int[] parentAssumptions,
            boolean enforceCombinedComplementAnalysis) {

        int[] combinedAssumptions = new int[members.length + parentAssumptions.length];
        System.arraycopy(parentAssumptions, 0, combinedAssumptions, members.length, parentAssumptions.length);

        for (int idx = 0; idx < members.length; idx++) {
            timeout.assertHaveTime();
            combinedAssumptions[idx] = createAssumptionForMemberInsideOr(tree, members, members[idx], enforceCombinedComplementAnalysis);
        }
        return combinedAssumptions;
    }

    /**
     * Creates a single complement for the given member to be used as an assumption for the other members in the same or.
     * 
     * @param tree
     * @param members
     * @param member
     * @param enforceCombinedComplementAnalysis also create complements for combined members (expensive)
     * @return complement assumption
     */
    private int createAssumptionForMemberInsideOr(EncodedExpressionTree tree, int[] members, int member, boolean enforceCombinedComplementAnalysis) {
        if (isUnknown(member) || isNegatedUnknown(member)) {
            return negate(member);
        }
        else if (getNodeType(member) == NodeType.LEAF) {
            return createComplementForLeafInsideOr(tree, members, member);
        }
        else if (enforceCombinedComplementAnalysis) {
            return this.cleanupImplicationsRecursively(tree, tree.getLogicHelper().createComplementOf(tree, member), false);
        }
        return INVALID;
    }

    /**
     * This method handles a special case in an OR of ANDs:
     * <ul>
     * <li><code>a = 1 OR a != 1</code> can be written as <code>a IS NOT UNKNOWN</code></li>
     * <li><code>a = &#64;other OR a != &#64;other</code> can be written as <code>a IS NOT UNKNOWN AND other IS NOT UNKNOWN</code></li>
     * </ul>
     * 
     * @param tree
     * @param members
     * @return updated members or members (unmodified array instance)
     */
    private int[] processIrrelevantSubConditionsInOrOfAnds(EncodedExpressionTree tree, int[] members) {

        if (members.length < 2) {
            return members;
        }

        boolean modified = false;
        int[] updatedMembers = Arrays.copyOf(members, members.length);

        for (int leftIdx = 0; leftIdx < members.length; leftIdx++) {
            for (int rightIdx = leftIdx + 1; rightIdx < members.length; rightIdx++) {
                timeout.assertHaveTime();

                modified = processIrrelevantSubConditionsInOrOfAnds(tree, updatedMembers, members[leftIdx], leftIdx, members[rightIdx], rightIdx) || modified;
            }
        }
        if (modified) {
            updatedMembers = tree.consolidateMembers(NodeType.OR, updatedMembers);
        }
        else {
            updatedMembers = members;
        }
        return updatedMembers;
    }

    /**
     * Here we look at two members of the same parent-array to do a deeper analysis if sub-conditions can be removed.
     * <p>
     * This method directly operates on the given parentMembers array and modifies it.
     * 
     * @param tree
     * @param parentMembers may be modified
     * @param leftMember
     * @param leftIdx
     * @param rightMember
     * @param rightIdx
     * @return true if the parentMembers array was modified
     */
    private boolean processIrrelevantSubConditionsInOrOfAnds(EncodedExpressionTree tree, int[] parentMembers, int leftMember, int leftIdx, int rightMember,
            int rightIdx) {

        boolean modified = false;
        NodeType leftNodeType = getNodeType(leftMember);
        NodeType rightNodeType = getNodeType(rightMember);

        if (isCombinedExpressionId(leftMember) && isCombinedExpressionId(rightMember) && leftNodeType == NodeType.AND && rightNodeType == NodeType.AND) {
            int[] leftSubMembers = tree.membersOf(leftMember);
            int[] rightSubMembers = tree.membersOf(rightMember);
            if (leftSubMembers.length == rightSubMembers.length) {
                modified = processIrrelevantSubConditionsInOrOfAnds(tree, parentMembers, leftSubMembers, leftIdx, rightSubMembers, rightIdx) || modified;
            }
        }
        return modified;
    }

    /**
     * This method processes the member sets of two ANDs (same length) to detect irrelevant sub-conditions. <br>
     * If successful, the parentMembers-array gets updated.
     * 
     * @param tree
     * @param parentMembers may be modified
     * @param leftAndMembers
     * @param leftParentIdx
     * @param rightAndMembers
     * @param rightParentIdx
     * @return true if the parentMembers array was modified
     */
    private boolean processIrrelevantSubConditionsInOrOfAnds(EncodedExpressionTree tree, int[] parentMembers, int[] leftAndMembers, int leftParentIdx,
            int[] rightAndMembers, int rightParentIdx) {

        boolean modified = false;
        for (int leftAndMemberIdx = 0; leftAndMemberIdx < leftAndMembers.length; leftAndMemberIdx++) {
            timeout.assertHaveTime();

            int leftAndMember = leftAndMembers[leftAndMemberIdx];
            if (getNodeType(leftAndMember) == NodeType.LEAF && !isSpecialSet(leftAndMember)
                    && checkOtherwiseSameConditions(leftAndMembers, leftAndMember, rightAndMembers, negate(leftAndMember))) {
                modified = true;
                int consolidated = 0;
                if (isUnknown(leftAndMember) || isNegatedUnknown(leftAndMember)) {
                    // irrelevant sub condition
                    // Example: (arg IS UNKNOWN AND b=1) OR (arg IS NOT UNKNOWN AND b=1) => b=1
                    consolidated = tree.createNode(NodeType.AND, MemberUtils.copySkipMember(leftAndMembers, leftAndMember));
                }
                else if (isReferenceMatch(leftAndMember)) {
                    // widened sub-condition
                    // Example: (a=@other AND b=1) OR (a != @other AND b=1) => (a IS NOT UNKNOWN AND other IS NOT UNKNOWN AND b=1)
                    int[] updatedAndMembers = Arrays.copyOf(leftAndMembers, leftAndMembers.length + 1);
                    updatedAndMembers[leftAndMemberIdx] = negate(createIsUnknownForArgName(leftAndMember));
                    updatedAndMembers[updatedAndMembers.length - 1] = negate(createIsUnknownForReferencedArgName(leftAndMember));
                    consolidated = tree.createNode(NodeType.AND, updatedAndMembers);
                }
                else {
                    // simplified sub-condition
                    // Example: (a=1 AND b=1) OR (a != 1 AND b=1) => (a IS NOT UNKNOWN AND b=1)
                    int[] updatedAndMembers = Arrays.copyOf(leftAndMembers, leftAndMembers.length);
                    updatedAndMembers[leftAndMemberIdx] = negate(createIsUnknownForArgName(leftAndMember));
                    consolidated = tree.createNode(NodeType.AND, updatedAndMembers);
                }
                parentMembers[leftParentIdx] = consolidated;
                parentMembers[rightParentIdx] = consolidated;
            }
        }

        return modified;
    }

    /**
     * This method answers the question if two complex conditions are the same when we skip the specified elements.<br>
     * Tests whether the <i>remaining</i> conditions are the same after removing the skip-members on both sides.
     * <p>
     * If so we can concentrate on the given pair of sub-conditions to detect irrelevant ones.
     * 
     * @param leftMembers
     * @param leftSkipMember
     * @param rightMembers
     * @param rightSkipMember
     * @return true if the remaining conditions are equal
     */
    private boolean checkOtherwiseSameConditions(int[] leftMembers, int leftSkipMember, int[] rightMembers, int rightSkipMember) {

        if (leftMembers.length != rightMembers.length) {
            return false;
        }
        int len = leftMembers.length;
        int leftIdx = 0;
        int rightIdx = 0;

        while (leftIdx < len && rightIdx < len) {

            int leftMember = leftMembers[leftIdx];
            int rightMember = rightMembers[rightIdx];

            if (leftMember == leftSkipMember) {
                leftIdx++;
            }
            else if (rightMember == rightSkipMember) {
                rightIdx++;
            }
            else if (rightMember == leftMember) {
                leftIdx++;
                rightIdx++;
            }
            else {
                return false;
            }

        }
        return (leftIdx == rightIdx);
    }

    /**
     * To avoid unnecessary copies we delay the creation of a member array copy.
     * 
     * @param members
     * @param updatedMembers initially provide empty array (not null)
     * @return copy of members if updatedMembers was null, otherwise updatedMembers
     */
    private int[] initUpdatedMembersIfRequired(int[] members, int[] updatedMembers) {
        return updatedMembers.length == 0 ? Arrays.copyOf(members, members.length) : updatedMembers;
    }

    /**
     * This method determines for a given member the logical complement (only for leaves).
     * <p>
     * Example 1: <code>a = 1 OR a IS UNKNOWN</code> In this case we can easily derive the complement <code>(a != 1)</code> to leverage it as an assumption for
     * further analysis and simplification of the other OR-members.<br>
     * The point here is that if the complement of <code>a != 1</code> is anyway covered by the surrounding OR (will become true) then we can take
     * <code>a != 1</code> as an assumption to reduce the complexity of any other OR-member.<br>
     * Example 2: <code>a != 1 OR a IS UNKNOWN</code> makes <code>(a = 1)</code> a cheap simple complement <br>
     * Example 3: <code>a = &#64;other OR a IS UNKNOWN OR other IS UNKNOWN</code> makes <code>(a != &#64;other)</code> a cheap simple complement Example 4:
     * <code>a = 1 OR (b = 1 AND (a != 1 OR a IS UNKNOWN))</code> makes <code>(a != 1 <b>OR</b> a IS UNKNOWN)</code> the complement which can be used to reduce
     * the second condition in the OR. The simplification would be <code>a = 1 OR b = 1</code>.
     * 
     * @param tree
     * @param members
     * @param member to create the complement for
     * @return complement or INVALID if not applicable
     */
    private int createComplementForLeafInsideOr(EncodedExpressionTree tree, int[] members, int member) {
        if (member == INVALID || isCombinedExpressionId(member) || isUnknown(member) || isNegatedUnknown(member) || isSpecialSet(member)) {
            return INVALID;
        }

        int negatedMember = negate(member);
        int unknownArgName = createIsUnknownForArgName(member);
        int unknownReferenceArgName = isReferenceMatch(member) ? createIsUnknownForReferencedArgName(member) : NONE;

        if (Arrays.binarySearch(members, unknownArgName) > -1) {
            // the IS UNKNOWN is in the outer OR, so this case we don't need to consider
            unknownArgName = NONE;
        }

        if (unknownReferenceArgName != NONE && Arrays.binarySearch(members, unknownReferenceArgName) > -1) {
            // the IS UNKNOWN is in the outer OR, so this case we don't need to consider
            unknownReferenceArgName = NONE;
        }

        if (unknownArgName == NONE && unknownReferenceArgName == NONE) {
            return negatedMember;
        }
        else {
            return createCombinedComplementOfLeaf(tree, negatedMember, unknownArgName, unknownReferenceArgName);
        }

    }

    /**
     * In case the complement is complex this method creates an OR
     * 
     * @param tree
     * @param negatedMember
     * @param unknownArgName
     * @param unknownReferenceArgName
     * @return combined node (OR)
     */
    private int createCombinedComplementOfLeaf(EncodedExpressionTree tree, int negatedMember, int unknownArgName, int unknownReferenceArgName) {
        int len = 2;
        if (unknownArgName != NONE && unknownReferenceArgName != NONE) {
            len = 3;
        }
        int[] conditionMembers = new int[len];
        int idx = 0;
        conditionMembers[idx] = negatedMember;
        idx++;
        if (unknownArgName != NONE) {
            conditionMembers[idx] = unknownArgName;
            idx++;
        }
        if (unknownReferenceArgName != NONE) {
            conditionMembers[idx] = unknownReferenceArgName;
        }
        return tree.createNode(NodeType.OR, conditionMembers);
    }

}
