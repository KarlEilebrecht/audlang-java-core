//@formatter:off
/*
 * NativeTypeCaster
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

import de.calamanari.adl.irl.CoreExpression;

/**
 * A {@link NativeTypeCaster} is a way to insert native adjustments in the conversion process of a {@link CoreExpression} to a target language by making
 * incompatible types compatible.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface NativeTypeCaster extends Serializable {

    /**
     * Formats a native db-field of an underlying native storage layer (db) by adjusting the left argument to make it compatible to the referenced type.<br/>
     * This typically means surrounding the native field name with a native cast operation.
     * <p/>
     * Example: <br/>
     * Be <code>color1 = color2</code> an expression, both values are numeric (1, 2, 3) but for whatever reason the native db-field for <code>c1</code> for
     * <code>color1</code> is of type integer and the field <code>c2</code> for <code>color2</code> is of type VARCHAR. The two db-fields cannot be directly
     * compared.<br/>
     * Let there further be a native db-function <code>str</code> that can convert from integer to string.<br/>
     * Now we could create a native expression like <code>str(c1)</code> to overcome the mismatch and create a valid target expression.
     * <p>
     * By default this method simply return the given nativeFieldName.
     * 
     * @param argName argument name on the left
     * @param nativeFieldName technical field name in the underlying storage layer
     * @param argType the {@link AdlType} of the argument to be adjusted
     * @param requestedArgType requested target type (may influence the decision)
     * @return natively formatted (adjusted) access expression to the native field
     * @throws TypeMismatchException in case of any error
     */
    String formatNativeTypeCast(String argName, String nativeFieldName, AdlType argType, AdlType requestedArgType);

}
