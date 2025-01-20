//@formatter:off
/*
 * Flag
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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

/**
 * A flag is any label with a meaning, like hints or directives that may influence a process.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface Flag extends Serializable {

    /**
     * @param flags (null is gracefully handled as no flags)
     * @return true if this flag is set (present in the given collection)
     */
    default boolean check(Collection<? extends Flag> flags) {
        return flags != null && flags.contains(this);
    }

    /**
     * @param flags (null is gracefully handled as no flags)
     * @return true if this flag is set (present in the given collection)
     */
    default boolean check(Flag... flags) {
        return flags != null && Arrays.stream(flags).anyMatch(this::equals);
    }

}
