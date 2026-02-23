//@formatter:off
/*
 * AudlangFieldTest
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class AudlangFieldTest {

    @Test
    void testBasics() {

        AudlangField field = new AudlangField("argName", Collections.emptyList(), Collections.emptyList());

        assertTrue(field.values().isEmpty());
        assertTrue(field.refArgNames().isEmpty());

        field = new AudlangField("argName", null, null);

        assertTrue(field.values().isEmpty());
        assertTrue(field.refArgNames().isEmpty());

        field = new AudlangField("argName", Arrays.asList("v1", "v2", "v7", "v7", "v3"), Arrays.asList("r1", "r2", "r7", "r7", "r3"));

        assertEquals(Arrays.asList("v1", "v2", "v3", "v7"), field.values());
        assertEquals(Arrays.asList("r1", "r2", "r3", "r7"), field.refArgNames());

    }

    @Test
    void testSpecialCases() {

        List<String> emptyList = Collections.emptyList();
        List<String> listWithNull = Arrays.asList("a", null);

        assertThrows(AudlangValidationException.class, () -> new AudlangField(null, emptyList, emptyList));
        assertThrows(AudlangValidationException.class, () -> new AudlangField("a", listWithNull, emptyList));
        assertThrows(AudlangValidationException.class, () -> new AudlangField("a", emptyList, listWithNull));

        AudlangField.Builder builder = AudlangField.forField(null);
        assertThrows(AudlangValidationException.class, builder::get);

        AudlangField.Builder builder2 = AudlangField.forField("argName");
        builder2.addValue(null);
        assertThrows(AudlangValidationException.class, builder2::get);

        AudlangField.Builder builder3 = AudlangField.forField("argName");
        builder3.addRefArgName(null);
        assertThrows(AudlangValidationException.class, builder3::get);

    }

}
