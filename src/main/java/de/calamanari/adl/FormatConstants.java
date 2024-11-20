//@formatter:off
/*
 * FormatConstants
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
 * Some constant values used for formatting output
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class FormatConstants {

    public static final String T_STRICT = "STRICT";
    public static final String T_NOT = "NOT";
    public static final String T_ANY = "ANY";
    public static final String T_OF = "OF";
    public static final String T_BETWEEN = "BETWEEN";
    public static final String T_IS = "IS";
    public static final String T_UNKNOWN = "UNKNOWN";
    public static final String T_CONTAINS = "CONTAINS";
    public static final String T_CURB = "CURB";

    public static final String EMPTY_PREFIX = "";
    public static final String STRICT_PREFIX = T_STRICT;

    /**
     * Indent size for output is 4 space characters
     */
    public static final String DEFAULT_INDENT = "    ";

    /**
     * The line break used when formatting output
     */
    public static final String LINE_BREAK = "\n";

    /**
     * We should try splitting comments into multiple lines if they are longer than {@value} characters
     */
    public static final int COMMENT_LINE_THRESHOLD = 50;

    /**
     * Comments longer than {@value} characters should be put on a dedicated line
     */
    public static final int COMPLEX_COMMENT_THRESHOLD = 20;

    public static final String COMMENT_START = "/*";
    public static final String COMMENT_END = "*/";

    private FormatConstants() {
        // constants only
    }

}
