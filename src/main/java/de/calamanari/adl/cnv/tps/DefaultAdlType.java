//@formatter:off
/*
 * AdlDefaultType
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

/**
 * This enum provides the core types according to the Audlang conventions
 * (<a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#2-type-conventions">§2 Audlang Spec</a>).
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public enum DefaultAdlType implements AdlType {

    /**
     * Textual values ({@link DefaultArgValueFormatter#STRING_IN_SINGLE_QUOTES}), all control-characters below 32 that cannot be escaped will be dropped.
     * <p/>
     * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#11-strings">§1.1 Audlang
     * Spec</a>
     */
    STRING(DefaultArgValueFormatter.STRING_IN_SINGLE_QUOTES),

    /**
     * Integer values ({@link DefaultArgValueFormatter#INTEGER})
     * <p/>
     * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#211-integer-values">§2.1.1
     * Audlang Spec</a>
     * 
     */
    INTEGER(DefaultArgValueFormatter.INTEGER),

    /**
     * Decimal values ({@link DefaultArgValueFormatter#DECIMAL})
     * <p/>
     * See also <a href=
     * "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#212-decimal-values-floating-point">§2.1.2
     * Audlang Spec</a>
     * 
     */
    DECIMAL(DefaultArgValueFormatter.DECIMAL),

    /**
     * Boolean values ({@link DefaultArgValueFormatter#BOOL}
     * <p/>
     * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#22-logical-values">§2.2 Audlang
     * Spec</a>
     */
    BOOL(DefaultArgValueFormatter.BOOL),

    /**
     * Date values ({@link DefaultArgValueFormatter#DATE}
     */
    DATE(DefaultArgValueFormatter.DATE);

    /**
     * default formatter associated with this type
     */
    private final ArgValueFormatter formatter;

    private DefaultAdlType(ArgValueFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public ArgValueFormatter getFormatter() {
        return formatter;
    }

    @Override
    public boolean supportsContains() {
        return this == STRING;
    }

    @Override
    public boolean supportsLessThanGreaterThan() {
        return this != BOOL;
    }

}
