//@formatter:off
/*
 * AudlangResult
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

import java.io.Serializable;

/**
 * The {@link AudlangResult} is the common and base class to return results (success <i>and</i> error) when processing an Audlang expression.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public class AudlangResult implements Serializable {

    private static final long serialVersionUID = 3345829697785841127L;

    private boolean error = false;

    private String errorMessage = null;

    private AudlangErrorInfo errorInfo = null;

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
     * @return optional error info with user message and details
     */
    public AudlangErrorInfo getErrorInfo() {
        return errorInfo;
    }

    /**
     * @param errorInfo optional error info with user message and details
     */
    public void setErrorInfo(AudlangErrorInfo errorInfo) {
        this.errorInfo = errorInfo;
    }

    @Override
    public String toString() {
        return "AudlangResult [source=" + source + ", error=" + error + ", errorMessage=" + errorMessage + ", errorInfo=" + errorInfo + "]";
    }

}