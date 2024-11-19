//@formatter:off
/*
 * NativeEscaper
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

/**
 * A {@link NativeEscaper} processes a sequence of characters in a way so it becomes compatible to a target system (e.g., SQL).
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface NativeEscaper {

    /**
     * Appends a character after escaping it if required
     * 
     * @param sb
     * @param ch to be appended
     * @throws AdlFormattingException if the character is not allowed, see {@link #isAllowed(char)}
     */
    void append(StringBuilder sb, char ch);

    /**
     * @param ch
     * @return true if the given character must be escaped
     */
    boolean needsEscaping(char ch);

    /**
     * Tells if the given character is supported by this escaper.
     * <p>
     * This method must not return <b>false</b> if {@link #needsEscaping(char)} returns <b>true</b>.
     * 
     * @param ch
     * @return true if the given character can be processed by {@link #append(StringBuilder, char)}
     */
    boolean isAllowed(char ch);
}
