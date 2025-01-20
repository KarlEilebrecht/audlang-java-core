//@formatter:off
/*
 * SpecialSetExpression
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

import static de.calamanari.adl.FormatUtils.appendSpecialSetExpression;

import java.util.Collections;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.FormatUtils.FormatInfo;
import de.calamanari.adl.SpecialSetType;

/**
 * {@link SpecialSetExpression} stands for the Audlang expressions <code>&lt;ALL&gt;</code> (resp. no restriction, always true, {@link SpecialSetType#ALL}) and
 * <code>&lt;NONE&gt;</code> (never true, {@link SpecialSetType#NONE})
 * 
 * <p>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#39-all-and-none">§3.9</a> Audlang
 * Spec
 * 
 * @param setType distinguishes between ALL and NONE
 * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public record SpecialSetExpression(SpecialSetType setType, String inline) implements CoreExpression {

    private static final SpecialSetExpression ALL = new SpecialSetExpression(SpecialSetType.ALL, null);

    private static final SpecialSetExpression NONE = new SpecialSetExpression(SpecialSetType.NONE, null);

    /**
     * @param setType distinguishes between ALL and NONE
     * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
     */
    public SpecialSetExpression(SpecialSetType setType, @SuppressWarnings("java:S1172") String inline) {
        if (setType == null) {
            throw new AudlangValidationException("setType must not be null");
        }
        this.setType = setType;
        this.inline = format(FormatStyle.INLINE);
    }

    /**
     * @return shorthand for creating an ALL-expression (reuse)
     */
    public static SpecialSetExpression all() {
        return ALL;
    }

    /**
     * @return shorthand for creating a NONE-expression (reuse)
     */
    public static SpecialSetExpression none() {
        return NONE;
    }

    @Override
    public void appendSingleLine(StringBuilder sb, FormatStyle style, int level) {
        appendSpecialSetExpression(sb, setType.name(), Collections.emptyList(), new FormatInfo(style, level, true));
    }

    @Override
    public void appendMultiLine(StringBuilder sb, FormatStyle style, int level) {
        appendSpecialSetExpression(sb, setType.name(), Collections.emptyList(), new FormatInfo(style, level));
    }

    @Override
    public String toString() {
        return inline;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SpecialSetExpression sps && sps.inline.equals(this.inline);
    }

    @Override
    public int hashCode() {
        return inline.hashCode();
    }

    @Override
    public int compareTo(CoreExpression other) {
        if (other instanceof SpecialSetExpression sps) {
            return setType.compareTo(sps.setType);
        }
        // special set expressions come before all other (usually indicating a mistake)
        return -1;
    }

    @Override
    public void accept(CoreExpressionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public CoreExpression negate(boolean strict) {
        return setType == SpecialSetType.ALL ? NONE : ALL;
    }

}
