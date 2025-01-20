//@formatter:off
/*
 * PassThroughTypeCaster
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

import de.calamanari.adl.AudlangErrorInfo;
import de.calamanari.adl.CommonErrors;

/**
 * Dummy implementation of a type caster that returns the target field name as-is (pass-through) if the requested type is the same as the given one or throws a
 * {@link TypeMismatchException} if not.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@SuppressWarnings("java:S6548")
public class PassThroughTypeCaster implements NativeTypeCaster {

    private static final long serialVersionUID = 7717200000162634541L;

    private static final PassThroughTypeCaster INSTANCE = new PassThroughTypeCaster();

    private PassThroughTypeCaster() {
        // singleton
    }

    public static PassThroughTypeCaster getInstance() {
        return INSTANCE;
    }

    @Override
    public String formatNativeTypeCast(String argName, String nativeFieldName, AdlType argType, AdlType requestedArgType) {
        if (argType.name().equals(requestedArgType.name())) {
            return nativeFieldName;
        }
        else {
            AudlangErrorInfo errorInfo = AudlangErrorInfo.argError(CommonErrors.ERR_3001_TYPE_MISMATCH, argName);
            throw new TypeMismatchException(String.format("Unable to make %s (type: %s, native field name: %s) compatible to type %s", argName, argType,
                    nativeFieldName, requestedArgType), errorInfo);
        }
    }

    /**
     * @return singleton instance in JVM
     */
    Object readResolve() {
        return INSTANCE;
    }

}
