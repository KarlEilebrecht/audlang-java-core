//@formatter:off
/*
 * SimpleExpression
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

import de.calamanari.adl.irl.MatchExpression.MatchOperator;

/**
 * The purpose of this interface is clarity for method signatures that cannot deal with combined or special expressions.
 * <p/>
 * This interface only defines some of the properties a match or negated match has.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface SimpleExpression extends CoreExpression {

    /**
     * @return the operand this simple expression deals with, may be null in case of IS UNKNOWN
     */
    Operand operand();

    /**
     * @return the operator this simple expression deals with, never null
     */
    MatchOperator operator();

    /**
     * @return the argument name, never null
     */
    String argName();

    /**
     * @return if {@link #operand()} is not null and operand is a reference then this method returns the value of the operand
     */
    default String referencedArgName() {
        Operand op = operand();
        return (op != null && op.isReference()) ? op.value() : null;
    }

}
