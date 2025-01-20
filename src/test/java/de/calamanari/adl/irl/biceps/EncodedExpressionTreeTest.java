//@formatter:off
/*
 * EncodedExpressionTreeTest
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.calamanari.adl.DeepCopyUtils;
import de.calamanari.adl.cnv.StandardConversions;
import de.calamanari.adl.irl.CoreExpression;

/**
 * @author <a href="mailto:Karl.Eilebrecht(a/t)calamanari.de">Karl Eilebrecht</a>
 */
class EncodedExpressionTreeTest {

    static final Logger LOGGER = LoggerFactory.getLogger(EncodedExpressionTreeTest.class);

    @Test
    void testNodeFromToCoreExpression() {

        CoreExpression expr = StandardConversions.parseCoreExpression("a = 1 AND (b = 4 OR (c = 8 AND (d = 9 OR e = foo)))");

        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(expr);

        assertEquals(expr, tree.toCoreExpression());

        CoreExpression expr1 = StandardConversions.parseCoreExpression("a = 1");

        assertEquals(tree.createNode(expr1), tree.createNode(expr1));

        assertEquals(tree.membersOf(tree.getRootNode())[0], tree.createNode(expr1));

        CoreExpression expr2 = StandardConversions.parseCoreExpression("(b = 4 OR (c = 8 AND (d = 9 OR e = foo)))");

        assertEquals(tree.createNode(expr2), tree.createNode(expr2));

        assertEquals(tree.membersOf(tree.getRootNode())[1], tree.createNode(expr2));

    }

    @Test
    void testCopy() {

        CoreExpression expr = StandardConversions.parseCoreExpression("a = 1 AND (b = 4 OR (c = 8 AND (d = 9 OR e = foo)))");

        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(expr);

        EncodedExpressionTree tree2 = tree.copy();

        // due to caching tree does not implement equals
        assertNotEquals(tree, tree2);

        assertEquals(tree.getRootNode(), tree2.getRootNode());

        assertEquals(expr, tree2.toCoreExpression());

        CoreExpression expr2 = StandardConversions.parseCoreExpression("a = 1 OR b = 4 OR (c = 8 AND (d = 9 OR e = foo))");

        assertEquals(tree.createNode(expr2), tree2.createNode(expr2));

    }

    @Test
    void testSerialization() {

        CoreExpression expr = StandardConversions.parseCoreExpression("a = 1 AND (b = 4 OR (c = 8 AND (d = 9 OR e = foo)))");

        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(expr);

        EncodedExpressionTree tree2 = DeepCopyUtils.deepCopy(tree);

        // due to caching tree does not implement equals
        assertNotEquals(tree, tree2);

        assertEquals(tree.getRootNode(), tree2.getRootNode());

        assertEquals(expr, tree2.toCoreExpression());

        CoreExpression expr2 = StandardConversions.parseCoreExpression("a = 1 OR b = 4 OR (c = 8 AND (d = 9 OR e = foo))");

        assertEquals(tree.createNode(expr2), tree2.createNode(expr2));

    }

    @Test
    void testInitialize() {

        CoreExpression expr = StandardConversions.parseCoreExpression("a = 1 AND (b = 4 OR (c = 8 AND (d = 9 OR e = foo)))");

        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(expr);

        EncodedExpressionTree tree2 = tree.copy();

        tree2.initialize(null);

        assertEquals(expr, tree.toCoreExpression());

        assertThrows(IllegalStateException.class, tree2::toCoreExpression);

        tree2.initialize(tree.getCodec());

        assertEquals(tree.getRootNode(), tree2.createNode(expr));
        assertThrows(IllegalStateException.class, tree2::toCoreExpression);

        tree2.setRootNode(tree.getRootNode());

        assertEquals(expr, tree2.toCoreExpression());

    }

    @Test
    void testNestingDepth() {

        CoreExpression expr = StandardConversions.parseCoreExpression("a = 1 AND (b = 4 OR (c = 8 AND (d = 9 OR e = foo)))");

        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(expr);

        assertEquals(4, tree.nestingDepthOf(tree.getRootNode()));

        CoreExpression expr1 = StandardConversions.parseCoreExpression("a = 1");

        assertEquals(0, tree.nestingDepthOf(tree.createNode(expr1)));

        expr1 = StandardConversions.parseCoreExpression("(c = 8 AND (d = 9 OR e = foo))");

        assertEquals(2, tree.nestingDepthOf(tree.createNode(expr1)));

    }

    @Test
    void testCreateNode() {

        CoreExpression expr = StandardConversions.parseCoreExpression("a = 1 AND (b = 4 OR (c = 8 AND (d = 9 OR e = foo)))");

        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(expr);

        int node1 = tree.createNode(StandardConversions.parseCoreExpression("a = 1"));
        int node2 = tree.createNode(StandardConversions.parseCoreExpression("(b = 4 OR (c = 8 AND (d = 9 OR e = foo)))"));

        int rootNode = tree.createNode(NodeType.AND, new int[] { node1, node2 });

        assertEquals(tree.getRootNode(), rootNode);

    }

