//@formatter:off
/*
 * GenericStringTypeCaster
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
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

/**
 * The {@link GenericTypeCaster} is meant for the special scenario where the logical data model uses a couple of types but the underlying storage layer only
 * deals with a generic type (usually text). In this case certain operators (e.g., comparisons less-than / greater-than) would not work as expected.
 * <p>
 * <i>Pre-casting</i> the values (fields) as part of the native query allows on-the-fly conversion to the corresponding types, so type-specific operations work
 * as expected. A {@link GenericTypeCaster} has a an internal map specifying a native cast per {@link AdlType} of the logical data model. That also means that
 * there can be configured only a single (unique) cast function <i>per {@link AdlType}</i> of the logical data model.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class GenericTypeCaster implements NativeTypeCaster {

    private static final long serialVersionUID = -7682833230457141934L;

    /**
     * This map contains the functions that create for a native field of type string a native expression to convert it into the expected target type.
     */
    private final Map<String, SerializableUnaryStringOperator> castFunctionMap;

    /**
     * Fluent API to setup a caster step-by-step
     * 
     * @param type the destination type the native castFunction produces based on the data field
     * @param castFunction lambda to surround a data field in a native query with a native conversion instruction to match the required type
     * @return builder
     */
    public static Builder withNativeCast(AdlType type, SerializableUnaryStringOperator castFunction) {
        return new Builder(type, castFunction);
    }

    /**
     * Creates a caster based on the given mappings (unmodifiable)
     * 
     * @param castFunctionMap
     */
    public GenericTypeCaster(Map<String, SerializableUnaryStringOperator> castFunctionMap) {

        Map<String, SerializableUnaryStringOperator> tempMap = new TreeMap<>();
        for (Map.Entry<String, SerializableUnaryStringOperator> entry : castFunctionMap.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new ConfigException("Neither a key (AdlType name) nor the mapping function can be null, given: " + castFunctionMap);
            }
            tempMap.put(entry.getKey(), entry.getValue());
        }
        this.castFunctionMap = Collections.unmodifiableMap(tempMap);
    }

    @Override
    public String formatNativeTypeCast(String argName, String nativeFieldName, AdlType argType, AdlType requestedArgType) {
        String res = nativeFieldName;
        UnaryOperator<String> castFunction = castFunctionMap.get(requestedArgType.name());
        if (castFunction != null) {
            res = castFunction.apply(nativeFieldName);
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName() + " {");
        if (this.castFunctionMap.size() > 0) {
            sb.append("\n");
            int idx = 0;
            for (String key : castFunctionMap.keySet()) {
                sb.append("    ");
                sb.append(key);
                sb.append(" -> { \u03bb }");
                idx++;
                if (idx < castFunctionMap.size()) {
                    sb.append(",");
                }
                sb.append("\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Fluent API for convenient setups of type casters
     */
    public static class Builder {

        Map<String, SerializableUnaryStringOperator> tempMap = new HashMap<>();

        private Builder(AdlType type, SerializableUnaryStringOperator castFunction) {
            tempMap.put(type.name(), castFunction);
        }

        /**
         * Adds another lambda for another type
         * 
         * @param type the destination type the native castFunction produces based on the data field
         * @param castFunction lambda to surround a data field in a native query with a native conversion instruction to match the required type
         * @return builder
         */
        public Builder withNativeCast(AdlType type, SerializableUnaryStringOperator castFunction) {
            tempMap.put(type.name(), castFunction);
            return this;
        }

        /**
         * @return the configured native type caster
         */
        public GenericTypeCaster get() {
            return new GenericTypeCaster(tempMap);
        }

    }
}
