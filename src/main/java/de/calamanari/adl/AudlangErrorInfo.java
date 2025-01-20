//@formatter:off
/*
 * AudlangErrorInfo
 * Copyright 2025 Karl Eilebrecht
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

import java.io.Serializable;

/**
 * An {@link AudlangErrorInfo} is a container with additional information (explanation) about an error, to be attached to an exception.
 * 
 * @param code error identifier
 * @param userMessage message that can be displayed to any app user (not too technical)
 * @param offendingArgNameLeft optional left argument name of a match
 * @param offendingArgNameRight optional right argument name of a reference match
 * @param offendingValue optional offending value that caused the problem
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public record AudlangErrorInfo(String code, String userMessage, String offendingArgNameLeft, String offendingArgNameRight, String offendingValue)
        implements Serializable {

    /**
     * @param code error identifier
     * @param userMessage message that can be displayed to any app user (not too technical)
     * @param offendingArgNameLeft optional left argument name of a match
     * @param offendingArgNameRight optional right argument name of a reference match
     * @param offendingValue optional offending value that caused the problem
     */
    public AudlangErrorInfo(String code, String userMessage, String offendingArgNameLeft, String offendingArgNameRight, String offendingValue) {
        this.code = String.valueOf(code);
        this.userMessage = String.valueOf(userMessage);
        this.offendingArgNameLeft = offendingArgNameLeft;
        this.offendingArgNameRight = offendingArgNameRight;
        this.offendingValue = offendingValue;
    }

    /**
     * @param message audlang message object to derive code and user message from
     * @param offendingArgNameLeft argument name of a match
     * @param offendingValue value that may have caused the problem
     * @param args to be appended to the message separated by space (optional to add detail information)
     * @return error info
     */
    public static AudlangErrorInfo argValueError(AudlangErrorMessage message, String offendingArgNameLeft, String offendingValue, Object... args) {
        return new AudlangErrorInfo(message.code(), message.userMessage(args), offendingArgNameLeft, null, offendingValue);
    }

    /**
     * @param message audlang message object to derive code and user message from
     * @param offendingArgNameLeft argument name of a match
     * @param args to be appended to the message separated by space (optional to add detail information)
     * @return error info
     */
    public static AudlangErrorInfo argError(AudlangErrorMessage message, String offendingArgNameLeft, Object... args) {
        return new AudlangErrorInfo(message.code(), message.userMessage(args), offendingArgNameLeft, null, null);
    }

    /**
     * @param message audlang message object to derive code and user message from
     * @param offendingArgNameLeft left argument name of a reference match that caused an issue
     * @param offendingArgNameRight right argument name of a reference match that caused an issue
     * @param args to be appended to the message separated by space (optional to add detail information)
     * @return error info
     */
    public static AudlangErrorInfo argRefError(AudlangErrorMessage message, String offendingArgNameLeft, String offendingArgNameRight, Object... args) {
        return new AudlangErrorInfo(message.code(), message.userMessage(args), offendingArgNameLeft, offendingArgNameRight, null);
    }

    /**
     * @param message audlang message object to derive code and user message from
     * @param args to be appended to the message separated by space (optional to add detail information)
     * @return error info
     */
    public static AudlangErrorInfo error(AudlangErrorMessage message, Object... args) {
        return new AudlangErrorInfo(message.code(), message.userMessage(args), null, null, null);
    }

    /**
     * @param info
     * @return info.toString() or {@link CommonErrors#ERR_4003_GENERAL_ERROR}.toString() if info was null
     */
    public static String toStringOrGeneric(AudlangErrorInfo info) {
        if (info == null) {
            return error(CommonErrors.ERR_4003_GENERAL_ERROR).toString();
        }
        else {
            return info.toString();
        }
    }
}