    @Test
    void testConsolidateMembers() {

        CoreExpression expr = StandardConversions.parseCoreExpression("a = 1 AND (b = 4 OR (c = 8 AND (d = 9 OR e = foo)))");

        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(expr);

        int node1 = tree.createNode(StandardConversions.parseCoreExpression("a = 1"));
        int node2 = tree.createNode(StandardConversions.parseCoreExpression("(b = 4 OR (c = 8 AND (d = 9 OR e = foo)))"));
        int node3 = tree.createNode(StandardConversions.parseCoreExpression("(b = 4 AND (c = 8 OR (d = 9 AND e = foo)))"));
        int node4 = tree.createNode(StandardConversions.parseCoreExpression("b = 4"));
        int node5 = tree.createNode(StandardConversions.parseCoreExpression("( ( d = 9 OR e = foo ) AND c = 8 )"));

        int[] before = new int[] { node2, node1, node1, node2, CoreExpressionCodec.INVALID, CoreExpressionCodec.ALL };

        assertArrayEquals(new int[] { node1, node2 }, tree.consolidateMembers(NodeType.AND, before));

        before = new int[] { node2, node1, node1, node2, CoreExpressionCodec.INVALID, CoreExpressionCodec.NONE };

        assertArrayEquals(new int[] { CoreExpressionCodec.NONE }, tree.consolidateMembers(NodeType.AND, before));

        before = new int[] { node3, node1, node1, node3, CoreExpressionCodec.INVALID, CoreExpressionCodec.NONE };

        assertArrayEquals(new int[] { node1, node3 }, tree.consolidateMembers(NodeType.OR, before));

        before = new int[] { node3, node1, node1, node3, CoreExpressionCodec.INVALID, CoreExpressionCodec.ALL };

        assertArrayEquals(new int[] { CoreExpressionCodec.ALL }, tree.consolidateMembers(NodeType.OR, before));

        before = new int[] { node2, node1, node1, node2, CoreExpressionCodec.INVALID, CoreExpressionCodec.NONE };

        assertArrayEquals(new int[] { node1, node5, node4 }, tree.consolidateMembers(NodeType.OR, before));

    }

    @Test
    void testConsolidateMembers2() {

        CoreExpression expr = StandardConversions.parseCoreExpression("a = 1 AND (b = 4 OR (c = 8 AND (d = 9 OR e = foo)))");

        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(expr);

        int node1 = tree.createNode(StandardConversions.parseCoreExpression("a = 1"));
        int node2 = tree.createNode(StandardConversions.parseCoreExpression("(b = 4 OR (c = 8 AND (d = 9 OR e = foo)))"));
        int node3 = tree.createNode(StandardConversions.parseCoreExpression("(b = 4 AND (c = 8 OR (d = 9 AND e = foo)))"));
        int node4 = tree.createNode(StandardConversions.parseCoreExpression("b = 4"));
        int node5 = tree.createNode(StandardConversions.parseCoreExpression("( ( d = 9 OR e = foo ) AND c = 8 )"));

        GrowingIntArray before = new GrowingIntArray(new int[] { node2, node1, node1, node2, CoreExpressionCodec.INVALID, CoreExpressionCodec.ALL }, false);

        assertArrayEquals(new int[] { node1, node2 }, tree.consolidateMembers(NodeType.AND, before));

        before = new GrowingIntArray(new int[] { node2, node1, node1, node2, CoreExpressionCodec.INVALID, CoreExpressionCodec.NONE }, false);

        assertArrayEquals(new int[] { CoreExpressionCodec.NONE }, tree.consolidateMembers(NodeType.AND, before));

        before = new GrowingIntArray(new int[] { node3, node1, node1, node3, CoreExpressionCodec.INVALID, CoreExpressionCodec.NONE }, false);

        assertArrayEquals(new int[] { node1, node3 }, tree.consolidateMembers(NodeType.OR, before));

        before = new GrowingIntArray(new int[] { node3, node1, node1, node3, CoreExpressionCodec.INVALID, CoreExpressionCodec.ALL }, false);

        assertArrayEquals(new int[] { CoreExpressionCodec.ALL }, tree.consolidateMembers(NodeType.OR, before));

        before = new GrowingIntArray(new int[] { node2, node1, node1, node2, CoreExpressionCodec.INVALID, CoreExpressionCodec.NONE }, false);

        assertArrayEquals(new int[] { node1, node5, node4 }, tree.consolidateMembers(NodeType.OR, before));

        before = new GrowingIntArray(new int[] { node1 }, false);

        assertArrayEquals(new int[] { node1 }, tree.consolidateMembers(NodeType.OR, before));

    }

