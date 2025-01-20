//@formatter:off
/*
 * ProcessContext
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A concrete {@link ProcessContext} provides access to the settings of the currently running conversion process.
 * <p>
 * All instances provide variables and flags which can be added or modified by any component/step of the process, so all steps can access a shared state.
 * <p>
 * By default a process context is <b>not</b> safe to be accessed by multiple threads concurrently.
 * <p>
 * Typically, process context objects are not serializable because these instances can carry complex state internally. This interface may just provide a
 * <i>restricted view</i> on the underlying object that maintains the state of the process.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface ProcessContext {

    /**
     * @return global variables of the current run (mutable)
     */
    Map<String, Serializable> getGlobalVariables();

    /**
     * @return flags of the current run (mutable)
     */
    Set<Flag> getGlobalFlags();

    /**
     * @return new instance of empty settings
     */
    public static ProcessContext empty() {
        return new ProcessContext() {

            private final Map<String, Serializable> globalVariables = new HashMap<>();

            private final Set<Flag> globalFlags = new HashSet<>();

            @Override
            public Map<String, Serializable> getGlobalVariables() {
                return globalVariables;
            }

            @Override
            public Set<Flag> getGlobalFlags() {
                return globalFlags;
            }
        };
    }

}
