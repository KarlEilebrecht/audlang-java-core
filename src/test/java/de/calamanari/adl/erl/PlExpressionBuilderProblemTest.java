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

import de.calamanari.adl.AudlangMessage;
import de.calamanari.adl.AudlangMessageSeverity;
import de.calamanari.adl.AudlangResult;
import de.calamanari.adl.CommonErrors;
import de.calamanari.adl.ConversionException;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class PlExpressionBuilderProblemTest {

    static final Logger LOGGER = LoggerFactory.getLogger(PlExpressionBuilderProblemTest.class);

    @Test
    void testBasicMistakes() {

        AudlangParseResult res = parse(null);

        assertTrue(res.isError());

        assertNotNull(res.getUserMessages());

        assertTrue(res.getUserMessages().size() > 0);

        assertEquals(CommonErrors.ERR_1000_PARSE_FAILED.code(), res.getUserMessages().get(0).code());

        assertEquals(AudlangMessageSeverity.ERROR, res.getUserMessages().get(0).severity());

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

    @Test
    void testAudlangResult() {
        AudlangResult res = new AudlangResult();

        assertEquals("AudlangResult [source=null, error=false, errorMessage=null, userMessages=[]]", res.toString());

        res.getUserMessages().add(AudlangMessage.msg(CommonErrors.ERR_1000_PARSE_FAILED));

        assertEquals("AudlangResult [source=null, error=false, errorMessage=null, userMessages=[AudlangMessage[code=ERR_1000, severity=ERROR, "
                + "userMessage=ERR_1000: The expression could not be parsed (syntax error)., relatedArgNameLeft=null, relatedArgNameRight=null, relatedValue=null]]]",
                res.toString());

        res.setUserMessages(null);

        assertNotNull(res.getUserMessages());

        assertEquals("AudlangResult [source=null, error=false, errorMessage=null, userMessages=[]]", res.toString());

        ConversionException ex = new ConversionException(AudlangMessage.msg(CommonErrors.ERR_1000_PARSE_FAILED));

        assertEquals(CommonErrors.ERR_1000_PARSE_FAILED.code(), ex.getUserMessage().code());

        ex = new ConversionException("Error!", new RuntimeException());

        assertEquals(CommonErrors.ERR_4003_GENERAL_ERROR.code(), ex.getUserMessage().code());

    }

    private static AudlangParseResult parse(String source) {
        return PlExpressionBuilder.stringToExpression(source);
    }
}
