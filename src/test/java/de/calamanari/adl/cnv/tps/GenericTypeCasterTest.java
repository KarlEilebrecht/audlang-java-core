//@formatter:off
/*
 * GenericTypeCasterTest
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.DeepCopyUtils;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class GenericTypeCasterTest {

    static final Logger LOGGER = LoggerFactory.getLogger(GenericTypeCasterTest.class);

    @Test
    void testBasics() {

        // @formatter:off
        GenericTypeCaster caster = GenericTypeCaster
                                        .withNativeCast(DefaultAdlType.DATE, s -> "d(" + s + ")")
                                        .withNativeCast(DefaultAdlType.INTEGER, s -> "i(" + s + ")")
                                        .get();
        // @formatter:on

        assertEquals("d(dateField)", caster.formatNativeTypeCast("argName", "dateField", DefaultAdlType.STRING, DefaultAdlType.DATE));
        assertEquals("i(intField)", caster.formatNativeTypeCast("argName", "intField", DefaultAdlType.STRING, DefaultAdlType.INTEGER));
        assertEquals("other", caster.formatNativeTypeCast("argName", "other", DefaultAdlType.STRING, DefaultAdlType.DECIMAL));

        GenericTypeCaster caster2 = DeepCopyUtils.deepCopy(caster);

        assertEquals(caster.toString(), caster2.toString());

        assertEquals("d(dateField)", caster2.formatNativeTypeCast("argName", "dateField", DefaultAdlType.STRING, DefaultAdlType.DATE));
        assertEquals("i(intField)", caster2.formatNativeTypeCast("argName", "intField", DefaultAdlType.STRING, DefaultAdlType.INTEGER));
        assertEquals("other", caster2.formatNativeTypeCast("argName", "other", DefaultAdlType.STRING, DefaultAdlType.DECIMAL));

        Map<String, SerializableUnaryStringOperator> map = new HashMap<>();

        map.put("x", s -> "d(" + s + ")");

        assertNotNull(new GenericTypeCaster(map));

        map.put(null, s -> "d(" + s + ")");
        assertThrows(ConfigException.class, () -> new GenericTypeCaster(map));

        map.clear();
        map.put("x", s -> "d(" + s + ")");
        map.put("y", null);
        assertThrows(ConfigException.class, () -> new GenericTypeCaster(map));

    }

}
