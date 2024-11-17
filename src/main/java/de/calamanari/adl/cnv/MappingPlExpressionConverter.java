//@formatter:off
/*
 * MappingPlExpressionConverter
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

import java.util.ArrayList;
import java.util.List;

import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.erl.PlCombinedExpression;
import de.calamanari.adl.erl.PlCurbExpression;
import de.calamanari.adl.erl.PlExpression;
import de.calamanari.adl.erl.PlMatchExpression;
import de.calamanari.adl.erl.PlMatchExpression.PlMatchOperator;
import de.calamanari.adl.erl.PlNegationExpression;
import de.calamanari.adl.erl.PlOperand;
import de.calamanari.adl.erl.PlSpecialSetExpression;

/**
 * The {@link MappingPlExpressionConverter} applies an {@link ArgNameValueMapping} to change the names and values of arguments in a given expression.
 * <p/>
 * This is primarily meant for switching between labels and technical IDs on the presentation layer.
 * <p/>
 * Mappings must be bijective (reversible) and structure-preserving (see {@link ArgNameValueMapper#isArgumentStructurePreserving()}).
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class MappingPlExpressionConverter extends AbstractPlExpressionConverter<PlExpression<?>, DefaultConversionContext<PlExpression<?>>> {

    private final ArgNameValueMapper mapper;

    /**
     * Creates a new converter based on the given mapper
     * 
     * @param mapper
     */
    public MappingPlExpressionConverter(ArgNameValueMapper mapper) {
        super(DefaultConversionContext::new);
        if (!mapper.isArgumentStructurePreserving()) {
            throw new IncompatibleMappingException("""
                    The given mapping is not applicable to a presentation layer expression because it potentially changes the structure of the expression.
                    PlExpression mapping conversions are intended to switch (back and forth) between ids/labels without changing the layout.
                    """);
        }
        if (!mapper.isBijective()) {
            throw new IncompatibleMappingException("""
                    The given mapping is not applicable to a presentation layer expression because it is not bijective.
                    PlExpression mapping conversions are intended to switch (back and forth) between ids/labels, which is impossible with the given mapping.
                    """);
        }
        this.mapper = mapper;
    }

    /**
     * Simply maps all the operands (if any) to create replacement operands<br/>
     * Because the mapper is bijective and structure-preserving the operands will remain compatible to the operator.
     * <p/>
     * Referenced argument names will be mapped to the corresponding argument name.
     * 
     * @param argName
     * @param operands
     * @return mapped operands
     */
    private List<PlOperand> mapOperands(PlMatchOperator operator, String argName, List<PlOperand> operands) {
        if (operator == PlMatchOperator.CONTAINS || operator == PlMatchOperator.CONTAINS_ANY_OF || operator == PlMatchOperator.NOT_CONTAINS
                || operator == PlMatchOperator.NOT_CONTAINS_ANY_OF || operator == PlMatchOperator.STRICT_NOT_CONTAINS
                || operator == PlMatchOperator.STRICT_NOT_CONTAINS_ANY_OF) {
            return operands;
        }
        List<PlOperand> destOperands = new ArrayList<>(operands.size());
        for (PlOperand op : operands) {
            if (op.isReference()) {
                String destValue = mapper.mapArgValue(op.value(), null).argName();
                destOperands.add(new PlOperand(destValue, true, op.comments()));
            }
            else {
                String destValue = mapper.mapArgValue(argName, op.value()).argValue();
                destOperands.add(new PlOperand(destValue, false, op.comments()));
            }
        }
        return destOperands;
    }

    /**
     * @param argName
     * @return mapped argument name
     */
    private String mapArgName(String argName) {
        return mapper.mapArgValue(argName, null).argName();
    }

    @Override
    public void handleMatchExpression(PlMatchExpression expression) {
        PlMatchExpression mappedMatch = new PlMatchExpression(mapArgName(expression.argName()), expression.operator(),
                mapOperands(expression.operator(), expression.argName(), expression.operands()), expression.comments());
        getParentContext().members().add(mappedMatch);
    }

    @Override
    public void exitCombinedExpression(PlCombinedExpression expression) {
        PlCombinedExpression mappedExpression = new PlCombinedExpression(expression.combiType(), getContext().members(), expression.comments());
        getParentContext().members().add(mappedExpression);
    }

    @Override
    public void exitCurbExpression(PlCurbExpression expression) {
        PlCurbExpression mappedExpression = new PlCurbExpression(
                new PlCombinedExpression(CombinedExpressionType.OR, getContext().members(), expression.comments()), expression.operator(), expression.bound(),
                expression.comments());
        getParentContext().members().add(mappedExpression);
    }

    @Override
    public void exitNegationExpression(PlNegationExpression expression) {
        getParentContext().members().add(new PlNegationExpression(getContext().members().get(0), expression.isStrict(), expression.comments()));
    }

    @Override
    public void handleSpecialSetExpression(PlSpecialSetExpression expression) {
        getParentContext().members().add(expression);
    }

    @Override
    protected PlExpression<?> finishResult() {
        return getRootContext().members().get(0);
    }

}
