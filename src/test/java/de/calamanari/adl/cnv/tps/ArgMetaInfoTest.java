//@formatter:off
/*
 * ArgMetaInfoTest
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class ArgMetaInfoTest {

    @Test
    void testBasics() {

        ArgMetaInfo info = new ArgMetaInfo("argName", DefaultAdlType.STRING, false, false);

        assertEquals("argName", info.argName());
        assertEquals("STRING", info.type().name());
        assertFalse(info.isAlwaysKnown());
        assertFalse(info.isCollection());

        info = new ArgMetaInfo("argName2", DefaultAdlType.INTEGER, true, false);

        assertEquals("argName2", info.argName());
        assertEquals("INTEGER", info.type().name());
        assertTrue(info.isAlwaysKnown());
        assertFalse(info.isCollection());

        info = new ArgMetaInfo("argName3", DefaultAdlType.BOOL, false, true);

        assertEquals("argName3", info.argName());
        assertEquals("BOOL", info.type().name());
        assertFalse(info.isAlwaysKnown());
        assertTrue(info.isCollection());

        info = new ArgMetaInfo("argName4", DefaultAdlType.DATE, true, true);

        assertEquals("argName4", info.argName());
        assertEquals("DATE", info.type().name());
        assertTrue(info.isAlwaysKnown());
        assertTrue(info.isCollection());

    }

    @Test
    void testSpecialCase() {

        assertThrows(ConfigException.class, () -> new ArgMetaInfo(null, DefaultAdlType.INTEGER, true, false));
        assertThrows(ConfigException.class, () -> new ArgMetaInfo("", DefaultAdlType.INTEGER, true, false));
        assertThrows(ConfigException.class, () -> new ArgMetaInfo("argName", null, true, true));
    }

}
