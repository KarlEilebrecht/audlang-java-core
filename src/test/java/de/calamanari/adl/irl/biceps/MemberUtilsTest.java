//@formatter:off
/*
 * MemberUtilsTest
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class MemberUtilsTest {

    @Test
    void testMergeDistinctMembers() {

        assertArrayEquals(new int[] { 0 }, MemberUtils.mergeDistinctMembers(new int[0], 0));
        assertArrayEquals(new int[] { 0, 1, 2, 3 }, MemberUtils.mergeDistinctMembers(new int[] { 1, 2, 3 }, 0));
        assertArrayEquals(new int[] { 1, 2, 3, 4 }, MemberUtils.mergeDistinctMembers(new int[] { 1, 2, 3 }, 4));
        assertArrayEquals(new int[] { 1, 2, 3, 4 }, MemberUtils.mergeDistinctMembers(new int[] { 1, 3, 4 }, 2));

        assertArrayEquals(new int[0], MemberUtils.mergeDistinctMembers(new int[0], new int[0]));
        assertArrayEquals(new int[] { 1, 2, 3 }, MemberUtils.mergeDistinctMembers(new int[] { 1, 2, 3 }, new int[0]));
        assertArrayEquals(new int[] { 1, 2, 3 }, MemberUtils.mergeDistinctMembers(new int[0], new int[] { 1, 2, 3 }));
        assertArrayEquals(new int[] { 0, 1, 2, 3 }, MemberUtils.mergeDistinctMembers(new int[] { 1, 2, 3 }, new int[] { 0 }));
        assertArrayEquals(new int[] { 1, 2, 3, 4 }, MemberUtils.mergeDistinctMembers(new int[] { 1, 2, 3 }, new int[] { 4 }));
        assertArrayEquals(new int[] { 1, 2, 3, 4 }, MemberUtils.mergeDistinctMembers(new int[] { 1, 3, 4 }, new int[] { 2 }));
        assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5 }, MemberUtils.mergeDistinctMembers(new int[] { 1, 3, 4 }, new int[] { 0, 2, 5 }));

        assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5 },
                MemberUtils.mergeDistinctMembers(new int[] { CoreExpressionCodec.INVALID, 1, CoreExpressionCodec.INVALID, 3, 4 },
                        new int[] { 0, CoreExpressionCodec.INVALID, 2, 5, CoreExpressionCodec.INVALID }));

        assertArrayEquals(new int[] { 0, 1, 2, 3, 4, 5 },
                MemberUtils.mergeDistinctMembers(new int[] { CoreExpressionCodec.INVALID, 1, CoreExpressionCodec.INVALID, 3, 3, 4 },
                        new int[] { 0, CoreExpressionCodec.INVALID, 2, 5, 5, 5, CoreExpressionCodec.INVALID }));

        assertArrayEquals(new int[0], MemberUtils.mergeDistinctMembers(new int[] { CoreExpressionCodec.INVALID, CoreExpressionCodec.INVALID }, new int[0]));
        assertArrayEquals(new int[0], MemberUtils.mergeDistinctMembers(new int[0], new int[] { CoreExpressionCodec.INVALID, CoreExpressionCodec.INVALID }));
        assertArrayEquals(new int[0], MemberUtils.mergeDistinctMembers(new int[] { CoreExpressionCodec.INVALID },
                new int[] { CoreExpressionCodec.INVALID, CoreExpressionCodec.INVALID }));

    }

    @Test
    void testSortDistinctMembers() {
        GrowingIntArray gar = new GrowingIntArray(new int[] { 6, 7, 1, 1, 1, 1, 4, CoreExpressionCodec.INVALID }, false);

        MemberUtils.sortDistinctMembers(gar);
        assertArrayEquals(new int[] { 1, 4, 6, 7 }, gar.toArray());

        gar = new GrowingIntArray();

        MemberUtils.sortDistinctMembers(gar);
        assertTrue(gar.isEmpty());

        gar = new GrowingIntArray(new int[] { CoreExpressionCodec.INVALID }, false);

        MemberUtils.sortDistinctMembers(gar);
        assertTrue(gar.isEmpty());

        int[] arr = new int[] { 7, 2, 3, 1 };
        assertSame(arr, MemberUtils.sortDistinctMembers(arr, false));
        arr = new int[] { 7, 2, 3, 1 };
        assertNotSame(arr, MemberUtils.sortDistinctMembers(arr, true));

        assertArrayEquals(new int[] { 1, 2, 3, 7 },
                MemberUtils.sortDistinctMembers(new int[] { 7, 3, 2, 1, CoreExpressionCodec.INVALID, CoreExpressionCodec.INVALID }, false));

        assertArrayEquals(new int[0], MemberUtils.sortDistinctMembers(new int[0], false));
        assertArrayEquals(new int[0], MemberUtils.sortDistinctMembers(new int[] { CoreExpressionCodec.INVALID, CoreExpressionCodec.INVALID }, false));

    }

    @Test
    void testDiscardSpecialSetMembers() {
        GrowingIntArray gar = new GrowingIntArray(new int[] { CoreExpressionCodec.ALL, 6, CoreExpressionCodec.ALL, 7, CoreExpressionCodec.NONE, 1, 1,
                CoreExpressionCodec.NONE, 1, 1, 4, CoreExpressionCodec.INVALID }, false);

        MemberUtils.discardSpecialSetMembers(gar);
        assertArrayEquals(new int[] { 6, 7, 1, 1, 1, 1, 4, CoreExpressionCodec.INVALID }, gar.toArray());

        assertArrayEquals(new int[] { 6, 7, 1, 1, 1, 1, 4, CoreExpressionCodec.INVALID },
                MemberUtils.discardSpecialSetMembers(new int[] { CoreExpressionCodec.ALL, 6, CoreExpressionCodec.ALL, 7, CoreExpressionCodec.NONE, 1, 1,
                        CoreExpressionCodec.NONE, 1, 1, 4, CoreExpressionCodec.INVALID }, false));

        int[] arr = new int[] { 1, 2, 3, 4, 5, CoreExpressionCodec.INVALID };

        assertSame(arr, MemberUtils.discardSpecialSetMembers(arr, false));
        assertNotSame(arr, MemberUtils.discardSpecialSetMembers(arr, true));

    }

    @Test
    void testCopyFilterCombinedMembers() {
        int[] members = new int[] { CoreExpressionCodec.encodeCombinedExpressionId(1, NodeType.AND),
                CoreExpressionCodec.encodeCombinedExpressionId(2, NodeType.OR), CoreExpressionCodec.ALL, CoreExpressionCodec.INVALID,
                CoreExpressionCodec.NONE };

        assertArrayEquals(
                new int[] { CoreExpressionCodec.encodeCombinedExpressionId(1, NodeType.AND), CoreExpressionCodec.encodeCombinedExpressionId(2, NodeType.OR) },
                MemberUtils.copyFilterCombinedMembers(members));

        members = new int[] { CoreExpressionCodec.ALL, CoreExpressionCodec.INVALID, CoreExpressionCodec.NONE };

        assertArrayEquals(new int[0], MemberUtils.copyFilterCombinedMembers(members));

        members = new int[0];

        assertArrayEquals(new int[0], MemberUtils.copyFilterCombinedMembers(members));

    }

    @Test
    void testCopyFilterLeafMembers() {
        int[] members = new int[] { CoreExpressionCodec.encodeCombinedExpressionId(1, NodeType.AND),
                CoreExpressionCodec.encodeCombinedExpressionId(2, NodeType.OR), CoreExpressionCodec.ALL, CoreExpressionCodec.INVALID, CoreExpressionCodec.NONE,
                CoreExpressionCodec.NONE };

        assertArrayEquals(new int[] { CoreExpressionCodec.ALL, CoreExpressionCodec.NONE, CoreExpressionCodec.NONE },
                MemberUtils.copyFilterLeafMembers(members));

        members = new int[] { CoreExpressionCodec.encodeCombinedExpressionId(1, NodeType.AND), CoreExpressionCodec.encodeCombinedExpressionId(2, NodeType.OR),
                CoreExpressionCodec.INVALID };

        assertArrayEquals(new int[0], MemberUtils.copyFilterLeafMembers(members));

        members = new int[0];

        assertArrayEquals(new int[0], MemberUtils.copyFilterLeafMembers(members));

    }

    @Test
    void testContainsAnyCombinedMember() {
        int[] members = new int[] { CoreExpressionCodec.encodeCombinedExpressionId(1, NodeType.AND),
                CoreExpressionCodec.encodeCombinedExpressionId(2, NodeType.OR), CoreExpressionCodec.ALL, CoreExpressionCodec.INVALID,
                CoreExpressionCodec.NONE };

        assertTrue(MemberUtils.containsAnyCombinedMember(members));

        members = new int[] { CoreExpressionCodec.ALL, CoreExpressionCodec.INVALID, CoreExpressionCodec.NONE };

        assertFalse(MemberUtils.containsAnyCombinedMember(members));

    }

    @Test
    void testCopySkipMember() {
        int[] members = new int[] { 17, 23, 412, 453, 3, 1, 18 };

        assertArrayEquals(new int[] { 23, 412, 453, 3, 1, 18 }, MemberUtils.copySkipMember(members, 17));

        assertArrayEquals(new int[] { 17, 23, 412, 453, 3, 18 }, MemberUtils.copySkipMember(members, 1));

        members = new int[] { 17, 23, 412, 1, 3, 1, 18 };
        assertArrayEquals(new int[] { 17, 23, 412, 3, 18 }, MemberUtils.copySkipMember(members, 1));

        members = new int[] { 17, 23, 412, 3, 18 };
        assertArrayEquals(new int[] { 17, 23, 412, 3, 18 }, MemberUtils.copySkipMember(members, 99));

        members = new int[] { 23, 23 };
        assertArrayEquals(new int[0], MemberUtils.copySkipMember(members, 23));

        members = new int[0];
        assertArrayEquals(new int[0], MemberUtils.copySkipMember(members, 23));

    }

    @Test
    void testSortedLeftMembersContainSortedRightMembers() {

        int[] members = new int[] { 2, 3, 5, 7, 11, 13, 19 };

        assertTrue(MemberUtils.sortedLeftMembersContainSortedRightMembers(new int[0], new int[0]));
        assertTrue(MemberUtils.sortedLeftMembersContainSortedRightMembers(members, new int[0]));
        assertTrue(MemberUtils.sortedLeftMembersContainSortedRightMembers(members, new int[] { 2 }));
        assertTrue(MemberUtils.sortedLeftMembersContainSortedRightMembers(members, new int[] { 7 }));
        assertTrue(MemberUtils.sortedLeftMembersContainSortedRightMembers(members, new int[] { 13 }));
        assertTrue(MemberUtils.sortedLeftMembersContainSortedRightMembers(members, new int[] { 19 }));
        assertTrue(MemberUtils.sortedLeftMembersContainSortedRightMembers(members, new int[] { 2, 3 }));
        assertTrue(MemberUtils.sortedLeftMembersContainSortedRightMembers(members, new int[] { 5, 7, 11 }));
        assertTrue(MemberUtils.sortedLeftMembersContainSortedRightMembers(members, new int[] { 5, 7, 19 }));
        assertTrue(MemberUtils.sortedLeftMembersContainSortedRightMembers(members, new int[] { 2, 11, 19 }));

        assertFalse(MemberUtils.sortedLeftMembersContainSortedRightMembers(new int[0], new int[] { 1 }));
        assertFalse(MemberUtils.sortedLeftMembersContainSortedRightMembers(new int[0], new int[] { 4 }));
        assertFalse(MemberUtils.sortedLeftMembersContainSortedRightMembers(new int[0], new int[] { 20 }));
        assertFalse(MemberUtils.sortedLeftMembersContainSortedRightMembers(new int[0], new int[] { 2, 11, 19, 20 }));
        assertFalse(MemberUtils.sortedLeftMembersContainSortedRightMembers(members, new int[] { 1 }));
        assertFalse(MemberUtils.sortedLeftMembersContainSortedRightMembers(members, new int[] { 4 }));
        assertFalse(MemberUtils.sortedLeftMembersContainSortedRightMembers(members, new int[] { 20 }));
        assertFalse(MemberUtils.sortedLeftMembersContainSortedRightMembers(members, new int[] { 2, 11, 19, 20 }));

    }

}
