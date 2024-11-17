//@formatter:off
/*
 * OrOfAndNormalizer
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

import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.INVALID;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.getNodeType;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isCombinedExpressionId;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.TimeOut;

/**
 * The {@link OrOfAndNormalizer} brings the expression tree in such a form that the result is either a LEAF or an AND composed of LEAFs or an OR that can
 * contain ANDs composed of LEAFs or LEAFs.
 * <p/>
 * In other words the result expression has at most 2 levels, and if there are two levels, the top-level will be an OR.
 * <p/>
 * Due to sorting this operation is deterministic. However, it can take quite long (combinatoric explosion), thus it is recommended to set a timeout.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class OrOfAndNormalizer implements ExpressionTreeProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrOfAndNormalizer.class);

    /**
     * To reduce the amount of unnecessary branches, from time to time we check for implications.<br/>
     * As this cleanup is expensive, there is a threshold involved. We won't cleanup a smaller group, and the maximum cleanup size has the same limit. Should
     * the normalization <i>run away</i> and produce way too many members then we must deal with the problem otherwise.
     */
    private static final int INTERMEDIATE_CLEANUP_THRESHOLD = 5_000;

    private final TimeOut timeout;

    private final ImplicationResolver implicationResolver;

    /**
     * Creates a new instance with the given resolver and reusing the given timeout
     * 
     * @param implicationResolver
     * @param timeout (null means default, see {@link TimeOut#createDefaultTimeOut(String)})
     */
    public OrOfAndNormalizer(ImplicationResolver implicationResolver, TimeOut timeout) {
        this.timeout = timeout == null ? TimeOut.createDefaultTimeOut(OrOfAndNormalizer.class.getSimpleName()) : timeout;
        this.implicationResolver = implicationResolver;
    }

    /**
     * Converts the given tree into OR-of-AND form
     * 
     * @param tree
     */
    @Override
    public void process(EncodedExpressionTree tree) {

        int rootNode = tree.getRootNode();

        rootNode = normalize(tree, rootNode);

        tree.setRootNode(rootNode);
        tree.getMemberArrayRegistry().triggerHousekeeping(rootNode);

    }

    /**
     * Normalizes the tree beginning with the given node top-down-bottom-up, means we first normalize the members before normalizing the parent.
     * 
     * @param tree
     * @param node
     * @return node or updated node
     */
    private int normalize(EncodedExpressionTree tree, int node) {
        if (!isCombinedExpressionId(node)) {
            return node;
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("normalize BEFORE: {}", tree.createDebugString(node));
        }

        NodeType nodeType = getNodeType(node);

        int[] members = tree.membersOf(node);

        int[] updatedMembers = normalizeMembers(tree, nodeType, members);

        if (updatedMembers.length == 1) {
            return updatedMembers[0];
        }

        if (nodeType == NodeType.AND && MemberUtils.containsAnyMemberOfTypeRecursively(tree.getMemberArrayRegistry(), updatedMembers, NodeType.OR)) {

            // potentially, we have an unwanted AND of OR, which needs to be cracked by multiplication

            updatedMembers = processMultiplication(tree, updatedMembers);
            nodeType = NodeType.OR;

        }

        updatedMembers = tree.consolidateMembers(nodeType, updatedMembers);

        int updatedNode = node;

        if (!Arrays.equals(updatedMembers, members)) {
            updatedNode = tree.createNode(nodeType, updatedMembers);
            updatedNode = implicationResolver.cleanupImplications(tree, updatedNode);
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("normalize AFTER: {}{}", ((updatedNode != node) ? "*" : " "), tree.createDebugString(updatedNode));
        }

        return updatedNode;
    }

    /**
     * After detecting an unwanted AND of OR this method starts resolving the problem by multiplying the members
     * 
     * @param tree
     * @param members
     * @return updated members, so that the parent is either a leaf or an OR
     */
    private int[] processMultiplication(EncodedExpressionTree tree, int[] members) {
        GrowingIntArray updatedMembers = new GrowingIntArray(members.length * 2);
        multiplyMembers(tree, members, updatedMembers);
        return updatedMembers.toArray();
    }

    /**
     * This method ensures that the members are normalized first (bottom-up) before dealing with the parent.
     * <p/>
     * After this method has run, the members are all ORs or ANDs (composed of leaves) or leaves.
     * 
     * @param tree
     * @param nodeType
     * @param members
     * @return normalized members
     */
    private int[] normalizeMembers(EncodedExpressionTree tree, NodeType nodeType, int[] members) {
        int[] updatedMembers = null;

        for (int idx = 0; idx < members.length; idx++) {
            timeout.assertHaveTime();

            int member = members[idx];
            int updatedMember = normalize(tree, member);
            if (updatedMember != member) {
                updatedMembers = (updatedMembers == null) ? Arrays.copyOf(members, members.length) : updatedMembers;
                updatedMembers[idx] = updatedMember;
            }
        }
        if (updatedMembers != null) {
            updatedMembers = tree.consolidateMembers(nodeType, updatedMembers);
        }
        else {
            updatedMembers = members;
        }
        return updatedMembers;

    }

    /**
     * Multiplies AND-of-OR into OR-of-AND if required, and fills the updatedMembers array subsequently
     * 
     * @param tree
     * @param members
     * @param updatedMembers
     */
    private void multiplyMembers(EncodedExpressionTree tree, int[] members, GrowingIntArray updatedMembers) {
        int prevMemberCount = updatedMembers.size();
        for (int member : members) {
            timeout.assertHaveTime();

            NodeType nodeType = getNodeType(member);
            switch (nodeType) {
            case LEAF:
                multiplyLeaf(tree, member, updatedMembers);
                break;
            case AND:
                multiplyAnd(tree, member, updatedMembers);
                break;
            case OR:
                multiplyOr(tree, member, updatedMembers);
                break;
            }
            prevMemberCount = triggerImplicationCleanup(tree, updatedMembers, prevMemberCount);
        }
    }

    /**
     * Called to reduce the number of bad branches (otherwise piling up through multiplication)
     * 
     * @param tree
     * @param updatedMembers
     * @param prevMemberCount
     * @return new member count to delay the next cleanup
     */
    private int triggerImplicationCleanup(EncodedExpressionTree tree, GrowingIntArray updatedMembers, int prevMemberCount) {
        if (updatedMembers.size() - prevMemberCount > INTERMEDIATE_CLEANUP_THRESHOLD) {
            cleanupImplications(tree, updatedMembers);
            prevMemberCount = updatedMembers.size();
        }
        return prevMemberCount;
    }

    /**
     * Performs an implications check on the accumulated members in the OR to reduce the number of members early.
     * <p/>
     * <b>Note:</b> This operation is so expensive that the cleanup call was reduced on a maximum number of members ({@value #INTERMEDIATE_CLEANUP_THRESHOLD})
     * in a single run. We basically sacrifice some memory consumption for performance reasons.
     * 
     * @param tree
     * @param updatedMembers the members of the OR after the cleanup
     */
    private void cleanupImplications(EncodedExpressionTree tree, GrowingIntArray updatedMembers) {
        int sizeBefore = updatedMembers.size();
        LOGGER.trace("cleanupImplications BEFORE: {}", sizeBefore);

        int[] orMembers = tree.consolidateMembers(NodeType.OR, updatedMembers.toArray());
        updatedMembers.clear();

        if (orMembers.length > INTERMEDIATE_CLEANUP_THRESHOLD) {
            for (int idx = INTERMEDIATE_CLEANUP_THRESHOLD; idx < orMembers.length; idx++) {
                updatedMembers.add(orMembers[idx]);
            }
            orMembers = Arrays.copyOf(orMembers, INTERMEDIATE_CLEANUP_THRESHOLD);
        }

        int orNode = implicationResolver.cleanupImplications(tree, tree.createNode(NodeType.OR, orMembers));
        if (getNodeType(orNode) == NodeType.OR) {
            for (int member : tree.membersOf(orNode)) {
                updatedMembers.add(member);
            }
        }
        else {
            updatedMembers.add(orNode);
        }

        int sizeAfter = updatedMembers.size();
        LOGGER.trace("cleanupImplications AFTER: {}{}", (sizeBefore != sizeAfter) ? "*" : " ", sizeAfter);
    }

    /**
     * Multiplies an OR with the already existing expressions on the same level
     * 
     * @param tree
     * @param orNode
     * @param updatedMembers to be updated
     */
    private void multiplyOr(EncodedExpressionTree tree, int orNode, GrowingIntArray updatedMembers) {
        if (updatedMembers.isEmpty()) {
            updatedMembers.add(orNode);
        }
        else if (updatedMembers.size() == 1 && getNodeType(updatedMembers.get(0)) == NodeType.OR) {
            // special case, array began with an OR
            int[] rightOrMembers = tree.membersOf(updatedMembers.get(0));
            int[] leftOrMembers = tree.membersOf(orNode);
            multiplyOrWithStartOr(tree, leftOrMembers, rightOrMembers, updatedMembers);
        }
        else {
            // the existing elements in the array are all either ANDs or leafs
            multiplyOrWithExistingAndsOrLeafs(tree, orNode, updatedMembers);
        }
    }

    /**
     * Multiplies an OR with an AND or a leaf, basically each member if the given orNode gets combined with each of the existing members (AND) to create new
     * members in the updatedMembers collection
     * 
     * @param tree
     * @param orNode
     * @param updatedMembers
     */
    private void multiplyOrWithExistingAndsOrLeafs(EncodedExpressionTree tree, int orNode, GrowingIntArray updatedMembers) {
        int[] existingMembers = updatedMembers.toArray();

        int[] orMembers = tree.membersOf(orNode);

        updatedMembers.clear();
        int prevMemberCount = 0;
        for (int orMember : orMembers) {
            if (getNodeType(orMember) == NodeType.AND) {
                for (int idx = 0; idx < existingMembers.length; idx++) {
                    timeout.assertHaveTime();

                    updatedMembers.add(combineAndWithExistingAndOrLeaf(tree, orMember, existingMembers[idx]));
                    prevMemberCount = triggerImplicationCleanup(tree, updatedMembers, prevMemberCount);
                }
            }
            else {
                // OR does not contain OR, so it must be a leaf
                for (int idx = 0; idx < existingMembers.length; idx++) {
                    updatedMembers.add(combineLeafWithExistingAndOrLeaf(tree, orMember, existingMembers[idx]));
                    prevMemberCount = triggerImplicationCleanup(tree, updatedMembers, prevMemberCount);
                }
            }
        }
    }

    /**
     * Multiplies an AND with the existing members
     * 
     * @param tree
     * @param andNode
     * @param updatedMembers
     */
    private void multiplyAnd(EncodedExpressionTree tree, int andNode, GrowingIntArray updatedMembers) {
        int len = updatedMembers.size();

        if (len == 0) {
            updatedMembers.add(andNode);
        }
        else if (len == 1 && getNodeType(updatedMembers.get(0)) == NodeType.OR) {
            // special case, array began with an OR
            int[] orMembers = tree.membersOf(updatedMembers.get(0));
            multiplyAndWithStartOr(tree, andNode, orMembers, updatedMembers);
        }
        else {
            // the existing elements in the array are all either ANDs or leafs
            for (int idx = 0; idx < len; idx++) {
                updatedMembers.set(idx, combineAndWithExistingAndOrLeaf(tree, andNode, updatedMembers.get(idx)));
            }
        }
    }

    /**
     * Multiplies a leaf with the existing members
     * 
     * @param tree
     * @param leaf
     * @param updatedMembers
     */
    private void multiplyLeaf(EncodedExpressionTree tree, int leaf, GrowingIntArray updatedMembers) {
        int len = updatedMembers.size();
        if (len == 0) {
            updatedMembers.add(leaf);
        }
        else if (len == 1 && getNodeType(updatedMembers.get(0)) == NodeType.OR) {
            // special case, array began with an OR
            int[] orMembers = tree.membersOf(updatedMembers.get(0));
            multiplyLeafWithStartOr(tree, leaf, orMembers, updatedMembers);
        }
        else {
            // the existing elements in the array are all either ANDs or leafs
            for (int idx = 0; idx < len; idx++) {
                updatedMembers.set(idx, combineLeafWithExistingAndOrLeaf(tree, leaf, updatedMembers.get(idx)));
            }
        }
    }

    /**
     * Multiplies a leaf with a start-OR (can happen if the first member of an AND was an OR, "delayed multiplication")
     * 
     * @param tree
     * @param leaf
     */
    private void multiplyLeafWithStartOr(EncodedExpressionTree tree, int leaf, int[] orMembers, GrowingIntArray updatedMembers) {
        updatedMembers.clear();
        for (int idx = 0; idx < orMembers.length; idx++) {
            timeout.assertHaveTime();

            int existingNode = orMembers[idx];
            updatedMembers.add(combineLeafWithExistingAndOrLeaf(tree, leaf, existingNode));
        }
    }

    /**
     * Multiplies an AND with a start-OR (can happen if the first member of an AND was an OR, "delayed multiplication")
     * 
     * @param tree
     * @param leaf
     * @param orMembers
     * @param updatedMembers
     */
    private void multiplyAndWithStartOr(EncodedExpressionTree tree, int andNode, int[] orMembers, GrowingIntArray updatedMembers) {
        updatedMembers.clear();
        for (int idx = 0; idx < orMembers.length; idx++) {
            timeout.assertHaveTime();

            int leaf = orMembers[idx];
            updatedMembers.add(combineLeafWithExistingAndOrLeaf(tree, leaf, andNode));
        }
    }

    /**
     * Multiplies an OR with a start-OR (can happen if the first member of an AND was an OR, "delayed multiplication")
     * 
     * @param tree
     * @param leaf
     * @param orMembers
     * @param updatedMembers
     */
    private void multiplyOrWithStartOr(EncodedExpressionTree tree, int[] leftOrMembers, int[] rightOrMembers, GrowingIntArray updatedMembers) {
        updatedMembers.clear();
        for (int leftMember : leftOrMembers) {
            for (int rightMember : rightOrMembers) {
                timeout.assertHaveTime();

                if (isCombinedExpressionId(leftMember)) {
                    updatedMembers.add(combineAndWithExistingAndOrLeaf(tree, leftMember, rightMember));
                }
                else {
                    updatedMembers.add(combineLeafWithExistingAndOrLeaf(tree, leftMember, rightMember));
                }
            }
        }
    }

    /**
     * Combines a leaf with an AND or leaf after plausibility check
     * 
     * @param tree
     * @param leaf
     * @param existingNode
     * @return combined node or {@link CoreExpressionCodec#INVALID} to indicated that this combination should be skipped.
     */
    private int combineLeafWithExistingAndOrLeaf(EncodedExpressionTree tree, int leaf, int existingNode) {
        if (existingNode == INVALID || tree.getLogicHelper().leftImpliesRight(existingNode, leaf)) {
            return existingNode;
        }
        else if (tree.getLogicHelper().leftContradictsRight(leaf, existingNode)) {
            return INVALID;
        }
        else if (tree.getLogicHelper().leftImpliesRight(leaf, existingNode)) {
            return leaf;
        }
        else if (getNodeType(existingNode) == NodeType.AND) {
            return tree.createNode(NodeType.AND, MemberUtils.mergeDistinctMembers(tree.membersOf(existingNode), leaf));
        }
        else if (existingNode != leaf) {
            return tree.createNode(NodeType.AND, new int[] { existingNode, leaf });
        }
        return existingNode;
    }

    /**
     * Combines an AND with an AND (simple merge) after plausibility check
     * 
     * @param tree
     * @param leaf
     * @param existingNode
     * @return combined node or {@link CoreExpressionCodec#INVALID} to indicated that this combination should be skipped.
     */
    private int combineAndWithExistingAndOrLeaf(EncodedExpressionTree tree, int andNode, int existingNode) {
        if (existingNode == INVALID || tree.getLogicHelper().leftImpliesRight(existingNode, andNode)) {
            return existingNode;
        }
        else if (tree.getLogicHelper().leftContradictsRight(andNode, existingNode)) {
            return INVALID;
        }
        else if (tree.getLogicHelper().leftImpliesRight(andNode, existingNode)) {
            return andNode;
        }
        else if (getNodeType(existingNode) == NodeType.AND) {
            return tree.createNode(NodeType.AND, MemberUtils.mergeDistinctMembers(tree.membersOf(existingNode), tree.membersOf(andNode)));
        }
        else {
            return tree.createNode(NodeType.AND, MemberUtils.mergeDistinctMembers(tree.membersOf(andNode), existingNode));
        }
    }

}
