//@formatter:off
/*
 * ArgNameValueMappingTest
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.DeepCopyUtils;
import de.calamanari.adl.cnv.ArgNameValueMapping.Builder;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class ArgNameValueMappingTest {

    @Test
    void testBasics() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("srcArg1", "blue", "destArg1", "light blue")
            .withMapping("srcArg2", "nice", "destArg2", "good")
            .withMapping("srcArg1", "red", "destArg1", "dark red")
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .get();
        // @formatter:on

        assertEquals(7, argNameValueMapping.mappings().size());

        assertEquals(new QualifiedArgValue("destArg1", "light blue"), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg1", "blue")));
        assertEquals(new QualifiedArgValue("destArg2", "good"), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg2", "nice")));
        assertEquals(new QualifiedArgValue("destArg1", "dark red"), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg1", "red")));
        assertEquals(new QualifiedArgValue("destArg3", "snowy"), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg3", "icy")));
        assertEquals(new QualifiedArgValue("destArg1", null), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg1", null)));
        assertEquals(new QualifiedArgValue("destArg2", null), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg2", null)));
        assertEquals(new QualifiedArgValue("destArg3", null), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg3", null)));

        assertEquals("""
                ArgNameValueMapping {
                    (srcArg1, null) -> (destArg1, null)
                    (srcArg1, blue) -> (destArg1, light blue)
                    (srcArg1, red) -> (destArg1, dark red)
                    (srcArg2, null) -> (destArg2, null)
                    (srcArg2, nice) -> (destArg2, good)
                    (srcArg3, null) -> (destArg3, null)
                    (srcArg3, icy) -> (destArg3, snowy)
                }""", argNameValueMapping.toString());

        assertEquals("ArgNameValueMapping {}", ArgNameValueMapping.create().get().toString());

        assertNotSame(argNameValueMapping, DeepCopyUtils.deepCopy(argNameValueMapping));

        assertEquals(argNameValueMapping, DeepCopyUtils.deepCopy(argNameValueMapping));

        assertEquals(argNameValueMapping.toString(), DeepCopyUtils.deepCopy(argNameValueMapping).toString());

    }

    @Test
    void testMulti() {

        Map<String, String> valueMap1 = new HashMap<>();
        valueMap1.put("blue", "light blue");
        valueMap1.put("red", "dark red");

        Map<String, String> valueMap2 = new HashMap<>();
        valueMap2.put("nice", "good");

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMappings("srcArg1", "destArg1", valueMap1)
            .withMappings("srcArg2", "destArg2", valueMap2)
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .get();
        // @formatter:on

        assertEquals(7, argNameValueMapping.mappings().size());

        assertEquals(new QualifiedArgValue("destArg1", "light blue"), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg1", "blue")));
        assertEquals(new QualifiedArgValue("destArg2", "good"), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg2", "nice")));
        assertEquals(new QualifiedArgValue("destArg1", "dark red"), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg1", "red")));
        assertEquals(new QualifiedArgValue("destArg3", "snowy"), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg3", "icy")));
        assertEquals(new QualifiedArgValue("destArg1", null), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg1", null)));
        assertEquals(new QualifiedArgValue("destArg2", null), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg2", null)));
        assertEquals(new QualifiedArgValue("destArg3", null), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg3", null)));

        assertEquals("""
                ArgNameValueMapping {
                    (srcArg1, null) -> (destArg1, null)
                    (srcArg1, blue) -> (destArg1, light blue)
                    (srcArg1, red) -> (destArg1, dark red)
                    (srcArg2, null) -> (destArg2, null)
                    (srcArg2, nice) -> (destArg2, good)
                    (srcArg3, null) -> (destArg3, null)
                    (srcArg3, icy) -> (destArg3, snowy)
                }""", argNameValueMapping.toString());

    }

    @Test
    void testExplicitNull() {

        Map<String, String> valueMap1 = new HashMap<>();
        valueMap1.put("blue", "light blue");
        valueMap1.put("red", "dark red");

        Map<String, String> valueMap2 = new HashMap<>();
        valueMap2.put("nice", "good");

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMappings("srcArg1", "destArg1", valueMap1)
            .withMappings("srcArg2", "destArg2", valueMap2)
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .withMapping("srcArg3", null, "destArg7", null)
            .get();
        // @formatter:on

        assertEquals(7, argNameValueMapping.mappings().size());

        assertEquals(new QualifiedArgValue("destArg1", "light blue"), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg1", "blue")));
        assertEquals(new QualifiedArgValue("destArg2", "good"), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg2", "nice")));
        assertEquals(new QualifiedArgValue("destArg1", "dark red"), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg1", "red")));
        assertEquals(new QualifiedArgValue("destArg3", "snowy"), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg3", "icy")));
        assertEquals(new QualifiedArgValue("destArg1", null), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg1", null)));
        assertEquals(new QualifiedArgValue("destArg2", null), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg2", null)));
        assertEquals(new QualifiedArgValue("destArg7", null), argNameValueMapping.mappings().get(new QualifiedArgValue("srcArg3", null)));

        assertEquals("""
                ArgNameValueMapping {
                    (srcArg1, null) -> (destArg1, null)
                    (srcArg1, blue) -> (destArg1, light blue)
                    (srcArg1, red) -> (destArg1, dark red)
                    (srcArg2, null) -> (destArg2, null)
                    (srcArg2, nice) -> (destArg2, good)
                    (srcArg3, null) -> (destArg7, null)
                    (srcArg3, icy) -> (destArg3, snowy)
                }""", argNameValueMapping.toString());

    }

    @Test
    void testReverse() {

        Map<String, String> valueMap1 = new HashMap<>();
        valueMap1.put("blue", "light blue");
        valueMap1.put("red", "dark red");

        Map<String, String> valueMap2 = new HashMap<>();
        valueMap2.put("nice", "good");

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMappings("srcArg1", "destArg1", valueMap1)
            .withMappings("srcArg2", "destArg2", valueMap2)
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .get();
        // @formatter:on

        assertEquals(7, argNameValueMapping.mappings().size());

        assertEquals("""
                ArgNameValueMapping {
                    (destArg1, null) -> (srcArg1, null)
                    (destArg1, dark red) -> (srcArg1, red)
                    (destArg1, light blue) -> (srcArg1, blue)
                    (destArg2, null) -> (srcArg2, null)
                    (destArg2, good) -> (srcArg2, nice)
                    (destArg3, null) -> (srcArg3, null)
                    (destArg3, snowy) -> (srcArg3, icy)
                }""", argNameValueMapping.reverse().toString());

        assertEquals("""
                ArgNameValueMapping {
                    (srcArg1, null) -> (destArg1, null)
                    (srcArg1, blue) -> (destArg1, light blue)
                    (srcArg1, red) -> (destArg1, dark red)
                    (srcArg2, null) -> (destArg2, null)
                    (srcArg2, nice) -> (destArg2, good)
                    (srcArg3, null) -> (destArg3, null)
                    (srcArg3, icy) -> (destArg3, snowy)
                }""", argNameValueMapping.reverse().reverse().toString());

    }

    @Test
    void testNoReverse() {

        Map<String, String> valueMap1 = new HashMap<>();
        valueMap1.put("blue", "light blue");
        valueMap1.put("red", "dark red");

        Map<String, String> valueMap2 = new HashMap<>();
        valueMap2.put("nice", "good");

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMappings("srcArg1", "destArg1", valueMap1)
            .withMappings("srcArg2", "destArg2", valueMap2)
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .withMapping("srcArg3", "slippery", "destArg3", "snowy")
            .get();
        // @formatter:on

        assertEquals("""
                ArgNameValueMapping {
                    (srcArg1, null) -> (destArg1, null)
                    (srcArg1, blue) -> (destArg1, light blue)
                    (srcArg1, red) -> (destArg1, dark red)
                    (srcArg2, null) -> (destArg2, null)
                    (srcArg2, nice) -> (destArg2, good)
                    (srcArg3, null) -> (destArg3, null)
                    (srcArg3, icy) -> (destArg3, snowy)
                    (srcArg3, slippery) -> (destArg3, snowy)
                }""", argNameValueMapping.toString());

        assertThrows(AmbiguousMappingException.class, argNameValueMapping::reverse);

    }

    @Test
    void testNoReverseReverse() {

        Map<String, String> valueMap1 = new HashMap<>();
        valueMap1.put("blue", "light blue");
        valueMap1.put("red", "dark red");

        Map<String, String> valueMap2 = new HashMap<>();
        valueMap2.put("nice", "good");

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMappings("srcArg1", "destArg1", valueMap1)
            .withMappings("srcArg2", "destArg2", valueMap2)
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .withMapping("srcArg3", null, "destArg7", null)
            .get();
        // @formatter:on

        assertEquals(7, argNameValueMapping.mappings().size());

        ArgNameValueMapping reversed = argNameValueMapping.reverse();

        assertEquals("""
                ArgNameValueMapping {
                    (destArg1, null) -> (srcArg1, null)
                    (destArg1, dark red) -> (srcArg1, red)
                    (destArg1, light blue) -> (srcArg1, blue)
                    (destArg2, null) -> (srcArg2, null)
                    (destArg2, good) -> (srcArg2, nice)
                    (destArg3, null) -> (srcArg3, null)
                    (destArg3, snowy) -> (srcArg3, icy)
                    (destArg7, null) -> (srcArg3, null)
                }""", reversed.toString());

        // The implicitly added null-value-mappings (IS UNKNOWN)
        // causes the anomaly that we cannot reverse the reverse

        assertThrows(AmbiguousMappingException.class, reversed::reverse);

    }

    @Test
    void testSpecialCase() {

        Map<String, String> valueMap1 = new HashMap<>();
        valueMap1.put("blue", "light blue");
        valueMap1.put(null, "dark red");

        Map<String, String> valueMap2 = new HashMap<>();
        valueMap2.put("nice", null);

        Builder builder = ArgNameValueMapping.create();

        assertThrows(IllegalArgumentException.class, () -> builder.withMappings("srcArg1", "destArg1", valueMap1));
        assertThrows(IllegalArgumentException.class, () -> builder.withMappings("srcArg2", "destArg2", valueMap2));
        assertThrows(IllegalArgumentException.class, () -> builder.withMapping(null, "icy", "destArg3", "snowy"));
        assertThrows(IllegalArgumentException.class, () -> builder.withMapping("srcArg3", "icy", null, "snowy"));

    }

}
