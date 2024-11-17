//@formatter:off
/*
 * DefaultArgMetaInfoLookupTest
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.cnv.tps.DefaultArgMetaInfoLookup.BuilderStepConfigOrGet;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class DefaultArgMetaInfoLookupTest {

    @Test
    void testBasics() {

        // @formatter:off
        DefaultArgMetaInfoLookup lookup = DefaultArgMetaInfoLookup
                                        .withArg("arg6")
                                            .ofType(DefaultAdlType.STRING)
                                            .thatIsAlwaysKnown()
                                        .withArg("arg2")
                                            .ofType(DefaultAdlType.INTEGER)
                                            .thatIsCollection()
                                        .withArg("arg9")
                                            .ofType(DefaultAdlType.BOOL)
                                        .withArg("arg10")
                                            .ofType(DefaultAdlType.DECIMAL)
                                            .thatIsAlwaysKnown()
                                            .thatIsCollection()
                                        .withArg("arg23")
                                            .ofType(DefaultAdlType.STRING
                                                    .withFormatter("STRING-77", DefaultArgValueFormatter.STRING_IN_DOUBLE_QUOTES))
                                        .get();
        // @formatter:on

        assertEquals("""
                DefaultArgMetaInfoLookup {
                    arg10 -> (DECIMAL, isAlwaysKnown=true, isCollection=true)
                    arg2 -> (INTEGER, isAlwaysKnown=false, isCollection=true)
                    arg23 -> (STRING-77, isAlwaysKnown=false, isCollection=false)
                    arg6 -> (STRING, isAlwaysKnown=true, isCollection=false)
                    arg9 -> (BOOL, isAlwaysKnown=false, isCollection=false)
                }""", lookup.toString());

        assertEquals(DefaultAdlType.BOOL, lookup.typeOf("arg9"));
        assertFalse(lookup.isAlwaysKnown("arg23"));
        assertTrue(lookup.isAlwaysKnown("arg10"));
        assertTrue(lookup.isCollection("arg10"));
        assertEquals(DefaultAdlType.INTEGER, lookup.typeOf("arg2"));
        assertFalse(lookup.isAlwaysKnown("arg2"));

        assertThrows(LookupException.class, () -> lookup.lookup("fooBar"));

        BuilderStepConfigOrGet builder = DefaultArgMetaInfoLookup.withArg(null).ofType(DefaultAdlType.STRING);
        assertThrows(ConfigException.class, () -> builder.get());

        BuilderStepConfigOrGet builder2 = DefaultArgMetaInfoLookup.withArg("argName").ofType(null);
        assertThrows(ConfigException.class, () -> builder2.get());

        BuilderStepConfigOrGet builder3 = DefaultArgMetaInfoLookup.withArg("argName").ofType(DefaultAdlType.STRING).withArg("argName")
                .ofType(DefaultAdlType.STRING);
        assertThrows(ConfigException.class, () -> builder3.get());

    }

    @Test
    void testMapSetup() {

        Map<String, ArgMetaInfo> map = new HashMap<>();

        map.put("argName1", new ArgMetaInfo("argName1", DefaultAdlType.BOOL, false, true));
        map.put("argName2", new ArgMetaInfo("argName2", DefaultAdlType.INTEGER, false, true));

        DefaultArgMetaInfoLookup lookup = new DefaultArgMetaInfoLookup(map);
        assertEquals("""
                DefaultArgMetaInfoLookup {
                    argName1 -> (BOOL, isAlwaysKnown=false, isCollection=true)
                    argName2 -> (INTEGER, isAlwaysKnown=false, isCollection=true)
                }""", lookup.toString());

        map.put(null, new ArgMetaInfo("argName1", DefaultAdlType.BOOL, false, true));

        assertThrows(ConfigException.class, () -> new DefaultArgMetaInfoLookup(map));

        map.clear();

        map.put("argName1", new ArgMetaInfo("argName1", DefaultAdlType.BOOL, false, true));
        map.put("argName2", null);

        assertThrows(ConfigException.class, () -> new DefaultArgMetaInfoLookup(map));

        map.clear();

        map.put("argName1", new ArgMetaInfo("argName1", DefaultAdlType.BOOL, false, true));
        map.put("argName2", new ArgMetaInfo("argName99", DefaultAdlType.INTEGER, false, true));

        assertThrows(ConfigException.class, () -> new DefaultArgMetaInfoLookup(map));

    }

    @Test
    void testListSetup() {

        List<ArgMetaInfo> entries = new ArrayList<>();

        entries.add(new ArgMetaInfo("argName2", DefaultAdlType.INTEGER, false, true));
        entries.add(new ArgMetaInfo("argName1", DefaultAdlType.BOOL, false, true));

        DefaultArgMetaInfoLookup lookup = new DefaultArgMetaInfoLookup(entries);

        assertEquals("""
                DefaultArgMetaInfoLookup {
                    argName1 -> (BOOL, isAlwaysKnown=false, isCollection=true)
                    argName2 -> (INTEGER, isAlwaysKnown=false, isCollection=true)
                }""", lookup.toString());

        assertEquals(new ArgMetaInfo("argName2", DefaultAdlType.INTEGER, false, true), lookup.lookup("argName2"));
        assertEquals(new ArgMetaInfo("argName2", DefaultAdlType.INTEGER, false, true),
                lookup.lookup("argName2", new ArgMetaInfo("templateName", DefaultAdlType.INTEGER, false, true)));

        entries.add(null);

        assertThrows(ConfigException.class, () -> new DefaultArgMetaInfoLookup(entries));

        entries.remove(entries.size() - 1);
        entries.add(new ArgMetaInfo("argName1", DefaultAdlType.BOOL, false, true));

        assertThrows(ConfigException.class, () -> new DefaultArgMetaInfoLookup(entries));

    }
}
