//@formatter:off
/*
 * QualifiedArgValue
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

import java.io.Serializable;

/**
 * Single value qualified by its argument name, suitable to be key or value.
 * <p>
 * While the argument name must not be null, the value can be null, aka "IS UNKNOWN".
 * 
 * @param argName argument name, not null
 * @param argValue
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public record QualifiedArgValue(String argName, String argValue) implements Comparable<QualifiedArgValue>, Serializable {

    /**
     * @param argName argument name, not null
     * @param argValue
     */
    public QualifiedArgValue {
        if (argName == null) {
            throw new IllegalArgumentException("argName must not be null, given: argName=null, argValue=" + argValue);
        }
    }

    @Override
    public int compareTo(QualifiedArgValue other) {
        int res = this.argName.compareTo(other.argName);
        if (res == 0 && (this.argValue != null || other.argValue != null)) {
            if (this.argValue == null) {
                res = -1;
            }
            else if (other.argValue == null) {
                res = 1;
            }
            else {
                res = this.argValue.compareTo(other.argValue);
            }
        }
        return res;
    }
}
