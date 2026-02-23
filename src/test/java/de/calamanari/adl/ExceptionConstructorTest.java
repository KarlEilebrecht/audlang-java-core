//@formatter:off
/*
 * ExceptionConstructorTest
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.cnv.AmbiguousMappingException;
import de.calamanari.adl.cnv.IncompatibleMappingException;
import de.calamanari.adl.cnv.MappingNotFoundException;
import de.calamanari.adl.cnv.tps.AdlFormattingException;
import de.calamanari.adl.cnv.tps.ContainsNotSupportedException;
import de.calamanari.adl.cnv.tps.LessThanGreaterThanNotSupportedException;
import de.calamanari.adl.cnv.tps.LookupException;
import de.calamanari.adl.cnv.tps.TypeMismatchException;
import de.calamanari.adl.erl.CurbComplexityException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class ExceptionConstructorTest {

    static final Logger LOGGER = LoggerFactory.getLogger(ExceptionConstructorTest.class);

    @Test
    void testExceptionConstructors() {

        // To avoid tons of boiler-plate code, we simply call all constructors

        // @formatter:off
        List<Class<? extends AdlException>> toBeTested = Arrays.asList(
                AudlangValidationException.class, 
                ConversionException.class, 
                TimeOutException.class,
                AmbiguousMappingException.class, 
                IncompatibleMappingException.class, 
                MappingNotFoundException.class, 
                AdlFormattingException.class,
                ContainsNotSupportedException.class, 
                LessThanGreaterThanNotSupportedException.class, 
                LookupException.class, 
                TypeMismatchException.class,
                CurbComplexityException.class);
        
        // @formatter:on

        toBeTested.stream().forEach(ExceptionConstructorTest::assertCanConstruct);
    }

    private static void assertCanConstruct(Class<? extends AdlException> exClass) {

        for (Constructor<?> con : exClass.getConstructors()) {

            Parameter[] params = con.getParameters();

            Object[] values = new Object[params.length];

            for (int i = 0; i < params.length; i++) {

                Parameter param = params[i];

                switch (param.getType().getSimpleName()) {
                case "String":
                    values[i] = "String#" + i;
                    break;
                case "Throwable":
                    values[i] = new RuntimeException("Ex#" + i);
                    break;
                case "AudlangMessage":
                    values[i] = AudlangMessage.msg(CommonErrors.ERR_1000_PARSE_FAILED);
                    break;
                case "int", "Integer", "long", "Long":
                    values[i] = i;
                    break;
                case "float", "Float", "double", "Double":
                    values[i] = 0.5;
                    break;
                case "boolean", "Boolean":
                    values[i] = true;
                    break;
                default:
                }

            }

            Object ex = null;
            try {
                LOGGER.info("Calling constructor: {} with params={}", con, Arrays.toString(values));

                ex = con.newInstance(values);
            }
            catch (Exception exUnexpected) {
                throw new RuntimeException(exUnexpected);
            }

            assertNotNull(ex);
            assertTrue(exClass.isAssignableFrom(ex.getClass()));

        }

    }

}
