//@formatter:off
/*
 * ArgNameValueMapping
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@link ArgNameValueMapping} is a container with the mappings of argument names to target argument names resp. values to target values within the realm of
 * their argument. E.g., this can be used to map between labels and technical IDs.
 * 
 * @param mappings from qualified value to another (mapping between taxonomies)
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public record ArgNameValueMapping(Map<QualifiedArgValue, QualifiedArgValue> mappings) implements Serializable {

    /**
     * Returns a builder to create a mapping step-by-step
     * 
     * @return builder instance
     */
    public static Builder create() {
        return new Builder();
    }

    /**
     * @param mappings from qualified value to another (mapping between taxonomies)
     * @throws IllegalArgumentException if any key or value of the given map was null
     */
    public ArgNameValueMapping(Map<QualifiedArgValue, QualifiedArgValue> mappings) {
        Map<QualifiedArgValue, QualifiedArgValue> tempMap = new TreeMap<>();

        for (Map.Entry<QualifiedArgValue, QualifiedArgValue> entry : mappings.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalArgumentException(
                        String.format("Neither key nor value of the map can be null, given: key=%s, value=%s", entry.getKey(), entry.getValue()));
            }
            tempMap.put(entry.getKey(), entry.getValue());
        }
        addMissingIsUnknowns(tempMap);
        this.mappings = Collections.unmodifiableMap(tempMap);
    }

    /**
     * Fixes a typical (annoying) problem when setting up mappings, the forgotten IS-UNKNOWNs (null values for arguments).
     * <p>
     * For each argument where we have any mapping but no null-value mapping, we add a mapping based on the first mapping we find.
     * <p>
     * Example: if color.!null! is not mapped but color.blue is mapped to arg1234.blue then we <i>implicitly</i> map color.!null! to arg1234.!null! to properly
     * cover IS UNKNOWN and reference matches.
     * 
     * @param tempMap
     */
    private static void addMissingIsUnknowns(Map<QualifiedArgValue, QualifiedArgValue> tempMap) {
        Map<String, String> argNameToArgNameMap = new HashMap<>();
        for (Map.Entry<QualifiedArgValue, QualifiedArgValue> entry : tempMap.entrySet()) {
            argNameToArgNameMap.putIfAbsent(entry.getKey().argName(), entry.getValue().argName());
        }

        for (Map.Entry<String, String> entry : argNameToArgNameMap.entrySet()) {
            QualifiedArgValue key = new QualifiedArgValue(entry.getKey(), null);
            tempMap.computeIfAbsent(key, k -> new QualifiedArgValue(entry.getValue(), null));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ArgNameValueMapping {");

        for (Map.Entry<QualifiedArgValue, QualifiedArgValue> entry : mappings.entrySet()) {
            sb.append("\n");
            sb.append("    (");
            sb.append(entry.getKey().argName());
            sb.append(", ");
            sb.append(entry.getKey().argValue());
            sb.append(") -> ");
            sb.append("(");
            sb.append(entry.getValue().argName());
            sb.append(", ");
            sb.append(entry.getValue().argValue());
            sb.append(")");
        }

        if (!mappings.isEmpty()) {
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();

    }

    /**
     * @return reverse mapping
     * @throws AmbiguousMappingException if it was impossible to create a reverse mapping due to duplicate mappings to the same target value in this instance
     */
    public ArgNameValueMapping reverse() {
        Map<QualifiedArgValue, QualifiedArgValue> reverseMappings = new HashMap<>();
        for (Map.Entry<QualifiedArgValue, QualifiedArgValue> entry : mappings.entrySet()) {
            QualifiedArgValue key = entry.getValue();
            QualifiedArgValue value = entry.getKey();
            QualifiedArgValue prevValue = reverseMappings.putIfAbsent(key, value);
            if (prevValue != null) {
                throw new AmbiguousMappingException(
                        String.format("Unable to create reverse mapping because at least two source keys map to the same value: %s -> %s, %s -> %s %n%s", value,
                                key, prevValue, key, this));
            }
        }
        return new ArgNameValueMapping(reverseMappings);
    }

    /**
     * Builder for convenient (test) setups
     */
    public static class Builder {

        private final Map<QualifiedArgValue, QualifiedArgValue> map = new HashMap<>();

        private Builder() {
            // all instances created privately
        }

        /**
         * Adds a single source to destination mapping entry
         * 
         * @param argNameFrom not null
         * @param argValueFrom null means no value
         * @param argNameTo not null
         * @param argValueTo null means no value
         * @return this builder
         * @throws AmbiguousMappingException if the given name/value pair was already mapped previously
         */
        public Builder withMapping(String argNameFrom, String argValueFrom, String argNameTo, String argValueTo) {

            QualifiedArgValue key = new QualifiedArgValue(argNameFrom, argValueFrom);
            QualifiedArgValue value = new QualifiedArgValue(argNameTo, argValueTo);

            QualifiedArgValue prevValue = map.putIfAbsent(key, value);
            if (prevValue != null && !prevValue.equals(value)) {
                throw new AmbiguousMappingException(String.format("Duplicate mapping of %s: %s <> %s", key, value, prevValue));
            }
            return this;
        }

        /**
         * This method allows mapping a set of values to target values, implicitly maps null to null
         * 
         * @param argNameFrom
         * @param argNameTo
         * @param valueMappings neither keys nor values can be null
         * @return this builder
         * @throws AmbiguousMappingException if the given name/value pair was already mapped previously
         * @throws IllegalArgumentException if any of the keys or values in the given value map was null
         */
        public Builder withMappings(String argNameFrom, String argNameTo, Map<String, String> valueMappings) {
            for (Map.Entry<String, String> entry : valueMappings.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    throw new IllegalArgumentException(
                            String.format("Neither key nor value of the valueMappings can be null, given: key=%s, value=%s", entry.getKey(), entry.getValue()));
                }
                withMapping(argNameFrom, entry.getKey(), argNameTo, entry.getValue());
            }
            return this;
        }

        /**
         * @return new instance of {@link ArgNameValueMapping}
         */
        public ArgNameValueMapping get() {
            return new ArgNameValueMapping(map);
        }

    }

}
