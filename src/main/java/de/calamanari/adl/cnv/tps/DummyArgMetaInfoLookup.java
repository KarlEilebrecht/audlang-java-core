//@formatter:off
/*
 * DummyArgMetaInfoLookup
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
 * The {@link DummyArgMetaInfoLookup} returns a default {@link DefaultAdlType#STRING}-meta-info for any given argument.
 * <p>
 * This is primarily meant for testing and debugging.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
@SuppressWarnings("java:S6548")
public class DummyArgMetaInfoLookup implements ArgMetaInfoLookup {

    private static final long serialVersionUID = 68320810726250005L;

    private static final DummyArgMetaInfoLookup INSTANCE = new DummyArgMetaInfoLookup();

    public static DummyArgMetaInfoLookup getInstance() {
        return INSTANCE;
    }

    private DummyArgMetaInfoLookup() {
        // singleton
    }

    @Override
    public ArgMetaInfo lookup(String argName) {
        assertArgNameNotNull(argName);
        return new ArgMetaInfo(argName, DefaultAdlType.STRING, false, false);
    }

    @Override
    public boolean contains(String argName) {
        assertArgNameNotNull(argName);
        return true;
    }

    @Override
    public AdlType typeOf(String argName) {
        assertArgNameNotNull(argName);
        return DefaultAdlType.STRING;
    }

    @Override
    @SuppressWarnings("java:S4144")
    public boolean isAlwaysKnown(String argName) {
        assertArgNameNotNull(argName);
        return false;
    }

    @Override
    @SuppressWarnings("java:S4144")
    public boolean isCollection(String argName) {
        assertArgNameNotNull(argName);
        return false;
    }

    /**
     * For consistent behavior we through an exception if the argName is null
     * 
     * @param argName
     * @throws IllegalArgumentException if argName was null
     */
    private void assertArgNameNotNull(String argName) {
        if (argName == null) {
            throw new IllegalArgumentException("Parameter argName must not be null.");
        }
    }

    /**
     * @return singleton instance in JVM
     */
    Object readResolve() {
        return INSTANCE;
    }

}
