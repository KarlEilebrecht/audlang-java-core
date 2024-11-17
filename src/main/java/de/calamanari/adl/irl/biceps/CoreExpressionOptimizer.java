//@formatter:off
/*
 * CoreExpressionOptimizer
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

package de.calamanari.adl.irl.biceps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.TimeOut;
import de.calamanari.adl.irl.CoreExpression;

/**
 * The {@link CoreExpressionOptimizer} orchestrates the process of transforming a given expression tree into a standardized optimized form.
 * <p/>
 * Usually, a couple of implications can be resolved to simplify the expression. Sometimes, conditions (or even arguments) turn out to be irrelevant.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class CoreExpressionOptimizer implements ExpressionTreeProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreExpressionOptimizer.class);

    private final TimeOut timeout;

    /**
     * @param timeout if null we will use the default: {@link TimeOut#createDefaultTimeOut(String)}
     */
    public CoreExpressionOptimizer(TimeOut timeout) {
        this.timeout = timeout == null ? TimeOut.createDefaultTimeOut(CoreExpressionOptimizer.class.getSimpleName()) : timeout;
    }

    /**
     * Creates an optimizer with default {@link TimeOut}
     */
    public CoreExpressionOptimizer() {
        this(null);
    }

    /**
     * Processes the tree which holds an expression tree (top-down starting from the root node, and then bottom-up)
     * 
     * @param tree
     */
    @Override
    public void process(EncodedExpressionTree tree) {

        int rootNode = tree.getRootNode();

        String debugStringBefore = null;

        if (LOGGER.isTraceEnabled()) {
            debugStringBefore = tree.createDebugString(rootNode);
            LOGGER.trace("process BEFORE: {}", debugStringBefore);
        }

        tree.getMemberArrayRegistry().triggerHousekeeping(rootNode);

        ImplicationResolver implicationResolver = new ImplicationResolver(timeout);
        OrOfAndNormalizer orOfAndNormalizer = new OrOfAndNormalizer(implicationResolver, timeout);
        OrOfAndOverlapRegrouper orOfAndOverlapRegrouper = new OrOfAndOverlapRegrouper(implicationResolver, timeout);

        implicationResolver.process(tree);
        orOfAndNormalizer.process(tree);
        orOfAndOverlapRegrouper.process(tree);

        if (LOGGER.isTraceEnabled()) {
            String debugStringAfter = tree.createDebugString(tree.getRootNode());
            LOGGER.trace("process AFTER: {}{}", (debugStringAfter.equals(debugStringBefore) ? " " : "*"), debugStringAfter);
        }

    }

    /**
     * Shorthand to run the optimization of an existing {@link CoreExpression}
     * 
     * @param expression
     * @param timeout (null means default, see {@link TimeOut#createDefaultTimeOut(String)})
     * @return optimized expression
     */
    public CoreExpression process(CoreExpression expression) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("process BEFORE: {}", expression.format(FormatStyle.INLINE));
        }
        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(expression);
        process(tree);
        CoreExpression res = tree.toCoreExpression();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("process AFTER: {}{}", (res.equals(expression) ? " " : "*"), res.format(FormatStyle.INLINE));
        }
        return res;
    }

}
