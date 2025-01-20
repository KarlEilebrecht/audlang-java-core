//@formatter:off
/*
 * LookupException
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

/**
 * Exception to indicate a failed lookup operation, typically related to missing meta data (configuration problem).
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@SuppressWarnings("java:S110")
public class LookupException extends ConfigException {

    private static final long serialVersionUID = -8599486010851792487L;

    /**
     * @param message
     * @param errorInfo
     */
    public LookupException(String message, AudlangErrorInfo errorInfo) {
        super(message, errorInfo);
    }

    /**
     * @param message
     * @param cause
     * @param errorInfo
     */
    public LookupException(String message, Throwable cause, AudlangErrorInfo errorInfo) {
        super(message, cause, errorInfo);
    }

    /**
     * @param message
     * @param cause
     */
    public LookupException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public LookupException(String message) {
        super(message);
    }

}
