//@formatter:off
/*
 * ConfigException
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
import de.calamanari.adl.CommonErrors;
import de.calamanari.adl.ConversionException;

/**
 * Exception to indicate configuration errors
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@SuppressWarnings("java:S110")
public class ConfigException extends ConversionException {

    private static final long serialVersionUID = 7736995313040055415L;

    /**
     * @param message
     * @param userMessage
     */
    public ConfigException(String message, AudlangMessage userMessage) {
        super(message, userMessage);
    }

    /**
     * @param message
     * @param cause
     * @param userMessage
     */
    public ConfigException(String message, Throwable cause, AudlangMessage userMessage) {
        super(message, cause, userMessage);
    }

    /**
     * @param message
     * @param cause
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause, AudlangMessage.msg(CommonErrors.ERR_4002_CONFIG_ERROR));
    }

    /**
     * @param message
     */
    public ConfigException(String message) {
        super(message, AudlangMessage.msg(CommonErrors.ERR_4002_CONFIG_ERROR));
    }

}
