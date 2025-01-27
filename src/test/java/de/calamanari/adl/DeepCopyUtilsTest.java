//@formatter:off
/*
 * DeepCopyUtilsTest
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

package de.calamanari.adl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class DeepCopyUtilsTest {

    @Test
    void testBasics() {

        AudlangMessage msg1 = AudlangMessage.msg(CommonErrors.ERR_1000_PARSE_FAILED);

        AudlangMessage msg2 = AudlangMessage.msg(CommonErrors.ERR_1000_PARSE_FAILED);

        assertNotSame(msg1, msg2);

        assertEquals(msg1, msg2);

        assertEquals(msg2, DeepCopyUtils.deepCopy(msg1));

        assertNull(DeepCopyUtils.deepCopy(null));

        Runnable notSerializable = new Runnable() {

            @Override
            public void run() {
                // nothing to do
            }
        };

        class Something implements Serializable {
            private static final long serialVersionUID = 1L;
            Object innerValue = notSerializable;
        }

        Something someThingThatCannotBeSerialized = new Something();
        assertEquals(notSerializable, someThingThatCannotBeSerialized.innerValue);
        assertThrows(DeepCopyException.class, () -> DeepCopyUtils.deepCopy(someThingThatCannotBeSerialized));

    }

}
