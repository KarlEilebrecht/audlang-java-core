//@formatter:off
/*
 * CoreExpression
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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.calamanari.adl.AudlangExpression;
import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.FormatStyle;

/**
 * {@link CoreExpression}s are the standard Audlang expressions on the <b>internal representation layer</b>.
 * <p/>
 * This layer is meant for optimization and translation into target languages. It avoids syntactic sugar and thus has fewer language elements while still being
 * compatible to the Audlang language specification. A special characteristic of {@link CoreExpression}s is that there are <b>no higher level negations</b>, in
 * other words: all negations <i>trickle down</i> to the leaf expressions.
 * <p/>
 * <b>Contract:</b>
 * <ul>
 * <li>A Core expressions is a directed acyclic graph (DAG).</li>
 * <li>Core expressions are immutable.</li>
 * <li>Core expressions are standardized and partially normalized, so the equals() and compare() methods are aligned to the textual representation.</li>
 * <li>Any List values and siblings must be ordered lexicographically and all lists (values and siblings) must be free of any duplicates (effectively they are
 * sets).</li>
 * <li>The methods <code>equals()</code> and <code>hashcode()</code> <b>MUST</b> be implemented consistently to the instance's <b>toString()</b> method, which
 * <b>MUST</b> return a valid inline-expression equivalent to this instance and according to the Audlang language specification.
 * </ul>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#3-basic-expressions">§3</a>,
 * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#3-composite-expressions">§4</a> AudLang Spec
 * 
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@JsonDeserialize(using = CoreExpressionDeserializer.class)
public interface CoreExpression extends AudlangExpression<CoreExpression, CoreExpressionVisitor> {

    @Override
    default boolean shouldUseMultiLineFormatting(FormatStyle style) {
        return style.isMultiLine() && !this.childExpressions().isEmpty();
    }

    /**
     * Returns a the negated form of this expression according to
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#5-negation">Audlang Spec §5</a>
     * <p/>
     * The negation on {@link CoreExpression} level is always on the leafs and always strict this method allows the caller to distinguish between a logical
     * negation (<code> strict=true</code>) and <i>without</i> (<code>strict=false</code>). So, if you want to express <i>expression1 <b>without</b>
     * expression2</i> then you should create a {@link CombinedExpression} or type AND with the members <code>expression1, expression2.negate(false)</code>.
     * 
     * @param strict true for a strict NOT, false if you want to express <i>all other</i>
     * @return negated form of this expression
     */
    CoreExpression negate(boolean strict);

    /**
     * Creates a new expression that expresses <i>"this expression <b>without</b> the other expression"</i> (aka <code>expr1 AND NOT expr2</code>).
     * <p/>
     * The method is a shorthand for creating a {@link CombinedExpression} of type {@link CombinedExpressionType#AND} with this expression and the other
     * expression negated.
     * 
     * @param other to be subtracted from this expression
     * @return result expression
     */
    default CoreExpression without(CoreExpression other) {
        return CombinedExpression.andOf(this, other.negate(false));
    }

}
