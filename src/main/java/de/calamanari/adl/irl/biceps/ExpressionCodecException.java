//@formatter:off
/*
 * ExpressionCodecException
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

package de.calamanari.adl.irl.biceps;

import de.calamanari.adl.AdlException;

/**
 * Exception to be thrown whenever anything goes wrong with the encoding or decoding of expressions.
 * <p/>
 * Usually, this king of error shows any implementation problem (bug).
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class ExpressionCodecException extends AdlException {

    private static final long serialVersionUID = 8404641540331694685L;

    /**
     * @param message
     */
    public ExpressionCodecException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ExpressionCodecException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ExpressionCodecException(String message, Throwable cause) {
        super(message, cause);
    }

}
