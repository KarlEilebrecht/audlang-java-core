//@formatter:off
/*
 * AudlangExpression
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface AudlangExpression<T extends AudlangExpression<T, V>, V> extends AudlangFieldAware, AudlangFormattable, Comparable<T>, Serializable {

    @Override
    default int compareTo(T o) {
        return this.toString().compareTo(String.valueOf(o));
    }

    /**
     * @return duplicate-free list of all fields referenced in an expression, ordered by argName, values and references are ordered
     */
    @Override
    default List<AudlangField> allFields() {
        Map<String, AudlangField.Builder> map = new TreeMap<>();
        collectFieldsInternal(map);
        return map.values().stream().map(AudlangField.Builder::get).toList();
    }

    /**
     * @param fieldMap map to collect all fields recursively in the given map, by default a no-op
     */
    default void collectFieldsInternal(Map<String, AudlangField.Builder> fieldMap) {
        // no-op
    }

    /**
     * @return list with all <b>direct</b> member expressions or {@link Collections#equals(Object)} if there is no child (default implementation)
     */
    default List<T> childExpressions() {
        return Collections.emptyList();
    }

    /**
     * This method traverses the expression recursively starting with this expression and adds all expressions that meet the filter condition to the given
     * result list.
     * <p/>
     * <b>Clarification</b>: Even <i>this</i> expression will be added to the result if applicable.
     * 
     * @param filter
     * @param result
     */
    default void collectExpressions(Predicate<T> filter, List<T> result) {

        @SuppressWarnings("unchecked")
        T thisCasted = (T) this;

        if (filter.test(thisCasted)) {
            result.add(thisCasted);
        }
        childExpressions().forEach(child -> child.collectExpressions(filter, result));

    }

    /**
     * This method traverses the expression recursively starting with this expression and adds all expressions that meet the filter condition to the given
     * result list. Effectively a shorthand for creating an empty list and calling {@link #collectExpressions(Predicate, List)}
     * <p/>
     * <b>Clarification</b>: Even <i>this</i> expression will be added to the result if applicable.
     * 
     * @param filter
     * @param result ordered, <b>mutable</b>
     */
    default List<T> collectExpressions(Predicate<T> filter) {
        List<T> res = new ArrayList<>();
        collectExpressions(filter, res);
        Collections.sort(res);
        return res;
    }

    /**
     * This method traverses the expression recursively starting with this expression and adds all expressions that meet the filter condition to a list.
     * <p/>
     * <b>Clarification</b>: Even <i>this</i> expression will be added to the result if applicable.
     * 
     * @param filter
     * @param result (ordered, duplicates removed, <b>mutable</b>)
     */
    default List<T> collectExpressionsUnique(Predicate<T> filter) {
        return new ArrayList<>(collectExpressions(filter).stream().distinct().toList());
    }

    /**
     * @return depth of this expression &gt;=1 (leaf, no children), each nested level adds 1
     */
    default int depth() {
        if (childExpressions().isEmpty()) {
            return 1;
        }
        else {
            return childExpressions().stream().mapToInt(AudlangExpression::depth).max().getAsInt() + 1;
        }
    }

    /**
     * Lets the visitor visit this expression and recursively all its child expressions.
     * 
     * @param visitor
     */
    void accept(V visitor);

}
