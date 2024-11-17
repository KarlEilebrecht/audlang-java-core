//@formatter:off
/*
 * Visit
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
 * This enumeration lists the possible types of visit during a visitor run. The visitor always enters and exits an element. This information is crucial when
 * visiting trees recursively.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public enum Visit {

    /**
     * Start of the visit of an element. The visitor has not visited any children of this item, yet.
     */
    ENTER,

    /**
     * End of the visit of an element. The visitor not visited the element itself and all children.
     */
    EXIT;

}
