//@formatter:off
/*
 * PlCommentConstraint
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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static de.calamanari.adl.erl.PlComment.Position.AFTER_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.BEFORE_EXPRESSION;
import static de.calamanari.adl.erl.PlComment.Position.C1;
import static de.calamanari.adl.erl.PlComment.Position.C2;
import static de.calamanari.adl.erl.PlComment.Position.C3;
import static de.calamanari.adl.erl.PlComment.Position.C4;
import static de.calamanari.adl.erl.PlComment.Position.C5;
import static de.calamanari.adl.erl.PlComment.Position.C6;

/**
 * Comments can only appear between certain tokens of the Audlang, and not every operator has the same number of tokens so that the number of available slots to
 * place a comment depends on the operator. This is covered by the constraint.
 */
public enum PlCommentConstraint {

    ONE_INTERNAL_COMMENT(C1),
    TWO_INTERNAL_COMMENTS(C1, C2),
    THREE_INTERNAL_COMMENTS(C1, C2, C3),
    FOUR_INTERNAL_COMMENTS(C1, C2, C3, C4),
    FIVE_INTERNAL_COMMENTS(C1, C2, C3, C4, C5),
    SIX_INTERNAL_COMMENTS(C1, C2, C3, C4, C5, C6);

    private final Set<PlComment.Position> validPositions;

    private final String message;

    private PlCommentConstraint(PlComment.Position... validPositions) {
        LinkedHashSet<PlComment.Position> set = new LinkedHashSet<>();
        set.add(BEFORE_EXPRESSION);
        set.addAll(Arrays.asList(validPositions));
        set.add(AFTER_EXPRESSION);
        this.validPositions = Collections.unmodifiableSet(set);
        this.message = String.format("Only the following comment positions are valid: %s", this.validPositions);
    }

    /**
     * @return error message containing the allowed positions
     */
    public String getMessage() {
        return message;
    }

    /**
     * Checks if the comment is acceptable at the given position
     * 
     * @param comment
     * @return true if comment can be placed
     */
    public boolean verify(PlComment comment) {
        return validPositions.contains(comment.position());
    }
}