//@formatter:off
/*
 * PlSingleOperandTest
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

import java.util.Collections;

import org.junit.jupiter.api.Test;

import de.calamanari.adl.AudlangValidationException;

import static de.calamanari.adl.erl.PlComment.Position.BEFORE_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_AFTER_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_AFTER_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_BEFORE_EXPR;
import static de.calamanari.adl.erl.SamplePlExpressions.COMMENT_BEFORE_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.SINGLE_COMMENT_C1;
import static de.calamanari.adl.erl.SamplePlExpressions.TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND;
import static de.calamanari.adl.erl.SamplePlExpressions.comments;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class PlOperandTest {

    @Test
    void testSpecialCases() {

        assertThrows(AudlangValidationException.class, () -> new PlOperand(null, false, null));

        assertThrows(AudlangValidationException.class, () -> new PlOperand("value", false, COMMENT_BEFORE_EXPR));
        assertThrows(AudlangValidationException.class, () -> new PlOperand("value", false, COMMENT_AFTER_EXPR));
        assertThrows(AudlangValidationException.class, () -> new PlOperand("value", false, SINGLE_COMMENT_C1));

        PlOperand operand = new PlOperand("x", false, null);

        assertSame(operand, operand.stripComments());

        assertSame(operand, operand.withComments(Collections.emptyList()));

        operand = new PlOperand("x", false, comments(BEFORE_OPERAND, "/* comment */"));

        assertSame(operand, operand.withComments(comments(BEFORE_OPERAND, "/* comment */")));

        assertNotEquals(operand, operand.withComments(comments(BEFORE_OPERAND, "/* comment1 */")));

    }

    @Test
    void testCommentHandling() {
        PlOperand op = new PlOperand("x", false, null);

        PlOperand op2 = new PlOperand("x", false, null);

        assertEquals(op, op2);
        assertEquals(op, op.stripComments());

        op2 = op2.withComments(TWO_COMMENTS_BEFORE_AND_AFTER_OPERAND);

        assertNotEquals(op, op2);

        assertEquals(op, op2.stripComments());

        assertSame(op, op.withComments(null));
        assertSame(op, op.withComments(Collections.emptyList()));

        PlOperand op3 = op.withComments(COMMENT_BEFORE_OPERAND);

        assertSame(op3, op3.withComments(COMMENT_BEFORE_OPERAND));

        PlOperand op4 = op.withComments(COMMENT_AFTER_OPERAND);

        assertSame(op4, op4.withComments(COMMENT_AFTER_OPERAND));

        assertEquals(op4.allComments(), op4.allDirectComments());

    }
}
