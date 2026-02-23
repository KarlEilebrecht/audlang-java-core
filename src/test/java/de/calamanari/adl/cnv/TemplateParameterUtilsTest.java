//@formatter:off
/*
 * TemplateParameterUtilsTest
 * Copyright 2025 Karl Eilebrecht
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class TemplateParameterUtilsTest {

    @Test
    void testBasics() {

        final Map<String, Object> lookup = new HashMap<>();

        lookup.put("argNull", null);
        lookup.put("empty", "");
        lookup.put("blank", "  ");
        lookup.put("name", "name");
        lookup.put("arg1", "value1");
        lookup.put("arg2", "value2");
        lookup.put("samePlaceholder", "${samePlaceholder}");
        lookup.put("otherPlaceholder", "${arg1}");
        lookup.put("twoPlaceholders", "${arg1}${arg2}");

        assertEquals("", TemplateParameterUtils.replaceVariables("", lookup::get));
        assertEquals("   ", TemplateParameterUtils.replaceVariables("   ", lookup::get));
        assertEquals(" ${argNull}  ", TemplateParameterUtils.replaceVariables(" ${argNull}  ", lookup::get));
        assertEquals(" ${empty}  ", TemplateParameterUtils.replaceVariables(" ${empty}  ", lookup::get));
        assertEquals(" ${blank}  ", TemplateParameterUtils.replaceVariables(" ${blank}  ", lookup::get));
        assertEquals(" name  ", TemplateParameterUtils.replaceVariables(" ${name}  ", lookup::get));
        assertEquals(" value1  ", TemplateParameterUtils.replaceVariables(" ${arg1}  ", lookup::get));
        assertEquals(" value1, value2  ", TemplateParameterUtils.replaceVariables(" ${arg1}, ${arg2}  ", lookup::get));
        assertEquals(" ${samePlaceholder}  ", TemplateParameterUtils.replaceVariables(" ${samePlaceholder}  ", lookup::get));
        assertEquals(" ${arg1}  ", TemplateParameterUtils.replaceVariables(" ${otherPlaceholder}  ", lookup::get));
        assertEquals(" ${arg1}${arg2}  ", TemplateParameterUtils.replaceVariables(" ${twoPlaceholders}  ", lookup::get));

        assertEquals("value1  ", TemplateParameterUtils.replaceVariables("${arg1}  ", lookup::get));
        assertEquals(" value1", TemplateParameterUtils.replaceVariables(" ${arg1}", lookup::get));
        assertEquals("value1", TemplateParameterUtils.replaceVariables("${arg1}", lookup::get));

        assertEquals(" ${arg1}  ", TemplateParameterUtils.replaceVariables(" ${arg1}  ", null));
        assertEquals(null, TemplateParameterUtils.replaceVariables(null, null));
        assertEquals(null, TemplateParameterUtils.replaceVariables(null, lookup::get));

        assertTrue(TemplateParameterUtils.containsAnyVariables("${var}"));
        assertTrue(TemplateParameterUtils.containsAnyVariables("foo/${var}/bla"));
        assertFalse(TemplateParameterUtils.containsAnyVariables(""));
        assertFalse(TemplateParameterUtils.containsAnyVariables(null));

        assertEquals(Arrays.asList("foo", "bar", "bla"), TemplateParameterUtils.extractVariableNames("${foo} lives in ${bar} but ${foo} does not like ${bla}"));
        assertEquals(Collections.emptyList(), TemplateParameterUtils.extractVariableNames("The quick brown fox jumped over the lazy dog"));
        assertEquals(Collections.emptyList(), TemplateParameterUtils.extractVariableNames(""));
        assertEquals(Collections.emptyList(), TemplateParameterUtils.extractVariableNames(null));

    }

}
