//@formatter:off
/*
 * AdlTypeDecoratorTest
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class AdlTypeDecoratorTest {

    static final Logger LOGGER = LoggerFactory.getLogger(AdlTypeDecoratorTest.class);

    @Test
    void testBasics() {

        AdlTypeDecorator decorator = (AdlTypeDecorator) DefaultAdlType.STRING.withFormatter(DefaultArgValueFormatter.STRING_IN_DOUBLE_QUOTES);

        assertEquals(DefaultArgValueFormatter.STRING_IN_DOUBLE_QUOTES, decorator.getFormatter());
        assertEquals(PassThroughTypeCaster.getInstance(), decorator.getNativeTypeCaster());

        assertEquals(DefaultAdlType.STRING, decorator.getBaseType());

        assertTrue(decorator.supportsContains());
        assertTrue(decorator.supportsLessThanGreaterThan());

        NativeTypeCaster ntc = new NativeTypeCaster() {

            private static final long serialVersionUID = -4716335076800837380L;

            @Override
            public String formatNativeTypeCast(String argName, String nativeFieldName, AdlType argType, AdlType requestedArgType) {
                return "bla";
            }

        };

        decorator = (AdlTypeDecorator) decorator.withNativeTypeCaster(ntc);

        assertEquals(DefaultArgValueFormatter.STRING_IN_DOUBLE_QUOTES, decorator.getFormatter());
        assertEquals(ntc, decorator.getNativeTypeCaster());

        assertEquals(DefaultAdlType.STRING, decorator.getBaseType());

        assertTrue(decorator.supportsContains());
        assertTrue(decorator.supportsLessThanGreaterThan());

    }
}
