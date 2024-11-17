//@formatter:off
/*
 * DummyArgNameValueMapper
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

/**
 * A {@link DummyArgNameValueMapper} returns the exact same names and values (no change).
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@SuppressWarnings("java:S6548")
public class DummyArgNameValueMapper implements ArgNameValueMapper {

    private static final long serialVersionUID = 6214742661274027246L;
    private static final DummyArgNameValueMapper INSTANCE = new DummyArgNameValueMapper();

    private DummyArgNameValueMapper() {
        // singleton
    }

    public static DummyArgNameValueMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public QualifiedArgValue mapArgValue(String argName, String value) {
        return new QualifiedArgValue(argName, value);
    }

    @Override
    public ArgNameValueMapper reverse() {
        return this;
    }

    @Override
    public boolean isArgumentStructurePreserving() {
        return true;
    }

    @Override
    public boolean isBijective() {
        return true;
    }

    /**
     * @return singleton instance in JVM
     */
    Object readResolve() {
        return INSTANCE;
    }

}
