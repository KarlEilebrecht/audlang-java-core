//@formatter:off
/*
 * AudlangFieldCollector
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

import java.util.List;

/**
 * Interface to be implemented by expressions aware of the {@link AudlangField}s they contain.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface AudlangFieldAware {

    /**
     * Implementations must comply to the following contract:
     * <ul>
     * <li>The returned list contains all fields of this expression and all its children (recursively).</li>
     * <li>The returned list contains fields appearing as argNames or as argRefs in any match.</li>
     * <li>The returned list is free of duplicates, each argName only appears once.</li>
     * <li>The returned list ordered by {@link AudlangField#argName()}.</li>
     * <li>The returned list should be unmodifiable.</li>
     * <li>All fields are <i>condensed</i>, see {@link AudlangField#condenseFieldData()}</li>
     * </ul>
     * 
     * @return list of all fields
     */
    List<AudlangField> allFields();

    /**
     * @return unmodifiable sorted list of all argNames found in an expression recursively
     */
    default List<String> allArgNames() {
        return allFields().stream().map(AudlangField::argName).toList();
    }

}
