//@formatter:off
/*
 * ExpressionTreeLevel
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

import java.io.Serializable;

import de.calamanari.adl.cnv.ConversionContext;

/**
 * A {@link ExpressionTreeLevel} is a temporary container for members on a certain level while building the expression tree bottom-up.
 */
public record ExpressionTreeLevel(GrowingIntArray members) implements ConversionContext, Serializable {

    /**
     * @return deep copy of this tree-level but unrelated to the given instance
     */
    public ExpressionTreeLevel copy() {
        return new ExpressionTreeLevel(members.copy());
    }

    @Override
    public void clear() {
        members.clear();
    }

}