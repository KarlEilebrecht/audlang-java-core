//@formatter:off
/*
 * TypeMismatchException
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

import de.calamanari.adl.AudlangMessage;
import de.calamanari.adl.ConversionException;

/**
 * Exception to be thrown if it was impossible to format a value (or adjust the type) to make it compatible to a target type.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@SuppressWarnings("java:S110")
public class TypeMismatchException extends ConversionException {

    private static final long serialVersionUID = -2582467015398806697L;

    /**
     * @param userMessage
     */
    public TypeMismatchException(AudlangMessage userMessage) {
        super(userMessage);
    }

    /**
     * @param message
     * @param userMessage
     */
    public TypeMismatchException(String message, AudlangMessage userMessage) {
        super(message, userMessage);
    }

}
