//@formatter:off
/*
 * AudlangValidationException
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

package de.calamanari.adl;

/**
 * Exception to be thrown if somewhere during the creation of the expression a structural or parameter error was detected.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class AudlangValidationException extends AdlException {

    private static final long serialVersionUID = 6907789382461441174L;

    private final AudlangMessage userMessage;

    /**
     * @param message
     * @param cause
     * @param userMessage detail information
     */
    public AudlangValidationException(String message, Throwable cause, AudlangMessage userMessage) {
        super(message + " " + AudlangMessage.toStringOrGenericError(userMessage), cause);
        this.userMessage = userMessage == null ? AudlangMessage.msg(CommonErrors.ERR_4003_GENERAL_ERROR) : userMessage;
    }

    /**
     * @param message
     * @param cause
     */
    public AudlangValidationException(String message, Throwable cause) {
        this(message, cause, null);
    }

    /**
     * @param message
     * @param userMessage detail information
     */
    public AudlangValidationException(String message, AudlangMessage userMessage) {
        this(message, null, userMessage);
    }

    /**
     * @param message
     */
    public AudlangValidationException(String message) {
        this(message, null, null);
    }

    /**
     * @param userMessage
     */
    public AudlangValidationException(AudlangMessage userMessage) {
        super(AudlangMessage.toStringOrGenericError(userMessage));
        this.userMessage = userMessage == null ? AudlangMessage.msg(CommonErrors.ERR_4003_GENERAL_ERROR) : userMessage;
    }

    /**
     * @return further error information, never null
     */
    public AudlangMessage getUserMessage() {
        return userMessage;
    }

}
