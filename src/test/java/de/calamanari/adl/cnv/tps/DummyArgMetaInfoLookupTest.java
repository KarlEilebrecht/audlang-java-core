//@formatter:off
/*
 * DummyArgMetaInfoLookupTest
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

package de.calamanari.adl.cnv.tps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.DeepCopyUtils;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class DummyArgMetaInfoLookupTest {

    @Test
    void testBasics() {

        DummyArgMetaInfoLookup dummy = DummyArgMetaInfoLookup.getInstance();

        DummyArgMetaInfoLookup dummy2 = DummyArgMetaInfoLookup.getInstance();

        assertSame(dummy, dummy2);

        assertTrue(dummy.contains("foo"));
        assertFalse(dummy.isAlwaysKnown("foo"));
        assertFalse(dummy.isAlwaysKnown("foo2"));
        assertFalse(dummy.isCollection("foo"));
        assertFalse(dummy.isCollection("foo2"));

        assertEquals(DefaultAdlType.STRING, dummy.typeOf("bla"));

        assertEquals(new ArgMetaInfo("foobar", DefaultAdlType.STRING, false, false), dummy.lookup("foobar"));
        assertEquals(new ArgMetaInfo("foobar", DefaultAdlType.STRING, false, false), dummy.lookup("foobar"));
        assertEquals(new ArgMetaInfo("foobar", DefaultAdlType.STRING, false, false),
                dummy.lookup("foobar", new ArgMetaInfo("foobar", DefaultAdlType.STRING, true, true)));

        assertSame(dummy, DeepCopyUtils.deepCopy(dummy));

        ArgMetaInfo fallback = new ArgMetaInfo("foobar", DefaultAdlType.STRING, false, false);

        assertThrows(IllegalArgumentException.class, () -> dummy.contains(null));
        assertThrows(IllegalArgumentException.class, () -> dummy.typeOf(null));
        assertThrows(IllegalArgumentException.class, () -> dummy.lookup(null));
        assertThrows(IllegalArgumentException.class, () -> dummy.lookup(null, fallback));
        assertThrows(IllegalArgumentException.class, () -> dummy.isCollection(null));
        assertThrows(IllegalArgumentException.class, () -> dummy.isAlwaysKnown(null));

    }

}
