//@formatter:off
/*
 * ArgNameValueMapper
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

import java.io.Serializable;

/**
 * An {@link ArgNameValueMapper} maps argument names and values of expressions, meant to conveniently switch between technical ids and labels.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface ArgNameValueMapper extends Serializable {

    /**
     * @param argName the argument name (realm) of the value, not null
     * @param value to be mapped, can be null (IS-UNKNOWN case)
     * @return the mapped argument value (qualified by the target argument name)
     * @throws MappingNotFoundException if there was no mapped argument value associated with the given one
     */
    QualifiedArgValue mapArgValue(String argName, String value);

    /**
     * Tells if the mapping preserves the argument structure of an expression. If this method returns true than the mapping never maps two values related to the
     * same source argument to two different target arguments.
     * <p>
     * Example: if <code>color.blue</code> maps to <code>arg4711.blue</code> and <code>color.red</code> maps to <code>arg888.red</code> then this method should
     * return <b>false</b>.
     * 
     * @return true if a single argument name maps to a single target argument name
     */
    boolean isArgumentStructurePreserving();

    /**
     * Tells wether a call to {@link #reverse()} would succeed or not
     * 
     * @return true if the mapping can be reversed
     */
    boolean isBijective();

    /**
     * @return a vice-versa-mapper for the given mapper if the mapping is bijective
     * @throws AmbiguousMappingException if the mapping of the source mapper was not bijective (two names or values mapped to the same target value)
     */
    ArgNameValueMapper reverse();

}
