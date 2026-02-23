//@formatter:off
/*
 * AudlangMessageTest
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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class AudlangMessageTest {

    static final Logger LOGGER = LoggerFactory.getLogger(AudlangMessageTest.class);

    @Test
    void testBasics() {

        AudlangMessage info = AudlangMessage.msg(CommonErrors.ERR_1000_PARSE_FAILED);

        assertEquals("ERR_1000", info.code());

        assertNull(info.relatedArgNameLeft());
        assertNull(info.relatedArgNameRight());
        assertNull(info.relatedValue());

        info = AudlangMessage.argMsg(CommonErrors.ERR_2004_VALUE_FORMAT, "a");

        assertEquals("ERR_2004", info.code());

        assertEquals("a", info.relatedArgNameLeft());
        assertNull(info.relatedArgNameRight());
        assertNull(info.relatedValue());

        info = AudlangMessage.argValueMsg(CommonErrors.ERR_2004_VALUE_FORMAT, "a", "1");
        assertEquals("ERR_2004", info.code());

        assertEquals("a", info.relatedArgNameLeft());
        assertNull(info.relatedArgNameRight());
        assertEquals("1", info.relatedValue());

        info = AudlangMessage.argRefMsg(CommonErrors.ERR_3001_TYPE_MISMATCH, "a", "b");
        assertEquals("ERR_3001", info.code());

        assertEquals("a", info.relatedArgNameLeft());
        assertEquals("b", info.relatedArgNameRight());
        assertNull(info.relatedValue());

    }

    @Test
    void testInConversionException() {

        AudlangMessage info = throwAndCatch(AudlangMessage.msg(CommonErrors.ERR_1000_PARSE_FAILED)).getUserMessage();

        assertEquals("ERR_1000", info.code());

        assertNull(info.relatedArgNameLeft());
        assertNull(info.relatedArgNameRight());
        assertNull(info.relatedValue());

        info = throwAndCatch(AudlangMessage.argMsg(CommonErrors.ERR_2004_VALUE_FORMAT, "a")).getUserMessage();

        assertEquals("ERR_2004", info.code());

        assertEquals("a", info.relatedArgNameLeft());
        assertNull(info.relatedArgNameRight());
        assertNull(info.relatedValue());

        info = new ConversionException("Test no info").getUserMessage();
        assertEquals("ERR_4003", info.code());

    }

    private static ConversionException throwAndCatch(AudlangMessage info) {

        ConversionException res = null;

        try {
            if (true) {
                throw new ConversionException("Test run", info);
            }
        }
        catch (ConversionException ex) {
            res = ex;
        }

        return res;

    }

}
