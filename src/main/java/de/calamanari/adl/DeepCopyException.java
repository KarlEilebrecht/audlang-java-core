//@formatter:off
/*
 * DeepCopyException
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

/**
 * Exception indicates that it was not possible to deep-copy an object via serialization
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class DeepCopyException extends RuntimeException {

    private static final long serialVersionUID = -2591331586620781594L;

    /**
     * @param message
     * @param cause
     */
    public DeepCopyException(String message, Throwable cause) {
        super(message, cause);
    }

}
