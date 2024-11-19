//@formatter:off
/*
 * CoreExpressionBuilder
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
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.encodeCombinedExpressionId;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.getNodeType;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isCombinedExpressionId;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isSpecialSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.irl.CombinedExpression;
import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.biceps.CoreExpressionCodec.Dictionary;

/**
 * The {@link EncodedExpressionTree} holds an expression in memory that is <i>work-in-progress</i> and that may need lots of transformations. It uses a
 * light-weight form to represent the nodes, so playing with the expressions won't blow-up memory, and any logic tests are way cheaper compared to performing
 * them on {@link CoreExpression}s.
 * <p>
 * Technically, the tree is a container for the logical tree's root node with supplementary functionality.
 * <p>
 * <b>Important:</b> The <i>combined nodes</i> (AND/OR) with their member arrays adhere to a few crucial conventions:
 * <ul>
 * <li>Member arrays are sorted ascending.</li>
 * <li>Member arrays don't contain any duplicates.</li>
 * <li>Member arrays don't contain {@link CoreExpressionCodec#INVALID}s.</li>
 * <li>Member arrays of type {@link NodeType#AND} are free of any simple contradictions, see
 * {@link ExpressionLogicHelper#haveAnySimpleContradictionInAndParent(int[])}</li>
 * <li>Member arrays of type {@link NodeType#OR} are free of IS-UNKNOWN-contradictions, see
 * {@link ExpressionLogicHelper#haveAnyIsUnknownContradictionInOrParent(int[])}</li>
 * </ul>
 * This simplifies a couple of technical and logical operations like comparison and containment detection.
 * <p>
 * 
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class EncodedExpressionTree implements Serializable {

    private static final long serialVersionUID = 6352104542911982200L;

    static final Logger LOGGER = LoggerFactory.getLogger(EncodedExpressionTree.class);

    /**
     * node registry with all the combined nodes (AND/OR) in this tree plus the temporarily created ones
     */
    private final MemberArrayRegistry memberArrayRegistry;

    /**
     * helper for working with the tree's nodes
     */
    private final ExpressionLogicHelper logicHelper;

    /**
     * Currently engaged codec, see {@link #initialize(CoreExpressionCodec)}
     */
    private CoreExpressionCodec codec;

    /**
     * The root level holds the tree's root node. Temporarily, there can be no root or even multiple roots (before combining them), but certain methods require
     * a single root node, such as {@link #getRootNode()} or {@link #toCoreExpression()}
     */
    private ExpressionTreeLevel rootLevel = null;

    /**
     * @return the tree's base level, holding the root(s)
     */
    public ExpressionTreeLevel getRootLevel() {
        return rootLevel;
    }

    /**
     * Constructor for internal purposes
     * 
     * @param memberArrayRegistry
     */
    private EncodedExpressionTree(MemberArrayRegistry memberArrayRegistry) {
        this.memberArrayRegistry = memberArrayRegistry;
        this.logicHelper = new ExpressionLogicHelper(memberArrayRegistry);
    }

    /**
     * Creates an empty tree without codec and root
     * <p>
     * This constructor is for internal conversion purposes only. For proper initialization of the instance before use, the methods
     * {@link #initialize(CoreExpressionCodec)} and {@link #createTreeLevel()} <b>must be called</b>.
     * <p>
     * <b>Hint:</b> Most of the time {@link #fromCoreExpression(CoreExpression)} is the right way to create a properly initialized tree.
     */
    public EncodedExpressionTree() {
        this(new MemberArrayRegistry());
    }

    /**
     * Returns a copy of this tree. Right afterwards both instances are effectively equal but their evolution afterwards may differ. While simple expression
     * encoding stays in sync (only depends on the immutable codec) the encoding of <i>combined sub-expressions</i> may differ.
     * 
     * @return deep copy of the current tree, unrelated to this instance (except for sharing the same codec)
     */
    public EncodedExpressionTree copy() {
        EncodedExpressionTree res = new EncodedExpressionTree(memberArrayRegistry.copy());
        res.codec = codec;
        res.rootLevel = rootLevel.copy();
        return res;
    }

    /**
     * Creates a tree from the given core expression
     * 
     * @param expression
     * @return tree
     */
    public static EncodedExpressionTree fromCoreExpression(CoreExpression expression) {
        EncodedExpressionTree tree = new EncodedExpressionTree();
        tree.initialize(new CoreExpressionCodec(new Dictionary(expression.allFields())));
        tree.createTreeLevel().members().add(tree.createNode(expression));
        return tree;
    }

    /**
     * If the tree is in a valid state (single root present at root level) this method returns the corresponding {@link CoreExpression}
     * 
     * @return new core expression reflecting this tree
     */
    public CoreExpression toCoreExpression() {
        return createCoreExpression(getRootNode());
    }

    /**
     * This method resets the tree (empty, no root node) for the purpose of reuse.
     * <p>
     * <b>Warning:</b> This method also resets the member array registry of this tree and consequently invalidates any earlier issued node.
     * 
     * @param codec fresh codec to start with
     */
    public void initialize(CoreExpressionCodec codec) {
        this.codec = codec;
        if (this.rootLevel != null) {
            this.rootLevel.members().clear();
        }
        memberArrayRegistry.clear();
    }

    /**
     * Computes the depth of the given node.
     * <ul>
     * <li>A leaf has the depth 0</li>
     * <li>An AND of two leaves has the depth 1.</li>
     * <li>An OR of a leaf and the before mentioned AND would have the depth 2.</li>
     * </ul>
     * 
     * @param node
     * @return depth of the given node
     */
    public int nestingDepthOf(int node) {
        if (getNodeType(node) == NodeType.LEAF) {
            return 0;
        }
        else {
            int[] members = membersOf(node);
            int maxDepth = 0;
            for (int member : members) {
                int memberDepth = nestingDepthOf(member);
                if (memberDepth > maxDepth) {
                    maxDepth = memberDepth;
                }
            }
            return maxDepth + 1;
        }
    }

    /**
     * This is the root (top level expression including all the other ones) of the tree
     * <p>
     * 
     * @return root node
     * @throws IllegalStateException if the tree currently has no root or multiple roots
     */
    public int getRootNode() {
        if (rootLevel == null || rootLevel.members().isEmpty() || rootLevel.members().size() > 1) {
            throw new IllegalStateException("Expecting root level with a single member, given: " + (rootLevel == null ? null : rootLevel.members()));
        }
        return rootLevel.members().get(0);
    }

    /**
     * Sets or overwrites the root node with the given one.
     * 
     * @param rootNode to be set as <i>the only root</i> of this tree
     */
    public void setRootNode(int rootNode) {
        if (rootLevel == null) {
            throw new IllegalStateException("No root level (null). This method must not be called before createTreeLevel() has been called at least once.");
        }
        rootLevel.members().clear();
        rootLevel.members().add(rootNode);
    }

    /**
     * Creates a new node from the given expression (recursively) in the tree's registry.
     * <p>
     * The returned id is not attached anywhere. As long as you don't set it as the new root or include it in further nodes (e.g., using
     * {@link #createNode(NodeType, int[])}), the node remains an orphan.
     * <p>
     * <b>Important:</b> This method cannot add any <i>new</i> arguments to an expression tree. All arguments must be known to the tree's
     * {@link CoreExpressionCodec}. If you want to combine unrelated expressions (or parts of them), then {@link #merge(EncodedExpressionTree)} might help.
     * 
     * @param expression
     * @return newly created node
     * @throws ExpressionCodecException if the given expression could not be encoded with this coded
     * @throws IllegalStateException if the tree was not properly initialized with a codec (see {@link #initialize(CoreExpressionCodec)})
     */
    public int createNode(CoreExpression expression) {
        if (codec == null) {
            throw new IllegalStateException("Cannot create node for expression without codec (instance not initialized), given: " + expression);
        }
        if (expression instanceof CombinedExpression cmb) {
            GrowingIntArray members = new GrowingIntArray(cmb.members().size());
            for (CoreExpression memberExpression : cmb.members()) {
                members.add(createNode(memberExpression));
            }
            return createNode(cmb.combiType() == CombinedExpressionType.AND ? NodeType.AND : NodeType.OR, members);
        }
        else {
            return codec.encode(expression);
        }
    }

    /**
     * Creates a new node from any list of members. After filtering and sorting, a new node will be created.
     * <p>
     * To be called if the caller cannot guarantee that the given members are free of duplicates and sorted.
     * <p>
     * The returned id is not attached anywhere. As long as you don't set it as the new root or include it in further nodes (e.g., using
     * {@link #createNode(NodeType, int[])}), the node remains an orphan.
     * 
     * @param nodeType
     * @param members
     * @return newly created node
     */
    public int createNode(NodeType nodeType, GrowingIntArray members) {
        int[] memberArray = consolidateMembers(nodeType, members);
        if (memberArray.length == 1) {
            return memberArray[0];
        }
        else {
            int id = memberArrayRegistry.registerMemberArray(memberArray);
            return encodeCombinedExpressionId(id, nodeType);
        }
    }

    /**
     * Creates a new node from any list of members. After filtering and sorting, a new node will be created. <br>
     * To be called if the caller cannot guarantee that the given members are free of duplicates and sorted.
     * <p>
     * The returned id is not attached anywhere. As long as you don't set it as the new root or include it in further nodes (e.g., using
     * {@link #createNode(NodeType, int[])}), the node remains an orphan.
     * 
     * @param nodeType
     * @param members
     * @return newly created node
     */
    public int createNode(NodeType nodeType, int[] members) {
        int[] memberArray = consolidateMembers(nodeType, members);
        if (memberArray.length == 1) {
            return memberArray[0];
        }
        else {
            int id = memberArrayRegistry.registerMemberArray(memberArray);
            return encodeCombinedExpressionId(id, nodeType);
        }
    }

    /**
     * This method prepared the members of an OR/AND without actually creating a new node, so it is roughly equivalent to first creating the combined node and
     * then obtaining its members.
     * 
     * @param nodeType
     * @param members (may be modified during processing, don't use afterwards)
     * @return the members considered final members of the AND/OR
     */
    public int[] consolidateMembers(NodeType nodeType, int[] members) {

        int[] membersBkp = null;
        if (LOGGER.isTraceEnabled()) {
            membersBkp = Arrays.copyOf(members, members.length);
            LOGGER.trace("consolidateMembers {} BEFORE: {}", nodeType, createDebugString(members));
        }

        if (nodeType == NodeType.LEAF) {
            throw new IllegalStateException("Attempt to consolidate members for a LEAF, given: " + Arrays.toString(members));
        }

        members = logicHelper.expandCombinedNodesOfSameType(nodeType, members);
        members = MemberUtils.sortDistinctMembers(members, false);
        if ((members.length == 0 && nodeType == NodeType.AND) || (nodeType == NodeType.OR && Arrays.binarySearch(members, ALL) > -1)) {
            return new int[] { ALL };
        }
        else if ((members.length == 0 && nodeType == NodeType.OR) || (nodeType == NodeType.AND && Arrays.binarySearch(members, NONE) > -1)) {
            return new int[] { NONE };
        }
        else if (members.length == 1) {
            return members;
        }

        members = MemberUtils.discardSpecialSetMembers(members, false);
        int[] res = finalizeMemberArray(nodeType, members);

        if (LOGGER.isTraceEnabled()) {
            boolean modified = !Arrays.equals(res, membersBkp);
            LOGGER.trace("consolidateMembers {} AFTER: {}{}", nodeType, (modified ? "*" : " "), createDebugString(res));
        }
        return res;
    }

    /**
     * This method prepared the members of an OR/AND without actually creating a new node, so it is roughly equivalent to first creating the combined node and
     * then obtaining its members.
     * 
     * @param nodeType
     * @param members
     * @return the members considered final members of the AND/OR
     */
    public int[] consolidateMembers(NodeType nodeType, GrowingIntArray members) {

        int[] membersBkp = null;
        if (LOGGER.isTraceEnabled()) {
            membersBkp = members.toArray();
            LOGGER.trace("consolidateMembers {} BEFORE: {}", nodeType, createDebugString(membersBkp));
        }

        if (nodeType == NodeType.LEAF) {
            throw new IllegalStateException("Attempt to consolidate members for a LEAF, given: " + members);
        }

        logicHelper.expandCombinedNodesOfSameType(nodeType, members);
        MemberUtils.sortDistinctMembers(members);
        if ((members.isEmpty() && nodeType == NodeType.AND) || (nodeType == NodeType.OR && members.binarySearch(ALL) > -1)) {
            return new int[] { ALL };
        }
        else if ((members.isEmpty() && nodeType == NodeType.OR) || (nodeType == NodeType.AND && members.binarySearch(NONE) > -1)) {
            return new int[] { NONE };
        }
        else if (members.size() == 1) {
            return members.toArray();
        }

        MemberUtils.discardSpecialSetMembers(members);
        int[] res = finalizeMemberArray(nodeType, members.toArray());

        if (LOGGER.isTraceEnabled()) {
            boolean modified = !Arrays.equals(res, membersBkp);
            LOGGER.trace("consolidateMembers {} AFTER: {}{}", nodeType, (modified ? "*" : " "), createDebugString(res));
        }
        return res;
    }

    /**
     * Returns the final member array after performing a quick validation
     * 
     * @param nodeType
     * @param members
     * @return final member array to be encoded
     */
    private int[] finalizeMemberArray(NodeType nodeType, int[] members) {
        if (members.length > 1) {
            if ((nodeType == NodeType.OR && members.length == 0)
                    || (nodeType == NodeType.AND && getLogicHelper().haveAnySimpleContradictionInAndParent(members))) {
                traceMemberArrayReplacement(nodeType, members, NONE);
                return new int[] { NONE };
            }
            else if ((nodeType == NodeType.AND && members.length == 0)
                    || (nodeType == NodeType.OR && getLogicHelper().haveAnyIsUnknownContradictionInOrParent(members))) {
                traceMemberArrayReplacement(nodeType, members, ALL);
                return new int[] { ALL };
            }
        }
        return members;
    }

    /**
     * Logs the replacement action
     * 
     * @param nodeType
     * @param members
     * @param replacement
     */
    private void traceMemberArrayReplacement(NodeType nodeType, int[] members, int replacement) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Replacing contradicting {}-member-array: {} -> {}", nodeType, createDebugString(members), createDebugString(replacement));
        }
    }

    /**
     * Creates a new tree level.
     * <p>
     * The only impact this method has on the tree is that the first level ever returned by this method becomes the root level.
     * 
     * @return newly created tree level
     */
    public ExpressionTreeLevel createTreeLevel() {
        ExpressionTreeLevel res = new ExpressionTreeLevel(new GrowingIntArray());
        if (rootLevel == null) {
            rootLevel = res;
        }
        return res;
    }

    /**
     * @param node
     * @return new {@link CoreExpression} reflecting the given node
     */
    public CoreExpression createCoreExpression(int node) {
        if (isCombinedExpressionId(node)) {
            int[] members = logicHelper.membersOf(node);
            List<CoreExpression> combinedMembers = new ArrayList<>(members.length);
            for (int subMember : members) {
                combinedMembers.add(createCoreExpression(subMember));
            }
            return CombinedExpression.of(combinedMembers, getNodeType(node) == NodeType.AND ? CombinedExpressionType.AND : CombinedExpressionType.OR);
        }
        else {
            return codec.decode(node);
        }
    }

    /**
     * Creates a new tree with at least <b>two roots</b>: the first one is the one of this tree, the second is the one of the other tree, both are based on a
     * combined codec. Should any of the trees have multiple roots before, the number of roots will increase left to right.
     * <p>
     * This method does neither modify <i>this</i> instance nor the <i>other</i>. The new instance is independent from both.
     * <p>
     * Merging a tree with itself creates a new tree by doubling the number of roots.
     * 
     * @param other to be merged
     * @return new tree with two (or more) roots
     */
    public EncodedExpressionTree merge(EncodedExpressionTree other) {
        EncodedExpressionTree res = this.copy();

        if (this == other) {
            res.getRootLevel().members().addAll(other.getRootLevel().members());
            return res;
        }

        res.codec = res.codec.merge(other.codec);
        for (int idx = 0; idx < other.getRootLevel().members().size(); idx++) {
            int additionalRoot = recode(other, other.getRootLevel().members().get(idx), res);
            res.getRootLevel().members().add(additionalRoot);
        }
        return res;
    }

    /**
     * Recodes source nodes recursively based on the destination tree
     * 
     * @param srcTree
     * @param srcNode
     * @param destTree
     * @return recoded node
     */
    private int recode(EncodedExpressionTree srcTree, int srcNode, EncodedExpressionTree destTree) {
        NodeType nodeType = getNodeType(srcNode);
        if (srcNode == INVALID || isSpecialSet(srcNode)) {
            return srcNode;
        }
        else if (nodeType == NodeType.LEAF) {
            return destTree.createNode(srcTree.createCoreExpression(srcNode));
        }
        else {
            int[] members = srcTree.membersOf(srcNode);
            for (int idx = 0; idx < members.length; idx++) {
                members[idx] = recode(srcTree, members[idx], destTree);
            }
            return destTree.createNode(nodeType, members);
        }
    }

    /**
     * @return the registry that holds the member arrays of the combined nodes of this tree
     */
    public MemberArrayRegistry getMemberArrayRegistry() {
        return memberArrayRegistry;
    }

    /**
     * @return logic helper to work with member nodes
     */
    public ExpressionLogicHelper getLogicHelper() {
        return logicHelper;
    }

    /**
     * @return codec that is responsible for encoding/decoding leafs (conditions)
     */
    public CoreExpressionCodec getCodec() {
        return codec;
    }

    /**
     * @param combinedNode
     * @return direct members of the given node
     */
    public int[] membersOf(int combinedNode) {
        return MemberUtils.membersOf(memberArrayRegistry, combinedNode);
    }

    /**
     * Returns all the leaves in the tree starting from the given node downwards.
     * 
     * @param node
     * @return all the leaves in the tree top-down, unique, sorted, includes the given node if it is a leaf
     */
    public int[] collectLeaves(int node) {
        if (getNodeType(node) == NodeType.LEAF) {
            return new int[] { node };
        }
        GrowingIntArray result = new GrowingIntArray();
        collectLeaves(node, result);
        MemberUtils.sortDistinctMembers(result);
        return result.toArray();
    }

    /**
     * Walks the tree top-down to collect all the leaves
     * 
     * @param node
     * @param result
     */
    private void collectLeaves(int node, GrowingIntArray result) {
        NodeType nodeType = getNodeType(node);
        if (nodeType == NodeType.LEAF) {
            result.add(node);
        }
        else {
            for (int member : membersOf(node)) {
                collectLeaves(member, result);
            }
        }
    }

    /**
     * @param node
     * @return string representation of the expression this node is representing
     */
    public String createDebugString(int node) {
        StringBuilder sb = new StringBuilder();
        appendDebugString(sb, node);
        return sb.toString();
    }

    /**
     * @param members
     * @return string representation containing all {@link #createDebugString(int)}s of all the given members as a list
     */
    public String createDebugString(int[] members) {
        if (members == null) {
            return "null";
        }
        else if (members.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < members.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            appendDebugString(sb, members[i]);
        }
        return sb.toString();
    }

    /**
     * See {@link #createDebugString(int)}
     * 
     * @param sb
     * @param node
     */
    public void appendDebugString(StringBuilder sb, int node) {
        if (isCombinedExpressionId(node)) {
            sb.append("( ");
            try {
                int[] members = logicHelper.membersOf(node);
                for (int i = 0; i < members.length; i++) {
                    if (i > 0) {
                        sb.append(" ");
                        sb.append(getNodeType(node));
                        sb.append(" ");
                    }
                    appendDebugString(sb, members[i]);
                }
            }
            catch (IndexOutOfBoundsException ex) {
                // This code is only for the case that a bug was introduced to
                // prevent the debug string method from crashing
                LOGGER.error("Decoding error, unrecognizable combined node: {}", node);
                sb.append("!?[");
                sb.append(node);
                sb.append("]?!");

            }
            sb.append(" )");
        }
        else if (node == INVALID) {
            sb.append("INVALID");
        }
        else if (codec != null) {
            try {
                sb.append(codec.decode(node));
            }
            catch (ExpressionCodecException ex) {
                // This code is only for the case that a bug was introduced to
                // prevent the debug string method from crashing
                LOGGER.error("Decoding error, unrecognizable node: " + node, ex);
                sb.append("!?(");
                sb.append(node);
                sb.append(")?!");
            }
        }
        else {
            // due to a bug or while debugging the method might be called
            // before proper initialization of the tree instance
            sb.append(node);
        }
    }

}
