//@formatter:off
/*
 * OperandConstraint
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

package de.calamanari.adl.irl;

/**
 * An {@link OperandConstraint} restricts the ability of an operator to take a certain operand or set of operands.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public enum OperandConstraint {

    NONE("Operator does not take any operand (unary operation)."),
    ONE_VALUE("Operator expects a value argument, no argument reference allowed."),
    ONE_VALUE_OR_ARG_REF("Operator expects a value or an argument reference.");

    private final String message;

    /**
     * @return the message to be included in reported error
     */
    public String getMessage() {
        return message;
    }

    private OperandConstraint(String message) {
        this.message = message;
    }

}