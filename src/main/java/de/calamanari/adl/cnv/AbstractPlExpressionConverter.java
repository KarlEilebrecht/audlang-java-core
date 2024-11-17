//@formatter:off
/*
 * AbstractPlExpressionConverter
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

import java.util.function.Supplier;

import de.calamanari.adl.ConversionException;
import de.calamanari.adl.FormatStyle;
import de.calamanari.adl.TimeOutException;
import de.calamanari.adl.Visit;
import de.calamanari.adl.erl.PlCombinedExpression;
import de.calamanari.adl.erl.PlCurbExpression;
import de.calamanari.adl.erl.PlExpression;
import de.calamanari.adl.erl.PlExpressionVisitor;
import de.calamanari.adl.erl.PlMatchExpression;
import de.calamanari.adl.erl.PlNegationExpression;
import de.calamanari.adl.erl.PlSpecialSetExpression;

/**
 * This class abstracts a couple of basic features for converters that convert a presentation layer expression.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public abstract class AbstractPlExpressionConverter<R, C extends ConversionContext> extends AbstractExpressionConverter<PlExpression<?>, R, C>
        implements PlExpressionVisitor {

    protected AbstractPlExpressionConverter(Supplier<? extends C> contextSupplier) {
        super(contextSupplier);
    }

    @Override
    public final void visit(PlMatchExpression expression) {
        increaseNormalizedDepth();
        pushContext();
        this.handleMatchExpression(expression);
        popContext();
        decreaseNormalizedDepth();
    }

    @Override
    public final void visit(PlCombinedExpression expression, Visit visit) {
        if (visit == Visit.ENTER) {
            increaseNormalizedDepth();
            pushContext();
            enterCombinedExpression(expression);
        }
        else {
            exitCombinedExpression(expression);
            popContext();
            decreaseNormalizedDepth();
        }
    }

    @Override
    public final void visit(PlCurbExpression expression, Visit visit) {
        if (visit == Visit.ENTER) {
            increaseNormalizedDepth();
            pushContext();
            enterCurbExpression(expression);
        }
        else {
            exitCurbExpression(expression);
            popContext();
            decreaseNormalizedDepth();
        }
    }

    @Override
    public final void visit(PlNegationExpression expression, Visit visit) {
        // Note: we do not increase the normalized depth in case of NOT
        if (visit == Visit.ENTER) {
            pushContext();
            enterNegationExpression(expression);
        }
        else {
            exitNegationExpression(expression);
            popContext();
        }
    }

    @Override
    public final void visit(PlSpecialSetExpression expression) {
        increaseNormalizedDepth();
        pushContext();
        handleSpecialSetExpression(expression);
        popContext();
        decreaseNormalizedDepth();
    }

    @Override
    protected void traverse() {
        this.getRootExpression().accept(this);
    }

    @Override
    protected ConversionException createConversionException(RuntimeException ex) {
        if (ex instanceof TimeOutException) {
            throw ex;
        }
        return new ConversionException(
                "Error during conversion of \n" + this.getRootExpression().format(FormatStyle.PRETTY_PRINT) + "\n\n(" + ex.getMessage() + ")", ex);
    }

    public void handleMatchExpression(PlMatchExpression expression) {
        // implement on demand
    }

    public void enterCombinedExpression(PlCombinedExpression expression) {
        // implement on demand
    }

    public void exitCombinedExpression(PlCombinedExpression expression) {
        // implement on demand
    }

    public void enterCurbExpression(PlCurbExpression expression) {
        // implement on demand
    }

    public void exitCurbExpression(PlCurbExpression expression) {
        // implement on demand
    }

    public void enterNegationExpression(PlNegationExpression expression) {
        // implement on demand
    }

    public void exitNegationExpression(PlNegationExpression expression) {
        // implement on demand
    }

    public void handleSpecialSetExpression(PlSpecialSetExpression expression) {
        // implement on demand
    }

}
