//@formatter:off
/*
 * OrOfAndOverlapRegrouper
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
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.getNodeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.TimeOut;

/**
 * The {@link OrOfAndOverlapRegrouper} takes a tree with an expression tree (typically previously normalized using the {@link OrOfAndNormalizer}) and step by
 * step (bottom-up) re-groups the expression. This is not only for the purpose of structure and beauty, we also can detect further implications this way to
 * shorten the overall expression.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class OrOfAndOverlapRegrouper implements ExpressionTreeProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrOfAndOverlapRegrouper.class);

    private final TimeOut timeout;

    private final ImplicationResolver implicationResolver;

    /**
     * Creates a new instance taking the given resolver and a timeout
     * 
     * @param implicationResolver
     * @param timeout (null means default, see {@link TimeOut#createDefaultTimeOut(String)})
     */
    public OrOfAndOverlapRegrouper(ImplicationResolver implicationResolver, TimeOut timeout) {
        this.timeout = timeout == null ? TimeOut.createDefaultTimeOut(OrOfAndOverlapRegrouper.class.getSimpleName()) : timeout;
        this.implicationResolver = implicationResolver;
    }

    /**
     * Compares two entries by their rank. The rank is determined by the number of occurrences followed by the size of the overlap, and for deterministic
     * results we finally do an array comparison.
     */
    private static final Comparator<Map.Entry<Overlap, int[]>> OVERLAP_ENTRY_RANK_COMPARATOR = (entry1, entry2) -> {
        int res = Integer.compare(entry1.getValue()[0], entry2.getValue()[0]);
        if (res == 0) {
            res = Integer.compare(entry1.getKey().size(), entry2.getKey().size());
        }
        if (res == 0) {
            res = Arrays.compare(entry1.getKey().members, entry2.getKey().members);
        }
        return res;
    };

    /**
     * Compares two entries solely by the size of the overlap
     */
    private static final Comparator<Map.Entry<Overlap, int[]>> OVERLAP_ENTRY_SIZE_COMPARATOR = (entry1, entry2) -> Integer.compare(entry1.getKey().size(),
            entry2.getKey().size());

    /**
     * Converts the given tree which must have OR-of-AND form
     * 
     * @param tree
     */
    @Override
    public void process(EncodedExpressionTree tree) {

        int rootNode = tree.getRootNode();

        int nestingDepth = tree.nestingDepthOf(rootNode);
        if (nestingDepth < 2) {
            // leaf or INVALID or simple AND or simple OR
            return;
        }

        rootNode = regroup(tree, rootNode);
        tree.setRootNode(rootNode);
        tree.getMemberArrayRegistry().triggerHousekeeping(rootNode);

    }

    /**
     * Runs the regrouping on any given node
     * 
     * @param tree
     * @param node
     * @return node or regrouped node
     */
    private int regroup(EncodedExpressionTree tree, int node) {
        int nestingDepth = tree.nestingDepthOf(node);
        if (nestingDepth < 2) {
            // leaf or INVALID or simple AND or simple OR
            return node;
        }
        if (getNodeType(node) == NodeType.AND) {
            return regroupAnd(tree, node);
        }
        else {
            return regroupOr(tree, node);
        }

    }

    /**
     * Runs the regrouping on an AND node which basically re-groups its members and potentially cleans up some implications afterwards
     * 
     * @param tree
     * @param node
     * @return node or replacement
     */
    private int regroupAnd(EncodedExpressionTree tree, int node) {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("regroupAnd BEFORE: {}", tree.createDebugString(node));
        }
        timeout.assertHaveTime();

        boolean modified = false;
        int[] members = tree.membersOf(node);
        int[] updatedMembers = tree.getLogicHelper().expandCombinedNodesOfSameType(NodeType.AND, members);
        if (updatedMembers != members) {
            members = MemberUtils.sortDistinctMembers(updatedMembers, false);
        }
        updatedMembers = null;
        for (int idx = 0; idx < members.length; idx++) {
            timeout.assertHaveTime();

            int member = members[idx];
            int updatedMember = regroup(tree, member);
            if (updatedMember != member) {
                updatedMembers = (updatedMembers == null) ? Arrays.copyOf(members, members.length) : updatedMembers;
                updatedMembers[idx] = updatedMember;
            }
        }
        if (updatedMembers != null) {
            updatedMembers = tree.getLogicHelper().expandCombinedNodesOfSameType(NodeType.AND, updatedMembers);
            int updatedNode = tree.createNode(NodeType.AND, updatedMembers);
            if (updatedNode != node) {
                updatedNode = cleanupImplications(tree, updatedNode);
                node = updatedNode;
                modified = true;
            }

        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("regroupAnd AFTER: {}{}", (modified ? "*" : " "), tree.createDebugString(node));
        }

        return node;
    }

    /**
     * This method is the heart of the re-grouper, it runs on an OR and analyzes the members (ANDs) looking for overlaps
     * 
     * @param tree
     * @param node
     * @return node or replacement
     */
    private int regroupOr(EncodedExpressionTree tree, int node) {

        boolean modified = false;
        boolean modifiedInRun = false;
        do {
            timeout.assertHaveTime();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("regroupOr BEFORE: {}", tree.createDebugString(node));
            }

            int updatedNode = processOrOfAndRegrouping(tree, node);
            modifiedInRun = (updatedNode != node);

            if (modifiedInRun) {
                updatedNode = cleanupImplications(tree, updatedNode);
            }

            modified = modified || modifiedInRun;
            node = updatedNode;

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("regroupOr AFTER: {}{}", (modified ? "*" : " "), tree.createDebugString(node));
            }

        } while (modifiedInRun);

        return node;
    }

    /**
     * Detail regrouping method that expects an OR (otherwise no-op) to process all the ANDs to detect overlaps
     * 
     * @param tree
     * @param cache
     * @param node
     * @return node or replacement
     */
    private int processOrOfAndRegrouping(EncodedExpressionTree tree, int node) {
        if (getNodeType(node) != NodeType.OR) {
            return node;
        }
        int[] members = tree.membersOf(node);
        GrowingIntArray candidates = filterMembersOfTypeAnd(members);
        if (candidates.size() > 1) {
            int[] groupingMembers = candidates.toArray();
            int[] remainingMembers = filterRemainder(members, groupingMembers);
            if (regroupOrOfAnds(tree, groupingMembers)) {
                int destIdx = 0;
                for (int member : groupingMembers) {
                    members[destIdx] = member;
                    destIdx++;
                }
                for (int member : remainingMembers) {
                    members[destIdx] = member;
                    destIdx++;
                }
                node = tree.createNode(NodeType.OR, members);
            }
        }
        return node;

    }

    /**
     * Re-groups the ANDs inside an OR in a deterministic way until no further overlaps can be found
     * 
     * @param tree
     * @param members to be updated
     * @return true if there was anything regrouped
     */
    private boolean regroupOrOfAnds(EncodedExpressionTree tree, int[] members) {
        boolean modified = false;
        boolean modifiedInRun = false;
        do {
            timeout.assertHaveTime();

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("regroupOrOfAnds BEFORE: {}", tree.createDebugString(members));
            }

            modifiedInRun = regroupOrOfAndsBranch(tree, members);
            modified = modified || modifiedInRun;

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("regroupOrOfAnds AFTER:  {}", tree.createDebugString(members));
            }
        } while (modifiedInRun);
        return modified;
    }

    /**
     * Performs a <i>depth</i> regrouping, we basically detect sub-groups contained in larger groups.
     * 
     * @param tree
     * @param members to be modified
     * @return true if there was anything regrouped
     */
    private boolean regroupOrOfAndsBranch(EncodedExpressionTree tree, int[] members) {
        Overlap overlap = findBestOverlap(tree, members);
        if (overlap != Overlap.EMPTY) {
            int updateIdx = -1;

            // the group members are the ones which get "grouped by" the overlap
            // Example: (a = 1 AND b = 2 AND c = 3) OR (a = 1 AND b = 4 AND c = 5)
            // overlap: a = 1
            // group: (b = 2 AND c = 3) OR (b = 4 AND c = 5)
            // result: (a = 1) AND ((b = 2 AND c = 3) OR (b = 4 AND c = 5))
            int[] groupMembers = new int[members.length];
            int groupLen = 0;
            for (int idx = 0; idx < members.length; idx++) {
                timeout.assertHaveTime();

                int member = members[idx];
                int updatedMember = subtractOverlap(tree, member, overlap);
                if (updatedMember != member) {
                    members[idx] = INVALID;
                    updateIdx = idx;
                    groupMembers[groupLen] = updatedMember;
                    groupLen++;
                }
            }
            if (groupLen < groupMembers.length) {
                groupMembers = Arrays.copyOf(groupMembers, groupLen);
            }
            int groupNode = tree.createNode(NodeType.OR, groupMembers);

            // there might be more potential for regrouping inside the group
            groupNode = processOrOfAndRegrouping(tree, groupNode);

            updateRegroupedMember(tree, members, updateIdx, overlap, groupNode);
            return true;
        }
        return false;
    }

    /**
     * Here we update the members array (members of the parent OR) with the potentially updated members by index.
     * <p>
     * Members that are no longer needed, get replaced by INVALID to filter them later when re-creating the parent node.
     * 
     * @param tree
     * @param members to be modified
     * @param updatedMemberIdx
     * @param overlap
     * @param groupNode the update for the given index
     */
    private void updateRegroupedMember(EncodedExpressionTree tree, int[] members, int updatedMemberIdx, Overlap overlap, int groupNode) {
        NodeType groupType = getNodeType(groupNode);

        switch (groupType) {
        case LEAF: {
            int overlapNode = overlap.members[0];
            if (overlapNode > groupNode) {
                members[updatedMemberIdx] = tree.createNode(NodeType.AND, new int[] { groupNode, overlapNode });
            }
            else {
                members[updatedMemberIdx] = tree.createNode(NodeType.AND, new int[] { overlapNode, groupNode });
            }
            break;
        }
        case AND: {
            int[] mergedMembers = MemberUtils.mergeDistinctMembers(overlap.members, tree.membersOf(groupNode));
            members[updatedMemberIdx] = tree.createNode(NodeType.AND, mergedMembers);
            break;
        }
        case OR: {
            int[] mergedMembers = MemberUtils.mergeDistinctMembers(overlap.members, groupNode);
            members[updatedMemberIdx] = tree.createNode(NodeType.AND, mergedMembers);
        }
        }

    }

    /**
     * Returns the members of the given andNode <i>minus</i> the overlap (if fully applicable).
     * 
     * @param tree
     * @param andNode
     * @param overlap
     * @return the unmodified andNode or - <i>if the complete overlap is present</i> - an AND-node of the <i>remaining</i> members
     */
    private int subtractOverlap(EncodedExpressionTree tree, int andNode, Overlap overlap) {
        if (getNodeType(andNode) != NodeType.AND) {
            return andNode;
        }
        int[] members = tree.membersOf(andNode);
        if (MemberUtils.sortedLeftMembersContainSortedRightMembers(members, overlap.members)) {
            if (members.length == overlap.size()) {
                return ALL;
            }
            int[] updatedMembers = new int[members.length - overlap.size()];
            int destIdx = 0;
            int overlapStartIdx = 0;
            for (int member : members) {
                timeout.assertHaveTime();

                int overlapIdx = overlapStartIdx >= overlap.members.length ? -1 : Arrays.binarySearch(overlap.members, overlapStartIdx, overlap.size(), member);
                if (overlapIdx < 0) {
                    updatedMembers[destIdx] = member;
                    destIdx++;
                }
                else {
                    overlapStartIdx = overlapIdx + 1;
                }
            }
            if (updatedMembers.length == 1) {
                return updatedMembers[0];
            }
            andNode = tree.createNode(NodeType.AND, updatedMembers);
        }
        return andNode;
    }

    /**
     * Determine the overlap with the highest rank, see {@link #OVERLAP_ENTRY_RANK_COMPARATOR}
     * 
     * @param tree
     * @param members
     * @return best overlap or {@link Overlap#EMPTY} if not found
     */
    private Overlap findBestOverlap(EncodedExpressionTree tree, int[] members) {
        Map<Overlap, int[]> overlaps = computeAllOverlaps(tree, members);
        if (overlaps.isEmpty()) {
            return Overlap.EMPTY;
        }
        List<Map.Entry<Overlap, int[]>> entries = new ArrayList<>(overlaps.entrySet());
        updateContainedOverlapCounts(entries);
        Collections.sort(entries, OVERLAP_ENTRY_RANK_COMPARATOR);
        return entries.get(entries.size() - 1).getKey();
    }

    /**
     * Computes all overlaps between any two members. The key of the returned map is a unique overlap (sub-member-combination) and the value is a counter
     * indicating how often the overlap occurred.
     * 
     * @param tree
     * @param members
     * @return map with all the overlaps found between members
     */
    private Map<Overlap, int[]> computeAllOverlaps(EncodedExpressionTree tree, int[] members) {
        Map<Overlap, int[]> overlaps = new HashMap<>();
        for (int leftIdx = 0; leftIdx < members.length - 1; leftIdx++) {
            int leftMember = members[leftIdx];
            if (getNodeType(leftMember) != NodeType.AND) {
                continue;
            }
            for (int rightIdx = leftIdx + 1; rightIdx < members.length; rightIdx++) {
                timeout.assertHaveTime();

                int rightMember = members[rightIdx];
                if (getNodeType(rightMember) != NodeType.AND) {
                    continue;
                }
                Overlap overlap = computeOverlap(tree.membersOf(leftMember), tree.membersOf(rightMember));
                if (overlap != Overlap.EMPTY) {
                    int[] counter = overlaps.computeIfAbsent(overlap, key -> new int[1]);
                    counter[0]++;
                }
            }
        }
        return overlaps;
    }

    /**
     * If one overlap is contained in another overlap then we increase its counter value to rank it up
     * 
     * @param overlapEntries
     */
    private void updateContainedOverlapCounts(List<Map.Entry<Overlap, int[]>> overlapEntries) {
        // put the shorter overlaps strictly before the longer ones
        // so it is possible that the next overlap can contain the current one but never vice-versa
        Collections.sort(overlapEntries, OVERLAP_ENTRY_SIZE_COMPARATOR);

        for (int leftIdx = 0; leftIdx < overlapEntries.size() - 1; leftIdx++) {
            Map.Entry<Overlap, int[]> leftEntry = overlapEntries.get(leftIdx);
            for (int rightIdx = leftIdx + 1; rightIdx < overlapEntries.size(); rightIdx++) {
                timeout.assertHaveTime();

                Map.Entry<Overlap, int[]> rightEntry = overlapEntries.get(rightIdx);
                if (rightEntry.getKey().contains(leftEntry.getKey())) {
                    // if there is a longer overlap (right) that contains a shorter one (left)
                    // then we must count the right overlap occurrences also for the left
                    leftEntry.getValue()[0] = leftEntry.getValue()[0] + rightEntry.getValue()[0];
                }
            }
        }
    }

    /**
     * Computes the overlap between the two given member sets (both must be sorted)
     * 
     * @param leftMembers
     * @param rightMembers
     * @return overlap or {@link Overlap#EMPTY}
     */
    private Overlap computeOverlap(int[] leftMembers, int[] rightMembers) {
        int[] commonMembers = new int[leftMembers.length];
        int overlapSize = 0;
        int rightStartIdx = 0;
        for (int member : leftMembers) {
            timeout.assertHaveTime();

            if (rightStartIdx >= rightMembers.length) {
                break;
            }
            int rightIdx = Arrays.binarySearch(rightMembers, rightStartIdx, rightMembers.length, member);
            if (rightIdx >= 0) {
                commonMembers[overlapSize] = member;
                overlapSize++;
                rightStartIdx = rightIdx + 1;
            }
        }
        if (overlapSize == 0) {
            return Overlap.EMPTY;
        }
        else if (overlapSize < commonMembers.length) {
            commonMembers = Arrays.copyOf(commonMembers, overlapSize);
        }
        return new Overlap(commonMembers);

    }

    /**
     * Utility to filter members while skipping any of the right list, both lists must be sorted and free of duplicates
     * 
     * @param members
     * @param skipMembers
     * @return array with the remainder
     */
    private int[] filterRemainder(int[] members, int[] skipMembers) {
        int[] res = new int[members.length - skipMembers.length];
        int destIdx = 0;
        for (int member : members) {
            timeout.assertHaveTime();

            if (Arrays.binarySearch(skipMembers, member) < 0) {
                res[destIdx] = member;
                destIdx++;
            }
        }
        return res;
    }

    /**
     * Utility to filter the members of type AND in an OR
     * 
     * @param members
     * @return members of type AND, may be empty
     */
    private GrowingIntArray filterMembersOfTypeAnd(int[] members) {
        GrowingIntArray res = new GrowingIntArray();
        for (int member : members) {
            if (getNodeType(member) == NodeType.AND) {
                res.add(member);
            }
        }
        return res;
    }

    /**
     * Whenever finishing a level (bottom-up) we cleanup potential implications
     * 
     * @param tree
     * @param node
     * @return node or cleaned node if there were implications resolved
     */
    private int cleanupImplications(EncodedExpressionTree tree, int node) {
        return implicationResolver.cleanupImplications(tree, node, true);
    }

    /**
     * The {@link Overlap} is a thin wrapper around an array of expression ids that reflects the overlap between two member arrays.<br>
     * We use these instances as <i>keys</i> of a map to rank overlaps by the number of times they are found within the same OR.
     * <p>
     * <b>Note:</b> This technique <i>relies</i> on the fact that member arrays are sorted. The wrapped member arrays are sorted as well and can thus serve as
     * keys.
     */
    private static final class Overlap {

        private static final Overlap EMPTY = new Overlap(new int[0]);

        final int[] members;

        /**
         * Due to the heavy use it makes sense to materialize the hashcode
         */
        private final int hashCode;

        /**
         * <b>Important:</b> NEVER change a given array after passing it to this method. The consequences would be unpredictable and very hard to diagnose!
         * 
         * @param members array to be wrapped, so it can serve as a key
         */
        Overlap(int[] members) {
            this.members = members;
            this.hashCode = Arrays.hashCode(members);
        }

        /**
         * @param other
         * @return true if the array wrapped by this instance contains all the members wrapped by the other instance
         */
        boolean contains(Overlap other) {
            return MemberUtils.sortedLeftMembersContainSortedRightMembers(members, other.members);
        }

        /**
         * @return length of the wrapped member array
         */
        int size() {
            return members.length;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Overlap other = (Overlap) obj;
            return this.hashCode == other.hashCode && Arrays.equals(members, other.members);
        }

    }

}
