//@formatter:off
/*
 * CommonErrors
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
 * Collection of common errors when processing Audlang expressions
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public enum CommonErrors implements AudlangUserMessage {

    ERR_1000_PARSE_FAILED("The expression could not be parsed (syntax error)."),

    ERR_1001_ALWAYS_TRUE("Expression yields always true."),

    ERR_1002_ALWAYS_FALSE("Expression yields always false."),

    ERR_2003_VALUE_RANGE("Value out of expected range."),

    ERR_2004_VALUE_FORMAT("Value format mismatch."),

    ERR_2005_VALUE_FORMAT_BOOL("Value boolean format mismatch."),

    ERR_2006_VALUE_FORMAT_DATE("Value date format mismatch."),

    ERR_2100_REFERENCE_MISMATCH("Reference match mismatch."),

    ERR_2101_REFERENCE_MATCH_NOT_SUPPORTED("Reference match not supported."),

    ERR_2200_CONTAINS_NOT_SUPPORTED("Contains not supported."),

    ERR_2201_LTGT_NOT_SUPPORTED("Operators less than (<) and greater than (>) not supported by the underlying system."),

    ERR_3000_MAPPING_FAILED("The expression could not be mapped to the underlying database (technical issue)."),

    ERR_3001_TYPE_MISMATCH("A value could not be mapped to / compared against the underlying database field (type mismatch)."),

    ERR_4001_COMPLEXITY_ERROR("The expression's complexity exceeds system limitations."),

    ERR_4002_CONFIG_ERROR("Configuration error (technical issue)."),

    ERR_4003_GENERAL_ERROR("General technical error.");

    private final String userMessage;

    private final String code;

    private CommonErrors(String userMessage) {
        String name = this.name();
        int pos = name.indexOf('_');
        pos = name.indexOf('_', pos + 1);
        this.code = name.substring(0, pos);
        this.userMessage = code + ": " + userMessage;
    }

    /**
     * @return message that can potentially be displayed to any application user (not too technical)
     */
    @Override
    public String userMessage() {
        return userMessage;
    }

    /**
     * @return unique error code
     */
    @Override
    public String code() {
        return code;
    }

    @Override
    public AudlangMessageSeverity severity() {
        return AudlangMessageSeverity.ERROR;
    }

}
