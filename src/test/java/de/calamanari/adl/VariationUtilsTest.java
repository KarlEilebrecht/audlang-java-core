//@formatter:off
/*
 * PermutationUtilsTest
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

package de.calamanari.adl;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class VariationUtilsTest {

    static final Logger LOGGER = LoggerFactory.getLogger(VariationUtilsTest.class);

    @Test
    void testSubListCreation() {

        assertTrue(VariationUtils.createSubLists(Arrays.asList("A", "B", "C"), 0).isEmpty());
        assertTrue(VariationUtils.createSubLists(Collections.emptyList(), -1).isEmpty());
        assertTrue(VariationUtils.createSubLists(Collections.emptyList(), 0).isEmpty());
        assertTrue(VariationUtils.createSubLists(Collections.emptyList(), 2).isEmpty());
        assertTrue(VariationUtils.createSubLists(Arrays.asList("A", "B", "C"), 4).isEmpty());

        assertEquals("[[A], [B], [C]]", VariationUtils.createSubLists(Arrays.asList("A", "B", "C"), 1).toString());
        assertEquals("[[A, B, C]]", VariationUtils.createSubLists(Arrays.asList("A", "B", "C"), 3).toString());
        assertEquals("[[A, B], [A, C], [B, C]]", VariationUtils.createSubLists(Arrays.asList("A", "B", "C"), 2).toString());

        assertEquals("[[A, B, C], [A, B, D], [A, C, D], [B, C, D]]", VariationUtils.createSubLists(Arrays.asList("A", "B", "C", "D"), 3).toString());

    }

    @Test
    void testSubListSizeComputation() {

        assertEstimatedSubListSize(Arrays.asList("A", "B", "C"), 1);
        assertEstimatedSubListSize(Arrays.asList("A", "B", "C"), 2);
        assertEstimatedSubListSize(Arrays.asList("A", "B", "C"), 3);
        assertEstimatedSubListSize(Arrays.asList("A", "B", "C", "D"), 1);
        assertEstimatedSubListSize(Arrays.asList("A", "B", "C", "D"), 2);
        assertEstimatedSubListSize(Arrays.asList("A", "B", "C", "D"), 3);
        assertEstimatedSubListSize(Arrays.asList("A", "B", "C", "D"), 4);

        assertEstimatedSubListSize(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"), 4);
        assertEstimatedSubListSize(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"), 7);
        assertEstimatedSubListSize(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"), 8);

        assertEquals(12870,
                VariationUtils.computeNumberOfSubLists(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"), 8));

        assertEquals(3432, VariationUtils.computeNumberOfSubLists(Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"), 7));

    }

    private static void assertEstimatedSubListSize(List<?> supplyList, int targetSize) {
        assertEquals(VariationUtils.createSubLists(supplyList, targetSize).size(), VariationUtils.computeNumberOfSubLists(supplyList, targetSize));
    }

    @Test
    void testAllVariationsCreation() {

        assertEquals(Collections.emptyList(), VariationUtils.createAllVariations(Collections.emptyMap()));

        Map<String, List<Integer>> supplyListMap = new LinkedHashMap<>();
        supplyListMap.put("A", Arrays.asList(1, 2, 3));

        assertEquals("[{A=1}, {A=2}, {A=3}]", VariationUtils.createAllVariations(supplyListMap).toString());

        supplyListMap.put("C", Collections.emptyList());

        supplyListMap.put("B", Arrays.asList(1, 5, 6));

        assertEquals("[{A=1, B=1}, {A=1, B=5}, {A=1, B=6}, {A=2, B=1}, {A=2, B=5}, {A=2, B=6}, {A=3, B=1}, {A=3, B=5}, {A=3, B=6}]",
                VariationUtils.createAllVariations(supplyListMap).toString());

        supplyListMap.put(null, Arrays.asList(7, 3, null));

        supplyListMap.put("D", Collections.emptyList());
        supplyListMap.put("E", Collections.emptyList());

        assertEquals("[{A=1, B=1, null=7}, {A=1, B=1, null=3}, {A=1, B=1, null=null}, {A=1, B=5, null=7}, {A=1, B=5, null=3}, "
                + "{A=1, B=5, null=null}, {A=1, B=6, null=7}, {A=1, B=6, null=3}, {A=1, B=6, null=null}, {A=2, B=1, null=7}, "
                + "{A=2, B=1, null=3}, {A=2, B=1, null=null}, {A=2, B=5, null=7}, {A=2, B=5, null=3}, {A=2, B=5, null=null}, "
                + "{A=2, B=6, null=7}, {A=2, B=6, null=3}, {A=2, B=6, null=null}, {A=3, B=1, null=7}, {A=3, B=1, null=3}, "
                + "{A=3, B=1, null=null}, {A=3, B=5, null=7}, {A=3, B=5, null=3}, {A=3, B=5, null=null}, {A=3, B=6, null=7}, "
                + "{A=3, B=6, null=3}, {A=3, B=6, null=null}]", VariationUtils.createAllVariations(supplyListMap).toString());

    }

    @Test
    void testVariationsSizeComputation() {
        Map<String, List<Integer>> supplyListMap = new LinkedHashMap<>();
        supplyListMap.put("A", Arrays.asList(1, 2, 3));

        assertEstimatedVariationsSize(supplyListMap);

        supplyListMap.put("C", Collections.emptyList());

        supplyListMap.put("B", Arrays.asList(1, 5, 6));

        assertEstimatedVariationsSize(supplyListMap);

        supplyListMap.put(null, Arrays.asList(7, 3, null));

        supplyListMap.put("D", Collections.emptyList());
        supplyListMap.put("E", Collections.emptyList());

        assertEstimatedVariationsSize(supplyListMap);
    }

    private static <K, V> void assertEstimatedVariationsSize(Map<K, List<V>> supplyMap) {
        assertEquals(VariationUtils.createAllVariations(supplyMap).size(), VariationUtils.computeNumberOfVariations(supplyMap));
    }

}
