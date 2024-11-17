//@formatter:off
/*
 * VariationUtils
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic implementation of a mechanism to produce variations based on a given list of elements.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class VariationUtils {

    /**
     * This method creates a list of all possible sublists of the supply list with the requested size
     * <p/>
     * <b>Note:</b> Should the given list contain duplicates then each position is treated as a separate identity. <br/>
     * For example: given <code>A, A, A, D</code> there is a list of sub-lists of length=3: <code>(A, A, A), (A, A, D), (A, A, D), (A, A, D)</code><br/>
     * In other words: <i>The number of returned sub-lists solely depends on the number of supplied elements and the target size.</i>
     * <p/>
     * <b>Important:</b>It is highly recommended to call {@link #computeNumberOfSubLists(List, int)} <i>before</> calling this method to avoid hangs or running
     * out of memory!
     * 
     * 
     * @param supplyList pool of available elements
     * @param targetSize requested sublist candidate size
     * @return list of sublists with the requested target size
     */
    public static <T> List<List<T>> createSubLists(List<T> supplyList, int targetSize) {
        List<List<T>> results = new ArrayList<>();
        createVariations(supplyList, 0, new ArrayList<>(), targetSize, results);
        return results;
    }

    /**
     * Computes the number of sub-lists expected to be returned by {@link #createSubLists(List, int)} with the same arguments.
     * <p/>
     * The number of sub-lists equals the <a href="https://en.wikipedia.org/wiki/Binomial_coefficient">binomial coefficient<a> <b><code>n</code></b> over
     * <b><code>k</code></b> with <b><code>n := supplyList.size()</code></b> and <b><code>k := targetSize</code></b>.
     * 
     * @param supplyList
     * @param targetSize
     * @return number of sub-lists to be expected or -1 if the result would exceed {@link Integer#MAX_VALUE}
     */
    public static int computeNumberOfSubLists(List<?> supplyList, int targetSize) {
        long n = supplyList.size();
        long nFact = factorial(n);
        long k = targetSize;
        long kFact = factorial(k);
        long nMinusKFact = factorial(n - k);

        if (nFact < 0 || kFact < 0 || nMinusKFact < 0) {
            return -1;
        }

        long res = (nFact / (kFact * nMinusKFact));

        if (res < 0 || res > Integer.MAX_VALUE) {
            return -1;
        }
        else {
            return (int) res;
        }

    }

    /**
     * @param n
     * @return factorial of n or -1 if n < 0 or in case of overflow
     */
    private static long factorial(long n) {
        if (n < 0) {
            return -1;
        }
        long res = 1;
        for (long factor = 2; factor <= n; factor++) {
            res = res * factor;
            if (res < 0) {
                return -1;
            }
        }
        return res;
    }

    /**
     * This method takes a map of keys mapped to a given number of values and produces a list of maps of all subsets of unique key-value maps.
     * <p/>
     * <i>Example:</i>
     * <ul>
     * <li>A given <code>supplyMap</code> maps the key <b><code>A</code></b> to the value list <b><code>[1, 2, 3]</code></b> and the key <b><code>B</code></b>
     * to the value list <b><code>[4, 5, 6]</code></b></li>
     * <li>The result would be a list of maps:
     * <code>[{A=1, B=4}, {A=1, B=5}, {A=1, B=6}, {A=2, B=4}, {A=2, B=5}, {A=2, B=6}, {A=3, B=4}, {A=3, B=5}, {A=3, B=6}]</code></li>
     * </ul>
     * <p/>
     * The order of the keys (and thus the subsets, strictly left-to-right) depends on the order of the keys in the given supply map.
     * <p/>
     * There are no preliminary assumptions about the key and value types. Keys as well as values can be null. <br/>
     * However, the lists assigned to keys in the supplyMap must not be null (otherwise NPE).
     * <p/>
     * Should one of the value lists assigned to a key be empty then this key won't appear in the generated result maps.
     * <p>
     * The number of returned variations equals the product of the size of all supplied option lists. In the example above, the number of variations is
     * <code>3 x 3 = 9</code>.
     * <p/>
     * <b>Note:</b> Should the list of options (supplied list) contain any duplicates then each position counts as a separate option. Let's say there is a
     * <code>supplyMap</code> with the key <b><code>A</code></b> mapped to the value list <b><code>[1, 2]</code></b> and the key <b><code>B</code></b> to the
     * value list <b><code>[4, 4]</code></b>, then the result would be <code>[{A=1, B=4}, {A=1, B=4}, {A=2, B=4}, {A=2, B=4}]</code>.
     * <p/>
     * <b>Important:</b>It is highly recommended to call {@link #computeNumberOfVariations(Map)} <i>before</> calling this method to avoid hangs or running out
     * of memory!
     * 
     * @param <K>
     * @param <V>
     * @param supplyMap
     * @return list with map variations
     */
    public static <K, V> List<Map<K, V>> createAllVariations(Map<K, List<V>> supplyMap) {
        if (supplyMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<K> supplyKeys = new ArrayList<>(supplyMap.keySet());
        List<List<V>> supplyValueLists = new ArrayList<>(supplyMap.values());
        List<Map<K, V>> res = new ArrayList<>();
        createAllVariations(supplyKeys, supplyValueLists, 0, Collections.emptyMap(), res);
        return res;
    }

    /**
     * This method returns the number of variations expected for the given supply map when calling {@link #createAllVariations(Map)}.
     * 
     * @param supplyMap
     * @return number of variations or <b>-1</b> if the number exceeds {@link Integer#MAX_VALUE}
     */
    public static <K, V> int computeNumberOfVariations(Map<K, List<V>> supplyMap) {
        long res = 0;
        for (Map.Entry<K, List<V>> entry : supplyMap.entrySet()) {
            int numberOfOptions = entry.getValue().size();
            if (res == 0) {
                res = numberOfOptions;
            }
            else if (numberOfOptions > 0) {
                res = res * numberOfOptions;
            }
            if (res > Integer.MAX_VALUE) {
                return -1;
            }
        }
        return (int) res;
    }

    /**
     * Recursive implementation to create all map subsets
     * 
     * @param <K>
     * @param <V>
     * @param supplyKeys all keys in order
     * @param supplyValueLists value lists for all the keys in the same order
     * @param supplyKeyIdx current position in the list of keys
     * @param parentCandidate candidate created by the recursive iteration before
     * @param results all final map subsets
     */
    private static <K, V> void createAllVariations(List<K> supplyKeys, List<List<V>> supplyValueLists, int supplyKeyIdx, Map<K, V> parentCandidate,
            List<Map<K, V>> results) {

        List<V> supplyValueList = supplyValueLists.get(supplyKeyIdx);

        while (supplyValueList.isEmpty()) {
            supplyKeyIdx++;
            if (supplyKeyIdx < supplyKeys.size()) {
                supplyValueList = supplyValueLists.get(supplyKeyIdx);
            }
            else {
                results.add(parentCandidate);
                break;
            }
        }

        for (int i = 0; i < supplyValueList.size(); i++) {
            Map<K, V> candidate = new LinkedHashMap<>(parentCandidate);
            candidate.put(supplyKeys.get(supplyKeyIdx), supplyValueList.get(i));
            if (supplyKeyIdx < supplyKeys.size() - 1) {
                createAllVariations(supplyKeys, supplyValueLists, supplyKeyIdx + 1, candidate, results);
            }
            else {
                results.add(candidate);
            }
        }

    }

    /**
     * This method creates unique variations recursively left to right based on the given candidate (initially empty) and a supply list with available elements.
     * 
     * @param supplyList pool of available elements
     * @param supplyListIdx current position in the pool, pending are the elements to the right
     * @param candidate the list which we are currently building
     * @param targetSize requested final sublist size
     * @param results collection of sublists
     */
    private static <T> void createVariations(List<T> supplyList, int supplyListIdx, List<T> parentCandidate, int targetSize, List<List<T>> results) {
        for (int i = supplyListIdx; i < supplyList.size(); i++) {
            List<T> candidate = new ArrayList<>(parentCandidate);
            candidate.add(supplyList.get(i));
            if (candidate.size() < targetSize && haveEnoughElements(supplyList, supplyListIdx, candidate, targetSize)) {
                // we take each element as the starting point of the subsequent search, and the search
                // is strictly left-to right, this way we ensure we look at each alternative combination once and once only
                createVariations(supplyList, i + 1, candidate, targetSize, results);
            }
            else if (candidate.size() == targetSize) {
                results.add(candidate);
            }
        }
    }

    /**
     * Checks if there are enough pending (unused, left to right) elements in the supply list to finish a sublist (candidate) of the requested length.
     * 
     * @param supplyList pool of available elements
     * @param supplyListIdx current position in the pool, pending are the elements to the right
     * @param candidate the list which we are currently building
     * @param targetSize requested final sublist size
     * @return true if we can complete the candidate with the remaining supply list elements
     */
    private static <T> boolean haveEnoughElements(List<T> supplyList, int supplyListIdx, List<T> candidate, int targetSize) {
        int required = targetSize - candidate.size();
        int available = supplyList.size() - supplyListIdx;
        return required <= available;
    }

    private VariationUtils() {
        // static utility
    }

}
