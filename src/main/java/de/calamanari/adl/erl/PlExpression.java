//@formatter:off
/*
 * PlExpression
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

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import de.calamanari.adl.AudlangExpression;
import de.calamanari.adl.FormatStyle;

/**
 * {@link PlExpression} stands for <i>external re<b>P</b>resentation <b>L</b>ayer expression</i>, any expression for the external representation. This kind of
 * expression must be immutable and represent an acyclic directed graph (DAG) of a user-facing Audlang-expressing in its original form (not optimized, order
 * preserved, all potentially useless items preserved).
 * <p>
 * The main purpose of this expression type is parsing and reproduction (maybe pretty-printing) of a given valid Audlang expression without changing it.
 * <p>
 * Because {@link PlExpression}s are not normalized, many different expressions may be logically identical. Thus, you can only compare two {@link PlExpression}s
 * on the textual level but not on the logical level.
 * <p>
 * See also <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#3-basic-expressions">§3</a>,
 * <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#3-composite-expressions">§4</a> AudLang Spec
 * <p>
 * <b>Instances must be immutable.</b>
 * <p>
 * The <code>toString()</code> method of any {@link PlExpression} <b>must</b> return a valid Audlang expression equivalent to the instance.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@JsonDeserialize(using = PlExpressionDeserializer.class)
public interface PlExpression<T extends PlExpression<T>> extends AudlangExpression<PlExpression<?>, PlExpressionVisitor>, CommentAware {

    @Override
    default boolean shouldUseMultiLineFormatting(FormatStyle style) {
        return style.isMultiLine() && (!this.childExpressions().isEmpty() || this.allComments().stream().anyMatch(PlComment::isComplex));
    }

    /**
     * Creates an expression with the same settings but without any comments (recursively).
     * 
     * @return new instance or this instance if there was effectively no change
     */
    T stripComments();

    /**
     * Creates an expression with the given comments on expression level.<br>
     * Any previously existing comments on expression level will not be copied.
     * <p>
     * <b>Clarification:</b> This method does not change any comments on any child expressions.
     * 
     * @param comments to set
     * @return new instance or this instance if there was effectively no change
     */
    T withComments(List<PlComment> comments);

    /**
     * @return new instance (possibly of a different sub type) without any higher-level language elements or this instance if there was no change
     */
    @SuppressWarnings("java:S1452")
    PlExpression<?> resolveHigherLanguageFeatures();

}
