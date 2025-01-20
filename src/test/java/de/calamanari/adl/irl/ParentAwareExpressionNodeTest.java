//@formatter:off
/*
 * ParentAwareExpressionNodeTest
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

package de.calamanari.adl.irl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.FormatStyle;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class ParentAwareExpressionNodeTest {

    static final Logger LOGGER = LoggerFactory.getLogger(ParentAwareExpressionNodeTest.class);

    @Test
    void testBasics() {

        CoreExpression red = MatchExpression.of("color", MatchOperator.EQUALS, Operand.of("red", false));
        CoreExpression blue = MatchExpression.of("color", MatchOperator.EQUALS, Operand.of("blue", false));
        CoreExpression yellow = MatchExpression.of("color", MatchOperator.EQUALS, Operand.of("yellow", false));
        CoreExpression green = MatchExpression.of("color", MatchOperator.EQUALS, Operand.of("green", false));
        CoreExpression black = MatchExpression.of("color", MatchOperator.EQUALS, Operand.of("black", false));

        CoreExpression parent0 = CombinedExpression.andOf(red, yellow);

        CoreExpression parent1 = CombinedExpression.orOf(parent0, green);

        CoreExpression parent2 = CombinedExpression.orOf(yellow, blue);

        CoreExpression parent3 = CombinedExpression.andOf(parent1, parent2);

        CoreExpression parent4 = CombinedExpression.orOf(parent3, red);

        assertEquals("""
                color = red
                OR (
                        (
                            color = blue
                         OR color = yellow
                        )
                    AND (
                            color = green
                         OR (
                                color = red
                            AND color = yellow
                            )
                        )
                    )""", parent4.format(FormatStyle.PRETTY_PRINT));

        List<ParentAwareExpressionNode> leafNodes = ParentAwareExpressionNode.collectLeafNodes(parent4);

        List<ParentAwareExpressionNode> leafNodesUnrelated = ParentAwareExpressionNode.collectLeafNodes(black);

        String leafPaths = leafNodes.stream().map(ParentAwareExpressionNode::toString).collect(Collectors.joining("\n\n"));

        assertEquals("""
                color = red OR ( (color = blue OR color = yellow) AND (color = green OR (color = red AND color = yellow) ) )
                    + color = red

                color = red OR ( (color = blue OR color = yellow) AND (color = green OR (color = red AND color = yellow) ) )
                    + (color = blue OR color = yellow) AND (color = green OR (color = red AND color = yellow) )
                        + color = blue OR color = yellow
                            + color = blue

                color = red OR ( (color = blue OR color = yellow) AND (color = green OR (color = red AND color = yellow) ) )
                    + (color = blue OR color = yellow) AND (color = green OR (color = red AND color = yellow) )
                        + color = blue OR color = yellow
                            + color = yellow

                color = red OR ( (color = blue OR color = yellow) AND (color = green OR (color = red AND color = yellow) ) )
                    + (color = blue OR color = yellow) AND (color = green OR (color = red AND color = yellow) )
                        + color = green OR (color = red AND color = yellow)
                            + color = green

                color = red OR ( (color = blue OR color = yellow) AND (color = green OR (color = red AND color = yellow) ) )
                    + (color = blue OR color = yellow) AND (color = green OR (color = red AND color = yellow) )
                        + color = green OR (color = red AND color = yellow)
                            + color = red AND color = yellow
                                + color = red

                color = red OR ( (color = blue OR color = yellow) AND (color = green OR (color = red AND color = yellow) ) )
                    + (color = blue OR color = yellow) AND (color = green OR (color = red AND color = yellow) )
                        + color = green OR (color = red AND color = yellow)
                            + color = red AND color = yellow
                                + color = yellow""", leafPaths);

        // 2x red, 2x yellow
        assertEquals(6, leafNodes.size());

        ParentAwareExpressionNode redNode1 = null;
        ParentAwareExpressionNode redNode2 = null;
        ParentAwareExpressionNode yellowNode1 = null;
        ParentAwareExpressionNode yellowNode2 = null;
        ParentAwareExpressionNode blueNode = null;
        ParentAwareExpressionNode greenNode = null;
        ParentAwareExpressionNode blackNode = leafNodesUnrelated.get(0);

        for (ParentAwareExpressionNode node : leafNodes) {
            if (node.expression().equals(red) && redNode1 == null) {
                redNode1 = node;
            }
            else if (node.expression().equals(red)) {
                redNode2 = node;
            }
            else if (node.expression().equals(yellow) && yellowNode1 == null) {
                yellowNode1 = node;
            }
            else if (node.expression().equals(yellow)) {
                yellowNode2 = node;
            }
            else if (node.expression().equals(blue)) {
                blueNode = node;
            }
            else if (node.expression().equals(green)) {
                greenNode = node;
            }
        }

        assertEquals(parent4, redNode1.findNearestCommonParent(greenNode).expression());
        assertEquals(parent1, redNode2.findNearestCommonParent(greenNode).expression());
        assertEquals(parent3, yellowNode1.findNearestCommonParent(greenNode).expression());
        assertEquals(parent1, yellowNode2.findNearestCommonParent(greenNode).expression());

        assertFalse(redNode1.hasCommonAndParentWith(greenNode));
        assertTrue(redNode2.hasCommonAndParentWith(blueNode));

        assertNull(greenNode.findNearestCommonParent(blackNode));
        assertFalse(redNode1.hasCommonAndParentWith(blackNode));
        assertFalse(blackNode.hasCommonAndParentWith(blackNode));
        assertFalse(blackNode.hasCommonAndParentWith(null));

    }

}
