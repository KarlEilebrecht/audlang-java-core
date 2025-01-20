//@formatter:off
/*
 * CurbComplexityException
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

package de.calamanari.adl.erl;

import de.calamanari.adl.AudlangErrorInfo;
import de.calamanari.adl.CommonErrors;
import de.calamanari.adl.ConversionException;

/**
 * Exception to be thrown to indicate that a given CURB is too complex to be executed.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@SuppressWarnings("java:S110")
public class CurbComplexityException extends ConversionException {

    private static final long serialVersionUID = 7452769338707227516L;

    /**
     * @param errorInfo
     */
    public CurbComplexityException(AudlangErrorInfo errorInfo) {
        super(errorInfo);
    }

    /**
     * @param message
     * @param errorInfo
     */
    public CurbComplexityException(String message) {
        super(message, AudlangErrorInfo.error(CommonErrors.ERR_4001_COMPLEXITY_ERROR));
    }

}
