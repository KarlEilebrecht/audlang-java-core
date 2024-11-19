//@formatter:off
/*
 * DefaultArgMetaInfoLookup
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A {@link DefaultArgMetaInfoLookup} is a configuration-based implementation of an {@link ArgMetaInfoLookup}.
 * <p>
 * Instances are deeply immutable.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public record DefaultArgMetaInfoLookup(Map<String, ArgMetaInfo> map) implements ArgMetaInfoLookup {

    /**
     * Starts a fluent process to create a lookup step by step
     * 
     * @param argName first argument name
     * @return builder
     */
    public static BuilderStepType withArg(String argName) {
        return new Builder(argName);
    }

    /**
     * Creates a lookup from the given map
     * 
     * @param map
     * @throws ConfigException if the mapping contained nulls
     */
    public DefaultArgMetaInfoLookup(Map<String, ArgMetaInfo> map) {
        for (Map.Entry<String, ArgMetaInfo> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new ConfigException(
                        String.format("Neither map keys nor values can be null, given: key(argName)=%s, value=%s", entry.getKey(), entry.getValue()));
            }
            if (!entry.getKey().equals(entry.getValue().argName())) {
                throw new ConfigException(String.format("Inconsistent mapping, given: key(argName)=%s, value=%s", entry.getKey(), entry.getValue()));
            }

        }
        this.map = Collections.unmodifiableMap(new TreeMap<>(map));
    }

    /**
     * Creates a lookup from the given list of unique entries
     * 
     * @param entries
     * @throws ConfigException if any entry is null or a duplicate
     */
    public DefaultArgMetaInfoLookup(List<ArgMetaInfo> entries) {
        this(createEntryMap(entries));
    }

    /**
     * Maps the argNames to the entries
     * 
     * @param entries
     * @return map for the given entries
     */
    private static Map<String, ArgMetaInfo> createEntryMap(List<ArgMetaInfo> entries) {

        Map<String, ArgMetaInfo> tempMap = HashMap.newHashMap(entries.size());
        for (ArgMetaInfo entry : entries) {
            if (entry == null) {
                throw new ConfigException(String.format("Null entry detected, given: %s", entries));
            }
            ArgMetaInfo prevEntry = tempMap.putIfAbsent(entry.argName(), entry);
            if (prevEntry != null) {
                throw new ConfigException(String.format("Duplicate entry detected for argName=%s, given: %s vs. %s", entry.argName(), entry, prevEntry));
            }
        }
        return tempMap;
    }

    /**
     * Returns the meta information for the given argName
     * 
     * @param argName
     * @return meta info
     * @throws IllegalArgumentException if argName is null
     * @throws LookupException if there is no meta data available for the given argName
     */
    @Override
    public ArgMetaInfo lookup(String argName) {
        if (argName == null) {
            throw new IllegalArgumentException("Parameter argName must not be null.");
        }

        ArgMetaInfo res = map.get(argName);
        if (res == null) {
            throw new LookupException("No meta data available for argName=" + argName);
        }
        return res;

    }

    @Override
    public boolean contains(String argName) {
        if (argName == null) {
            throw new IllegalArgumentException("Parameter argName must not be null.");
        }
        return map.containsKey(argName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append(" {");
        if (!map.isEmpty()) {
            sb.append("\n");
        }
        for (Map.Entry<String, ArgMetaInfo> entry : map.entrySet()) {
            sb.append("    ");
            sb.append(entry.getKey());
            sb.append(" -> (");
            sb.append(entry.getValue().type());
            sb.append(", isAlwaysKnown=");
            sb.append(entry.getValue().isAlwaysKnown());
            sb.append(", isCollection=");
            sb.append(entry.getValue().isCollection());
            sb.append(")\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public interface BuilderFinal {

        /**
         * @return the lookup
         * @throws ConfigException if the configuration was incorrect
         */
        public DefaultArgMetaInfoLookup get();

    }

    public interface BuilderStepAddOrGet extends BuilderFinal {

        /**
         * Add another argument to be configured
         * 
         * @param argName mandatory
         * @return builder
         */
        public BuilderStepType withArg(String argName);

    }

    public interface BuilderStepType {

        /**
         * Sets the type of the argument
         * 
         * @param type mandatory
         * @return builder
         */
        public BuilderStepConfigOrGet ofType(AdlType type);

    }

    public interface BuilderStepConfigIsAlwaysKnownOrGet extends BuilderFinal {

        /**
         * Configures the argument to be always known (option)
         * 
         * @return builder
         */
        public BuilderStepConfigOrGet thatIsAlwaysKnown();

    }

    public interface BuilderStepConfigIsCollectionOrGet extends BuilderFinal {

        /**
         * Configures the argument to be a collection (option)
         * 
         * @return builder
         */
        public BuilderStepConfigOrGet thatIsCollection();

    }

    /**
     * Choice interface
     */
    public interface BuilderStepConfigOrGet extends BuilderStepAddOrGet, BuilderStepConfigIsAlwaysKnownOrGet, BuilderStepConfigIsCollectionOrGet {
        // composite interface
    }

    /**
     * Fluent builder implementation
     * 
     * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
     */
    private static class Builder implements BuilderStepAddOrGet, BuilderStepType, BuilderStepConfigOrGet {

        private final Map<String, ArgMetaInfo> tempMap = new HashMap<>();

        private String nextArgName = null;
        private AdlType nextType = null;
        private boolean nextIsAlwaysKnown = false;
        private boolean nextIsCollection = false;

        private Builder(String firstArgName) {
            this.nextArgName = firstArgName;
        }

        @Override
        public BuilderStepConfigOrGet thatIsAlwaysKnown() {
            this.nextIsAlwaysKnown = true;
            return this;
        }

        @Override
        public BuilderStepConfigOrGet thatIsCollection() {
            this.nextIsCollection = true;
            return this;
        }

        @Override
        public BuilderStepConfigOrGet ofType(AdlType type) {
            this.nextType = type;
            return this;
        }

        @Override
        public BuilderStepType withArg(String argName) {
            addNextEntry();
            this.nextArgName = argName;
            return this;
        }

        @Override
        public DefaultArgMetaInfoLookup get() {
            addNextEntry();
            return new DefaultArgMetaInfoLookup(this.tempMap);
        }

        /**
         * Takes the collected values and updated the temp map
         */
        private void addNextEntry() {
            ArgMetaInfo prevEntry = this.tempMap.putIfAbsent(this.nextArgName, new ArgMetaInfo(nextArgName, nextType, nextIsAlwaysKnown, nextIsCollection));
            if (prevEntry != null) {
                throw new ConfigException(String.format("Attempt to re-map argName=%s, type=%s, isAlwaysKnown=%s, isCollection=%s, previous entry: %s",
                        this.nextArgName, this.nextType, this.nextIsAlwaysKnown, this.nextIsCollection, prevEntry));
            }
            this.nextArgName = null;
            this.nextType = null;
            this.nextIsAlwaysKnown = false;
            this.nextIsCollection = false;
        }

    }

}
