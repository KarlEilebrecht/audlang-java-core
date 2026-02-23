//@formatter:off
/*
 * DefaultArgNameValueMapperTest
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class DefaultArgNameValueMapperTest {

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

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping);

        assertTrue(mapper.isArgumentStructurePreserving());
        assertTrue(mapper.isBijective());

        assertEquals(new QualifiedArgValue("destArg1", "light blue"), mapper.mapArgValue("srcArg1", "blue"));
        assertEquals(new QualifiedArgValue("destArg2", "good"), mapper.mapArgValue("srcArg2", "nice"));
        assertEquals(new QualifiedArgValue("destArg1", "dark red"), mapper.mapArgValue("srcArg1", "red"));
        assertEquals(new QualifiedArgValue("destArg3", "snowy"), mapper.mapArgValue("srcArg3", "icy"));
        assertEquals(new QualifiedArgValue("destArg1", null), mapper.mapArgValue("srcArg1", null));
        assertEquals(new QualifiedArgValue("destArg2", null), mapper.mapArgValue("srcArg2", null));
        assertEquals(new QualifiedArgValue("destArg3", null), mapper.mapArgValue("srcArg3", null));

        assertThrows(MappingNotFoundException.class, () -> mapper.mapArgValue("bla", null));

    }

    @Test
    void testFallback() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("srcArg1", "blue", "destArg1", "light blue")
            .withMapping("srcArg2", "nice", "destArg2", "good")
            .withMapping("srcArg1", "red", "destArg1", "dark red")
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapperWithFallback = new DefaultArgNameValueMapper(argNameValueMapping, DummyArgNameValueMapper.getInstance());

        assertTrue(mapperWithFallback.isArgumentStructurePreserving());
        assertTrue(mapperWithFallback.isBijective());

        assertEquals(new QualifiedArgValue("destArg1", "light blue"), mapperWithFallback.mapArgValue("srcArg1", "blue"));
        assertEquals(new QualifiedArgValue("destArg2", "good"), mapperWithFallback.mapArgValue("srcArg2", "nice"));
        assertEquals(new QualifiedArgValue("destArg1", "dark red"), mapperWithFallback.mapArgValue("srcArg1", "red"));
        assertEquals(new QualifiedArgValue("destArg3", "snowy"), mapperWithFallback.mapArgValue("srcArg3", "icy"));
        assertEquals(new QualifiedArgValue("destArg1", null), mapperWithFallback.mapArgValue("srcArg1", null));
        assertEquals(new QualifiedArgValue("destArg2", null), mapperWithFallback.mapArgValue("srcArg2", null));
        assertEquals(new QualifiedArgValue("destArg3", null), mapperWithFallback.mapArgValue("srcArg3", null));
        assertEquals(new QualifiedArgValue("bla", null), mapperWithFallback.mapArgValue("bla", null));

    }

    @Test
    void testNotBijective() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("srcArg1", "blue", "destArg1", "light blue")
            .withMapping("srcArg2", "nice", "destArg2", "good")
            .withMapping("srcArg1", "red", "destArg1", "dark red")
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .withMapping("srcArg3", "slippery", "destArg3", "snowy")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping);
        assertTrue(mapper.isArgumentStructurePreserving());
        assertFalse(mapper.isBijective());

        // @formatter:off
        ArgNameValueMapping bijectiveMapping = ArgNameValueMapping.create()
            .withMapping("srcArg1", "blue", "destArg1", "light blue")
            .withMapping("srcArg2", "nice", "destArg2", "good")
            .withMapping("srcArg1", "red", "destArg1", "dark red")
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper bijectiveMapper = new DefaultArgNameValueMapper(bijectiveMapping);
        assertTrue(bijectiveMapper.isBijective());

        mapper = new DefaultArgNameValueMapper(bijectiveMapping, mapper);
        assertTrue(mapper.isArgumentStructurePreserving());
        assertFalse(mapper.isBijective());

        mapper = new DefaultArgNameValueMapper(argNameValueMapping, bijectiveMapper);
        assertTrue(mapper.isArgumentStructurePreserving());
        assertFalse(mapper.isBijective());

    }

    @Test
    void testNotStructurePreserving() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("srcArg1", "blue", "destArg1", "light blue")
            .withMapping("srcArg2", "nice", "destArg2", "good")
            .withMapping("srcArg1", "red", "destArg4", "dark red")
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping);
        assertFalse(mapper.isArgumentStructurePreserving());
        assertTrue(mapper.isBijective());

        // @formatter:off
        ArgNameValueMapping structurePreservingMapping = ArgNameValueMapping.create()
            .withMapping("srcArg1", "blue", "destArg1", "light blue")
            .withMapping("srcArg2", "nice", "destArg2", "good")
            .withMapping("srcArg1", "red", "destArg1", "dark red")
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper structurePreservingMapper = new DefaultArgNameValueMapper(structurePreservingMapping);
        assertTrue(structurePreservingMapper.isArgumentStructurePreserving());

        mapper = new DefaultArgNameValueMapper(structurePreservingMapping, mapper);
        assertFalse(mapper.isArgumentStructurePreserving());
        assertTrue(mapper.isBijective());

        mapper = new DefaultArgNameValueMapper(argNameValueMapping, structurePreservingMapper);
        assertFalse(mapper.isArgumentStructurePreserving());
        assertTrue(mapper.isBijective());

    }

    @Test
    void testReverse() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("srcArg1", "blue", "destArg1", "light blue")
            .withMapping("srcArg2", "nice", "destArg2", "good")
            .withMapping("srcArg1", "red", "destArg4", "dark red")
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapper = new DefaultArgNameValueMapper(argNameValueMapping);

        ArgNameValueMapper reverseMapper = mapper.reverse();

        assertSame(mapper, reverseMapper.reverse());

        assertEquals(new QualifiedArgValue("srcArg1", "blue"), map(reverseMapper, mapper.mapArgValue("srcArg1", "blue")));
        assertEquals(new QualifiedArgValue("srcArg2", "nice"), map(reverseMapper, mapper.mapArgValue("srcArg2", "nice")));
        assertEquals(new QualifiedArgValue("srcArg1", "red"), map(reverseMapper, mapper.mapArgValue("srcArg1", "red")));
        assertEquals(new QualifiedArgValue("srcArg3", "icy"), map(reverseMapper, mapper.mapArgValue("srcArg3", "icy")));
        assertEquals(new QualifiedArgValue("srcArg1", null), map(reverseMapper, mapper.mapArgValue("srcArg1", null)));
        assertEquals(new QualifiedArgValue("srcArg2", null), map(reverseMapper, mapper.mapArgValue("srcArg2", null)));
        assertEquals(new QualifiedArgValue("srcArg3", null), map(reverseMapper, mapper.mapArgValue("srcArg3", null)));

    }

    @Test
    void testCopy() {

        // @formatter:off
        ArgNameValueMapping argNameValueMapping = ArgNameValueMapping.create()
            .withMapping("srcArg1", "blue", "destArg1", "light blue")
            .withMapping("srcArg2", "nice", "destArg2", "good")
            .withMapping("srcArg1", "red", "destArg1", "dark red")
            .withMapping("srcArg3", "icy", "destArg3", "snowy")
            .get();
        // @formatter:on

        DefaultArgNameValueMapper mapperWithFallback = new DefaultArgNameValueMapper(argNameValueMapping, DummyArgNameValueMapper.getInstance());

        mapperWithFallback = DeepCopyUtils.deepCopy(mapperWithFallback);

        assertTrue(mapperWithFallback.isArgumentStructurePreserving());
        assertTrue(mapperWithFallback.isBijective());

        assertEquals(new QualifiedArgValue("destArg1", "light blue"), mapperWithFallback.mapArgValue("srcArg1", "blue"));
        assertEquals(new QualifiedArgValue("destArg2", "good"), mapperWithFallback.mapArgValue("srcArg2", "nice"));
        assertEquals(new QualifiedArgValue("destArg1", "dark red"), mapperWithFallback.mapArgValue("srcArg1", "red"));
        assertEquals(new QualifiedArgValue("destArg3", "snowy"), mapperWithFallback.mapArgValue("srcArg3", "icy"));
        assertEquals(new QualifiedArgValue("destArg1", null), mapperWithFallback.mapArgValue("srcArg1", null));
        assertEquals(new QualifiedArgValue("destArg2", null), mapperWithFallback.mapArgValue("srcArg2", null));
        assertEquals(new QualifiedArgValue("destArg3", null), mapperWithFallback.mapArgValue("srcArg3", null));
        assertEquals(new QualifiedArgValue("bla", null), mapperWithFallback.mapArgValue("bla", null));

    }

    private static QualifiedArgValue map(ArgNameValueMapper mapper, QualifiedArgValue qav) {
        return mapper.mapArgValue(qav.argName(), qav.argValue());
    }

}
