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

/**
 * The {@link AudlangParseResult} covers any positive or negative result (failure) when parsing a textual Audlang expression. It allows either returning a valid
 * expression or an error message with proper explanation why the parse run failed.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class AudlangParseResult {

    private boolean error = false;

    private String errorMessage = null;

    private PlExpression<?> resultExpression;

    private String source = null;

    /**
     * @return original text that was parsed
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source original text that was parsed
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return true if this parse run failed, so there is no {@link #getResultExpression()}
     */
    public boolean isError() {
        return error;
    }

    /**
     * @param error true to indicate a failed parse run
     */
    public void setError(boolean error) {
        this.error = error;
    }

    /**
     * @return error message from a failed parse run
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage message related to the failed parse run
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

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
        return this.getClass().getSimpleName() + " [error=" + error + ", errorMessage=" + errorMessage + ", source=" + source + ", resultExpression="
                + resultExpression + "]";
    }

}
