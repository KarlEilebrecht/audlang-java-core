//@formatter:off
/*
 * PlToCoreExpressionConverter
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

import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.ALL;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.NONE;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.getOperator;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isNegation;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.negate;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.CombinedExpressionType;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.SpecialSetType;
import de.calamanari.adl.TimeOut;
import de.calamanari.adl.erl.PlCombinedExpression;
import de.calamanari.adl.erl.PlCurbExpression;
import de.calamanari.adl.erl.PlExpression;
import de.calamanari.adl.erl.PlMatchExpression;
import de.calamanari.adl.erl.PlNegationExpression;
import de.calamanari.adl.erl.PlOperand;
import de.calamanari.adl.erl.PlSpecialSetExpression;
import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.MatchExpression;
import de.calamanari.adl.irl.MatchOperator;
import de.calamanari.adl.irl.Operand;
import de.calamanari.adl.irl.biceps.CoreExpressionCodec;
import de.calamanari.adl.irl.biceps.CoreExpressionCodec.Dictionary;
import de.calamanari.adl.irl.biceps.EncodedExpressionTree;
import de.calamanari.adl.irl.biceps.ExpressionTreeLevel;
import de.calamanari.adl.irl.biceps.ExpressionTreeProcessor;
import de.calamanari.adl.irl.biceps.ImplicationResolver;
import de.calamanari.adl.irl.biceps.NodeType;

/**
 * The {@link PlToCoreExpressionConverter} takes a presentation layer expression and produces a corresponding {@link CoreExpression}.
 * <p>
 * Because {@link CoreExpression}s have less features and they follow a couple of conventions, the created expression may look drastically different which still
 * reflecting the same <i>logical</i> expression.
 * <p>
 * Optionally, the converter can be configured with a post-processor ({@link ExpressionTreeProcessor}), so that the produced expression can be optimized as part
 * of the conversion process.
 * <p>
 * Conversion and especially optimization can take long, maybe too long. Thus, it is possible to configure a timeout after the process will throw a
 * {@link TimeoutException} indicating that the duration exceeded the estimated time.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class PlToCoreExpressionConverter extends AbstractPlExpressionConverter<CoreExpression, ExpressionTreeLevel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlToCoreExpressionConverter.class);

    /**
     * Before creating the {@link CoreExpression} we represent the expression tree in binary form, see {@link CoreExpressionCodec}.
     */
    private final EncodedExpressionTree tree;

    /**
     * post-processor or null if not configured
     */
    private final ExpressionTreeProcessor postProcessor;

    private final TimeOut timeout;

    /**
     * Creates a converter for the given tree
     * 
     * @param tree for the conversion, usually starting with a fresh one
     * @param postProcessor optional post-processor or null
     * @param timeout if null we will use the default: {@link TimeOut#createDefaultTimeOut(String)}
     */
    protected PlToCoreExpressionConverter(EncodedExpressionTree tree, ExpressionTreeProcessor postProcessor, TimeOut timeout) {
        super(tree::createTreeLevel);
        this.timeout = timeout == null ? TimeOut.createDefaultTimeOut(ImplicationResolver.class.getSimpleName()) : timeout;
        this.tree = tree;
        this.postProcessor = postProcessor;
    }

    /**
     * Creates a new converter with a fresh (empty) expression tree
     * 
     * @param postProcessor optional post-processor or null
     * @param timeout if null we will use the default: {@link TimeOut#createDefaultTimeOut(String)}
     */
    public PlToCoreExpressionConverter(ExpressionTreeProcessor postProcessor, TimeOut timeout) {
        this(new EncodedExpressionTree(), postProcessor, timeout);
    }

    /**
     * Creates a new converter with a fresh (empty) expression tree and default timeout
     * 
     * @param postProcessor optional post-processor or null
     */
    public PlToCoreExpressionConverter(ExpressionTreeProcessor postProcessor) {
        this(new EncodedExpressionTree(), postProcessor, null);
    }

    /**
     * Creates a new converter with a fresh (empty) expression tree and default timeout and without post-processor
     */
    public PlToCoreExpressionConverter() {
        this(null);
    }

    @Override
    public void handleMatchExpression(PlMatchExpression expression) {
        timeout.assertHaveTime();
        getParentContext().members().add(tree.createNode(createMatchExpression(expression)));
    }

    @Override
    public void exitCombinedExpression(PlCombinedExpression expression) {
        timeout.assertHaveTime();
        if (expression.combiType() == CombinedExpressionType.OR) {
            getParentContext().members().add(tree.createNode(NodeType.OR, getContext().members()));
        }
        else {
            getParentContext().members().add(tree.createNode(NodeType.AND, getContext().members()));
        }
    }

    @Override
    public void enterCurbExpression(PlCurbExpression expression) {
        throw new IllegalStateException("BUG: Unexpected expression type (curb expressions should have been resolved before): " + expression);
    }

    @Override
    public void exitNegationExpression(PlNegationExpression expression) {
        timeout.assertHaveTime();
        if (!expression.isStrict()) {
            throw new IllegalStateException("BUG: Unexpected non-strict negation: " + expression);
        }
        else if (!(expression.delegate() instanceof PlMatchExpression)) {
            throw new IllegalStateException("BUG: Unexpected negation on aggregate (should have been resolved before): " + expression);
        }

        if (getContext().members().size() == 1) {
            int encoded = getContext().members().get(0);
            if (isNegation(encoded) && getOperator(encoded) == MatchOperator.IS_UNKNOWN) {
                // Special case: STRICT NOT arg IS UNKNOWN <=> <NONE>
                // This happens if the the original expression was: arg != @arg (can't be fulfilled under any circumstances)
                getParentContext().members().add(NONE);
            }
            else {
                getParentContext().members().add(negate(encoded));
            }
        }
        else {
            throw new IllegalStateException(String.format("BUG: Expected single match child element, found: %s after processing %s", getContext(), expression));
        }

    }

    @Override
    public void handleSpecialSetExpression(PlSpecialSetExpression expression) {
        timeout.assertHaveTime();
        if (expression.setType() == SpecialSetType.ALL) {
            getParentContext().members().add(ALL);
        }
        else {
            getParentContext().members().add(NONE);
        }
    }

    @Override
    protected PlExpression<?> prepareRootExpression() {
        PlExpression<?> res = getRootExpression();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Preparing expression for conversion:\n{}", res.format(FormatStyle.PRETTY_PRINT));
        }
        res = res.resolveHigherLanguageFeatures();
        CoreExpressionCodec codec = new CoreExpressionCodec(new Dictionary(res.allFields()));
        tree.initialize(codec);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Prepared expression for conversion:\n{}", res.format(FormatStyle.PRETTY_PRINT));
        }
        return res;
    }

    @Override
    protected CoreExpression finishResult() {
        if (postProcessor != null) {
            postProcessor.process(tree);
        }
        return tree.toCoreExpression();

    }

    /**
     * @param expression
     * @return corresponding {@link CoreExpression} for the given presentation layer expression
     */
    private CoreExpression createMatchExpression(PlMatchExpression expression) {
        switch (expression.operator()) {
        case IS_UNKNOWN:
            return MatchExpression.of(expression.argName(), MatchOperator.IS_UNKNOWN, null);
        case EQUALS:
            return MatchExpression.of(expression.argName(), MatchOperator.EQUALS, convertMatchOperand(expression.operands().get(0)));
        case CONTAINS:
            return MatchExpression.of(expression.argName(), MatchOperator.CONTAINS, convertMatchOperand(expression.operands().get(0)));
        case LESS_THAN:
            return MatchExpression.of(expression.argName(), MatchOperator.LESS_THAN, convertMatchOperand(expression.operands().get(0)));
        case GREATER_THAN:
            return MatchExpression.of(expression.argName(), MatchOperator.GREATER_THAN, convertMatchOperand(expression.operands().get(0)));
        // $CASES-OMITTED$
        default:
            throw new IllegalStateException("BUG: Unexpected operator (all higher-level language features should have been resolved): " + expression);
        }
    }

    /**
     * @param operand
     * @return converted operator suitable for {@link MatchException}s
     */
    private static Operand convertMatchOperand(PlOperand operand) {
        return new Operand(operand.value(), operand.isReference());
    }

}
