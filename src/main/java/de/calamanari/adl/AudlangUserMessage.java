//@formatter:off
/*
 * AudlangUserMessage
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

/**
 * Interface for information messages and errors that can be presented to end users
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface AudlangUserMessage {

    /**
     * @return code of the error
     */
    String code();

    /**
     * @return message that can be displayed to any end user (not too technical)
     */
    String userMessage();

    /**
     * @return severity of the message
     */
    AudlangMessageSeverity severity();

    /**
     * Formats the user message (this message may be displayed to an end user, do not include technical details)
     * 
     * @param args to be appended separated by space (optional to add detail information)
     * @return formatted user message
     */
    default String userMessage(Object... args) {

        if (args == null || args.length == 0) {
            return this.userMessage();
        }
        else {
            StringBuilder sb = new StringBuilder(this.userMessage());
            for (int i = 0; i < args.length; i++) {
                sb.append(" ");
                sb.append("%s");
            }
            return String.format(sb.toString(), args);
        }

    }

}
