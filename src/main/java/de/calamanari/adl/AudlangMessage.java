//@formatter:off
/*
 * AudlangMessage
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
import java.util.Collection;

/**
 * An {@link AudlangMessage} is a container with additional information (explanation) about an error or a process result
 * 
 * @param code identifier of the type of the message
 * @param severity see {@link AudlangMessageSeverity}
 * @param userMessage message that can be displayed to any app user (not too technical)
 * @param relatedArgNameLeft optional left argument name of a match
 * @param relatedArgNameRight optional right argument name of a reference match
 * @param relatedValue optional value that caused the problem or is related to this message
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public record AudlangMessage(String code, AudlangMessageSeverity severity, String userMessage, String relatedArgNameLeft, String relatedArgNameRight,
        String relatedValue) implements Serializable {

    /**
     * Creates a message including maximum context information for reference.
     * 
     * @param code identifier of the type of the message
     * @param severity see {@link AudlangMessageSeverity}
     * @param userMessage message that can be displayed to any app user (not too technical)
     * @param relatedArgNameLeft optional left argument name of a match
     * @param relatedArgNameRight optional right argument name of a reference match
     * @param relatedValue optional value that caused the problem or is related to this message
     */
    public AudlangMessage(String code, AudlangMessageSeverity severity, String userMessage, String relatedArgNameLeft, String relatedArgNameRight,
            String relatedValue) {
        this.code = String.valueOf(code);
        this.severity = severity == null ? AudlangMessageSeverity.INFO : severity;
        this.userMessage = String.valueOf(userMessage);
        this.relatedArgNameLeft = relatedArgNameLeft;
        this.relatedArgNameRight = relatedArgNameRight;
        this.relatedValue = relatedValue;
    }

    /**
     * Creates a message including an argument name and a value for reference.
     * 
     * @param message audlang message object to derive code and user message from
     * @param relatedArgNameLeft argument name of a match
     * @param relatedValue value that is related
     * @param args to be appended to the message separated by space (optional to add detail information)
     * @return message object
     */
    public static AudlangMessage argValueMsg(AudlangUserMessage message, String relatedArgNameLeft, String relatedValue, Object... args) {
        return new AudlangMessage(message.code(), message.severity(), message.userMessage(args), relatedArgNameLeft, null, relatedValue);
    }

    /**
     * Creates a message including an argument name for reference.
     * 
     * @param message audlang message object to derive code and user message from
     * @param relatedArgNameLeft argument name of a match
     * @param args to be appended to the message separated by space (optional to add detail information)
     * @return message object
     */
    public static AudlangMessage argMsg(AudlangUserMessage message, String relatedArgNameLeft, Object... args) {
        return new AudlangMessage(message.code(), message.severity(), message.userMessage(args), relatedArgNameLeft, null, null);
    }

    /**
     * Creates a message including both argument names for reference.
     * 
     * @param message audlang message object to derive code and user message from
     * @param offendingArgNameLeft left argument name of a reference match
     * @param offendingArgNameRight right argument name of a reference match
     * @param args to be appended to the message separated by space (optional to add detail information)
     * @return message object
     */
    public static AudlangMessage argRefMsg(AudlangUserMessage message, String offendingArgNameLeft, String offendingArgNameRight, Object... args) {
        return new AudlangMessage(message.code(), message.severity(), message.userMessage(args), offendingArgNameLeft, offendingArgNameRight, null);
    }

    /**
     * Creates a message without additional context information.
     * 
     * @param message audlang message object to derive code and user message from
     * @param args to be appended to the message separated by space (optional to add detail information)
     * @return message object
     */
    public static AudlangMessage msg(AudlangUserMessage message, Object... args) {
        return new AudlangMessage(message.code(), message.severity(), message.userMessage(args), null, null, null);
    }

    /**
     * Picks the first error from the given collection or returns the fallback if the given list was empty or null.
     * 
     * @param messages
     * @param fallback to be returned in case the messages were empty or null
     * @return first error or fallback
     */
    public static AudlangMessage pickFirstError(Collection<AudlangMessage> messages, AudlangMessage fallback) {
        if (messages != null) {
            return messages.stream().filter(m -> m.severity() == AudlangMessageSeverity.ERROR).findFirst().orElse(fallback);
        }
        return fallback;
    }

    /**
     * @param info
     * @return info.toString() or {@link CommonErrors#ERR_4003_GENERAL_ERROR}.toString() if info was null
     */
    public static String toStringOrGenericError(AudlangMessage info) {
        if (info == null) {
            return msg(CommonErrors.ERR_4003_GENERAL_ERROR).toString();
        }
        else {
            return info.toString();
        }
    }

}
