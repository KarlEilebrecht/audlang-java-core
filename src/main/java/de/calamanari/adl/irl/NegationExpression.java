//@formatter:off
/*
 * NegationExpression
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

import static de.calamanari.adl.FormatUtils.appendNegationExpressionMultiLine;
import static de.calamanari.adl.FormatUtils.appendNegationExpressionSingleLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.calamanari.adl.AudlangField;
import de.calamanari.adl.AudlangValidationException;
import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.FormatUtils.FormatInfo;
import de.calamanari.adl.Visit;

/**
 * A {@link NegationExpression} represents a logical NOT on the Audlang internal representation layer and covers all kind of negation <i>on attribute level</i>.
 * <p>
 * On this layer all expressions are implicitly STRICT. Thus the strict feature is no longer part of the expression. Only when printing the expression the
 * STRICT flag will be added to keep the expression text compatible to the Audlang specification. The other important difference to the presentation layer is
 * that {@link NegationExpression}s can only contain {@link MatchExpression}s. Negating any {@link CombinedExpression} triggers a structural change so that the
 * negations <i>trickle down</i> to the leafs ({@link MatchExpression}s).
 * <p>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#5-negation">§5</a> Audlang Spec
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@JsonDeserialize(using = JsonDeserializer.None.class)
public record NegationExpression(MatchExpression delegate, String inline) implements SimpleExpression {

    /**
     * @param delegate {@link MatchExpression} to be negated
     * @param inline this is an internal value computed by the constructor, no matter what you specify here, it will be ignored
     */
    public NegationExpression(MatchExpression delegate, @SuppressWarnings("java:S1172") String inline) {
        if (delegate == null) {
            throw new AudlangValidationException("The delegate of a negation must not be null.");
        }
        this.delegate = delegate;
        this.inline = format(FormatStyle.INLINE);
    }

    /**
     * Returns the negated form of this expression according to
     * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#5-negation">Audlang Spec §5</a>
     * <p>
     * The negation on {@link CoreExpression} level sits always on the leafs and it is always strict. This method allows the caller to distinguish between a
     * logical negation (<code> strict=true</code>) and <i>without</i> (<code>strict=false</code>). So, if you want to express <i>expression1 <b>without</b>
     * expression2</i> then you should create a {@link CombinedExpression} of type AND with the members <code>expression1, expression2.negate(false)</code>.
     * 
     * @param strict true for a strict NOT, false if you want to express <i>all other</i>
     * @return negated form of this expression
     */
    public static CoreExpression of(CoreExpression expression, boolean strict) {
        return expression.negate(strict);
    }

    @Override
    public List<CoreExpression> childExpressions() {
        return Collections.unmodifiableList(Arrays.asList(delegate));
    }

    @Override
    public void collectFieldsInternal(Map<String, AudlangField.Builder> fieldMap) {
        delegate.collectFieldsInternal(fieldMap);
    }

    @Override
    public void appendMultiLine(StringBuilder sb, FormatStyle style, int level) {
        appendNegationExpressionMultiLine(sb, delegate, true, Collections.emptyList(), new FormatInfo(style, level));
    }

    @Override
    public void appendSingleLine(StringBuilder sb, FormatStyle style, int level) {
        appendNegationExpressionSingleLine(sb, delegate, true, Collections.emptyList(), new FormatInfo(style, level));
    }

    @Override
    public String toString() {
        return inline;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NegationExpression neg && neg.inline.equals(this.inline);
    }

    @Override
    public int hashCode() {
        return inline.hashCode();
    }

    @Override
    public int compareTo(CoreExpression other) {
        if (other instanceof SpecialSetExpression) {
            // special set expressions come first (usually indicating a mistake)
            return 1;
        }
        else if (other instanceof CombinedExpression) {
            // move the combined expressions after the simple ones
            return -1;
        }
        else if (other instanceof MatchExpression match) {
            int res = delegate.compareTo(match);
            if (res == 0) {
                // negation of a match AFTER the same match
                res = 1;
            }
            return res;
        }
        return SimpleExpression.super.compareTo(other);
    }

    @Override
    public void accept(CoreExpressionVisitor visitor) {
        visitor.visit(this, Visit.ENTER);
        delegate.accept(visitor);
        visitor.visit(this, Visit.EXIT);
    }

    @Override
    public CoreExpression negate(boolean strict) {
        if (strict || delegate.operator() == MatchOperator.IS_UNKNOWN) {
            // (1) all core expression negations are implicitly strict and STRICT NOT STRICT NOT expr <=> expr
            // (2) NOT STRICT NOT arg IS UNKNOWN resp. STRICT NOT STRICT NOT arg IS UNKNOWN both return arg IS UNKNOWN
            // See also Audlang spec §5
            return delegate;
        }
        else {
            // special case Audlang Spec §5, regarding NOT STRICT NOT arg = value or ref, must include the unknowns
            List<CoreExpression> orMembers = new ArrayList<>();
            orMembers.add(delegate);
            orMembers.add(new MatchExpression(delegate.argName(), MatchOperator.IS_UNKNOWN, null, null));
            Operand operand = delegate.operand();
            if (operand.isReference()) {
                orMembers.add(new MatchExpression(operand.value(), MatchOperator.IS_UNKNOWN, null, null));
            }
            return CombinedExpression.of(orMembers, CombinedExpressionType.OR);
        }
    }

    @Override
    public Operand operand() {
        return delegate.operand();
    }

    @Override
    public MatchOperator operator() {
        return delegate.operator();
    }

    @Override
    public String argName() {
        return delegate.argName();
    }

}
