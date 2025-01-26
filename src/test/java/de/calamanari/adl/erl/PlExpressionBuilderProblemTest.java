//@formatter:off
/*
 * PlExpressionBuilderProblemTest
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

package de.calamanari.adl.erl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.CommonErrors;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class PlExpressionBuilderProblemTest {

    static final Logger LOGGER = LoggerFactory.getLogger(PlExpressionBuilderProblemTest.class);

    @Test
    void testBasicMistakes() {

        AudlangParseResult res = parse(null);

        assertTrue(res.isError());

        assertNotNull(res.getErrorInfo());

        assertEquals(CommonErrors.ERR_1000_PARSE_FAILED.code(), res.getErrorInfo().code());

        res = parse("");

        assertTrue(res.isError());

        res = parse("   ");

        assertTrue(res.isError());

        res = parse("/*");

        assertTrue(res.isError());

        res = parse("/* comment only */");

        assertTrue(res.isError());

        res = parse("CURB (a=b or c=d) > B");

        LOGGER.info("{}", res);

    }

    private static AudlangParseResult parse(String source) {
        return PlExpressionBuilder.stringToExpression(source);
    }
}
