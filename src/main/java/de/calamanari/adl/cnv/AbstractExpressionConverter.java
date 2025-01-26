//@formatter:off
/*
 * AbstractExpressionConverter
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import de.calamanari.adl.ConversionException;

/**
 * Base class for any expression converter, manages the state while traversing expressions
 * <p>
 * <b>Important:</b> An instance has neither a valid {@link #getRootContext()} nor a {@link #getContext()} before {@link #convert(Object)} is called.<br>
 * The {@link #convert(Object)} methods fully (re-)initializes the state. If you want to modify the root context before conversion start, use
 * {@link #setContextPreparator(UnaryOperator)}. Then you get control over any context object after supply but before it is actually used.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public abstract class AbstractExpressionConverter<S, R, C extends ConversionContext> implements ExpressionConverter<S, R> {

    /**
     * initial context
     */
    private C rootContext;

    /**
     * Context of the current level, initially same as root context
     */
    private C context;

    /**
     * The expression to be converted after preparation (see {@link #prepareExpression(Object)})
     */
    private S rootExpression;

    /**
     * This is a callback to allow intercepting the context initialization, e.g., for adding global information
     */
    private UnaryOperator<C> contextPreparator = UnaryOperator.identity();

    /**
     * method to create a fresh context
     */
    private final Supplier<? extends C> contextSupplier;

    /**
     * stack with the contexts for the different levels while visiting expressions
     */
    protected final Deque<C> contextStack = new ArrayDeque<>();

    /**
     * nesting depth when visiting nested expressions
     */
    private int normalizedDepth = 0;

    /**
     * Returns the root context of the current run
     * 
     * @return root context
     */
    protected final C getRootContext() {
        return rootContext;
    }

    /**
     * @return enclosing context, if any, otherwise null
     */
    protected final C getParentContext() {
        return contextStack.peek();
    }

    /**
     * @return the current context
     */
    protected final C getContext() {
        return context;
    }

    /**
     * @return a freshly created and prepared context
     */
    protected final C createNewContext() {
        return contextPreparator.apply(contextSupplier.get());
    }

    /**
     * @return the root expression currently being processed
     */
    protected final S getRootExpression() {
        return rootExpression;
    }

    /**
     * @return the depth in the iterated expression tree ignoring certain levels
     */
    protected final int getNormalizedDepth() {
        return normalizedDepth;
    }

    /**
     * @return the raw depth in the iterated expression tree
     */
    protected final int getCurrentDepth() {
        return contextStack.size();
    }

    /**
     * Implementation specific increment of the normalized nesting depth
     */
    protected final void increaseNormalizedDepth() {
        normalizedDepth++;
    }

    /**
     * Implementation specific reduction of the normalized nesting depth
     */
    protected final void decreaseNormalizedDepth() {
        normalizedDepth--;
    }

    /**
     * @param contextSupplier method for later creation of fresh contexts
     */
    protected AbstractExpressionConverter(Supplier<? extends C> contextSupplier) {
        if (contextSupplier == null) {
            throw new IllegalArgumentException("Context supplier cannot be null.");
        }
        this.contextSupplier = contextSupplier;
    }

    /**
     * switches to a fresh context after putting the previous context on the stack to become the {@link #getParentContext()}
     */
    protected void pushContext() {
        contextStack.push(context);
        context = createNewContext();
    }

    /**
     * replaces the current context with the {@link #getParentContext()}
     */
    protected void popContext() {
        context = contextStack.pop();
    }

    /**
     * This method is for concrete converters to allow to replace the root expression <i>before</i> starting traversal.
     * 
     * @return either the unmodified root expression as returned by {@link #getRootExpression()} or a somehow modified expression
     */
    protected S prepareRootExpression() {
        return rootExpression;
    }

    /**
     * Advanced preparation step for <i>every</i> created context instance obtained from the supplier
     * <p>
     * Provide a function to add (global) elements or to replace the context with anything compatible.
     * 
     * @param rootContextPreparator null means {@link UnaryOperator#identity()} (default)
     */
    public void setContextPreparator(UnaryOperator<C> contextPreparator) {
        this.contextPreparator = contextPreparator == null ? UnaryOperator.identity() : contextPreparator;
    }

    /**
     * This method resets the state of this converter as a first step during {@link #convert(Object)}.
     * <p>
     * If you override this method, don't forget to call the super-implementation!
     */
    protected void init() {
        this.rootContext = null;
        this.context = null;
        this.normalizedDepth = 0;
        this.contextStack.clear();
        this.rootExpression = null;
    }

    /**
     * Flow is as follows:
     * <ul>
     * <li><code><b>convert(rootExpression)</b></code>
     * <ul>
     * <li><code>{@link #init()}</code></li>
     * <li><code>rootContext = {@link #contextPreparator}.apply({@link #createNewContext()})</code></li>
     * <li><code>{@link #prepareRootExpression()}</code></li>
     * <li>Traverse expression (conversion)</li>
     * <li><code>{@link #finishResult()}</code></li>
     * </ul>
     * </ul>
     */
    @Override
    public final R convert(S rootExpression) {
        this.init();
        this.rootContext = createNewContext();
        this.context = this.rootContext;
        this.rootExpression = rootExpression;
        this.rootExpression = prepareRootExpression();
        if (this.rootExpression == null) {
            throw new ConversionException("rootExpression must not be null.");
        }
        try {
            traverse();
            return finishResult();
        }
        catch (RuntimeException ex) {
            throw createConversionException(ex);
        }
    }

    /**
     * Called to start traversing the root expression
     * 
     */
    protected abstract void traverse();

    /**
     * This method gets called after traversing the expression
     * 
     * 
     * @return final conversion result
     */
    protected abstract R finishResult();

    /**
     * Called if the conversion stumbles across any runtime exception, so the current implementation can wrap it properly into a {@link ConversionException}.
     * 
     * @param ex caught exception
     * @return wrapped exception
     */
    protected abstract ConversionException createConversionException(RuntimeException ex);

}
