//@formatter:off
/*
 * AudlangErrorInfoTest
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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class AudlangErrorInfoTest {

    static final Logger LOGGER = LoggerFactory.getLogger(AudlangErrorInfoTest.class);

    @Test
    void testBasics() {

        AudlangErrorInfo info = AudlangErrorInfo.error(CommonErrors.ERR_1000_PARSE_FAILED);

        assertEquals("ERR_1000", info.code());

        assertNull(info.offendingArgNameLeft());
        assertNull(info.offendingArgNameRight());
        assertNull(info.offendingValue());

        info = AudlangErrorInfo.argError(CommonErrors.ERR_2004_VALUE_FORMAT, "a");

        assertEquals("ERR_2004", info.code());

        assertEquals("a", info.offendingArgNameLeft());
        assertNull(info.offendingArgNameRight());
        assertNull(info.offendingValue());

        info = AudlangErrorInfo.argValueError(CommonErrors.ERR_2004_VALUE_FORMAT, "a", "1");
        assertEquals("ERR_2004", info.code());

        assertEquals("a", info.offendingArgNameLeft());
        assertNull(info.offendingArgNameRight());
        assertEquals("1", info.offendingValue());

        info = AudlangErrorInfo.argRefError(CommonErrors.ERR_3001_TYPE_MISMATCH, "a", "b");
        assertEquals("ERR_3001", info.code());

        assertEquals("a", info.offendingArgNameLeft());
        assertEquals("b", info.offendingArgNameRight());
        assertNull(info.offendingValue());

    }

    @Test
    void testInConversionException() {

        AudlangErrorInfo info = throwAndCatch(AudlangErrorInfo.error(CommonErrors.ERR_1000_PARSE_FAILED)).getErrorInfo();

        assertEquals("ERR_1000", info.code());

        assertNull(info.offendingArgNameLeft());
        assertNull(info.offendingArgNameRight());
        assertNull(info.offendingValue());

        info = throwAndCatch(AudlangErrorInfo.argError(CommonErrors.ERR_2004_VALUE_FORMAT, "a")).getErrorInfo();

        assertEquals("ERR_2004", info.code());

        assertEquals("a", info.offendingArgNameLeft());
        assertNull(info.offendingArgNameRight());
        assertNull(info.offendingValue());

        info = new ConversionException("Test no info").getErrorInfo();
        assertEquals("ERR_4003", info.code());

    }

    private static ConversionException throwAndCatch(AudlangErrorInfo info) {

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
