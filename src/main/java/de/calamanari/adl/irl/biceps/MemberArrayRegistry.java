//@formatter:off
/*
 * MemberArrayRegistry
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for encoded combined expressions.
 * <p>
 * For performance reasons it was required to temporarily represent combined expressions in encoded form (int-arrays).<br>
 * The registry issues for any int-array (combinedNode) an id and keeps it in memory for later lookup.<br>
 * Because the information whether a combined node is an AND or an OR is part of the reference (not part of the cached payload), we can use the same member
 * array safely for both in parallel.
 * <p>
 * Temporarily created int-arrays are slowly piling up in the registry. Thus, {@link #triggerHousekeeping(int)} allows in safe situations to run the
 * housekeeping which sets any id currently not used in the given rootNode or its child nodes to <b>null</b>.<br>
 * In other words: the underlying list is never shrinking but the payloads (int-arrays) become subject to garbage collection.
 * <p>
 * We assume that the list never grows too far before the processing ends and the registry gets garbage-collected.<br>
 * The benefit of this lean approach is that the access (id = position in list) is extremely fast (compared to any map), and we don't need to deal with
 * difficult garbage collection questions in other parts of the code.
 * <p>
 * This registry uses the arrays themselves as keys for caching <i>on insert</i>. So, while inserts are expensive, lookups are extremely cheap. As there are way
 * more lookups (find member array related to id) than inserts (new member array resp. find id for existing one), the registry has a positive impact on the
 * overall performance. <br>
 * Having a guarantee to get the same ID for an equal combined node anywhere else within an expression tree also speeds up a couple of other operations.
 * <p>
 * The downside of the above approach is the increased responsibility of the users of this class to <b>never ever modify any of the arrays</b> after passing
 * them to the registry!
 * <p>
 * Instances are <b>not</b> safe to be accessed concurrently by multiple threads.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class MemberArrayRegistry implements Serializable {

    private static final long serialVersionUID = -2189220857484883834L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MemberArrayRegistry.class);

    /**
     * Threshold for housekeeping: We only try cleanup if the number of currently registered ids is &gt;= {@value}
     */
    private static final int HOUSEKEEPING_THRESHOLD = 1000;

    /**
     * This is the number of cleaned (unused) slots.
     */
    private int cleanedIdCount;

    /**
     * This list stores the registered int-arrays, the id is the list position. The list never shrinks.
     */
    private List<int[]> memberArrays;

    /**
     * Cache to avoid duplicate ids for the same member arrays within the same expression tree
     */
    private final Map<CacheEntry, Integer> cache;

    /**
     * For internal use: creates the registry from previously validated data
     * 
     * @param combinedNodes
     * @param cleanedIdCount
     * @param cache
     */
    private MemberArrayRegistry(List<int[]> combinedNodes, int cleanedIdCount, Map<CacheEntry, Integer> cache) {
        this.memberArrays = combinedNodes;
        this.cleanedIdCount = cleanedIdCount;
        this.cache = cache;
    }

    /**
     * Creates an empty registry
     */
    public MemberArrayRegistry() {
        this(new ArrayList<>(), 0, new HashMap<>());
    }

    /**
     * Registers the array of the given combined node and returns a unique id. All ids are positive and start with <code>0</code>, counting strictly increasing.
     * <p>
     * Any later request to register the logical identical array (same members in the same order) will return the same id.
     * <p>
     * <b>Note:</b> If any entry was subject to housekeeping (because it was unused), and later the same array (same members, same order) comes again it will
     * get a fresh id.
     * 
     * @param memberArray
     * @return unique id, so that the member-array can be looked up again later, see also
     *         {@link CoreExpressionCodec#encodeCombinedExpressionId(int, de.calamanari.adl.irl.NodeType)}
     */
    public int registerMemberArray(int[] memberArray) {
        CacheEntry cacheKey = new CacheEntry(memberArray);
        return cache.computeIfAbsent(cacheKey, _ -> registerNewMemberArray(memberArray));
    }

    /**
     * Issues a new id for the given array assuming it was not registered before.
     * 
     * @param memberArray
     * @return id for the given member array
     */
    private int registerNewMemberArray(int[] memberArray) {
        int id = memberArrays.size();
        memberArrays.add(memberArray);
        return id;
    }

    /**
     * Returns the member-array associated with the given id (the ID, not to be confused with the expression node that holds it!)
     * 
     * @param id see {@link CoreExpressionCodec#decodeCombinedExpressionId(int)}
     * @return member array
     * @throws IllegalStateException if the given combined node was already cleaned, see {@link #triggerHousekeeping(int)}
     * @throws IndexOutOfBoundsException if the given id is negative or if it was never issued by {@link #registerMemberArray(int[])}
     */
    public int[] lookupMemberArray(int id) {
        if (id < 0 || id >= memberArrays.size()) {
            throw new IndexOutOfBoundsException("Lookup of member array failed: Invalid id=" + id);
        }
        int[] res = memberArrays.get(id);
        if (res == null) {
            throw new IllegalStateException("Lookup of member array failed: Illegal attempt to access node that no longer exists: id=" + id);
        }
        return res;
    }

    /**
     * This method takes the given rootNode as entry point into a tree which contains all combined nodes still in use.<br>
     * Then it releases every id in the internal cache which is <i>not</i> in this list.
     * <p>
     * Note: The given node must be truly the root. If this method is called on any lower level, all parents (and thus the expression) will be destroyed.<br>
     * For the same reason you must call this method if your expression tree has currently more than one root!
     * 
     * @param rootNode entry point of an expression tree
     */
    public int triggerHousekeeping(int rootNode) {

        if ((memberArrays.size() - cleanedIdCount) < HOUSEKEEPING_THRESHOLD) {
            return 0;
        }

        List<Integer> validIds = new ArrayList<>(memberArrays.size());
        collectValidIds(rootNode, validIds);
        Collections.sort(validIds);
        int[] uniqueSortedIds = new int[validIds.size()];
        int numberOfUniqueIds = 0;
        int prevId = -1;
        for (int i = 0; i < validIds.size(); i++) {
            int id = validIds.get(i);
            if (i == 0 || id != prevId) {
                uniqueSortedIds[numberOfUniqueIds] = id;
                numberOfUniqueIds++;
            }
            prevId = id;
        }

        int res = 0;
        for (int id = 0; id < memberArrays.size(); id++) {
            if (memberArrays.get(id) != null && Arrays.binarySearch(uniqueSortedIds, 0, numberOfUniqueIds, id) < 0) {
                memberArrays.set(id, null);
                cleanedIdCount++;
                res++;
            }

        }

        if (res > 0) {
            // cleanup cache as well
            List<Map.Entry<CacheEntry, Integer>> cachedIds = new ArrayList<>(cache.entrySet());
            for (Map.Entry<CacheEntry, Integer> entry : cachedIds) {
                if (!isValidId(entry.getValue())) {
                    cache.remove(entry.getKey());
                }
            }

        }

        LOGGER.debug("Number of ids issued: {}, currently in use: {}", memberArrays.size(), numberOfUniqueIds);
        return res;
    }

    /**
     * Recursively collects all ids still in use based on the given node and its siblings
     * 
     * @param node current node
     * @param validIdCollection result collection, initially empty
     */
    private void collectValidIds(int node, List<Integer> validIdCollection) {
        if (CoreExpressionCodec.isCombinedExpressionId(node)) {
            validIdCollection.add(CoreExpressionCodec.decodeCombinedExpressionId(node));
            for (int member : lookupMemberArray(CoreExpressionCodec.decodeCombinedExpressionId(node))) {
                collectValidIds(member, validIdCollection);
            }
        }
    }

    /**
     * This method returns the number of ids currently in the registry (not yet been removed by housekeeping)
     * 
     * @return number of valid node ids
     */
    public int getNumberOfValidIds() {
        return memberArrays.size() - cleanedIdCount;
    }

    /**
     * @param id see {@link CoreExpressionCodec#decodeCombinedExpressionId(int)}
     * @return true if the given id is still valid, false if it was subject to housekeeping and is now invalid
     */
    public boolean isValidId(int id) {
        return memberArrays.get(id) != null;
    }

    /**
     * This method clears this instance which invalidates all the previously issued ids. New ids will start from <code>0</code>
     */
    public void clear() {
        // we replace the list to ensure the initial size
        memberArrays = new ArrayList<>();
        cleanedIdCount = 0;
        cache.clear();
    }

    /**
     * This method returns a copy of this registry, so that both can evolve independently.
     * <p>
     * As mentioned earlier: The implementation of {@link MemberArrayRegistry} heavily <i>relies</i> on the fact that member-int-arrays are never subject to
     * modification and thus safe to be used as cache-keys. For the same reason this method does not clone these arrays but only copies the management
     * structures.
     * 
     * @return copy
     */
    public MemberArrayRegistry copy() {
        return new MemberArrayRegistry(new ArrayList<>(memberArrays), cleanedIdCount, new HashMap<>(cache));
    }

    /**
     * A cache entry gives a cached integer array an identity (equals + hashcode) based on its members and their order.
     * 
     */
    private static final class CacheEntry implements Serializable {

        private static final long serialVersionUID = -594363567854023827L;

        final int[] members;

        private final int hashCode;

        private CacheEntry(int[] members, int hashCode) {
            this.members = members;
            this.hashCode = hashCode;
        }

        CacheEntry(int[] members) {
            this(members, Arrays.hashCode(members));
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
            CacheEntry other = (CacheEntry) obj;
            return this.hashCode == other.hashCode && Arrays.equals(members, other.members);
        }

    }

}
