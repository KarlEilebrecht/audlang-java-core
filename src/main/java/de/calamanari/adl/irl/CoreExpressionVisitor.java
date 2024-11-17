//@formatter:off
/*
 * CoreExpressionVisitor
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

import de.calamanari.adl.Visit;

/**
 * A {@link CoreExpressionVisitor} receives all details of a {@link CoreExpression} (internal representation layer) to convert it into a new presentation form.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface CoreExpressionVisitor {

    void visit(MatchExpression expression);

    void visit(SpecialSetExpression expression);

    void visit(CombinedExpression expression, Visit visit);

    void visit(NegationExpression expression, Visit visit);

}
