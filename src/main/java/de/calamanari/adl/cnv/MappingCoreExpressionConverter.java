//@formatter:off
/*
 * MappingCoreExpressionConverter
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

package de.calamanari.adl.cnv;

import de.calamanari.adl.irl.CombinedExpression;
import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.MatchExpression;
import de.calamanari.adl.irl.MatchExpression.MatchOperator;
import de.calamanari.adl.irl.NegationExpression;
import de.calamanari.adl.irl.Operand;
import de.calamanari.adl.irl.SpecialSetExpression;

/**
 * The {@link MappingCoreExpressionConverter} applies a {@link ArgNameValueMapping} to a given expression to translate argument names and values.
 * <p/>
 * Other than on the presentation layer this {@link CoreExpression} converter also covers <i>non-bijective</i> mappings and those that imply changes to the data
 * model like mapping two values of the same attribute in the source system to two different attributes in the target system.
 * <p/>
 * Example: <code>color=blue OR color=red</code> could be mapped so that the result expression would be: <code>arg123=1 OR arg162=1</code> (structurally
 * different data model).
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class MappingCoreExpressionConverter extends AbstractCoreExpressionConverter<CoreExpression, DefaultConversionContext<CoreExpression>> {

    private final ArgNameValueMapper mapper;

    /**
     * Creates a new converter based on the given mapper
     * 
     * @param mapper
     */
    public MappingCoreExpressionConverter(ArgNameValueMapper mapper) {
        super(DefaultConversionContext::new);
        this.mapper = mapper;
    }

    @Override
    public void handleMatchExpression(MatchExpression expression) {
        MatchExpression mappedExpression = null;
        if (expression.operand() == null) {
            QualifiedArgValue qav = mapper.mapArgValue(expression.argName(), null);
            mappedExpression = new MatchExpression(qav.argName(), expression.operator(), qav.argValue() == null ? null : new Operand(qav.argValue(), false),
                    null);
        }
        else if (expression.operand().isReference()) {
            QualifiedArgValue qav = mapper.mapArgValue(expression.argName(), null);
            QualifiedArgValue qavRef = mapper.mapArgValue(expression.operand().value(), null);
            mappedExpression = new MatchExpression(qav.argName(), expression.operator(), new Operand(qavRef.argName(), true), null);
        }
        else if (expression.operator() == MatchOperator.CONTAINS) {
            QualifiedArgValue qav = mapper.mapArgValue(expression.argName(), null);
            mappedExpression = new MatchExpression(qav.argName(), expression.operator(), expression.operand(), null);
        }
        else {
            QualifiedArgValue qav = mapper.mapArgValue(expression.argName(), expression.operand().value());
            mappedExpression = new MatchExpression(qav.argName(), expression.operator(), new Operand(qav.argValue(), false), null);
        }
        getParentContext().members().add(mappedExpression);
    }

    @Override
    public void exitCombinedExpression(CombinedExpression expression) {
        getParentContext().members().add(CombinedExpression.of(getContext().members(), expression.combiType()));
    }

    @Override
    public void exitNegationExpression(NegationExpression expression) {
        getParentContext().members().add(new NegationExpression((MatchExpression) getContext().members().get(0), null));
    }

    @Override
    public void handleSpecialSetExpression(SpecialSetExpression expression) {
        getParentContext().members().add(expression);
    }

    @Override
    protected CoreExpression finishResult() {
        return getRootContext().members().get(0);
    }

}
