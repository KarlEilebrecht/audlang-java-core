//@formatter:off
/*
 * AudlangMessageSeverity
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

/**
 * Severity for messages reported back from a process that processes an Audlang expression
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public enum AudlangMessageSeverity {

    /**
     * To be used for plain information
     */
    INFO,

    /**
     * To be used for any message that indicates a potential problem or critical consequence.
     */
    WARNING,

    /**
     * To be used <b>only</b> in conjunction with error responses / failed processing.
     */
    ERROR;

}
