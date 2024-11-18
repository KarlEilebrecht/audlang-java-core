//@formatter:off
/*
 * GrowingIntArrayTest
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class GrowingIntArrayTest {

    @Test
    void testBasics() {

        GrowingIntArray gra = new GrowingIntArray();

        assertEquals(0, gra.size());
        assertTrue(gra.isEmpty());
        assertEquals("[]", gra.toString());

        gra.add(5);
        assertEquals(1, gra.size());
        assertEquals(5, gra.get(0));
        assertEquals("[5]", gra.toString());
        assertArrayEquals(new int[] { 5 }, gra.toArray());

        gra.clear();
        assertEquals(0, gra.size());
        assertTrue(gra.isEmpty());
        assertEquals("[]", gra.toString());

        gra.add(7);
        gra.add(8);
        gra.add(9);
        assertEquals(3, gra.size());
        assertEquals(7, gra.get(0));
        assertEquals("[7, 8, 9]", gra.toString());
        assertArrayEquals(new int[] { 7, 8, 9 }, gra.toArray());

        gra.set(0, 1);
        gra.set(2, 10);
        assertEquals(3, gra.size());
        assertEquals(1, gra.get(0));
        assertEquals("[1, 8, 10]", gra.toString());
        assertArrayEquals(new int[] { 1, 8, 10 }, gra.toArray());

        assertEquals(1, gra.indexOf(8));

    }

    @Test
    void testSort() {

        GrowingIntArray gra = new GrowingIntArray();

        gra.add(7);
        gra.add(8);
        gra.add(9);
        assertEquals(3, gra.size());
        assertEquals(7, gra.get(0));
        assertEquals("[7, 8, 9]", gra.toString());
        assertArrayEquals(new int[] { 7, 8, 9 }, gra.toArray());

        gra.add(2);
        assertEquals(4, gra.size());
        assertEquals(7, gra.get(0));
        assertEquals("[7, 8, 9, 2]", gra.toString());
        assertArrayEquals(new int[] { 7, 8, 9, 2 }, gra.toArray());

        gra.sort();
        assertEquals(4, gra.size());
        assertEquals(2, gra.get(0));
        assertEquals("[2, 7, 8, 9]", gra.toString());
        assertArrayEquals(new int[] { 2, 7, 8, 9 }, gra.toArray());

        gra.set(0, 1);
        gra.set(3, 10);
        assertEquals(4, gra.size());
        assertEquals(1, gra.get(0));
        assertEquals("[1, 7, 8, 10]", gra.toString());
        assertArrayEquals(new int[] { 1, 7, 8, 10 }, gra.toArray());

        assertEquals(1, gra.binarySearch(7));

    }

    @Test
    void testSetLength() {

        GrowingIntArray gra = new GrowingIntArray();

        gra.add(7);
        gra.add(8);
        gra.add(9);
        assertEquals(3, gra.size());
        assertEquals(7, gra.get(0));
        assertEquals("[7, 8, 9]", gra.toString());
        assertArrayEquals(new int[] { 7, 8, 9 }, gra.toArray());

        gra.add(2);
        assertEquals(4, gra.size());
        assertEquals(7, gra.get(0));
        assertEquals("[7, 8, 9, 2]", gra.toString());
        assertArrayEquals(new int[] { 7, 8, 9, 2 }, gra.toArray());

        gra.setLength(2);
        assertEquals(2, gra.size());
        assertEquals(7, gra.get(0));
        assertEquals("[7, 8]", gra.toString());
        assertArrayEquals(new int[] { 7, 8 }, gra.toArray());

    }

    @Test
    void testCopy() {

        int[] arr = new int[] { 1, 2, 3, 4, 5 };

        GrowingIntArray gra = new GrowingIntArray(arr, true);

        gra.set(0, 8);

        assertEquals(1, arr[0]);

        gra = new GrowingIntArray(arr, false);

        gra.set(0, 8);

        assertEquals(8, arr[0]);

        gra.ensureCapacity(5);

        gra.addAll(arr);
        assertArrayEquals(new int[] { 8, 2, 3, 4, 5, 8, 2, 3, 4, 5 }, gra.toArray());

        gra.set(0, 9);

        assertEquals(8, arr[0]);

        GrowingIntArray gra2 = gra.copy();
        gra2.set(1, 15);

        assertEquals(2, gra.get(1));
        assertEquals(15, gra2.get(1));

        gra = new GrowingIntArray(arr, true);
        assertArrayEquals(arr, gra.toArray());
        assertNotSame(arr, gra.toArray());

    }

    @Test
    void testAddAll() {

        GrowingIntArray gra = new GrowingIntArray();

        gra.addAll(new int[] { 1, 2, 3, 4 });
        gra.addAll(new int[] { 4, 5, 6, 7, 8 }, 1, 3);
        gra.addAll(new int[] { 4, 5, 6, 7, 8 }, 1, 1);
        gra.addAll(new GrowingIntArray(new int[] { 7, 8, 9 }, false));
        gra.addAll(new GrowingIntArray(new int[] { 8, 9, 10, 11, 12, 13 }, false), 2, 3);
        gra.addAll(new GrowingIntArray(new int[] { 8, 9, 10, 11, 12, 13 }, false), 1, 1);

        assertArrayEquals(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, gra.toArray());

    }

    @Test
    void testSpecialCases() {

        assertThrows(IllegalArgumentException.class, () -> new GrowingIntArray(-1));

        GrowingIntArray gra = new GrowingIntArray(10);

        assertThrows(IndexOutOfBoundsException.class, () -> gra.setLength(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> gra.setLength(11));

        int[] arrToBeAdded = new int[] { 7, 8, 9 };
        GrowingIntArray graToBeAdded = new GrowingIntArray(arrToBeAdded, false);

        assertThrows(IndexOutOfBoundsException.class, () -> gra.addAll(arrToBeAdded, -1, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> gra.addAll(arrToBeAdded, 0, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> gra.addAll(graToBeAdded, -1, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> gra.addAll(graToBeAdded, 0, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> graToBeAdded.get(5));

    }

}
