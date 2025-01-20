//@formatter:off
/*
 * DefaultConversionContext
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

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link DefaultConversionContext} provides an array list of the specified type to deal with the members parsed from a particular expressen level.
 * 
 * @param members the members of this context level
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public record DefaultConversionContext<R>(List<R> members) implements ConversionContext {

    /**
     * @param members (null means empty)
     */
    public DefaultConversionContext(List<R> members) {
        this.members = members == null ? new ArrayList<>() : members;
    }

    /**
     * Creates a new context object with an empty mutable list
     */
    public DefaultConversionContext() {
        this(new ArrayList<>());
    }

    @Override
    public void clear() {
        members.clear();
    }

}
