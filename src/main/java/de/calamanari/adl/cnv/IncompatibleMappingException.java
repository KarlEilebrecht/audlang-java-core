//@formatter:off
/*
 * IncompatibleMappingException
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
 * Exception to be thrown if a conversion failed because a given mapping is incompatible to the expression.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@SuppressWarnings("java:S110")
public class IncompatibleMappingException extends ConversionException {

    private static final long serialVersionUID = 846998201365743602L;

    /**
     * @param message
     * @param cause
     * @param userMessage
     */
    public IncompatibleMappingException(String message, Throwable cause, AudlangMessage userMessage) {
        super(message, cause, userMessage);
    }

    /**
     * @param message
     * @param cause
     */
    public IncompatibleMappingException(String message, Throwable cause) {
        super(message, cause, AudlangMessage.msg(CommonErrors.ERR_3000_MAPPING_FAILED));
    }

    /**
     * @param message
     */
    public IncompatibleMappingException(String message) {
        super(message, AudlangMessage.msg(CommonErrors.ERR_3000_MAPPING_FAILED));
    }

}
