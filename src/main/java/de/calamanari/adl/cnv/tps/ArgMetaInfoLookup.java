//@formatter:off
/*
 * ArgMetaInfoLookup
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

import java.io.Serializable;

/**
 * An {@link ArgMetaInfoLookup} contains the metadata about the arguments related to the current conversion run.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface ArgMetaInfoLookup extends Serializable {

    /**
     * Returns the meta information for the given argName
     * 
     * @param argName
     * @return meta info
     * @throws IllegalArgumentException if argName is null
     * @throws LookupException if there is no meta data available for the given argName
     */
    ArgMetaInfo lookup(String argName);

    /**
     * Returns the meta information for the given argName
     * 
     * @param argName
     * @param defaultInfo default information to be returned in case of a failed lookup
     * @return meta info, either from the lookup or (if not found) <i>based on</i> the given default (matching argName)
     * @throws IllegalArgumentException if argName is null
     * @throws LookupException if there is no meta data available for the given argName
     */
    default ArgMetaInfo lookup(String argName, ArgMetaInfo defaultInfo) {
        if (this.contains(argName)) {
            return this.lookup(argName);
        }
        else if (defaultInfo == null || defaultInfo.argName().equals(argName)) {
            return defaultInfo;
        }
        else {
            return new ArgMetaInfo(argName, defaultInfo.type(), defaultInfo.isAlwaysKnown(), defaultInfo.isCollection());
        }
    }

    /**
     * @param argName
     * @return true if this lookup contains the given argument
     */
    boolean contains(String argName);

    /**
     * Shorthand for a lookup and get the type
     * 
     * @param argName
     * @return type of the argName
     */
    default AdlType typeOf(String argName) {
        return lookup(argName).type();
    }

    /**
     * Shorthand for a lookup and get the flag information
     * 
     * @param argName
     * @return true if the given argument is always known (can never be UNKNOWN)
     */
    default boolean isAlwaysKnown(String argName) {
        return lookup(argName).isAlwaysKnown();
    }

    /**
     * Shorthand for a lookup and get the flag information
     * 
     * @param argName
     * @return true if the given argument is a collection attribute
     */
    default boolean isCollection(String argName) {
        return lookup(argName).isCollection();
    }

}
