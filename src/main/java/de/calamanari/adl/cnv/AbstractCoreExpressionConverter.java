//@formatter:off
/*
 * AbstractCoreExpressionConverter
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
import de.calamanari.adl.irl.CombinedExpression;
import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.CoreExpressionVisitor;
import de.calamanari.adl.irl.MatchExpression;
import de.calamanari.adl.irl.NegationExpression;
import de.calamanari.adl.irl.SpecialSetExpression;

/**
 * This class abstracts a couple of basic features for converters that convert a {@link CoreExpression}.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public abstract class AbstractCoreExpressionConverter<R, C extends ConversionContext> extends AbstractExpressionConverter<CoreExpression, R, C>
        implements CoreExpressionVisitor {

    protected AbstractCoreExpressionConverter(Supplier<? extends C> contextSupplier) {
        super(contextSupplier);
    }

    @Override
    public final void visit(MatchExpression expression) {
        increaseNormalizedDepth();
        pushContext();
        this.handleMatchExpression(expression);
        popContext();
        decreaseNormalizedDepth();
    }

    @Override
    public final void visit(CombinedExpression expression, Visit visit) {
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
    public final void visit(NegationExpression expression, Visit visit) {
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
    public final void visit(SpecialSetExpression expression) {
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
        if (ex instanceof TimeOutException || ex instanceof ConversionException) {
            throw ex;
        }
        return new ConversionException(
                "Error during conversion of \n" + this.getRootExpression().format(FormatStyle.PRETTY_PRINT) + "\n\n(" + ex.getMessage() + ")", ex);
    }

    public void handleMatchExpression(MatchExpression expression) {
        // implement on demand
    }

    public void enterCombinedExpression(CombinedExpression expression) {
        // implement on demand
    }

    public void exitCombinedExpression(CombinedExpression expression) {
        // implement on demand
    }

    public void enterNegationExpression(NegationExpression expression) {
        // implement on demand
    }

    public void exitNegationExpression(NegationExpression expression) {
        // implement on demand
    }

    public void handleSpecialSetExpression(SpecialSetExpression expression) {
        // implement on demand
    }

}
