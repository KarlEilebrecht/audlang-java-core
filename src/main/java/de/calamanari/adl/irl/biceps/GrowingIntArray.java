//@formatter:off
/*
 * GrowingIntArray
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

/**
 * Lean helper structure to store int-arrays before knowing the exact number.
 * <p/>
 * The array underneath grows infinitely. To avoid dealing with wrapper objects this class does not implement the List interface.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class GrowingIntArray {

    /**
     * Fixed increment in case the existing array is too small
     */
    public static final int SIZE_INCREMENT = 100;

    /**
     * Default initial capacity
     */
    public static final int DEFAULT_START_SIZE = 10;

    private int[] data;

    /**
     * Current position and effective length
     */
    private int idx = 0;

    public GrowingIntArray() {
        this.data = new int[DEFAULT_START_SIZE];
    }

    /**
     * Creates a new array with the given reserve for new elements
     * 
     * @param initialCapacity
     */
    public GrowingIntArray(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity must not be negative, given: " + initialCapacity);
        }
        this.data = new int[initialCapacity];
    }

    /**
     * @param source
     * @param copy if false the given array will be wrapped instead of creating a copy
     */
    public GrowingIntArray(int[] source, boolean copy) {
        if (copy) {
            this.data = Arrays.copyOf(source, source.length);
        }
        else {
            this.data = source;
        }
        idx = this.data.length;
    }

    /**
     * This method allows to reduce the length (virtually discard trailing members)
     * 
     * @param newLength
     */
    public void setLength(int newLength) {
        if (newLength < 0 || newLength > idx) {
            throw new IndexOutOfBoundsException(String.format("Expected: 0 <= newLength < %d, given: %d", this.idx, newLength));
        }
        idx = newLength;
    }

    /**
     * @return number of values in the array
     */
    public int size() {
        return idx;
    }

    /**
     * Ensures that the internal array has enough space to add the expected number of <i>additional</i> elements starting at the current position.
     * 
     * @param expectedNumberOfElements
     */
    public void ensureCapacity(int expectedNumberOfElements) {
        if (idx + expectedNumberOfElements > data.length) {
            data = Arrays.copyOf(data, idx + expectedNumberOfElements);
        }
    }

    /**
     * Adds a value to the end of the array and increases the size
     * 
     * @param value
     */
    public void add(int value) {
        if (idx == data.length) {
            ensureCapacity(SIZE_INCREMENT);
        }
        data[idx] = value;
        idx++;
    }

    /**
     * Shorthand for adding all the given members at once
     * 
     * @param members to be added
     */
    public void addAll(int[] members) {
        ensureCapacity(members.length);
        for (int member : members) {
            add(member);
        }
    }

    /**
     * Shorthand for adding all the given members at once
     * 
     * @param members to be added
     * @param fromIdx start idx in source array (inclusive)
     * @patam toIdx end idx in source array (exclusive)
     */
    public void addAll(int[] members, int fromIdx, int toIdx) {
        if (fromIdx < 0 || toIdx > members.length || fromIdx > toIdx) {
            throw new ArrayIndexOutOfBoundsException(String.format(
                    "Expecting 0 <= fromIdx <= toIdx <= members.length, given: members.length=%s, fromIdx=%s, toIdx=%s", members.length, fromIdx, toIdx));
        }
        if (fromIdx == toIdx) {
            return;
        }
        ensureCapacity(toIdx - fromIdx);
        for (int i = fromIdx; i < toIdx; i++) {
            data[idx] = members[i];
            idx++;
        }
    }

    /**
     * @param idx to be validated
     */
    private void assertIndexInRange(int idx) {
        if (idx < 0 || idx >= this.idx) {
            throw new IndexOutOfBoundsException(String.format("Expected: 0 <= idx < %d, given: %d", this.idx, idx));
        }
    }

    /**
     * @param idx
     * @return value stored at the given position
     */
    public int get(int idx) {
        assertIndexInRange(idx);
        return data[idx];
    }

    /**
     * Replaces the value at the given position with the given one
     * 
     * @param idx
     * @param value
     */
    public void set(int idx, int value) {
        assertIndexInRange(idx);
        data[idx] = value;
    }

    /**
     * @return <b>copy</b> of the internal array of the effective length
     */
    public int[] toArray() {
        return Arrays.copyOf(data, idx);
    }

    /**
     * sorts the elements in this array (ascending order)
     */
    public void sort() {
        Arrays.sort(data, 0, idx);
    }

    /**
     * Performs a binary search on the internal array
     * 
     * @param member <b>must be sorted</b>, see also {@link #sort()}
     * @return 0 or positive if found, negative value otherwise
     */
    public int binarySearch(int member) {
        return Arrays.binarySearch(data, 0, idx, member);
    }

    /**
     * Linear search for the given member
     * 
     * @param member search candidate
     * @return position or -1 if not found
     */
    public int indexOf(int member) {
        for (int i = 0; i < idx; i++) {
            if (data[i] == member) {
                return idx;
            }
        }
        return -1;
    }

    /**
     * Clears this array, so {@link #size()} will return 0.
     */
    public void clear() {
        idx = 0;
    }

    /**
     * @return true if this array is empty
     */
    public boolean isEmpty() {
        return idx == 0;
    }

    /**
     * @return deep copy, unrelated to this instance
     */
    public GrowingIntArray copy() {
        return new GrowingIntArray(this.toArray(), false);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < idx; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(data[i]);
        }
        sb.append("]");
        return sb.toString();
    }

}
