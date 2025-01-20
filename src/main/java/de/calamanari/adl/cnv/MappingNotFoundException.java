//@formatter:off
/*
 * MappingNotFoundException
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

import de.calamanari.adl.AudlangErrorInfo;
import de.calamanari.adl.CommonErrors;
import de.calamanari.adl.ConversionException;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@SuppressWarnings("java:S110")
public class MappingNotFoundException extends ConversionException {

    private static final long serialVersionUID = -7608044615020775518L;

    /**
     * @param message
     * @param cause
     * @param errorInfo
     */
    public MappingNotFoundException(String message, Throwable cause, AudlangErrorInfo errorInfo) {
        super(message, cause, errorInfo);
    }

    /**
     * @param message
     * @param cause
     */
    public MappingNotFoundException(String message, Throwable cause) {
        super(message, cause, AudlangErrorInfo.error(CommonErrors.ERR_3000_MAPPING_FAILED));
    }

    /**
     * @param message
     * @param errorInfo
     */
    public MappingNotFoundException(String message, AudlangErrorInfo errorInfo) {
        super(message, errorInfo);
    }

    /**
     * @param message
     */
    public MappingNotFoundException(String message) {
        super(message, AudlangErrorInfo.error(CommonErrors.ERR_3000_MAPPING_FAILED));
    }

}
