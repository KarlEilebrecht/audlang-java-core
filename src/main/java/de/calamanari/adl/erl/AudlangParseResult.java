//@formatter:off
/*
 * AudlangParseResult
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

package de.calamanari.adl.erl;

import de.calamanari.adl.AudlangResult;

/**
 * The {@link AudlangParseResult} covers any positive or negative result (failure) when parsing a textual Audlang expression. It allows either returning a valid
 * expression or an error message with proper explanation why the parse run failed.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class AudlangParseResult extends AudlangResult {

    private static final long serialVersionUID = 6845829697785841894L;

    private PlExpression<?> resultExpression;

    /**
     * @return result expression or null if {@link #isError()}
     */
    @SuppressWarnings("java:S1452")
    public PlExpression<?> getResultExpression() {
        return resultExpression;
    }

    /**
     * @param resultExpression expression created from the parsed text
     */
    public void setResultExpression(PlExpression<?> resultExpression) {
        this.resultExpression = resultExpression;
    }

    @Override
    public String toString() {
        return "AudlangParseResult [resultExpression=" + resultExpression + ", source=" + getSource() + ", error=" + isError() + ", errorMessage="
                + getErrorMessage() + ", userMessages=" + getUserMessages() + "]";
    }

}