    @Test
    void testMerge() {

        CoreExpression expr1 = StandardConversions.parseCoreExpression("a = 1 AND b > 5 AND e = foo");

        CoreExpression expr2 = StandardConversions.parseCoreExpression("(b = 4 OR (c = 8 AND (d = 9 OR e = foo)))");

        EncodedExpressionTree tree1 = EncodedExpressionTree.fromCoreExpression(expr1);

        EncodedExpressionTree tree2 = EncodedExpressionTree.fromCoreExpression(expr2);

        assertArrayEquals(new int[] { toNode(tree1, "a = 1"), toNode(tree1, "b > 5"), toNode(tree1, "e = foo") }, tree1.collectLeaves(tree1.getRootNode()));

        assertArrayEquals(new int[] { toNode(tree2, "b = 4"), toNode(tree2, "c = 8"), toNode(tree2, "d = 9"), toNode(tree2, "e = foo") },
                tree2.collectLeaves(tree2.getRootNode()));

        EncodedExpressionTree tree3 = tree1.merge(tree1);

        assertNotEquals(tree1, tree3);

        assertArrayEquals(new int[] { tree1.getRootNode(), tree1.getRootNode() }, tree3.getRootLevel().members().toArray());

        EncodedExpressionTree tree4 = tree1.merge(tree2);

        assertThrows(IllegalStateException.class, tree4::getRootNode);

        assertEquals(tree1.getRootNode(), tree4.getRootLevel().members().get(0));

        assertNotEquals(tree2.getRootNode(), tree4.getRootLevel().members().get(1));

        int node1 = tree4.createNode(expr1);
        int node2 = tree4.createNode(expr2);
        int node3 = tree4.createNode(NodeType.AND, new int[] { node1, node2 });
        tree4.setRootNode(node3);

        assertEquals("a = 1 AND b > 5 AND e = foo AND (b = 4 OR (c = 8 AND (d = 9 OR e = foo) ) )", tree4.toCoreExpression().toString());

        // merge inherits combined nodes from tree1
        assertArrayEquals(new int[] { toNode(tree1, "a = 1"), toNode(tree1, "b > 5"), toNode(tree4, "b = 4"), toNode(tree1, "e = foo"), toNode(tree4, "c = 8"),
                toNode(tree4, "d = 9") }, tree4.collectLeaves(tree4.getRootNode()));

        CoreExpression exprNone = StandardConversions.parseCoreExpression("<NONE>");

        EncodedExpressionTree treeNone = EncodedExpressionTree.fromCoreExpression(exprNone);

        EncodedExpressionTree tree5 = tree1.merge(treeNone);

        assertEquals(toNode(tree1, "a = 1 OR b > 5 OR e = foo OR <NONE>"), toNode(tree5, "a = 1 OR b > 5 OR e = foo OR <NONE>"));

        assertArrayEquals(new int[] { toNode(tree1, "a = 1") }, tree1.collectLeaves(tree1.membersOf(tree1.getRootNode())[0]));

    }

    @Test
    void testSpecialCases() {

        EncodedExpressionTree tree = new EncodedExpressionTree();

        CoreExpression expr1 = StandardConversions.parseCoreExpression("a = 1 AND b > 5 AND e = foo");

        assertThrows(IllegalStateException.class, tree::getRootNode);
        assertThrows(IllegalStateException.class, () -> tree.setRootNode(2736));
        assertThrows(IllegalStateException.class, () -> tree.createNode(expr1));

        EncodedExpressionTree tree1 = EncodedExpressionTree.fromCoreExpression(expr1);

        tree.createTreeLevel();
        tree.setRootNode(tree1.getRootNode());

        assertEquals("( !?[" + tree.getRootNode() + "]?! )", tree.createDebugString(tree.getRootNode()));

        int nodeA = toNode(tree1, "a = 1");

        tree.setRootNode(nodeA);

        assertEquals("" + nodeA, tree.createDebugString(tree.getRootNode()));

        tree.setRootNode(CoreExpressionCodec.INVALID);

        assertEquals("INVALID", tree.createDebugString(tree.getRootNode()));

    }

    @Test
    void testBug3() {
        CoreExpression expr = StandardConversions.parseCoreExpression("(color.1 = red OR color.1 = blue) AND color.3 = yellow");
        CoreExpression expr2 = StandardConversions.parseCoreExpression("(color.1 = red OR color.1 = blue)");

        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(expr);

        int node1 = tree.getRootNode();

        int node2 = tree.createNode(expr2);

        assertTrue(tree.getLogicHelper().leftImpliesRight(node1, node2));

    }

    @Test
    void testCreateDebugString() {
        CoreExpression expr = StandardConversions.parseCoreExpression("a = 1 AND (b = 4 OR (c = 8 AND (d = 9 OR e = foo)))");

        EncodedExpressionTree tree = EncodedExpressionTree.fromCoreExpression(expr);

        assertEquals("( a = 1 AND ( ( ( d = 9 OR e = foo ) AND c = 8 ) OR b = 4 ) )", tree.createDebugString(tree.getRootNode()));
        assertEquals("a = 1, ( ( ( d = 9 OR e = foo ) AND c = 8 ) OR b = 4 )", tree.createDebugString(tree.membersOf(tree.getRootNode())));

    }

    private static int toNode(EncodedExpressionTree tree, String expressionString) {
        return tree.createNode(StandardConversions.parseCoreExpression(expressionString));
    }

}
