//@formatter:off
/*
 * AudlangField
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 * An {@link AudlangField} reflects an argument name with all the values and argument name references collected from an Audlang expression.
 * <p>
 * <ul>
 * <li>An {@link AudlangField} is immutable.</li>
 * <li>Lists are immutable copies of the source lists.</li>
 * <li>{@link #values()} and {@link #refArgNames()} are free of duplicates and ordered, independent from the given source lists.</li>
 * </ul>
 * 
 * @param argName mandatory name of the argument
 * @param values list of values collected for this argument, null is same as empty list
 * @param refArgNames list of referenced argument names collected for this argument, null is same as empty list
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public record AudlangField(String argName, List<String> values, List<String> refArgNames) implements Serializable {

    /**
     * Use this method to build a field with all its details step by step
     * 
     * @param argName
     * @return builder
     */
    public static final Builder forField(String argName) {
        return new Builder(argName);
    }

    /**
     * @param argName mandatory name of the argument
     * @param values list of values collected for this argument, null is same as empty list
     * @param refArgNames list of referenced argument names collected for this argument, null is same as empty list
     */
    public AudlangField(String argName, List<String> values, List<String> refArgNames) {
        if (argName == null) {
            throw new AudlangValidationException(String.format("argName must not be null, given: values=%s, refArgNames=%s", values, refArgNames));
        }
        if (values != null && values.stream().anyMatch(Objects::isNull)) {
            throw new AudlangValidationException(
                    String.format("Argument values must not be null, given: argName=%s, values=%s, refArgNames=%s", argName, values, refArgNames));
        }
        if (refArgNames != null && refArgNames.stream().anyMatch(Objects::isNull)) {
            throw new AudlangValidationException(
                    String.format("Referenced argument names must not be null, given: argName=%s, values=%s, refArgNames=%s", argName, values, refArgNames));
        }

        this.argName = argName;
        this.values = condenseList(values);
        this.refArgNames = condenseList(refArgNames);
    }

    /**
     * @param values null will be handled like empty list
     * @return new unmodifiable list, ordered and all duplicates removed
     */
    private static List<String> condenseList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        else if (values.size() == 1) {
            return Collections.unmodifiableList(Arrays.asList(values.get(0)));
        }
        else {
            return Collections.unmodifiableList(new ArrayList<>(new TreeSet<>(values)));
        }
    }

    /**
     * Supplementary class for building an immutable record step by step.
     */
    public static class Builder {

        private final String argName;
        private final List<String> values = new ArrayList<>();
        private final List<String> refArgNames = new ArrayList<>();

        private Builder(String argName) {
            this.argName = argName;
        }

        public void addValue(String value) {
            values.add(value);
        }

        public void addRefArgName(String refArgName) {
            this.refArgNames.add(refArgName);
        }

        /**
         * @return immutable {@link AudlangField}
         */
        public AudlangField get() {
            return new AudlangField(argName, values, refArgNames);
        }

    }
}
