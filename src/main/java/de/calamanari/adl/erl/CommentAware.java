//@formatter:off
/*
 * CommentAware
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

import java.util.ArrayList;
import java.util.List;

/**
 * Interface to be implemented by language elements of the presentation layer that can carry comments.
 * 
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
public interface CommentAware {

    /**
     * @return List with all comments found in this exception and its child elements
     */
    default List<PlComment> allComments() {
        List<PlComment> res = new ArrayList<>();
        collectAllComments(res);
        return res;
    }

    /**
     * @return List with all comments found on this element
     */
    List<PlComment> allDirectComments();

    /**
     * Collects all comments recursively
     * 
     * @param result to be filled
     */
    default void collectAllComments(List<PlComment> result) {
        // no-op
    }

}
