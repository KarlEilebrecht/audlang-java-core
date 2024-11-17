//@formatter:off
/*
 * FormatStyle
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
 * Output styles when printing an expression as a string
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public enum FormatStyle {

    /**
     * output as a single line
     */
    INLINE(false, ""),

    /**
     * Multi-line-output with 4-space-indentation
     */
    PRETTY_PRINT(true, FormatConstants.DEFAULT_INDENT);

    private final String indent;

    private final boolean multiLine;

    private FormatStyle(boolean multiLine, String indent) {
        this.indent = indent;
        this.multiLine = multiLine;
    }

    /**
     * @return indentation, not null
     */
    public String getIndent() {
        return indent;
    }

    /**
     * @return true, if this is a multi-line format
     */
    public boolean isMultiLine() {
        return multiLine;
    }

}
