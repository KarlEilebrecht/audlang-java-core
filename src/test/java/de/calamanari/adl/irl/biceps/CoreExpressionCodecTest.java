//@formatter:off
/*
 * CoreExpressionCodecTest
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

package de.calamanari.adl.irl.biceps;

import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.createIsUnknownForArgName;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.createIsUnknownForReferencedArgName;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.decodeCombinedExpressionId;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.encodeCombinedExpressionId;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.getNodeType;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.getOperator;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.haveSameArgName;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isCombinedExpressionId;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isLeftNegationOfRight;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isNegation;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isReferenceMatch;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.isSpecialSet;
import static de.calamanari.adl.irl.biceps.CoreExpressionCodec.negate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.irl.CombinedExpression;
import de.calamanari.adl.irl.CoreExpression;
import de.calamanari.adl.irl.MatchExpression;
import de.calamanari.adl.irl.MatchExpression.MatchOperator;
import de.calamanari.adl.irl.Operand;
import de.calamanari.adl.irl.SimpleExpression;
import de.calamanari.adl.irl.SpecialSetExpression;
import de.calamanari.adl.irl.biceps.CoreExpressionCodec.Dictionary;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class CoreExpressionCodecTest {

    static final Logger LOGGER = LoggerFactory.getLogger(CoreExpressionCodecTest.class);

    @Test
    void testBasics() {

        CoreExpressionCodec codec = new CoreExpressionCodec(new Dictionary(Arrays.asList("arg", "a", "b", "c"), Arrays.asList("val", "1", "2")));

        SimpleExpression expression = (SimpleExpression) MatchExpression.of("arg", MatchOperator.EQUALS, Operand.of("val", false));

        int encoded = codec.encode(expression);

        codec.clearCaches();

        CoreExpression decoded = codec.decode(encoded);

        assertEquals(expression, decoded);

        expression = (SimpleExpression) MatchExpression.of("arg", MatchOperator.EQUALS, Operand.of("c", true));

        encoded = codec.encode(expression);

        codec.clearCaches();

        decoded = codec.decode(encoded);

        assertEquals(expression, decoded);

        assertEquals("arg", codec.getArgName(encoded));
        assertEquals(expression.operand(), codec.getOperand(encoded));
        assertEquals(expression.operator(), getOperator(encoded));
        assertEquals("c", codec.getReferencedArgName(encoded));
        assertNull(codec.getValue(encoded));

        assertTrue(isReferenceMatch(encoded));

        assertFalse(isSpecialSet(encoded));
        assertTrue(isSpecialSet(codec.encode(SpecialSetExpression.all())));
        assertTrue(isSpecialSet(codec.encode(SpecialSetExpression.none())));

        SimpleExpression expression2 = (SimpleExpression) MatchExpression.of("arg", MatchOperator.EQUALS, Operand.of("val", false));

        int encoded2 = codec.encode(expression2);

        assertTrue(haveSameArgName(encoded, encoded2));

        SimpleExpression expression3 = (SimpleExpression) MatchExpression.of("arg", MatchOperator.IS_UNKNOWN, null);

        assertEquals(codec.encode(expression3), createIsUnknownForArgName(encoded2));

        expression3 = (SimpleExpression) MatchExpression.of("c", MatchOperator.IS_UNKNOWN, null);

        assertEquals(codec.encode(expression3), createIsUnknownForReferencedArgName(encoded));
        assertEquals(CoreExpressionCodec.NONE, CoreExpressionCodec.negate(CoreExpressionCodec.ALL));
        assertEquals(CoreExpressionCodec.ALL, CoreExpressionCodec.negate(CoreExpressionCodec.NONE));

    }

    @Test
    void testReferenceEncoding() {

        int[] validIds = new int[] { 0, 1, 100, 1_000, 1_000_000, 99_888_777, CoreExpressionCodec.MAX_EXTERNAL_EXPRESSION_ID };

        for (int id : validIds) {

            int encodedAnd = encodeCombinedExpressionId(id, NodeType.AND);
            assertTrue(isCombinedExpressionId(encodedAnd));
            assertEquals(NodeType.AND, getNodeType(encodedAnd));
            assertEquals(id, decodeCombinedExpressionId(encodedAnd));

            int encodedOr = encodeCombinedExpressionId(id, NodeType.OR);
            assertTrue(isCombinedExpressionId(encodedOr));
            assertEquals(NodeType.OR, getNodeType(encodedOr));
            assertEquals(id, decodeCombinedExpressionId(encodedOr));

        }

    }

    @Test
    void testOperators() {

        CoreExpressionCodec codec = new CoreExpressionCodec(new Dictionary(Arrays.asList("arg", "a", "b", "c"), Arrays.asList("val", "1", "2")));

        for (MatchOperator op : MatchOperator.values()) {
            Operand operand = null;
            if (op != MatchOperator.IS_UNKNOWN) {
                operand = Operand.of("val", false);
            }
            SimpleExpression expression = (SimpleExpression) MatchExpression.of("c", op, operand);
            int encoded = codec.encode(expression);

            codec.clearCaches();

            CoreExpression decoded = codec.decode(encoded);

            assertEquals(expression, decoded);

        }

    }

    @Test
    void testNegation() {

        CoreExpressionCodec codec = new CoreExpressionCodec(new Dictionary(Arrays.asList("arg", "a", "b", "c"), Arrays.asList("val", "1", "2")));

        SimpleExpression expression = (SimpleExpression) MatchExpression.of("arg", MatchOperator.EQUALS, Operand.of("val", false));

        int encoded = codec.encode(expression);

        int backup = encoded;

        encoded = negate(encoded);

        assertTrue(isLeftNegationOfRight(encoded, backup));
        assertTrue(isLeftNegationOfRight(backup, encoded));

        assertTrue(isNegation(encoded));

        codec.clearCaches();

        CoreExpression decoded = codec.decode(encoded);

        assertEquals(expression.negate(true), decoded);

        encoded = negate(encoded);
        codec.clearCaches();

        decoded = codec.decode(encoded);

        assertEquals(expression, decoded);

    }

    @Test
    void testScanFields() {
        SimpleExpression expression = (SimpleExpression) MatchExpression.of("arg", MatchOperator.EQUALS, Operand.of("val", false));

        CoreExpressionCodec codec = new CoreExpressionCodec(new Dictionary(expression.allFields()));
        int encoded = codec.encode(expression);

        codec.clearCaches();

        CoreExpression decoded = codec.decode(encoded);

        assertEquals(expression, decoded);

        expression = (SimpleExpression) MatchExpression.of("arg", MatchOperator.EQUALS, Operand.of("a", true));

        codec = new CoreExpressionCodec(new Dictionary(expression.allFields()));
        encoded = codec.encode(expression);

        codec.clearCaches();

        decoded = codec.decode(encoded);

        assertEquals(expression, decoded);

    }

    @Test
    void testSpecialCases() {
        CoreExpressionCodec codec = new CoreExpressionCodec(new Dictionary(Arrays.asList("arg", "a", "b", "c"), Arrays.asList("val", "1", "2")));

        SimpleExpression expression = (SimpleExpression) MatchExpression.of("arg", MatchOperator.EQUALS, Operand.of("bla", true));
        SimpleExpression expression2 = (SimpleExpression) MatchExpression.of("arg", MatchOperator.EQUALS, Operand.of("bla", false));

        assertThrows(ExpressionCodecException.class, () -> codec.encode(expression));
        assertThrows(ExpressionCodecException.class, () -> codec.encode(expression2));

        int combined = CoreExpressionCodec.encodeCombinedExpressionId(25, NodeType.AND);
        assertThrows(ExpressionCodecException.class, () -> CoreExpressionCodec.encodeCombinedExpressionId(-25, NodeType.AND));
        assertThrows(ExpressionCodecException.class, () -> CoreExpressionCodec.encodeCombinedExpressionId(200_000_000, NodeType.OR));
        assertThrows(ExpressionCodecException.class, () -> CoreExpressionCodec.encodeCombinedExpressionId(25, null));
        assertThrows(ExpressionCodecException.class, () -> CoreExpressionCodec.encodeCombinedExpressionId(25, NodeType.LEAF));
        assertThrows(ExpressionCodecException.class, () -> CoreExpressionCodec.decodeCombinedExpressionId(250));
        assertThrows(ExpressionCodecException.class, () -> CoreExpressionCodec.decodeCombinedExpressionId(CoreExpressionCodec.INVALID));

        assertThrows(ExpressionCodecException.class, () -> CoreExpressionCodec.createIsUnknownForArgName(combined));
        assertThrows(ExpressionCodecException.class, () -> CoreExpressionCodec.createIsUnknownForReferencedArgName(combined));
        assertThrows(ExpressionCodecException.class, () -> CoreExpressionCodec.negate(combined));
        assertThrows(ExpressionCodecException.class, () -> CoreExpressionCodec.negate(CoreExpressionCodec.INVALID));
        assertThrows(ExpressionCodecException.class, () -> CoreExpressionCodec.createIsUnknownForArgName(CoreExpressionCodec.NONE));
        assertThrows(ExpressionCodecException.class, () -> CoreExpressionCodec.createIsUnknownForReferencedArgName(CoreExpressionCodec.NONE));

    }

    @Test
    void testSpecialCases2() {
        CoreExpressionCodec codec = new CoreExpressionCodec(new Dictionary(Arrays.asList("arg", "a", "b", "c"), Arrays.asList("val", "1", "2")));

        assertSame(codec, codec.merge(codec));

        SimpleExpression expression = (SimpleExpression) MatchExpression.of("arg", MatchOperator.EQUALS, Operand.of("bla", true));

        assertThrows(ExpressionCodecException.class, () -> codec.encode(expression));

        CoreExpression orExpression = CombinedExpression.orOf(expression, expression.negate(false));

        int combined = CoreExpressionCodec.encodeCombinedExpressionId(25, NodeType.AND);

        assertThrows(ExpressionCodecException.class, () -> codec.getArgName(combined));
        assertThrows(ExpressionCodecException.class, () -> codec.getArgName(CoreExpressionCodec.INVALID));
        assertNull(codec.getArgName(CoreExpressionCodec.ALL));
        assertThrows(ExpressionCodecException.class, () -> codec.getReferencedArgName(combined));
        assertThrows(ExpressionCodecException.class, () -> codec.getReferencedArgName(CoreExpressionCodec.INVALID));
        assertNull(codec.getReferencedArgName(CoreExpressionCodec.NONE));
        assertThrows(ExpressionCodecException.class, () -> codec.getValue(combined));
        assertThrows(ExpressionCodecException.class, () -> codec.getValue(CoreExpressionCodec.INVALID));
        assertNull(codec.getValue(CoreExpressionCodec.ALL));
        assertThrows(ExpressionCodecException.class, () -> codec.getOperand(combined));
        assertThrows(ExpressionCodecException.class, () -> codec.getOperand(CoreExpressionCodec.INVALID));
        assertNull(codec.getOperand(CoreExpressionCodec.ALL));

        assertThrows(ExpressionCodecException.class, () -> codec.encode(orExpression));
        assertThrows(ExpressionCodecException.class, () -> codec.decode(CoreExpressionCodec.INVALID));

        assertThrows(ExpressionCodecException.class, () -> codec.getArgName(28376283));
        assertThrows(ExpressionCodecException.class, () -> codec.getValue(-1_983_099_373));

    }

    public static final String binStr(int i) {
        String res = "00000000000000000000000000000000" + Integer.toBinaryString(i);

        return res.substring(res.length() - 32);
    }

}
