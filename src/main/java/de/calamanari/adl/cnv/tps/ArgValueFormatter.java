//@formatter:off
/*
 * ArgValueFormatter
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

package de.calamanari.adl.cnv.tps;

import java.io.Serializable;

import de.calamanari.adl.irl.MatchExpression.MatchOperator;

/**
 * Classes implementing this interface encapsulate the knowledge how to format an argument value within the realm of its argument and in the context of the
 * current operator.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface ArgValueFormatter extends Serializable {

    /**
     * Formats a given argument value considering the realm of its argument and the context of the current operator.
     * <p/>
     * Clarification: This method <b>only formats the value</b>, not the expression. The argName and operator are just context information.
     * 
     * @param argName
     * @param argValue
     * @param operator (as a hint for formatting the value)
     * @return formatted value
     */
    String format(String argName, String argValue, MatchOperator operator);

}
