//@formatter:off
/*
 * QualifiedArgValueTest
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

package de.calamanari.adl.cnv;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.DeepCopyUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class QualifiedArgValueTest {

    @Test
    void testBasics() {

        QualifiedArgValue value1 = new QualifiedArgValue("a", "b");
        QualifiedArgValue value2 = new QualifiedArgValue("a", "b");

        assertEquals(value1, value2);

        value1 = new QualifiedArgValue("a", "");
        value2 = new QualifiedArgValue("a", "");

        assertEquals(value1, value2);

        value1 = new QualifiedArgValue("a", null);
        value2 = new QualifiedArgValue("a", null);

        assertEquals(value1, value2);

        value1 = new QualifiedArgValue("arg", "value");
        value2 = new QualifiedArgValue("arg", "value");

        assertEquals(value1, value2);

        value2 = new QualifiedArgValue("arg", "value2");

        assertNotEquals(value1, value2);

        assertNotSame(value1, DeepCopyUtils.deepCopy(value1));

        assertEquals(value1, DeepCopyUtils.deepCopy(value1));

    }

    @Test
    void testCompare() {

        QualifiedArgValue value1 = new QualifiedArgValue("a", "b");
        QualifiedArgValue value2 = new QualifiedArgValue("a", "b");

        assertEquals(0, value1.compareTo(value2));
        assertEquals(0, value2.compareTo(value1));

        value1 = new QualifiedArgValue("a", "");
        value2 = new QualifiedArgValue("a", "");

        assertEquals(0, value1.compareTo(value2));
        assertEquals(0, value2.compareTo(value1));

        value1 = new QualifiedArgValue("a", null);
        value2 = new QualifiedArgValue("a", null);

        assertEquals(0, value1.compareTo(value2));
        assertEquals(0, value2.compareTo(value1));

        value1 = new QualifiedArgValue("arg", "value");
        value2 = new QualifiedArgValue("arg", "value");

        assertEquals(0, value1.compareTo(value2));
        assertEquals(0, value2.compareTo(value1));

        value1 = new QualifiedArgValue("arg", "value");
        value2 = new QualifiedArgValue("arg1", "value");

        assertEquals("arg".compareTo("arg1"), value1.compareTo(value2));
        assertEquals("arg1".compareTo("arg"), value2.compareTo(value1));

        value1 = new QualifiedArgValue("arg", "value");
        value2 = new QualifiedArgValue("arg", "value2");

        assertEquals("value".compareTo("value2"), value1.compareTo(value2));
        assertEquals("value2".compareTo("value"), value2.compareTo(value1));

        value1 = new QualifiedArgValue("arg", null);
        value2 = new QualifiedArgValue("arg", "value2");

        assertEquals(-1, value1.compareTo(value2));
        assertEquals(1, value2.compareTo(value1));

    }

    @Test
    void testSpecialCase() {

        assertThrows(IllegalArgumentException.class, () -> new QualifiedArgValue(null, null));
        assertThrows(IllegalArgumentException.class, () -> new QualifiedArgValue(null, "a"));

    }

}
