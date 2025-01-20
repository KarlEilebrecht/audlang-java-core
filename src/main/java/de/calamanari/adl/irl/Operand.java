//@formatter:off
/*
 * Operand
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

package de.calamanari.adl.irl;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import de.calamanari.adl.AudlangFormattable;
import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.util.AdlTextUtils;

/**
 * An {@link Operand} can either be a plain string value or an argument reference for comparison to an argument.
 * <p>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#122-argument-values">§1.2.2</a>,
 * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#123-argument-reference">§1.2.3</a> Audlang
 * Spec
 * 
 * @param value either a plain value of the plain name of another argument
 * @param isReference true to indicate that the given value is the name of another argument
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public record Operand(String value, @JsonInclude(JsonInclude.Include.NON_DEFAULT) boolean isReference) implements AudlangFormattable, Serializable {

    /**
     * @param value either a plain value of the plain name of another argument
     * @param isReference true to indicate that the given value is the name of another argument
     */
    public Operand {
        if (value == null) {
            throw new AudlangValidationException("value must not be null");
        }
    }

    public static Operand of(String value, boolean isReference) {
        return new Operand(value, isReference);
    }

    public void appendInternal(StringBuilder sb) {
        String outputValue = AdlTextUtils.addDoubleQuotesIfRequired(AdlTextUtils.escapeSpecialCharacters(value));
        if (isReference()) {
            sb.append('@');
        }
        sb.append(outputValue);
    }

    @Override
    public void appendSingleLine(StringBuilder sb, FormatStyle style, int level) {
        appendInternal(sb);
    }

    @Override
    public void appendMultiLine(StringBuilder sb, FormatStyle style, int level) {
        appendInternal(sb);
    }

    @Override
    public boolean shouldUseMultiLineFormatting(FormatStyle style) {
        return false;
    }

}
