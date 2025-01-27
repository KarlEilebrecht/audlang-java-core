//@formatter:off
/*
 * ContainsNotSupportedException
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
 * Exception to indicate that a conversion failed because the CONTAINS operator is not supported for a given argument resp. by the underlying data store.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@SuppressWarnings("java:S110")
public class ContainsNotSupportedException extends ConversionException {

    private static final long serialVersionUID = 1947265474454671831L;

    /**
     * @param userMessage
     */
    public ContainsNotSupportedException(AudlangMessage userMessage) {
        super(userMessage);
    }

    /**
     * @param message
     * @param userMessage
     */
    public ContainsNotSupportedException(String message, AudlangMessage userMessage) {
        super(message, userMessage);
    }

}
