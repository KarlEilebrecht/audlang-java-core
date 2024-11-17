//@formatter:off
/*
 * DummyArgNameValueMapperTest
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.DeepCopyUtils;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class DummyArgNameValueMapperTest {

    @Test
    void testBasics() {

        DummyArgNameValueMapper dummy = DummyArgNameValueMapper.getInstance();

        DummyArgNameValueMapper dummy2 = DummyArgNameValueMapper.getInstance();

        assertSame(dummy, dummy2);
        assertSame(dummy, dummy.reverse());

        assertTrue(dummy.isArgumentStructurePreserving());
        assertTrue(dummy.isBijective());

        assertEquals(new QualifiedArgValue("argName", "argValue"), dummy.mapArgValue("argName", "argValue"));

        assertSame(dummy, DeepCopyUtils.deepCopy(dummy));

    }

}
