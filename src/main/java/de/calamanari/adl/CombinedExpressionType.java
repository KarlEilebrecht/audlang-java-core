//@formatter:off
/*
 * CombinedExpressionType
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
 * The two logical ways to combine two or more expressions
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public enum CombinedExpressionType {
    AND, OR;

    /**
     * This method tries to align the connector (AND/OR) with the current indentation (cosmetics)
     * 
     * @param combiType
     * @param style
     * @param level
     * @return connector string
     */
    public static String createOperatorString(CombinedExpressionType combiType, FormatStyle style, int level) {
        String indent = style.getIndent();
        if (style.isMultiLine() && level > 0 && indent.length() > combiType.name().length()) {
            return (indent + combiType.name()).substring(combiType.name().length() + 1);
        }
        else {
            return combiType.name();
        }
    }

    public CombinedExpressionType switchType() {
        return this == AND ? OR : AND;
    }
}
