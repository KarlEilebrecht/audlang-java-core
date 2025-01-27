//@formatter:off
/*
 * AmbiguousMappingException
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

import de.calamanari.adl.AudlangMessage;
import de.calamanari.adl.CommonErrors;
import de.calamanari.adl.ConversionException;

/**
 * Exception to be thrown if any mapping detects that a source value cannot be mapped to a target value because there are multiple options.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@SuppressWarnings("java:S110")
public class AmbiguousMappingException extends ConversionException {

    private static final long serialVersionUID = -5301772588584728076L;

    /**
     * @param message
     */
    public AmbiguousMappingException(String message) {
        super(message, AudlangMessage.msg(CommonErrors.ERR_3000_MAPPING_FAILED));
    }

    /**
     * @param message
     * @param userMessage
     */
    public AmbiguousMappingException(String message, AudlangMessage userMessage) {
        super(message, userMessage);
    }

}
