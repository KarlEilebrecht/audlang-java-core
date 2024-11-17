//@formatter:off
/*
 * ArgMetaInfo
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

import java.io.Serializable;

/**
 * An {@link ArgMetaInfo} contains meta data about an argument of an expression.
 *
 * @param argName name of the argument, mandatory
 * @param type type of the argument, mandatory
 * @param isAlwaysKnown if true, the argument <b>can never be UNKNOWN</b>, see
 *            <a href="https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#38-is-not-unknown">§3.8 Audlang
 *            Spec</a>
 * @param isCollection if true, the argument is a <b>value collection</b>, see <a href=
 *            "https://github.com/KarlEilebrecht/audlang-spec/blob/main/doc/AudienceDefinitionLanguageSpecification.md#7-audlang-and-collection-attributes">§7
 *            Audlang Spec</a>
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public record ArgMetaInfo(String argName, AdlType type, boolean isAlwaysKnown, boolean isCollection) implements Serializable {

    public ArgMetaInfo {
        if (argName == null || argName.isEmpty() || type == null) {
            throw new ConfigException(String.format("Arguments argName and type are mandatory, given: argName=%s, type=%s, isAlwaysKnown=%s, isCollection=%s",
                    argName, type, isAlwaysKnown, isCollection));
        }
    }
}
