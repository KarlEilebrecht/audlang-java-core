//@formatter:off
/*
 * MemberUtils
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
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.getNodeType;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isCombinedExpressionId;

import java.util.Arrays;

/**
 * This utility collection simplifies the work with the members of encoded expressions in the tree.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class MemberUtils {

    public static final int[] EMPTY_MEMBERS = new int[0];

    /**
     * This method merges two member lists into a single one.<br>
     * All duplicates as well as {@link CoreExpressionCodec#INVALID}s will be filtered.
     * <p>
     * <b>Important:</b> Both sides must be sorted (only INVALIDs may occur anywhere). <br>
     * If any of the list is not sorted the result is unpredictable.
     * 
     * @param right
     * @param left
     * @return array that contains the set of unique ids from both sides, without any INVALIDs
     */
    @SuppressWarnings("java:S3776")
    public static int[] mergeDistinctMembers(int[] right, int[] left) {

        // ignoring java:S3776 about complexity because of the primitive state variables, code is easier to read this way

        if (right.length < left.length) {
            int[] temp = right;
            right = left;
            left = temp;
        }

        int rightIdx = 0;
        int leftIdx = 0;
        int[] res = new int[right.length + left.length];

        int resIdx = 0;

        int prevMember = INVALID;

        while (leftIdx < left.length) {
            int leftMember = left[leftIdx];
            if (leftMember == INVALID || (prevMember != INVALID && leftMember == prevMember)) {
                leftIdx++;
            }
            else if (rightIdx >= right.length) {
                res[resIdx] = leftMember;
                prevMember = leftMember;
                leftIdx++;
                resIdx++;
            }
            else {
                int rightMember = right[rightIdx];
                if (rightMember == INVALID || (prevMember != INVALID && rightMember == prevMember)) {
                    rightIdx++;
                }
                else if (rightMember < leftMember) {
                    res[resIdx] = rightMember;
                    prevMember = rightMember;
                    rightIdx++;
                    resIdx++;
                }
                else if (rightMember > leftMember) {
                    res[resIdx] = leftMember;
                    prevMember = leftMember;
                    leftIdx++;
                    resIdx++;
                }
                else {
                    res[resIdx] = rightMember;
                    prevMember = rightMember;
                    rightIdx++;
                    leftIdx++;
                    resIdx++;
                }
            }
        }
        resIdx = copyPendingMembers(right, rightIdx, res, resIdx);
        if (resIdx < res.length) {
            res = Arrays.copyOf(res, resIdx);
        }
        return res;
    }

    /**
     * Copies the remaining members after the existing ones
     * 
     * @param src
     * @param srcStartIdx
     * @param dest
     * @param destIdx
     * @return final size
     */
    private static int copyPendingMembers(int[] src, int srcStartIdx, int[] dest, int destIdx) {
        int prevMember = INVALID;
        for (int idx = srcStartIdx; idx < src.length; idx++) {
            int leftMember = src[idx];
            if (leftMember != INVALID && leftMember != prevMember) {
                dest[destIdx] = src[idx];
                destIdx++;
                prevMember = leftMember;
            }
        }
        return destIdx;
    }

    /**
     * This method merges an additional member into a member list.<br>
     * All duplicates as well as {@link CoreExpressionCodec#INVALID}s will be filtered.
     * <p>
     * <b>Important:</b> The members array must be sorted (only INVALIDs may occur anywhere). <br>
     * If the list is not sorted the result is unpredictable.
     * 
     * @param members sorted array (won't be modified)
     * @param additionalMember to be merged
     * @return merged array (always a new instance)
     */
    public static int[] mergeDistinctMembers(int[] members, int additionalMember) {
        if (additionalMember == INVALID) {
            return discardInvalidMembersDedup(members, true);
        }
        int[] res = new int[members.length + 1];
        int len = 0;
        boolean added = false;
        int prevMember = INVALID;
        for (int member : members) {
            if (member != INVALID && member != prevMember) {
                if (member < additionalMember || (added && member > additionalMember)) {
                    res[len] = member;
                    len++;
                    prevMember = member;
                }
                else if (member > additionalMember) {
                    res[len] = additionalMember;
                    res[len + 1] = member;
                    len = len + 2;
                    added = true;
                    prevMember = member;
                }
            }
        }
        if (!added) {
            res[len] = additionalMember;
            len++;
        }
        if (len < res.length) {
            res = Arrays.copyOf(res, len);
        }
        return res;
    }

    /**
     * Removes all duplicates and INVALIDs and sorts the members
     * 
     * @param members will be modified
     */
    public static void sortDistinctMembers(GrowingIntArray members) {
        members.sort();
        int resIdx = 0;
        int prevMember = INVALID;
        for (int idx = 0; idx < members.size(); idx++) {
            int member = members.get(idx);
            if (member != INVALID && member != prevMember) {
                if (idx > resIdx) {
                    members.set(resIdx, member);
                }
                prevMember = member;
                resIdx++;
            }
        }
        members.setLength(resIdx);
    }

    /**
     * Returns a copy of the given members that is sorted and free from duplicates/INVALIDs
     * 
     * @param members
     * @param forceCopy if true we even copy if the size of the array did not change
     * @return sorted array
     */
    public static int[] sortDistinctMembers(int[] members, boolean forceCopy) {
        int[] res = forceCopy ? Arrays.copyOf(members, members.length) : members;
        Arrays.sort(res);
        return discardInvalidMembersDedup(res);
    }

    /**
     * Removes duplicates and INVALIDs
     * 
     * @param res will be modified, must be sorted
     * @return copy or given array
     */
    private static int[] discardInvalidMembersDedup(int[] res) {
        int resIdx = 0;
        int prevMember = INVALID;

        for (int member : res) {
            if (member != INVALID && member != prevMember) {
                res[resIdx] = member;
                resIdx++;
                prevMember = member;
            }
        }
        return truncateIfRequired(res, resIdx);
    }

    /**
     * Returns a copy of the given members free from duplicates/INVALIDs
     * 
     * @param members must be sorted
     * @param forceCopy if true we even copy if the size of the array did not change
     * @return copy or given array
     */
    private static int[] discardInvalidMembersDedup(int[] members, boolean forceCopy) {
        int[] res = forceCopy ? Arrays.copyOf(members, members.length) : members;
        return discardInvalidMembersDedup(res);
    }

    /**
     * Removes any special set member (ALL/NONE) from the list
     * 
     * @param members may be modified
     * @return copy or given array if there was no change
     */
    private static int[] discardSpecialSetMembers(int[] members) {
        int resIdx = 0;
        for (int member : members) {
            if (member != ALL && member != NONE) {
                members[resIdx] = member;
                resIdx++;
            }
        }
        return truncateIfRequired(members, resIdx);
    }

    /**
     * Returns a copy of the given members free without any special sets
     * 
     * @param members
     * @param forceCopy if true we even copy if the size of the array did not change
     * @return copy or given array
     */
    public static int[] discardSpecialSetMembers(int[] members, boolean forceCopy) {
        int[] res = forceCopy ? Arrays.copyOf(members, members.length) : members;
        return discardSpecialSetMembers(res);
    }

    /**
     * Removes all the special set members (ALL/NONE)
     * 
     * @param members to be cleaned
     */
    public static void discardSpecialSetMembers(GrowingIntArray members) {
        int destIdx = 0;
        for (int idx = 0; idx < members.size(); idx++) {
            int member = members.get(idx);
            if (member != ALL && member != NONE) {
                if (idx > destIdx) {
                    members.set(destIdx, member);
                }
                destIdx++;
            }
        }
        members.setLength(destIdx);
    }

    /**
     * Copies the given array ignoring all LEAF expressions and INVALIDs
     * 
     * @param members
     * @return only the combined members, potentially empty
     */
    public static int[] copyFilterCombinedMembers(int[] members) {
        int[] res = new int[members.length];
        int resIdx = 0;
        for (int member : members) {
            if (isCombinedExpressionId(member)) {
                res[resIdx] = member;
                resIdx++;
            }
        }
        return truncateIfRequired(res, resIdx);

    }

    /**
     * Copies the given array ignoring all combined expressions and INVALIDs
     * 
     * @param members
     * @return only the leaf members, potentially empty
     */
    public static int[] copyFilterLeafMembers(int[] members) {
        int[] res = new int[members.length];
        int resIdx = 0;
        for (int member : members) {
            if (member != INVALID && !isCombinedExpressionId(member)) {
                res[resIdx] = member;
                resIdx++;
            }
        }
        return truncateIfRequired(res, resIdx);

    }

    /**
     * Utility method to truncate the given array to the specified length if required, basically a convenience method to replace an array with a shorter one if
     * (and only if) the required length is shorter than the physical one.
     * 
     * @param members
     * @param length
     * @return members or array copy of the new (smaller) length
     */
    private static int[] truncateIfRequired(int[] members, int length) {
        if (length == 0) {
            members = EMPTY_MEMBERS;
        }
        else if (length < members.length) {
            members = Arrays.copyOf(members, length);
        }
        return members;
    }

    /**
     * @param memberArrayRegistry
     * @param combinedNode
     * @return members of the combined node or exception if this was a leaf
     */
    public static int[] membersOf(MemberArrayRegistry memberArrayRegistry, int combinedNode) {
        return memberArrayRegistry.lookupMemberArray(CoreExpressionCodec.decodeCombinedExpressionId(combinedNode));
    }

    /**
     * @param members
     * @return true if any of the given members is a combined one
     */
    public static boolean containsAnyCombinedMember(int[] members) {
        for (int member : members) {
            if (isCombinedExpressionId(member)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param memberArrayRegistry
     * @param members
     * @param nodeType
     * @return true if any member is of the type or any of the siblings
     */
    public static boolean containsAnyMemberOfTypeRecursively(MemberArrayRegistry memberArrayRegistry, int[] members, NodeType nodeType) {
        for (int member : members) {
            if (getNodeType(member) == nodeType || (isCombinedExpressionId(member)
                    && containsAnyMemberOfTypeRecursively(memberArrayRegistry, membersOf(memberArrayRegistry, member), nodeType))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a copy of the members array skipping the given skipMember (if present)
     * 
     * @param members
     * @param skipMember to be skipped if present
     * @return copy of the members array without the given member
     */
    public static int[] copySkipMember(int[] members, int skipMember) {
        int[] res = new int[members.length > 0 ? members.length - 1 : 0];

        int destIdx = 0;
        for (int member : members) {
            if (member != skipMember) {
                if (destIdx == res.length) {
                    // special case: skipMember was not a member,
                    return Arrays.copyOf(members, members.length);
                }
                res[destIdx] = member;
                destIdx++;
            }
        }
        if (destIdx < res.length) {
            res = Arrays.copyOf(res, destIdx);
        }
        return res;
    }

    /**
     * Contains-check on <b>sorted</b> arrays.
     * <p>
     * By convention the empty array is contained in every array.
     * 
     * @param leftMembers sorted, free of duplicates
     * @param rightMembers sorted, free of duplicates
     * @return true if the left array contains the right one
     */
    public static boolean sortedLeftMembersContainSortedRightMembers(int[] leftMembers, int[] rightMembers) {
        if (rightMembers.length > leftMembers.length) {
            return false;
        }
        else if (rightMembers.length == 0) {
            return true;
        }
        else {
            int rightStartMember = rightMembers[0];
            int leftIdx = Arrays.binarySearch(leftMembers, 0, leftMembers.length, rightStartMember);

            if (leftIdx < 0 || (leftMembers.length - leftIdx) < rightMembers.length) {
                // not found or left array has not enough remaining elements
                return false;
            }
            else if (rightMembers.length == 1) {
                return true;
            }

            return sortedLeftMembersContainSortedRightRemainingMembers(leftMembers, leftIdx + 1, rightMembers);
        }
    }

    /**
     * Detail search for the <i>remaining</i> elements of <i>rightMembers</i> (starting with the second element) in <i>leftMembers</i> starting at the given
     * leftIdx.
     * <p>
     * The reasoning is that both sides are sorted, so all the remaining elements must <i>follow</i>.
     * 
     * @param leftMembers
     * @param leftIdx position to start searching on the left
     * @param rightMembers
     * @return true if the elements of rightMembers are all present in leftMembers
     */
    private static boolean sortedLeftMembersContainSortedRightRemainingMembers(int[] leftMembers, int leftIdx, int[] rightMembers) {
        int rightIdx = 1;
        while (rightIdx < rightMembers.length) {

            int leftMember = leftMembers[leftIdx];
            int rightMember = rightMembers[rightIdx];
            if (leftMember > rightMember) {
                return false;
            }
            else if (leftMember < rightMember) {
                leftIdx++;
            }
            else {
                leftIdx++;
                rightIdx++;
            }
            if ((leftMembers.length - leftIdx) < (rightMembers.length - rightIdx)) {
                // not enough remaining left members to contain all the remaining right members
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the left contains the right one (member overlap). If right is a leaf then this method returns true if right is one of the members of left.
     * If both are leaves then the result is true if both are identical.<br>
     * If both are combined nodes of different nodes (AND vs. OR) the result is always false.<br>
     * Otherwise the result is the same as returned by {@link #sortedLeftMembersContainSortedRightMembers(int[], int[])}
     * 
     * @param memberArrayRegistry
     * @param left
     * @param right
     * @return true if right is contained in left
     */
    public static boolean leftMembersContainRightMembers(MemberArrayRegistry memberArrayRegistry, int left, int right) {
        NodeType leftNodeType = getNodeType(left);
        NodeType rightNodeType = getNodeType(right);

        if (leftNodeType == NodeType.LEAF && rightNodeType == NodeType.LEAF) {
            return (left == right);
        }
        else if (rightNodeType == NodeType.LEAF) {
            int[] leftMembers = membersOf(memberArrayRegistry, left);
            return Arrays.binarySearch(leftMembers, right) > -1;
        }
        else if (leftNodeType != rightNodeType) {
            return false;
        }
        else {
            int[] leftMembers = membersOf(memberArrayRegistry, left);
            int[] rightMembers = membersOf(memberArrayRegistry, left);
            return sortedLeftMembersContainSortedRightMembers(leftMembers, rightMembers);
        }
    }

    private MemberUtils() {
        // static utilities
    }

}
